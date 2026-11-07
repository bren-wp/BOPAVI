package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class RoyalRingTrailTest {
    @Test fun ringsFollowRealMovingOpeningAndStayInsideIt() {
        val gate=LevelEngine.create(0,1).gates.first()
        assertTrue(RoyalRingTrail.enabled(gate))
        assertEquals(6,RoyalRingTrail.count)
        for(t in listOf(0f,1.3f,3.7f)) {
            val opening=LevelEngine.opening(gate,t,2)
            var previousX=Float.NEGATIVE_INFINITY
            var previousY=Float.POSITIVE_INFINITY
            for(i in 0 until RoyalRingTrail.count) {
                val x=RoyalRingTrail.x(380f,i)
                val y=RoyalRingTrail.y(opening,i)
                assertTrue(x>previousX)
                assertTrue(y<=previousY)
                assertTrue(y>=opening.top+24f)
                assertTrue(y<=opening.bottom-24f)
                previousX=x
                previousY=y
            }
        }
        val other=LevelEngine.create(1,1).gates.first()
        assertFalse(RoyalRingTrail.enabled(other))
        assertFalse(RoyalRingTrail.enabled(gate.copy(coin=false)))
    }

    @Test fun anActualFlapCanCollectRingOnlyOnceAndChangeRealScore() {
        val base=LevelEngine.create(0,1)
        val safe=base.gates.first().copy(
            x=280f,center=311f,gap=340f,width=70f,
            movement=0f,phase=0f,coin=true,star=false,power=0)
        val flight=GameSimulation(
            base.copy(gates=listOf(safe),speed=1f,wind=0f),endless=true)
        assertTrue(flight.royalRingVisible(0,0))
        assertFalse(flight.royalRingVisible(0,-1))
        assertFalse(flight.royalRingVisible(0,6))
        flight.flap()
        flight.step(1f/120f)
        assertTrue("First arc pickup is a real ring",flight.coins>=1)
        assertFalse("Collected first ring cannot repaint",flight.royalRingVisible(0,0))
        val earned=flight.coins
        flight.step(1f/120f)
        assertEquals("No second reward for the same collected ring",earned,flight.coins)
        assertEquals("Ring pickup participates in live score",earned*10,flight.score())
    }

    @Test fun nonRoyalWorldCannotFabricateRings() {
        val run=GameSimulation(LevelEngine.create(1,1),endless=true)
        for(i in run.level.gates.indices)
            for(r in 0 until RoyalRingTrail.count)
                assertFalse(run.royalRingVisible(i,r))
    }
}
