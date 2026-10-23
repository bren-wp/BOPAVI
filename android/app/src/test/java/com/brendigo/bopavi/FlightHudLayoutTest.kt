package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightHudLayoutTest {
    @Test fun lifeHeartsNeverOverlapScoreFoodOrAbility() {
        val regions=listOf(FlightHudLayout.score,FlightHudLayout.food,
            FlightHudLayout.lives,FlightHudLayout.ability,
            FlightHudLayout.notice)
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
        assertTrue(FlightHudLayout.heartCenters.toList().zipWithNext().all { (a,b) -> b-a>=48f })
        // Both card rows have deliberate gaps; no hearts touch their borders.
        assertEquals(8f,FlightHudLayout.food.left-FlightHudLayout.score.right,0f)
        assertEquals(8f,FlightHudLayout.lives.left-FlightHudLayout.food.right,0f)
        assertEquals(9f,FlightHudLayout.ability.top-FlightHudLayout.lives.bottom,0f)
    }

    @Test fun heartsAreCenteredAndTheirCountStaysInAnIndependentHeaderLane() {
        val hud=FlightHudLayout
        val lifeCardCenter=(hud.lives.left+hud.lives.right)/2f
        val groupCenter=hud.heartCenters.average().toFloat()
        assertEquals(lifeCardCenter,groupCenter,.01f)
        assertTrue(hud.lives.right-hud.lives.left>hud.score.right-hud.score.left)
        assertTrue(hud.lives.right-hud.lives.left>hud.food.right-hud.food.left)
        assertTrue(hud.lifeCountRight<=hud.lives.right-16f)
        assertTrue(hud.lifeCountRight>=hud.lives.left+110f)
        assertTrue(hud.heartBaseline-10f>=38f+hud.heartHeaderClearance)
        assertTrue(hud.heartBaseline+7f<=hud.lives.bottom-6f)
        assertTrue(hud.ability.right<=hud.pauseLaneStart-24f)
        assertTrue(hud.ability.top>hud.score.bottom)
        assertTrue(hud.notice.top>=hud.ability.bottom+8f)
        assertTrue(hud.notice.right<=hud.ability.right)
        assertTrue(hud.notice.right<=hud.pauseLaneStart-24f)
    }

    @Test fun onlyUpcomingMovingPillarsGetWarnings() {
        assertFalse(FlightHudLayout.movingWarningVisible(0f,300f,126f))
        assertFalse(FlightHudLayout.movingWarningVisible(18f,160f,126f))
        assertFalse(FlightHudLayout.movingWarningVisible(18f,481f,126f))
        assertTrue(FlightHudLayout.movingWarningVisible(18f,260f,126f))
        assertTrue(FlightHudLayout.movingWarningVisible(7f,440f,126f))
    }
}
