package com.brendigo.bopavi

/** Actual owner 941×1672 composition: a hero above four genuine pilots. */
internal object PilotSelectionLayout {
    const val referenceWidth=941f
    const val referenceHeight=1672f
    // Actual owner tall Skins composition is ~150px above the uniformly
    // centered camera on 1080×2400. With a 480px letterbox, .18 vs .5
    // removes 154px of displacement; at 16:9 both anchors are identical.
    const val ownerVerticalAnchor=.18f
    // Owner 1080x2400 reference: hero ≈34px, stone podium ≈90px,
    // 4 selection cards and EQUIP ≈150px lower than the old uniform
    // top-anchored transform. 16:9 has zero letterboxing difference.
    const val heroVerticalAnchor=.25f
    const val podiumVerticalAnchor=.37f
    const val cardsVerticalAnchor=.5f
    const val bannerLeft=250f
    const val bannerTop=166f
    const val bannerWidth=441f
    const val bannerHeight=128f
    // The v1.0.88 same-resolution Android capture has a wider Bopi silhouette
    // than the original owner Skins art. Its square actor also overlaps BOTH
    // carousel arrow rectangles, which risks illustrated/interaction overlap.
    // Keep the hero center at the 941px stage midpoint, preserve the actual
    // animated 512px sprite and stored skin mapping, but reduce its camera
    // box from 688px to 626px. This leaves visible space around each arrow
    // and lifts the painted lower feathers above the cards and native info.
    // Exact RGB parity requires fresh candidate emulator PNG measurements.
    const val heroLeft=157f
    const val heroTop=260f
    const val heroSize=626f
    const val platformLeft=166f
    const val platformTop=811f
    const val platformWidth=612f
    const val platformHeight=129f
    val cardLefts=floatArrayOf(23f,252f,481f,710f)
    const val cardTop=973f
    const val cardWidth=207f
    const val cardHeight=307f
    const val activeSelectionTop=1299f
    fun fitsReference():Boolean=cardLefts.size==4 &&
        cardLefts.all{it>=0f && it+cardWidth<=referenceWidth} &&
        (0 until cardLefts.lastIndex).all{index->
            cardLefts[index]+cardWidth+10f<=cardLefts[index+1]
        } &&
        bannerLeft>=0f && bannerLeft+bannerWidth<=referenceWidth &&
        heroLeft>=0f && heroLeft+heroSize<=referenceWidth &&
        heroTop+heroSize<cardTop &&
        // Native left/right arrow touch areas: [26,138] and [803,915].
        heroLeft>=138f && heroLeft+heroSize<=803f &&
        platformTop+platformHeight<cardTop &&
        cardTop+cardHeight<activeSelectionTop &&
        activeSelectionTop+193f<=referenceHeight
}
