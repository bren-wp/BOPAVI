package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View

/**
 * Illustrative plaque surface for the three native, genuinely selectable worlds.
 * Content, earned stars, locks and click handling remain Android Views. This
 * paints NO static text, stars, stage numbers or unearned progress.
 */
internal class LevelMapPlaqueDrawable(private val style:Int):Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var requestedAlpha=255
    private var tintFilter:ColorFilter?=null
    private fun ink(color:Int) {
        p.reset()
        p.isAntiAlias=true
        p.color=color
        p.alpha=(Color.alpha(color)*requestedAlpha/255).coerceIn(0,255)
        p.colorFilter=tintFilter
    }
    override fun draw(canvas:Canvas) {
        if(bounds.isEmpty)return
        val saved=canvas.save()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        canvas.scale(bounds.width()/342f,bounds.height()/116f)
        val top=when(style%3){0->0xffffd78c.toInt();1->0xff88e8ff.toInt();else->0xffffa345.toInt()}
        val bottom=when(style%3){0->0xff8c411b.toInt();1->0xff273767.toInt();else->0xff6b1d24.toInt()}
        val outline=when(style%3){0->0xff3b1e12.toInt();1->0xff141a3f.toInt();else->0xff240d15.toInt()}
        ink(0x99091b33.toInt())
        canvas.drawRoundRect(4f,9f,340f,114f,33f,33f,p)
        ink(outline)
        canvas.drawRoundRect(3f,2f,338f,104f,31f,31f,p)
        ink(0xffc78737.toInt())
        canvas.drawRoundRect(9f,4f,333f,96f,27f,27f,p)
        ink(top)
        p.shader=LinearGradient(0f,7f,0f,92f,top,bottom,Shader.TileMode.CLAMP)
        canvas.drawRoundRect(14f,9f,329f,89f,23f,23f,p)
        p.shader=null
        ink(0x77ffffff)
        canvas.drawRoundRect(24f,15f,318f,22f,5f,5f,p)
        // The warm wood, turquoise ruins and lava each have subtle physical
        // grain/cracks, all behind the readable, live world name and star count.
        for(i in 0..5) {
            val startX=29f+i*54f
            p.style=Paint.Style.STROKE
            p.strokeWidth=if(style%3==2)2.7f else 1.5f
            ink(if(style%3==0)0x66713b1b else if(style%3==1)0x668feaff else 0x99ff9b33.toInt())
            p.style=Paint.Style.STROKE
            canvas.drawLine(startX,27f+(i%3)*5f,startX+20f,
                29f+(i%3)*5f,p)
        }
        p.style=Paint.Style.FILL
        ink(0xfff8e7b1.toInt())
        canvas.drawCircle(31f,51f,4f,p)
        canvas.drawCircle(311f,51f,4f,p)
        ink(0xffaf6619.toInt())
        canvas.drawCircle(31f,51f,2f,p)
        canvas.drawCircle(311f,51f,2f,p)
        canvas.restoreToCount(saved)
    }
    override fun setAlpha(alpha:Int){requestedAlpha=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(colorFilter:ColorFilter?){tintFilter=colorFilter;invalidateSelf()}
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}

/** White feathered wings behind the real golden PLAY LEVEL button. */
internal class LevelMapPlayWings(context:Context):View(context){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val feather=Path()
    init { importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO }
    private fun drawWing(c:Canvas,mirror:Boolean) {
        c.save()
        if(mirror){c.translate(760f,0f);c.scale(-1f,1f)}
        feather.reset()
        feather.moveTo(116f,49f)
        feather.cubicTo(84f,10f,40f,5f,18f,23f)
        feather.cubicTo(3f,40f,19f,76f,53f,95f)
        feather.cubicTo(75f,100f,104f,84f,116f,65f)
        feather.close()
        p.style=Paint.Style.FILL
        p.color=0xff072e6a.toInt()
        c.drawPath(feather,p)
        p.color=Color.WHITE
        c.save();c.translate(4f,-4f);c.drawPath(feather,p);c.restore()
        for(i in 0..3){
            p.color=if(i%2==0)0xfff8fdff.toInt() else 0xffd6edff.toInt()
            val x=20f+i*20f
            c.drawOval(x,42f+i*4f,x+39f,85f+i*2f,p)
        }
        p.color=0xff7dccff.toInt()
        p.style=Paint.Style.STROKE;p.strokeWidth=2f
        c.drawArc(22f,13f,108f,83f,180f,135f,false,p)
        p.style=Paint.Style.FILL
        c.restore()
    }
    override fun onDraw(canvas:Canvas) {
        val saved=canvas.save()
        canvas.scale(width/760f,height/132f)
        drawWing(canvas,false)
        drawWing(canvas,true)
        canvas.restoreToCount(saved)
    }
}
