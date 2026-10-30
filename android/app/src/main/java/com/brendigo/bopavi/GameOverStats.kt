package com.brendigo.bopavi

import java.text.NumberFormat
import java.util.Locale

/**
 * Results display is a projection of an actual finished flight.
 * Endless has berries, not spendable coins or invented crystal currency.
 * Finite has real collected coins and level stars.
 */
internal data class GameOverStats(
    val distance:String,
    val collectedLabel:String,
    val collected:String,
    val thirdLabel:String,
    val third:String
) {
    companion object {
        private fun format(value:Int):String =
            NumberFormat.getIntegerInstance(Locale.US).format(value.coerceAtLeast(0))

        fun fromRun(distance:Float,coins:Int,stars:Int,food:Int,
                    score:Int,endless:Boolean):GameOverStats {
            val travelled=if(distance.isFinite())distance.coerceAtLeast(0f).toInt() else 0
            return if(endless)GameOverStats(
                format(travelled),"Food Collected",format(food),
                "Score",format(score))
            else GameOverStats(
                format(travelled),"Coins Collected",format(coins),
                "Stars Collected",format(stars))
        }
    }
}
