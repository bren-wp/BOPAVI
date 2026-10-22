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
    @Test fun owlHasARealLowerTerminalFallSpeedWithoutChangingCollision() {
        val original=LevelEngine.create(0,1)
        val far=original.copy(gates=listOf(original.gates.first().copy(
            x=10000f,coin=false,star=false,power=0,movement=0f)))
        val normal=GameSimulation(far,endless=true)
        val owl=GameSimulation(far,endless=true,pilotAbility=PilotAbility.SOVA)
        normal.flap();owl.flap()
        repeat(120) {
            normal.step(1f/120f)
            owl.step(1f/120f)
        }
        assertFalse(normal.finished)
        assertFalse(owl.finished)
        assertEquals(365f,normal.velocity,1f)
        assertEquals(315f,owl.velocity,1f)
        assertTrue("Lower fall speed must yield noticeably more room to react",owl.y<normal.y-8f)
        assertEquals(18f,owl.radius,0f)
        assertEquals(1,owl.flaps)
    }

    @Test fun everyPilotKeepsFlightPhysicsConsistentAt30_60_120Hz() {
        val base=LevelEngine.create(0,1)
        val far=base.copy(gates=listOf(base.gates.first().copy(
            x=10000f,coin=false,star=false,power=0,movement=0f)))
        for(ability in PilotAbility.values()) {
            val at30=GameSimulation(far,endless=true,pilotAbility=ability).apply { flap() }
            val at60=GameSimulation(far,endless=true,pilotAbility=ability).apply { flap() }
            val at120=GameSimulation(far,endless=true,pilotAbility=ability).apply { flap() }
            repeat(30){ at30.step(1f/30f) }
            repeat(60){ at60.step(1f/60f) }
            repeat(120){ at120.step(1f/120f) }
            for(run in listOf(at30,at60)) {
                assertFalse("$ability must finish a collision-free one-second flight",run.finished)
                assertEquals("$ability vertical movement must match 120Hz",at120.y,run.y,2f)
                assertEquals("$ability terminal speed must match 120Hz",at120.velocity,run.velocity,2f)
                assertEquals("$ability horizontal travel must match 120Hz",at120.distance,run.distance,1f)
            }
        }
    }

    @Test fun abilityChipColoursExposeShieldReadyAndSpentStates() {
        assertNotEquals(PilotAbility.LISAC.hudTint(true),PilotAbility.LISAC.hudTint(false))
        assertNotEquals(PilotAbility.BOPI.hudTint(false),PilotAbility.SOVA.hudTint(false))
        assertEquals("LISAC · ŠTIT SPREMAN",PilotAbility.LISAC.hudStatus(true))
        assertEquals("LISAC · ŠTIT POTROŠEN",PilotAbility.LISAC.hudStatus(false))
        assertEquals(365f,PilotAbility.BOPI.terminalFallSpeed,0f)
        assertEquals(315f,PilotAbility.SOVA.terminalFallSpeed,0f)
    }

}
