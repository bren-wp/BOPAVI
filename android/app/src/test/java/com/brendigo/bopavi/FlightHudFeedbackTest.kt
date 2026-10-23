package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightHudFeedbackTest {
    @Test fun initialObservationDoesNotInventLostOrRestoredLives() {
        val tracker=FlightHudFeedback()
        assertNull(tracker.update(3,false,0f))
        assertNull(tracker.update(3,false,.5f))
    }

    @Test fun lossAndRecoveryAreShownOncePerActualChange() {
        val tracker=FlightHudFeedback()
        tracker.update(3,false,0f)
        val lost=tracker.update(2,false,.5f)
        assertNotNull(lost)
        assertEquals(FlightHudFeedback.LOST_LIFE,lost!!.label)
        assertSame(lost,tracker.update(2,false,.6f))
        assertSame(lost,tracker.update(2,false,1.59f))
        assertNull(tracker.update(2,false,1.8f))
        val gained=tracker.update(3,false,2f)
        assertEquals(FlightHudFeedback.RESTORED_LIFE,gained!!.label)
        assertNotEquals(lost.tint,gained.tint)
        assertNull(tracker.update(3,false,3.2f))
    }

    @Test fun foxGuardDoesNotPretendToConsumeHeart() {
        val tracker=FlightHudFeedback()
        assertNull(tracker.update(3,true,0f))
        val guard=tracker.update(3,false,.1f)
        assertEquals(FlightHudFeedback.USED_SHIELD,guard!!.label)
        assertSame(guard,tracker.update(3,false,.5f))
        assertNull(tracker.update(3,false,1.3f))
        assertNull(tracker.update(3,false,2.0f))
        val lost=tracker.update(2,false,2.2f)
        assertEquals(FlightHudFeedback.LOST_LIFE,lost!!.label)
    }

    @Test fun lowFrameRatesDoNotRepeatEventsAndNoEventSurvivesRestart() {
        for(rate in listOf(30,60,120)) {
            val tracker=FlightHudFeedback()
            tracker.update(3,true,0f)
            val original=tracker.update(3,false,1f/rate)
            assertNotNull(original)
            for(frame in 2 until rate) {
                assertSame(original,tracker.update(3,false,frame.toFloat()/rate))
            }
            assertNull(tracker.update(3,false,2f))
            // Each new GameView gets fresh event state at the next run.
            assertNull(FlightHudFeedback().update(3,true,0f))
        }
    }

    @Test fun corruptTimeCannotLeavePermanentNotice() {
        val tracker=FlightHudFeedback()
        tracker.update(3,false,0f)
        tracker.update(2,false,.2f)
        assertNull(tracker.update(2,false,Float.NaN))
        assertNull(tracker.update(2,false,2f))
    }
}
