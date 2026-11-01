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
internal class FlightLoadingView(context: Context, pilotChoice: Int, private val startup:Boolean=false) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val imageOptions = BitmapFactory.Options().apply { inScaled = false }
    private val backdrop: Bitmap? =
        BitmapFactory.decodeResource(resources, R.drawable.homeback, imageOptions)
    private val brand: Bitmap? =
        BitmapFactory.decodeResource(resources, R.drawable.logo, imageOptions)
    // The previous portrait-only sprite cropped away Bopi's flying silhouette.
    // Body and wings share an original 512 x 512 composition space and are
    // decoded once; all assets are shipped locally with the Android build.
    private val bodies = intArrayOf(R.drawable.bopi0,R.drawable.bopi1,R.drawable.bopi2,
        R.drawable.bopi3,R.drawable.bopi4,R.drawable.bopi5,R.drawable.bopi6,
        R.drawable.bopi7,R.drawable.bopi8,R.drawable.bopi9,R.drawable.bopi10)
    private val leftWings = intArrayOf(R.drawable.bopileft0,R.drawable.bopileft1,
        R.drawable.bopileft2,R.drawable.bopileft3,R.drawable.bopileft4,
        R.drawable.bopileft5,R.drawable.bopileft6,R.drawable.bopileft7,
        R.drawable.bopileft8,R.drawable.bopileft9,R.drawable.bopileft10)
    private val rightWings = intArrayOf(R.drawable.bopiright0,R.drawable.bopiright1,
        R.drawable.bopiright2,R.drawable.bopiright3,R.drawable.bopiright4,
        R.drawable.bopiright5,R.drawable.bopiright6,R.drawable.bopiright7,
        R.drawable.bopiright8,R.drawable.bopiright9,R.drawable.bopiright10)
    private val selected = pilotChoice.coerceIn(0,bodies.lastIndex)
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
        contentDescription = if(startup)"BOPAVI se pokreće" else "Loading level"
        isClickable = false
        isFocusable = false
    }

    fun completePreparation() {
        preparationFinished = true
        contentDescription = if(startup)"BOPAVI je spreman" else "Level loaded"
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

    /**
     * Perspective gold ring instead of a flat coin with a star. The owner
     * Loading reference shows tilted, hollow rings following Bopi's flight.
     */
    private fun ring(canvas: Canvas, x: Float, y: Float,
                     horizontalRadius: Float, verticalRadius: Float, tilt: Float) {
        val saved=canvas.save()
        canvas.rotate(tilt,x,y)
        val ellipse=RectF(x-horizontalRadius,y-verticalRadius,
            x+horizontalRadius,y+verticalRadius)
        paint.reset()
        paint.isAntiAlias=true
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=horizontalRadius*.40f
        paint.color=0x5507254d
        canvas.drawOval(RectF(ellipse.left+5f,ellipse.top+11f,
            ellipse.right+5f,ellipse.bottom+11f),paint)
        paint.strokeWidth=horizontalRadius*.43f
        paint.color=0xffa94d00.toInt()
        canvas.drawOval(ellipse,paint)
        paint.strokeWidth=horizontalRadius*.33f
        paint.color=0xffffa800.toInt()
        canvas.drawOval(ellipse,paint)
        paint.strokeWidth=horizontalRadius*.20f
        paint.color=0xffffda3d.toInt()
        canvas.drawOval(ellipse,paint)
        paint.strokeWidth=horizontalRadius*.095f
        paint.color=0xfffff4a9.toInt()
        canvas.drawArc(ellipse,202f,158f,false,paint)
        paint.style=Paint.Style.FILL
        canvas.restoreToCount(saved)
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

        // Translucent wind trails follow the original flying pose without
        // obscuring the islands or pretending to be a flat UI container.
        paint.reset()
        paint.isAntiAlias=true
        paint.style=Paint.Style.STROKE
        paint.strokeCap=Paint.Cap.ROUND
        paint.color=0x99edfcff.toInt()
        paint.strokeWidth=10f
        canvas.drawArc(RectF(-115f,590f,870f,1084f),
            38f,105f,false,paint)
        paint.color=0x66ffffff
        paint.strokeWidth=5f
        canvas.drawArc(RectF(-80f,628f,895f,1100f),
            44f,95f,false,paint)
        paint.style=Paint.Style.FILL
        image(canvas, brand, logoRect)
        // Compose the same original pilot as actual gameplay, wings behind
        // the head/body. Unlike a portrait, this contains real wing geometry.
        image(canvas, leftWing, pilotRect)
        image(canvas, rightWing, pilotRect)
        image(canvas, body, pilotRect)

        // Ring positions follow the ORIGINAL Loading reference, not the
        // Game Over screenshot. They are illustrative, never wallet values.
        ring(canvas,104f,862f,75f,92f,-23f)
        ring(canvas,354f,1010f,73f,105f,-29f)
        ring(canvas,550f,1074f,37f,50f,-23f)
        ring(canvas,658f,1102f,27f,34f,-20f)
        ring(canvas,738f,1101f,21f,28f,-18f)
        ring(canvas,28f,1197f,149f,176f,-27f)

        // Owner's supplied Splash has a raised, blue-tinted progress track
        // near the bottom. Its cyan animation remains indeterminate: no fake
        // completion percentage or arbitrary network-dependent wait.
        val outer=RectF(FlightLoadingLayout.track.left,
            FlightLoadingLayout.track.top,
            FlightLoadingLayout.track.right,
            FlightLoadingLayout.track.bottom)
        val radius=outer.height()*.50f
        fill(canvas,RectF(outer.left+2f,outer.top+9f,outer.right+3f,
            outer.bottom+12f),0x66031d57, radius)
        gradient(canvas,outer,0xfff5ffff.toInt(),0xff62a8ed.toInt(),radius)
        gradient(canvas,RectF(outer.left+5f,outer.top+5f,outer.right-5f,
            outer.bottom-5f),0xff1f67cb.toInt(),0xff0b2d78.toInt(),radius-5f)
        val inner=RectF(outer.left+12f,outer.top+11f,
            outer.right-12f,outer.bottom-12f)
        gradient(canvas,inner,0xff0c4ba5.toInt(),0xff153a84.toInt(),radius-12f)
        // The illuminated segment changes width without implying a reported
        // load percentage. Once game preparation completes, fill the track.
        val phase=(android.os.SystemClock.uptimeMillis()%1550L)/1550f
        val glow=if(preparationFinished)1f else .30f+.44f*
            (.5f-.5f*kotlin.math.cos(phase*2f*Math.PI.toFloat()))
        val segment=RectF(inner.left+3f,inner.top+4f,
            inner.left+3f+(inner.width()-6f)*glow,inner.bottom-4f)
        gradient(canvas,segment,0xffa6ffff.toInt(),0xff00a5fd.toInt(),
            segment.height()*.5f)
        val clip=canvas.save()
        canvas.clipRoundRect(segment,segment.height()*.5f,
            segment.height()*.5f)
        paint.reset()
        paint.isAntiAlias=true
        paint.color=0x44ffffff
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=6f
        val stripes=segment.width().toInt()/24+2
        for(i in 0..stripes){
            val x=segment.left+i*24f-8f
            canvas.drawLine(x,segment.bottom-1f,x+30f,segment.top+1f,paint)
        }
        canvas.restoreToCount(clip)
        fill(canvas,RectF(inner.left+7f,inner.top+6f,
            inner.right-7f,inner.top+12f),0x55ffffff,6f)
        outlinedText(canvas,if(preparationFinished)"Ready!" else "Loading...",
            FlightLoadingLayout.captionCenterX,
            FlightLoadingLayout.captionBaselineY,53f)
        if(!preparationFinished && isAttachedToWindow)postInvalidateDelayed(60L)
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
