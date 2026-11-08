package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightLoadingLayoutTest {
    @Test fun realReferenceArtworkRetainsItsNaturalProportions(){
        val layout=FlightLoadingLayout
        assertTrue(layout.logo.isInsideReference())
        assertTrue(layout.pilot.isInsideReference())
        assertTrue(layout.track.isInsideReference())
        assertTrue("Enlarged logo respects the reference side margins",
            layout.logo.left>=70f && layout.logo.right<=871f)
        assertTrue("Visible pilot cannot overlap wordmark",layout.logo.bottom<layout.pilotPaintedTop)
        assertTrue("Branded wordmark must not be stretched",
            layout.aspectError(layout.logo,780f,325f)<.01f)
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
    @Test fun tallScreenPinsBrandedHeaderAndProgressToOppositeEnds(){
        val l=FlightLoadingLayout
        val w=1080f
        val h=2400f
        val scale=kotlin.math.min(w/l.referenceWidth,h/l.referenceHeight)
        val extraHeight=(h/scale-l.referenceHeight).coerceAtLeast(0f)
        assertTrue(extraHeight>0f)
        val centerOffset=(h-l.referenceHeight*scale)/2f
        val headerTop=centerOffset+scale*(l.logo.top-extraHeight*.45f)
        val barBottom=centerOffset+scale*(l.track.bottom+extraHeight*.45f)
        val paintedHeroTop=centerOffset+scale*
            (l.pilotPaintedTop-extraHeight*.25f)
        assertTrue(headerTop>=0f)
        assertTrue(headerTop<h*.10f)
        assertTrue(paintedHeroTop>headerTop)
        assertTrue(barBottom>h*.80f)
        assertTrue(barBottom<h)
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
