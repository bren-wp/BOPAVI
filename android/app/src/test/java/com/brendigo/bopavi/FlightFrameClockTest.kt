package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class FlightFrameClockTest {
    @Test fun idlePreviewWaitingDoesNotAdvanceFirstFlap() {
        val clock=FlightFrameClock()
        val simulation=GameSimulation(LevelEngine.create(0,1),endless=true)
        assertNull(clock.tick(1_000_000_000L,running=false))
        assertNull(clock.tick(121_000_000_000L,running=false))
        simulation.flap()
        assertNull("First active frame is a baseline, not an idle catch-up",
            clock.tick(121_016_000_000L,running=true))
        assertEquals(0f,simulation.time,0f)
        assertEquals(366f,simulation.y,0f)
        val delta=clock.tick(121_032_000_000L,running=true)
        requireNotNull(delta)
        simulation.step(delta)
        assertEquals(.016f,simulation.time,.00001f)
        assertFalse(simulation.finished)
    }

    @Test fun pauseBackgroundAndQaCaptureEachDiscardElapsedTime() {
        val clock=FlightFrameClock()
        assertNull(clock.tick(10_000_000_000L,true))
        assertEquals(.016f,clock.tick(10_016_000_000L,true)!!,.000001f)
        assertNull(clock.tick(10_016_100_000L,false))
        assertNull("Never integrate elapsed background time",clock.tick(200_000_000_000L,true))
        assertEquals(.016f,clock.tick(200_016_000_000L,true)!!,.000001f)
        clock.reset() // flight QA capture complete
        assertNull(clock.tick(300_000_000_000L,true))
        assertEquals(.020f,clock.tick(300_020_000_000L,true)!!,.000001f)
    }

    @Test fun subMillisecondRepaintsAndClockRewindsStaySafe() {
        val clock=FlightFrameClock()
        assertNull(clock.tick(2_000_000_000L,true))
        assertNull(clock.tick(2_000_400_000L,true))
        assertEquals(.001f,clock.tick(2_001_000_000L,true)!!,.000001f)
        assertNull("A non-monotonic callback cannot integrate negative time",
            clock.tick(1_000_000_000L,true))
        assertEquals(.008f,clock.tick(1_008_000_000L,true)!!,.000001f)
    }
}
