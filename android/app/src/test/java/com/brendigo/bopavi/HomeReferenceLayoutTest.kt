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
        assertTrue(home.skins.bottom<home.shop.y)
        assertTrue(home.shop.bottom<home.settings.y)
        assertTrue(home.heroArtwork.bottom<home.skins.y)
        for(i in home.actions.indices)for(j in i+1 until home.actions.size)
            assertFalse(home.actions[i].overlaps(home.actions[j]))
    }

    @Test fun referenceModeKeepsBreakpointAndLargerFontsAccessible() {
        assertTrue(HomeReferenceLayout.supports(941,1672,true))
        assertTrue(HomeReferenceLayout.supports(1080,2400,true))
        assertTrue(HomeReferenceLayout.supports(720,1280,true,density=1f))
        assertFalse(HomeReferenceLayout.supports(720,1280,true,density=2f))
        assertEquals(1f,HomeReferenceLayout.actionScale(1080,1920),.0001f)
        assertEquals(1.19f,HomeReferenceLayout.actionScale(1080,2280),.0001f)
        val narrow=HomeReferenceLayout.actionBounds(1080,1920)
        val tall=HomeReferenceLayout.actionBounds(1080,2400)
        assertEquals(3,narrow.size)
        assertEquals(3,tall.size)
        assertTrue(tall[0].w>narrow[0].w)
        assertTrue(tall[2].y>narrow[2].y)
        for(items in listOf(narrow,tall))
            for(i in 0 until items.lastIndex)
                assertTrue(items[i].bottom<items[i+1].y)
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
