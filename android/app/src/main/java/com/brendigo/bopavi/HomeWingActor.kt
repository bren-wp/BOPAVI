package com.brendigo.bopavi

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Home-only motion on the original registered transparent Bopi wing layers.
 *
 * The body and reference coordinates never move. Android's reduced-motion
 * setting is respected, and infinite animators stop while the window is
 * invisible or detached so background navigation does not leak animation.
 */
internal class HomeWingActor(
    context:Context,
    leftRes:Int,
    rightRes:Int,
    bodyRes:Int,
    private val allowMotion:Boolean=true
):FrameLayout(context) {
    private fun layer(res:Int):ImageView=ImageView(context).apply {
        setImageResource(res)
        scaleType=ImageView.ScaleType.FIT_CENTER
        importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private val leftWing=layer(leftRes)
    private val rightWing=layer(rightRes)
    private val body=layer(bodyRes)
    private val hasOwnerAviator=bodyRes==R.drawable.bopi0
    private fun accessory(front:Boolean):View=object:View(context){
        init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
        override fun onDraw(canvas:Canvas){
            val box=RectF(0f,0f,width.toFloat(),height.toFloat())
            if(front)BopiOwnerAccessories.drawFront(canvas,box)
            else BopiOwnerAccessories.drawBack(canvas,box)
        }
    }
    private val leftFlap=ObjectAnimator.ofFloat(leftWing,View.ROTATION,-3.5f,4f).apply {
        duration=840L
        repeatCount=ValueAnimator.INFINITE
        repeatMode=ValueAnimator.REVERSE
        interpolator=AccelerateDecelerateInterpolator()
    }
    private val rightFlap=ObjectAnimator.ofFloat(rightWing,View.ROTATION,3.5f,-4f).apply {
        duration=840L
        repeatCount=ValueAnimator.INFINITE
        repeatMode=ValueAnimator.REVERSE
        interpolator=AccelerateDecelerateInterpolator()
    }

    init {
        importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        if(hasOwnerAviator)addView(accessory(false),LayoutParams(-1,-1))
        addView(leftWing,LayoutParams(-1,-1))
        addView(rightWing,LayoutParams(-1,-1))
        addView(body,LayoutParams(-1,-1))
        if(hasOwnerAviator)addView(accessory(true),LayoutParams(-1,-1))
    }

    private fun stopFlapping() {
        leftFlap.cancel()
        rightFlap.cancel()
        leftWing.rotation=0f
        rightWing.rotation=0f
    }

    private fun syncFlapping() {
        if(allowMotion && isAttachedToWindow && windowVisibility==View.VISIBLE &&
            ValueAnimator.areAnimatorsEnabled()) {
            if(!leftFlap.isStarted)leftFlap.start()
            if(!rightFlap.isStarted)rightFlap.start()
        } else {
            stopFlapping()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        syncFlapping()
    }

    override fun onWindowVisibilityChanged(visibility:Int) {
        super.onWindowVisibilityChanged(visibility)
        syncFlapping()
    }

    override fun onDetachedFromWindow() {
        stopFlapping()
        super.onDetachedFromWindow()
    }
}
