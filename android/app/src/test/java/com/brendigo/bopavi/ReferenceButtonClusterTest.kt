package com.brendigo.bopavi

import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceButtonClusterTest {
    @Test fun playClusterHasNoHugeBlankTextColumn() {
        val v=ReferenceButtonCluster.measure(645,191,275f,.70f,.29f)
        assertTrue(v.totalWidth<645)
        assertTrue(v.gap in 4..12)
        assertTrue(v.iconWidth in 110..145)
        assertTrue(v.labelWidth in 285..315)
    }
    @Test fun skinsShopAndSettingsStayInsideBounds() {
        for(w in listOf(325,453)) for(word in listOf(144f,172f,186f)) {
            val v=ReferenceButtonCluster.measure(w,137,word,.61f,.29f)
            assertTrue(v.totalWidth<=w)
            assertTrue(v.labelWidth>=word.toInt())
        }
    }
    @Test fun resultsActionsUseMeasuredCompactText() {
        for(v in listOf(
            ReferenceButtonCluster.measure(575,150,270f,.70f,.27f),
            ReferenceButtonCluster.measure(459,124,230f,.70f,.27f))) {
            assertTrue(v.gap<=12)
            assertTrue(v.totalWidth<=575)
        }
    }
    @Test fun narrowContentStillFits() {
        for(w in listOf(80,145,320,941)) {
            val v=ReferenceButtonCluster.measure(w,95,500f,.75f,.30f)
            assertTrue(v.totalWidth<=w)
            assertTrue(v.labelWidth>0)
        }
    }
}
