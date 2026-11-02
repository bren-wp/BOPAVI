package com.brendigo.bopavi

/**
 * Pause has no owner-supplied raster reference yet. These measurable native
 * constraints preserve readable text and full-size, real Android hit targets
 * on compact devices and with enlarged system fonts.
 */
internal object PauseModalLayout {
    const val minTouchDp=48

    fun compact(screenHeightDp:Int,fontScale:Float):Boolean =
        screenHeightDp<690 || fontScale>=1.3f

    fun heroHeightDp(compact:Boolean):Int=if(compact)112 else 168
    fun headerHeightDp(compact:Boolean):Int=if(compact)55 else 70
    fun buttonHeightDp(compact:Boolean):Int=if(compact)55 else 63
    fun dialogHeightDp(compact:Boolean):Int=if(compact)480 else 590
}
