package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable

/** Native glass/wood Pause panel inspired by the game's result screens.
 * Independent real Android buttons and selected pilot stay above the art. */
internal class PauseFantasyPanelDrawable:Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val petal=Path()
    private var alphaValue=255
    private var filter:ColorFilter?=null
    override fun draw(c:Canvas){
        if(bounds.isEmpty)return
        val saved=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/420f,bounds.height()/590f)
        p.reset();p.isAntiAlias=true;p.alpha=alphaValue;p.colorFilter=filter
        p.color=0x9907203e.toInt()
        c.drawRoundRect(2f,11f,418f,589f,40f,40f,p)
        p.shader=LinearGradient(0f,0f,0f,576f,
            intArrayOf(0xff75dcff.toInt(),0xff1376d3.toInt(),
                0xff09346e.toInt(),0xff05172e.toInt()),
            null,Shader.TileMode.CLAMP)
        c.drawRoundRect(0f,0f,420f,573f,37f,37f,p);p.shader=null
        p.style=Paint.Style.STROKE;p.strokeWidth=7f
        p.color=0xffffd479.toInt()
        c.drawRoundRect(5f,6f,415f,566f,34f,34f,p)
        p.strokeWidth=3f;p.color=0xffe3fbff.toInt()
        c.drawRoundRect(12f,13f,408f,559f,30f,30f,p)
        p.style=Paint.Style.FILL
        // A few subtle stars away from live text.
        for(i in 0..7){
            val x=32f+(i*53%356)
            val y=42f+(i*79%445)
            p.color=0xaaffecb2.toInt()
            p.strokeWidth=3f;p.style=Paint.Style.STROKE
            c.drawLine(x-5f,y,x+5f,y,p)
            c.drawLine(x,y-5f,x,y+5f,p)
        }
        p.style=Paint.Style.FILL
        // Tiny green owner-style corner leaves.
        for(right in 0..1){
            val cp=c.save()
            if(right==1){c.translate(420f,0f);c.scale(-1f,1f)}
            for(i in 0..2){
                val x=24f+i*11f
                val y=557f-i*12f
                petal.reset()
                petal.moveTo(x,y)
                petal.cubicTo(x+3f,y-36f,x+25f,y-40f,x+37f,y-35f)
                petal.cubicTo(x+38f,y-8f,x+15f,y+4f,x,y)
                petal.close()
                p.color=if(i%2==0)0xff61bd29.toInt() else 0xff17864a.toInt()
                c.drawPath(petal,p)
            }
            c.restoreToCount(cp)
        }
        c.restoreToCount(saved)
    }
    override fun setAlpha(alpha:Int){alphaValue=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(filter:ColorFilter?){this.filter=filter;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}

/** Glowing flight-halo behind the actual chosen Bopi portrait. */
internal class PauseHeroFrameDrawable:Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var alphaValue=255
    private var filter:ColorFilter?=null
    override fun draw(c:Canvas){
        if(bounds.isEmpty)return
        val saved=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/420f,bounds.height()/168f)
        p.reset();p.isAntiAlias=true;p.alpha=alphaValue;p.colorFilter=filter
        p.shader=RadialGradient(210f,82f,167f,
            intArrayOf(0xfffff6bb.toInt(),0x77cbf8ff,
                0x002c76cc),null,Shader.TileMode.CLAMP)
        c.drawOval(52f,3f,368f,166f,p);p.shader=null
        p.color=0x9903275c.toInt()
        c.drawOval(129f,14f,291f,170f,p)
        p.shader=LinearGradient(0f,15f,0f,165f,
            0xfffff9c1.toInt(),0xffffaa2e.toInt(),Shader.TileMode.CLAMP)
        c.drawOval(138f,14f,283f,158f,p);p.shader=null
        p.color=0xff784010.toInt()
        p.style=Paint.Style.STROKE;p.strokeWidth=5f
        c.drawOval(138f,14f,283f,158f,p)
        p.style=Paint.Style.FILL
        c.restoreToCount(saved)
    }
    override fun setAlpha(alpha:Int){alphaValue=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(filter:ColorFilter?){this.filter=filter;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
