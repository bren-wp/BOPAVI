package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

/**
 * Density-independent, circular, raised 3D medal for Home shortcuts.
 *
 * Every crown, podium and speaker is a separate vector glyph drawn above this
 * reusable surface. Draws in a fixed 100-unit design space, then scales to the
 * View's actual square bounds, avoiding dp-dependent oval distortion.
 */
internal class HomeMedalDrawable:Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var requestedAlpha=255
    private var filter:ColorFilter?=null
    private var pressed=false
    private var enabled=true

    override fun isStateful():Boolean=true
    override fun onStateChange(states:IntArray):Boolean {
        val nextPressed=android.R.attr.state_pressed in states
        val nextEnabled=android.R.attr.state_enabled in states
        if(pressed==nextPressed && enabled==nextEnabled)return false
        pressed=nextPressed
        enabled=nextEnabled
        invalidateSelf()
        return true
    }

    private fun fill(c:Canvas,color:Int,rect:RectF,shader:Shader?=null) {
        p.reset()
        p.isAntiAlias=true
        p.color=Color.argb(Color.alpha(color)*requestedAlpha/255,
            Color.red(color),Color.green(color),Color.blue(color))
        p.colorFilter=filter
        p.shader=shader
        c.drawOval(rect,p)
        p.shader=null
    }

    override fun draw(canvas:Canvas) {
        if(bounds.isEmpty)return
        val save=canvas.save()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        canvas.scale(bounds.width()/100f,bounds.height()/100f)
        if(!enabled)canvas.saveLayerAlpha(0f,0f,100f,100f,150)
        val shift=if(pressed)3f else 0f
        // Navy cast shadow and wide chocolate-gold dimensional sidewall.
        fill(canvas,0x88001a42.toInt(),RectF(3f,11f,97f,100f))
        fill(canvas,0xff09224c.toInt(),RectF(1f,5f+shift,99f,95f))
        fill(canvas,0xff8d410d.toInt(),RectF(4f,6f+shift,96f,92f))
        fill(canvas,Color.WHITE,RectF(5f,1f+shift,95f,89f),
            LinearGradient(0f,0f,0f,100f,
                intArrayOf(0xfffff8c0.toInt(),0xffffbf36.toInt(),
                    0xffaa590f.toInt()),null,Shader.TileMode.CLAMP))
        // The owner's three shortcuts have a dark glossy navy enamel face,
        // not the sky-blue fill inherited from a level-selection button.
        // Keep a distinct inner bevel and a gold, genuinely raised rim.
        fill(canvas,0xff06214e.toInt(),RectF(11f,9f+shift,89f,83f))
        fill(canvas,Color.WHITE,RectF(14f,10f+shift,86f,79f),
            LinearGradient(0f,8f,0f,84f,
                intArrayOf(0xff284e7b.toInt(),0xff12325e.toInt(),
                    0xff06172f.toInt()),null,Shader.TileMode.CLAMP))
        // Delicate upper-left enamel glare, like the source 3D medallions.
        p.reset()
        p.isAntiAlias=true
        p.color=0x99d8f5ff.toInt()
        p.style=Paint.Style.STROKE
        p.strokeWidth=3.3f
        p.strokeCap=Paint.Cap.ROUND
        canvas.drawArc(RectF(20f,15f+shift,79f,69f+shift),
            204f,105f,false,p)
        p.strokeWidth=2f
        p.color=0x70ffffff
        canvas.drawArc(RectF(24f,17f+shift,78f,70f+shift),
            220f,90f,false,p)
        canvas.restoreToCount(save)
    }

    override fun setAlpha(alpha:Int) {
        requestedAlpha=alpha.coerceIn(0,255)
        invalidateSelf()
    }
    override fun setColorFilter(colorFilter:ColorFilter?) {
        filter=colorFilter
        invalidateSelf()
    }
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
