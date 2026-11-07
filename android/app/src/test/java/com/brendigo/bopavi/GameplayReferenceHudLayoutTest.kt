package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class GameplayReferenceHudLayoutTest {
    @Test fun walletsScoreAndPauseDoNotOverlap() {
        val h=GameplayReferenceHudLayout
        assertTrue(h.coinWallet.right<h.crystalWallet.left)
        assertTrue(h.coinWallet.bottom<h.score.top)
        assertTrue(h.crystalWallet.bottom<h.score.top)
        assertTrue(h.score.right<h.pause.left)
        assertTrue(h.coinPlus.encloses(107f,25f))
        assertTrue(h.crystalPlus.encloses(225f,25f))
    }
    @Test fun pauseAndWalletHitTargetsUseActualNativeViewport() {
        val h=GameplayReferenceHudLayout
        assertEquals(1,h.hitArea(451f,35f,480,800,0))
        assertEquals(2,h.hitArea(105f,24f,480,800,0))
        assertEquals(2,h.hitArea(224f,24f,480,800,0))
        assertEquals(0,h.hitArea(178f,380f,480,800,0))
        // 1080x2400 Android phone uses uniform 480x800 camera width.
        assertEquals(1,h.hitArea(451f*2.25f,36f*2.25f+65f,1080,2400,65))
        assertEquals(2,h.hitArea(107f*2.25f,26f*2.25f+65f,1080,2400,65))
        assertEquals(0,h.hitArea(470f,1000f,1080,2400,65))
    }
    @Test fun tinyAndInvalidWindowsCannotDivideByZero() {
        val h=GameplayReferenceHudLayout
        assertEquals(0,h.hitArea(3f,4f,0,640,0))
        assertEquals(0,h.hitArea(3f,4f,320,0,0))
        assertEquals(1,h.hitArea(451f*.5f,35f*.5f,240,400,0))
    }
}
