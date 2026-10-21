#!/usr/bin/env python3
"""Static CI guard: seamless batch promotion never allocates identical pickup arrays."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
simulation=(src/"GameSimulation.kt").read_text(encoding="utf-8")
view=(src/"GameView.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/LevelEngineTest.kt").read_text(encoding="utf-8")

assert "private fun clearOrResize(flags:BooleanArray,size:Int):BooleanArray" in simulation
assert "if(flags.size!=size)return BooleanArray(size)" in simulation
assert "flags.fill(false)" in simulation
for name in ("collectedFood","collectedHearts","collectedCoins",
             "collectedStars","collectedPowers"):
    assert f"{name} = clearOrResize({name},gateCount)" in simulation,name
assert "marathonBatchReusesPickupStateWithoutLeakingCollectedRewards" in unit
assert "GameFramePacing.redrawDelayMillis(reducedMotion,powerSaveMode)" in view
assert "private val frameClock=FlightFrameClock()" in view
assert "frameClock.tick(now,running=true)?.let { game.step(it) }" in view
print("PASS: pickup-state reuse, no reward leakage and adaptive vsync scheduling")
