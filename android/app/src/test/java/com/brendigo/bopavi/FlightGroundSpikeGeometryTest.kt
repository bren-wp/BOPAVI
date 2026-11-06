package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightGroundSpikeGeometryTest {
    @Test fun spikeTipsRemainInExistingSolidGround() {
        val g=FlightGroundSpikeGeometry
        assertTrue(g.completelyWithinSolidFloor())
        assertEquals(753f,g.solidFloorTop,0f)
        assertEquals(800f,g.cameraBottom,0f)
        assertEquals(5,g.spikeCount)
        assertTrue(g.stoneTop>=g.solidFloorTop)
        for(i in 0 until g.spikeCount) {
            assertTrue("Spike $i cannot protrude into flyable space",
                g.tipY(i)>=g.solidFloorTop)
            assertTrue(g.tipY(i)<g.spineBottom)
            assertTrue(g.centerX(i)-g.spikeHalfWidth>0f)
            assertTrue(g.centerX(i)+g.spikeHalfWidth<480f)
            if(i>0)assertTrue(g.centerX(i)>g.centerX(i-1))
        }
    }

    @Test fun visualSpikesNeverReduceTheBirdsAllowedMovement() {
        val birdRadius=18f
        val maxBirdCenterY=FlightGroundSpikeGeometry.solidFloorTop-birdRadius
        for(i in 0 until FlightGroundSpikeGeometry.spikeCount)
            assertTrue(maxBirdCenterY+birdRadius<=
                FlightGroundSpikeGeometry.tipY(i))
    }

    @Test fun spikeGeometryRejectsInvalidIndex() {
        try {
            FlightGroundSpikeGeometry.centerX(-1)
            fail("Negative index must be rejected")
        } catch(_:IllegalArgumentException) {}
        try {
            FlightGroundSpikeGeometry.tipY(FlightGroundSpikeGeometry.spikeCount)
            fail("Out-of-range spike must be rejected")
        } catch(_:IllegalArgumentException) {}
    }
}
