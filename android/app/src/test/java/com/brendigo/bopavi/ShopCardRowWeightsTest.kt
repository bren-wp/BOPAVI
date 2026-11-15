package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class ShopCardRowWeightsTest {
    @Test fun nativeSixProductCardKeepsSameTotalAndBuyArea() {
        assertEquals(4.04f,ShopCardRowWeights.TOTAL,.0001f)
        assertEquals(1f,ShopCardRowWeights.BUY,0f)
        assertTrue(ShopCardRowWeights.ILLUSTRATION>1f)
        assertTrue(ShopCardRowWeights.EFFECT>.7f)
        assertTrue(ShopCardRowWeights.HEADER>.65f)
    }

    @Test fun longNativeProductTitlesReceiveLargerTextArea() {
        val previouslyAvailable=.65f/4.04f
        assertTrue(ShopCardRowWeights.titleFraction()>previouslyAvailable*1.3f)
        assertTrue(ShopCardRowWeights.titleFraction()<.25f)
    }
}
