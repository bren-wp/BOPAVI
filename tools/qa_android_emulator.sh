#!/bin/sh
# Single-process script for reactivecircus/android-emulator-runner.
# Its script: input is executed one line at a time; compound shell statements
# must live in this file, not inside the action input.
set -eu
mkdir -p qa/screenshots
adb wait-for-device
adb install -r qa/apk/app-debug.apk
adb shell am start -W -n com.brendigo.bopavi/.MainActivity
sleep 4
adb shell pidof com.brendigo.bopavi

screenshot="qa/screenshots/android-home.png"
success=0
for attempt in 1 2 3 4; do
  if adb exec-out screencap -p > "$screenshot" &&
     python3 -c 'from pathlib import Path; p=Path("qa/screenshots/android-home.png"); assert p.stat().st_size>2000 and p.read_bytes()[:8]==bytes.fromhex("89504e470d0a1a0a")'; then
    success=1
    break
  fi
  echo "Screenshot attempt $attempt failed; retrying" >&2
  sleep 3
done

if [ "$success" -ne 1 ]; then
  echo "Android PNG capture failed after four attempts" >&2
  adb logcat -d -t 160 | tail -n 70 || true
  exit 1
fi
echo "PASS: Android app launched and home screenshot validated"
