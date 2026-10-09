package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

/** Deterministic gameplay contract, independent of Android framework or rendering. */
class LevelEngineTest {

    @Test fun visiblePillarCapCollisionKillsImmediatelyWithoutShield() {
        // The front edge of the wide cap overlaps Bopi, though the thin shaft does not.
        val level=LevelEngine.create(0,2)
        val near=level.gates[0].copy(x=154f,center=510f,gap=160f,movement=0f,coin=false,star=false,power=0)
        val synthetic=level.copy(gates=listOf(near),speed=140f,wind=0f)
        val flight=GameSimulation(synthetic)
        flight.flap()
        flight.step(1f/60f)
        assertTrue("Visible cap overlap must end the run in the same simulation step",flight.finished)
        assertFalse(flight.won)
        assertEquals(0,flight.passed)
    }

    @Test fun shieldAbsorbsExactlyOneCapImpact() {
        val level=LevelEngine.create(0,2)
        val near=level.gates[0].copy(x=154f,center=510f,gap=160f,movement=0f,coin=false,star=false,power=0)
        val flight=GameSimulation(level.copy(gates=listOf(near),speed=140f,wind=0f),initialShield=1)
        flight.flap()
        flight.step(1f/60f)
        assertFalse(flight.finished)
        assertEquals(0,flight.shield)
        assertTrue(flight.invulnerable>0f)
    }
    @Test fun illustratedBirdSurvivesSafeCapOverlap() {
        val level=LevelEngine.create(0,2)
        val safe=level.gates[0].copy(x=154f,center=366f,gap=220f,movement=0f,coin=false,star=false,power=0)
        val run=GameSimulation(level.copy(gates=listOf(safe),speed=140f,wind=0f))
        run.flap();run.step(1f/60f)
        assertFalse("Do not kill bird when its body remains inside visible opening",run.finished)
    }
    @Test fun sameMotionAcross60_90_120HzFrames() {
        val level=LevelEngine.create(0,2)
        val at60=GameSimulation(level)
        val at90=GameSimulation(level)
        val at120=GameSimulation(level)
        at60.flap();at90.flap();at120.flap()
        repeat(36){at60.step(1f/60f)}
        repeat(54){at90.step(1f/90f)}
        repeat(72){at120.step(1f/120f)}
        assertFalse(at60.finished)
        assertFalse(at90.finished)
        assertFalse(at120.finished)
        assertEquals(at60.distance,at90.distance,0.2f)
        assertEquals(at60.distance,at120.distance,0.2f)
        // Semi-implicit integration permits small bounded displacement differences.
        assertEquals(at60.y,at90.y,4f)
        assertEquals(at60.y,at120.y,4f)
    }

    @Test fun nextLevelArrivesWithoutDistanceResetOrBlankTransition() {
        val level=LevelEngine.create(0,2)
        val safe=level.gates.first().copy(x=20f,center=366f,gap=220f,movement=0f,coin=false,star=false,power=0)
        val game=GameSimulation(level.copy(gates=listOf(safe),speed=140f,wind=0f),endless=true)
        game.flap(); game.step(1f/60f)
        assertEquals(1,game.completionCount)
        assertEquals(3L,game.displayLevel)
        assertTrue("Global distance must remain monotonic",game.distance>0f)
        assertEquals("Next gate should already be in view",300f,game.gateX(game.level.gates[0]),0.01f)
        assertTrue(game.levelTransition>0f)
        assertTrue(game.active)
        assertFalse(game.finished)
    }

    @Test fun worldsStayIndependentAndHaveSafeGaps() {
        assertEquals(8, LevelEngine.WORLD_COUNT)
        assertEquals(8, LevelEngine.names.toSet().size)
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            for (n in listOf(1, 5, 15, 30, 60, 360, 1024, LevelEngine.LEVELS_PER_WORLD)) {
                val level = LevelEngine.create(world,n)
                assertEquals(world,level.world)
                assertTrue(level.gates.size >= 11)
                assertTrue(level.gates.all { it.kind == world })
                for(gate in level.gates) {
                    for(t in listOf(0f, 1.25f, 2.5f, 8f)) {
                        val opening = LevelEngine.opening(gate,t)
                        assertTrue("Gap too tight: w=$world level=$n",opening.bottom-opening.top >= 144.9f)
                        assertTrue(opening.top > 0f && opening.bottom < 755f)
                    }
                }
            }
        }
    }

    @Test fun levelIdentityRemainsStableAndUnique() {
        val seen = HashSet<String>()
        for (n in 1..256) {
            val first=LevelEngine.create(0,n)
            val repeat=LevelEngine.create(0,n)
            val sig=LevelEngine.signature(first)
            assertEquals(sig,LevelEngine.signature(repeat))
            assertTrue("Collision at level $n",seen.add(sig))
        }
        val nextCycle=LevelEngine.createStream(0,LevelEngine.LEVELS_PER_WORLD.toLong()+1)
        assertEquals(0,nextCycle.world)
        assertNotEquals(LevelEngine.signature(LevelEngine.create(0,1)),LevelEngine.signature(nextCycle))
    }

    @Test fun coinsAreEarnedOnlyOnMilestones() {
        assertEquals(0,LevelEngine.milestoneReward(1))
        assertEquals(20,LevelEngine.milestoneReward(5))
        assertEquals(50,LevelEngine.milestoneReward(15))
        assertEquals(100,LevelEngine.milestoneReward(30))
        assertEquals(200,LevelEngine.milestoneReward(60))
        assertEquals(0,LevelEngine.milestoneReward(61))
    }


    @Test fun movingHazardsKeepSafeGeometryWithoutPerFrameAllocation() {
        for (world in listOf(3, 6, 7)) {
            val gate = LevelEngine.create(world, 120).gates[0].copy(movement=16f)
            for (frame in 0..720) {
                val bounds = LevelEngine.opening(gate, frame / 120f)
                assertTrue(bounds.top.isFinite() && bounds.bottom.isFinite())
                assertTrue("Animated opening must remain traversable", bounds.bottom - bounds.top >= 144.9f)
            }
        }
    }

    @Test fun shieldDoesNotConsumeMultipleChargesFromOneContinuousImpact() {
        val level = LevelEngine.create(0, 2)
        val obstacle = level.gates[0].copy(x=154f, center=510f, gap=160f, movement=0f, coin=false, star=false, power=0)
        val game = GameSimulation(level.copy(gates=listOf(obstacle), speed=140f, wind=0f), initialShield=2)
        game.flap()
        repeat(8) { game.step(1f / 120f) }
        assertEquals("One ongoing collision may only consume one shield charge", 1, game.shield)
        assertFalse(game.finished)
    }

    @Test fun difficultyModesAdjustPhysicsButRetainSafeOpening() {
        val level=LevelEngine.create(0,2)
        val easy=GameSimulation(level,difficulty=0)
        val normal=GameSimulation(level,difficulty=1)
        val hard=GameSimulation(level,difficulty=2)
        for(game in listOf(easy,normal,hard)){game.flap();game.step(1f/60f)}
        assertTrue(easy.distance < normal.distance && normal.distance < hard.distance)
        assertTrue(easy.y < normal.y && normal.y < hard.y)
        assertEquals(level.gates.size,easy.level.gates.size)
        assertEquals(level.gates.size,hard.level.gates.size)
        assertEquals(2,GameSimulation(level,difficulty=42).difficulty)
    }

    @Test fun boundaryValuesCannotGenerateInvalidLevels() {
        for(world in 0..7) {
            assertEquals(1,LevelEngine.create(world,1).number)
            assertEquals(LevelEngine.LEVELS_PER_WORLD,LevelEngine.create(world,LevelEngine.LEVELS_PER_WORLD).number)
        }
    }
}
