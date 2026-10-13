#!/bin/sh
# Android visual-flow smoke: capture actual home, gameplay and collision result.
set -eu
mkdir -p qa/screenshots
adb wait-for-device
adb install -r qa/apk/app-debug.apk
# Debug-only QA: keep the real first active frame stable through slow Pixel
# screencap calls. GameSimulation resumes with a second genuine flap, and the
# production (non-debuggable) build ignores this Intent extra entirely.
adb shell am start -W -n com.brendigo.bopavi/.MainActivity --ez bopavi_qa_capture_first_flap true
sleep 3
adb shell pidof com.brendigo.bopavi

capture(){
  file="qa/screenshots/$1.png"
  attempt=1
  while [ "$attempt" -le 4 ]; do
    if adb exec-out screencap -p > "$file" &&
       python3 - "$file" <<'PY'
import sys
from pathlib import Path
path=Path(sys.argv[1])
assert path.stat().st_size>2000
assert path.read_bytes()[:8]==bytes.fromhex("89504e470d0a1a0a")
PY
    then
      echo "PASS: screenshot $file"
      return 0
    fi
    echo "Screenshot attempt $attempt failed for $file" >&2
    attempt=$((attempt+1))
    sleep 3
  done
  adb logcat -d -t 180 | tail -n 75 || true
  return 1
}

# Find the real accessible IGRAJ view instead of guessing a screen percentage.
dump_ui(){
  adb shell uiautomator dump /sdcard/bopavi-window.xml >/dev/null 2>&1
  adb exec-out cat /sdcard/bopavi-window.xml > qa/screenshots/android-current-ui.xml
  test -s qa/screenshots/android-current-ui.xml
}
# The hosted Pixel emulator can show a *system launcher* ANR over a healthy
# BOPAVI Activity. Recover only that exact dialog; never dismiss a BOPAVI ANR.
# The next assertion still requires the real destination screen to be visible.
recover_launcher_anr(){
  adb shell pidof com.brendigo.bopavi >/dev/null || return 1
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re, subprocess, sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
titles=[n.get("text","") for n in root.iter("node")
        if n.get("resource-id")=="android:id/alertTitle"]
waits=[n for n in root.iter("node") if n.get("resource-id")=="android:id/aerr_wait"
       and n.get("text")=="Wait" and n.get("clickable")=="true"]
if titles != ["Pixel Launcher isn't responding"] or len(waits)!=1:
    sys.exit(1)
m=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',waits[0].get("bounds",""))
if not m:
    raise SystemExit("FAIL: invalid system launcher Wait button bounds")
left,top,right,bottom=map(int,m.groups())
# Pressing Wait leaves the hung launcher alive, so its ANR repeatedly
# covers the game and consumes gallery gestures. Terminate only this
# verified SYSTEM launcher process; never terminate or hide BOPAVI.
print("Stopping confirmed unresponsive Pixel Launcher (not BOPAVI)",flush=True)
subprocess.run(["adb","shell","am","force-stop","com.google.android.apps.nexuslauncher"],check=True)
subprocess.run(["adb","shell","pidof","com.brendigo.bopavi"],check=True,
               stdout=subprocess.DEVNULL)
PY
}
verify_three_home_actions(){
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
# The reference shows the header gear *and* the central SETTINGS CTA.
# Both must be independently clickable, instead of rejecting valid UX.
expected={"IGRAJ":1,"SVJETOVI":1,"POSTAVKE":2}
nodes=[n for n in root.iter('node') if n.get('clickable')=='true']
for name,count in expected.items():
    matches=[n for n in nodes if name in (n.get('text','')+' '+n.get('content-desc',''))]
    if len(matches)!=count:
        raise SystemExit(f'FAIL: home requires {count} accessible {name} actions, got {len(matches)}')
    for entry in matches:
        bounds=entry.get('bounds','')
        if not bounds or bounds.startswith('[0,0][0,0]'):
            raise SystemExit(f'FAIL: {name} has invalid button bounds')
# Confirm other new header actions are individually accessible.
for label in ('PROFIL ·','ZADACI I POSTIGNUĆA','TRGOVINA ·'):
    found=[n for n in nodes if label in n.get('content-desc','')]
    if len(found)!=1:
        raise SystemExit(f'FAIL: missing actual tappable home header {label}: {len(found)}')
# Settings gear and middle settings CTA must have separate screen rectangles.
settings=[n.get('bounds') for n in nodes if 'POSTAVKE' in
          (n.get('text','')+' '+n.get('content-desc',''))]
if len(set(settings))!=2:
    raise SystemExit('FAIL: top settings gear overlaps central settings CTA')
# Check the actual captured pixels against accessible hit rectangles.
# Passing a label-only UI smoke is not sufficient if an action is clipped,
# overlaps another action, or is too small to tap on a compact phone.
import re, struct, subprocess
from pathlib import Path
image=Path('qa/screenshots/android-home.png').read_bytes()
if image[:8] != bytes.fromhex('89504e470d0a1a0a'):
    raise SystemExit('FAIL: Home QA requires a real PNG screenshot')
width,height=struct.unpack('>II',image[16:24])
density_output=subprocess.check_output(['adb','shell','wm','density'],text=True)
densities=re.findall(r'(?:Physical|Override) density:\\s*(\\d+)',density_output)
if not densities:
    raise SystemExit('FAIL: cannot determine screen density for touch target QA')
min_touch=round(40*int(densities[-1])/160)
regions=[]
for entry in nodes:
    rect=re.fullmatch(r'\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]',entry.get('bounds',''))
    if rect is None:
        raise SystemExit('FAIL: malformed Home clickable bounds')
    left,top,right,bottom=map(int,rect.groups())
    name=(entry.get('text','')+' '+entry.get('content-desc','')).strip()
    if not (0<=left<right<=width and 0<=top<bottom<=height):
        raise SystemExit(f'FAIL: Home control outside screenshot: {name} {rect.group(0)}')
    if right-left<min_touch or bottom-top<min_touch:
        raise SystemExit(f'FAIL: Home control too small for a finger: {name} {rect.group(0)}')
    regions.append((name,(left,top,right,bottom)))
for i,(name,a) in enumerate(regions):
    for other,b in regions[i+1:]:
        overlap_x=min(a[2],b[2])-max(a[0],b[0])
        overlap_y=min(a[3],b[3])-max(a[1],b[1])
        if overlap_x>3 and overlap_y>3:
            raise SystemExit(f'FAIL: overlapping Home tap targets: {name} / {other}')
print(f'PASS: {len(regions)} Home actions fit {width}x{height}; minimum {min_touch}px touch targets',flush=True)
# A pause control or gameplay status on the first screen is always a regression.
for n in root.iter('node'):
    label=(n.get('text','')+' '+n.get('content-desc',''))
    if 'Izbornik tijekom igre' in label or 'Bopi leti' in label:
        raise SystemExit('FAIL: gameplay control/status leaked onto initial home')
print('PASS: 3 accessible home buttons and no gameplay pause/HUD',flush=True)
PY
}
# Exercise the real settings destination, not only the home button labels.
tap_settings(){
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re, subprocess, sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
buttons=[n for n in root.iter('node') if n.get('clickable')=='true' and
         'POSTAVKE' in (n.get('text','')+' '+n.get('content-desc',''))]
if len(buttons)!=2:
    raise SystemExit(f'FAIL: expected header gear and central POSTAVKE CTA, found {len(buttons)}')
regions=[]
for button in buttons:
    match=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',button.get('bounds',''))
    if not match: raise SystemExit('FAIL: invalid settings shortcut bounds')
    l,t,r,b=map(int,match.groups())
    regions.append((t,l,r,b))
# The lower button is the original large action. The new top gear has its
# own independent tap target and was already checked in home accessibility.
t,l,r,b=max(regions)
subprocess.run(['adb','shell','input','tap',str((l+r)//2),str((t+b)//2)],check=True)
PY
}

# Validate native legal disclosure from the actual no-scroll Settings footer.
verify_settings_legal(){
  python3 - <<'PY'
import re,subprocess,time,xml.etree.ElementTree as ET
def dump():
    subprocess.run(["adb","shell","uiautomator","dump","/sdcard/bopavi-legal.xml"],
                   check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,timeout=24)
    blob=subprocess.run(["adb","exec-out","cat","/sdcard/bopavi-legal.xml"],
                        check=True,capture_output=True,timeout=15).stdout
    return ET.fromstring(blob)
for button in ("POLITIKA PRIVATNOSTI","UVJETI KORIŠTENJA"):
    home=dump()
    candidates=[n for n in home.iter("node") if n.get("clickable")=="true"
                and n.get("content-desc")==button]
    if len(candidates)!=1:
        raise SystemExit(f"FAIL: expected one native {button} legal footer button")
    bounds=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",candidates[0].get("bounds",""))
    if not bounds: raise SystemExit(f"FAIL: invalid legal footer bounds {button}")
    left,top,right,bottom=map(int,bounds.groups())
    if right<=left or bottom<=top:
        raise SystemExit(f"FAIL: noninteractive legal footer bounds {button}")
    subprocess.run(["adb","shell","input","tap",
                    str((left+right)//2),str((top+bottom)//2)],check=True,timeout=15)
    time.sleep(.4)
    dialog=dump()
    labels=[n.get("text","") for n in dialog.iter("node")]
    if not any(button==label for label in labels):
        raise SystemExit(f"FAIL: legal native dialog did not open: {button}")
    if not any("Brendigo" in label for label in labels):
        raise SystemExit(f"FAIL: legal publisher missing: {button}")
    subprocess.run(["adb","shell","input","keyevent","4"],check=True,timeout=15)
    time.sleep(.3)
    again=dump()
    if not any(n.get("content-desc")=="NAPREDNE POSTAVKE" for n in again.iter("node")):
        raise SystemExit("FAIL: legal notice Back did not restore compact Settings")
print("PASS: privacy/terms native footer, truthful Brendigo notices and no-scroll return",
      flush=True)
PY
}
# Verify actual touch selection and persisted difficulty without disturbing
# the user's normal default before the gameplay/collision QA starts.
verify_difficulty_cards(){
  # Reference Settings is a non-scrolling overview. Navigate through the
  # real Advanced Settings action before testing the original difficulty cards.
  dump_ui
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
items=[n for n in root.iter('node') if n.get('clickable')=='true'
       and 'NAPREDNE POSTAVKE' in (n.get('text','')+' '+n.get('content-desc',''))]
if len(items)!=1:
    raise SystemExit(f'FAIL: expected one advanced settings button, got {len(items)}')
bounds=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',items[0].get('bounds',''))
if not bounds: raise SystemExit('FAIL: invalid advanced settings bounds')
l,t,r,b=map(int,bounds.groups())
subprocess.run(['adb','shell','input','tap',str((l+r)//2),str((t+b)//2)],check=True)
PY
  python3 - <<'PY'
import re,subprocess,time,xml.etree.ElementTree as ET
def dump():
    subprocess.run(['adb','shell','uiautomator','dump','/sdcard/window.xml'],
                   check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,timeout=24)
    xml=subprocess.run(['adb','shell','cat','/sdcard/window.xml'],
                       check=True,capture_output=True,text=True,timeout=15).stdout
    return ET.fromstring(xml)
def target(name):
    root=dump()
    matches=[n for n in root.iter('node') if n.get('clickable')=='true'
             and ('Težina igre '+name+',') in n.get('content-desc','')]
    if len(matches)>1: raise SystemExit(f'FAIL: duplicate difficulty card {name}')
    return matches[0] if matches else None
def locate(name):
    for _ in range(9):
        found=target(name)
        if found is not None:
            match=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',found.get('bounds',''))
            if match:
                l,t,r,b=map(int,match.groups())
                if r>l and b>t:
                    return found,(l+r)//2,(t+b)//2
        subprocess.run(['adb','shell','input','swipe','190','740','190','335','440'],
                       check=True,timeout=12)
        time.sleep(.32)
    raise SystemExit(f'FAIL: accessible {name} card not reachable by real swipe')
for name,expected in [('LAGANO','LAGANO'),('NORMALNO','NORMALNO')]:
    card,x,y=locate(name)
    subprocess.run(['adb','shell','input','tap',str(x),str(y)],check=True,timeout=12)
    time.sleep(.5)
    selected=target(expected)
    if selected is None or 'odabrano' not in selected.get('content-desc',''):
        raise SystemExit(f'FAIL: difficulty {name} did not persist its touch selection')
    print(f'PASS: selected real premium difficulty card {name}',flush=True)
PY
}
# Verify every world is a genuine clickable card with saved progress, not
# a static illustration or invented screenshot stars. Keep world indices intact.
verify_world_cards(){
  python3 - <<'PY'
import re,subprocess,time,xml.etree.ElementTree as ET
worlds=('Zeleni otoci','Kristalno nebo','Lava vrhovi','Noćni dvorci',
        'Biserno more','Nebeske kule','Čarobna šuma','Zvjezdani bezdani')
def dump():
    subprocess.run(['adb','shell','uiautomator','dump','/sdcard/window.xml'],
                   check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,timeout=24)
    xml=subprocess.run(['adb','shell','cat','/sdcard/window.xml'],
                       capture_output=True,text=True,check=True,timeout=15).stdout
    return ET.fromstring(xml)
def tap(button):
    match=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',button.get('bounds',''))
    if match is None: raise SystemExit('FAIL: invalid worlds button bounds')
    l,t,r,b=map(int,match.groups())
    if r<=l or b<=t: raise SystemExit('FAIL: zero-size worlds button')
    subprocess.run(['adb','shell','input','tap',str((l+r)//2),str((t+b)//2)],
                   check=True,timeout=12)
def nodes(root):
    return [n for n in root.iter('node') if n.get('clickable')=='true']
root=dump()
entry=[n for n in nodes(root) if 'SVJETOVI' in (n.get('text','')+' '+n.get('content-desc',''))]
if len(entry)!=1: raise SystemExit(f'FAIL: expected one home SVJETOVI action, got {len(entry)}')
tap(entry[0]);time.sleep(1.1)
# Authentic portrait screenshot used for visual comparison and README.
# The 2x4 gallery remains independently tappable; this is not a mockup.
with open('qa/screenshots/android-worlds.png','wb') as target:
    subprocess.run(['adb','exec-out','screencap','-p'],stdout=target,
                   check=True,timeout=22)
with open('qa/screenshots/android-worlds.png','rb') as captured:
    if captured.read(8)!=bytes.fromhex('89504e470d0a1a0a'):
        raise SystemExit('FAIL: Android worlds screenshot is not a valid PNG')
seen=set()
for attempt in range(13):
    root=dump()
    for node in nodes(root):
        title=node.get('content-desc','')
        for name in worlds:
            if title.startswith(name+', otključano,'):
                if not all(piece in title for piece in ('level ','prikupljeno ','rekord ','zvjezdice ',' od 30')):
                    raise SystemExit(f'FAIL: world missing real save metrics: {name}')
                seen.add(name)
    if len(seen)==8:break
    subprocess.run(['adb','shell','input','swipe','220','746','220','291','430'],
                   check=True,timeout=12)
    time.sleep(.4)
if len(seen)!=8:
    raise SystemExit(f'FAIL: not all world cards reachable: {sorted(set(worlds)-seen)}')
print('PASS: all eight real illustrated world cards expose saved level, collected items and record',flush=True)
subprocess.run(['adb','shell','input','keyevent','4'],check=True,timeout=12)
PY
}
tap_play(){
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re, subprocess, sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
buttons=[n for n in root.iter('node') if n.get('clickable')=='true' and 'IGRAJ' in (n.get('text','')+' '+n.get('content-desc',''))]
if len(buttons)!=1:
    # Hosted API 35 emulator occasionally boots with a SYSTEM launcher ANR
    # over the app. Dismiss only the exact Pixel Launcher dialog; never an
    # ANR from com.brendigo.bopavi, which must still fail the smoke test.
    titles=[n.get("text","") for n in root.iter("node") if n.get("resource-id")=="android:id/alertTitle"]
    waits=[n for n in root.iter("node") if n.get("resource-id")=="android:id/aerr_wait"
           and n.get("text")=="Wait" and n.get("clickable")=="true"]
    if titles==["Pixel Launcher isn't responding"] and len(waits)==1:
        m=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',waits[0].get("bounds",""))
        if not m: raise SystemExit("FAIL: invalid launcher ANR Wait button bounds")
        l,t,r,b=map(int,m.groups())
        print("Stopping exact unresponsive Pixel Launcher before play navigation",flush=True)
        subprocess.run(["adb","shell","am","force-stop",
                        "com.google.android.apps.nexuslauncher"],check=True)
        subprocess.run(["adb","shell","pidof","com.brendigo.bopavi"],check=True,
                       stdout=subprocess.DEVNULL)
        sys.exit(0)
    raise SystemExit(f'FAIL: expected one clickable IGRAJ button, got {len(buttons)}; dialog={titles}')
match=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',buttons[0].get('bounds',''))
if not match:
    raise SystemExit('FAIL: invalid accessible IGRAJ bounds')
l,t,r,b=map(int,match.groups())
if r<=l or b<=t:
    raise SystemExit('FAIL: empty accessible IGRAJ bounds')
x,y=(l+r)//2,(t+b)//2
print(f'Tap IGRAJ at {x},{y}',flush=True)
subprocess.run(['adb','shell','input','tap',str(x),str(y)],check=True)
PY
}
# The first play tap must reach the *idle* game preview, NOT an already
# active flight. A visible pause button before a first flap is a hard failure.
verify_idle_preview(){
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
nodes=list(root.iter('node'))
labels=[n.get('text','')+' '+n.get('content-desc','') for n in nodes]
if not any('Dodirni za let Bopija' in x for x in labels):
    raise SystemExit('FAIL: expected idle Bopi game view before any flap')
if any('Izbornik tijekom igre' in x for x in labels):
    raise SystemExit('FAIL: pause button visible before first flap')
print('PASS: idle flight preview shows no pause button',flush=True)
PY
}
# Verify PROFIL opens a real offline profile, not Postavke, then returns home.
verify_profile_tab(){
  python3 - <<'PY'
import re,subprocess,sys,time,xml.etree.ElementTree as ET
def dump():
    subprocess.run(["adb","shell","uiautomator","dump","/sdcard/bopavi-profile.xml"],
                   check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    xml=subprocess.run(["adb","exec-out","cat","/sdcard/bopavi-profile.xml"],
                       check=True,capture_output=True).stdout
    return ET.fromstring(xml)
root=dump()
matches=[n for n in root.iter("node") if n.get("clickable")=="true"
         and n.get("content-desc")=="PROFIL"]
if len(matches)!=1: raise SystemExit("FAIL: expected exactly one clickable profile tab")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",matches[0].get("bounds",""))
if not m: raise SystemExit("FAIL: invalid profile tab hit bounds")
l,t,r,b=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((l+r)//2),str((t+b)//2)],check=True)
time.sleep(1.5)
root=dump()
texts=[n.get("text","") for n in root.iter("node")]
desc=[n.get("content-desc","") for n in root.iter("node")]
if not any("Tvoj letački dnevnik" in text for text in texts):
    raise SystemExit("FAIL: PROFIL did not navigate to its dedicated screen")
if not any("Letački profil" in label for label in desc):
    raise SystemExit("FAIL: missing genuine saved-player profile and pilot description")
if any("IZGLED I ZVUK" in text for text in texts):
    raise SystemExit("FAIL: PROFIL incorrectly opened settings")
# A no-scroll profile must expose Edit and the four bottom actions without
# swipe. Bounds of the real tap surfaces must be inside the display.
nodes=list(root.iter("node"))
for label in ("Natrag na početnu","Uredi profil","ZADACI","TRGOVINA"):
    targets=[n for n in nodes if n.get("clickable")=="true" and
             label.casefold() in (n.get("content-desc","")+" "+n.get("text","")).casefold()]
    if not targets:
        raise SystemExit("FAIL: profile compact action unreachable: "+label)
    if not any(n.get("bounds") and re.fullmatch(r"\[\d+,\d+\]\[\d+,\d+\]",n.get("bounds",""))
               for n in targets):
        raise SystemExit("FAIL: profile action bounds malformed: "+label)

# Only an actual emulator frame of the visited, validated profile qualifies
# as visual evidence. Never substitute the uploaded concept artwork.
from pathlib import Path
capture=subprocess.run(["adb","exec-out","screencap","-p"],check=True,capture_output=True)
if not capture.stdout.startswith(bytes.fromhex("89504e470d0a1a0a")) or len(capture.stdout)<2000:
    raise SystemExit("FAIL: genuine profile screenshot missing or invalid")
Path("qa/screenshots/android-profile.png").write_bytes(capture.stdout)
print("PASS: dedicated PROFILE screen shows saved pilot data and real screenshot",flush=True)
subprocess.run(["adb","shell","input","keyevent","4"],check=True)
time.sleep(1)
root=dump()
labels=[n.get("text","")+" "+n.get("content-desc","") for n in root.iter("node")]
for title in ("IGRAJ","SVJETOVI","POSTAVKE"):
    if not any(title in label for label in labels):
        raise SystemExit("FAIL: profile Back did not restore home: "+title)
print("PASS: profile Android Back returns to playable home",flush=True)
PY
}

# Verify Shop and Records from the real Home footer, then ensure Back
# returns to Home. Capture *authentic* emulator frames for visual review.
verify_shop_records_tabs(){
  python3 - <<'PY'
import re,subprocess,time,xml.etree.ElementTree as ET
from pathlib import Path
def dump():
    subprocess.run(["adb","shell","uiautomator","dump","/sdcard/bopavi-tabs.xml"],
                   check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,timeout=24)
    raw=subprocess.run(["adb","exec-out","cat","/sdcard/bopavi-tabs.xml"],
                       check=True,capture_output=True,timeout=15).stdout
    return ET.fromstring(raw)
def tap_tab(name):
    root=dump()
    matches=[n for n in root.iter("node") if n.get("clickable")=="true"
             and n.get("content-desc")==name]
    if len(matches)!=1: raise SystemExit(f"FAIL: expected exactly one {name} footer tab")
    bounds=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",matches[0].get("bounds",""))
    if not bounds: raise SystemExit(f"FAIL: malformed bounds for {name}")
    left,top,right,bottom=map(int,bounds.groups())
    subprocess.run(["adb","shell","input","tap",
                    str((left+right)//2),str((top+bottom)//2)],check=True,timeout=15)
    time.sleep(.8)
for name,expected,path in (
    ("TRGOVINA","OPREMA ZA BOPIJEV LET","android-shop.png"),
    ("REKORDI","MOJI REKORDI","android-records.png"),
):
    tap_tab(name)
    root=dump()
    labels=[n.get("text","")+" "+n.get("content-desc","") for n in root.iter("node")]
    if not any(expected in v for v in labels):
        raise SystemExit(f"FAIL: {name} destination not open")
    if name=="TRGOVINA" and not all(any(text in v for v in labels) for text in ("KUPI ŠTIT","KUPI MAGNET")):
        raise SystemExit("FAIL: live shield/magnet purchase actions not reachable")
    if name=="REKORDI" and not any("Prvi rekord čeka" in v or "OSOBNI REKORD" in v for v in labels):
        raise SystemExit("FAIL: offline records neither show real results nor honest empty state")
    png=subprocess.run(["adb","exec-out","screencap","-p"],
                       check=True,capture_output=True,timeout=20).stdout
    if not png.startswith(bytes.fromhex("89504e470d0a1a0a")) or len(png)<2000:
        raise SystemExit(f"FAIL: invalid {name} screenshot")
    Path("qa/screenshots",path).write_bytes(png)
    if name=="REKORDI":
        # Test the actual Records -> Achievements -> Records round-trip.
        # Every one of the eight save-backed world cards must fit without
        # scrolling or send Back to the Settings screen by mistake.
        record_actions=[n for n in root.iter("node") if n.get("clickable")=="true"
                        and "MOJA POSTIGNUĆA" in (n.get("text","")+" "+n.get("content-desc",""))]
        if len(record_actions)!=1: raise SystemExit("FAIL: Records achievement action missing")
        hit=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",record_actions[0].get("bounds",""))
        if not hit: raise SystemExit("FAIL: invalid Records achievement bounds")
        l,t,r,b=map(int,hit.groups())
        subprocess.run(["adb","shell","input","tap",str((l+r)//2),str((t+b)//2)],
                       check=True,timeout=15)
        time.sleep(.7)
        achievements=dump()
        ach_labels=[n.get("content-desc","") for n in achievements.iter("node")
                    if n.get("clickable")=="true"]
        world_names=("Zeleni otoci","Kristalno nebo","Lava vrhovi","Noćni dvorci",
                     "Biserno more","Nebeske kule","Čarobna šuma","Zvjezdani bezdani")
        for world in world_names:
            if not any(world.casefold()+"," in value.casefold()
                       and "zvjezdice " in value and "rekord " in value
                       and "Otvori levele" in value for value in ach_labels):
                raise SystemExit("FAIL: live achievement world inaccessible: "+world)
        frame=subprocess.run(["adb","exec-out","screencap","-p"],
                             check=True,capture_output=True,timeout=20).stdout
        if not frame.startswith(bytes.fromhex("89504e470d0a1a0a")):
            raise SystemExit("FAIL: achievements screenshot invalid")
        Path("qa/screenshots/android-achievements.png").write_bytes(frame)
        subprocess.run(["adb","shell","input","keyevent","4"],check=True,timeout=15)
        time.sleep(.7)
        after=dump()
        returned=[n.get("text","")+" "+n.get("content-desc","") for n in after.iter("node")]
        if not any("MOJI REKORDI" in v for v in returned):
            raise SystemExit("FAIL: Achievements Back did not restore Records")
        print("PASS: eight save-backed achievement cards and Records round-trip",flush=True)
    subprocess.run(["adb","shell","input","keyevent","4"],check=True,timeout=15)
    time.sleep(.7)
    home=dump()
    text=[n.get("text","")+" "+n.get("content-desc","") for n in home.iter("node")]
    if not all(any(token in value for value in text) for token in ("IGRAJ","SVJETOVI","POSTAVKE")):
        raise SystemExit(f"FAIL: {name} Back did not restore Home")
    print(f"PASS: {name} functional tab and actual screenshot",flush=True)
PY
}

gameplay_ready=0
home_captured=0
attempt=1
while [ "$attempt" -le 6 ]; do
  adb shell pidof com.brendigo.bopavi >/dev/null || { echo "FAIL: BOPAVI exited" >&2;exit 1; }
  dump_ui
  # A system Pixel Launcher ANR can cover the healthy app immediately after
  # emulator boot, before the first IGRAJ/home branch. Recover only the exact
  # verified system dialog, then recheck the actual foreground UI.
  if recover_launcher_anr; then
    sleep 2
    dump_ui
  fi
  if grep -q 'Dodirni za let Bopija' qa/screenshots/android-current-ui.xml; then
    verify_idle_preview
    gameplay_ready=1
    break
  fi
  # Bopi-only navigation must launch straight into the idle flight.
  # An accidental reappearance of the removed gallery is a release blocker.
  if grep -q 'ODABERI LIKA' qa/screenshots/android-current-ui.xml; then
    capture android-obsolete-picker-diagnostic || true
    echo "FAIL: obsolete character picker appeared before Bopi flight" >&2
    exit 1
  fi
  if [ "$home_captured" -eq 0 ] && grep -q 'IGRAJ' qa/screenshots/android-current-ui.xml; then
    capture android-home
    verify_three_home_actions
    # Confirm settings can open and Android Back returns to the same 3 actions.
    # Retry only after verifying actual UI state. A system Pixel Launcher ANR
    # can intercept the first settings tap after boot, even if game is healthy.
    settings_ready=0
    settings_attempt=1
    while [ "$settings_attempt" -le 4 ]; do
      adb shell pidof com.brendigo.bopavi >/dev/null || {
        echo "FAIL: BOPAVI exited during settings navigation" >&2; exit 1;
      }
      dump_ui
      if grep -q 'IZGLED I ZVUK' qa/screenshots/android-current-ui.xml; then
        settings_ready=1
        break
      fi
      if recover_launcher_anr; then
        sleep 2
      elif grep -q 'POSTAVKE' qa/screenshots/android-current-ui.xml &&
           grep -q 'IGRAJ' qa/screenshots/android-current-ui.xml; then
        tap_settings
        sleep 2
      else
        echo "Unexpected foreground window while opening POSTAVKE" >&2
        break
      fi
      settings_attempt=$((settings_attempt+1))
    done
    if [ "$settings_ready" -ne 1 ]; then
      capture android-settings-diagnostic || true
      head -c 5000 qa/screenshots/android-current-ui.xml >&2 || true
      adb logcat -d -t 100 | tail -n 80 || true
      echo "FAIL: POSTAVKE did not open after verified, bounded retries" >&2
      exit 1
    fi
    capture android-settings
    verify_settings_legal
    verify_difficulty_cards
    adb shell input keyevent 4
    sleep 1
    # The system launcher can raise an ANR *while navigating back*, too.
    # Do not mistake a covered UI for a broken BOPAVI navigation path.
    # A healthy BOPAVI process and actual three-button home are still mandatory.
    home_ready=0
    return_attempt=1
    while [ "$return_attempt" -le 5 ]; do
      adb shell pidof com.brendigo.bopavi >/dev/null || {
        echo "FAIL: BOPAVI exited while returning from settings" >&2; exit 1;
      }
      dump_ui
      if grep -q 'IGRAJ' qa/screenshots/android-current-ui.xml &&
         grep -q 'POSTAVKE' qa/screenshots/android-current-ui.xml; then
        verify_three_home_actions
        home_ready=1
        break
      fi
      if recover_launcher_anr; then
        # Android Back might have been consumed by the system ANR dialog.
        sleep 2
      elif grep -q 'IZGLED I ZVUK' qa/screenshots/android-current-ui.xml; then
        adb shell input keyevent 4
        sleep 1
      else
        echo "Unexpected UI after returning from settings" >&2
        break
      fi
      return_attempt=$((return_attempt+1))
    done
    if [ "$home_ready" -ne 1 ]; then
      capture android-home-return-diagnostic || true
      head -c 5000 qa/screenshots/android-current-ui.xml >&2 || true
      adb logcat -d -t 100 | tail -n 80 || true
      echo "FAIL: three-button home did not reappear after POSTAVKE" >&2
      exit 1
    fi
    echo "PASS: settings opens and returns to the three-button home"
    verify_profile_tab
    verify_shop_records_tabs
    verify_world_cards
    sleep 1
    dump_ui
    verify_three_home_actions
    home_captured=1
  fi
  if grep -q 'IGRAJ' qa/screenshots/android-current-ui.xml; then
    tap_play
  elif recover_launcher_anr; then
    # System-only recovery does not count as evidence of a passed UI test.
    sleep 2
  else
    capture android-navigation-diagnostic || true
    echo "FAIL: expected HOME, pilot picker or idle GameView, but foreground is another screen" >&2
    head -c 5000 qa/screenshots/android-current-ui.xml >&2 || true
    exit 1
  fi
  sleep 2
  attempt=$((attempt+1))
done
dump_ui
if grep -q 'Dodirni za let Bopija' qa/screenshots/android-current-ui.xml; then
  verify_idle_preview
  gameplay_ready=1
fi
if [ "$gameplay_ready" -ne 1 ]; then
  capture android-navigation-diagnostic || true
  echo "FAIL: IGRAJ tap did not open the game" >&2
  head -c 5000 qa/screenshots/android-current-ui.xml >&2 || true
  adb logcat -d -t 200 | tail -n 100 || true
  exit 1
fi
echo "PASS: IGRAJ opens idle flight without a premature pause button"
# Coordinates derived from the emulator resolution, not a hard-coded device size.
size=$(adb shell wm size | tail -n 1 | sed 's/.*: //' | tr -d '\r')
width=${size%x*}
height=${size#*x}
case "$width:$height" in
  *[!0-9:]*|:*) echo "Invalid Android screen dimensions: $size" >&2; exit 1;;
esac
center=$((width/2))
adb shell pidof com.brendigo.bopavi
capture android-gameplay-ready
# Hosted emulator can consume the first input during launcher recovery.
# Check actual GameView accessibility state before waiting for collision results.
flight_started=0
attempt=1
while [ "$attempt" -le 6 ]; do
  adb shell pidof com.brendigo.bopavi >/dev/null || { echo "FAIL: BOPAVI exited before flight" >&2; exit 1; }
  # Capture several real on-device frames after ONE first flap.
  # Emulator latency can keep the first frame idle, then render real gameplay,
  # then finish before a host-side screenshot reaches the Surface compositor.
  # Preserve every candidate; only a frame different from BOTH the verified
  # idle screen and the eventual result can become the release gameplay image.
  adb shell "input tap $center $((height*50/100)); sleep 0.27; screencap -p /sdcard/bopavi-flight-1.png; sleep 0.22; screencap -p /sdcard/bopavi-flight-2.png; sleep 0.22; screencap -p /sdcard/bopavi-flight-3.png"
  for frame in 1 2 3; do
    adb exec-out cat "/sdcard/bopavi-flight-$frame.png" > "qa/screenshots/android-flight-candidate-$frame.png"
    python3 - "qa/screenshots/android-flight-candidate-$frame.png" <<'PY'
from pathlib import Path
import sys
data=Path(sys.argv[1]).read_bytes()
assert len(data)>2000 and data.startswith(bytes.fromhex("89504e470d0a1a0a"))
PY
  done
  live_candidate_found=0
  for frame in 1 2 3; do
    file="qa/screenshots/android-flight-candidate-$frame.png"
    if ! cmp -s qa/screenshots/android-gameplay-ready.png "$file"; then
      cp "$file" qa/screenshots/android-gameplay-active.png
      live_candidate_found=1
      break
    fi
  done
  if [ "$live_candidate_found" -ne 1 ]; then
    echo "No changed in-flight frame was rendered; retrying genuine first flap"
    attempt=$((attempt+1))
    sleep 0.10
    continue
  fi
  # The first real flap is held only in a debuggable QA launch. Release
  # it now with a second physical input, after preserving pixel-proof PNGs.
  # This runs the ordinary real-time GameSimulation to its actual collision.
  adb shell input tap "$center" "$((height*50/100))"
  sleep 1
  dump_ui
  if grep -q 'Bopi leti' qa/screenshots/android-current-ui.xml; then
    if ! grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
      echo "FAIL: pause did not appear after actual first flight flap" >&2
      exit 1
    fi
    # The screenshot was taken after input, but before slow UI automation.
    # Never publish an unchanged idle frame as active gameplay.
    if cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-gameplay-active.png; then
      echo "FAIL: active screenshot duplicates pre-flight preview" >&2
      exit 1
    fi
    flight_started=1
    break
  fi
  if grep -q 'PONOVO' qa/screenshots/android-current-ui.xml; then
    if grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
      echo "FAIL: pause leaked into completed result screen" >&2
      exit 1
    fi
    if cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-gameplay-active.png; then
      echo "FAIL: result acknowledged flight but screenshot is the unchanged idle frame" >&2
      exit 1
    fi
    flight_started=1
    break
  fi
  if grep -q "Pixel Launcher isn't responding" qa/screenshots/android-current-ui.xml; then
    tap_play
  fi
  # Launcher swallowed the gesture or the UI has not confirmed the flight.
  # Discard that candidate; never publish a launcher/idle screenshot as play.
  rm -f qa/screenshots/android-gameplay-active.png
  echo "Waiting for Bopi to start flying ($attempt/6)"
  attempt=$((attempt+1))
done
if [ "$flight_started" -ne 1 ]; then
  capture android-flight-start-diagnostic || true
  echo "FAIL: gameplay did not acknowledge the first flight gesture" >&2
  head -c 5000 qa/screenshots/android-current-ui.xml >&2 || true
  exit 1
fi
echo "PASS: first flight gesture acknowledged by GameView"
# A real collision must lead to results; never treat an idle game as success.
# Wait for the actual result UI rather than assuming every runner reaches it in 5s.
# A game crash or a missing retry button is still a hard failure.
result_ready=0
attempt=1
while [ "$attempt" -le 15 ]; do
  if ! adb shell pidof com.brendigo.bopavi >/dev/null; then
    echo "FAIL: BOPAVI process exited during gameplay" >&2
    adb logcat -d -t 250 | tail -n 100 || true
    exit 1
  fi
  if dump_ui && grep -q 'PONOVO' qa/screenshots/android-current-ui.xml; then
    result_ready=1
    break
  fi
  echo "Waiting for Android result and PONOVO button ($attempt/15)"
  attempt=$((attempt+1))
  sleep 2
done
if [ "$result_ready" -ne 1 ]; then
  capture android-result-diagnostic || true
  echo "FAIL: gameplay did not expose the PONOVO action within 30 seconds" >&2
  head -c 5000 qa/screenshots/android-current-ui.xml || true
  adb logcat -d -t 250 | tail -n 100 || true
  exit 1
fi
# The v0.1.61 Release published an active-flight PNG and Results PNG
# with the exact same SHA-256 digest even though the UI tree reached Results.
# A settled result frame must differ from BOTH game screenshots. Recapture
# after an actual UI/vsync wait; never upload duplicate visual evidence.
result_capture_unique=0
result_capture_attempt=1
while [ "$result_capture_attempt" -le 8 ]; do
  dump_ui
  if ! grep -q 'PONOVO' qa/screenshots/android-current-ui.xml ||
     grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
    echo "FAIL: actual result UI disappeared before screenshot" >&2
    exit 1
  fi
  sleep 0.40
  capture android-result
  # The first changed frame may still be an early Results transition. Before
  # judging validity, inspect all stored *real* screencaps from the first tap.
  # No filtering by UI mock, sprite files or reference images is permitted.
  selected_real_flight=0
  for candidate in qa/screenshots/android-flight-candidate-*.png; do
    if [ -s "$candidate" ] &&
       ! cmp -s qa/screenshots/android-gameplay-ready.png "$candidate" &&
       ! cmp -s qa/screenshots/android-result.png "$candidate"; then
      cp "$candidate" qa/screenshots/android-gameplay-active.png
      selected_real_flight=1
      break
    fi
  done
  if [ "$selected_real_flight" -ne 1 ]; then
    echo "No genuinely different gameplay frame in captured first-flap sequence"
  fi
  if [ "$selected_real_flight" -eq 1 ] &&
     ! cmp -s qa/screenshots/android-gameplay-active.png qa/screenshots/android-result.png &&
     ! cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-result.png; then
    result_capture_unique=1
    break
  fi
  echo "Waiting for distinct rendered results screenshot ($result_capture_attempt/8)"
  result_capture_attempt=$((result_capture_attempt+1))
done
if [ "$result_capture_unique" -ne 1 ]; then
  capture android-result-diagnostic || true
  echo "FAIL: Results PNG duplicates active or idle gameplay after bounded retries" >&2
  exit 1
fi
dump_ui
python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
descriptions=[n.get("content-desc","") for n in root.iter("node")]
pilots=[value for value in descriptions if value.startswith("Odabrani letač: ")]
if len(pilots)!=1 or "svijet" not in pilots[0]:
    raise SystemExit("FAIL: real result must expose one selected pilot and world illustration")
titles=[n.get("text","") for n in root.iter("node")]
if sum("NOVI REKORD!" in title for title in titles)>1:
    raise SystemExit("FAIL: duplicate new-record banner in result")
print("PASS: result renders real selected pilot/world and accessible record state",flush=True)
PY
if grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
  echo "FAIL: gameplay pause control leaked onto result screen" >&2
  exit 1
fi
echo "PASS: home / idle flight / active flight / result pause lifecycle verified"
