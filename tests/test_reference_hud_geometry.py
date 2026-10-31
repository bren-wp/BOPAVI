"""Lives-only gameplay HUD. Scores and food remain on Results and saved records."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
view=(root/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
activity=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
draw=view.split("    private fun drawHud(c:Canvas){",1)[1].split("    /** Developer QA outlines",1)[0]
assert "drawHud(canvas)" in view
assert "hudTopSafeInsetPx+4f*hudScale" in view
for required in ('hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)',
                 'birdBitmap?.let', 'drawHudHeart(c,h.heartCenters[i],h.heartBaseline,i<lives)',
                 'FlightHudReadability.lifeCountColor(lives,game.maxLives)',
                 '" Posljednji život."', 'Životi: $lives/'):
    assert required in draw,required
for obsolete in ('"BODOVI"','"HRANA"','"MOĆ"','hudGlass(c,h.score','hudGlass(c,h.food',
                 'drawHudPowerIcon(', 'drawHudCaption(', 'game.score()', 'game.foodCollected', 'cachedLivesLabel', 'drawHudRightText('):
    assert obsolete not in draw,obsolete
assert 'progress.recordRun(g)' in activity
assert 'recordSummaryRow("NAJBOLJI REZULTAT"' in activity
print("PASS: only lives visible in flight; real score and food remain in Results")
