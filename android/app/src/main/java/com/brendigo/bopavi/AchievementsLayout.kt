package com.brendigo.bopavi

internal object AchievementsLayout {
    const val width=941f
    const val height=1672f
    val cols=floatArrayOf(60f,487f)
    val rows=floatArrayOf(517f,743f,969f,1195f)
    const val cardWidth=394f
    const val cardHeight=209f
    fun valid()=cols.size==2 && rows.size==4 &&
        cols.all{it>=0 && it+cardWidth<=width} &&
        rows.zipWithNext().all{it.second>=it.first+cardHeight} &&
        rows.last()+cardHeight<1481f
}
