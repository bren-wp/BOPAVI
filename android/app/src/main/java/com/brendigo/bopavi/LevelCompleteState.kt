package com.brendigo.bopavi

import java.text.NumberFormat
import java.util.Locale

/**
 * A read-only snapshot of a completed finite run. The chest is a receipt of
 * the milestone bonus ACTUALLY committed to persistent storage, not a promise
 * of gems or an additional one-tap grant.
 */
internal data class LevelCompleteState(
    val level:Long,
    val stars:Int,
    val collectedCoins:Int,
    val score:Int,
    val justAwardedCoins:Int
) {
    val nextAvailable:Boolean get()=level<LevelEngine.LEVELS_PER_WORLD.toLong()
    val rating:String get()=stars.toString()+"/3"
    val rewardText:String get()=
        if(justAwardedCoins>0)"+ "+format(justAwardedCoins)+" COINS"
        else "NO NEW BONUS"
    val collectedText:String get()=format(collectedCoins)
    val scoreText:String get()=format(score)
    fun shareText():String =
        "BOPAVI — Level "+format(level)+
            " complete! Stars: "+rating+
            ", score: "+scoreText+"."

    companion object {
        private fun format(n:Number):String =
            NumberFormat.getIntegerInstance(Locale.US).format(n.toLong().coerceAtLeast(0L))
        fun fromRun(level:Long,stars:Int,coins:Int,score:Int,
                    awarded:Int):LevelCompleteState =
            LevelCompleteState(
                level.coerceIn(1L,LevelEngine.LEVELS_PER_WORLD.toLong()),
                stars.coerceIn(0,3),coins.coerceAtLeast(0),
                score.coerceAtLeast(0),awarded.coerceAtLeast(0))
    }
}
