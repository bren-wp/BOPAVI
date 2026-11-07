package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightLoadingLayoutTest {
    @Test fun realReferenceArtworkRetainsItsNaturalProportions(){
        val layout=FlightLoadingLayout
        assertTrue(layout.logo.isInsideReference())
        assertTrue(layout.pilot.isInsideReference())
        assertTrue(layout.track.isInsideReference())
        assertTrue("Splash logo should keep side margins",layout.logo.left>=90f && layout.logo.right<=850f)
        assertTrue("Visible pilot cannot overlap wordmark",layout.logo.bottom<layout.pilotPaintedTop)
        assertTrue("Branded wordmark must not be stretched",
            layout.aspectError(layout.logo,780f,297f)<.01f)
        assertEquals("Shared wing/body canvases must remain square",
            1f,layout.pilot.width/layout.pilot.height,0f)
    }
    @Test fun scaleToFitPreservesEveryArtBoxOnAViewportMatrix(){
        val art=listOf(FlightLoadingLayout.logo,FlightLoadingLayout.pilot,
                       FlightLoadingLayout.track)
        for(w in 240..2560 step 80) for(h in 320..3840 step 160){
            val scale=kotlin.math.min(w/941f,h/1672f)
            val dx=(w-941f*scale)/2f
            val dy=(h-1672f*scale)/2f
            for(b in art){
                assertTrue(dx+b.left*scale>=-0.01f)
                assertTrue(dy+b.top*scale>=-0.01f)
                assertTrue(dx+b.right*scale<=w+.01f)
                assertTrue(dy+b.bottom*scale<=h+.01f)
            }
        }
    }
    @Test fun titlePilotAndTrackStayInReferenceVerticalOrder(){
        val l=FlightLoadingLayout
        assertTrue(l.logo.bottom<l.pilotPaintedTop)
        assertTrue("Painted hero feathers follow the source image",
            l.pilotPaintedTop in 450f..490f)
        assertTrue(l.pilot.bottom<l.track.top)
        assertTrue(l.captionBaselineY>l.track.bottom)
        assertTrue(l.captionBaselineY<l.track.bottom+90f)
        assertEquals(619f,l.track.width,0f)
        assertEquals(657f,l.pilot.width,0f)
        assertEquals(1515f,l.captionBaselineY,0f)
        assertTrue(l.track.bottom<l.referenceHeight)
    }
}
