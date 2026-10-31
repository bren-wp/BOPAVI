package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class AchievementCatalogTest {
    @Test fun freshInstallHasEightUnreachedGoals() {
        val goals=AchievementCatalog.goals(AchievementSnapshot(0,0,0L,0,0,0,0,0))
        assertEquals(8,goals.size)
        assertEquals(8,goals.map{it.id}.distinct().size)
        assertTrue(goals.all{!it.achieved && it.percent==0})
        assertTrue(AchievementsLayout.valid())
    }
    @Test fun thresholdsReadCurrentSavedCounters() {
        val goals=AchievementCatalog.goals(AchievementSnapshot(2,10,100L,25,20,1000,10,5))
        assertTrue(goals.all{it.achieved && it.percent==100})
        assertEquals("FIRST VICTORY: 2 of 1. Goal reached",goals[0].accessibility)
    }
    @Test fun cappedPercentHandlesExtremeFlapsAndFailingRuns() {
        val goals=AchievementCatalog.goals(
            AchievementSnapshot(0,3,Long.MAX_VALUE,7,4,150,0,5))
        assertEquals(30,goals[1].percent)
        assertEquals(100,goals[2].percent)
        assertEquals(28,goals[3].percent)
        assertEquals(15,goals[5].percent)
        assertFalse(goals.first().achieved)
        assertTrue(goals.last().achieved)
    }
}
