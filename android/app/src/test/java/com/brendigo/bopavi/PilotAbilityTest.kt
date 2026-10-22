package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class PilotAbilityTest {
    @Test fun threeSelectablePilotsAreDistinctAndStable() {
        assertEquals(PilotAbility.BOPI,PilotAbility.fromChoice(0))
        assertEquals(PilotAbility.SOVA,PilotAbility.fromChoice(1))
        assertEquals(PilotAbility.LISAC,PilotAbility.fromChoice(2))
        assertEquals(PilotAbility.BOPI,PilotAbility.fromChoice(999))
        assertEquals(1.06f,PilotAbility.BOPI.speedMultiplier,0f)
        assertEquals(.92f,PilotAbility.SOVA.gravityMultiplier,0f)
    }

    @Test fun legacyFlightRemainsCompatibleButRealBopiTravelsFaster() {
        val level=LevelEngine.create(0,1)
        val classic=GameSimulation(level,endless=true)
        val bopi=GameSimulation(level,endless=true,pilotAbility=PilotAbility.BOPI)
        assertEquals(level.speed,classic.currentScrollSpeed,0f)
        assertTrue(bopi.currentScrollSpeed>classic.currentScrollSpeed)
        classic.flap();bopi.flap()
        classic.step(1f/60f);bopi.step(1f/60f)
        assertEquals(-255f,bopi.velocity,15f)
        assertTrue(bopi.distance>classic.distance)
        assertEquals(classic.radius,bopi.radius,0f)
    }

    @Test fun owlDescendsMoreSlowlyWithoutModifyingColliderOrInput() {
        val level=LevelEngine.create(0,1)
        val classic=GameSimulation(level,endless=true)
        val owl=GameSimulation(level,endless=true,pilotAbility=PilotAbility.SOVA)
        classic.flap();owl.flap()
        classic.step(.1f);owl.step(.1f)
        assertTrue(owl.velocity<classic.velocity)
        assertTrue(owl.y<classic.y)
        assertEquals(18f,owl.radius,0f)
        assertEquals(-255f,GameSimulation(level,pilotAbility=PilotAbility.SOVA).apply{flap()}.velocity,0f)
    }

    @Test fun foxBlocksExactlyOneRealCollisionThenLosesAHeart() {
        val level=LevelEngine.create(0,1)
        val danger=level.gates.first().copy(x=110f,center=260f,gap=164f,
            movement=0f,coin=false,star=false,power=0)
        val fox=GameSimulation(level.copy(gates=listOf(danger),speed=140f),
            endless=true,pilotAbility=PilotAbility.LISAC)
        assertTrue(fox.pilotGuardReady)
        fox.flap();fox.step(1f/60f)
        assertFalse(fox.pilotGuardReady)
        assertEquals(3,fox.lives)
        assertTrue(fox.invulnerable>0f)
        assertFalse(fox.lastImpactLostLife)
        // A second impact in the same immunity window must not consume lives.
        fox.step(.1f)
        assertEquals(3,fox.lives)
        val immunity=GameSimulation::class.java.getDeclaredField("invulnerable")
        immunity.isAccessible=true
        immunity.setFloat(fox,0f)
        fox.step(1f/60f)
        assertEquals(2,fox.lives)
        assertTrue(fox.lastImpactLostLife)
        assertEquals("LISAC · ŠTIT POTROŠEN",fox.pilotAbility.hudStatus(fox.pilotGuardReady))
    }

    @Test fun guardCannotBeCarriedIntoLegacyFiniteLevels() {
        val legacy=GameSimulation(LevelEngine.create(0,1),
            pilotAbility=PilotAbility.LISAC)
        assertFalse(legacy.pilotGuardReady)
        assertEquals(1,legacy.maxLives)
    }
}
