package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightLoadingTrackTest {
    @Test fun scannerMovesWithoutPretendingToMeasureGeneration() {
        val beginning=FlightLoadingTrack.segment(100f,0L,false)
        val farEnd=FlightLoadingTrack.segment(100f,775L,false)
        val nextCycle=FlightLoadingTrack.segment(100f,1550L,false)
        assertEquals(70f,beginning.width,.001f)
        assertEquals(0f,beginning.offset,.001f)
        assertEquals(10f,farEnd.offset,.001f)
        assertEquals(70f,farEnd.width,.001f)
        assertEquals(beginning.offset,nextCycle.offset,.001f)
    }

    @Test fun onlyActualCompletionFillsTheBar() {
        for(time in listOf(0L,375L,775L,1200L,1550L)) {
            val pending=FlightLoadingTrack.segment(350f,time,false)
            assertTrue(pending.width<350f)
            assertTrue(pending.offset>=0f)
            assertTrue(pending.offset+pending.width<=350.001f)
            val done=FlightLoadingTrack.segment(350f,time,true)
            assertEquals(0f,done.offset,0f)
            assertEquals(350f,done.width,0f)
        }
    }

    @Test fun veryNarrowTracksStayWithinBounds() {
        for(width in listOf(0f,1f,5f,12f,28f)) {
            for(t in 0L..1550L step 155L) {
                val part=FlightLoadingTrack.segment(width,t,false)
                assertTrue(part.offset>=0f)
                assertTrue(part.width>=0f)
                assertTrue(part.offset+part.width<=width+.001f)
            }
        }
    }
}
