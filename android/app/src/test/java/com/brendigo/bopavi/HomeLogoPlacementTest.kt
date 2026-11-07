package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class HomeLogoPlacementTest {
    @Test fun referenceHomeKeepsFullSizeLogoBelowCurrencyRow() {
        val fit=HomeLogoPlacement.fit(85f,100f,285f,8f)
        assertEquals(1f,fit.scale,.0001f)
        assertEquals(0f,fit.offsetY,.0001f)
        assertTrue(fit.visibleTop(100f,285f)>=85f+8f)
    }
    @Test fun notchedAndroidHeaderScalesUniformlyWithoutOverlap() {
        val fit=HomeLogoPlacement.fit(176f,100f,285f,8f)
        assertTrue(fit.scale in HomeLogoPlacement.MIN_READABLE_SCALE..1f)
        assertTrue(fit.visibleTop(100f,285f)>=184f-.001f)
        assertEquals(0f,fit.offsetY,.0001f)
    }
    @Test fun extremelyTallCutoutNeverMakesTheWordmarkDisappear() {
        val fit=HomeLogoPlacement.fit(362f,100f,285f,8f)
        assertEquals(HomeLogoPlacement.MIN_READABLE_SCALE,fit.scale,.0001f)
        assertTrue(fit.offsetY>0f)
        assertTrue(fit.visibleTop(100f,285f)>=370f-.001f)
    }
    @Test fun zeroHeightDuringInitialMeasureIsSafe() {
        val fit=HomeLogoPlacement.fit(100f,0f,0f,8f)
        assertEquals(1f,fit.scale,.0001f)
        assertEquals(0f,fit.offsetY,.0001f)
    }
}
