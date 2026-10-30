package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class LevelMapGeometryTest {
    @Test fun threeReferenceWorldsAreOnTheFirstMapPage() {
        assertEquals(listOf(0,1,2),LevelMapGeometry.displayedWorlds(0))
        assertEquals(listOf(3,4,5),LevelMapGeometry.displayedWorlds(3))
        assertEquals(listOf(6,7),LevelMapGeometry.displayedWorlds(7))
        assertEquals(60,LevelMapGeometry.starsPerWorld)
    }
    @Test fun realLevelNodesNeverExceedTheMillionLevelLimit() {
        for(frontier in listOf(1,2,3,20,100,1_048_576,1_048_577)){
            val levels=LevelMapGeometry.nearestLevels(frontier)
            assertEquals(6,levels.size)
            assertTrue(levels.all{it in 1..LevelEngine.LEVELS_PER_WORLD})
            assertEquals(levels.distinct(),levels)
            assertEquals(frontier.coerceIn(1,LevelEngine.LEVELS_PER_WORLD),
                LevelMapGeometry.playableFrontier(frontier))
        }
    }
    @Test fun lockedNodesCannotLaunchAtFreshInstall() {
        assertTrue(LevelMapGeometry.isUnlocked(1,1))
        assertFalse(LevelMapGeometry.isUnlocked(2,1))
        assertFalse(LevelMapGeometry.isUnlocked(0,1))
        assertFalse(LevelMapGeometry.isUnlocked(LevelEngine.LEVELS_PER_WORLD+1,Int.MAX_VALUE))
    }
    @Test fun referenceAnchorsStayWithin941By1672() {
        assertEquals(6,LevelMapGeometry.nodes.size)
        assertTrue(LevelMapGeometry.nodesInsideReference())
        assertTrue(LevelMapGeometry.starStripsInsideReference())
        assertEquals(LevelMapGeometry.Point(45f,969f),LevelMapGeometry.nodes[0])
        assertEquals(LevelMapGeometry.Point(421f,596f),LevelMapGeometry.nodes[3])
        assertEquals(LevelMapGeometry.Point(508f,207f),LevelMapGeometry.nodes[5])
        for(point in LevelMapGeometry.nodes+LevelMapGeometry.plaques){
            assertTrue(point.x>=0 && point.x<LevelMapGeometry.width)
            assertTrue(point.y>=0 && point.y<LevelMapGeometry.height)
        }
    }
}
