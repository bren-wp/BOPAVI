#!/usr/bin/env python3
"""Require real Android UI state evidence before accepting release captures."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
qa=(repo/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
for label in (
    "STATE: flight-idle confirmed by native GameView",
    "STATE: real native active Gameplay with live SCORE, local coins, crystals and Pause",
    "FAIL: tapping PLAY did not reach Flight",
    "FAIL: active GameView score/coins/crystals/pause accessibility not found",
    "FAIL: RETRY did not reach idle Flight",
    "ŽIVOTI:", "TAP TO FLY WITH", "input keyevent 4",
    "REZULTAT:", "NOVČIĆI:", "KRISTALI:", "GORE DESNO PAUZA",
):
    assert label in qa,label
assert 'and "ŽIVOTI:" in label(n)' in qa
assert '"DODIRNI ZA ZAMAH KRILIMA" in label(n)' in qa
assert 'and n.get("clickable")=="true"' in qa
assert "capture android-gameplay-ready" in qa
assert "capture android-gameplay-active" in qa
assert "capture android-game-over-reference-size" in qa
assert 'struct.unpack(">II",data[16:24])==(941,1672)' in qa
assert '"DODIRNI ZA ZAMAH KRILIMA" in labels' in qa
for state in ("options","records"):
    assert "capture android-"+state in qa,state
for state in ("privacy","terms"):
    assert '"android-'+state+'"' in qa,state
assert 'screencap","-p"' in qa
assert 'FAIL: original Android legal screenshot is invalid' in qa
# Release screenshots use the owner's real nine-row Settings page.
# Difficulty is now checked through the native Controls dialog and must
# persist through two real selections before capture is accepted.
assert 'for label in ("SETTINGS","Music","Sound Effects","Notifications",' in qa
assert '"Privacy Policy","Terms of Service","Restore Purchases"' in qa
assert "FAIL: Home Settings inaccessible" in qa
for contract in (
    'tap("Touch",True)',
    'tap("Difficulty: Standardno")',
    'tap("Opušteno")',
    'find("Difficulty: Opušteno")',
    'tap("Standardno")',
    'find("Difficulty: Standardno")',
    "PASS: real nine-row Settings, dynamic difficulty dialog and persisted selection",
):
    assert contract in qa,contract
assert 'for expected in ("LEADERBOARD","OFFLINE","THIS DEVICE","BEST SCORE"' in qa
assert "FAIL: Leaderboard Home action missing" in qa
assert "PASS: local records screen visible and returns Home" in qa
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
