package com.brendigo.bopavi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Small, consistent BOPAVI UI pictograms. Android paints every icon from the
 * same geometric vocabulary rather than substituting platform-specific emoji.
 * Decorative only: each host control provides the full Croatian TalkBack label.
 */
internal class BopaviUiIconDrawable(
    private val kind: Kind,
    private val ink: Int,
    private val pixelSize: Int
) : Drawable() {
    enum class Kind { PLAY, PAUSE, RESTART, HOME, EXIT, SETTINGS, RECORDS, STAR, BERRY, GATE, FLIGHT, WING, COIN, SHOP, LEADERBOARD, SOUND }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2.1f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()
    private var requestedAlpha = 255

    override fun getIntrinsicWidth(): Int = pixelSize
    override fun getIntrinsicHeight(): Int = pixelSize
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    override fun setAlpha(alpha: Int) {
        requestedAlpha = alpha.coerceIn(0, 255)
        invalidateSelf()
    }
    override fun setColorFilter(filter: ColorFilter?) {
        paint.colorFilter = filter
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if(bounds.isEmpty) return
        val checkpoint = canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width() / 24f, bounds.height() / 24f)
        paint.color = Color.argb(
            (Color.alpha(ink) * requestedAlpha / 255).coerceIn(0, 255),
            Color.red(ink), Color.green(ink), Color.blue(ink)
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.1f
        path.reset()
        when(kind) {
            Kind.PLAY -> {
                paint.style = Paint.Style.FILL
                path.moveTo(7f, 3.5f)
                path.lineTo(20f, 12f)
                path.lineTo(7f, 20.5f)
                path.close()
                canvas.drawPath(path, paint)
            }
            Kind.PAUSE -> {
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(5f, 4f, 10f, 20f, 1.5f, 1.5f, paint)
                canvas.drawRoundRect(14f, 4f, 19f, 20f, 1.5f, 1.5f, paint)
            }
            Kind.RESTART -> {
                canvas.drawArc(4f, 4f, 20f, 20f, -60f, 296f, false, paint)
                path.moveTo(13.5f, 3f)
                path.lineTo(20f, 4.1f)
                path.lineTo(17.8f, 10f)
                canvas.drawPath(path, paint)
            }
            Kind.HOME -> {
                path.moveTo(3f, 11f)
                path.lineTo(12f, 4f)
                path.lineTo(21f, 11f)
                path.moveTo(5.5f, 10f)
                path.lineTo(5.5f, 20f)
                path.lineTo(18.5f, 20f)
                path.lineTo(18.5f, 10f)
                path.moveTo(10f, 20f)
                path.lineTo(10f, 14f)
                path.lineTo(14f, 14f)
                path.lineTo(14f, 20f)
                canvas.drawPath(path, paint)
            }
            Kind.EXIT -> {
                canvas.drawRoundRect(11f, 3f, 21f, 21f, 1.5f, 1.5f, paint)
                path.moveTo(16f, 12f)
                path.lineTo(3f, 12f)
                path.moveTo(8f, 7f)
                path.lineTo(3f, 12f)
                path.lineTo(8f, 17f)
                canvas.drawPath(path, paint)
            }
            Kind.SETTINGS -> {
                canvas.drawCircle(12f, 12f, 5.3f, paint)
                canvas.drawCircle(12f, 12f, 1.7f, paint)
                for(index in 0 until 8) {
                    val angle = index * PI / 4.0
                    val dx = cos(angle).toFloat()
                    val dy = sin(angle).toFloat()
                    canvas.drawLine(
                        12f + dx * 7.3f, 12f + dy * 7.3f,
                        12f + dx * 10f, 12f + dy * 10f, paint
                    )
                }
            }
            Kind.COIN -> {
                // Crown coin in the reference wallet: gold rim and a tiny
                // impressed crown, not the trophy used by record screens.
                canvas.drawCircle(12f,12f,9f,paint)
                canvas.drawCircle(12f,12f,6.8f,paint)
                path.moveTo(7.5f,13.6f)
                path.lineTo(7f,9.4f)
                path.lineTo(10.2f,11.2f)
                path.lineTo(12f,7.5f)
                path.lineTo(13.8f,11.2f)
                path.lineTo(17f,9.4f)
                path.lineTo(16.5f,13.6f)
                path.close()
                canvas.drawPath(path,paint)
            }
            Kind.SHOP -> {
                paint.style=Paint.Style.FILL
                canvas.drawRoundRect(4f,10f,20f,21f,1.3f,1.3f,paint)
                paint.style=Paint.Style.STROKE
                canvas.drawRect(8f,15f,12f,21f,paint)
                canvas.drawLine(3f,10f,5f,4f,19f,4f,paint)
                for(index in 0..3){
                    canvas.drawLine(5f+index*4f,4f,5f+index*4f,10f,paint)
                }
            }
            Kind.LEADERBOARD -> {
                paint.style=Paint.Style.FILL
                canvas.drawRoundRect(3f,13f,9f,22f,1f,1f,paint)
                canvas.drawRoundRect(9f,6f,15f,22f,1f,1f,paint)
                canvas.drawRoundRect(15f,16f,21f,22f,1f,1f,paint)
            }
            Kind.SOUND -> {
                paint.style=Paint.Style.FILL
                path.moveTo(3f,9f)
                path.lineTo(8f,9f)
                path.lineTo(13f,4f)
                path.lineTo(13f,20f)
                path.lineTo(8f,15f)
                path.lineTo(3f,15f)
                path.close()
                canvas.drawPath(path,paint)
                paint.style=Paint.Style.STROKE
                canvas.drawArc(12f,6f,20f,18f,-65f,130f,false,paint)
                canvas.drawArc(10f,3f,23f,21f,-65f,130f,false,paint)
            }
            Kind.RECORDS -> {
                path.moveTo(7f, 4f)
                path.lineTo(17f, 4f)
                path.lineTo(17f, 10f)
                path.cubicTo(17f, 16f, 7f, 16f, 7f, 10f)
                path.close()
                canvas.drawPath(path, paint)
                canvas.drawArc(2f, 5f, 9f, 13f, 85f, 200f, false, paint)
                canvas.drawArc(15f, 5f, 22f, 13f, -105f, 200f, false, paint)
                canvas.drawLine(12f, 15f, 12f, 19f, paint)
                canvas.drawLine(8f, 20f, 16f, 20f, paint)
            }
            Kind.STAR -> {
                paint.style = Paint.Style.FILL
                for(index in 0 until 10) {
                    val angle = -PI / 2.0 + index * PI / 5.0
                    val radius = if(index % 2 == 0) 10f else 4.4f
                    val x = 12f + cos(angle).toFloat() * radius
                    val y = 12f + sin(angle).toFloat() * radius
                    if(index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            Kind.GATE -> {
                // The two opposing pillars share one canvas grammar with
                // the real in-flight obstacles; their central gap stays clear.
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(4f, 2f, 10f, 9f, 1.2f, 1.2f, paint)
                canvas.drawRoundRect(14f, 2f, 20f, 9f, 1.2f, 1.2f, paint)
                canvas.drawRoundRect(4f, 15f, 10f, 22f, 1.2f, 1.2f, paint)
                canvas.drawRoundRect(14f, 15f, 20f, 22f, 1.2f, 1.2f, paint)
            }
            Kind.FLIGHT -> {
                // Directional movement, not a font glyph or an emoji plane.
                path.moveTo(2.8f, 18f)
                path.quadTo(8f, 9f, 18.5f, 6f)
                path.moveTo(11f, 5.5f)
                path.lineTo(19f, 5.5f)
                path.lineTo(16f, 13f)
                canvas.drawPath(path, paint)
                canvas.drawLine(4f, 20f, 10f, 20f, paint)
            }
            Kind.WING -> {
                paint.style = Paint.Style.FILL
                path.moveTo(3f, 16f)
                path.cubicTo(7f, 6f, 13f, 3f, 21f, 4f)
                path.cubicTo(20f, 9f, 14f, 13f, 11f, 17f)
                path.lineTo(7f, 20f)
                path.close()
                canvas.drawPath(path, paint)
                paint.color = Color.argb(Color.alpha(paint.color), 8, 48, 91)
                canvas.drawLine(7f, 18f, 17f, 7f, paint)
            }
            Kind.BERRY -> {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(8.5f, 14f, 5f, paint)
                canvas.drawCircle(15.5f, 14f, 5f, paint)
                // The fruit is the requested red ink; foliage must stay
                // green, otherwise a strawberry reads as an abstract red blob.
                // Honour Drawable alpha and any host-provided ColorFilter.
                paint.color = Color.argb(Color.alpha(paint.color), 76, 211, 137)
                paint.strokeWidth = 1.9f
                canvas.drawLine(12f, 10f, 13f, 5f, paint)
                path.moveTo(13f, 7f)
                path.quadTo(18f, 2f, 20f, 6f)
                path.quadTo(19f, 9f, 13f, 7f)
                path.close()
                canvas.drawPath(path, paint)
            }
        }
        canvas.restoreToCount(checkpoint)
    }
}
