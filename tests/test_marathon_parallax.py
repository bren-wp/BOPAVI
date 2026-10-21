#!/usr/bin/env python3
"""Marathon scenery has its own Double-precision, render-only phase."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
sim=(src/"GameSimulation.kt").read_text()
engine=(src/"LevelEngine.kt").read_text()
view=(src/"GameView.kt").read_text()
tests=(root/"android/app/src/test/java/com/brendigo/bopavi/LevelEngineTest.kt").read_text()
assert "internal val preciseTravel:Double get() = preciseDistance" in sim
assert "fun offset(distance: Double, layer: Int" in engine
assert "fun wrapped(distance:Double,speed:Double,period:Float)" in engine
assert "fun wave(distance:Double,frequency:Double)" in engine
assert "ParallaxScenery.wave(game.preciseTravel,.0011)" in view
assert "ParallaxScenery.wave(game.preciseTravel,.00045)" in view
assert "ParallaxScenery.offset(game.preciseTravel,layer,reducedMotion)" in view
assert view.count("ParallaxScenery.wrapped(game.preciseTravel,")>=6
assert "game.distance*" not in view and "sin(game.distance" not in view
assert "endlessSceneryKeepsSubpixelMotionPastFloatLimit" in tests
assert "longFlightSceneryPhasesAreBoundedAndMotionOnly" in tests
assert "val radius = 18f" in sim and "1f / 120f" in sim
print("PASS: precision-preserving scenery projection, no gameplay physics changes")
