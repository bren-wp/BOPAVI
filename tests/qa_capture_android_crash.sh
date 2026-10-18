#!/usr/bin/env bash
# Controlled Android cold-start/background stress + evidence capture.
# Does not clear logcat, app data, permissions, or upload any private logs.
set -euo pipefail
iterations="${1:-20}"
out="${2:-qa/crash-triage/automated}"
package="com.brendigo.bopavi"
case "$iterations" in ''|*[!0-9]*) echo "iterations must be an integer" >&2; exit 2;; esac
if (( iterations < 1 || iterations > 100 )); then echo "iterations must be 1..100" >&2; exit 2; fi
command -v adb >/dev/null || { echo "adb unavailable" >&2; exit 2; }
if [[ "$(adb devices | awk '$2=="device"{n++} END{print n+0}')" != "1" ]]; then
  echo "Connect exactly one authorized Android test device (adb devices -l)" >&2; exit 2
fi
mkdir -p "$out"
adb shell pm path "$package" | grep -q 'package:' || { echo "$package not installed" >&2; exit 2; }
date -u +%Y-%m-%dT%H:%M:%SZ > "$out/start-utc.txt"
adb shell getprop ro.build.fingerprint > "$out/build-fingerprint.txt"
adb shell dumpsys package "$package" > "$out/package-before.txt"
adb logcat -b crash -v threadtime -d > "$out/crash-before.log"
result=0
collect() {
  local status=$?
  set +e
  date -u +%Y-%m-%dT%H:%M:%SZ > "$out/end-utc.txt"
  adb logcat -b crash -v threadtime -d > "$out/crash-after.log" 2> "$out/logcat-error.txt"
  adb logcat -b main -b system -v threadtime -d > "$out/system-after.log" 2>/dev/null
  adb shell dumpsys meminfo "$package" > "$out/meminfo-after.txt" 2>/dev/null
  echo "$status" > "$out/exit-code.txt"
  echo "Evidence: $out (treat pre-existing log records separately; scrub private data before sharing)"
  exit "$status"
}
trap collect EXIT
for ((i=1; i<=iterations; i++)); do
  echo "Cold-start attempt $i/$iterations" | tee -a "$out/attempts.txt"
  # force-stop is expected here; do NOT interpret it as an application crash.
  adb shell am force-stop "$package"
  adb shell am start -W -n "$package/.MainActivity" > "$out/launch-$i.txt"
  sleep 1
  if ! adb shell pidof "$package" > "$out/pid-$i.txt"; then
    echo "App process missing after start in attempt $i" | tee -a "$out/failures.txt" >&2
    exit 1
  fi
  # HOME/background/wake without wiping local records or tampering with memory pressure.
  adb shell input keyevent KEYCODE_HOME
  sleep 1
  adb shell am start -W -n "$package/.MainActivity" > "$out/return-$i.txt"
  if ! adb shell pidof "$package" > "$out/resumed-pid-$i.txt"; then
    echo "App process missing after foreground in attempt $i" | tee -a "$out/failures.txt" >&2
    exit 1
  fi
done
echo "PASS: $iterations controlled cold-start/background probes; no complete native pause/retry test implied."
