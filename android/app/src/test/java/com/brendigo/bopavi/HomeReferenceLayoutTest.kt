package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class HomeReferenceLayoutTest {
    @Test fun exactlyThreeIndependentHomeActions() {
        val home=HomeReferenceLayout
        assertTrue(home.valid())
        assertEquals(3,home.actions.size)
        assertEquals(listOf(home.skins,home.shop,home.settings),home.actions)
        assertEquals(941f,home.width,0f)
        assertEquals(1672f,home.height,0f)
        assertEquals(home.skins.y,home.shop.y,0f)
        assertTrue(home.settings.y>home.skins.bottom)
        assertTrue(home.heroArtwork.bottom<home.skins.y)
        for(i in home.actions.indices)for(j in i+1 until home.actions.size)
            assertFalse(home.actions[i].overlaps(home.actions[j]))
    }

    @Test fun referenceModeKeepsBreakpointAndLargerFontsAccessible() {
        assertTrue(HomeReferenceLayout.supports(941,1672,true))
        assertTrue(HomeReferenceLayout.supports(1080,2400,true))
        assertTrue(HomeReferenceLayout.supports(720,1280,true))
        assertFalse(HomeReferenceLayout.supports(1920,1080,false))
        assertFalse(HomeReferenceLayout.supports(941,1672,true,1.5f))
    }

    @Test fun balanceIsPassiveAndLogoStaysClear() {
        val home=HomeReferenceLayout
        assertTrue(home.coins.inside())
        assertTrue(home.coins.bottom<home.logoArtwork.y)
        assertTrue(home.logoArtwork.bottom<home.heroArtwork.y)
        assertFalse(home.coins.overlaps(home.logoArtwork))
    }
}
