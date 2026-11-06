package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * Owner-reference raised enamel for the *native* Game Over Retry/Home views.
 *
 * Never a screenshot or a faux button: this is the pressed-aware background
 * of the existing clickable Android LinearLayout. Artwork is purely local.
 * Source palette uses warm gold and deep blue, with extruded lower edges.
 */
internal class ResultActionEnamelDrawable(private val golden:Boolean):Drawable() {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val face=RectF()
    private val gleam=Path()
    private var requestedAlpha=255
    private var colorFilter:ColorFilter?=null
    private var pressed=false
    private var enabled=true

    override fun isStateful():Boolean=true
    override fun onStateChange(states:IntArray):Boolean {
        val down=android.R.attr.state_pressed in states
        val active=android.R.attr.state_enabled in states
        if(pressed==down && enabled==active)return false
        pressed=down
        enabled=active
        invalidateSelf()
        return true
    }

    override fun draw(canvas:Canvas) {
        if(bounds.isEmpty)return
        val saved=canvas.save()
        val w=bounds.width().toFloat()
        val h=bounds.height().toFloat()
        canvas.translate(bounds.left.toFloat(),bounds.top.toFloat())
        // Native click area is unchanged; only visual height compresses.
        val drop=min(24f,h*.155f)*(if(pressed).49f else 1f)
        val round=min(h*.46f,w*.23f)
        val xInset=maxOf(5f,w*.011f)
        if(!enabled)canvas.saveLayerAlpha(0f,0f,w,h,155)

        fun coat(color:Int){
            paint.reset()
            paint.isAntiAlias=true
            paint.color=color
            paint.alpha=(Color.alpha(color)*requestedAlpha/255).coerceIn(0,255)
            paint.colorFilter=colorFilter
        }
        fun pill(left:Float,top:Float,right:Float,bottom:Float,
                 radius:Float,color:Int){
            coat(color)
            canvas.drawRoundRect(left,top,right,bottom,radius,radius,paint)
        }
        val ink=if(golden)0xff5c280d.toInt() else 0xff062655.toInt()
        val wall=if(golden)0xffbb4812.toInt() else 0xff06469c.toInt()
        val bottom=if(golden)0xffd86a12.toInt() else 0xff0368c2.toInt()
        val rim=if(golden)0xffffe56a.toInt() else 0xff62dafa.toInt()
        val top=if(golden)0xffffff91.toInt() else 0xff80f0ff.toInt()
        val mid=if(golden)0xffffc11f.toInt() else 0xff12aeff.toInt()
        val lower=if(golden)0xffff920c.toInt() else 0xff0771df.toInt()

        // Separate shadow, navy/brown contour and thick physically extruded
        // lower wall are visible against both dark and light game scenery.
        pill(xInset+1f,drop+5f,w-xInset+2f,h,round,0x8805193c.toInt())
        pill(2f,drop*.47f,w-2f,h-2f,round,ink)
        pill(5f,drop*.35f,w-5f,h-5f,round,wall)
        pill(5f,drop*.21f,w-5f,h-drop*.31f,round,bottom)
        pill(5f,0f,w-5f,h-drop*.76f,round,ink)
        pill(xInset,4f,w-xInset,h-drop*.82f,round*.94f,rim)
        pill(xInset+4f,7f,w-xInset-4f,h-drop-6f,round*.86f,
            0xffffffff.toInt())

        // Three-band colored face gives a bright glossy crown, saturated
        // middle and shaded bottom instead of a flat stock Android button.
        face.set(xInset+9f,10f,w-xInset-9f,h-drop-10f)
        coat(Color.WHITE)
        paint.shader=LinearGradient(0f,face.top,0f,face.bottom,
            intArrayOf(top,mid,lower),
            floatArrayOf(0f,.47f,1f),Shader.TileMode.CLAMP)
        canvas.drawRoundRect(face,round*.80f,round*.80f,paint)
        paint.shader=null

        // Asymmetric convex white reflection follows the same curved face.
        gleam.reset()
        gleam.moveTo(face.left+11f,face.top+4f)
        gleam.lineTo(face.right-12f,face.top+4f)
        gleam.cubicTo(face.right-face.width()*.15f,
            face.top+face.height()*.27f,
            face.left+face.width()*.51f,
            face.top+face.height()*.34f,
            face.left+11f,face.top+face.height()*.38f)
        gleam.close()
        val clipped=canvas.save()
        canvas.clipRect(face.left,face.top,face.right,face.bottom)
        coat(0x5fffffff)
        canvas.drawPath(gleam,paint)
        canvas.restoreToCount(clipped)

        coat(0xbaffffff.toInt())
        paint.style=Paint.Style.STROKE
        paint.strokeCap=Paint.Cap.ROUND
        paint.strokeWidth=min(6f,h*.039f)
        canvas.drawArc(RectF(face.left+11f,face.top+6f,
            face.left+min(face.width()*.29f,130f),
            face.top+min(face.height()*.42f,57f)),
            201f,88f,false,paint)
        paint.style=Paint.Style.FILL

        // Deep inset at the base and tiny sidewall sparkle emphasize depth.
        pill(face.left+25f,face.bottom-6f,face.right-24f,
            face.bottom-3f,2f,if(golden)0x66a24503 else 0x66022a74)
        if(pressed){
            pill(face.left,face.top,face.right,face.bottom,
                round*.80f,0x26001036)
        }
        canvas.restoreToCount(saved)
    }

    override fun setAlpha(alpha:Int) {
        requestedAlpha=alpha.coerceIn(0,255)
        invalidateSelf()
    }
    override fun setColorFilter(filter:ColorFilter?) {
        colorFilter=filter
        invalidateSelf()
    }
    @Suppress("DEPRECATION")
    override fun getOpacity():Int=PixelFormat.TRANSLUCENT
}
