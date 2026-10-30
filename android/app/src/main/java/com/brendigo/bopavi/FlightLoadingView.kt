package com.brendigo.bopavi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * Owner's Splash/Loading artwork, implemented as real preflight preparation.
 *
 * The artwork is drawn from original offline BOPAVI resources and native
 * primitives. The progress track is EMPTY until stream preparation finishes;
 * it never invents a percentage or delays a ready game for animation.
 * The OS launch window still goes directly to Home (no duplicate splash).
 *
 * Baseline 941 × 1672 with uniform scaling for text/logo/character. The
 * fantasy backdrop is center-cropped, never stretched, at other aspect ratios.
 */
internal class FlightLoadingView(context: Context, pilotChoice: Int) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val imageOptions = BitmapFactory.Options().apply { inScaled = false }
    private val backdrop: Bitmap? =
        BitmapFactory.decodeResource(resources, R.drawable.homeback, imageOptions)
    private val brand: Bitmap? =
        BitmapFactory.decodeResource(resources, R.drawable.logo, imageOptions)
    // The previous portrait-only sprite cropped away Bopi's flying silhouette.
    // Body and wings share an original 512 x 512 composition space and are
    // decoded once; all assets are shipped locally with the Android build.
    private val bodies = intArrayOf(R.drawable.bopi0,R.drawable.bopi9,R.drawable.bopi10)
    private val leftWings = intArrayOf(R.drawable.bopileft0,R.drawable.bopileft9,R.drawable.bopileft10)
    private val rightWings = intArrayOf(R.drawable.bopiright0,R.drawable.bopiright9,R.drawable.bopiright10)
    private val selected = pilotChoice.coerceIn(0,2)
    private val body: Bitmap? =
        BitmapFactory.decodeResource(resources,bodies[selected],imageOptions)
    private val leftWing: Bitmap? =
        BitmapFactory.decodeResource(resources,leftWings[selected],imageOptions)
    private val rightWing: Bitmap? =
        BitmapFactory.decodeResource(resources,rightWings[selected],imageOptions)
    private val logoRect=RectF(FlightLoadingLayout.logo.left,FlightLoadingLayout.logo.top,
        FlightLoadingLayout.logo.right,FlightLoadingLayout.logo.bottom)
    private val pilotRect=RectF(FlightLoadingLayout.pilot.left,FlightLoadingLayout.pilot.top,
        FlightLoadingLayout.pilot.right,FlightLoadingLayout.pilot.bottom)

    private var preparationFinished = false

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = "Loading level"
        isClickable = false
        isFocusable = false
    }

    fun completePreparation() {
        preparationFinished = true
        contentDescription = "Level loaded"
        invalidate()
    }

    private fun image(canvas: Canvas, bitmap: Bitmap?, rect: RectF) {
        if (bitmap == null) return
        paint.reset()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, null, rect, paint)
    }

    private fun fill(canvas: Canvas, rect: RectF, color: Int, radius: Float) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = color
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    private fun gradient(
        canvas: Canvas,
        rect: RectF,
        top: Int,
        bottom: Int,
        radius: Float
    ) {
        paint.reset()
        paint.isAntiAlias = true
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.left, rect.bottom,
            top, bottom, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
    }

    private fun outlinedText(canvas: Canvas, text: String, x: Float, y: Float, size: Float) {
        paint.reset()
        paint.isAntiAlias = true
        paint.typeface = android.graphics.Typeface.create(
            "sans-serif-rounded", android.graphics.Typeface.BOLD
        )
        paint.textSize = size
        paint.textAlign = Paint.Align.CENTER
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = size * .14f
        paint.style = Paint.Style.STROKE
        paint.color = 0xff073872.toInt()
        canvas.drawText(text, x, y, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawText(text, x, y, paint)
    }

    private fun coin(canvas: Canvas, x: Float, y: Float, radius: Float) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = 0xffa64e00.toInt()
        canvas.drawCircle(x, y + 6f, radius + 5f, paint)
        paint.color = 0xffffa510.toInt()
        canvas.drawCircle(x, y, radius + 3f, paint)
        paint.color = 0xffffdb45.toInt()
        canvas.drawCircle(x, y - 3f, radius - 3f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.WHITE
        canvas.drawArc(RectF(x-radius+6f, y-radius+6f, x+radius-6f, y+radius-6f),
            205f, 55f, false, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xffaa6400.toInt()
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textSize = radius * 1.35f
        canvas.drawText("★", x, y + radius * .42f, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        canvas.drawColor(0xff168bdf.toInt())
        val bg = backdrop
        if (bg != null) {
            // Exact aspect-ratio preservation for islands and waterfalls.
            val crop = max(w / bg.width, h / bg.height)
            val sourceW = w / crop
            val sourceH = h / crop
            val left = (bg.width - sourceW) / 2f
            val top = (bg.height - sourceH) / 2f
            paint.reset()
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            canvas.drawBitmap(
                bg,
                Rect(left.toInt(), top.toInt(),
                    (left + sourceW).toInt().coerceAtMost(bg.width),
                    (top + sourceH).toInt().coerceAtMost(bg.height)),
                RectF(0f, 0f, w, h),
                paint
            )
        }

        val scale = min(w / FlightLoadingLayout.referenceWidth,
            h / FlightLoadingLayout.referenceHeight)
        val offsetX = (w - FlightLoadingLayout.referenceWidth * scale) / 2f
        val offsetY = (h - FlightLoadingLayout.referenceHeight * scale) / 2f
        val checkpoint = canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)

        // Slight sky veil keeps large Bopi and the wordmark legible over
        // bright waterfalls without inserting a flat background rectangle.
        paint.reset()
        paint.color = 0x1affffff
        canvas.drawOval(RectF(90f, 410f, 855f, 1210f), paint)
        image(canvas, brand, logoRect)
        // Compose the same original pilot as actual gameplay, wings behind
        // the head/body. Unlike a portrait, this contains real wing geometry.
        image(canvas, leftWing, pilotRect)
        image(canvas, rightWing, pilotRect)
        image(canvas, body, pilotRect)

        // Coins are decoration only. No fictional balances or purchases.
        coin(canvas, 158f, 1005f, 53f)
        coin(canvas, 740f, 1108f, 37f)
        coin(canvas, 605f, 1185f, 29f)

        // Golden winged frame and royal-blue/cyan inner track from reference.
        val outer = RectF(139f, 1333f, 802f, 1441f)
        fill(canvas, RectF(130f, 1335f, 168f, 1406f), Color.WHITE, 34f)
        fill(canvas, RectF(773f, 1335f, 811f, 1406f), Color.WHITE, 34f)
        fill(canvas, outer, 0xff43200b.toInt(), 57f)
        gradient(canvas, RectF(147f, 1339f, 794f, 1432f),
            0xffffed7b.toInt(), 0xffff8c06.toInt(), 48f)
        fill(canvas, RectF(166f, 1354f, 775f, 1415f), 0xff38170a.toInt(), 38f)
        gradient(canvas, RectF(177f, 1363f, 764f, 1407f),
            0xff172e66.toInt(), 0xff562009.toInt(), 27f)
        if (preparationFinished) {
            gradient(canvas, RectF(181f, 1366f, 758f, 1405f),
                0xff8dfaff.toInt(), 0xff0088f5.toInt(), 25f)
        }
        outlinedText(canvas, if (preparationFinished) "Ready!" else "Loading...",
            470f, 1529f, 61f)
        canvas.restoreToCount(checkpoint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        backdrop?.recycle()
        brand?.recycle()
        body?.recycle()
        leftWing?.recycle()
        rightWing?.recycle()
    }
}
