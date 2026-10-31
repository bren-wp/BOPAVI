package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightLoadingLayoutTest {
    @Test fun realReferenceArtworkRetainsItsNaturalProportions() {
        val layout=FlightLoadingLayout
        assertTrue(layout.logo.isInsideReference())
        assertTrue(layout.pilot.isInsideReference())
        assertTrue(layout.track.isInsideReference())
        assertTrue("Branded wordmark must not be stretched",
            layout.aspectError(layout.logo,780f,236f)<.01f)
        assertEquals("Shared body/wing canvases must remain square",
            1f,layout.pilot.width/layout.pilot.height,0f)
    }

    @Test fun titlePilotAndTrackStayInReferenceVerticalOrder() {
        val l=FlightLoadingLayout
        assertTrue(l.logo.bottom < l.pilot.top)
        assertTrue(l.pilot.bottom < l.track.top)
        assertTrue(l.track.bottom < l.captionBaselineY)
        assertEquals(706f,l.track.width,0f)
        assertEquals(715f,l.pilot.width,0f)
        assertEquals(1493f,l.captionBaselineY,0f)
        assertTrue(l.captionBaselineY<l.referenceHeight)
    }
}
