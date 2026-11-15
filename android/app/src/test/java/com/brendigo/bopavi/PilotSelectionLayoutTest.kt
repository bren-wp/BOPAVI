package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class PilotSelectionLayoutTest {
    @Test fun heroAndFourNativeCardsRemainInsideApprovedStage() {
        val layout=PilotSelectionLayout
        assertTrue(layout.fitsReference())
        assertEquals(941f,layout.referenceWidth,0f)
        assertEquals(1672f,layout.referenceHeight,0f)
        assertEquals(4,layout.cardLefts.size)
        assertTrue(layout.heroLeft>=138f) // Previous 112px native arrow ends at 138.
        assertTrue(layout.heroLeft+layout.heroSize<=803f) // Next arrow starts at 803.
        assertTrue(layout.heroTop+layout.heroSize<layout.cardTop)
        assertTrue(layout.platformTop<layout.heroTop+layout.heroSize)
        assertEquals((layout.referenceWidth-layout.heroSize)/2f,
            layout.heroLeft,.6f)
    }
}
