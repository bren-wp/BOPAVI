package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PilotFlightVisualTest {
    @Test fun allBopiSkinsRetainReferenceScale() {
        // 864x1536 portrait owner refs: Bopi's wings/scarf fill roughly
        // 37.5% of the logical camera width without touching the screen edge.
        for(skin in 0..8) assertEquals(180f,PilotFlightVisual.diameter(skin),0f)
        assertEquals(.375f,PilotFlightVisual.diameter(0)/480f,.001f)
    }

    @Test fun owlAndFoxPreserveTheirBroaderReferenceSilhouettes() {
        assertEquals(202f,PilotFlightVisual.diameter(9),0f)
        assertEquals(196f,PilotFlightVisual.diameter(10),0f)
        assertTrue(PilotFlightVisual.diameter(9)>PilotFlightVisual.diameter(0))
        assertTrue(PilotFlightVisual.diameter(10)>PilotFlightVisual.diameter(0))
        // The wings and tail stay in the game lane, without clipping at left.
        for(skin in 0..10) {
            val half=PilotFlightVisual.diameter(skin)*.5f
            assertTrue(FlightCameraComposition.birdX-half>=0f)
            assertTrue(FlightCameraComposition.birdX+half<=480f)
        }
    }
}
