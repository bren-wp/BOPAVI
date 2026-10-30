package com.brendigo.bopavi

import java.text.NumberFormat
import java.util.Locale

/** Only saved local results. No remote/global leaderboard is implied. */
internal data class LeaderboardEntry(
    val name:String,val score:Int,val world:Int,val difficulty:Int,val gates:Int
)
internal data class LeaderboardSummary(
    val best:Int,val food:Int,val gates:Int,val runs:Int,val flaps:Long
)
internal data class RankedEntry(val rank:Int,val entry:LeaderboardEntry){
    val description:String get()=
        "Rank $rank, ${entry.name}, ${entry.score} points, "+
        "${LevelEngine.names[entry.world]}, ${entry.gates} obstacles"
}
internal object LocalLeaderboardPresentation {
    fun rows(source:List<LeaderboardEntry>):List<RankedEntry> =
        source.filter{it.world in 0..7 && it.difficulty in 0..2 &&
            it.score in 0..100_000_000 && it.gates in 0..100_000_000}
            .sortedWith(compareByDescending<LeaderboardEntry>{it.score}
                .thenByDescending{it.gates}).take(10)
            .mapIndexed{index,item->
                RankedEntry(index+1,item.copy(
                    name=item.name.trim().take(24).ifBlank{"Igrač"}))
            }
    fun number(value:Number):String =
        NumberFormat.getIntegerInstance(Locale.US).format(value.toLong().coerceAtLeast(0L))
}
