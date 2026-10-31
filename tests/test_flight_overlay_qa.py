"""The touch-only flight owns no Pause overlay; system Back provides safe menu."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text()
view=(src/"GameView.kt").read_text()
physics=(src/"GameSimulation.kt").read_text()
units=(r/"android/app/src/test/java/com/brendigo/bopavi/LevelEngineTest.kt").read_text()
qa=(r/"tools/qa_android_emulator.sh").read_text()
assert not (src/"FlightOverlayLayout.kt").exists()
assert "FlightOverlayLayout" not in activity
assert "gamePauseButton" not in activity
assert "private fun showPremiumPause(game:GameView)" in activity
assert "game.paused=true" in activity and "showPremiumPause(view)" in activity
assert "if(pauseDialog?.isShowing!=true)showPremiumPause(view)" in activity
assert "input keyevent 4" in qa
assert "val hudScale=(width/480f).coerceAtMost(height/640f)" in view
assert 'hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)' in view
assert "if (x > birdX + radius + (if(endless) 7f else 115f)) break" in physics
assert "optimizedGateBroadphaseStillDetectsFirstPaintedLipContact" in units
assert "PillarCollision.sweptBodyHitsPillar" in physics
print("PASS: no Pause HUD, system Back pause dialog, original collision broadphase")
