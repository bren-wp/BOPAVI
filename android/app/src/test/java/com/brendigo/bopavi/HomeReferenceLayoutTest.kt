package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class HomeReferenceLayoutTest {
    @Test fun ownerReferenceHasFourMainButtonsAndThreeFooterShortcuts(){
        val home=HomeReferenceLayout
        assertTrue(home.valid())
        assertEquals(7,home.actions.size)
        assertEquals(941f,home.width,0f)
        assertEquals(1672f,home.height,0f)
        assertTrue(home.play.w>600f)
        assertTrue(home.playWings.x<home.play.x)
        assertTrue(home.playWings.right>home.play.right)
        assertTrue(home.playWings.bottom<=home.play.bottom)
        assertTrue(home.heroArtwork.bottom<home.play.y)
        assertTrue(home.logo.bottom<home.heroArtwork.y)
        assertEquals("Bopi must retain the square 512px composition grid",home.hero.w,home.hero.h,0f)
        assertTrue("Home Bopi should not shrink inside a landscape image view",home.hero.w>=640f)
        assertTrue(home.skins.bottom<home.achievements.y)
        assertEquals(home.skins.y,home.shop.y,0f)
        assertTrue(home.settings.y>home.skins.y)
        assertTrue(home.settings.bottom<home.achievements.y)
        assertEquals(home.achievements.y,home.leaderboard.y,0f)
        assertEquals(home.leaderboard.y,home.sound.y,0f)
    }
    @Test fun referenceModeIsOnlyUsedOnLargePortraitScreens(){
        assertTrue(HomeReferenceLayout.supports(941,1672,true))
        assertTrue(HomeReferenceLayout.supports(1080,2400,true))
        assertFalse(HomeReferenceLayout.supports(720,1280,true))
        assertFalse(HomeReferenceLayout.supports(1920,1080,false))
    }
    @Test fun screenDensityAndLargeFontBreakpointMatrix(){
        val modes=listOf(
            Triple(360,640,false),Triple(480,800,false),
            Triple(600,960,false),Triple(720,1280,false),
            Triple(941,1672,true),Triple(1080,1920,true),
            Triple(1080,2400,true),Triple(1440,2960,true),
            Triple(2400,1080,false))
        for((w,h,reference) in modes){
            assertEquals("$w x $h",reference,
                HomeReferenceLayout.supports(w,h,w<h,1f))
            assertFalse(HomeReferenceLayout.supports(w,h,w<h,1.5f))
        }
        assertFalse(HomeReferenceLayout.supports(941,1672,false,1f))
        assertFalse(HomeReferenceLayout.supports(941,1672,true,2f))
        assertFalse(HomeReferenceLayout.supports(941,1672,true,0f))
        // Exhaustive logical breakpoint sweep: 29 widths × 23 heights ×
        // four font scales. This checks selection logic, not visual pixels.
        for(w in 240..2560 step 80) for(h in 320..3840 step 160)
            for(scale in floatArrayOf(1f,1.3f,1.5f,2f)){
                val supported=HomeReferenceLayout.supports(w,h,w<h,scale)
                if(supported){
                    assertTrue(w>=860 && h>=1480 && w<h && scale<1.3f)
                }
                if(scale>=1.3f) assertFalse(supported)
                if(w>=h) assertFalse(supported)
            }
    }

    @Test fun upperRightLiveWalletsAndThreeIndependentFooterControls(){
        val home=HomeReferenceLayout
        assertFalse(home.coins.overlaps(home.stars))
        assertTrue(home.coins.x>home.logo.x)
        assertTrue(home.stars.x>home.coins.x)
        assertTrue(home.coins.bottom<home.logo.y)
        assertTrue(home.leaderboard.inside())
        assertFalse(home.leaderboard.overlaps(home.achievements))
        assertFalse(home.leaderboard.overlaps(home.sound))
        assertTrue(home.sound.inside())
    }
}
