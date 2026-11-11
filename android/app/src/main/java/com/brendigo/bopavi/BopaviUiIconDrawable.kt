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
    enum class Kind { PLAY, PAUSE, RESTART, HOME, EXIT, SETTINGS, RECORDS, STAR, BERRY, GATE, FLIGHT, SHIRT, WING, COIN, CROWN_COIN, GEM, SHOP, LEADERBOARD, SOUND, SHIELD, MAGNET, MUSIC, BELL, GLOBE, GAMEPAD, CLOUD, HEADSET }

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
            Kind.MUSIC -> {
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=2.5f
                canvas.drawLine(12f,5f,19f,3f,paint)
                canvas.drawLine(12f,5f,12f,17f,paint)
                canvas.drawLine(19f,3f,19f,14f,paint)
                paint.style=Paint.Style.FILL
                canvas.drawOval(6f,15f,13f,20f,paint)
                canvas.drawOval(13f,12f,20f,17f,paint)
            }
            Kind.SHIELD -> {
                // One consistent shield silhouette for every upgraded tier.
                paint.style=Paint.Style.FILL
                path.moveTo(12f,2f)
                path.lineTo(20f,5f)
                path.lineTo(19f,14f)
                path.quadTo(16f,19f,12f,22f)
                path.quadTo(8f,19f,5f,14f)
                path.lineTo(4f,5f)
                path.close()
                canvas.drawPath(path,paint)
                paint.color=Color.argb(Color.alpha(paint.color),8,48,91)
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=1.8f
                canvas.drawLine(12f,5f,12f,17f,paint)
            }
            Kind.MAGNET -> {
                // Horseshoe with protected inner slot and alternating pole tips.
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=5f
                canvas.drawArc(4.5f,2.5f,19.5f,18.5f,0f,180f,false,paint)
                paint.style=Paint.Style.FILL
                canvas.drawRoundRect(2f,12f,8f,21f,1f,1f,paint)
                canvas.drawRoundRect(16f,12f,22f,21f,1f,1f,paint)
                paint.color=Color.argb(Color.alpha(paint.color),8,48,91)
                canvas.drawLine(2.5f,16f,7.5f,16f,paint)
                canvas.drawLine(16.5f,16f,21.5f,16f,paint)
            }
            Kind.COIN, Kind.CROWN_COIN -> {
                // The approved counter coin bears a raised flying feather.
                canvas.drawCircle(12f,12f,9f,paint)
                canvas.drawCircle(12f,12f,7f,paint)
                paint.style=Paint.Style.FILL
                path.moveTo(7f,17f)
                path.cubicTo(7.1f,10f,12f,5.7f,18f,6f)
                path.cubicTo(18f,12.6f,13.8f,16.5f,7f,17f)
                path.close()
                canvas.drawPath(path,paint)
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=1.1f
                canvas.drawLine(6.8f,18f,16.7f,7.7f,paint)
            }
            Kind.SHOP -> {
                paint.style=Paint.Style.FILL
                canvas.drawRoundRect(4f,10f,20f,21f,1.3f,1.3f,paint)
                paint.style=Paint.Style.STROKE
                canvas.drawRect(8f,15f,12f,21f,paint)
                canvas.drawLine(5f,4f,19f,4f,paint)
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
            Kind.HEADSET -> {
                // Functional restore action gets the owner's white
                // headset/support pictogram without rasterizing its button.
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=2.9f
                canvas.drawArc(4f,3f,20f,21f,190f,160f,false,paint)
                canvas.drawRoundRect(2.5f,11f,6.7f,19f,1.6f,1.6f,paint)
                canvas.drawRoundRect(17.3f,11f,21.5f,19f,1.6f,1.6f,paint)
                canvas.drawArc(11f,15f,21f,23f,10f,130f,false,paint)
                paint.style=Paint.Style.FILL
                canvas.drawRoundRect(10f,19f,15f,21f,1f,1f,paint)
            }
            Kind.BELL -> {
                // Domed bell, hanging clapper and ring tab from owner Settings.
                paint.style=Paint.Style.FILL
                path.moveTo(5f,17f)
                path.quadTo(7f,15f,7f,9.5f)
                path.cubicTo(7f,3.5f,17f,3.5f,17f,9.5f)
                path.quadTo(17f,15f,19f,17f)
                path.close()
                canvas.drawPath(path,paint)
                canvas.drawOval(10f,18f,14f,22f,paint)
                paint.style=Paint.Style.STROKE
                canvas.drawLine(4f,17f,20f,17f,paint)
            }
            Kind.GLOBE -> {
                canvas.drawCircle(12f,12f,9f,paint)
                canvas.drawOval(8f,3f,16f,21f,paint)
                canvas.drawLine(3f,12f,21f,12f,paint)
                canvas.drawArc(4f,4.5f,20f,19.5f,20f,140f,false,paint)
                canvas.drawArc(4f,4.5f,20f,19.5f,200f,140f,false,paint)
            }
            Kind.GAMEPAD -> {
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=2.3f
                path.moveTo(7f,7f)
                path.cubicTo(4f,6f,3f,10f,2.5f,16f)
                path.quadTo(2.4f,19f,5.5f,18.5f)
                path.lineTo(9f,16.5f)
                path.lineTo(15f,16.5f)
                path.lineTo(18.5f,18.5f)
                path.quadTo(21.6f,19f,21.5f,16f)
                path.cubicTo(21f,10f,20f,6f,17f,7f)
                path.close()
                canvas.drawPath(path,paint)
                canvas.drawLine(7f,10f,7f,15f,paint)
                canvas.drawLine(4.5f,12.5f,9.5f,12.5f,paint)
                paint.style=Paint.Style.FILL
                canvas.drawCircle(16.5f,11.2f,1.4f,paint)
                canvas.drawCircle(19f,13.6f,1.4f,paint)
            }
            Kind.CLOUD -> {
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=2.2f
                path.moveTo(6f,19f)
                path.cubicTo(1f,18f,2f,11f,6.7f,10f)
                path.cubicTo(8f,3.5f,16f,2.5f,18f,10f)
                path.cubicTo(23f,10.5f,23f,18f,18f,19f)
                canvas.drawPath(path,paint)
                canvas.drawLine(12f,20.2f,12f,11f,paint)
                canvas.drawLine(8.5f,14.3f,12f,10.8f,paint)
                canvas.drawLine(15.5f,14.3f,12f,10.8f,paint)
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
            Kind.STAR, Kind.GEM -> {
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
            Kind.SHIRT -> {
                // Reference Skins tile: a blue jersey with a white feather.
                val originalAlpha=Color.alpha(paint.color)
                paint.style=Paint.Style.FILL
                paint.color=Color.argb(originalAlpha,24,122,229)
                path.moveTo(8f,3f)
                path.lineTo(10f,5f)
                path.lineTo(14f,5f)
                path.lineTo(16f,3f)
                path.lineTo(22f,7.5f)
                path.lineTo(19.2f,12f)
                path.lineTo(17.3f,10.8f)
                path.lineTo(17.3f,21f)
                path.lineTo(6.7f,21f)
                path.lineTo(6.7f,10.8f)
                path.lineTo(4.8f,12f)
                path.lineTo(2f,7.5f)
                path.close()
                canvas.drawPath(path,paint)
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=1.2f
                paint.color=Color.argb(originalAlpha,231,250,255)
                canvas.drawPath(path,paint)
                path.reset()
                paint.style=Paint.Style.FILL
                path.moveTo(9f,15f)
                path.cubicTo(10f,10f,15f,8.5f,16f,9.5f)
                path.cubicTo(16f,13f,12.5f,16f,9f,15f)
                path.close()
                canvas.drawPath(path,paint)
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=.7f
                paint.color=Color.argb(originalAlpha,43,112,200)
                canvas.drawLine(9f,15f,15.2f,10f,paint)
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
