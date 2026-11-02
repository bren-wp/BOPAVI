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
