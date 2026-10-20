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
# Debug-only QA: keep the real first active frame stable through slow Pixel
# screencap calls. GameSimulation resumes with a second genuine flap, and the
# production (non-debuggable) build ignores this Intent extra entirely.
adb shell am start -W -n com.brendigo.bopavi/.MainActivity --ez bopavi_qa_capture_first_flap true
# Android 16 needs extra time after the cold boot before System UI and
# Accessibility Manager can reliably serve window hierarchy dumps.
sleep 12
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
python3 - <<'PY'
import re,subprocess,xml.etree.ElementTree as ET
root=ET.parse("qa/screenshots/android-current-ui.xml").getroot()
nodes=list(root.iter("node"))
click=[n for n in nodes if n.get("clickable")=="true"]
def label(n):return (n.get("text","")+" "+n.get("content-desc","")).upper()
play=[n for n in click if "PLAY" in label(n) or "IGRAJ" in label(n)]
if len(play)!=1:raise SystemExit("FAIL: one native PLAY action required")
if any("BEST SCORE" in label(n) or "NAJBOLJI REZULTAT" in label(n) for n in nodes):raise SystemExit("FAIL: obsolete Home record badge returned")
for bad in ("WORLDS","SVJETOVI","SHOP","TRGOVINA","SETTINGS","POSTAVKE","PROFILE","PROFIL","LEVELS","LEVELI"):
    if any(bad in label(n) for n in click):raise SystemExit("FAIL: forbidden Home navigation: "+bad)
m=re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",play[0].get("bounds",""))
if not m:raise SystemExit("FAIL: PLAY bounds unavailable")
a,b,c,d=map(int,m.groups())
if c-a<130 or d-b<40:raise SystemExit("FAIL: PLAY hit target too small")
subprocess.run(["adb","shell","input","tap",str((a+c)//2),str((b+d)//2)],check=True)
print("PASS: accessible Home Play and no extra menus",flush=True)
PY
sleep 2
adb shell pidof com.brendigo.bopavi >/dev/null || { echo "FAIL: BOPAVI process exited before gameplay" >&2; exit 1; }
capture android-gameplay-ready
screen_size="$(adb shell wm size | tail -1 | sed 's/.*: //' | tr -d '\r')"
width="$(printf '%s' "$screen_size" | cut -dx -f1)"
height="$(printf '%s' "$screen_size" | cut -dx -f2)"
center=$((width/2))
adb shell "input tap $center $((height/2)); sleep 0.27; screencap -p /sdcard/flight-a.png; sleep 0.22; screencap -p /sdcard/flight-b.png"
adb exec-out cat /sdcard/flight-a.png > qa/screenshots/flight-a.png
adb exec-out cat /sdcard/flight-b.png > qa/screenshots/flight-b.png
for name in a b; do
  if [ -s "qa/screenshots/flight-$name.png" ] &&
     ! cmp -s qa/screenshots/android-gameplay-ready.png "qa/screenshots/flight-$name.png"; then
    cp "qa/screenshots/flight-$name.png" qa/screenshots/android-gameplay-active.png
    break
  fi
done
test -s qa/screenshots/android-gameplay-active.png || { echo "FAIL: active flight frame missing" >&2; exit 1; }
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
sleep 1
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
