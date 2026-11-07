package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class PilotSelectionModelTest {
    @Test fun oldPlayablePilotIdsAreUnchanged() {
        assertEquals(listOf(0,1,2),PilotSelectionModel.pilots.map{it.id})
        assertEquals("CLASSIC AVIATOR",PilotSelectionModel.pilots[0].title)
        assertEquals("SKY EAGLE",PilotSelectionModel.pilots[1].title)
        assertEquals("SKY FOX",PilotSelectionModel.pilots[2].title)
        assertEquals(PilotAbility.SOVA,PilotSelectionModel.pilots[1].ability)
        assertEquals(PilotAbility.LISAC,PilotSelectionModel.pilots[2].ability)
    }
    @Test fun splashHomeAndFlightUseIdenticalPilotSpriteIds() {
        assertEquals(0,PilotSelectionModel.spriteIdForChoice(0))
        assertEquals(9,PilotSelectionModel.spriteIdForChoice(1))
        assertEquals(10,PilotSelectionModel.spriteIdForChoice(2))
        assertEquals(0,PilotSelectionModel.spriteIdForChoice(-100))
        assertEquals(10,PilotSelectionModel.spriteIdForChoice(100))
        assertNotEquals(1,PilotSelectionModel.spriteIdForChoice(1))
        assertNotEquals(2,PilotSelectionModel.spriteIdForChoice(2))
    }
    @Test fun selectedBadgeAndTalkBackFollowSavedArcadeChoice() {
        assertTrue(PilotSelectionModel.selected(1,1))
        assertFalse(PilotSelectionModel.selected(1,0))
        assertEquals("Selected SKY EAGLE",PilotSelectionModel.accessibility(1,1))
        assertEquals("Choose CLASSIC AVIATOR",PilotSelectionModel.accessibility(1,0))
    }
    @Test fun displayedPilotsCannotInventLockedPrices() {
        assertEquals("FREE • UNLOCKED",PilotSelectionModel.priceLabel())
        assertEquals(2,PilotSelectionModel.safeChoice(100))
        assertEquals(0,PilotSelectionModel.safeChoice(-4))
        assertTrue(PilotSelectionLayout.fitsReference())
    }
}
