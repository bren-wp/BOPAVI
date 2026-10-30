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
    private val navy=0xff092654.toInt()
    private fun dp(px:Int)=(px*host.resources.displayMetrics.density+.5f).toInt()
    private fun plate(top:Int,bottom:Int,outline:Int,radius:Int)=
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(top,bottom)).apply{
            cornerRadius=dp(radius).toFloat()
            setStroke(dp(3),outline)
        }
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
                plate(0xff266cbe.toInt(),0xff051b46.toInt(),
                    0xff80d4ff.toInt(),20),null)
            setPadding(dp(4),0,dp(4),0)
            contentDescription="$title: $amount. $destination"
            isClickable=true;isFocusable=true
            setOnClickListener{onClick()}
        }
        group.addView(glyph(kind,
            if(kind==BopaviUiIconDrawable.Kind.COIN)0xffffc634.toInt()
            else 0xffdb66ff.toInt(),29),
            LinearLayout.LayoutParams(dp(37),dp(37)))
        group.addView(label(NumberFormat.getIntegerInstance(Locale.US).format(amount),
            18,Color.WHITE),LinearLayout.LayoutParams(0,-1,1f))
        group.addView(label("+",22,Color.WHITE).apply{
            background=plate(0xff8af55e.toInt(),0xff16a32a.toInt(),
                0xff0e6525.toInt(),11)
        },LinearLayout.LayoutParams(dp(27),dp(31)))
        return group
    }

    private fun pill(text:String,description:String,
                     kind:BopaviUiIconDrawable.Kind,
                     top:Int,bottom:Int,border:Int,
                     font:Int,onClick:()->Unit):View {
        val group=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                plate(top,bottom,border,35),null)
            elevation=dp(5).toFloat()
            contentDescription=description
            isClickable=true;isFocusable=true
            setOnClickListener{onClick()}
            setPadding(dp(9),0,dp(9),0)
        }
        group.addView(glyph(kind,
            if(kind==BopaviUiIconDrawable.Kind.PLAY)0xff75420e.toInt()
            else Color.WHITE,if(kind==BopaviUiIconDrawable.Kind.PLAY)45 else 30),
            LinearLayout.LayoutParams(dp(if(kind==BopaviUiIconDrawable.Kind.PLAY)71 else 53),
                dp(if(kind==BopaviUiIconDrawable.Kind.PLAY)71 else 54)))
        group.addView(label(text,font,Color.WHITE),
            LinearLayout.LayoutParams(0,-1,1f))
        return group
    }
    private fun footer(name:String,description:String,
                       kind:BopaviUiIconDrawable.Kind,onClick:()->Unit):View{
        return LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            contentDescription=description
            isClickable=true;isFocusable=true
            setOnClickListener{onClick()}
            addView(glyph(kind,0xffffd34e.toInt(),42).apply {
                background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xff2463b2.toInt(),0xff061c45.toInt())).apply {
                    shape=GradientDrawable.OVAL
                    setStroke(dp(3),0xffffd56d.toInt())
                }
            },LinearLayout.LayoutParams(dp(61),dp(61)))
            addView(label(name,14,Color.WHITE),
                LinearLayout.LayoutParams(-1,dp(33)))
        }
    }
    private fun place(stage:LevelMapStage,view:View,b:HomeBounds,interactive:Boolean=false){
        stage.place(view,b.x,b.y,b.w,b.h,minTouch=interactive)
    }

    fun create(pilot:Int,onPlay:()->Unit,onSkins:()->Unit,onShop:()->Unit,
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
        place(stage,ImageView(host).apply{
            setImageResource(pilot)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },HomeReferenceLayout.hero)

        place(stage,pill("PLAY","▶  PLAY",
            BopaviUiIconDrawable.Kind.PLAY,
            0xffffe75f.toInt(),0xffff9206.toInt(),0xffa35011.toInt(),46,onPlay),
            HomeReferenceLayout.play,true)
        place(stage,pill("SKINS","Otvori izbor likova",
            BopaviUiIconDrawable.Kind.FLIGHT,
            0xffbf47ff.toInt(),0xff690cbd.toInt(),0xffddb8ff.toInt(),26,onSkins),
            HomeReferenceLayout.skins,true)
        place(stage,pill("SHOP","Otvori trgovinu nadogradnji",
            BopaviUiIconDrawable.Kind.SHOP,
            0xff7eeb48.toInt(),0xff0e9b29.toInt(),0xffbffca3.toInt(),26,onShop),
            HomeReferenceLayout.shop,true)
        place(stage,pill("SETTINGS","Otvori opcije igre",
            BopaviUiIconDrawable.Kind.SETTINGS,
            0xff42d4ff.toInt(),0xff076ecf.toInt(),0xffb2f7ff.toInt(),25,onSettings),
            HomeReferenceLayout.settings,true)

        place(stage,footer("ACHIEVEMENTS","Pogledaj svoja postignuća",
            BopaviUiIconDrawable.Kind.RECORDS,onAchievements),
            HomeReferenceLayout.achievements,true)
        place(stage,footer("LEADERBOARD","Pogledaj svoje rekorde",
            BopaviUiIconDrawable.Kind.LEADERBOARD,onLeaderboard),
            HomeReferenceLayout.leaderboard,true)
        place(stage,footer("SOUND","Otvori zvučne postavke",
            BopaviUiIconDrawable.Kind.SOUND,onSound),
            HomeReferenceLayout.sound,true)
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
