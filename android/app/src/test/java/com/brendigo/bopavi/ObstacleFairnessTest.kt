package com.brendigo.bopavi

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max

/**
 * Deterministic stress tests for all generated world families and level cycles.
 * This is a conservative corridor-reachability bound, not a substitute for
 * actual human flight replay on representative Android devices.
 */
class ObstacleFairnessTest {
    @Test fun seededGateRhythmsHaveBodyClearanceAndRecoverableAltitudeChanges() {
        val ordinals = (1L..150L).toList() +
            listOf(360L, 720L, 1_024L, 50_000L, 1_048_576L,
                1_048_577L, 10_000_000L, 1_000_000_000L)
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            for(ordinal in ordinals) {
                val level=LevelEngine.createStream(world,ordinal)
                for(gate in level.gates) {
                    // Moving lips, difficulty pulses and world-specific tidal
                    // variation must never close a physical 18-unit-radius body.
                    for(difficulty in 0..2) {
                        for(t in floatArrayOf(0f, .37f, 1.15f, 3.6f, 11f)) {
                            val opening=LevelEngine.opening(gate,t,difficulty)
                            assertTrue("Non-finite mouth world=$world ordinal=$ordinal",
                                opening.top.isFinite() && opening.bottom.isFinite())
                            assertTrue("Impassable mouth world=$world ordinal=$ordinal",
                                opening.bottom-opening.top>=144.9f)
                        }
                    }
                }
                for((a,b) in level.gates.zipWithNext()) {
                    val corridorDistance=b.x-a.x-a.width-36f
                    assertTrue("Overlapping shafts world=$world ordinal=$ordinal",
                        corridorDistance>0f)
                    // Worst hard mode, even if both lips oscillate away from
                    // each other. Reserve 4 extra logical units at each end.
                    fun safeHalf(g:LevelEngine.Gate):Float =
                        max(72.5f,(g.gap-30f)*.5f-5f)-18f-4f
                    val potentialShift=abs(b.center-a.center)+
                        a.movement+b.movement+10f
                    val necessaryShift=max(0f,potentialShift-safeHalf(a)-safeHalf(b))
                    // Maximum climb authority after repeated flaps: 255 world
                    // units/s. The time budget *excludes* both shaft widths and
                    // both body radii at worst-case 1.30x horizontal speed.
                    val flightSeconds=corridorDistance/(level.speed*1.30f)
                    val climbAuthority=255f*flightSeconds
                    assertTrue(
                        "Altitude transition too aggressive world=$world ordinal=$ordinal "+
                            "need=$necessaryShift climb=$climbAuthority",
                        necessaryShift<=climbAuthority+2f)
                }
            }
        }
    }

    @Test fun futureStreamTransitionsRemainConservativeForAllWorlds() {
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            for(ordinal in listOf(1L, 15L, 360L, 1_048_576L, 5_000_000L)) {
                val run=GameSimulation(LevelEngine.createStream(world,ordinal),
                    endless=true,initialOrdinal=ordinal)
                val last=run.level.gates.last()
                val first=run.upcomingGates().first()
                assertTrue("Unreachable seam world=$world ordinal=$ordinal",
                    abs(first.center-last.center)<=82.01f)
                val delta=run.upcomingGateX(first)-run.gateX(last)
                assertTrue("Gate seam gap out of expected cadence: $delta",
                    delta in 215f..231f)
            }
        }
    }
}
