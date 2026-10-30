package com.brendigo.bopavi

import android.app.Activity
import android.view.View

/** Typed routes for real offline screens. No duplicated/dead UI builders. */
internal class ReferenceMenus(private val host:Activity, private val progress:ProgressStore) {
    fun skins(choice:Int,onChoose:(Int)->Unit,onBack:()->Unit):View =
        PilotSelectionScreen(host,progress).create(choice,onChoose,onBack)

    fun shop(onBuy:(Int)->Unit,onClaim:()->Unit,onBack:()->Unit):View =
        ShopScreen(host,progress).create(onBuy,onClaim,onBack)

    fun levels(world:Int,onWorld:(Int)->Unit,onPlay:(Int,Long)->Unit,onBack:()->Unit):View =
        LevelMapScreen(host,progress).create(world,onWorld,onPlay,onBack)
}
