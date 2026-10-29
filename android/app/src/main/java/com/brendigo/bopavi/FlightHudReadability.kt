package com.brendigo.bopavi

/** Only the remaining-life state is presented during gameplay. */
internal object FlightHudReadability {
    fun lifeCountColor(lives:Int,maxLives:Int):Int = when {
        lives<=1 -> 0xffffa3b2.toInt()
        lives<maxLives -> 0xffffd99b.toInt()
        else -> 0xffffdce5.toInt()
    }

    fun isFinalLife(lives:Int,maxLives:Int):Boolean =
        maxLives>1 && lives==1
}
