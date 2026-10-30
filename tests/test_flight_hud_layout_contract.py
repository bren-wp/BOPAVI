"""Only one screen-safe HUD card is allowed in a flight."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
layout=(src/"FlightHudLayout.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHudLayoutTest.kt").read_text()
hud=view.split("    private fun drawHud(c:Canvas){",1)[1].split("    /** Developer QA outlines",1)[0]
assert "val lives=FlightHudBox(12f,16f,244f,84f)" in layout
assert "val heartCenters=floatArrayOf(116f,169f,222f)" in layout
assert "const val avatarRadius=27f" in layout
assert "val score =" not in layout and "val food =" not in layout and "val ability =" not in layout
assert "val notice =" not in layout and "pauseLaneStart" not in layout
assert 'birdBitmap?.let' in hud and 'drawHudHeart(' in hud
assert 'contentDescription=lastHudAccessibility' in hud
assert "cachedScore" not in view and "cachedFood" not in view
assert "FlightHudLayout.movingWarningVisible(g.movement,x,game.birdX)" in view
assert "onlyLivesCardIsLeftAlignedAndFullyOnScreen" in unit
assert "onlyUpcomingMovingPillarsGetWarnings" in unit
print("PASS: left-aligned lives-only HUD, collision-safe moving gate warnings")
