#!/usr/bin/env python3
"""CI contract: real Pause captured via binary PNG screencap at source size."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
script=(root/"tools/qa_android_pause_reference.py").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
for required in (
    'adb("shell","wm","size","941x1672")',
    'adb("shell","wm","density","320")',
    'tap(expect_one("▶  PLAY"))',
    'tap(active)',
    'adb("shell","input","keyevent","4")',
    'assert_pause()',
    'screenshot("android-pause-reference-size")',
    'assert (w,h)==(941,1672)',
    'tap(expect_one("NASTAVI"))',
    '"FAIL: Pause remained after Resume"',
    'adb("shell","wm","size","reset")',
    'adb("shell","wm","density","reset")',
):
    assert required in script,required
assert "python3 tools/qa_android_pause_reference.py" in qa
print("PASS: authentic native Pause capture and resume contract")
