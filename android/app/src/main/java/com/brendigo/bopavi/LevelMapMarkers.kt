package com.brendigo.bopavi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Three decorative native stars; only saved results determine their fill. */
internal class LevelMapStarStrip(
    context:Context,private val earned:Int
):View(context){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    private fun star(canvas:Canvas,cx:Float,cy:Float,r:Float,filled:Boolean){
        path.reset()
        for(i in 0..9) {
            val angle=i*Math.PI/5.0-Math.PI/2.0
            val radial=if(i%2==0)r else r*.50f
            val x=cx+cos(angle).toFloat()*radial
            val y=cy+sin(angle).toFloat()*radial
            if(i==0)path.moveTo(x,y) else path.lineTo(x,y)
        }
        path.close()
        paint.style=Paint.Style.FILL
        paint.color=if(filled)0xffffcd30.toInt() else 0xffadbecb.toInt()
        canvas.drawPath(path,paint)
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=r*.14f
        paint.color=if(filled)0xffa45910.toInt() else 0xff56687d.toInt()
        canvas.drawPath(path,paint)
        paint.style=Paint.Style.FILL
    }
    override fun onDraw(canvas:Canvas) {
        val scale=min(width/156f,height/42f)
        val saved=canvas.save()
        canvas.scale(scale,scale)
        val count=earned.coerceIn(0,3)
        for(i in 0..2)star(canvas,29f+i*49f,22f,20f,i<count)
        canvas.restoreToCount(saved)
    }
}

/** Platform-independent closed padlock, not an emoji that changes with fonts. */
internal class LevelMapLockGlyph(context:Context):View(context){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas) {
        val scale=min(width/40f,height/46f)
        val saved=canvas.save()
        canvas.scale(scale,scale)
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=5f
        paint.strokeCap=Paint.Cap.ROUND
        paint.color=0xff203451.toInt()
        canvas.drawArc(10f,4f,30f,31f,180f,180f,false,paint)
        paint.style=Paint.Style.FILL
        paint.color=0xffecf4ff.toInt()
        canvas.drawRoundRect(5f,19f,35f,44f,6f,6f,paint)
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=3f
        paint.color=0xff293f5f.toInt()
        canvas.drawRoundRect(5f,19f,35f,44f,6f,6f,paint)
        paint.style=Paint.Style.FILL
        canvas.drawCircle(20f,30f,3f,paint)
        canvas.drawRoundRect(18f,31f,22f,38f,1f,1f,paint)
        canvas.restoreToCount(saved)
    }
}
