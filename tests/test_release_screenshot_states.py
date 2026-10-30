#!/usr/bin/env python3
"""Require real Android UI state evidence before accepting release captures."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
qa=(repo/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
for label in (
    "STATE: flight-idle confirmed by native GameView",
    "STATE: flight-active confirmed with lives-only native HUD",
    "FAIL: tapping PLAY did not reach Flight",
    "FAIL: lives-only active GameView not accessible",
    "FAIL: RETRY did not reach idle Flight",
    "ŽIVOTI:", "TAP TO FLY WITH", "input keyevent 4",
):
    assert label in qa,label
assert 'and "ŽIVOTI:" in label(n)' in qa
assert '"DODIRNI ZA ZAMAH KRILIMA" in label(n)' in qa
assert 'and n.get("clickable")=="true"' in qa
assert "capture android-gameplay-ready" in qa
assert "capture android-gameplay-active" in qa
for state in ("options","records"):
    assert "capture android-"+state in qa,state
for state in ("privacy","terms"):
    assert '"android-'+state+'"' in qa,state
assert 'screencap","-p"' in qa
assert 'FAIL: original Android legal screenshot is invalid' in qa
assert 'for expected in ("OPCIJE IGRE"' in qa
assert 'for expected in ("MOJI REKORDI"' in qa
assert "capture android-pause" in qa
assert 'STATE: pause verified by real NASTAVI/PONOVO/IZLAZ dialog' in qa
assert 'FAIL: real Pause dialog/buttons did not appear' in qa
assert 'FAIL: Pause screenshot duplicates active gameplay' in qa
assert qa.index("capture android-gameplay-active") < qa.index("capture android-pause")
assert qa.index("capture android-pause") < qa.index("capture android-result")
assert qa.index("test \"$flying\" -eq 1") < qa.index("capture android-gameplay-active")
assert qa.index("test \"$ready\" -eq 1") < qa.index("capture android-gameplay-ready")
assert "cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-gameplay-active.png" in qa
print("PASS: screenshots are gated by true native Flight idle/active/retry states")
