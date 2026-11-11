package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.sin

/**
 * Purely decorative drawing behind the real Settings controls.
 * The canvas uses the owner's 739x1007 frame space; it contains no text,
 * prices, screenshots, buttons or hit targets.
 */
internal class SettingsWoodPanelDrawable : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val petal = Path()
    private var opacity = 255
    private var filter:ColorFilter? = null

    private fun color(argb:Int) {
        p.reset()
        p.isAntiAlias = true
        p.color = argb
        p.alpha = opacity
        p.colorFilter = filter
    }
    private fun round(c:Canvas, l:Float, t:Float, r:Float, b:Float, radius:Float,
        argb:Int) {
        color(argb)
        c.drawRoundRect(l,t,r,b,radius,radius,p)
    }
    private fun gradient(c:Canvas,l:Float,t:Float,r:Float,b:Float,radius:Float,
        top:Int,bottom:Int) {
        color(Color.WHITE)
        p.shader = LinearGradient(l,t,l,b,top,bottom,Shader.TileMode.CLAMP)
        c.drawRoundRect(l,t,r,b,radius,radius,p)
        p.shader = null
    }
    private fun leaf(c:Canvas,x:Float,y:Float,angle:Float,scale:Float) {
        val saved=c.save()
        c.translate(x,y);c.rotate(angle);c.scale(scale,scale)
        petal.reset()
        petal.moveTo(0f,0f)
        petal.cubicTo(-23f,-18f,-34f,-61f,-4f,-74f)
        petal.cubicTo(26f,-61f,30f,-21f,0f,0f)
        petal.close()
        color(0xff17582b.toInt());c.drawPath(petal,p)
        color(Color.WHITE)
        p.shader=LinearGradient(-18f,-69f,22f,-9f,
            0xffa9ed4e.toInt(),0xff27913b.toInt(),Shader.TileMode.CLAMP)
        c.drawPath(petal,p);p.shader=null
        color(0xffe2ff8a.toInt())
        p.style=Paint.Style.STROKE;p.strokeWidth=2.3f
        c.drawLine(0f,-5f,-4f,-65f,p)
        p.style=Paint.Style.FILL
        c.restoreToCount(saved)
    }
    private fun daisy(c:Canvas,x:Float,y:Float,scale:Float) {
        for(i in 0 until 7) {
            val radians=i*Math.PI*2/7
            val px=x+cos(radians).toFloat()*13f*scale
            val py=y+sin(radians).toFloat()*13f*scale
            color(0xfff6fff6.toInt())
            c.drawOval(px-8f*scale,py-12f*scale,
                px+8f*scale,py+12f*scale,p)
        }
        color(0xffffb72c.toInt());c.drawCircle(x,y,10f*scale,p)
        color(0xfffff2a5.toInt());c.drawCircle(x-2f*scale,y-3f*scale,4f*scale,p)
    }
    override fun draw(c:Canvas) {
        if(bounds.isEmpty)return
        val saved=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/739f,bounds.height()/1007f)

        // Raised dark outer wood, shallow bevel and warm carved inner rim.
        round(c,1f,19f,738f,1006f,54f,0x90082132.toInt())
        round(c,0f,8f,739f,995f,52f,0xff57270f.toInt())
        gradient(c,3f,1f,736f,986f,50f,
            0xffbc7536.toInt(),0xff693113.toInt())
        round(c,12f,10f,727f,979f,43f,0xff8d451e.toInt())
        gradient(c,19f,15f,720f,970f,39f,
            0xffffe9b6.toInt(),0xffbd7f45.toInt())
        round(c,27f,24f,712f,963f,34f,0xfff8e1bc.toInt())
        gradient(c,29f,27f,710f,961f,32f,
            0xffffffee.toInt(),0xfff4dfb9.toInt())

        // Vertical glowing edge highlights and etched wood grooves.
        color(0x99ffffff.toInt())
        p.style=Paint.Style.STROKE;p.strokeWidth=2.5f
        c.drawRoundRect(30f,28f,709f,959f,31f,31f,p)
        p.style=Paint.Style.FILL
        for(i in 0..9) {
            val y=76f+i*86f
            round(c,8f,y,15f,y+34f,3f,0x88502312.toInt())
            round(c,722f,y+9f,730f,y+39f,3f,0x88502312.toInt())
            round(c,12f,y+2f,14f,y+31f,1f,0x88ffd98a.toInt())
            round(c,724f,y+10f,726f,y+37f,1f,0x88ffd98a.toInt())
        }
        // Lower 3D wooden lip, below the Restore/Home action.
        gradient(c,23f,956f,715f,986f,16f,
            0xffffd485.toInt(),0xff8c4b21.toInt())
        round(c,35f,964f,703f,973f,4f,0x66ffedc2.toInt())

        // Decorative botanic corners remain behind the native bottom button.
        for(i in 0..5) {
            leaf(c,42f+i*18f,990f-i*4f,-70f+i*17f,
                if(i%2==0)1.0f else .76f)
            leaf(c,697f-i*18f,990f-i*4f,70f-i*17f,
                if(i%2==0)1.0f else .76f)
        }
        daisy(c,48f,944f,1.05f)
        daisy(c,688f,946f,1.05f)
        c.restoreToCount(saved)
    }
    override fun setAlpha(alpha:Int){opacity=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}

/** Illustrated gold SETTINGS sign, with wings and a true raised orange lip. */
internal class SettingsGoldTitleDrawable : Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    private var opacity=255
    private var filter:ColorFilter?=null

    override fun draw(c:Canvas) {
        if(bounds.isEmpty)return
        val saved=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/577f,bounds.height()/101f)
        fun fill(color:Int) {
            p.reset();p.isAntiAlias=true
            p.color=color;p.alpha=opacity;p.colorFilter=filter
        }
        // Individual white feathers, dark navy edges and small shadows.
        for(mirror in listOf(false,true)) {
            val s=c.save()
            if(mirror){c.translate(577f,0f);c.scale(-1f,1f)}
            for(i in 0..2){
                path.reset()
                val y=32f+i*18f
                path.moveTo(73f,y)
                path.cubicTo(37f,y-27f,10f,y-17f,7f,y-3f)
                path.cubicTo(12f,y+25f,44f,y+24f,72f,y+21f)
                path.close()
                fill(0xff123669.toInt());c.drawPath(path,p)
                c.save();c.translate(2f,-3f)
                fill(Color.WHITE);c.drawPath(path,p)
                c.restore()
            }
            c.restoreToCount(s)
        }
        fill(0xff693113.toInt())
        c.drawRoundRect(47f,11f,530f,101f,43f,43f,p)
        fill(0xffec7912.toInt())
        c.drawRoundRect(48f,3f,529f,94f,41f,41f,p)
        fill(Color.WHITE)
        p.shader=LinearGradient(0f,8f,0f,91f,
            intArrayOf(0xfffff8a7.toInt(),0xffffce24.toInt(),
                0xffff960c.toInt()),null,Shader.TileMode.CLAMP)
        c.drawRoundRect(53f,6f,524f,89f,37f,37f,p)
        p.shader=null
        fill(0xaafffff5.toInt())
        p.style=Paint.Style.STROKE;p.strokeWidth=3f
        c.drawArc(62f,11f,514f,67f,192f,153f,false,p)
        p.style=Paint.Style.FILL
        c.restoreToCount(saved)
    }
    override fun setAlpha(alpha:Int){opacity=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
