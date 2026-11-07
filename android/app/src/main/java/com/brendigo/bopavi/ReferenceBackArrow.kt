package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/** White 3D owner-reference back arrow inside a live native blue button. */
internal class ReferenceBackArrow(private val px:Int):Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val shape=Path()
    private var requestedAlpha=255
    private var filter:ColorFilter?=null

    init {
        shape.moveTo(27f,7f)
        shape.cubicTo(22f,7f,11f,20f,6f,27f)
        shape.cubicTo(4f,30f,5f,34f,8f,37f)
        shape.lineTo(27f,55f)
        shape.cubicTo(33f,59f,38f,56f,38f,49f)
        shape.lineTo(38f,43f)
        shape.lineTo(55f,43f)
        shape.cubicTo(60f,43f,61f,39f,61f,34f)
        shape.lineTo(61f,28f)
        shape.cubicTo(61f,23f,58f,20f,53f,20f)
        shape.lineTo(38f,20f)
        shape.lineTo(38f,14f)
        shape.cubicTo(38f,7f,33f,4f,27f,7f)
        shape.close()
    }
    override fun draw(canvas:Canvas) {
        if(bounds.isEmpty)return
        val saved=canvas.save()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        canvas.scale(bounds.width()/66f,bounds.height()/66f)
        p.reset();p.isAntiAlias=true
        p.strokeJoin=Paint.Join.ROUND
        p.color=0xff003279.toInt();p.alpha=requestedAlpha
        p.style=Paint.Style.STROKE;p.strokeWidth=8f
        p.colorFilter=filter
        canvas.drawPath(shape,p)
        p.style=Paint.Style.FILL;p.color=0xfffcffff.toInt()
        p.alpha=requestedAlpha;canvas.drawPath(shape,p)
        p.style=Paint.Style.STROKE
        p.strokeWidth=3f;p.color=0xff82c8fa.toInt()
        p.alpha=requestedAlpha
        canvas.save();canvas.translate(-1f,-1f)
        canvas.drawPath(shape,p);canvas.restore()
        canvas.restoreToCount(saved)
    }
    override fun setAlpha(value:Int){requestedAlpha=value.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(value:ColorFilter?){filter=value;invalidateSelf()}
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
    override fun getIntrinsicWidth():Int=px
    override fun getIntrinsicHeight():Int=px
}

/**
 * Non-clickable arrow artwork over an independently clickable native Button.
 * Leaves the REAL Back/Home action, ripple and accessibility on that Button.
 */
internal class ReferenceArrowOverlayView(context:android.content.Context):android.view.View(context){
    private val arrow=ReferenceBackArrow(58)
    init {
        importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable=false
        isFocusable=false
    }
    override fun onDraw(canvas:Canvas) {
        val size=minOf(width*.62f,height*.70f).toInt().coerceAtLeast(1)
        val l=(width-size)/2
        val t=(height-size)/2
        arrow.setBounds(l,t,l+size,t+size)
        arrow.draw(canvas)
    }
}
