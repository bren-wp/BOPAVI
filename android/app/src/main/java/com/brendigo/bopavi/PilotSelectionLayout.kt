package com.brendigo.bopavi

/** Reference camera geometry; every card, avatar and label uses the same scale. */
internal object PilotSelectionLayout {
    const val referenceWidth=941f
    const val referenceHeight=1672f
    val cardLefts=floatArrayOf(52f,344f,636f)
    const val cardTop=435f
    const val cardWidth=253f
    const val cardHeight=652f
    const val portraitInset=19f
    const val portraitTop=478f
    const val portraitSize=215f
    const val activeSelectionTop=1191f
    const val homeTop=1475f
    fun fitsReference():Boolean=cardLefts.size==3 &&
        cardLefts.all{it>=0f && it+cardWidth<=referenceWidth} &&
        cardTop+cardHeight<activeSelectionTop && homeTop+120f<=referenceHeight
}
