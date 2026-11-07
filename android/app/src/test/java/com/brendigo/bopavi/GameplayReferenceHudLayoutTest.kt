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
    @Test fun shortDisplaysAndNotchesFitControlsAndAgreeWithTouchMapping() {
        val h=GameplayReferenceHudLayout
        for(viewport in listOf(Triple(320,480,40),Triple(240,320,84),
            Triple(480,640,55),Triple(864,1536,60),Triple(1080,2400,65))) {
            val (width,height,safe)=viewport
            val scale=h.scaleFor(width,height,safe)
            assertTrue(scale>0f)
            val originX=(width-480f*scale)*.5f
            // All HUD content, including the pause button, fits in safe area.
            assertTrue(originX+h.pause.right*scale<=width)
            assertTrue(safe+(h.pause.bottom+1f)*scale<=height)
            val pauseX=originX+451f*scale
            val pauseY=safe+36f*scale
            val plusX=originX+107f*scale
            val plusY=safe+26f*scale
            assertEquals("pause touch mismatch on $viewport",1,
                h.hitArea(pauseX,pauseY,width,height,safe))
            assertEquals("wallet touch mismatch on $viewport",2,
                h.hitArea(plusX,plusY,width,height,safe))
        }
        assertEquals(0f,h.scaleFor(240,80,84),0f)
        assertEquals(0,h.hitArea(10f,10f,240,80,84))
    }
}
