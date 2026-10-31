package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class GameOverStatsTest {
    @Test fun endlessShowsActualFoodAndScoreNotFictionalGems() {
        val values=GameOverStats.fromRun(1843.9f,coins=0,stars=0,food=56,
            score=1900,endless=true)
        assertEquals("1,843",values.distance)
        assertEquals("Food Collected",values.collectedLabel)
        assertEquals("56",values.collected)
        assertEquals("Score",values.thirdLabel)
        assertEquals("1,900",values.third)
    }
    @Test fun finiteShowsActualCoinsAndStars() {
        val values=GameOverStats.fromRun(39f,coins=3,stars=2,food=0,
            score=321,endless=false)
        assertEquals("Coins Collected",values.collectedLabel)
        assertEquals("3",values.collected)
        assertEquals("Stars Collected",values.thirdLabel)
        assertEquals("2",values.third)
    }
    @Test fun invalidDistanceAndCountersNeverLeakToUi() {
        val values=GameOverStats.fromRun(Float.NaN,-1,-3,-2,-4,true)
        assertEquals("0",values.distance)
        assertEquals("0",values.collected)
        assertEquals("0",values.third)
    }
}
