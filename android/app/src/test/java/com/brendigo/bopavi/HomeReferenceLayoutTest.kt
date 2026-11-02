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
        assertTrue(home.hero.bottom<home.play.y)
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
