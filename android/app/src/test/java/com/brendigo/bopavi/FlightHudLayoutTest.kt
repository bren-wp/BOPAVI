package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightHudLayoutTest {
    @Test fun onlyLivesCardIsLeftAlignedAndFullyOnScreen() {
        val h=FlightHudLayout
        assertEquals(99.5f,(h.lives.left+h.lives.right)/2f,.001f)
        assertTrue(h.lives.right<190f)
        assertTrue(h.lives.left>=0f && h.lives.right<=480f)
        assertTrue(h.lives.top>=0f && h.lives.bottom<=800f)
        assertTrue(h.heartCenters.toList().zipWithNext().all{(a,b)->b-a>=34f})
        for(x in h.heartCenters){
            assertTrue(h.lives.encloses(x-h.heartHalfWidth,h.heartBaseline-10f))
            assertTrue(h.lives.encloses(x+h.heartHalfWidth,h.heartBaseline+7f))
        }
        assertEquals(h.heartCenters.average().toFloat(),119f,.001f)
        assertTrue(h.lives.encloses(h.avatarX-h.avatarRadius,h.avatarY-h.avatarRadius))
        assertTrue(h.lives.encloses(h.avatarX+h.avatarRadius,h.avatarY+h.avatarRadius))
    }

    @Test fun centerCutoutDoesNotDisplaceLeftHudButLeftNotchDoes() {
        assertFalse(FlightHudLayout.intersectsHudX(470,610,40f,410f))
        assertTrue(FlightHudLayout.intersectsHudX(30,100,40f,410f))
        assertFalse(FlightHudLayout.intersectsHudX(410,510,40f,410f))
        assertTrue(FlightHudLayout.intersectsHudX(409,510,40f,410f))
    }

    @Test fun onlyUpcomingMovingPillarsGetWarnings() {
        assertFalse(FlightHudLayout.movingWarningVisible(0f,300f,126f))
        assertFalse(FlightHudLayout.movingWarningVisible(18f,160f,126f))
        assertFalse(FlightHudLayout.movingWarningVisible(18f,481f,126f))
        assertTrue(FlightHudLayout.movingWarningVisible(18f,260f,126f))
        assertTrue(FlightHudLayout.movingWarningVisible(7f,440f,126f))
    }
}
