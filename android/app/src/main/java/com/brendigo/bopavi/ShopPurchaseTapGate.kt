package com.brendigo.bopavi

/**
 * UI-only repeat-tap filter. It never changes currency, inventory or reward
 * amounts; only ProgressStore.buyPerk carries out a legitimate purchase.
 * Window is deliberately short, and a different product remains buyable.
 */
internal class ShopPurchaseTapGate(private val intervalMs:Long=450L){
    private var lastIndex=-1
    private var lastTime=-1L

    fun allow(index:Int,nowMs:Long):Boolean{
        if(index !in 0..5 || nowMs<0L)return false
        if(index==lastIndex && lastTime>=0L && nowMs>=lastTime &&
            nowMs-lastTime<intervalMs)return false
        lastIndex=index
        lastTime=nowMs
        return true
    }
}
