#!/usr/bin/env python3
"""Supplied Splash geometry: uncropped 512px pilot and slim cyan Loading bar."""
from pathlib import Path
repo=Path(__file__).resolve().parents[1]
src=repo/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"FlightLoadingView.kt").read_text()
layout=(src/"FlightLoadingLayout.kt").read_text()
test=(repo/"android/app/src/test/java/com/brendigo/bopavi/FlightLoadingLayoutTest.kt").read_text()
main=(src/"MainActivity.kt").read_text()
assert "bopiportrait" not in view
for required in (
    "R.drawable.bopi0","R.drawable.bopileft0","R.drawable.bopiright0",
    "image(canvas, leftWing, pilotRect)",
    "image(canvas, rightWing, pilotRect)",
    "image(canvas, body, pilotRect)",
    "image(canvas, brand, logoRect)",
    "leftWing?.recycle()","rightWing?.recycle()",
    'if(startup)"BOPAVI se pokreće" else "Loading level"',
    "if(preparationFinished)1f",
    "FlightLoadingLayout.captionBaselineY",
    "FlightLoadingLayout.track.left",
    "paint.shader = LinearGradient",
):
    assert required in view,required
assert "val logo=LoadingArtBox(28f,96f,913f,364f)" in layout
assert "val pilot=LoadingArtBox(90f,381f,850f,1141f)" in layout
assert "val track=LoadingArtBox(222f,1482f,719f,1563f)" in layout
assert "const val captionBaselineY=1468f" in layout
assert "realReferenceArtworkRetainsItsNaturalProportions" in test
assert "titlePilotAndTrackStayInReferenceVerticalOrder" in test
assert "private fun ring(canvas: Canvas" in view
assert "android.graphics.DashPathEffect(" in view
assert "coin(canvas," not in view
assert "ring(canvas,806f,957f,166f,186f,-10f)" in view
assert "ring(canvas,556f,1115f,64f,75f,-14f)" in view
assert "postInvalidateDelayed(60L)" in view
assert "isAttachedToWindow" in view
assert "val phase=(android.os.SystemClock.uptimeMillis()%1550L)/1550f" in view
assert "feather(canvas," not in view
assert 'val loading=FlightLoadingView(this,chosenPilot)' in main
assert "Thread({" in main and 'loading.completePreparation()' in main
assert "Thread.sleep" not in main
assert 'FlightLoadingView(this,selectedPilotChoice,startup=true)' in main
assert 'selectedScreen="splash"' in main
assert "},1450L)" in main
assert 'selectedScreen="loading"' in main
assert "progress.consumePerks()" in main
print("PASS: owner Splash retains original Bopi, cyan enamel bar and truthful loading")
