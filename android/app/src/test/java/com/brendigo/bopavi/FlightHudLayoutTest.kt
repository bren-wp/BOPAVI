package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightHudLayoutTest {
    @Test fun lifeHeartsNeverOverlapScoreFoodOrAbility() {
        val regions=listOf(FlightHudLayout.score,FlightHudLayout.food,
            FlightHudLayout.lives,FlightHudLayout.ability)
        for((i,box) in regions.withIndex()) {
            assertTrue("Positive HUD area $i",box.left<box.right && box.top<box.bottom)
            assertTrue("HUD stays inside logical viewport $i",
                box.left>=0f && box.top>=0f && box.right<=480f && box.bottom<=800f)
            for(other in regions.drop(i+1))
                assertFalse("HUD panels cannot overlap",box.intersects(other))
        }
        for(center in FlightHudLayout.heartCenters) {
            assertTrue(FlightHudLayout.lives.encloses(
                center-FlightHudLayout.heartHalfWidth,FlightHudLayout.heartBaseline-10f))
            assertTrue(FlightHudLayout.lives.encloses(
                center+FlightHudLayout.heartHalfWidth,FlightHudLayout.heartBaseline+7f))
        }
        assertTrue(FlightHudLayout.ability.right<FlightHudLayout.pauseLaneStart)
        assertTrue(FlightHudLayout.heartCenters.toList().zipWithNext().all { (a,b) -> b-a>=30f })
    }

    @Test fun onlyUpcomingMovingPillarsGetWarnings() {
        assertFalse(FlightHudLayout.movingWarningVisible(0f,300f,126f))
        assertFalse(FlightHudLayout.movingWarningVisible(18f,160f,126f))
        assertFalse(FlightHudLayout.movingWarningVisible(18f,481f,126f))
        assertTrue(FlightHudLayout.movingWarningVisible(18f,260f,126f))
        assertTrue(FlightHudLayout.movingWarningVisible(7f,440f,126f))
    }
}
