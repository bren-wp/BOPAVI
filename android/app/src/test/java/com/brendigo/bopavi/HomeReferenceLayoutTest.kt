package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class HomeReferenceLayoutTest {
    @Test fun portraitOwnerReferenceHasNineLiveNonoverlappingControls(){
        assertTrue(HomeReferenceLayout.valid())
        assertEquals(7,HomeReferenceLayout.actions.size)
        assertEquals(941f,HomeReferenceLayout.width,0f)
        assertEquals(1672f,HomeReferenceLayout.height,0f)
        assertTrue(HomeReferenceLayout.play.w>600f)
        assertTrue(HomeReferenceLayout.hero.bottom<HomeReferenceLayout.play.y)
    }
    @Test fun referenceModeIsOnlyUsedOnLargePortraitScreens(){
        assertTrue(HomeReferenceLayout.supports(941,1672,true))
        assertTrue(HomeReferenceLayout.supports(1080,2400,true))
        assertFalse(HomeReferenceLayout.supports(720,1280,true))
        assertFalse(HomeReferenceLayout.supports(1920,1080,false))
    }
    @Test fun actualTopWalletsAreSeparatedAndActionsFit(){
        assertFalse(HomeReferenceLayout.coins.overlaps(HomeReferenceLayout.stars))
        assertTrue(HomeReferenceLayout.coins.inside())
        assertTrue(HomeReferenceLayout.stars.inside())
        assertTrue(HomeReferenceLayout.sound.inside())
    }
}
