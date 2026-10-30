package com.brendigo.bopavi

/** A pure replay destination: no disk writes, view changes or score updates. */
internal sealed class PauseReplayTarget {
    object Endless:PauseReplayTarget()
    data class Finite(val world:Int,val level:Long):PauseReplayTarget()

    companion object {
        fun fromStage(stage:Pair<Int,Long>?):PauseReplayTarget =
            if(stage==null)Endless else Finite(stage.first,stage.second)
    }
}
