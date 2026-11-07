#!/usr/bin/env python3
"""Shop-only full-size Android UI smoke is required in API36 workflow."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
source=(root/"tools/qa_android_shop_focus.py").read_text()
workflow=(root/".github/workflows/native-ci.yml").read_text()
for needed in (
    'adb("shell","wm","size","941x1672")',
    'adb("shell","wm","density","320")',
    'screenshot("android-shop-focus-reference-size")',
    'require("OWNED:",minimum=6)',
    'require("HOW TO EARN STARS",clickable=True)',
    'require("HOW TO EARN COINS",clickable=True)',
    'assert adb("shell","pidof",APP)==pid',
    'adb("shell","wm","size","reset")',
    'adb("shell","wm","density","reset")',
):
    assert needed in source,needed
assert "python3 tools/qa_android_shop_focus.py" in workflow
print("PASS: focused native Shop screenshot and wallet controls are checked on API36")
