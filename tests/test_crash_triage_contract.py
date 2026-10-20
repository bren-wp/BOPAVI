#!/usr/bin/env python3
"""Android evidence contract; neither static checks nor CI prove zero crashes."""
from pathlib import Path
import subprocess
R=Path(__file__).resolve().parents[1]
triage=(R/"docs/qa/CRASH-TRIAGE.md").read_text(encoding="utf-8")
source=(R/"tests/qa_capture_android_crash.sh").read_text(encoding="utf-8")
assert "VERDICT: UNCONFIRMED" in triage
for required in ("adb logcat -b crash -v threadtime", "dumpsys meminfo",
                 "adb bugreport", "30/60/90/120 Hz", "process-death",
                 "FATAL EXCEPTION", "ANR", "SIGSEGV", "low RAM"):
    assert required in triage,required
assert "adb logcat -b crash -v threadtime -d" in source
assert "am force-stop" in source and "pidof" in source
for unsafe in ("logcat -c", "pm clear", "kill -9", "catch (_:Exception)"):
    assert unsafe not in source,unsafe
subprocess.run(["bash","-n",str(R/"tests/qa_capture_android_crash.sh")],check=True)
print("PASS: Android-only crash evidence and non-destructive recovery contract")
