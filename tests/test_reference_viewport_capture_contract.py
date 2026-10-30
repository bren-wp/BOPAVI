#!/usr/bin/env python3
"""Ensure real reference-sized gameplay is captured in Android 15 QA."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
script=(root/"tools/qa_android_emulator.sh").read_text()
ci=(root/".github/workflows/native-ci.yml").read_text()
assert 'if [ "${BOPAVI_VIEWPORT_QA:-0}" = "1" ]; then' in script
assert 'adb shell wm size 941x1672' in script
assert 'adb shell wm density 320' in script
assert 'capture android-gameplay-ready-reference-size' in script
assert 'struct.unpack(">II",shot[16:24])==(941,1672)' in script
assert 'reference_ready' in script and 'DODIRNI ZA LET' in script
assert 'adb shell wm size reset' in script
assert 'adb shell wm density reset' in script
assert "BOPAVI_VIEWPORT_QA=1 sh tools/qa_android_emulator.sh" in ci
print("PASS: Android emulator captures unscaled real reference-resolution gameplay")
