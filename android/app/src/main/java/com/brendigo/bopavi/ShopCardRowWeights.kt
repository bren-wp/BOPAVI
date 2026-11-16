package com.brendigo.bopavi

/**
 * Pure weight contract for the 6 native 3×2 Shop product cards.
 *
 * The v1.0.88 release allocated only .65/4.04 of each card's useful height
 * to a max-two-line product name. On authentic API36 1080×1920 captures,
 * "SUPER MAGNET" and "DOUBLE SHIELD" were visibly clipped to one line.
 * Reallocate space FROM decorative illustration/effect, not from the real
 * buy button. Same overall 4.04 weights => no new product card/row geometry.
 */
internal object ShopCardRowWeights {
    const val HEADER=.90f
    const val ILLUSTRATION=1.35f
    const val EFFECT=.79f
    const val BUY=1.00f
    const val TOTAL=HEADER+ILLUSTRATION+EFFECT+BUY

    fun titleFraction():Float=HEADER/TOTAL
}
