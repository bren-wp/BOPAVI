package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightHudReadabilityTest {
    @Test fun ordinaryCountersRetainFullSizeAndWidth() {
        assertEquals(23f,FlightHudReadability.fontSize(30f,90f),0f)
        assertEquals(1f,FlightHudReadability.horizontalScale(30f,90f,23f),0f)
    }

    @Test fun overflowAlwaysFitsWithoutRemovingAnyDigits() {
        for(width in listOf(25f,48f,79f,96f)) {
            for(measured in listOf(70f,110f,240f,420f,900f)) {
                val font=FlightHudReadability.fontSize(measured,width)
                val squeeze=FlightHudReadability.horizontalScale(measured,width,font)
                assertTrue(font in 11f..23f)
                assertTrue(squeeze>0f && squeeze<=1f)
                assertTrue("Actual drawn width must stay inside the card",
                    measured*(font/23f)*squeeze<=width+.001f)
            }
        }
    }

    @Test fun invalidMeasurementsNeverCreateInfiniteOrNegativePaintState() {
        for(value in listOf(0f,-5f,Float.NaN,Float.POSITIVE_INFINITY)) {
            val size=FlightHudReadability.fontSize(value,72f)
            val scale=FlightHudReadability.horizontalScale(value,72f,size)
            assertTrue(size.isFinite() && size>0f)
            assertTrue(scale.isFinite() && scale>0f)
        }
    }

    @Test fun finalLifeIsProminentWithoutFalseWarningsAtFullHealth() {
        assertEquals(0xffffdce5.toInt(),FlightHudReadability.lifeCountColor(3,3))
        assertEquals(0xffffd99b.toInt(),FlightHudReadability.lifeCountColor(2,3))
        assertEquals(0xffffa3b2.toInt(),FlightHudReadability.lifeCountColor(1,3))
        assertTrue(FlightHudReadability.isFinalLife(1,3))
        assertFalse(FlightHudReadability.isFinalLife(2,3))
        assertFalse(FlightHudReadability.isFinalLife(1,1))
    }
    @Test fun captionFitsLongPilotStatusAndNotices() {
        for(width in listOf(30f,68f,140f,228f,266f)) {
            for(measured in listOf(32f,130f,245f,400f,760f)) {
                val size=FlightHudReadability.captionFontSize(measured,width)
                val scale=FlightHudReadability.captionHorizontalScale(measured,width,size)
                assertTrue("Caption font must stay legible",size in 11f..15f)
                assertTrue("Caption scale must remain finite",scale.isFinite() && scale>0f && scale<=1f)
                assertTrue("Ability/notice must not overflow card",
                    measured*(size/15f)*scale<=width+.001f)
            }
        }
        assertEquals(15f,FlightHudReadability.captionFontSize(140f,266f),0f)
        assertEquals(1f,FlightHudReadability.captionHorizontalScale(140f,266f,15f),0f)
    }

    @Test fun invalidCaptionMeasurementsCannotPoisonCanvasPaint() {
        for(value in listOf(0f,-10f,Float.NaN,Float.POSITIVE_INFINITY)) {
            val size=FlightHudReadability.captionFontSize(value,220f)
            val scale=FlightHudReadability.captionHorizontalScale(value,220f,size)
            assertTrue(size.isFinite() && size>0f)
            assertTrue(scale.isFinite() && scale>0f)
        }
    }

}
