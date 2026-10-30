#!/bin/sh
# Android visual-flow smoke: capture actual home, gameplay and collision result.
set -eu
mkdir -p qa/screenshots
# A hosted Android emulator can disappear from ADB after many successful
# navigation assertions. This is a runner transport failure, NOT proof that
# BOPAVI crashed. Mark only an actual missing/offline ADB device at exit,
# so CI may re-create an emulator and repeat the *entire* interactive test.
# Never retry an assertion error while the device is connected; such a failure
# must stay red and preserve diagnostics.
qa_exit_diagnostic() {
  qa_exit_code="$1"
  trap - 0
  # Restore the emulator display even after a failed compact/tall layout test.
  if [ "${BOPAVI_RESIZE_IN_PROGRESS:-0}" = 1 ]; then
    adb shell wm size reset >/dev/null 2>&1 || true
    adb shell wm density reset >/dev/null 2>&1 || true
  fi
  if [ "$qa_exit_code" -ne 0 ]; then
    qa_adb_state="$(adb get-state 2>&1 || true)"
    if [ "$qa_adb_state" != "device" ]; then
      printf 'Confirmed ADB transport loss: %s\n' "$qa_adb_state" >&2
      printf 'ADB transport unavailable at test exit: %s\n' "$qa_adb_state" > qa/screenshots/adb-transport-offline.retry
      adb devices -l >&2 || true
    else
      echo 'Connected Android device: preserving genuine application/test failure; no emulator retry' >&2
    fi
  fi
  exit "$qa_exit_code"
}
trap 'qa_exit_diagnostic "$?"' 0
adb wait-for-device
adb install -r qa/apk/app-debug.apk
# Preserve an isolated launch window. Emulator cold-boot log spam can evict
# app exceptions from the ordinary main log before our startup timeout.
adb logcat -c >/dev/null 2>&1 || true
# Debug-only QA: keep the real first active frame stable through slow Pixel
# screencap calls. GameSimulation resumes with a second genuine flap, and the
# production (non-debuggable) build ignores this Intent extra entirely.
adb shell am start -W -n com.brendigo.bopavi/.MainActivity --ez bopavi_qa_capture_first_flap true
# Android 16 needs extra time after the cold boot before System UI and
# Accessibility Manager can reliably serve window hierarchy dumps.
sleep 12
# On AGP/edge-to-edge migrations, a process that vanished before the first
# screenshot is a real startup failure. Capture the OS exception and process
# death record before the emulator is torn down; do NOT treat it as an ADB
# transport error or retry it away.
if ! adb shell pidof com.brendigo.bopavi; then
  echo 'FAIL: BOPAVI process exited during native Activity startup' >&2
  adb logcat -b crash -d -v threadtime > qa/screenshots/android-startup-crash.txt 2>&1 || true
  adb logcat -d -v threadtime -s AndroidRuntime:* ActivityManager:* ActivityTaskManager:*       > qa/screenshots/android-startup-processes.txt 2>&1 || true
  adb shell dumpsys activity activities > qa/screenshots/android-startup-activities.txt 2>&1 || true
  cat qa/screenshots/android-startup-crash.txt >&2 || true
  grep -E -C 10 'com.brendigo.bopavi|FATAL EXCEPTION|crash|died|has died|force-finishing|Start proc'       qa/screenshots/android-startup-processes.txt | tail -n 200 >&2 || true
  exit 1
fi

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

# Find the real accessible PLAY view instead of guessing a screen percentage.
dump_ui(){
  # API36 may return a text error in place of hierarchy XML immediately
  # after boot or during an unrelated System UI ANR. Never treat that
  # diagnostic as a screen, nor let stale XML count as a passing test.
  attempt=1
  while [ "$attempt" -le 5 ]; do
    adb shell rm -f /sdcard/bopavi-window.xml >/dev/null 2>&1 || true
    adb shell uiautomator dump /sdcard/bopavi-window.xml >/dev/null 2>&1 || true
    adb exec-out cat /sdcard/bopavi-window.xml > qa/screenshots/android-current-ui.xml 2>/dev/null || true
    if python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
try:
    root=ET.parse(sys.argv[1]).getroot()
    assert root.tag=="hierarchy" and list(root.iter("node"))
except (ET.ParseError,AssertionError,OSError):
    sys.exit(1)
PY
    then
      return 0
    fi
    echo "Waiting for valid Android hierarchy, attempt $attempt/5" >&2
    attempt=$((attempt+1))
    sleep 4
  done
  echo "FAIL: Android System UI did not provide a valid interactive hierarchy" >&2
  adb shell pidof com.brendigo.bopavi || true
  adb logcat -d -t 200 | tail -n 75 >&2 || true
  return 1
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
if len(waits)!=1 or titles not in (
    ["Pixel Launcher isn't responding"],["System UI isn't responding"]):
    sys.exit(1)
m=re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',waits[0].get("bounds",""))
if not m:
    raise SystemExit("FAIL: invalid system launcher Wait button bounds")
left,top,right,bottom=map(int,m.groups())
# Pixel Launcher and System UI are distinct system processes. Recovery
# is allowed ONLY for these verified dialog titles, never for a BOPAVI ANR.
if titles == ["Pixel Launcher isn't responding"]:
    print("Stopping confirmed unresponsive Pixel Launcher (not BOPAVI)",flush=True)
    subprocess.run(["adb","shell","am","force-stop",
                    "com.google.android.apps.nexuslauncher"],check=True)
else:
    # Never force-stop System UI (which owns status/navigation surfaces).
    # Press the exact native Wait action to let it recover, then require the
    # real BOPAVI page to appear before any smoke-test PASS.
    print("Waiting for confirmed hung System UI to recover (not BOPAVI)",flush=True)
    subprocess.run(["adb","shell","input","tap",
                    str((left+right)//2),str((top+bottom)//2)],check=True)
subprocess.run(["adb","shell","pidof","com.brendigo.bopavi"],check=True,
               stdout=subprocess.DEVNULL)
PY
}
# New production UX: one Play -> live endless game -> genuine result -> Retry.
sleep 2
dump_ui
# A system-only Pixel Launcher ANR can cover an otherwise running BOPAVI
# after emulator cold boot. Recover ONLY the allowlisted system dialog,
# then capture a fresh hierarchy and demand the actual game's PLAY control.
if recover_launcher_anr; then
  sleep 3
  dump_ui
fi
capture android-home
# A freshly recovered hosted Launcher can swallow one tap. Recheck the
# ACTUAL legal dialog and owner body text rather than counting a tap as success.
# Retry a native Home link at most three times while the game remains reachable;
# missing content, crashes or a non-home screen must still fail closed.
open_legal_dialog(){
  legal_link="$1"
  legal_title="$2"
  legal_owner_text="$3"
  legal_required_text="$4"
  legal_opened=0
  # The illustrated Home contains no legal footer; open the real Settings
  # route first and scroll its native settings page to the legal rows.
  dump_ui
  python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
options=[n for n in nodes if n.get("clickable")=="true"
         and n.get("content-desc")=="Otvori opcije igre"]
if len(options)!=1:raise SystemExit("FAIL: Home Settings missing for offline legal check")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",options[0].get("bounds",""))
if not m:raise SystemExit("FAIL: invalid native Settings tap bounds")
x,y,z,w=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((x+z)//2),str((y+w)//2)],check=True)
PY
  sleep 1
  python3 - <<'PY'
import re,subprocess
size=subprocess.check_output(["adb","shell","wm","size"],text=True)
m=re.findall(r"(\d+)x(\d+)",size)
if not m:raise SystemExit("FAIL: emulator dimensions missing for Settings scroll")
w,h=map(int,m[-1])
subprocess.run(["adb","shell","input","swipe",str(w//2),str(h*4//5),
                str(w//2),str(h//4),"430"],check=True)
PY
  sleep 1
  for legal_attempt in 1 2 3; do
    dump_ui
    if ! python3 - "$legal_link" <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
links=[n for n in nodes if n.get("clickable")=="true"
       and n.get("content-desc","")==sys.argv[1]]
if len(links)!=1:raise SystemExit("FAIL: Settings legal control missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",links[0].get("bounds",""))
if not m:raise SystemExit("FAIL: native legal tap bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
    then
      echo 'FAIL: Settings legal control missing' >&2
      return 1
    fi
    sleep 2
    dump_ui
    # Verify the owner/title/required section and dismiss from the SAME
    # hierarchy. On slow API35 uiautomator, an additional dump can show the
    # resumed Home instead of the already-verified modal.
    if python3 - "$legal_title" "$legal_owner_text" "$legal_required_text" <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
alltext=" ".join(label(n) for n in nodes)
for required in sys.argv[1:]:
    if required not in alltext:raise SystemExit("FAIL: in-app legal dialog lacks "+required)
# Capture the actual verified native legal window before dismissing it.
# These are original adb pixels, not screenshots fabricated from HTML.
from pathlib import Path
screen="android-privacy" if sys.argv[1]=="POLITIKA PRIVATNOSTI" else "android-terms"
capture=subprocess.check_output(["adb","exec-out","screencap","-p"])
if capture[:8]!=bytes.fromhex("89504e470d0a1a0a") or len(capture)<2000:
    raise SystemExit("FAIL: original Android legal screenshot is invalid")
Path("qa/screenshots/"+screen+".png").write_bytes(capture)
closing=[n for n in nodes if n.get("clickable")=="true" and "ZATVORI" in label(n)]
if len(closing)!=1:raise SystemExit("FAIL: native legal dialog Close missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",closing[0].get("bounds",""))
if not m:raise SystemExit("FAIL: native legal Close bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
    then legal_opened=1;break; fi
    echo "Legal dialog not yet visible after native tap $legal_attempt/3" >&2
  done
  test "$legal_opened" -eq 1 || { echo "FAIL: native legal dialog did not open with owner text" >&2; return 1; }
  # The underlying Settings modal is still present after the legal window closes.
  adb shell input keyevent 4
  sleep 1
}
# Read the ACTUAL native policy dialog before entering flight, not static copy.
open_legal_dialog "Pročitaj politiku privatnosti" "POLITIKA PRIVATNOSTI" "BRENDIGO" "OBRIŠI PODATKE"
echo "PASS: Croatian privacy dialog accessible, offline and dismissible"
sleep 1
dump_ui
# Terms must work offline from the second bottom link on the same Home screen.
open_legal_dialog "Pročitaj uvjete korištenja" "UVJETI KORIŠTENJA" "BRENDIGO" "OGRANIČENU"
echo "PASS: native terms dialog accessible, offline and dismissible"
sleep 1
dump_ui
# Exercise both newly reachable Home overlays before tapping IGRAJ.
# The real dialog and radio selection must survive a native UI dump.
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
options=[n for n in nodes if n.get("clickable")=="true" and
    n.get("content-desc")=="Otvori opcije igre"]
if len(options)!=1:raise SystemExit("FAIL: Home quick options inaccessible")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",options[0].get("bounds",""))
if not m:raise SystemExit("FAIL: quick options hitbox invalid")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
sleep 1
dump_ui
capture android-options
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
alltext=" ".join(label(n) for n in nodes)
for expected in ("OPCIJE IGRE","OPUŠTENO","STANDARDNO","IZAZOVNO","GOTOVO"):
    if expected not in alltext:raise SystemExit("FAIL: options dialog missing "+expected)
choice=[n for n in nodes if n.get("clickable")=="true" and
    n.get("text","").upper()=="OPUŠTENO"]
if len(choice)!=1:raise SystemExit("FAIL: relaxed difficulty not selectable")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",choice[0].get("bounds",""))
if not m:raise SystemExit("FAIL: difficulty tap bounds invalid")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
sleep 1
dump_ui
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
relaxed=[n for n in nodes if n.get("text","").upper()=="OPUŠTENO"]
if len(relaxed)!=1 or relaxed[0].get("checked")!="true":
    raise SystemExit("FAIL: relaxed flight difficulty not selected")
# Restore standard difficulty so later gameplay and physics QA are comparable.
standard=[n for n in nodes if n.get("clickable")=="true" and
    n.get("text","").upper()=="STANDARDNO"]
if len(standard)!=1:raise SystemExit("FAIL: standard flight difficulty missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",standard[0].get("bounds",""))
if not m:raise SystemExit("FAIL: standard option bounds invalid")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
sleep 1
dump_ui
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
standard=[n for n in nodes if n.get("text","").upper()=="STANDARDNO"]
if len(standard)!=1 or standard[0].get("checked")!="true":
    raise SystemExit("FAIL: restored standard difficulty not selected")
done=[n for n in nodes if n.get("clickable")=="true" and n.get("text","").upper()=="GOTOVO"]
if len(done)!=1:raise SystemExit("FAIL: options close action missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",done[0].get("bounds",""))
if not m:raise SystemExit("FAIL: options close bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: native options dialog and difficulty selection",flush=True)
PY
sleep 1
dump_ui
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
records=[n for n in nodes if n.get("clickable")=="true" and
    n.get("content-desc")=="Pogledaj svoje rekorde"]
if len(records)!=1:raise SystemExit("FAIL: Home records action missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",records[0].get("bounds",""))
if not m:raise SystemExit("FAIL: records action bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
PY
sleep 1
dump_ui
capture android-records
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
nodes=list(ET.parse("qa/screenshots/android-current-ui.xml").getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
alltext=" ".join(label(n) for n in nodes)
for expected in ("MOJI REKORDI","NAJBOLJI REZULTAT","ZATVORI"):
    if expected not in alltext:raise SystemExit("FAIL: native records dialog missing "+expected)
close=[n for n in nodes if n.get("clickable")=="true" and n.get("text","").upper()=="ZATVORI"]
if len(close)!=1:raise SystemExit("FAIL: records dialog close missing")
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",close[0].get("bounds",""))
if not m:raise SystemExit("FAIL: records dialog close bounds missing")
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: local records visible and dismissible",flush=True)
PY
sleep 1
dump_ui
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
root=ET.parse("qa/screenshots/android-current-ui.xml").getroot()
nodes=list(root.iter("node"))
click=[n for n in nodes if n.get("clickable")=="true"]
def label(n):return (n.get("text","")+" "+n.get("content-desc","")).upper()
play=[n for n in click if "PLAY" in label(n) or "IGRAJ" in label(n)]
if len(play)!=1:raise SystemExit("FAIL: one native PLAY action required")
if any("BEST SCORE" in label(n) or "NAJBOLJI REZULTAT" in label(n) for n in nodes):raise SystemExit("FAIL: obsolete Home record badge returned")
for menu in ("SKINS","SHOP"):
    found=[n for n in click if n.get("text","").strip().upper()==menu]
    if len(found)!=1:raise SystemExit("FAIL: expected one real Home menu: "+menu)
for bad in ("PROFILE","PROFIL","CLOUD SAVE","RESTORE PURCHASES"):
    if any(bad in label(n) for n in click):raise SystemExit("FAIL: unsupported Home route: "+bad)
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",play[0].get("bounds",""))
if not m:raise SystemExit("FAIL: PLAY bounds unavailable")
a,b,c,d=map(int,m.groups())
if c-a<130 or d-b<40:raise SystemExit("FAIL: PLAY hit target too small")
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: accessible Home Play, Skins and Shop with separate Settings route",flush=True)
PY
# A successful tap on the Home PLAY button is not proof that the Flight view
# has appeared. Wait for the *actual native GameView accessibility state*.
# The earlier flow sometimes captured a second Home animation as Flight Preview.
ready=0
for attempt in $(seq 1 15); do
  adb shell pidof com.brendigo.bopavi >/dev/null || {
    echo "FAIL: BOPAVI process exited before gameplay" >&2; exit 1;
  }
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
def label(n):return (n.get("text","")+" "+n.get("content-desc","")).upper()
game=[n for n in nodes if "TAP TO FLY WITH" in label(n) or "DODIRNI ZA LET" in label(n)]
play=[n for n in nodes if n.get("clickable")=="true"
      and ("▶  PLAY" in label(n) or "▶  IGRAJ" in label(n))]
sys.exit(0 if len(game)==1 and not play else 1)
PY
  then
    ready=1; break
  fi
  echo "Waiting for actual Flight idle GameView, attempt $attempt/15" >&2
  sleep 2
done
test "$ready" -eq 1 || { echo "FAIL: tapping PLAY did not reach Flight" >&2; exit 1; }
capture android-gameplay-ready
printf 'STATE: flight-idle confirmed by native GameView\n' >> qa/screenshots/state-evidence.txt

screen_size="$(adb shell wm size | tail -1 | sed 's/.*: //' | tr -d '\r')"
width="$(printf '%s' "$screen_size" | cut -dx -f1)"
height="$(printf '%s' "$screen_size" | cut -dx -f2)"
center=$((width/2))
adb shell input tap "$center" "$((height/2))"
# The debug-only first-flap capture gate holds the real active GameSimulation
# until the *second* tap. Require the accessible flying state AND native pause
# control instead of treating any changed frame as active gameplay.
flying=0
for attempt in $(seq 1 15); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
flight=[n for n in nodes if n.get("class")=="android.view.View"
        and n.get("clickable")=="true"
        and "ŽIVOTI:" in label(n)
        and "DODIRNI ZA ZAMAH KRILIMA" in label(n)
        and "BODOVI:" not in label(n) and "HRANA:" not in label(n)
        and "MOĆ:" not in label(n)]
pause=[n for n in nodes if n.get("clickable")=="true"
       and ("PAUZIRAJ IGRU" in label(n) or "PAUSE GAME" in label(n))]
sys.exit(0 if len(flight)==1 and len(pause)==0 else 1)
PY
  then flying=1; break; fi
  sleep 1
done
test "$flying" -eq 1 || { echo "FAIL: lives-only active GameView not accessible"; exit 1; }
capture android-gameplay-active
printf 'STATE: flight-active confirmed with lives-only native HUD\n' >> qa/screenshots/state-evidence.txt
if cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-gameplay-active.png; then
  echo "FAIL: active flight still duplicates idle screenshot" >&2; exit 1
fi
# No pause icon exists in gameplay. Android system Back opens the accessible
# Pause dialog; verify there is no hidden tappable Pause overlay in the flight.
adb shell input keyevent 4
pause_ready=0
for attempt in $(seq 1 12); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
names=[label(n) for n in nodes if n.get("clickable")=="true"]
headings=[label(n) for n in nodes]
ok=(any("PAUZA" in n for n in headings)
    and sum("NASTAVI" in n for n in names)==1
    and sum("PONOVO" in n for n in names)==1
    and sum("IZLAZ" in n for n in names)==1)
sys.exit(0 if ok else 1)
PY
  then pause_ready=1; break; fi
  sleep 1
done
test "$pause_ready" -eq 1 || { echo "FAIL: real Pause dialog/buttons did not appear" >&2; exit 1; }
capture android-pause
printf 'STATE: pause verified by real NASTAVI/PONOVO/IZLAZ dialog\n' >> qa/screenshots/state-evidence.txt
if cmp -s qa/screenshots/android-pause.png qa/screenshots/android-gameplay-active.png; then
  echo "FAIL: Pause screenshot duplicates active gameplay" >&2; exit 1
fi
python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re,subprocess,sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
continue_buttons=[n for n in nodes if n.get("clickable")=="true" and "NASTAVI" in label(n)]
assert len(continue_buttons)==1,"FAIL: no unique Continue control in Pause"
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",continue_buttons[0].get("bounds",""))
assert m,"FAIL: Continue tap bounds missing"
x1,y1,x2,y2=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((x1+x2)//2),str((y1+y2)//2)],check=True)
print("PASS: real Pause Continue action pressed",flush=True)
PY
# Dismissing an Android AlertDialog is asynchronous. A tap sent immediately
# after NASTAVI can be consumed by the still-attached modal window instead
# of GameView, leaving the deliberate first-flap screenshot hold frozen.
# Wait for the original live flight view (not a newly created preview).
flight_resumed=0
for attempt in $(seq 1 14); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
label=lambda n:(n.get("text","")+" "+n.get("content-desc","")).upper()
click=[label(n) for n in nodes if n.get("clickable")=="true"]
active=any("ŽIVOTI:" in label(n) and n.get("class")=="android.view.View"
           for n in nodes)
overlay=any("PAUZIRAJ IGRU" in s for s in click)
modal=any("NASTAVI" in s for s in click)
sys.exit(0 if active and not overlay and not modal else 1)
PY
  then flight_resumed=1; break; fi
  sleep 1
done
test "$flight_resumed" -eq 1 || {
  echo "FAIL: Continue did not return to the actual live flight window" >&2; exit 1;
}
printf 'STATE: Pause Continue returned to native flight HUD\n' >> qa/screenshots/state-evidence.txt
# The debug-only first-flap still capture now releases its hold on second tap.
adb shell input tap "$center" "$((height/2))"
result_ready=0
for attempt in $(seq 1 18); do
  adb shell pidof com.brendigo.bopavi >/dev/null || { echo "FAIL: BOPAVI process exited during gameplay" >&2; exit 1; }
  if dump_ui && (grep -q 'RETRY' qa/screenshots/android-current-ui.xml || grep -q 'PONOVO' qa/screenshots/android-current-ui.xml); then
    result_ready=1; break
  fi
  sleep 2
done
test "$result_ready" -eq 1 || { echo "FAIL: real game did not expose a result after collision" >&2; exit 1; }
sleep 0.5
capture android-result
if cmp -s qa/screenshots/android-result.png qa/screenshots/android-gameplay-ready.png ||
   cmp -s qa/screenshots/android-result.png qa/screenshots/android-gameplay-active.png; then
  echo "FAIL: Results PNG duplicates active or idle gameplay" >&2; exit 1
fi
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
root=ET.parse("qa/screenshots/android-current-ui.xml").getroot()
nodes=list(root.iter("node"))
def label(n):return (n.get("text","")+" "+n.get("content-desc","")).upper()
labels=" ".join(label(n) for n in nodes)
# The simplified Result contains only earned points, saved best, and live actions.
# Never require deleted currencies, gate cards, or old Flight Over headings.
if not any(title in labels for title in ("FLIGHT OVER", "RESULT", "REZULTAT", "LET ZAVRŠEN")):
    raise SystemExit("FAIL: loss result headline missing")
if not any(score in labels for score in ("SCORE", "REZULTAT")):
    raise SystemExit("FAIL: real run score missing")
if any(gate in labels for gate in ("UKUPNO PROLAZA", "TOTAL GATES", "PRIKUPLJENE KOVANICE", "COLLECTED COINS")):
    raise SystemExit("FAIL: obsolete reward/gate summary returned")
if "BEST SCORE" not in labels and "NAJBOLJI REZULTAT" not in labels:raise SystemExit("FAIL: saved best missing")
click=[n for n in nodes if n.get("clickable")=="true"]
retry=[n for n in click if "RETRY" in label(n) or "PONOVO" in label(n)]
home=[n for n in click if "HOME" in label(n) or "POČETNA" in label(n)]
if len(retry)!=1 or len(home)!=1:raise SystemExit("FAIL: Retry/Home result controls missing")
for bad in ("WORLDS","SVJETOVI","SHOP","TRGOVINA","LEVELS","LEVELI"):
    if any(bad in label(n) for n in click):raise SystemExit("FAIL: deleted menu exposed "+bad)
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",retry[0].get("bounds",""))
a,b,c,d=map(int,m.groups())
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: genuine loss result and Retry/Home controls",flush=True)
PY
# Verify that RETRY returns to the *new idle GameView*, not a second
# screenshot of the Results screen or Home, before accepting the image.
retry_ready=0
for attempt in $(seq 1 12); do
  if dump_ui && python3 - qa/screenshots/android-current-ui.xml <<'PY'
import sys,xml.etree.ElementTree as ET
nodes=list(ET.parse(sys.argv[1]).getroot().iter("node"))
labels=" ".join((n.get("text","")+" "+n.get("content-desc","")).upper() for n in nodes)
sys.exit(0 if ("TAP TO FLY WITH" in labels or "DODIRNI ZA LET" in labels)
         and "▶  RETRY" not in labels and "▶  PONOVO" not in labels else 1)
PY
  then retry_ready=1; break; fi
  sleep 1
done
test "$retry_ready" -eq 1 || { echo "FAIL: RETRY did not reach idle Flight" >&2; exit 1; }
capture android-gameplay-retry
if cmp -s qa/screenshots/android-gameplay-retry.png qa/screenshots/android-result.png; then
  echo "FAIL: Retry did not launch another flight" >&2; exit 1
fi
echo "PASS: simplified real-device Home, game, result and Retry"

# Optional second-stage responsive verification. These are real emulator
# window-size/density overrides after the unchanged screenshot-release flow.
# Every assertion runs against actual native clickable controls, not mock art.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  BOPAVI_RESIZE_IN_PROGRESS=1
  for spec in 720x1280:320:compact 1080x2400:420:tall; do
    dims="$(printf '%s' "$spec" | cut -d: -f1)"
    density="$(printf '%s' "$spec" | cut -d: -f2)"
    label="$(printf '%s' "$spec" | cut -d: -f3)"
    adb shell wm size "$dims"
    adb shell wm density "$density"
    adb shell am force-stop com.brendigo.bopavi
    adb shell am start -W -n com.brendigo.bopavi/.MainActivity
    sleep 4
    adb shell pidof com.brendigo.bopavi >/dev/null || {
      echo "FAIL: BOPAVI exited at $dims/$density" >&2; exit 1;
    }
    dump_ui
    capture "android-home-$label"
    python3 - "$dims" "$density" "$label" <<'PY'
import re,sys,struct,xml.etree.ElementTree as ET
from pathlib import Path
dims,density,label=sys.argv[1:]
width,height=map(int,dims.split("x"))
source=Path("qa/screenshots/android-home-"+label+".png").read_bytes()
assert source[:8]==bytes.fromhex("89504e470d0a1a0a")
actual=struct.unpack(">II",source[16:24])
assert actual==(width,height),(label,actual)
root=ET.parse("qa/screenshots/android-current-ui.xml").getroot()
nodes=list(root.iter("node"))
play=[n for n in nodes if n.get("clickable")=="true"
      and any(x in (n.get("text","")+" "+n.get("content-desc","")).upper()
              for x in ("PLAY","IGRAJ"))]
assert len(play)==1,(label,"missing or duplicate accessible PLAY")
# On reference Home the legal documents are in Settings; test actual Home
# touch targets rather than removed pilot cards / old legal footer.
for title,needle in (
    ("Settings","OTVORI OPCIJE IGRE"),
    ("Records","POGLEDAJ SVOJE REKORDE"),
    ("Skins","OTVORI IZBOR LIKOVA"),
    ("Shop","OTVORI TRGOVINU NADOGRADNJI"),
    ("Achievements","POGLEDAJ SVOJA POSTIGNUĆA"),
    ("Sound","OTVORI ZVUČNE POSTAVKE"),
):
    found=[n for n in nodes if n.get("clickable")=="true" and
           needle in (n.get("text","")+" "+n.get("content-desc","")).upper()]
    assert len(found)==1,(label,title,"missing or duplicate accessible Home control")
    match=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",found[0].get("bounds",""))
    assert match,(label,title,"missing bounds")
    x1,y1,x2,y2=map(int,match.groups())
    assert 0<=x1<x2<=width and 0<=y1<y2<=height,(label,title,"clipped")
    assert y2-y1>=48*int(density)/160,(label,title,"touch target below 48dp")

m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",play[0].get("bounds",""))
assert m,(label,"PLAY bounds absent")
l,t,r,b=map(int,m.groups())
assert 0<=l<r<=width and 0<=t<b<=height,(label,"clipped PLAY control")
min48=48*int(density)/160
assert r-l>=min48 and b-t>=min48,(label,"PLAY target smaller than 48dp")
assert not any(n.get("scrollable")=="true" and
               n.get("package")=="com.brendigo.bopavi" for n in nodes),(label,"unwanted scroll")
print("PASS: native",label,"PLAY hit target, screen insets and no scrolling")
PY
  done
  adb shell wm size reset
  adb shell wm density reset
  BOPAVI_RESIZE_IN_PROGRESS=0
fi

# Actual dynamic text scaling on compact and tall Android displays.
# Keep the release screenshot flow unchanged; these additional emulator
# states validate native touch geometry and scroll reachability only.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  python3 tools/qa_android_fontscale.py
fi

# Separate real-device geometry QA after the original screenshot/gameplay flow.
# Verify landscape Home and a flight that survives rotation without a restart.
if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then
  python3 tools/qa_android_reference_menus.py
  python3 tools/qa_android_background.py
  python3 tools/qa_android_rotation.py
fi
