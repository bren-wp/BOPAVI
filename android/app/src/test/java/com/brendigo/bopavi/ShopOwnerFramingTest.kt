package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Measured original owner 1080x1920 WebP and v1.0.87 native capture edges. */
class ShopOwnerFramingTest {
    @Test fun standardOwnerFrameReachesItsRealReferenceEdges() {
        val factor = ShopReferenceLayout.horizontalFactor(1080, 1920)
        val left = 540f + (21f - 540f) * factor
        val right = 540f + (1062f - 540f) * factor
        assertTrue("Owner's left wooden rim must remain within 3px", left in 98f..104f)
        assertTrue("Owner's right wooden rim must remain within 3px", right in 976f..982f)
        assertTrue("Horizontal camera must preserve positive card widths", factor > .80f)
    }

    @Test fun otherOwnerSizesAndHistoricalQaWidthRemainUnchanged() {
        for ((w, h) in listOf(
            1080 to 2280, 1080 to 2340, 1080 to 2400,
            1440 to 3040, 941 to 1672, 720 to 1280,
        )) {
            assertEquals("Unexpected horizontal scaling at ${w}x${h}",
                1f, ShopReferenceLayout.horizontalFactor(w, h), .00001f)
        }
    }
}
