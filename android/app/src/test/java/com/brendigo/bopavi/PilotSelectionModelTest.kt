package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class PilotSelectionModelTest {
    @Test fun oldPlayablePilotIdsAreUnchanged() {
        assertEquals(listOf(0,1,2,3),PilotSelectionModel.pilots.map{it.id})
        assertEquals("CLASSIC AVIATOR",PilotSelectionModel.pilots[0].title)
        assertEquals("ROYAL BOPI",PilotSelectionModel.pilots[1].title)
        assertEquals("FOREST EXPLORER",PilotSelectionModel.pilots[2].title)
        assertEquals("STAR PILOT",PilotSelectionModel.pilots[3].title)
        assertEquals(PilotAbility.BOPI,PilotSelectionModel.pilots[3].ability)
        assertEquals(PilotAbility.SOVA,PilotSelectionModel.pilots[1].ability)
        assertEquals(PilotAbility.LISAC,PilotSelectionModel.pilots[2].ability)
    }
    @Test fun homeAndFlightUseIdenticalPilotSpriteIds() {
        assertEquals(0,PilotSelectionModel.spriteIdForChoice(0))
        assertEquals(9,PilotSelectionModel.spriteIdForChoice(1))
        assertEquals(10,PilotSelectionModel.spriteIdForChoice(2))
        assertEquals(8,PilotSelectionModel.spriteIdForChoice(3))
        assertEquals(0,PilotSelectionModel.spriteIdForChoice(-100))
        assertEquals(8,PilotSelectionModel.spriteIdForChoice(100))
        assertNotEquals(1,PilotSelectionModel.spriteIdForChoice(1))
        assertNotEquals(2,PilotSelectionModel.spriteIdForChoice(2))
    }
    @Test fun selectedBadgeAndTalkBackFollowSavedArcadeChoice() {
        assertTrue(PilotSelectionModel.selected(1,1))
        assertFalse(PilotSelectionModel.selected(1,0))
        assertEquals("Selected SKY OWL",PilotSelectionModel.accessibility(1,1))
        assertEquals("Choose CLASSIC AVIATOR",PilotSelectionModel.accessibility(1,0))
        assertEquals("Locked STAR PILOT. Unlock with earned coins",
            PilotSelectionModel.accessibility(0,3,false))
        assertEquals("Selected STAR PILOT",PilotSelectionModel.accessibility(3,3,true))
        assertTrue(PilotSelectionModel.locked(3,false))
        assertFalse(PilotSelectionModel.locked(3,true))
        assertFalse(PilotSelectionModel.locked(2,false))
    }
    @Test fun fourthPilotUsesExistingEarnedCosmeticNotFakeCurrency() {
        assertEquals("THREE FREE PILOTS • STAR PILOT EARNED WITH COINS",
            PilotSelectionModel.priceLabel())
        assertEquals(3,PilotSelectionModel.safeChoice(100))
        assertEquals(0,PilotSelectionModel.safeChoice(-4))
        assertTrue(PilotSelectionLayout.fitsReference())
    }
}
