package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Test

class PauseReplayTargetTest {
    @Test fun finiteReplayPreservesSelectedWorldAndStage() {
        assertEquals(PauseReplayTarget.Finite(2,17L),
            PauseReplayTarget.fromStage(2 to 17L))
        assertEquals(PauseReplayTarget.Finite(0,1L),
            PauseReplayTarget.fromStage(0 to 1L))
    }
    @Test fun endlessReplayDoesNotInventLevelSelection() {
        assertEquals(PauseReplayTarget.Endless,PauseReplayTarget.fromStage(null))
    }
}
