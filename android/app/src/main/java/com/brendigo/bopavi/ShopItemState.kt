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
