package com.brendigo.bopavi

import android.app.Activity
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * Loss-only native BOPAVI result screen. Coordinate targets follow the
 * owner's 941×1672 Game Over reference; no screenshot is used as a UI.
 * Buttons and row values stay real Android views for accessibility.
 */
internal class GameOverScreen(private val host:Activity) {
    private fun dp(px:Int)=((px*host.resources.displayMetrics.density)+.5f).toInt()

    private fun statRow(label:String,value:String,icon:BopaviUiIconDrawable.Kind):View {
        val row=LinearLayout(host).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(8,0,8,0)
            background=ResultRowEnamelDrawable()
            contentDescription="$label: $value"
            isFocusable=true
        }
        // Reference-size rows are ~90px high. A 65dp icon holder is 130px
        // on the API35 320dpi QA emulator and visibly overlaps native text.
        row.addView(ImageView(host).apply{
            // The red distance marker and genuine collectible pictograms
            // use the same three-dimensional vector enamel as Home/Shop.
            setImageDrawable(HomeHeroIconDrawable(icon,dp(28)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(0,-1,.15f))
        row.addView(ResultStatText(host,label,false).apply{
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(0,-1,.55f))
        row.addView(ResultStatText(host,value,true).apply{
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(0,-1,.30f))
        return row
    }

    /** A single full-size native touch target with a compact raised pair. */
    private fun action(label:String,primary:Boolean,onClick:()->Unit):View {
        val textMeasure=Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface=android.graphics.Typeface.create(
                "sans-serif-black",android.graphics.Typeface.BOLD)
        }
        return object:LinearLayout(host) {
            override fun onMeasure(widthMeasureSpec:Int,heightMeasureSpec:Int) {
                if(childCount==2) {
                    val w=View.MeasureSpec.getSize(widthMeasureSpec)
                    val h=View.MeasureSpec.getSize(heightMeasureSpec)
                    textMeasure.textSize=minOf(
                        60f*host.resources.displayMetrics.scaledDensity,h*.68f)
                    val cluster=ReferenceButtonCluster.measure(
                        w,h,textMeasure.measureText(label),.70f,.27f)
                    (getChildAt(0).layoutParams as LinearLayout.LayoutParams).apply {
                        width=cluster.iconWidth
                        height=LinearLayout.LayoutParams.MATCH_PARENT
                        weight=0f
                    }
                    (getChildAt(1).layoutParams as LinearLayout.LayoutParams).apply {
                        width=cluster.labelWidth
                        height=LinearLayout.LayoutParams.MATCH_PARENT
                        marginStart=cluster.gap
                        weight=0f
                    }
                }
                super.onMeasure(widthMeasureSpec,heightMeasureSpec)
            }
        }.apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
            background=android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(0x44ffffff),
                ResultActionEnamelDrawable(primary),null)
            contentDescription=label
            elevation=dp(6).toFloat()
            minimumHeight=dp(48)
            isClickable=true
            isFocusable=true
            addView(ImageView(host).apply {
                setImageDrawable(HomeHeroIconDrawable(
                    if(primary)BopaviUiIconDrawable.Kind.RESTART
                    else BopaviUiIconDrawable.Kind.HOME,100))
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(0,-1))
            addView(HomeEmbossedLabel(host,label,60f,primary).apply {
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(0,-1))
            setOnClickListener{onClick()}
            // The entire native hit target remains clickable. This short
            // pressure response never replaces onClick or changes its bounds.
            setOnTouchListener{v,event->
                if(android.animation.ValueAnimator.areAnimatorsEnabled()){
                    when(event.actionMasked){
                        android.view.MotionEvent.ACTION_DOWN->
                            v.animate().scaleX(.985f).scaleY(.975f)
                                .setDuration(85L).start()
                        android.view.MotionEvent.ACTION_UP,
                        android.view.MotionEvent.ACTION_CANCEL->
                            v.animate().scaleX(1f).scaleY(1f)
                                .setDuration(125L).start()
                    }
                }
                false
            }
        }
    }

    fun create(worldDrawable:Int,pilotDrawable:Int,stats:GameOverStats,
               onRetry:()->Unit,onHome:()->Unit):View {
        val root=FrameLayout(host).apply{setBackgroundColor(0xff278bdb.toInt())}
        // Sky Meadows' loss backdrop uses the richly decorated Home
        // fantasy islands, while other worlds keep their own biomes.
        val lossBackdrop=if(worldDrawable==R.drawable.world0)
            R.drawable.homeback else worldDrawable
        root.addView(ImageView(host).apply{
            setImageResource(lossBackdrop)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x88092358.toInt(),0x332b70b5,
                    0x11ffb7ac,0x77432a65))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        // Original local wood and leaves sit behind the raised wordmark.
        stage.place(GameOverTitlePlank(host),198f,333f,545f,135f,
            referenceAligned=true)
        stage.place(ResultWingDecoration(host),145f,177f,650f,165f,
            referenceAligned=true)
        stage.place(GameOverCrown(host),431f,53f,79f,76f,
            referenceAligned=true)
        stage.place(ResultEmblemLabel(host,"GAME",97f,
            ResultEmblemLabel.Tone.ICE).apply {
            contentDescription="Game Over"
        },171f,129f,599f,146f,referenceAligned=true)
        stage.place(ResultEmblemLabel(host,"OVER",103f,
            ResultEmblemLabel.Tone.CORAL).apply {
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },153f,267f,635f,166f,referenceAligned=true)

        // Painted floating grass/stone podium stays behind the actual Bopi.
        stage.place(GameOverPodium(host),168f,728f,605f,216f,
            referenceAligned=true)
        // Separate ambient feathers stay decorative, outside touch controls.
        stage.place(GameOverFeathers(host),125f,406f,690f,508f,
            referenceAligned=true)
        // Separate soft contact shadow adds depth without replacing the
        // portrait with a flattened screenshot or modifying result data.
        stage.place(View(host).apply{
            background=GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x770b2144,0x220b2144)).apply {
                shape=GradientDrawable.OVAL
            }
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },299f,749f,344f,55f,referenceAligned=true)
        // The classic blue aviator needs a true seated Game Over pose.
        // Keep the selected portrait for alternate pilots until each has
        // an approved dedicated seated illustration of its own.
        val resultPilot:View=if(pilotDrawable==R.drawable.bopiportrait0)
            GameOverSeatedBopi(host)
        else ImageView(host).apply{
            setImageResource(pilotDrawable)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        stage.place(resultPilot,190f,393f,560f,497f,
            referenceAligned=true)

        val statistics=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(12,14,12,14)
            background=GameOverScoreFrameDrawable()
            contentDescription="Game Over statistics"
        }
        val rows=listOf(
            statRow("Distance",stats.distance,BopaviUiIconDrawable.Kind.GATE),
            statRow(stats.collectedLabel,stats.collected,
                if(stats.collectedLabel=="Food Collected")
                    BopaviUiIconDrawable.Kind.BERRY
                else BopaviUiIconDrawable.Kind.CROWN_COIN),
            statRow(stats.thirdLabel,stats.third,
                if(stats.thirdLabel=="Stars Collected")
                    BopaviUiIconDrawable.Kind.STAR
                else BopaviUiIconDrawable.Kind.RECORDS)
        )
        for(item in rows){
            statistics.addView(item,LinearLayout.LayoutParams(-1,0,1f).apply {
                setMargins(0,3,0,3)
            })
        }
        stage.place(statistics,123f,927f,695f,310f,
            referenceAligned=true)

        stage.place(action("RETRY",true,onRetry),183f,1233f,575f,150f,
            minTouch=true,referenceAligned=true)
        stage.place(action("HOME",false,onHome),241f,1404f,459f,124f,
            minTouch=true,referenceAligned=true)
        stage.setOnApplyWindowInsetsListener{ _,insets ->
            if(Build.VERSION.SDK_INT>=30){
                val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
                stage.setSafe(safe.left,safe.top,safe.right,safe.bottom)
            } else {
                @Suppress("DEPRECATION")
                stage.setSafe(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
            }
            insets
        }
        stage.requestApplyInsets()
        return root
    }
}

/** Decorative original vector geometry, never a clickable fake image. */
