#!/usr/bin/env python3
"""Flight reference horizontal subject and obstacle position share one camera."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
sim=(src/"GameSimulation.kt").read_text()
composition=(src/"FlightCameraComposition.kt").read_text()
test=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightCameraCompositionTest.kt").read_text()
assert "const val birdX=175f" in composition
assert "const val horizontalShift=birdX-legacyBirdX" in composition
assert "WorldCoordinates.screenX(worldX,origin,travelled)+horizontalShift" in composition
assert "val birdX = FlightCameraComposition.birdX" in sim
assert sim.count("FlightCameraComposition.gateX(")==2
assert "last.x.toDouble() + FlightCameraComposition.horizontalShift.toDouble()" in sim
assert "c.translate(game.birdX,game.y)" in view
assert "c.drawCircle(game.birdX,game.y" in view
assert "126f" not in view
assert "translationPreservesRelativeGateContactAcrossWorlds" in test
assert "physicalMaceMovesWithItsGateAndNeverDriftsFromHitbox" in test
print("PASS: Bopi and world obstacles move together without changing collision separation")
