package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class ShopHeaderLayoutTest {
    @Test fun headerAndTitleUseOneOwnerOffset() {
        assertEquals(85f,ShopHeaderLayout.titleOffset,0f)
        assertEquals(208f,ShopHeaderLayout.titleTop,0f)
        // Header stays in the first portion of the native reference scene;
        // it cannot occlude the featured purchase card at y=562.
        assertTrue(ShopHeaderLayout.titleTop+201f<ShopReferenceLayout.featureTop)
    }
}
