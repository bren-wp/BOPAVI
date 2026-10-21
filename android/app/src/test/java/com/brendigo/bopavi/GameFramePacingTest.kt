package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameFramePacingTest {
    @Test fun displayVsyncIsUsedWhenMotionAllowedAndBatterySaverOff() {
        assertNull(GameFramePacing.redrawDelayMillis(false,false))
    }

    @Test fun userReducedMotionAlwaysCapsFullScreenRedrawWork() {
        assertEquals(33L,GameFramePacing.redrawDelayMillis(true,false))
        assertEquals(33L,GameFramePacing.redrawDelayMillis(true,true))
    }

    @Test fun osPowerSaverCapsRedrawsWithoutAlteringPhysics() {
        assertEquals(33L,GameFramePacing.redrawDelayMillis(false,true))
        // Frame policy only describes redraw cadence; physics maintains
        // its independent <=120Hz integration step on every device.
        val physics=GameSimulation(LevelEngine.create(0,1),endless=true)
        physics.flap()
        physics.step(1f/30f)
        assertEquals(1f/30f,physics.time,.00001f)
    }
}
