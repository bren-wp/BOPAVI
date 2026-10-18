#!/usr/bin/env python3
"""Source and evidence-collection contract; a passing test cannot certify crash-free hardware."""
from pathlib import Path
import subprocess

R = Path(__file__).resolve().parents[1]
triage = (R / "docs/qa/CRASH-TRIAGE.md").read_text(encoding="utf-8")
script = R / "tests/qa_capture_android_crash.sh"
source = script.read_text(encoding="utf-8")
assert "VERDICT: UNCONFIRMED" in triage
for required in (
    "adb logcat -b crash -v threadtime", "dumpsys meminfo",
    "adb bugreport", "Devices and Simulators", ".ips", "dwarfdump",
    "xcrun atos", "30/60/90/120 Hz", "process-death",
    "FATAL EXCEPTION", "ANR", "SIGSEGV",
):
    assert required in triage, required
assert "adb logcat -b crash -v threadtime -d" in source
assert "am force-stop" in source
assert "pidof" in source
for unsafe in ("logcat -c", "pm clear", "kill -9", "catch (_:Exception)"):
    assert unsafe not in source, unsafe
subprocess.run(["bash", "-n", str(script)], check=True)
print("PASS: crash-triage evidence contract, Android shell syntax, no destructive data/log wiping")
