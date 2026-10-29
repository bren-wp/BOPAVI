package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightHudReadabilityTest {
    @Test fun lifeColorsMatchTheRealState() {
        assertEquals(0xffffdce5.toInt(),FlightHudReadability.lifeCountColor(3,3))
        assertEquals(0xffffd99b.toInt(),FlightHudReadability.lifeCountColor(2,3))
        assertEquals(0xffffa3b2.toInt(),FlightHudReadability.lifeCountColor(1,3))
    }
    @Test fun finalLifeWarningOnlyWhenOneHeartRemains() {
        assertFalse(FlightHudReadability.isFinalLife(3,3))
        assertFalse(FlightHudReadability.isFinalLife(2,3))
        assertFalse(FlightHudReadability.isFinalLife(1,1))
        assertTrue(FlightHudReadability.isFinalLife(1,3))
    }
}
