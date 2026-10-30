#!/usr/bin/env python3
"""Require a real native Loading capture with original reference dimensions."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
qa=(root/"tools/qa_android_loading.py").read_text()
workflow=(root/".github/workflows/native-ci.yml").read_text()
for expected in (
    'REFERENCE_OUT = Path("qa/screenshots/android-loading-reference-size.png")',
    'adb("shell", "wm", "size", "941x1672")',
    'adb("shell", "wm", "density", "320")',
    'ref_png=adb("exec-out","screencap","-p")',
    'struct.unpack(">II",ref_png[16:24])==(941,1672)',
    '"FAIL: reference-size Loading screenshot does not match 941x1672"',
    'adb("shell", "wm", "size", "reset")',
    'adb("shell", "wm", "density", "reset")',
    'assert sum(n.get("content-desc")=="Loading level" for n in ref_nodes)==1',
):
    assert expected in qa,expected
assert "python3 tools/qa_android_loading.py" in workflow
print("PASS: reference Loading capture is native, checked and reset")
