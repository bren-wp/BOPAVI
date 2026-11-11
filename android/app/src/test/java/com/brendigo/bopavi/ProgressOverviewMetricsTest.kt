package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class ProgressOverviewMetricsTest {
    @Test fun completedLevelsNeverInventProgress(){
        assertEquals(0,ProgressOverviewMetrics.completedLevels(0))
        assertEquals(0,ProgressOverviewMetrics.completedLevels(1))
        assertEquals(1,ProgressOverviewMetrics.completedLevels(2))
        assertEquals(19,ProgressOverviewMetrics.completedLevels(20))
        assertEquals(LevelEngine.LEVELS_PER_WORLD,
            ProgressOverviewMetrics.completedLevels(Int.MAX_VALUE))
    }

    @Test fun starMilestoneIsOnlyTheFirstTwentyLevels(){
        assertEquals(0,ProgressOverviewMetrics.firstTwentyStarPercent(-20))
        assertEquals(0,ProgressOverviewMetrics.firstTwentyStarPercent(0))
        assertEquals(50,ProgressOverviewMetrics.firstTwentyStarPercent(30))
        assertEquals(100,ProgressOverviewMetrics.firstTwentyStarPercent(60))
        assertEquals(100,ProgressOverviewMetrics.firstTwentyStarPercent(9999))
    }

    @Test fun actualWorldLabelsAndAccessibilityHaveNoFakeRecords(){
        assertEquals("SKY MEADOWS",ProgressOverviewMetrics.worldName(0))
        assertEquals("STAR ABYSS",ProgressOverviewMetrics.worldName(7))
        val label=ProgressOverviewMetrics.detail(2,5,8)
        assertTrue(label.contains("4 completed levels"))
        assertTrue(label.contains("8 of 60 possible stars"))
        assertTrue(label.contains("first 20 levels"))
    }
}
