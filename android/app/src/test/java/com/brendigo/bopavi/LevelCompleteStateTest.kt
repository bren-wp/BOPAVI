package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class LevelCompleteStateTest {
    @Test fun showsOnlyPersistedFirstClearReward() {
        val data=LevelCompleteState.fromRun(5L,2,4,875,20)
        assertEquals("2/3",data.rating)
        assertEquals("4",data.collectedText)
        assertEquals("875",data.scoreText)
        assertEquals("+ 20 COINS",data.rewardText)
        assertTrue(data.nextAvailable)
    }

    @Test fun replayAndNonMilestoneDoNotPretendToGrantChestLoot() {
        val again=LevelCompleteState.fromRun(5L,3,7,950,0)
        assertEquals("NO NEW BONUS",again.rewardText)
        val regular=LevelCompleteState.fromRun(3L,3,1,450,0)
        assertEquals("NO NEW BONUS",regular.rewardText)
    }

    @Test fun endOfWorldDoesNotProduceInvalidNextLevel() {
        val last=LevelCompleteState.fromRun(LevelEngine.LEVELS_PER_WORLD.toLong(),
            3,0,900,200)
        assertFalse(last.nextAvailable)
        assertEquals("3/3",last.rating)
    }

    @Test fun formattedShareHasOnlyRealRunResult() {
        val state=LevelCompleteState.fromRun(3L,3,1,2100,0)
        assertEquals("BOPAVI — Level 3 complete! Stars: 3/3, score: 2,100.",
            state.shareText())
        assertFalse(state.shareText().contains("gem",ignoreCase=true))
    }

    @Test fun corruptedValuesDoNotAppearNegative() {
        val state=LevelCompleteState.fromRun(-40L,9,-3,-4,-3)
        assertEquals(1L,state.level)
        assertEquals("3/3",state.rating)
        assertEquals("0",state.collectedText)
        assertEquals("NO NEW BONUS",state.rewardText)
    }
}
