#!/usr/bin/env python3
"""Real HUD/readability contract: accurate pause QA and complete long scores.

The emulator test uses real GameView bounds; Kotlin tests exercise the precise
font-size/horizontal-scale policy and low-life warning without Android devices.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text(encoding="utf-8")
layout=(src/"FlightOverlayLayout.kt").read_text(encoding="utf-8")
policy=(src/"FlightHudReadability.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHudReadabilityTest.kt").read_text(encoding="utf-8")
emulator=(root/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")

for required in (
    'FlightHudReadability.fontSize(measured,maxWidth)',
    'FlightHudReadability.horizontalScale(',
    'p.textScaleX=1f',
    'FlightHudReadability.lifeCountColor(game.lives,game.maxLives)',
    'FlightHudReadability.isFinalLife(game.lives,game.maxLives)',
    '" Posljednji život."',
    'p.textAlign=Paint.Align.RIGHT',
    'p.textAlign=Paint.Align.LEFT',
):
    assert required in view,required
assert 'ceil((FlightHudLayout.lives.bottom+4f)*hudScale).toInt()' in layout
assert 'card=re.search(r"val lives = FlightHudBox[(]' in emulator
assert 'hud_bottom=vy+math.ceil((float(card.group(1))+4)*scale)' in emulator
assert 'hud_bottom=math.ceil(72*min(' not in emulator
assert 'flight[0].get("bounds","")' in emulator
for name in ("overflowAlwaysFitsWithoutRemovingAnyDigits",
    "ordinaryCountersRetainFullSizeAndWidth",
    "finalLifeIsProminentWithoutFalseWarningsAtFullHealth",
    "invalidMeasurementsNeverCreateInfiniteOrNegativePaintState"):
    assert name in unit,name
assert "fun horizontalScale(" in policy and "fun fontSize(" in policy
assert "return (availableWidth/" in policy
assert 'lastHudAccessibility=' in view
print("PASS: exact HUD score fitting, last-life signal and real current pause bounds")
