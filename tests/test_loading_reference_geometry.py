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
    "FlightLoadingTrack.segment(inner.width()-6f,",
    "FlightLoadingLayout.captionBaselineY",
    "FlightLoadingLayout.track.left",
    "paint.shader = LinearGradient",
):
    assert required in view,required
assert "val logo=LoadingArtBox(91f,95f,850f,384f)" in layout
assert "val pilot=LoadingArtBox(110f,280f,780f,950f)" in layout
assert "val track=LoadingArtBox(161f,1350f,780f,1454f)" in layout
assert "const val captionBaselineY=1515f" in layout
assert "val pilotPaintedTop:Float get()=pilot.top+81f*pilot.height/512f" in layout
assert "assertTrue(l.captionBaselineY>l.track.bottom)" in test
assert "realReferenceArtworkRetainsItsNaturalProportions" in test
assert "titlePilotAndTrackStayInReferenceVerticalOrder" in test
assert "private fun ring(canvas: Canvas" in view
assert "android.graphics.DashPathEffect(" not in view
assert "paint.color=0xffffc70a.toInt()" not in view  # Splash gold uses its own palette
assert "paint.color=0xffffb800.toInt()" in view
assert "ring(canvas,104f,830f,61f,87f,-18f)" in view
assert "ring(canvas,80f,1230f,150f,190f,-21f)" in view
assert "coin(canvas," not in view
assert "ring(canvas,350f,989f,72f,106f,-25f)" in view
assert "trackWings(canvas)" in view
assert "0xffffbc27.toInt()" in view
assert "paint.pathEffect=android.graphics.DashPathEffect" not in view
assert "ring(canvas,546f,1057f,37f,54f,-21f)" in view
assert "ring(canvas,666f,1081f,25f,37f,-19f)" in view
assert "postInvalidateDelayed(60L)" in view
assert "isAttachedToWindow" in view
assert "android.os.SystemClock.uptimeMillis()-scanStartedAtMs," in view
assert "preparationFinished)" in view
assert "feather(canvas," not in view
assert 'val loading=FlightLoadingView(this,chosenPilot)' in main
assert "Thread({" in main and 'loading.completePreparation()' in main
assert "Thread.sleep" not in main
assert 'FlightLoadingView(this,selectedPilotChoice,startup=true)' in main
assert 'selectedScreen="splash"' in main
assert "},2450L)" in main  # Actual branded startup duration, retained deliberately
assert 'selectedScreen="loading"' in main
assert "progress.consumePerks()" in main
print("PASS: owner Splash retains original Bopi, cyan enamel bar and truthful loading")
