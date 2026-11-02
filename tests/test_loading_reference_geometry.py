#!/usr/bin/env python3
"""Supplied Splash: winged 3D gold/cyan bar, golden rings, original pilot."""
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
assert "val track=LoadingArtBox(175f,1320f,766f,1422f)" in layout
assert "const val captionBaselineY=1483f" in layout
assert "assertTrue(l.captionBaselineY>l.track.bottom)" in test
assert "realReferenceArtworkRetainsItsNaturalProportions" in test
assert "titlePilotAndTrackStayInReferenceVerticalOrder" in test
assert "private fun ring(canvas: Canvas" in view
assert "android.graphics.DashPathEffect(" not in view
assert "paint.color=0xffffc70a.toInt()" not in view  # Splash gold uses its own palette
assert "paint.color=0xffffb800.toInt()" in view
assert "ring(canvas,108f,880f,58f,92f,-18f)" in view
assert "coin(canvas," not in view
assert "ring(canvas,355f,1015f,69f,108f,-25f)" in view
assert "trackWings(canvas)" in view
assert "0xffffbc27.toInt()" in view
assert "paint.pathEffect=android.graphics.DashPathEffect" not in view
assert "ring(canvas,549f,1033f,37f,54f,-21f)" in view
assert "ring(canvas,658f,1047f,26f,40f,-19f)" in view
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
