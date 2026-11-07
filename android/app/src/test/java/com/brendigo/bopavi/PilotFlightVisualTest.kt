package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PilotFlightVisualTest {
    @Test fun importedBopiNeverUsesOversizedCompanionBounds(){
        assertEquals(142f,PilotFlightVisual.diameter(0),0f)
        for(skin in 1..8)assertEquals(142f,PilotFlightVisual.diameter(skin),0f)
    }
    @Test fun owlAndFoxMatchTheirLargerReferenceSilhouettes(){
        assertEquals(184f,PilotFlightVisual.diameter(9),0f)
        assertEquals(182f,PilotFlightVisual.diameter(10),0f)
        assertTrue(PilotFlightVisual.diameter(9)>PilotFlightVisual.diameter(0))
        assertTrue(PilotFlightVisual.diameter(10)>PilotFlightVisual.diameter(0))
        assertTrue(PilotFlightVisual.diameter(9)/480f<0.4f)
        assertTrue(PilotFlightVisual.diameter(10)/480f<0.4f)
    }
}
