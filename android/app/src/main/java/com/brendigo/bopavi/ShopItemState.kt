package com.brendigo.bopavi

/**
 * A readonly view model based solely on the existing saved inventory.
 * Prices are defined by ProgressStore; never add a second fictional wallet.
 */
internal data class ShopItemState(
    val index:Int,
    val name:String,
    val price:Int,
    val owned:Int,
    val balance:Int
) {
    val soldOut:Boolean get()=owned>=99
    val affordable:Boolean get()=balance>=price
    val canBuy:Boolean get()=!soldOut && affordable
    val isShield:Boolean get()=index%2==0
    val tier:Int get()=index/2+1
    // Presentation-only copy mirrors the functional three shield/magnet tiers;
    // no GEM PACK, random skin or virtual gem wallet is advertised unless
    // those products are backed by a real offline transaction.
    val storefrontTitle:String get()=when(index){
        0 -> "SHIELD"
        1 -> "MAGNET"
        2 -> "DOUBLE SHIELD"
        3 -> "LONG MAGNET"
        4 -> "TRIPLE SHIELD"
        else -> "SUPER MAGNET"
    }
    val perkEffect:String get()=if(isShield) {
        when(tier){1->"Blocks one hit";2->"Blocks two hits";else->"Blocks three hits"}
    } else {
        val seconds=when(tier){1->8;2->12;else->18}
        "Attracts rewards for ${seconds}s"
    }
    val iconTierLabel:String get()=if(isShield)"×${tier}" else
        "${when(tier){1->8;2->12;else->18}}s"
    val buttonLabel:String get()=when {
        soldOut->"MAX 99"
        !affordable->"NEED $price COINS"
        else->"BUY • $price"
    }
    val accessibility:String get()=
        "$name, price $price coins, owned $owned of 99, "+
            (if(soldOut)"inventory full" else if(!affordable)"not enough coins" else "available to buy")

    companion object {
        fun fromStore(progress:ProgressStore):List<ShopItemState> =
            progress.perkPrices.indices.map{index->
                ShopItemState(index,progress.perkNames[index],progress.perkPrices[index],
                    progress.perkCount(index),progress.coins())
            }
    }
}
