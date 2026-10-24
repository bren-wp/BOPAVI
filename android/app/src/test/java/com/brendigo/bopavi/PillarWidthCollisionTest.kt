package com.brendigo.bopavi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The renderer uses Gate.width directly. Keep the body and swept hitboxes
 * identical for every slim/wide obstacle, so visual difficulty is genuine.
 */
class PillarWidthCollisionTest {
    private val widths = floatArrayOf(54f, 68f, 74f, 80f, 92f, 104f)
    private val mouth = LevelEngine.Opening(270f, 430f)
    private val birdX = 126f
    private val radius = 18f

    @Test fun everyWidthHasRealSolidShaftAndOpenCorridor() {
        for (width in widths) {
            val x = birdX - width / 2f
            assertTrue("Upper shaft missing for width=$width",
                PillarCollision.bodyHitsPillar(birdX, 210f, radius, x, width, mouth))
            assertTrue("Lower shaft missing for width=$width",
                PillarCollision.bodyHitsPillar(birdX, 480f, radius, x, width, mouth))
            assertFalse("Clear mouth collides for width=$width",
                PillarCollision.bodyHitsPillar(birdX, 350f, radius, x, width, mouth))
            assertFalse("Distant pillar collides for width=$width",
                PillarCollision.bodyHitsPillar(birdX, 210f, radius, birdX + radius + 10f, width, mouth))
        }
    }

    @Test fun sweptCollisionDetectsBetweenFrameCrossingsForEveryWidth() {
        for (width in widths) {
            val before = birdX + radius + 35f
            val after = birdX - width - radius - 35f
            assertFalse(PillarCollision.bodyHitsPillar(birdX, 210f, radius, before, width, mouth))
            assertFalse(PillarCollision.bodyHitsPillar(birdX, 210f, radius, after, width, mouth))
            assertTrue("Missed complete between-frame shaft crossing width=$width",
                PillarCollision.sweptBodyHitsPillar(birdX, 210f, 210f, radius,
                    before, after, width, mouth, mouth))
            assertFalse("False swept collision through clear mouth width=$width",
                PillarCollision.sweptBodyHitsPillar(birdX, 350f, 350f, radius,
                    before, after, width, mouth, mouth))
        }
    }

    @Test fun staticSweepsExactlyMatchDiscreteCollisionsAcrossGeneratedWorlds() {
        val samples = intArrayOf(1, 59, 60, 119, 180, 359, 360, 1_048_576)
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            for (number in samples) {
                val level = LevelEngine.create(world, number)
                for (gate in level.gates) {
                    for (difficulty in 0..2) {
                        val opening = LevelEngine.opening(gate, 1.2f, difficulty)
                        val x = birdX - gate.width / 2f
                        val mid = (opening.top + opening.bottom) / 2f
                        for (y in floatArrayOf(opening.top - 25f, mid, opening.bottom + 25f)) {
                            val expected = PillarCollision.bodyHitsPillar(
                                birdX, y, radius, x, gate.width, opening)
                            val actual = PillarCollision.sweptBodyHitsPillar(
                                birdX, y, y, radius, x, x, gate.width, opening, opening)
                            assertEquals("world=$world level=$number width=${gate.width} y=$y",
                                expected, actual)
                        }
                    }
                }
            }
        }
    }
}
