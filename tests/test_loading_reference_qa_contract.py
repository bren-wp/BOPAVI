#!/usr/bin/env python3
"""Require a real native Loading capture with original reference dimensions."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
qa=(root/"tools/qa_android_loading.py").read_text()
workflow=(root/".github/workflows/native-ci.yml").read_text()
runner=(root/"tools/qa_android_emulator.sh").read_text()
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
# The user-approved PLAY button is now part of production Home. Regression
# run 43653896270 proved a stale three-action Loading QA assumption blocked
# Android15 after successful rotation, gameplay and reference Loading capture.
for action in ("Pokreni igru", "Otvori izbor likova",
               "Otvori trgovinu nadogradnji", "Otvori opcije igre"):
    assert action in qa, action
assert 'if set(actions)==expected and len(actions)==4:break' in qa
assert 'assert len(actions)==4 and set(actions)==expected' in qa
assert 'n.get("enabled")=="true"' in qa
assert 'three-action Home missing' not in qa
assert 'len(actions)==3' not in qa
assert 'n.get("text","")=="PLAY"' not in qa
assert "python3 tools/qa_android_loading.py" in runner
assert "BOPAVI_VIEWPORT_QA=1 sh tools/qa_android_emulator.sh" in workflow
print("PASS: reference Loading native capture/reset and four real Home actions including PLAY")
