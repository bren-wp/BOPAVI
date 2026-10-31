package com.brendigo.bopavi

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The painted collectible must exist exactly when the physical game can award it. */
class ReferenceGameplayPickupsTest {
    private fun stage(coin:Boolean=true,star:Boolean=true,power:Int=1):LevelEngine.Level {
        val gate=LevelEngine.Gate(
            x=420f,center=360f,gap=200f,width=80f,movement=0f,
            phase=0f,kind=0,coin=coin,star=star,power=power
        )
        return LevelEngine.Level(
            world=0,number=1,zone=0,speed=120f,wind=0f,
            gates=listOf(gate),type=0
        )
    }

    @Test fun onlyRealFiniteRewardsAreVisible() {
        val finite=GameSimulation(stage(),endless=false)
        assertTrue(finite.coinVisible(0))
        assertTrue(finite.crystalVisible(0))
        assertTrue(finite.powerVisible(0))
        assertFalse(finite.coinVisible(-1))
        assertFalse(finite.crystalVisible(99))
        assertFalse(finite.powerVisible(0,incoming=true))
        assertFalse(finite.coinVisible(0,incoming=true))
    }

    @Test fun endlessRunDoesNotPaintFictionalFiniteRewards() {
        val endless=GameSimulation(stage(),endless=true)
        assertFalse(endless.coinVisible(0))
        assertFalse(endless.crystalVisible(0))
        assertFalse(endless.powerVisible(0))
    }

    @Test fun absentRewardsRemainInvisible() {
        val finite=GameSimulation(stage(coin=false,star=false,power=0),endless=false)
        assertFalse(finite.coinVisible(0))
        assertFalse(finite.crystalVisible(0))
        assertFalse(finite.powerVisible(0))
    }
}
