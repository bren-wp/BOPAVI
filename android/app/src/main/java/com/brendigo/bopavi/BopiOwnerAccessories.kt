package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader

/**
 * Splash / Home ONLY: independently painted 3D scarf, gold crest and the
 * second aviator lens from the owner's Bopi reference. Render on top of
 * the preserved approved three-layer sprite; gameplay skins, hitboxes,
 * locomotion, physics and sprite alpha are completely unchanged.
 *
 * All geometry uses the 512×512 transparent original pilot layer grid.
 * It is NOT a flattened screenshot or a duplicate bird image.
 */
internal object BopiOwnerAccessories {
    private fun project(canvas:Canvas,box:RectF,draw:()->Unit) {
        val saved=canvas.save()
        canvas.translate(box.left,box.top)
        canvas.scale(box.width()/512f,box.height()/512f)
        draw()
        canvas.restoreToCount(saved)
    }

    fun drawBack(canvas:Canvas,box:RectF) = project(canvas,box) {
        val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.FILL}
        // Reference's long wind-swept red ribbon sits BEHIND the bird,
        // with broad curved rather than triangular sprite geometry.
        val tail=Path().apply{
            moveTo(224f,323f)
            cubicTo(182f,297f,115f,282f,76f,286f)
            cubicTo(109f,301f,135f,330f,141f,355f)
            cubicTo(93f,348f,65f,336f,42f,319f)
            cubicTo(78f,383f,156f,394f,237f,354f)
            close()
        }
        p.color=0xff7c1731.toInt()
        canvas.drawPath(tail,p)
        p.shader=LinearGradient(90f,294f,162f,385f,
            intArrayOf(0xffff6545.toInt(),0xfff02a20.toInt(),
                0xffa70f28.toInt()),null,Shader.TileMode.CLAMP)
        val inner=Path().apply{
            moveTo(220f,328f)
            cubicTo(167f,295f,123f,298f,78f,294f)
            cubicTo(118f,320f,137f,350f,171f,358f)
            cubicTo(128f,358f,91f,341f,62f,328f)
            cubicTo(112f,385f,181f,374f,225f,351f)
            close()
        }
        canvas.drawPath(inner,p)
        p.shader=null
        p.color=0x77ffb49b
        p.style=Paint.Style.STROKE
        p.strokeWidth=3.5f
        p.strokeCap=Paint.Cap.ROUND
        canvas.drawArc(RectF(85f,295f,233f,373f),210f,98f,false,p)
    }

    fun drawFront(canvas:Canvas,box:RectF) = project(canvas,box) {
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        p.style=Paint.Style.FILL
        // Owner reference red scarf crossing the white chest.
        val band=Path().apply{
            // Rounded neck-wrap, not the old triangular red wedge that
            // concealed the white chest on 941px Splash and Home.
            moveTo(230f,321f)
            cubicTo(256f,338f,294f,356f,322f,350f)
            cubicTo(345f,346f,360f,334f,369f,325f)
            cubicTo(372f,344f,353f,364f,330f,376f)
            cubicTo(293f,391f,253f,369f,235f,350f)
            cubicTo(228f,341f,225f,328f,230f,321f)
            close()
        }
        p.color=0xff752131.toInt()
        canvas.drawPath(band,p)
        p.shader=LinearGradient(230f,309f,297f,391f,
            intArrayOf(0xffff7353.toInt(),0xffe62921.toInt(),
                0xff8b162d.toInt()),null,Shader.TileMode.CLAMP)
        canvas.drawPath(band,p)
        p.shader=null
        val fold=Path().apply{
            moveTo(246f,333f)
            cubicTo(279f,355f,315f,363f,354f,338f)
        }
        p.color=0xbaff9b73.toInt()
        p.style=Paint.Style.STROKE
        p.strokeWidth=5f
        p.strokeCap=Paint.Cap.ROUND
        canvas.drawPath(fold,p)
        p.style=Paint.Style.FILL

        // Raised gold royal clasp: three individually readable bevel layers.
        val mx=308f;val my=371f
        p.color=0x79051428
        canvas.drawCircle(mx+2f,my+6f,28f,p)
        p.color=0xff542416.toInt()
        canvas.drawCircle(mx,my,28f,p)
        p.shader=LinearGradient(mx-13f,my-24f,mx+14f,my+24f,
            intArrayOf(0xfffff8ad.toInt(),0xffffb724.toInt(),
                0xffaf5807.toInt()),null,Shader.TileMode.CLAMP)
        canvas.drawCircle(mx,my-2f,23f,p)
        p.shader=null
        p.color=0xff6f320c.toInt()
        canvas.drawCircle(mx,my-2f,17f,p)
        p.color=0xffffda45.toInt()
        val crown=Path().apply{
            moveTo(mx-12f,my+2f)
            lineTo(mx-13f,my-7f)
            lineTo(mx-5f,my-2f)
            lineTo(mx,my-10f)
            lineTo(mx+6f,my-2f)
            lineTo(mx+13f,my-7f)
            lineTo(mx+12f,my+3f)
            close()
        }
        canvas.drawPath(crown,p)
        p.color=0xfffff0a4.toInt()
        canvas.drawRoundRect(RectF(mx-12f,my+4f,mx+12f,my+8f),2f,2f,p)

        // The owner's goggle pair is secured to one continuous leather
        // aviator band. Without this curved bridge, the second gold lens
        // floats away from Bopi's head in both native Home and Splash.
        val leatherBridge=Path().apply{
            moveTo(340f,153f)
            cubicTo(367f,142f,392f,150f,422f,164f)
            cubicTo(426f,173f,426f,183f,419f,190f)
            cubicTo(393f,172f,369f,166f,344f,172f)
            close()
        }
        p.color=0x99071a3d.toInt()
        val bridgeShadow=canvas.save()
        canvas.translate(0f,5f)
        canvas.drawPath(leatherBridge,p)
        canvas.restoreToCount(bridgeShadow)
        p.shader=LinearGradient(348f,145f,388f,191f,
            intArrayOf(0xffb86b35.toInt(),0xff754021.toInt(),
                0xff47251d.toInt()),null,Shader.TileMode.CLAMP)
        canvas.drawPath(leatherBridge,p)
        p.shader=null
        p.color=0xffdf9f57.toInt()
        p.style=Paint.Style.STROKE
        p.strokeWidth=3.1f
        p.strokeCap=Paint.Cap.ROUND
        canvas.drawArc(RectF(345f,150f,429f,191f),185f,112f,false,p)
        p.style=Paint.Style.FILL
        // The original approved sprite has one round goggle. The owner's
        // new pilot has two cyan lenses in a tilted brown leather band.
        // Draw the second lens over the bridge, ABOVE the right eye.
        val g=canvas.save()
        // The old oval hovered too far to the right of the real head.
        // Translate only the second 3D lens 26/512 units toward Bopi's
        // original left lens; no real eye or gameplay sprite is modified.
        canvas.translate(-26f,1f)
        canvas.rotate(28f,419f,176f)
        p.color=0x6b041b3c
        canvas.drawOval(RectF(389f,151f,453f,210f),p)
        p.color=0xff60301d.toInt()
        canvas.drawOval(RectF(386f,146f,450f,203f),p)
        p.shader=LinearGradient(386f,147f,444f,205f,
            0xffffeb76.toInt(),0xffb76615.toInt(),Shader.TileMode.CLAMP)
        canvas.drawOval(RectF(392f,150f,444f,196f),p)
        p.shader=null
        p.color=0xff11486c.toInt()
        canvas.drawOval(RectF(397f,156f,438f,191f),p)
        p.shader=LinearGradient(402f,160f,434f,190f,
            0xffb7faff.toInt(),0xff0b91da.toInt(),Shader.TileMode.CLAMP)
        canvas.drawOval(RectF(400f,157f,436f,187f),p)
        p.shader=null
        p.color=0x99ffffff.toInt()
        p.style=Paint.Style.STROKE
        p.strokeCap=Paint.Cap.ROUND
        p.strokeWidth=3.5f
        canvas.drawArc(RectF(402f,159f,433f,187f),202f,112f,false,p)
        p.style=Paint.Style.FILL
        // Gold rivets and the small glass reflection make this lens read as
        // raised metal, not an isolated blue dot.
        p.color=0xffe9b967.toInt()
        canvas.drawCircle(393f,175f,3.6f,p)
        canvas.drawCircle(443f,175f,3.6f,p)
        p.color=0x99ffffff.toInt()
        canvas.drawOval(RectF(405f,160f,415f,167f),p)
        canvas.restoreToCount(g)
    }
}
