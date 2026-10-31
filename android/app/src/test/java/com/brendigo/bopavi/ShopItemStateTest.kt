package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

class ShopItemStateTest {
    @Test fun actualSixPerksAndTheirPricesKeepExistingIds() {
        val prices=listOf(80,65,150,120,230,195)
        val all=prices.mapIndexed{idx,price->
            ShopItemState(idx,"Power $idx",price,0,999)
        }
        assertEquals(listOf(0,1,2,3,4,5),all.map{it.index})
        assertTrue(all.all{it.canBuy})
        assertEquals(listOf(true,false,true,false,true,false),all.map{it.isShield})
        assertEquals(listOf(1,1,2,2,3,3),all.map{it.tier})
    }
    @Test fun insufficientBalanceAndMaxInventoryBlockActualButton() {
        val poor=ShopItemState(0,"Shield",80,0,79)
        assertFalse(poor.canBuy)
        assertEquals("NEED 80 COINS",poor.buttonLabel)
        val full=ShopItemState(1,"Magnet",65,99,1000)
        assertFalse(full.canBuy)
        assertEquals("MAX 99",full.buttonLabel)
        assertTrue(ShopItemState(0,"Shield",80,98,80).canBuy)
    }
    @Test fun shopGeometryAndAccessibilityMatchReference() {
        assertTrue(ShopReferenceLayout.valid())
        assertEquals("Shield, price 80 coins, owned 5 of 99, available to buy",
            ShopItemState(0,"Shield",80,5,200).accessibility)
    }
}
