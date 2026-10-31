package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightReadyOverlayLayoutTest {
    @Test fun tutorialMatchesReferenceRelativeBounds() {
        val a=FlightReadyOverlayLayout
        assertTrue(a.insideCamera())
        assertTrue("Reference anchor must stay within 6 source pixels",
            a.sourceMappedErrorPx()<6f)
        assertEquals(190f,a.right-a.left,0f)
        assertEquals(54f,a.bottom-a.top,0f)
    }

    @Test fun liveHudStillFitsWhenGameIsReady() {
        assertTrue(FlightHudLayout.lives.right<FlightReadyOverlayLayout.left+
            40f)
        assertTrue(FlightHudLayout.lives.bottom<FlightReadyOverlayLayout.top)
    }
}
