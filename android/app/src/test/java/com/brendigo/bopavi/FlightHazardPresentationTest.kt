package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class FlightHazardPresentationTest {
    @Test fun noHazardsBeforeRealFirstFlap() {
        assertFalse(FlightHazardPresentation.obstaclesVisible(
            firstFlapReceived=false,finished=false))
    }

    @Test fun showRealHazardsAfterFirstFlap() {
        assertTrue(FlightHazardPresentation.obstaclesVisible(
            firstFlapReceived=true,finished=false))
    }

    @Test fun finishedRunCannotDrawGameplayHazardsOverResult() {
        assertFalse(FlightHazardPresentation.obstaclesVisible(
            firstFlapReceived=true,finished=true))
        assertFalse(FlightHazardPresentation.obstaclesVisible(
            firstFlapReceived=false,finished=true))
    }

    @Test fun actualSimulationFlapBeginsAndRetainsActiveRun() {
        val simulation=GameSimulation(LevelEngine.createStream(0,1L),endless=false)
        assertFalse(simulation.active)
        assertFalse(FlightHazardPresentation.obstaclesVisible(
            simulation.active,simulation.finished))
        simulation.flap()
        assertTrue(simulation.active)
        assertEquals(1,simulation.flaps)
        assertTrue(FlightHazardPresentation.obstaclesVisible(
            simulation.active,simulation.finished))
        // Pause is a presentation/lifecycle state on GameView; neither
        // pause nor an Android background transition resets game.active.
        assertTrue(FlightHazardPresentation.obstaclesVisible(
            simulation.active,simulation.finished))
    }

    @Test fun newRunStartsHiddenAgainWithoutOldStateLeak() {
        val previous=GameSimulation(LevelEngine.createStream(0,1L))
        previous.flap()
        val replay=GameSimulation(LevelEngine.createStream(0,1L))
        assertTrue(previous.active)
        assertFalse(replay.active)
        assertFalse(FlightHazardPresentation.obstaclesVisible(
            replay.active,replay.finished))
    }
}
