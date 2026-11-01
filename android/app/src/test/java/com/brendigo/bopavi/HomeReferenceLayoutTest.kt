package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class HomeReferenceLayoutTest {
    @Test fun ownerReferenceHasSixLargeButtonsAndAnAccessibleRecordsBadge(){
        val home=HomeReferenceLayout
        assertTrue(home.valid())
        assertEquals(6,home.actions.size)
        assertEquals(941f,home.width,0f)
        assertEquals(1672f,home.height,0f)
        assertTrue(home.play.w>600f)
        assertTrue(home.hero.bottom<home.play.y)
        assertTrue(home.skins.bottom<home.achievements.y)
        assertEquals(home.skins.y,home.shop.y,0f)
        assertEquals(home.shop.y,home.settings.y,0f)
        assertEquals(home.achievements.y,home.sound.y,0f)
    }
    @Test fun referenceModeIsOnlyUsedOnLargePortraitScreens(){
        assertTrue(HomeReferenceLayout.supports(941,1672,true))
        assertTrue(HomeReferenceLayout.supports(1080,2400,true))
        assertFalse(HomeReferenceLayout.supports(720,1280,true))
        assertFalse(HomeReferenceLayout.supports(1920,1080,false))
    }
    @Test fun liveWalletsAreSeparatedAndRecordsBadgeStaysInsideAchievements(){
        val home=HomeReferenceLayout
        assertFalse(home.coins.overlaps(home.stars))
        assertTrue(home.coins.x<home.logo.x)
        assertTrue(home.stars.x>home.logo.x)
        assertTrue(home.leaderboard.inside())
        assertTrue(home.leaderboard.x>=home.achievements.x)
        assertTrue(home.leaderboard.right<=home.achievements.right)
        assertTrue(home.sound.inside())
    }
}
