package com.brendigo.bopavi

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ceil
import kotlin.math.min

class FlightOverlayLayoutTest {
    @Test fun pauseNeverCoversTheLivesCardOnCompactAndLargeScreens() {
        for(widthDp in listOf(320,360,420,600,840)) {
            val density=2.5f
            val widthPx=(widthDp*density).toInt()
            val heightPx=((if(widthDp<600) 640 else 1000)*density).toInt()
            for(safeTop in listOf(0,50,120)) {
                val layoutTop=FlightOverlayLayout.pauseTopPx(
                    widthPx,heightPx,density,safeTop)
                val scale=min(widthPx/480f,heightPx/640f)
                val cardBottom=safeTop+
                    ceil((FlightHudLayout.lives.bottom+4f)*scale).toInt()
                assertTrue("The pause button may not overlap the lives HUD",
                    layoutTop>=cardBottom+(10*density).toInt())
                assertTrue("Pause control must fit within the physical viewport",
                    layoutTop+(56*density).toInt()<heightPx)
            }
        }
    }

    @Test fun pauseRespectsRightSideCutoutsAndMinimumMargin() {
        for(safeRight in listOf(-1,0,20,80)) {
            val right=FlightOverlayLayout.pauseRightPx(3f,safeRight)
            assertTrue(right>=(15*3))
            assertTrue(right>=safeRight.coerceAtLeast(0)+(10*3))
        }
    }
    @Test fun compactAndSplitScreenWindowsUseActualLifeCardHeight() {
        for(width in listOf(320,360,540,900)) {
            for(height in listOf(300,420,640,960)) {
                val density=2.25f
                val w=(width*density).toInt()
                val h=(height*density).toInt()
                val scale=min(w/480f,h/640f)
                val hudBottom=ceil((FlightHudLayout.lives.bottom+4f)*scale).toInt()
                val pauseTop=FlightOverlayLayout.pauseTopPx(w,h,density,0)
                assertTrue(pauseTop>=hudBottom+(12*density+.5f).toInt())
                assertTrue("Pause must remain visible on realistic resized windows",
                    pauseTop+(56*density).toInt()<=h)
            }
        }
    }

}
