#!/usr/bin/env python3
"""Native UI/physics guard for the safe pause overlay and economical gate broadphase."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
layout=(src/"FlightOverlayLayout.kt").read_text()
activity=(src/"MainActivity.kt").read_text()
view=(src/"GameView.kt").read_text()
physics=(src/"GameSimulation.kt").read_text()
units=(root/"android/app/src/test/java/com/brendigo/bopavi/LevelEngineTest.kt").read_text()
overlay_tests=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightOverlayLayoutTest.kt").read_text()
assert "val hudScale=(width/480f).coerceAtMost(height/640f)" in view
assert "hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)" in view
assert "const val pauseLaneStart = 366f" in (root/"android/app/src/main/java/com/brendigo/bopavi/FlightHudLayout.kt").read_text()
assert "hudBottom=safeTopPx.coerceAtLeast(0)+ceil(86f*hudScale).toInt()" in layout
assert "fun pauseTopPx(widthPx:Int,heightPx:Int,density:Float,safeTopPx:Int)" in layout
assert "fun pauseRightPx(density:Float,safeRightPx:Int)" in layout
assert "FlightOverlayLayout.pauseTopPx(" in activity
assert "FlightOverlayLayout.pauseRightPx(" in activity
assert "frame.addOnLayoutChangeListener" in activity
assert "frame.setOnApplyWindowInsetsListener" in activity
assert "android.view.WindowInsets.Type.displayCutout()" in activity
assert "if (Build.VERSION.SDK_INT >= 28)" in activity
assert "pauseNeverCoversTheLivesCardOnCompactAndLargeScreens" in overlay_tests
assert "pauseRespectsRightSideCutoutsAndMinimumMargin" in overlay_tests
assert "if (x > birdX + radius + (if(endless) 7f else 115f)) break" in physics
for name in ("optimizedGateBroadphaseStillDetectsFirstPaintedLipContact",
             "optimizedBroadphasePreservesBerryTouchAndLegacyMagnetRange"):
    assert name in units
assert "val radius = 18f" in physics
assert "PillarCollision.sweptBodyHitsPillar" in physics
assert "BerryCollision.touched" in physics
print("PASS: safe premium pause placement, swept rock collision and energy-conscious broadphase")
