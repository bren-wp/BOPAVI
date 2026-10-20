#!/usr/bin/env python3
"""Require real Android UI state evidence before accepting release captures."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
qa=(repo/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
for label in (
    "STATE: flight-idle confirmed by native GameView",
    "STATE: flight-active confirmed by native GameView and Pause control",
    "FAIL: tapping PLAY did not reach Flight",
    "FAIL: first flap never reached active HUD/PAUSE state",
    "FAIL: RETRY did not reach idle Flight",
    "BOPI IS FLYING", "TAP TO FLY WITH", "PAUSE GAME",
):
    assert label in qa,label
assert "capture android-gameplay-ready" in qa
assert "capture android-gameplay-active" in qa
assert qa.index("test \"$flying\" -eq 1") < qa.index("capture android-gameplay-active")
assert qa.index("test \"$ready\" -eq 1") < qa.index("capture android-gameplay-ready")
assert "cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-gameplay-active.png" in qa
print("PASS: screenshots are gated by true native Flight idle/active/retry states")
