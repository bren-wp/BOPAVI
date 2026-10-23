package com.brendigo.bopavi

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Place the real, accessible pause Button below the score / fruit / lives HUD.
 *
 * The flight world is rendered at 480 x 800, while the HUD uses a separate
 * width-constrained scale and is offset by the real status/cutout safe inset.
 * Reusing the exact HUD transform avoids overlapping the new third life card
 * on compact phones, notches and large screens. This does not touch physics.
 */
internal object FlightOverlayLayout {
    private fun dp(amount:Float,density:Float):Int =
        (amount*density.coerceAtLeast(.5f)+.5f).toInt()

    fun pauseTopPx(widthPx:Int,heightPx:Int,density:Float,safeTopPx:Int):Int {
        val w=widthPx.coerceAtLeast(1)
        val h=heightPx.coerceAtLeast(1)
        val hudScale=min(w/480f,h/640f)
        // onDraw translates HUD by safeTop+4*scale. The taller top cards end
        // at y=82, so the real Pause button must start below y=(82+4)*scale.
        val hudBottom=safeTopPx.coerceAtLeast(0)+ceil(86f*hudScale).toInt()
        return max(dp(24f,density),hudBottom+dp(12f,density))
    }

    fun pauseRightPx(density:Float,safeRightPx:Int):Int =
        max(dp(15f,density),safeRightPx.coerceAtLeast(0)+dp(10f,density))
}
