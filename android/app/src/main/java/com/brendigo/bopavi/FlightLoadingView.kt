package com.brendigo.bopavi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
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
    private val wingShape = Path()
    // Reuse the same temporary gloss contour every animation frame.
    private val progressGlossClip = Path()
    private val imageOptions = BitmapFactory.Options().apply { inScaled = false }
    private val backdrop: Bitmap? =
        BitmapFactory.decodeResource(resources, R.drawable.loadingback, imageOptions)
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
    private val selected = PilotSelectionModel.spriteIdForChoice(pilotChoice)
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
    // Relative time makes the first cyan sweep begin at the left edge;
    // uptime since device boot is not the animation's start time.
    private val scanStartedAtMs = android.os.SystemClock.uptimeMillis()

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
        // A separate navy 3D extrusion and blue edge match the owner's
        // raised "Loading..." lettering without replacing accessible state.
        paint.strokeWidth = size * .20f
        paint.style = Paint.Style.STROKE
        paint.color = 0xff052359.toInt()
        canvas.drawText(text, x, y+4f, paint)
        paint.strokeWidth = size * .125f
        paint.color = 0xff166ac4.toInt()
        canvas.drawText(text, x, y, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawText(text, x, y, paint)
    }

    /** The owner's flying rings are hollow metallic GOLD (no candy stripes).
     * The dark lower extrusion and pale arc recreate local enamel depth.
     */
    private fun ring(canvas: Canvas, x: Float, y: Float,
                     horizontalRadius: Float, verticalRadius: Float, tilt: Float) {
        val saved=canvas.save()
        canvas.rotate(tilt,x,y)
        val oval=RectF(x-horizontalRadius,y-verticalRadius,
            x+horizontalRadius,y+verticalRadius)
        paint.reset()
        paint.isAntiAlias=true
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=horizontalRadius*.42f
        paint.color=0x66062255
        canvas.drawOval(RectF(oval.left+6f,oval.top+10f,
            oval.right+6f,oval.bottom+10f),paint)
        paint.color=0xff945015.toInt()
        paint.strokeWidth=horizontalRadius*.37f
        canvas.drawOval(oval,paint)
        paint.color=0xffffb800.toInt()
        paint.strokeWidth=horizontalRadius*.30f
        canvas.drawOval(oval,paint)
        paint.color=0xffffed87.toInt()
        paint.strokeWidth=horizontalRadius*.13f
        canvas.drawArc(oval,198f,158f,false,paint)
        paint.color=0xfffff9c4.toInt()
        paint.strokeWidth=horizontalRadius*.055f
        canvas.drawArc(oval,226f,85f,false,paint)
        paint.style=Paint.Style.FILL
        canvas.restoreToCount(saved)
    }

    /** Noninteractive white feather wings sit behind the ACTUAL loading track.
     * Drawing a winged bar never substitutes a fake loading percentage.
     */
    private fun trackWings(canvas:Canvas) {
        for(side in 0..1){
            val save=canvas.save()
            if(side==1){
                canvas.translate(941f,0f)
                canvas.scale(-1f,1f)
            }
            wingShape.reset()
            wingShape.moveTo(174f,1358f)
            wingShape.cubicTo(145f,1362f,126f,1334f,119f,1329f)
            wingShape.cubicTo(112f,1364f,130f,1398f,162f,1405f)
            wingShape.cubicTo(132f,1410f,116f,1393f,101f,1389f)
            wingShape.cubicTo(112f,1423f,147f,1436f,186f,1410f)
            wingShape.close()
            paint.reset()
            paint.isAntiAlias=true
            paint.style=Paint.Style.FILL
            paint.color=0xff09245c.toInt()
            canvas.drawPath(wingShape,paint)
            val inner=canvas.save()
            canvas.translate(0f,-4f)
            paint.color=0xffe9f6ff.toInt()
            canvas.drawPath(wingShape,paint)
            canvas.restoreToCount(inner)
            paint.style=Paint.Style.STROKE
            paint.strokeWidth=3f
            paint.color=0xff99c9ed.toInt()
            canvas.drawLine(119f,1355f,170f,1383f,paint)
            canvas.drawLine(114f,1398f,169f,1407f,paint)
            canvas.restoreToCount(save)
        }
        paint.style=Paint.Style.FILL
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
        // The owner Splash shows a genuine PERSPECTIVE trail: tiny
        // far rings on the right, larger hoops toward lower left, and
        // one oversized partly cropped ring in the extreme foreground.
        // They are decorative geometry, not earned coins.
        ring(canvas,746f,1097f,14f,23f,-18f)
        ring(canvas,666f,1081f,25f,37f,-19f)
        ring(canvas,546f,1057f,37f,54f,-21f)
        ring(canvas,350f,989f,72f,106f,-25f)
        ring(canvas,104f,830f,61f,87f,-18f)
        ring(canvas,80f,1230f,150f,190f,-21f)

        // Owner's supplied Splash has a raised, blue-tinted progress track
        // near the bottom. Its cyan animation remains indeterminate: no fake
        // completion percentage or arbitrary network-dependent wait.
        val outer=RectF(FlightLoadingLayout.track.left,
            FlightLoadingLayout.track.top,
            FlightLoadingLayout.track.right,
            FlightLoadingLayout.track.bottom)
        val radius=outer.height()*.50f
        trackWings(canvas)
        // The reference bar has a navy extrusion beneath a high-polish GOLD
        // outer rim. Keep this decoration behind the true native loading state.
        fill(canvas,RectF(outer.left-5f,outer.top+5f,
            outer.right+6f,outer.bottom+13f),0x8c072657.toInt(),radius+5f)
        fill(canvas,RectF(outer.left-3f,outer.top-4f,
            outer.right+3f,outer.bottom+7f),0xff482614.toInt(),radius+3f)
        gradient(canvas,outer,0xffffed85.toInt(),0xff9a3f0d.toInt(),radius)
        paint.reset()
        paint.isAntiAlias=true
        paint.style=Paint.Style.STROKE
        paint.strokeCap=Paint.Cap.ROUND
        paint.strokeWidth=3f
        paint.color=0xfffff6d2.toInt()
        canvas.drawArc(RectF(outer.left+6f,outer.top+3f,
            outer.right-6f,outer.bottom-6f),202f,137f,false,paint)
        paint.style=Paint.Style.FILL
        gradient(canvas,RectF(outer.left+5f,outer.top+5f,outer.right-5f,
            outer.bottom-5f),0xffffbc27.toInt(),0xff66300e.toInt(),radius-5f)
        val inner=RectF(outer.left+13f,outer.top+13f,
            outer.right-13f,outer.bottom-13f)
        gradient(canvas,inner,0xff632809.toInt(),0xff431907.toInt(),radius-13f)
        // The dark track remains unfilled while a 24%-wide scanning light
        // moves across it; the entire bar lights only after preparation.
        // Level generation has no known fractional completion: animate
        // a scanning highlight until the genuine ready signal arrives.
        val scan=FlightLoadingTrack.segment(inner.width()-6f,
            android.os.SystemClock.uptimeMillis()-scanStartedAtMs,
            preparationFinished)
        val segment=RectF(inner.left+3f+scan.offset,inner.top+4f,
            inner.left+3f+scan.offset+scan.width,inner.bottom-4f)
        gradient(canvas,segment,0xffa6ffff.toInt(),0xff00a5fd.toInt(),
            segment.height()*.5f)
        val clip=canvas.save()
        progressGlossClip.reset()
        progressGlossClip.addRoundRect(segment,segment.height()*.5f,
            segment.height()*.5f,Path.Direction.CW)
        canvas.clipPath(progressGlossClip)
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
        fill(canvas,RectF(segment.left+6f,segment.top+2f,
            segment.right-6f,segment.top+8f),
            0x55ffffff,6f)
        // Keep the owner's exact Loading... caption until navigation leaves
        // this screen; state is still conveyed truthfully by accessibility.
        outlinedText(canvas,"Loading...",
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
