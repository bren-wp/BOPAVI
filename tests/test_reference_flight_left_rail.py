"""The flight must not resurrect legacy score/wallet/status rails."""
from pathlib import Path
view=(Path(__file__).resolve().parents[1]/
      "android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
hud=view.split("    private fun drawHud(c:Canvas){",1)[1].split("    /** Developer QA outlines",1)[0]
for obsolete in ("hudOrb(","hudGoldCoin(","hudPilotPortrait(","game.magnetTime",
                 "game.shield","game.score()","game.foodCollected",
                 '"BODOVI"','"HRANA"','"MOĆ"'):
    assert obsolete not in hud,obsolete
assert 'Životi: $lives/' in hud and 'birdBitmap?.let' in hud
assert 'game.flap()' in view
assert "override fun onTouchEvent(event:MotionEvent)" in view
print("PASS: no legacy flight rail, only actual portrait, lives and tap-to-flap")
