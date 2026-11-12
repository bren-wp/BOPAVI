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
    @Test fun compactAndLargeTextDevicesUseActualNativeScrollControls() {
        assertTrue(LevelMapGeometry.supports(941,1672,true,1f))
        assertFalse(LevelMapGeometry.supports(720,1280,true,1f))
        assertFalse(LevelMapGeometry.supports(941,1672,true,1.5f))
        assertFalse(LevelMapGeometry.supports(1920,1080,false,1f))
    }
    @Test fun sourceMapUsesNativeScrollWhenNodesFallBelow48Dp() {
        assertTrue(LevelMapGeometry.supportsNodeTouch(941,1672,2f))
        assertFalse(LevelMapGeometry.supportsNodeTouch(941,1672,3f))
        assertFalse(LevelMapGeometry.supportsNodeTouch(720,1280,2f))
        assertFalse(LevelMapGeometry.supportsNodeTouch(941,1672,Float.NaN))
    }

    @Test fun firstOwnerPageRetainsItsExactIslandColors() {
        assertArrayEquals(intArrayOf(0xff3eae59.toInt(),0xff8b7269.toInt(),
            0xffa6ef75.toInt()),LevelMapGeometry.islandPalette(0,0))
        assertArrayEquals(intArrayOf(0xff288fe2.toInt(),0xff646b96.toInt(),
            0xff8de6ff.toInt()),LevelMapGeometry.islandPalette(1,1))
        assertArrayEquals(intArrayOf(0xffe55b3d.toInt(),0xff664454.toInt(),
            0xfffa9b54.toInt()),LevelMapGeometry.islandPalette(2,2))
    }
    @Test fun laterPagesHaveDistinctWorldBiomesWithoutChangingStageGeometry() {
        for(slot in 0..2) {
            assertArrayEquals(LevelMapGeometry.islandPalette(0,slot),
                LevelMapGeometry.islandPalette(2,slot))
            assertArrayEquals(LevelMapGeometry.islandPalette(3,slot),
                LevelMapGeometry.islandPalette(5,slot))
            assertArrayEquals(LevelMapGeometry.islandPalette(6,slot),
                LevelMapGeometry.islandPalette(7,slot))
            assertFalse(LevelMapGeometry.islandPalette(0,slot)
                .contentEquals(LevelMapGeometry.islandPalette(3,slot)))
            assertFalse(LevelMapGeometry.islandPalette(3,slot)
                .contentEquals(LevelMapGeometry.islandPalette(6,slot)))
        }
        try {
            LevelMapGeometry.islandPalette(0,3)
            fail("Invalid island slot should fail closed")
        } catch(_:IllegalArgumentException) {
            // expected
        }
        assertTrue(LevelMapGeometry.nodesInsideReference())
        assertTrue(LevelMapGeometry.starStripsInsideReference())
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
        assertTrue(LevelMapGeometry.firstThreeMarkersAndStarsDoNotOverlap())
        assertEquals(LevelMapGeometry.Point(45f,969f),LevelMapGeometry.nodes[0])
        assertEquals(LevelMapGeometry.Point(421f,596f),LevelMapGeometry.nodes[3])
        assertEquals(LevelMapGeometry.Point(508f,207f),LevelMapGeometry.nodes[5])
        for(point in LevelMapGeometry.nodes+LevelMapGeometry.plaques){
            assertTrue(point.x>=0 && point.x<LevelMapGeometry.width)
            assertTrue(point.y>=0 && point.y<LevelMapGeometry.height)
        }
    }
}
