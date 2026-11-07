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
    @Test fun storefrontCardsHaveSixDistinctRealProducts() {
        val names=listOf("SHIELD","MAGNET","DOUBLE SHIELD",
            "LONG MAGNET","TRIPLE SHIELD","SUPER MAGNET")
        val all=(0..5).map {
            ShopItemState(it,"Stored perk $it",100,1,1000)
        }
        assertEquals(names,all.map { it.storefrontTitle })
        assertEquals(6,all.map { it.storefrontTitle }.distinct().size)
        assertEquals(listOf("×1","8s","×2","12s","×3","18s"),
            all.map{it.iconTierLabel})
        assertEquals(listOf("Blocks one hit","Attracts rewards for 8s",
            "Blocks two hits","Attracts rewards for 12s",
            "Blocks three hits","Attracts rewards for 18s"),
            all.map{it.perkEffect})
        assertTrue(all.all{it.canBuy})
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
        assertTrue(ShopReferenceLayout.supports(941,1672,true,1.0f))
        assertFalse(ShopReferenceLayout.supports(720,1280,true,1.0f))
        assertFalse(ShopReferenceLayout.supports(941,1672,true,1.5f))
        assertFalse(ShopReferenceLayout.supports(1920,1080,false,1.0f))
        assertEquals(3,ShopReferenceLayout.columns.size)
        assertEquals(2,ShopReferenceLayout.rows.size)
        assertTrue(ShopReferenceLayout.featureTop+ShopReferenceLayout.featureHeight<
            ShopReferenceLayout.rows[0])
        assertTrue(ShopReferenceLayout.rows[0]+ShopReferenceLayout.cardHeight<
            ShopReferenceLayout.rows[1])
        assertEquals("Shield, price 80 coins, owned 5 of 99, available to buy",
            ShopItemState(0,"Shield",80,5,200).accessibility)
    }
}
