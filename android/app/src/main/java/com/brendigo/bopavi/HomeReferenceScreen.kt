package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
    private fun glyph(kind:BopaviUiIconDrawable.Kind,color:Int,pixels:Int):ImageView=
        ImageView(host).apply {
            setImageDrawable(BopaviUiIconDrawable(kind,color,dp(pixels)))
            scaleType=ImageView.ScaleType.CENTER_INSIDE
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

    private fun wallet(title:String,amount:Int,kind:BopaviUiIconDrawable.Kind,
                       destination:String,onClick:()->Unit):View{
        val group=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                HomeEnamelDrawable(0xff2b91f5.toInt(),0xff1b62bb.toInt(),
                    0xff0c285c.toInt(),0xff9ae3ff.toInt()),null)
            elevation=dp(5).toFloat()
            setPadding(dp(4),0,dp(4),0)
            contentDescription="$title: $amount. $destination"
            isClickable=true;isFocusable=true
            setOnClickListener{onClick()}
        }
        val token=FrameLayout(host).apply{
            if(kind==BopaviUiIconDrawable.Kind.COIN){
                background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xfffff3a5.toInt(),0xffffae00.toInt(),
                        0xffe06a04.toInt())).apply{
                    shape=GradientDrawable.OVAL
                    setStroke(dp(2),0xfffff8cb.toInt())
                }
            }
            elevation=dp(3).toFloat()
            addView(glyph(kind,if(kind==BopaviUiIconDrawable.Kind.STAR)
                0xffffd025.toInt() else 0xff985015.toInt(),
                if(kind==BopaviUiIconDrawable.Kind.STAR)40 else 26),
                FrameLayout.LayoutParams(dp(if(kind==BopaviUiIconDrawable.Kind.STAR)
                    40 else 28),dp(if(kind==BopaviUiIconDrawable.Kind.STAR)
                    40 else 28),Gravity.CENTER))
        }
        group.addView(token,LinearLayout.LayoutParams(dp(40),dp(40)))
        group.addView(label(NumberFormat.getIntegerInstance(Locale.US).format(amount),
            24,Color.WHITE),LinearLayout.LayoutParams(0,-1,1f))
        group.addView(label("+",22,Color.WHITE).apply{
            background=HomeEnamelDrawable(0xffbaff8f.toInt(),
                0xff21df28.toInt(),0xff078224.toInt(),0xffd2ffb4.toInt())
        },LinearLayout.LayoutParams(dp(27),dp(31)))
        return group
    }

    /** Glossy raised 3D CTA with native hit target, icon and label. */
    private fun pill(text:String,description:String,
                     kind:BopaviUiIconDrawable.Kind,
                     top:Int,bottom:Int,border:Int,
                     font:Int,onClick:()->Unit):View {
        val isPlay=kind==BopaviUiIconDrawable.Kind.PLAY
        val group=LinearLayout(host).apply{
            orientation=if(isPlay)LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                HomeEnamelDrawable(top,if(isPlay)0xffffaf12.toInt() else top,
                    bottom,border),null)
            elevation=dp(8).toFloat()
            contentDescription=description
            isClickable=true;isFocusable=true
            minimumHeight=dp(48)
            setOnClickListener{onClick()}
            setOnTouchListener{view,event->
                when(event.actionMasked){
                    android.view.MotionEvent.ACTION_DOWN->
                        view.animate().scaleX(.975f).scaleY(.975f).setDuration(90).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL->
                        view.animate().scaleX(1f).scaleY(1f).setDuration(130).start()
                }
                false
            }
            setPadding(dp(if(isPlay)29 else 8),dp(if(isPlay)6 else 9),
                dp(if(isPlay)29 else 8),dp(if(isPlay)16 else 14))
        }
        val ink=Color.WHITE
        val imageSize=if(isPlay)72 else 53
        val symbol=glyph(kind,ink,imageSize)
        if(isPlay){
            group.addView(symbol,LinearLayout.LayoutParams(dp(83),dp(78)).apply{
                rightMargin=dp(11)
            })
            group.addView(label(text,font,ink),LinearLayout.LayoutParams(0,-1,1f))
        }else{
            group.addView(symbol,LinearLayout.LayoutParams(dp(62),0,1f))
            group.addView(label(text,font,ink),LinearLayout.LayoutParams(-1,dp(37)))
        }
        return group
    }

    private fun place(stage:LevelMapStage,view:View,b:HomeBounds,interactive:Boolean=false){
        stage.place(view,b.x,b.y,b.w,b.h,minTouch=interactive)
    }

    fun create(pilot:Int,pilotIndex:Int,onPlay:()->Unit,onSkins:()->Unit,onShop:()->Unit,
               onSettings:()->Unit,onAchievements:()->Unit,
               onLeaderboard:()->Unit,onSound:()->Unit,
               onCoins:()->Unit,onStars:()->Unit):View {
        val root=FrameLayout(host).apply{setBackgroundColor(0xff118edb.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x180e3e83,0x0062d2ff,0x55092c43))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))

        // Decorative rings are local Canvas geometry, not false pickups.
        stage.place(HomeOrbitDecoration(host),0f,0f,941f,1672f)
        place(stage,wallet("COINS",progress.coins(),
            BopaviUiIconDrawable.Kind.COIN,"Otvori trgovinu",onCoins),
            HomeReferenceLayout.coins,true)
        val earnedStars=(0 until LevelEngine.WORLD_COUNT)
            .sumOf{progress.worldStars(it)}
        place(stage,wallet("STARS",earnedStars,
            BopaviUiIconDrawable.Kind.STAR,"Otvori kartu razina",onStars),
            HomeReferenceLayout.stars,true)

        place(stage,ImageView(host).apply{
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },HomeReferenceLayout.logo)
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
        val chosen=pilotIndex.coerceIn(0,bodies.lastIndex)
        val actor=FrameLayout(host).apply {
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        for(sprite in intArrayOf(left[chosen],right[chosen],bodies[chosen])){
            actor.addView(ImageView(host).apply{
                setImageResource(sprite)
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },FrameLayout.LayoutParams(-1,-1))
        }
        place(stage,actor,HomeReferenceLayout.hero)

        place(stage,pill("PLAY","▶  PLAY",
            BopaviUiIconDrawable.Kind.PLAY,
            0xffffe75f.toInt(),0xffff9206.toInt(),0xffa35011.toInt(),46,onPlay),
            HomeReferenceLayout.play,true)
        place(stage,pill("SKINS","Otvori izbor likova",
            BopaviUiIconDrawable.Kind.SHIRT,
            0xff51d8ff.toInt(),0xff0077de.toInt(),0xffa7f6ff.toInt(),28,onSkins),
            HomeReferenceLayout.skins,true)
        place(stage,pill("SHOP","Otvori trgovinu nadogradnji",
            BopaviUiIconDrawable.Kind.SHOP,
            0xfff152ff.toInt(),0xff7723d8.toInt(),0xffffc1ff.toInt(),29,onShop),
            HomeReferenceLayout.shop,true)
        place(stage,pill("SETTINGS","Otvori opcije igre",
            BopaviUiIconDrawable.Kind.SETTINGS,
            0xff39d3ff.toInt(),0xff0655c6.toInt(),0xffc5f7ff.toInt(),24,onSettings),
            HomeReferenceLayout.settings,true)

        place(stage,pill("ACHIEVEMENTS","Pogledaj svoja postignuća",
            BopaviUiIconDrawable.Kind.RECORDS,
            0xff17d958.toInt(),0xff057d29.toInt(),0xffc7ffad.toInt(),22,
            onAchievements),HomeReferenceLayout.achievements,true)
        // The original Home image uses a two-card final row. Retain the
        // previous Records feature as a small visible trophy badge inside the
        // Achievements card, with its own real 48dp+ touch/accessibility target.
        val records=FrameLayout(host).apply {
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                HomeEnamelDrawable(0xfffff48d.toInt(),0xffffbb12.toInt(),
                    0xffbd650b.toInt(),0xffffeeb2.toInt()),null)
            isClickable=true;isFocusable=true
            contentDescription="Pogledaj svoje rekorde"
            setOnClickListener{onLeaderboard()}
            elevation=dp(11).toFloat()
            addView(glyph(BopaviUiIconDrawable.Kind.LEADERBOARD,
                Color.WHITE,38),FrameLayout.LayoutParams(dp(39),dp(39),
                Gravity.CENTER))
        }
        place(stage,records,HomeReferenceLayout.leaderboard,true)
        place(stage,pill("SOUND","Otvori zvučne postavke",
            BopaviUiIconDrawable.Kind.SOUND,
            0xff1bd4ff.toInt(),0xff0866c8.toInt(),0xffc3f7ff.toInt(),27,
            onSound),HomeReferenceLayout.sound,true)
        stage.setOnApplyWindowInsetsListener{_,insets->
            if(Build.VERSION.SDK_INT>=30){
                val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
                stage.setSafe(bars.left,bars.top,bars.right,bars.bottom)
            }else{
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

/** Gold perspective rings and breeze trails, painted offline behind live CTAs. */
internal class HomeOrbitDecoration(host:Activity):View(host){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    private fun ring(canvas:Canvas,x:Float,y:Float,rx:Float,ry:Float,angle:Float){
        val save=canvas.save()
        canvas.rotate(angle,x,y)
        val area=RectF(x-rx,y-ry,x+rx,y+ry)
        paint.reset()
        paint.isAntiAlias=true
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=10f
        paint.color=0xffb96b0e.toInt()
        canvas.drawOval(area,paint)
        paint.strokeWidth=6f
        paint.color=0xffffd348.toInt()
        canvas.drawOval(area,paint)
        paint.strokeWidth=2.5f
        paint.color=0xfffffaac.toInt()
        canvas.drawArc(area,210f,105f,false,paint)
        canvas.restoreToCount(save)
    }
    override fun onDraw(canvas:Canvas){
        val saved=canvas.save()
        canvas.scale(width/941f,height/1672f)
        paint.reset()
        paint.isAntiAlias=true
        paint.color=0x88b5f8ff.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=7f
        path.reset()
        path.moveTo(180f,773f)
        path.cubicTo(227f,831f,315f,861f,391f,862f)
        canvas.drawPath(path,paint)
        paint.color=0x55ffffff
        paint.strokeWidth=4f
        canvas.drawArc(RectF(80f,538f,815f,902f),25f,76f,false,paint)
        ring(canvas,755f,661f,25f,55f,24f)
        ring(canvas,815f,713f,17f,41f,20f)
        ring(canvas,865f,738f,13f,29f,16f)
        canvas.restoreToCount(saved)
    }
}
