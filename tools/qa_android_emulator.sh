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

capture android-home
# Find the real accessible IGRAJ view instead of guessing a screen percentage.
dump_ui(){
  adb shell uiautomator dump /sdcard/bopavi-window.xml >/dev/null 2>&1
  adb exec-out cat /sdcard/bopavi-window.xml > qa/screenshots/android-current-ui.xml
  test -s qa/screenshots/android-current-ui.xml
}
tap_play(){
  python3 - qa/screenshots/android-current-ui.xml <<'PY'
import re, subprocess, sys, xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
buttons=[n for n in root.iter('node') if n.get('clickable')=='true' and 'IGRAJ' in (n.get('text','')+' '+n.get('content-desc',''))]
if len(buttons)!=1:
    raise SystemExit(f'FAIL: expected one clickable IGRAJ button, got {len(buttons)}')
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
gameplay_ready=0
attempt=1
while [ "$attempt" -le 3 ]; do
  dump_ui
  if grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
    gameplay_ready=1
    break
  fi
  tap_play
  sleep 2
  attempt=$((attempt+1))
done
dump_ui
if grep -q 'Izbornik tijekom igre' qa/screenshots/android-current-ui.xml; then
  gameplay_ready=1
fi
if [ "$gameplay_ready" -ne 1 ]; then
  capture android-navigation-diagnostic || true
  echo "FAIL: IGRAJ tap did not open the game" >&2
  head -c 5000 qa/screenshots/android-current-ui.xml >&2 || true
  adb logcat -d -t 200 | tail -n 100 || true
  exit 1
fi
echo "PASS: IGRAJ enters interactive gameplay"
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
# First touch makes Bopi flap; without further taps a collision must lead to results.
adb shell input tap "$center" "$((height*50/100))"
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
echo "PASS: full native Android visual flow captured without a process crash"
