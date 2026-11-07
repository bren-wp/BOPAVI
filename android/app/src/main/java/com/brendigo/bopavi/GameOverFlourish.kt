package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.view.View

/** Decorative wood beneath the raised Game/Over heading — never a button. */
internal class GameOverTitlePlank(context:Context):View(context){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val wood=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val checkpoint=canvas.save()
        canvas.scale(width/545f,height/135f)
        wood.reset()
        wood.moveTo(39f,26f)
        wood.quadTo(273f,5f,503f,27f)
        wood.lineTo(486f,96f)
        wood.quadTo(273f,127f,51f,97f)
        wood.close()
        p.style=Paint.Style.FILL
        p.color=0xff48200f.toInt()
        val drop=canvas.save()
        canvas.translate(0f,9f)
        canvas.drawPath(wood,p)
        canvas.restoreToCount(drop)
        p.shader=LinearGradient(0f,11f,0f,132f,
            intArrayOf(0xffd99942.toInt(),0xffa8612b.toInt(),
                0xff6d3718.toInt()),null,Shader.TileMode.CLAMP)
        canvas.drawPath(wood,p)
        p.shader=null
        p.style=Paint.Style.STROKE
        p.strokeWidth=7f
        p.color=0xff4a250f.toInt()
        canvas.drawPath(wood,p)
        p.strokeWidth=3f
        p.color=0xffffc67c.toInt()
        canvas.drawArc(RectF(43f,20f,499f,100f),196f,146f,false,p)
        p.color=0x886f3a15.toInt()
        p.strokeWidth=3f
        for(i in 0..3){
            val y=51f+i*12f
            canvas.drawLine(91f,y,454f,y-5f,p)
        }
        p.shader=null
        p.style=Paint.Style.FILL
        // Two leaf clusters visible beside the owner's wooden lettering.
        for(right in 0..1){
            val save=canvas.save()
            if(right==1){canvas.translate(545f,0f);canvas.scale(-1f,1f)}
            for(i in 0..3){
                val leaf=Path().apply {
                    moveTo(65f,70f)
                    quadTo(16f+i*13f,9f+i*14f,4f+i*8f,30f+i*11f)
                    quadTo(18f+i*8f,86f+i*7f,65f,70f)
                    close()
                }
                p.shader=LinearGradient(0f,20f,75f,105f,
                    0xffb0ef49.toInt(),0xff277a26.toInt(),Shader.TileMode.CLAMP)
                canvas.drawPath(leaf,p)
                p.shader=null
                p.style=Paint.Style.STROKE
                p.strokeWidth=3f
                p.color=0xff1d5b24.toInt()
                canvas.drawPath(leaf,p)
                p.style=Paint.Style.FILL
            }
            canvas.restoreToCount(save)
        }
        canvas.restoreToCount(checkpoint)
    }
}

/** Lightweight local blue feather accents around the Game Over character. */
internal class GameOverFeathers(context:Context):View(context){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val feather=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val saved=canvas.save()
        canvas.scale(width/690f,height/508f)
        val x=floatArrayOf(26f,98f,598f,640f,76f,614f)
        val y=floatArrayOf(232f,390f,225f,383f,108f,134f)
        val tilt=floatArrayOf(-36f,52f,30f,-48f,20f,-22f)
        for(i in x.indices){
            val cp=canvas.save()
            canvas.translate(x[i],y[i])
            canvas.rotate(tilt[i])
            feather.reset()
            feather.moveTo(0f,-25f)
            feather.cubicTo(18f,-15f,20f,9f,0f,32f)
            feather.cubicTo(-17f,11f,-16f,-15f,0f,-25f)
            feather.close()
            p.style=Paint.Style.FILL
            p.shader=LinearGradient(-15f,-18f,15f,27f,
                0xff9befff.toInt(),0xff0873d2.toInt(),Shader.TileMode.CLAMP)
            canvas.drawPath(feather,p)
            p.shader=null
            p.style=Paint.Style.STROKE
            p.strokeWidth=2.5f
            p.color=0xff124b9e.toInt()
            canvas.drawPath(feather,p)
            p.strokeWidth=1.7f
            p.color=Color.WHITE
            canvas.drawLine(0f,-21f,0f,23f,p)
            canvas.restoreToCount(cp)
        }
        canvas.restoreToCount(saved)
    }
}


/**
 * Actual wood-and-ivory score plate matching the owner's three result rows.
 * Values are NOT painted here: the independent accessible TextViews in
 * GameOverScreen remain authoritative and fully dynamic.
 */
internal class GameOverScoreFrameDrawable:android.graphics.drawable.Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var opacity=255
    private var tint:android.graphics.ColorFilter?=null
    override fun draw(c:Canvas){
        if(bounds.isEmpty)return
        val saved=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/695f,bounds.height()/310f)
        p.reset();p.isAntiAlias=true
        p.alpha=opacity;p.colorFilter=tint
        p.color=0xff603015.toInt()
        c.drawRoundRect(2f,9f,693f,309f,35f,35f,p)
        p.color=0xffab672d.toInt()
        c.drawRoundRect(0f,3f,695f,301f,34f,34f,p)
        p.shader=LinearGradient(0f,7f,0f,296f,
            intArrayOf(0xffffedd0.toInt(),0xfffff9e7.toInt(),
                0xffe9bd87.toInt()),null,Shader.TileMode.CLAMP)
        c.drawRoundRect(10f,10f,685f,291f,27f,27f,p)
        p.shader=null
        p.style=Paint.Style.STROKE;p.strokeWidth=3f
        p.color=0xffffdca9.toInt()
        c.drawRoundRect(13f,13f,682f,288f,25f,25f,p)
        p.style=Paint.Style.FILL
        for(i in 0..2){
            val y=22f+i*91f
            p.color=if(i%2==0)0x44b57f47 else 0x34e7c098
            c.drawRoundRect(23f,y,672f,y+83f,29f,29f,p)
        }
        c.restoreToCount(saved)
    }
    override fun setAlpha(value:Int){opacity=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(filter:android.graphics.ColorFilter?){tint=filter;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=android.graphics.PixelFormat.TRANSLUCENT
}
