package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View

/**
 * Purely decorative native Skins reference artwork. Every character choice,
 * wallet counter, navigation control and description remains a real Android
 * view above it. Geometry is expressed on the owner's 941 x 1672 stage.
 */
internal class SkinsReferenceDecor(context:Context):View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val outline=Path()

    init {
        importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable=false
        isFocusable=false
    }
    private fun rounded(c:Canvas,l:Float,t:Float,r:Float,b:Float,corner:Float,
                        top:Int,bottom:Int){
        p.reset();p.isAntiAlias=true
        p.shader=LinearGradient(l,t,l,b,top,bottom,Shader.TileMode.CLAMP)
        c.drawRoundRect(l,t,r,b,corner,corner,p)
        p.shader=null
    }
    private fun leaf(c:Canvas,x:Float,y:Float,angle:Float,scale:Float){
        val saved=c.save()
        c.translate(x,y);c.rotate(angle);c.scale(scale,scale)
        outline.reset()
        outline.moveTo(0f,0f)
        outline.cubicTo(-31f,-25f,-28f,-63f,-5f,-73f)
        outline.cubicTo(26f,-55f,27f,-23f,0f,0f)
        outline.close()
        p.reset();p.isAntiAlias=true
        p.shader=LinearGradient(-15f,-62f,16f,-8f,
            0xffb5ed3f.toInt(),0xff278230.toInt(),Shader.TileMode.CLAMP)
        c.drawPath(outline,p)
        p.shader=null
        p.style=Paint.Style.STROKE;p.strokeWidth=3f
        p.color=0xff175522.toInt()
        c.drawPath(outline,p)
        p.strokeWidth=2f
        p.color=0xffc5fa72.toInt()
        c.drawLine(0f,-8f,-4f,-60f,p)
        p.style=Paint.Style.FILL
        c.restoreToCount(saved)
    }
    override fun onDraw(c:Canvas){
        if(width<=0 || height<=0)return
        val save=c.save()
        c.scale(width/941f,height/1672f)
        // Two suspended ropes with braided amber highlights.
        for(x in floatArrayOf(304f,617f)){
            p.reset();p.isAntiAlias=true
            p.color=0xff44230d.toInt();p.strokeWidth=18f
            c.drawLine(x,80f,x,190f,p)
            p.color=0xffd38c3b.toInt();p.strokeWidth=11f
            c.drawLine(x-2f,79f,x-2f,185f,p)
            p.color=0xfff4c776.toInt();p.strokeWidth=3f
            for(i in 0..5){
                val y=84f+i*18f
                c.drawLine(x-7f,y,x+5f,y+7f,p)
            }
        }
        // Thick, curved wood plaque behind the live selected hero label.
        outline.reset()
        outline.moveTo(205f,206f)
        outline.cubicTo(212f,157f,340f,143f,472f,143f)
        outline.cubicTo(618f,142f,710f,163f,733f,205f)
        outline.lineTo(720f,284f)
        outline.cubicTo(598f,316f,342f,317f,218f,281f)
        outline.close()
        p.reset();p.isAntiAlias=true
        val depth=c.save();c.translate(0f,13f)
        p.color=0xff45200d.toInt();c.drawPath(outline,p)
        c.restoreToCount(depth)
        p.shader=LinearGradient(0f,149f,0f,311f,
            intArrayOf(0xffffcd7c.toInt(),0xffb96c2e.toInt(),
                0xff733615.toInt()),null,Shader.TileMode.CLAMP)
        c.drawPath(outline,p);p.shader=null
        p.style=Paint.Style.STROKE;p.strokeWidth=7f
        p.color=0xff66300f.toInt();c.drawPath(outline,p)
        p.strokeWidth=3f;p.color=0xfffbe0a8.toInt()
        c.drawArc(232f,167f,704f,266f,190f,158f,false,p)
        p.style=Paint.Style.FILL
        // Fine horizontal wood grain stays behind lettering.
        p.color=0x44954816
        for(i in 0..4)c.drawRoundRect(248f,192f+i*18f,
            695f,194f+i*18f,2f,2f,p)
        rounded(c,347f,271f,594f,325f,24f,
            0xffae6631.toInt(),0xff70330f.toInt())
        p.style=Paint.Style.STROKE;p.strokeWidth=5f
        p.color=0xff59280d.toInt()
        c.drawRoundRect(347f,271f,594f,325f,24f,24f,p)
        p.style=Paint.Style.FILL
        for(i in 0..4){
            leaf(c,220f+i*11f,294f,-55f+i*17f,.95f)
            leaf(c,723f-i*11f,295f,55f-i*17f,.95f)
        }

        // The owner's podium is a floating cut-stone dais, NOT a flat
        // rectangular card. Keep all of its relief behind the native hero,
        // carousel arrows and selectable skin cards.
        p.reset();p.isAntiAlias=true
        p.color=0x88092348.toInt()
        c.drawOval(163f,907f,788f,985f,p)
        outline.reset()
        outline.moveTo(156f,853f)
        outline.cubicTo(194f,825f,718f,824f,786f,856f)
        outline.lineTo(793f,897f)
        outline.lineTo(759f,957f)
        outline.lineTo(696f,980f)
        outline.lineTo(235f,980f)
        outline.lineTo(176f,951f)
        outline.lineTo(147f,893f)
        outline.close()
        p.shader=LinearGradient(177f,833f,759f,978f,
            intArrayOf(0xffe5c7a3.toInt(),0xff97847c.toInt(),
                0xff373f54.toInt()),null,Shader.TileMode.CLAMP)
        c.drawPath(outline,p);p.shader=null
        p.color=0xff564c59.toInt();p.style=Paint.Style.STROKE
        p.strokeWidth=7f;c.drawPath(outline,p);p.style=Paint.Style.FILL
        // Offset stone joints and angled facets read as stacked rock blocks,
        // rather than the previous six identical vertical rounded rectangles.
        for(i in 0..6){
            val x=182f+i*88f
            val topY=854f+(i%2)*6f
            val footY=952f-(i%3)*8f
            outline.reset()
            outline.moveTo(x,topY)
            outline.lineTo(x+77f,topY+6f)
            outline.lineTo(x+67f,footY)
            outline.lineTo(x+18f,footY+12f)
            outline.close()
            p.color=if(i%2==0)0x667b6057 else 0x66566572
            c.drawPath(outline,p)
            p.color=0x66fff4d7;p.strokeWidth=2.5f
            p.style=Paint.Style.STROKE
            c.drawLine(x+7f,topY+6f,x+67f,topY+12f,p)
            p.style=Paint.Style.FILL
        }
        p.color=0xff256c39.toInt()
        c.drawOval(160f,792f,788f,883f,p)
        p.shader=LinearGradient(170f,797f,760f,865f,
            0xffc3f36b.toInt(),0xff278342.toInt(),Shader.TileMode.CLAMP)
        c.drawOval(169f,791f,778f,863f,p);p.shader=null
        p.color=0x99fbffc8.toInt()
        p.style=Paint.Style.STROKE;p.strokeWidth=4f
        c.drawArc(187f,799f,750f,853f,189f,156f,false,p)
        p.style=Paint.Style.FILL
        p.shader=RadialGradient(472f,821f,275f,
            intArrayOf(0xffffffd9.toInt(),0xffffcf51.toInt(),
                0xffad622c.toInt()),null,Shader.TileMode.CLAMP)
        c.drawOval(252f,801f,690f,864f,p);p.shader=null
        p.style=Paint.Style.STROKE;p.color=0xffffeaa2.toInt()
        p.strokeWidth=6f;c.drawOval(270f,806f,675f,860f,p)
        p.style=Paint.Style.FILL
        for(i in 0..18){
            val x=157f+i*35f
            val y=832f+(i%4)*5f
            p.color=if(i%2==0)0xff3b9c2e.toInt() else 0xff74d04b.toInt()
            c.drawOval(x-10f,y-20f,x+18f,y+5f,p)
        }
        // Small daisies share the cream/gold owner reference palette.
        for(x in floatArrayOf(184f,245f,676f,748f)){
            val y=849f+(x.toInt()%3)*7f
            for(i in 0..5){
                val theta=i*kotlin.math.PI/3.0
                val px=x+kotlin.math.cos(theta).toFloat()*12f
                val py=y+kotlin.math.sin(theta).toFloat()*12f
                p.color=0xfffffefa.toInt()
                c.drawCircle(px,py,7f,p)
            }
            p.color=0xffffc12c.toInt();c.drawCircle(x,y,8f,p)
        }
        c.restoreToCount(save)
    }
}

/** Four real touch cards: selected gold, available blue, locked purple. */
internal class SkinsCardEnamelDrawable(
    private val selected:Boolean,private val locked:Boolean=false
):Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var alphaValue=255
    private var tint:ColorFilter?=null
    override fun draw(c:Canvas){
        if(bounds.isEmpty)return
        val save=c.save()
        c.translate(bounds.left.toFloat(),bounds.top.toFloat())
        c.scale(bounds.width()/207f,bounds.height()/307f)
        p.reset();p.isAntiAlias=true;p.alpha=alphaValue;p.colorFilter=tint
        p.color=0x77071d48
        c.drawRoundRect(1f,15f,206f,307f,28f,28f,p)
        p.shader=LinearGradient(0f,4f,0f,296f,
            if(selected)intArrayOf(0xffffed94.toInt(),0xffffba21.toInt(),0xffb46916.toInt())
            else if(locked)intArrayOf(0xffb7a9fa.toInt(),0xff594895.toInt(),0xff1d1a51.toInt())
            else intArrayOf(0xffa6eaff.toInt(),0xff1b84d8.toInt(),0xff073a86.toInt()),
            null,Shader.TileMode.CLAMP)
        c.drawRoundRect(0f,0f,207f,297f,27f,27f,p);p.shader=null
        p.color=if(selected)0xfffff8da.toInt()
            else if(locked)0xff504481.toInt() else 0xff2f6ebc.toInt()
        c.drawRoundRect(8f,8f,199f,287f,23f,23f,p)
        p.shader=LinearGradient(0f,19f,0f,274f,
            if(selected)0xffffffff.toInt()
                else if(locked)0xff846bc4.toInt() else 0xff75c8ff.toInt(),
            if(selected)0xffffdf99.toInt()
                else if(locked)0xff241c5c.toInt() else 0xff0e458f.toInt(),
            Shader.TileMode.CLAMP)
        c.drawRoundRect(13f,13f,194f,274f,20f,20f,p);p.shader=null
        p.style=Paint.Style.STROKE
        p.strokeWidth=3f;p.color=0xffe7f9ff.toInt()
        c.drawRoundRect(13f,13f,194f,274f,20f,20f,p)
        p.strokeWidth=3f
        p.color=if(selected)0xffa66724.toInt()
            else if(locked)0xff262154.toInt() else 0xff164b8b.toInt()
        c.drawRoundRect(1f,1f,206f,296f,26f,26f,p)
        p.style=Paint.Style.FILL
        p.color=0x88ffffff.toInt()
        c.drawRoundRect(24f,19f,181f,26f,4f,4f,p)
        c.restoreToCount(save)
    }
    override fun setAlpha(alpha:Int){alphaValue=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(filter:ColorFilter?){tint=filter;invalidateSelf()}
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
