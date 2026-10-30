package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightCameraCompositionTest {
    @Test fun subjectMatchesReferenceHorizontalAnchor() {
        assertEquals(175f,FlightCameraComposition.birdX,0f)
        assertEquals(49f,FlightCameraComposition.horizontalShift,0f)
        assertTrue(FlightCameraComposition.birdX/480f in .36f.. .38f)
    }

    @Test fun translationPreservesRelativeGateContactAcrossWorlds() {
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            val sim=GameSimulation(LevelEngine.create(world,1),endless=true)
            for(g in sim.level.gates) {
                val legacyX=WorldCoordinates.screenX(g.x,0.0,0.0)
                assertEquals(legacyX-FlightCameraComposition.legacyBirdX,
                    sim.gateX(g)-sim.birdX,.001f)
            }
            assertEquals(0f,sim.levelProgress,0f)
            val first=sim.level.gates.first()
            assertEquals(first.x+FlightCameraComposition.horizontalShift,
                sim.gateX(first),.001f)
        }
    }

    @Test fun physicalMaceMovesWithItsGateAndNeverDriftsFromHitbox() {
        val level=LevelEngine.create(0,1)
        val sim=GameSimulation(level,endless=true)
        val idx=4
        val gate=sim.level.gates[idx]
        assertTrue(SkyMaceHazard.enabled(gate,idx))
        val beforeX=SkyMaceHazard.bobX(gate,gate.x,0f)
        val afterX=SkyMaceHazard.bobX(gate,sim.gateX(gate),0f)
        assertEquals(FlightCameraComposition.horizontalShift,afterX-beforeX,.001f)
        val opening=LevelEngine.opening(gate,0f)
        val ballY=SkyMaceHazard.bobY(gate,opening,0f,idx)
        assertTrue(SkyMaceHazard.sweptHit(gate,idx,afterX,ballY,ballY,
            sim.radius,sim.gateX(gate),sim.gateX(gate),opening,opening,0f,0f))
    }
}
