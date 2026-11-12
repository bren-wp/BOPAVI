package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

/**
 * Real native Home controls on the reference's 941×1672 coordinate system.
 * Not a flattened mockup: every CTA is tappable and navigates to the game.
 * The original sky and all Bopi sprites are local Android resources.
 */
internal class HomeReferenceScreen(private val host:Activity,
                                   private val progress:ProgressStore) {
    private fun dp(px:Int)=(px*host.resources.displayMetrics.density+.5f).toInt()
    private var skinPortraitRes=R.drawable.bopiportrait0
    private fun label(s:String,font:Int,color:Int):TextView=TextView(host).apply{
        text=s
        textSize=font.toFloat()
        gravity=Gravity.CENTER
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        setTextColor(color)
        setAutoSizeTextTypeUniformWithConfiguration(12,font,1,
            android.util.TypedValue.COMPLEX_UNIT_SP)
        setShadowLayer(dp(2).toFloat(),0f,dp(3).toFloat(),0x77092050)
        importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private fun wallet(title:String,amount:Int,
                       kind:BopaviUiIconDrawable.Kind):View{
        val group=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                HomeEnamelDrawable(0xff2c5294.toInt(),0xff122c66.toInt(),
                    0xff071941.toInt(),0xff3086c7.toInt(),
                    sourceHome=true),null)
            elevation=dp(5).toFloat()
            // The outer Stage uses 941px reference units, not dp.
            setPadding(2,0,2,0)
            contentDescription="$title: $amount"
            isClickable=false
            isFocusable=false
        }
        val token=FrameLayout(host).apply{
            elevation=dp(3).toFloat()
            addView(ImageView(host).apply{
                setImageDrawable(HomeHeroIconDrawable(kind,dp(28)))
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },FrameLayout.LayoutParams(-1,-1,Gravity.CENTER))
        }
        // Width fractions are tied to the 220px reference wallet, not dp:
        // dp-sized icons overflow a 64px rail on 3x-density phones.
        group.addView(token,LinearLayout.LayoutParams(0,-1,.26f))
        group.addView(label(NumberFormat.getIntegerInstance(Locale.US).format(amount),
            24,Color.WHITE),LinearLayout.LayoutParams(0,-1,.74f))
        return group
    }

    /** Exactly three accessible native Home actions, preserving 3D artwork. */
    private fun pill(text:String,description:String,
                     kind:BopaviUiIconDrawable.Kind,
                     top:Int,bottom:Int,border:Int,
                     font:Int,onClick:()->Unit):View {
        val measure=Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        }
        val group=object:LinearLayout(host){
            override fun onMeasure(widthMeasureSpec:Int,heightMeasureSpec:Int){
                if(childCount==2){
                    val w=View.MeasureSpec.getSize(widthMeasureSpec)
                    val measuredHeight=View.MeasureSpec.getSize(heightMeasureSpec)
                    measure.textSize=minOf(font*host.resources.displayMetrics.scaledDensity,
                        measuredHeight*.71f)
                    val cluster=ReferenceButtonCluster.measure(
                        w,measuredHeight,measure.measureText(text),.61f,.29f)
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
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                HomeEnamelDrawable(top,top,bottom,border,sourceHome=true),null)
            elevation=dp(8).toFloat()
            contentDescription=description
            isClickable=true
            isFocusable=true
            minimumHeight=dp(48)
            setOnClickListener{onClick()}
            if(!progress.lessMotion())setOnTouchListener{view,event->
                when(event.actionMasked){
                    android.view.MotionEvent.ACTION_DOWN->
                        view.animate().scaleX(.975f).scaleY(.975f).setDuration(90).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(130).start()
                }
                false
            }
            setPadding(0,0,0,0)
        }
        val picture=ImageView(host).apply{
            if(kind==BopaviUiIconDrawable.Kind.SHIRT)
                setImageResource(skinPortraitRes)
            else setImageDrawable(HomeHeroIconDrawable(kind,dp(52)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            scaleX=if(kind==BopaviUiIconDrawable.Kind.SHIRT)1.22f else 1.12f
            scaleY=scaleX
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val caption=HomeEmbossedLabel(host,text,font.toFloat(),false,.71f,.94f)
        group.addView(picture,LinearLayout.LayoutParams(0,-1))
        group.addView(caption,LinearLayout.LayoutParams(0,-1))
        return group
    }

    private fun place(stage:LevelMapStage,view:View,b:HomeBounds,
                      interactive:Boolean=false,verticalAnchor:Float=.5f){
        stage.place(view,b.x,b.y,b.w,b.h,minTouch=interactive,
            referenceAligned=true,verticalAnchor=verticalAnchor)
    }

    fun create(pilotIndex:Int,onSkins:()->Unit,onShop:()->Unit,
               onSettings:()->Unit):View {
        val root=FrameLayout(host).apply{setBackgroundColor(0xff118edb.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x24ffffff,0x00ffffff,0x00ffffff,
                    0x00ffffff,0x9911130e.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        // Keep top balances anchored above the centered fantasy scene.
        val walletRail=HomeTopWalletRail(host)
        root.addView(walletRail,FrameLayout.LayoutParams(-1,-1))

        // Decorative rings are local Canvas geometry, not false pickups.
        stage.place(HomeOrbitDecoration(host),0f,0f,941f,1672f,
            referenceAligned=true)
        walletRail.attach(wallet("COINS",progress.coins(),
            BopaviUiIconDrawable.Kind.COIN),
            HomeReferenceLayout.coins)

        val logoView=ImageView(host).apply{
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        place(stage,logoView,HomeReferenceLayout.logo,verticalAnchor=.05f)
        // The wallet rail follows a notch/safe-area whereas the visual
        // logo follows the reference camera. Keep the WHOLE native logo
        // below the actual wallet row, not beneath clickable coin counters.
        walletRail.onRowBottomChanged={rowBottom->
            logoView.post{
                if(logoView.isAttachedToWindow && logoView.height>0){
                    val gap=8f*host.resources.displayMetrics.density
                    // Only shrink when the real PAINTED crown overlaps
                    // the native wallet, never for empty logo PNG padding.
                    val paintedTop=logoView.top+logoView.height*.12f
                    val placement=if(rowBottom+gap<=paintedTop)
                        HomeLogoFit(1f,0f)
                    else HomeLogoPlacement.fit(
                        rowBottom.toFloat(),logoView.top.toFloat(),
                        logoView.height.toFloat(),gap)
                    val scale=placement.scale
                    logoView.pivotX=logoView.width/2f
                    logoView.pivotY=logoView.height.toFloat()
                    // Uniform scaling preserves *all* BOPAVI letters. In an
                    // extreme notch layout a small vertical shift is safer
                    // than shrinking the brand to an invisible 0x0 point.
                    logoView.translationY=placement.offsetY
                    logoView.scaleX=scale
                    logoView.scaleY=scale
                }
            }
        }
        // All original 512×512 wing and body layers align pixel-for-pixel.
        val bodies=intArrayOf(R.drawable.bopi0,R.drawable.bopi1,R.drawable.bopi2,
            R.drawable.bopi3,R.drawable.bopi4,R.drawable.bopi5,R.drawable.bopi6,
            R.drawable.bopi7,R.drawable.bopi8,R.drawable.bopi9,R.drawable.bopi10)
        val left=intArrayOf(R.drawable.bopileft0,R.drawable.bopileft1,
            R.drawable.bopileft2,R.drawable.bopileft3,R.drawable.bopileft4,
            R.drawable.bopileft5,R.drawable.bopileft6,R.drawable.bopileft7,
            R.drawable.bopileft8,R.drawable.bopileft9,R.drawable.bopileft10)
        val right=intArrayOf(R.drawable.bopiright0,R.drawable.bopiright1,
            R.drawable.bopiright2,R.drawable.bopiright3,R.drawable.bopiright4,
            R.drawable.bopiright5,R.drawable.bopiright6,R.drawable.bopiright7,
            R.drawable.bopiright8,R.drawable.bopiright9,R.drawable.bopiright10)
        // Save-backed pilot choices 0..3 resolve to real sprite IDs 0,9,10,8;
        // the fourth choice is the existing Orion collectible appearance.
        val chosen=PilotSelectionModel.spriteIdForChoice(pilotIndex)
        val portraits=intArrayOf(R.drawable.bopiportrait0,
            R.drawable.bopiportrait9,R.drawable.bopiportrait10,
            R.drawable.bopiportrait8)
        skinPortraitRes=portraits[pilotIndex.coerceIn(0,3)]
        // Preserve the original sprite body + wing layers; only wings
        // animate and the actual collision geometry remains unchanged.
        // The actor stops all animators when the window is hidden/detached.
        val actor=HomeWingActor(host,
            left[chosen],right[chosen],bodies[chosen],
            allowMotion=!progress.lessMotion())
        place(stage,actor,HomeReferenceLayout.hero,verticalAnchor=.25f)

        place(stage,pill("SKINS","Otvori izbor likova",
            BopaviUiIconDrawable.Kind.SHIRT,
            0xffe671ff.toInt(),0xff9110e2.toInt(),0xff5e128e.toInt(),27,onSkins),
            HomeReferenceLayout.skins,true,verticalAnchor=.62f)
        place(stage,pill("SHOP","Otvori trgovinu nadogradnji",
            BopaviUiIconDrawable.Kind.SHOP,
            0xffb4ff65.toInt(),0xff2abe1c.toInt(),0xff1a7213.toInt(),28,onShop),
            HomeReferenceLayout.shop,true,verticalAnchor=.62f)
        place(stage,pill("SETTINGS","Otvori opcije igre",
            BopaviUiIconDrawable.Kind.SETTINGS,
            0xff76e5ff.toInt(),0xff096fd8.toInt(),0xff0c347d.toInt(),22,onSettings),
            HomeReferenceLayout.settings,true,verticalAnchor=.73f)

        stage.setOnApplyWindowInsetsListener{_,insets->
            if(Build.VERSION.SDK_INT>=30){
                val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
                stage.setSafe(bars.left,bars.top,bars.right,bars.bottom)
                // A hidden status bar must not pull the full-screen
                // Home currency HUD down to its invisible inset height.
                // Explicit cutout bounds are checked per wallet instead.
                val statusTop=if(insets.isVisible(
                    android.view.WindowInsets.Type.statusBars()))
                    insets.getInsets(
                        android.view.WindowInsets.Type.statusBars()).top
                    else 0
                walletRail.setSafe(bars.left,statusTop,bars.right,
                    insets.displayCutout?.boundingRects.orEmpty())
            }else{
                @Suppress("DEPRECATION")
                stage.setSafe(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
                walletRail.setSafe(insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,insets.systemWindowInsetRight)
            }
            insets
        }
        stage.requestApplyInsets()
        return root
    }
}

/** Layered candy hoops, sky glints and breeze trails behind live Home controls. */
