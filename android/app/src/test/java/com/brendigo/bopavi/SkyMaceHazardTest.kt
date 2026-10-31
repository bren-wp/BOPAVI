package com.brendigo.bopavi

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SkyMaceHazardTest {
    @Test fun introductoryGatesAreSafeAndOnlyApprovedWorldHasMaces() {
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            val gates=LevelEngine.create(world,1).gates
            for(index in gates.indices) {
                val enabled=SkyMaceHazard.enabled(gates[index],index)
                assertTrue("Unexpected obstacle world=$world index=$index",
                    enabled == (world == 0 && index >= 4 && index % 3 == 1))
            }
        }
    }

    @Test fun drawnPoseIsRealCollisionAndCenterLaneRemainsOpen() {
        for(number in listOf(1,60,180,360,1_048_576)) {
            val gates=LevelEngine.create(0,number).gates
            for((index,gate) in gates.withIndex()) {
                if(!SkyMaceHazard.enabled(gate,index))continue
                for(difficulty in 0..2)for(time in floatArrayOf(0f,.5f,2.5f,7.5f)) {
                    val opening=LevelEngine.opening(gate,time,difficulty)
                    val x=150f
                    val bobX=SkyMaceHazard.bobX(gate,x,time)
                    val bobY=SkyMaceHazard.bobY(gate,opening,time,index)
                    val laneCenter=(opening.top+opening.bottom)*.5f
                    // The safe centerline is clear even on hard difficulty.
                    assertTrue("Mace blocks the centerline level=$number index=$index",
                        abs(bobY-laneCenter)>SkyMaceHazard.radius+18f+5f)
                    assertTrue("Rendered mace must cause physical contact",
                        SkyMaceHazard.sweptHit(gate,index,bobX,bobY,bobY,18f,
                            x,x,opening,opening,time,time))
                    assertFalse("Centerline must not hit a visual-only mace",
                        SkyMaceHazard.sweptHit(gate,index,bobX,laneCenter,
                            laneCenter,18f,x,x,opening,opening,time,time))
                }
            }
        }
    }

    @Test fun movingMaceCrossingTheBirdBetweenEndpointsIsDetected() {
        val gate=LevelEngine.create(0,1).gates[4]
        val opening=LevelEngine.opening(gate,0f)
        val x=110f
        val bobX=SkyMaceHazard.bobX(gate,x,0f)
        val bobY=SkyMaceHazard.bobY(gate,opening,0f,4)
        assertTrue(SkyMaceHazard.sweptHit(gate,4,bobX,
            bobY-50f,bobY+50f,18f,x,x,opening,opening,0f,0f))
    }
}
