package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class LocalLeaderboardPresentationTest {
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
