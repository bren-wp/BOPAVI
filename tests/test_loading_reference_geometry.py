#!/usr/bin/env python3
"""Loading/Splash visual geometry uses real uncropped Bopi layers, no fake wait."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
src=repo/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"FlightLoadingView.kt").read_text()
layout=(src/"FlightLoadingLayout.kt").read_text()
test=(repo/"android/app/src/test/java/com/brendigo/bopavi/FlightLoadingLayoutTest.kt").read_text()
main=(src/"MainActivity.kt").read_text()
assert "bopiportrait" not in view
for required in ("R.drawable.bopi0","R.drawable.bopileft0","R.drawable.bopiright0",
                 "image(canvas, leftWing, pilotRect)",
                 "image(canvas, rightWing, pilotRect)",
                 "image(canvas, body, pilotRect)",
                 "image(canvas, brand, logoRect)",
                 "leftWing?.recycle()","rightWing?.recycle()",
                 'contentDescription = "Loading level"',
                 "if(preparationFinished){"):
    assert required in view,required
assert "val logo=LoadingArtBox(93f,113f,848f,341f)" in layout
assert "val pilot=LoadingArtBox(113f,346f,828f,1061f)" in layout
assert "realReferenceArtworkRetainsItsNaturalProportions" in test
assert 'val track=LoadingArtBox(118f,1317f,824f,1423f)' in layout
assert "private fun ring(canvas: Canvas" in view
assert "coin(canvas," not in view
assert "ring(canvas,104f,862f,75f,92f,-23f)" in view
assert "ring(canvas,28f,1197f,149f,176f,-27f)" in view
assert "postInvalidateDelayed(60L)" in view
assert "isAttachedToWindow" in view
assert "val phase=(android.os.SystemClock.uptimeMillis()%1400L)/1400f" in view
assert "feather(canvas,true)" in view and "feather(canvas,false)" in view
assert "titlePilotAndTrackStayInReferenceVerticalOrder" in test
assert 'val loading=FlightLoadingView(this,chosenPilot)' in main
assert "Thread({" in main and 'loading.completePreparation()' in main
assert "else showHome()" in main
assert "Thread.sleep" not in main
assert 'selectedScreen="loading"' in main
assert "progress.consumePerks()" in main
print("PASS: Splash/Loading preserves original winged Bopi, natural aspect and real preparation")
