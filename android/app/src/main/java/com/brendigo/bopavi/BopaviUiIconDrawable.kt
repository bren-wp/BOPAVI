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
    enum class Kind { PLAY, PAUSE, RESTART, HOME, EXIT, SETTINGS, RECORDS, STAR, BERRY }

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
            Kind.BERRY -> {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(8.5f, 14f, 5f, paint)
                canvas.drawCircle(15.5f, 14f, 5f, paint)
                path.moveTo(12f, 10f)
                path.quadTo(12f, 5f, 15f, 4f)
                path.moveTo(13f, 7f)
                path.quadTo(18f, 2f, 20f, 6f)
                path.quadTo(19f, 9f, 13f, 7f)
                canvas.drawPath(path, paint)
            }
        }
        canvas.restoreToCount(checkpoint)
    }
}
