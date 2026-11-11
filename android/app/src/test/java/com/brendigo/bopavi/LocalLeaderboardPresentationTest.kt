package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class LocalLeaderboardPresentationTest {
    @Test fun leaderboardReferenceIsOnlyUsedWhenTextAndOrientationFit(){
        assertTrue(LeaderboardLayout.supports(941,1672,true,1f))
        assertFalse(LeaderboardLayout.supports(720,1280,true,1f))
        assertFalse(LeaderboardLayout.supports(1920,1080,false,1f))
        assertFalse(LeaderboardLayout.supports(941,1672,true,1.5f))
    }
    @Test fun highDensityTwoLineScoresUseReadableScrollCards() {
        assertTrue(LeaderboardLayout.supportsScoreRows(941,1672,2f))
        assertFalse(LeaderboardLayout.supportsScoreRows(941,1672,3f))
        assertFalse(LeaderboardLayout.supportsScoreRows(720,1280,2f))
        assertFalse(LeaderboardLayout.supportsScoreRows(941,1672,Float.NaN))
    }

    @Test fun scoresSortDescendingWithGateTieBreakAndOnlyTenRows(){
        val runs=(0..12).map{LeaderboardEntry("Player",100+it,0,1,it)}+
            listOf(LeaderboardEntry("Winner",112,1,2,90),
                   LeaderboardEntry("Invalid",999999,9,1,10))
        val ranked=LocalLeaderboardPresentation.rows(runs)
        assertEquals(10,ranked.size)
        assertEquals("Winner",ranked.first().entry.name)
        assertEquals((1..10).toList(),ranked.map{it.rank})
    }
    @Test fun emptyOrInvalidSavesDoNotProduceFakePlayers(){
        assertTrue(LocalLeaderboardPresentation.rows(emptyList()).isEmpty())
        assertTrue(LocalLeaderboardPresentation.rows(listOf(
            LeaderboardEntry("Invalid",100,-1,1,2))).isEmpty())
        assertTrue(LeaderboardLayout.valid())
    }
    @Test fun accessibilityUsesRealNameAndScores(){
        val entry=LocalLeaderboardPresentation.rows(
            listOf(LeaderboardEntry("Bopi",2700,0,1,12))).single()
        assertTrue(entry.description.contains("Bopi"))
        assertTrue(entry.description.contains("2700 points"))
        assertTrue(entry.description.contains("12 obstacles"))
        assertEquals("2,700",LocalLeaderboardPresentation.number(2700))
    }
}
