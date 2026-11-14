package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class ShopBottomLayoutTest {
    @Test fun woodExtensionClosesGapAtEveryOwnerResolution() {
        assertTrue(ShopBottomLayout.spansViewport(1080,1920))
        assertTrue(ShopBottomLayout.spansViewport(1080,2280))
        assertTrue(ShopBottomLayout.spansViewport(1080,2340))
        assertTrue(ShopBottomLayout.spansViewport(1080,2400))
        assertTrue(ShopBottomLayout.spansViewport(1440,3040))
        assertFalse(ShopBottomLayout.spansViewport(0,2400))
        assertFalse(ShopBottomLayout.spansViewport(1080,0))
        assertEquals(1f,ShopBottomLayout.bottomAnchor,0f)
    }
}
