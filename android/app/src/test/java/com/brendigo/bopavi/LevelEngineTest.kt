package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

/** Deterministic gameplay contract, independent of Android framework or rendering. */
class LevelEngineTest {
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

    @Test fun boundaryValuesCannotGenerateInvalidLevels() {
        for(world in 0..7) {
            assertEquals(1,LevelEngine.create(world,1).number)
            assertEquals(LevelEngine.LEVELS_PER_WORLD,LevelEngine.create(world,LevelEngine.LEVELS_PER_WORLD).number)
        }
    }
}
