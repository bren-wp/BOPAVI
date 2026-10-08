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
# Coordinates derived from the emulator resolution, not a hard-coded device size.
size=$(adb shell wm size | tail -n 1 | sed 's/.*: //' | tr -d '\r')
width=${size%x*}
height=${size#*x}
case "$width:$height" in
  *[!0-9:]*|:*) echo "Invalid Android screen dimensions: $size" >&2; exit 1;;
esac
center=$((width/2))
adb shell input tap "$center" "$((height*88/100))"
sleep 1
adb shell pidof com.brendigo.bopavi
capture android-gameplay-ready
# First touch makes Bopi flap; without further taps a collision must lead to results.
adb shell input tap "$center" "$((height*50/100))"
sleep 5
adb shell pidof com.brendigo.bopavi
# Do not accept a PNG containing Android's immersive onboarding dialog.
# The game must really reach the result view with an actionable retry button.
adb shell uiautomator dump /sdcard/bopavi-window.xml >/dev/null
adb shell cat /sdcard/bopavi-window.xml | grep -q 'PONOVO'
capture android-result
echo "PASS: full native Android visual flow captured without a process crash"
