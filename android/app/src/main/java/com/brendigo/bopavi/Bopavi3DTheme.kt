package com.brendigo.bopavi

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable

/**
 * One local embossed surface system for Home, navigation, menus, cards and
 * result actions. The legacy screen gradients remain for flat background art;
 * genuine interactive Android buttons share the owner's gloss and 3D lip.
 */
internal object Bopavi3DTheme {
    enum class Finish { GOLD, SKY, GREEN, VIOLET, CREAM, STONE }

    fun panel(finish:Finish):Drawable = when(finish) {
        Finish.GOLD -> HomeEnamelDrawable(
            0xfffff69b.toInt(),0xffffb21b.toInt(),
            0xffdc7610.toInt(),0xfffff6d4.toInt())
        Finish.SKY -> HomeEnamelDrawable(
            0xff91f1ff.toInt(),0xff168fe7.toInt(),
            0xff0649a5.toInt(),0xffd2faff.toInt())
        Finish.GREEN -> HomeEnamelDrawable(
            0xffa6ff91.toInt(),0xff22ce56.toInt(),
            0xff08702c.toInt(),0xffe6ffd5.toInt())
        Finish.VIOLET -> HomeEnamelDrawable(
            0xfff39aff.toInt(),0xff9b31ef.toInt(),
            0xff4a198c.toInt(),0xffffddff.toInt())
        Finish.CREAM -> HomeEnamelDrawable(
            0xfffffff7.toInt(),0xffffe6b8.toInt(),
            0xffc6915c.toInt(),0xfffff4dd.toInt())
        Finish.STONE -> HomeEnamelDrawable(
            0xffe4edf9.toInt(),0xffa3b5cc.toInt(),
            0xff466181.toInt(),0xfff4f8ff.toInt())
    }

    fun button(finish:Finish):RippleDrawable =
        RippleDrawable(ColorStateList.valueOf(0x55ffffff),panel(finish),null)

    /** World-specific palette can retain its own hues without flat buttons. */
    fun custom(top:Int,bottom:Int,border:Int):RippleDrawable =
        RippleDrawable(ColorStateList.valueOf(0x55ffffff),
            HomeEnamelDrawable(top,top,bottom,border),null)
}
