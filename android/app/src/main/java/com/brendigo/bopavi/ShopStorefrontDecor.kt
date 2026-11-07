package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import kotlin.math.sin

/**
 * Native, offline 941x1672 Shop storefront artwork.  No screenshot or fake
 * balance is embedded.  All products, prices and purchases remain live
 * accessible sibling Views in ShopScreen.
 *
 * Wooden roof, alternating awning, lanterns, shelf, purple offer corona and
 * six cream-card sockets follow the owner's three-column Shop composition.
 */
internal class ShopStorefrontDecor(context:Context):View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}

    private fun color(ink:Int) {
        p.reset()
        p.isAntiAlias=true
        p.color=ink
    }
    private fun round(c:Canvas,x:Float,y:Float,w:Float,h:Float,r:Float,ink:Int){
        color(ink);c.drawRoundRect(x,y,x+w,y+h,r,r,p)
    }
    private fun shiny(c:Canvas,x:Float,y:Float,w:Float,h:Float,r:Float,
        top:Int,bottom:Int){
        color(Color.WHITE)
        p.shader=LinearGradient(x,y,x,y+h,top,bottom,Shader.TileMode.CLAMP)
        c.drawRoundRect(x,y,x+w,y+h,r,r,p)
        p.shader=null
    }
    private fun woodenBoard(c:Canvas) {
        // Shadow and three layers of relief stay clear of the upper wallets.
        round(c,205f,138f,535f,183f,57f,0x992a1207.toInt())
        path.reset()
        path.moveTo(220f,175f)
        path.cubicTo(296f,110f,409f,114f,472f,116f)
        path.cubicTo(588f,114f,661f,139f,696f,175f)
        path.lineTo(713f,298f)
        path.cubicTo(600f,325f,314f,330f,215f,303f)
        path.close()
        color(0xff63300e.toInt());c.drawPath(path,p)
        color(Color.WHITE)
        p.shader=LinearGradient(0f,120f,0f,316f,0xfff7b54b.toInt(),
            0xff9b4a1e.toInt(),Shader.TileMode.CLAMP)
        c.save();c.translate(0f,-12f);c.drawPath(path,p);c.restore();p.shader=null
        // Wood-grain engraving, never a text replacement.
        for(i in 0..7){
            val y=160f+i*17f
            color(if(i%2==0)0x7b7b370f else 0x55502413)
            p.style=Paint.Style.STROKE;p.strokeWidth=2f+(i%3)
            path.reset()
            path.moveTo(266f,y)
            path.cubicTo(357f,y-13f,520f,y+16f,651f,y-4f)
            c.drawPath(path,p)
            p.style=Paint.Style.FILL
        }
        wing(c,242f,245f,false)
        wing(c,704f,245f,true)
    }
    private fun wing(c:Canvas,x:Float,y:Float,mirror:Boolean){
        val saved=c.save();c.translate(x,y)
        if(mirror)c.scale(-1f,1f)
        for(i in 0..3){
            path.reset();path.moveTo(0f,i*11f)
            path.cubicTo(-31f,-35f+i*5f,-66f,-21f+i*9f,-71f,-7f+i*11f)
            path.cubicTo(-56f,26f+i*8f,-19f,25f+i*7f,0f,18f+i*5f)
            path.close()
            color(0xff112b5d.toInt());c.drawPath(path,p)
            c.save();c.translate(0f,-3f);color(0xfffffefa.toInt())
            c.drawPath(path,p);c.restore()
        }
        c.restoreToCount(saved)
    }
    private fun awning(c:Canvas) {
        // The wall is intentionally behind the Bopi portrait and storefront.
        shiny(c,62f,333f,816f,278f,27f,0xff8c421d.toInt(),0xff51230e.toInt())
        for(i in 0..13){
            val x=78f+i*60f
            color(if(i%2==0)0xffa86934.toInt() else 0xffa95d2b.toInt())
            c.drawRect(x,376f,x+4f,561f,p)
        }
        round(c,52f,299f,839f,120f,36f,0xff7a301c.toInt())
        shiny(c,46f,298f,852f,95f,35f,0xfffffff5.toInt(),0xffdbd6c7.toInt())
        // Exactly 12 fabric panels; alternating red and ivory with round hems.
        for(i in 0..11){
            val x=48f+i*71f
            val red=i%2==0
            val cloth=if(red)0xfff0443f.toInt() else 0xfffff4de.toInt()
            path.reset()
            path.moveTo(x,304f);path.lineTo(x+71f,304f)
            path.lineTo(x+71f,386f)
            path.cubicTo(x+62f,421f,x+10f,421f,x,386f)
            path.close()
            color(cloth);c.drawPath(path,p)
            p.style=Paint.Style.STROKE
            p.strokeWidth=2f;p.color=if(red)0xffa31922.toInt()
                else 0xffd3bca4.toInt()
            c.drawPath(path,p);p.style=Paint.Style.FILL
        }
        round(c,49f,305f,844f,9f,3f,0xbfffffff.toInt())
        // Draw the two supporting posts with golden brackets.
        for(x in floatArrayOf(71f,856f)){
            round(c,x-17f,389f,34f,204f,10f,0xff693318.toInt())
            shiny(c,x-12f,391f,24f,194f,7f,0xffe1a558.toInt(),
                0xff773716.toInt())
            round(c,x-27f,548f,54f,14f,4f,0xffe9aa46.toInt())
        }
    }
    private fun lantern(c:Canvas,cx:Float,cy:Float){
        color(0xffffca4e.toInt())
        p.strokeWidth=5f;p.style=Paint.Style.STROKE
        c.drawLine(cx,cy-52f,cx,cy-33f,p);p.style=Paint.Style.FILL
        round(c,cx-18f,cy-33f,36f,8f,4f,0xff74360d.toInt())
        path.reset()
        path.moveTo(cx-17f,cy-24f);path.lineTo(cx+17f,cy-24f)
        path.lineTo(cx+12f,cy+22f);path.lineTo(cx-12f,cy+22f)
        path.close();color(0xff75370f.toInt());c.drawPath(path,p)
        color(Color.WHITE)
        p.shader=RadialGradient(cx,cy,27f,
            intArrayOf(0xffffffd5.toInt(),0xfff9b83e.toInt(),0xffaa5b20.toInt()),
            floatArrayOf(0f,.50f,1f),Shader.TileMode.CLAMP)
        c.drawRoundRect(cx-12f,cy-20f,cx+12f,cy+16f,4f,4f,p);p.shader=null
        round(c,cx-20f,cy+15f,40f,9f,4f,0xffeab448.toInt())
    }
    private fun frame(c:Canvas){
        // The same illustrated wooden body encloses Featured and all six
        // genuinely interactive native cards. No embedded products or prices.
        shiny(c,18f,550f,905f,1021f,30f,0xffad6732.toInt(),0xff633016.toInt())
        round(c,27f,577f,889f,972f,24f,0xff5a2a17.toInt())
        shiny(c,35f,580f,873f,952f,22f,0xffbd7d41.toInt(),0xff69351d.toInt())
        // Three thin shelf rails add physical separation without covering taps.
        for(y in floatArrayOf(874f,1205f,1548f)){
            round(c,34f,y+7f,875f,23f,10f,0xff552914.toInt())
            shiny(c,34f,y,875f,14f,7f,0xffffc57a.toInt(),0xff9b581f.toInt())
        }
        for(x in floatArrayOf(31f,913f)){
            round(c,x-13f,587f,26f,959f,8f,0xff81421e.toInt())
            shiny(c,x-8f,590f,16f,946f,7f,0xfff1b46a.toInt(),0xff743516.toInt())
        }
        round(c,0f,1566f,941f,106f,19f,0xff4b2518.toInt())
        shiny(c,0f,1560f,941f,49f,18f,0xffcf8a4b.toInt(),0xff81431e.toInt())
        for(i in 0..7){
            round(c,4f+i*119f,1616f,110f,11f,5f,0x55351613)
        }
    }
    private fun featuredAura(c:Canvas){
        // Violet radial gold-edged feature, under the live Featured contents.
        color(Color.WHITE)
        p.shader=RadialGradient(690f,710f,278f,
            intArrayOf(0xaafff2b4.toInt(),0x66ffa8ff,0x003e05c9),
            floatArrayOf(0f,.43f,1f),Shader.TileMode.CLAMP)
        c.drawOval(458f,584f,886f,856f,p);p.shader=null
        for(i in 0..11){
            val x=492f+(i*131%365)
            val y=613f+(i*73%221)
            color(0xffffe8a5.toInt())
            p.strokeWidth=3.5f;p.style=Paint.Style.STROKE
            val size=5f+(i%3)*3
            c.drawLine(x-size,y,x+size,y,p)
            c.drawLine(x,y-size,x,y+size,p)
            p.style=Paint.Style.FILL
        }
    }
    override fun onDraw(c:Canvas){
        val saved=c.save()
        c.scale(width/941f,height/1672f)
        // Decorative layers render only within this exact 941x1672 stage,
        // with a proportional viewport transform managed by LevelMapStage.
        awning(c)
        woodenBoard(c)
        frame(c)
        lantern(c,87f,467f)
        lantern(c,853f,467f)
        featuredAura(c)
        // Countertop is in front of the mascot's feet but behind all buttons.
        round(c,24f,550f,891f,39f,10f,0xff613118.toInt())
        shiny(c,23f,546f,895f,26f,8f,0xfff9c778.toInt(),0xffaf6629.toInt())
        c.restoreToCount(saved)
    }
}
