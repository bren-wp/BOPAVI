package com.brendigo.bopavi

/** Measured 941x1672 shop canvas; all components share one camera. */
internal object ShopReferenceLayout {
    const val width=941f
    const val height=1672f
    val columns=floatArrayOf(52f,484f)
    val rows=floatArrayOf(454f,791f,1128f)
    const val cardWidth=405f
    const val cardHeight=324f
    const val homeTop=1505f
    const val homeHeight=113f
    fun valid():Boolean=columns.size==2 && rows.size==3 &&
        columns.all{it>=0f && it+cardWidth<=width} &&
        rows.all{it>=0f && it+cardHeight<=height} &&
        rows.last()+cardHeight<homeTop && homeTop+homeHeight<=height
}
