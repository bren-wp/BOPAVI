#!/bin/sh
# Android visual-flow smoke: capture actual home, gameplay and collision result.
set -eu
mkdir -p qa/screenshots
adb wait-for-device
adb install -r qa/apk/app-debug.apk
adb shell am start -W -n com.brendigo.bopavi/.MainActivity
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
print("Recovering confirmed Pixel Launcher ANR while BOPAVI remains running",flush=True)
subprocess.run(["adb","shell","input","tap",str((left+right)//2),
                str((top+bottom)//2)],check=True)
PY
}
verify_three_home_actions(){
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
names=("IGRAJ","SVJETOVI","POSTAVKE")
nodes=[n for n in root.iter('node') if n.get('clickable')=='true']
for name in names:
    matches=[n for n in nodes if name in (n.get('text','')+' '+n.get('content-desc',''))]
    if len(matches)!=1:
        raise SystemExit(f'FAIL: home must expose one accessible clickable {name}, got {len(matches)}')
    bounds=matches[0].get('bounds','')
    if not bounds or bounds.startswith('[0,0][0,0]'):
        raise SystemExit(f'FAIL: {name} has invalid button bounds')
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
if len(buttons)!=1:
    raise SystemExit(f'FAIL: expected one accessible POSTAVKE shortcut, found {len(buttons)}')
match=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',buttons[0].get('bounds',''))
if not match: raise SystemExit('FAIL: invalid settings shortcut bounds')
l,t,r,b=map(int,match.groups())
subprocess.run(['adb','shell','input','tap',str((l+r)//2),str((t+b)//2)],check=True)
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
        print("Recovering exact emulator Pixel Launcher ANR; BOPAVI process remains verified",flush=True)
        subprocess.run(["adb","shell","input","tap",str((l+r)//2),str((t+b)//2)],check=True)
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
tap_selected_pilot(){
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re,subprocess,sys,time,xml.etree.ElementTree as ET

def node_labels(root):
    return [n.get('text','')+' '+n.get('content-desc','') for n in root.iter('node')]

def refresh_ui():
    subprocess.run(['adb','shell','uiautomator','dump','/sdcard/bopavi-window.xml'],
                   check=True,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    return ET.fromstring(subprocess.check_output(
        ['adb','exec-out','cat','/sdcard/bopavi-window.xml']))

def swipe_gallery(root,up=True):
    candidates=[n for n in root.iter('node') if n.get('scrollable')=='true']
    if not candidates:
        raise SystemExit('FAIL: character gallery screen is not scrollable')
    m=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',candidates[0].get('bounds',''))
    if not m: raise SystemExit('FAIL: invalid gallery scrolling bounds')
    l,t,r,b=map(int,m.groups())
    x=(l+r)//2
    top=t+int((b-t)*.24)
    bottom=t+int((b-t)*.78)
    y1,y2=(bottom,top) if up else (top,bottom)
    subprocess.run(['adb','shell','input','swipe',str(x),str(y1),str(x),str(y2),'430'],check=True)
    time.sleep(.4)

root=ET.parse(sys.argv[1]).getroot()
if not any('ODABERI LIKA' in s or 'POLETI S' in s for s in node_labels(root)):
    raise SystemExit('FAIL: mandatory pre-flight picker missing')

# The nine cards extend beyond the emulator viewport. Verify actual rendered
# accessibility cards while scrolling, not just the initially visible portion.
required={'Portantin','Noa','Any'}
seen=set()
for attempt in range(7):
    labels=node_labels(root)
    for name in required:
        if any(n.get('clickable')=='true' and
               name in (n.get('content-desc','')+' '+n.get('text',''))
               for n in root.iter('node')):
            seen.add(name)
    if required <= seen: break
    swipe_gallery(root,up=True)
    root=refresh_ui()
if not required <= seen:
    raise SystemExit(f'FAIL: missing characters after scrolling: {sorted(required-seen)}')
print('PASS: scrolled actual character gallery and found Portantin, Noa and Any',flush=True)

# Scroll back: flight confirmation deliberately stays near the top of menu.
for attempt in range(8):
    buttons=[n for n in root.iter('node') if n.get('clickable')=='true'
             and 'POLETI S' in (n.get('text','')+' '+n.get('content-desc',''))]
    if len(buttons)==1 and any('ODABERI LIKA' in s for s in node_labels(root)):
        break
    swipe_gallery(root,up=False)
    root=refresh_ui()
else:
    raise SystemExit('FAIL: flight confirmation unreachable after gallery scrolling')
m=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',buttons[0].get('bounds',''))
if not m: raise SystemExit('FAIL: invalid pilot launch bounds')
l,t,r,b=map(int,m.groups())
# A scroll may still be settling: tap only after the full title and button
# are simultaneously visible, then verify actual navigation before succeeding.
time.sleep(.8)
x,y=(l+r)//2,(t+b)//2
for attempt in range(3):
    subprocess.run(['adb','shell','input','tap',str(x),str(y)],check=True)
    time.sleep(.8)
    state=refresh_ui()
    labels=node_labels(state)
    if any('Dodirni za let Bopija' in value for value in labels):
        print('PASS: mandatory character picker launched the idle flight',flush=True)
        break
    if not any('POLETI S' in value for value in labels):
        print('PASS: pilot choice left the picker; outer QA verifies gameplay state',flush=True)
        break
else:
    raise SystemExit('FAIL: launch button did not leave pilot selection after verified taps')
PY
}
gameplay_ready=0
home_captured=0
attempt=1
while [ "$attempt" -le 6 ]; do
  adb shell pidof com.brendigo.bopavi >/dev/null || { echo "FAIL: BOPAVI exited" >&2;exit 1; }
  dump_ui
  if grep -q 'Dodirni za let Bopija' qa/screenshots/android-current-ui.xml; then
    verify_idle_preview
    gameplay_ready=1
    break
  fi
  if grep -q 'POLETI S' qa/screenshots/android-current-ui.xml; then
    capture android-pilot-picker
    tap_selected_pilot
    sleep 2
    continue
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
    home_captured=1
  fi
  tap_play
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
  adb shell input tap "$center" "$((height*50/100))"
  sleep 1
  dump_ui
  if grep -q 'Bopi leti' qa/screenshots/android-current-ui.xml; then
    if ! grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
      echo "FAIL: pause did not appear after actual first flight flap" >&2
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
    flight_started=1
    break
  fi
  if grep -q "Pixel Launcher isn't responding" qa/screenshots/android-current-ui.xml; then
    tap_play
  fi
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
capture android-result
dump_ui
if grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
  echo "FAIL: gameplay pause control leaked onto result screen" >&2
  exit 1
fi
echo "PASS: home / idle flight / active flight / result pause lifecycle verified"
