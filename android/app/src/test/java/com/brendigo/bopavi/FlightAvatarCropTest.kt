package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The original body/wing art and full portrait share the 512x512 frame. */
class FlightAvatarCropTest {
    @Test fun sourceCropIsSquareAndInsideTheVerifiedSprite() {
        val crop=FlightAvatarCrop.source
        assertTrue(crop.left>=0 && crop.top>=0)
        assertTrue(crop.right<=512 && crop.bottom<=512)
        assertEquals(crop.width,crop.height)
        assertTrue(crop.width()>=300)
    }
}
