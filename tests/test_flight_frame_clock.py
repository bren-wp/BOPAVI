#!/usr/bin/env python3
"""No physics catch-up on idle, paused, background or QA still-frame views."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text(encoding="utf-8")
clock=(src/"FlightFrameClock.kt").read_text(encoding="utf-8")
tests=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightFrameClockTest.kt").read_text(encoding="utf-8")
assert "private val frameClock=FlightFrameClock()" in view
assert "if(game.active && !paused && !qaWaitingForCapture && !game.finished)" in view
assert "frameClock.tick(now,running=true)?.let { game.step(it) }" in view
assert "else frameClock.reset()" in view
assert "if(firstFlap) frameClock.reset()" in view
assert "frameClock.reset()" in view.split("if(qaWaitingForCapture) {",1)[1].split("} else {",1)[0]
assert "for(i in upcoming.indices)" in view
assert "game.upcomingGates().withIndex()" not in view
assert "GameFramePacing.redrawDelayMillis(reducedMotion,powerSaveMode)" in view
assert "nanos<1_000_000L" in clock
assert "if(!running)" in clock and "reset()" in clock
assert "if(previous==null || nowNanos<previous)" in clock
for case in ("idlePreviewWaitingDoesNotAdvanceFirstFlap","pauseBackgroundAndQaCaptureEachDiscardElapsedTime","subMillisecondRepaintsAndClockRewindsStaySafe"):
    assert case in tests,case
print("PASS: no first-tap idle-time catch-up, monotonic frame deltas, vsync redraw and no indexed draw allocations")
