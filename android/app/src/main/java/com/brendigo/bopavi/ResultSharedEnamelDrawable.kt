package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Consistent glass-and-parchment result card for actual persisted run stats.
 * This drawable paints no numbers, icons, or text: accessible TextViews on top
 * remain truthful for both endless and finite results.
 */
internal class ResultRowEnamelDrawable:Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var alphaValue=255
    private var filter:ColorFilter?=null
    override fun draw(c:Canvas){
        if(bounds.isEmpty)return
        val save=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/640f,bounds.height()/88f)
        p.reset();p.isAntiAlias=true;p.alpha=alphaValue;p.colorFilter=filter
        p.color=0x66522c13
        c.drawRoundRect(2f,5f,638f,88f,35f,35f,p)
        p.shader=LinearGradient(0f,0f,0f,79f,
            intArrayOf(0xffffe9c6.toInt(),0xfffff9e7.toInt(),
                0xffe9bd87.toInt()),null,Shader.TileMode.CLAMP)
        c.drawRoundRect(0f,0f,640f,80f,35f,35f,p)
        p.shader=null
        p.color=0xfffff9ed.toInt()
        c.drawRoundRect(7f,5f,633f,74f,31f,31f,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=2.5f
        p.color=0xffd7b58b.toInt()
        c.drawRoundRect(7f,5f,633f,74f,31f,31f,p)
        p.style=Paint.Style.FILL
        p.color=0x77ffffff
        c.drawRoundRect(27f,8f,602f,13f,2f,2f,p)
        c.restoreToCount(save)
    }
    override fun setAlpha(alpha:Int){alphaValue=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(colorFilter:ColorFilter?){filter=colorFilter;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}

/** Wood-rimmed reward backing matches Game Over's warm result board. */
internal class LevelCompleteRewardPlateDrawable:Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var alphaValue=255
    private var filter:ColorFilter?=null
    override fun draw(c:Canvas){
        if(bounds.isEmpty)return
        val save=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/741f,bounds.height()/278f)
        p.reset();p.isAntiAlias=true;p.alpha=alphaValue;p.colorFilter=filter
        p.color=0x880a254b.toInt()
        c.drawRoundRect(4f,11f,737f,278f,38f,38f,p)
        p.shader=LinearGradient(0f,0f,0f,273f,
            0xffffd284.toInt(),0xff79401c.toInt(),Shader.TileMode.CLAMP)
        c.drawRoundRect(0f,0f,741f,268f,37f,37f,p);p.shader=null
        p.shader=LinearGradient(0f,13f,0f,249f,
            intArrayOf(0xffffffff.toInt(),0xfffff3d7.toInt(),
                0xffffdeb0.toInt()),null,Shader.TileMode.CLAMP)
        c.drawRoundRect(12f,12f,729f,253f,29f,29f,p);p.shader=null
        p.style=Paint.Style.STROKE;p.color=0xffae622d.toInt()
        p.strokeWidth=4f
        c.drawRoundRect(12f,12f,729f,253f,29f,29f,p)
        p.style=Paint.Style.FILL
        p.shader=RadialGradient(368f,192f,180f,
            0x77fff1a3,0x00ffffff,Shader.TileMode.CLAMP)
        c.drawCircle(368f,192f,170f,p);p.shader=null
        c.restoreToCount(save)
    }
    override fun setAlpha(alpha:Int){alphaValue=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(colorFilter:ColorFilter?){filter=colorFilter;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
