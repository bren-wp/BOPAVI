package com.brendigo.bopavi

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A painted cap legitimately extends 7 units left and 5 units right of the
 * shaft; distant cliffs must not have the same visual overhang because their
 * physical collider is strictly [x, x + width]. Protect every generated
 * 54–104-unit pillar width and a fully passable corridor.
 */
class PillarVisualContactTest {
    @Test fun onlyRoundedMouthLipsHaveLateralOverhang() {
        val mouth=LevelEngine.Opening(300f,470f)
        for(width in listOf(54f,68f,74f,80f,92f,104f)) {
            val left=220f
            val right=left+width
            val narrowBird=2f
            // Upper/lower roots are drawn inside the shaft; an outside bird
            // can fly next to distant scenery without an invisible hit.
            assertFalse("No phantom upper root collision at width $width",
                PillarCollision.bodyHitsPillar(left-5f,65f,narrowBird,left,width,mouth))
            assertFalse("No phantom lower root collision at width $width",
                PillarCollision.bodyHitsPillar(right+5f,680f,narrowBird,left,width,mouth))
            // Only the clearly visible rounded cap shoulders can be struck.
            assertTrue("Upper cap shoulder must collide at width $width",
                PillarCollision.bodyHitsPillar(left-5f,286f,3f,left,width,mouth))
            assertTrue("Lower cap shoulder must collide at width $width",
                PillarCollision.bodyHitsPillar(right+4f,483f,3f,left,width,mouth))
            assertFalse("The true corridor remains open at width $width",
                PillarCollision.bodyHitsPillar(left+width*.5f,382f,18f,left,width,mouth))
        }
    }

    @Test fun realBodyCollisionRemainsConsistentAcrossDifficultyIndependentArt() {
        for(width in listOf(54f,68f,74f,80f,92f,104f)) {
            val mouth=LevelEngine.Opening(270f,450f)
            assertTrue(PillarCollision.bodyHitsPillar(
                240f,150f,18f,220f,width,mouth))
            assertFalse(PillarCollision.bodyHitsPillar(
                240f,360f,18f,220f,width,mouth))
        }
    }
}
