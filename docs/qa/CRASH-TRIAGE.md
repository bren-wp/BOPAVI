# BOPAVI — Android crash / ANR evidence and reproduction

**VERDICT: UNCONFIRMED** for any specific physical-device crash without a
matching stack trace. The Android-only CI success proves only its tested paths,
not universal device stability or absence of crashes.

## Evidence collection

On an authorized Android device with the same installed build, record Android
version, device model and `com.brendigo.bopavi` versionName/versionCode.
Do not collect or publish personally identifying logs without consent.

```bash
adb devices -l
adb shell dumpsys package com.brendigo.bopavi | grep -E 'versionCode=|versionName='
adb shell pidof com.brendigo.bopavi || true
mkdir -p qa/crash-triage
adb logcat -b crash -v threadtime -d > qa/crash-triage/before-crash.log
adb logcat -b main -b system -v threadtime -d > qa/crash-triage/before-system.log
bash tests/qa_capture_android_crash.sh 20 qa/crash-triage
adb logcat -b crash -v threadtime -d > qa/crash-triage/after-crash.log
adb shell dumpsys meminfo com.brendigo.bopavi > qa/crash-triage/meminfo.txt
# Only with explicit permission: adb bugreport qa/crash-triage/bugreport.zip
```

Never clear logs or delete user data while triaging. An ADB disconnect
is not an app crash. A controlled `am force-stop` or background
process-death test is not a spontaneous crash. Preserve timestamps
and compare the new entries with the pre-test log.

## Android test matrix

1. Cold launch with existing saves, 20 consecutive restarts and cold starts.
2. Tap to fly, double-tap, near-miss, collision, pause/retry/exit, one-shot fruit.
3. HOME → background → foreground, screen lock, audio-focus loss / regain.
4. 30/60/90/120 Hz devices (where available), long flights, frame stutter, low RAM.
5. Process-death and relaunch with preserved scores; no use of `pm clear`.
6. Confirm no `FATAL EXCEPTION`, `SIGSEGV`, or unexplained `ANR` in the tested interval.
7. Inspect native backtrace or Kotlin stack on each real failure and reproduce
   before fixing; measure `dumpsys meminfo` trends, frame pacing and battery.

## Classification

**Confirmed** requires a reproducible same-build stack and affected code
path; add a regression test and fix the root cause. **Unconfirmed** means
the relevant evidence is missing. **Resolved** requires passing exact
repro steps after a targeted fix; never silently mask exceptions.
