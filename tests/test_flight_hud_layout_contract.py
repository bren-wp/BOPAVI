"""Only one screen-safe HUD card is allowed in a flight."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
layout=(src/"FlightHudLayout.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHudLayoutTest.kt").read_text()
hud=view.split("    private fun drawHud(c:Canvas){",1)[1].split("    /** Developer QA outlines",1)[0]
assert "val lives=FlightHudBox(122f,16f,358f,92f)" in layout
assert "val heartCenters=floatArrayOf(178f,240f,302f)" in layout
assert "const val lifeCountRight=334f" in layout
assert "val score =" not in layout and "val food =" not in layout and "val ability =" not in layout
assert "val notice =" not in layout and "pauseLaneStart" not in layout
assert 'text(c,"ŽIVOTI"' in hud and 'drawHudHeart(' in hud
assert 'contentDescription=lastHudAccessibility' in hud
assert "cachedScore" not in view and "cachedFood" not in view
assert "FlightHudLayout.movingWarningVisible(g.movement,x,game.birdX)" in view
assert "onlyLivesCardIsCenteredAndFullyOnScreen" in unit
assert "onlyUpcomingMovingPillarsGetWarnings" in unit
print("PASS: centered hearts-only display, collision-safe moving gate warnings")
