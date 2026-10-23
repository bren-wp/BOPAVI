package com.brendigo.bopavi

/**
 * Pure HUD policy for long marathon counters and the actual remaining lives.
 * The visible number is never truncated or rounded. The caller measures the
 * true glyph width with its own Paint, applies these scalar values, then resets
 * textScaleX after drawing. No allocations are required inside onDraw().
 */
internal object FlightHudReadability {
    private const val BASE_SIZE=23f
    private const val MIN_SIZE=11f

    fun fontSize(measuredAtBase:Float,availableWidth:Float):Float {
        if(!measuredAtBase.isFinite() || measuredAtBase<=0f ||
            !availableWidth.isFinite() || availableWidth<=0f) return MIN_SIZE
        return (BASE_SIZE*(availableWidth/measuredAtBase).coerceAtMost(1f))
            .coerceIn(MIN_SIZE,BASE_SIZE)
    }

    fun horizontalScale(measuredAtBase:Float,availableWidth:Float,size:Float):Float {
        if(!measuredAtBase.isFinite() || measuredAtBase<=0f ||
            !availableWidth.isFinite() || availableWidth<=0f ||
            !size.isFinite() || size<=0f) return 1f
        // A large 10-digit score could still overflow at MIN_SIZE. Keep every
        // digit in its card without allocating or producing a fake abbreviated score.
        return (availableWidth/(measuredAtBase*size/BASE_SIZE)).coerceIn(.01f,1f)
    }

    fun lifeCountColor(lives:Int,maxLives:Int):Int = when {
        lives<=1 -> 0xffffa3b2.toInt() // Last heart: high-contrast warning
        lives<maxLives -> 0xffffd99b.toInt()
        else -> 0xffffdce5.toInt()
    }

    fun isFinalLife(lives:Int,maxLives:Int):Boolean =
        maxLives>1 && lives==1
}
