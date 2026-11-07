package com.brendigo.bopavi

/**
 * Keep the complete winged wordmark below the *actual* native wallet row.
 * Extreme camera cutouts must never make BOPAVI invisible.
 *
 * Scale about the bottom edge (same anchors as reference art); shift only
 * when the remaining space would otherwise force a zero-sized brand.
 */
internal data class HomeLogoFit(val scale:Float,val offsetY:Float) {
    fun visibleTop(originalTop:Float,height:Float):Float =
        originalTop + height*(1f-scale) + offsetY
}

internal object HomeLogoPlacement {
    const val MIN_READABLE_SCALE=.62f

    fun fit(walletBottom:Float,logoTop:Float,
            logoHeight:Float,gap:Float):HomeLogoFit {
        if(logoHeight<=0f) return HomeLogoFit(1f,0f)
        val fullBottom=logoTop+logoHeight
        val available=(fullBottom-walletBottom-gap)/logoHeight
        val scale=available.coerceIn(MIN_READABLE_SCALE,1f)
        val newTop=fullBottom-logoHeight*scale
        val offset=(walletBottom+gap-newTop).coerceAtLeast(0f)
        return HomeLogoFit(scale,offset)
    }
}
