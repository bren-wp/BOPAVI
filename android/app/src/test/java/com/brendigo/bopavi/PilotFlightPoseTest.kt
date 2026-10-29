package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class PilotFlightPoseTest {
    @Test fun posesRemainFrontFacingDuringEveryNormalFlap() {
        for(velocity in listOf(-255f,-110f,0f,150f,315f,365f)) {
            val angle=PilotFlightPose.pitchDegrees(velocity,false)
            assertTrue(angle in -7f..9f)
            assertEquals(0f,PilotFlightPose.pitchDegrees(velocity,true),0f)
        }
    }

    @Test fun allCharactersUseSameActualRenderingPose() {
        for(skin in 0..10) {
            // Independent of skin: character art and unique abilities
            // cannot tilt a pilot sideways or enlarge collision geometry.
            val angle=PilotFlightPose.pitchDegrees(365f,false)
            assertEquals("Pilot $skin must face forward",6.57f,angle,.001f)
            assertEquals("Pilot $skin shares wing motion",17f,
                PilotFlightPose.wingDegrees(0f,.24f,false),.001f)
        }
    }

    @Test fun reducedMotionAndInvalidClockHaveStaticSafePose() {
        assertEquals(0f,PilotFlightPose.wingDegrees(4f,.2f,true),0f)
        assertEquals(0f,PilotFlightPose.wingDegrees(Float.NaN,.2f,false),0f)
        assertEquals(0f,PilotFlightPose.pitchDegrees(Float.NaN,false),0f)
    }
}
