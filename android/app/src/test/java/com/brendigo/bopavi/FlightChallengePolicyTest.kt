package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightChallengePolicyTest {
    @Test fun generatedWorldsAreFasterButBoundedAndDeterministic() {
        assertEquals(148f,FlightChallengePolicy.levelSpeed(0,0,0,0),0f)
        assertTrue(FlightChallengePolicy.levelSpeed(5,7,100,2)<=228f)
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            val level=LevelEngine.create(world,1)
            assertTrue(level.speed>=148f)
            assertTrue(level.speed<=228f)
            assertEquals(level,LevelEngine.create(world,1))
        }
    }

    @Test fun gatesAreTougherAfterWarmupWithoutImpossibleMouths() {
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            for(number in listOf(1,30,61,180,360,1048576)) {
                val level=LevelEngine.create(world,number)
                assertTrue(level.gates.all{it.gap>=164f})
                for((index,gate) in level.gates.withIndex()) {
                    if(index<2)assertTrue(gate.gap>=164f)
                    val hard=LevelEngine.opening(gate,0f,2)
                    assertTrue(hard.bottom-hard.top>=144.9f)
                    assertTrue(hard.top>0f && hard.bottom<755f)
                }
            }
        }
        assertEquals(195f,FlightChallengePolicy.gateGap(195f,0,4),0f)
        assertEquals(164f,FlightChallengePolicy.gateGap(164f,6,5),0f)
    }

    @Test fun movingPillarsStayBoundedAndZeroRemainsStill() {
        assertEquals(0f,FlightChallengePolicy.gateMovement(0f,8,5),0f)
        assertEquals(12f,FlightChallengePolicy.gateMovement(12f,2,5),0f)
        assertEquals(17.5f,FlightChallengePolicy.gateMovement(12f,5,5),0f)
        assertTrue(FlightChallengePolicy.gateMovement(16f,8,5)<=22f)
    }
}
