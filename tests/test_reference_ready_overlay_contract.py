#!/usr/bin/env python3
"""Ready gameplay has an authentic life HUD and source-proportional tutorial."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
layout=(src/"FlightReadyOverlayLayout.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightReadyOverlayLayoutTest.kt").read_text()
assert "if(!game.finished) {" in view
assert "drawHud(canvas)" in view
assert "drawFlightReadyTutorial(canvas)" in view
assert "val croatian=resources.configuration.locales.get(0).language==\"hr\"" in view
assert "DODIRNI ZA LET" in view and "Tap to fly" in view
assert "const val left=145f" in layout and "const val right=335f" in layout
assert "const val top=596f" in layout and "const val bottom=650f" in layout
assert "tutorialMatchesReferenceRelativeBounds" in unit
assert "liveHudStillFitsWhenGameIsReady" in unit
assert "rect(canvas,87f,671f,393f,746f" not in view
print("PASS: ready-state HUD and reference-sized instruction bubble")
