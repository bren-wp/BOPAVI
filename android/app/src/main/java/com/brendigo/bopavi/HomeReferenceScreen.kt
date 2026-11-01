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
            elevation=dp(3).toFloat()
            addView(ImageView(host).apply{
                setImageDrawable(HomeHeroIconDrawable(kind,dp(28)))
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },FrameLayout.LayoutParams(dp(28),dp(28),Gravity.CENTER))
        }
        group.addView(token,LinearLayout.LayoutParams(dp(28),dp(28)))
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
        val isWide=isPlay || kind==BopaviUiIconDrawable.Kind.SHIRT ||
            kind==BopaviUiIconDrawable.Kind.SHOP ||
            kind==BopaviUiIconDrawable.Kind.SETTINGS
        val group=LinearLayout(host).apply{
            orientation=if(isWide)LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
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
        val imageSize=if(isPlay)88 else 52
        val symbol=ImageView(host).apply{
            setImageDrawable(HomeHeroIconDrawable(kind,dp(imageSize)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val title=HomeEmbossedLabel(host,text,font.toFloat(),isPlay)
        if(isWide){
            group.addView(symbol,LinearLayout.LayoutParams(
                dp(if(isPlay)93 else 59),-1).apply { rightMargin=dp(6) })
            group.addView(title,LinearLayout.LayoutParams(0,-1,1f))
        }else{
            group.addView(symbol,LinearLayout.LayoutParams(dp(69),0,1f))
            group.addView(title,LinearLayout.LayoutParams(-1,dp(40)))
        }
        return group
    }

    /** Three actual Home shortcuts. Their icons are painted as circles,
     * but every shortcut is one native accessible click target. */
    private fun footerShortcut(text:String,description:String,
        kind:BopaviUiIconDrawable.Kind,onClick:()->Unit):View {
        val control=LinearLayout(host).apply {
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            contentDescription=description
            isClickable=true;isFocusable=true
            minimumHeight=dp(48)
            setOnClickListener { onClick() }
        }
        val round=FrameLayout(host).apply {
            isDuplicateParentStateEnabled=true
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xff225caa.toInt(),0xff061c51.toInt())).apply {
                    shape=GradientDrawable.OVAL
                    setStroke(dp(3),0xffffcf63.toInt())
                },null)
            elevation=dp(4).toFloat()
            addView(glyph(kind,Color.WHITE,36),
                FrameLayout.LayoutParams(dp(38),dp(38),Gravity.CENTER))
        }
        control.addView(round,LinearLayout.LayoutParams(dp(48),0,1f))
        control.addView(HomeEmbossedLabel(host,text,15f,false),
            LinearLayout.LayoutParams(-1,dp(29)))
        return control
    }

    private fun place(stage:LevelMapStage,view:View,b:HomeBounds,interactive:Boolean=false){
        stage.place(view,b.x,b.y,b.w,b.h,minTouch=interactive)
    }

    fun create(pilotIndex:Int,onPlay:()->Unit,onSkins:()->Unit,onShop:()->Unit,
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
        // Preserve the three registered original sprite layers; only the
        // transparent wings animate, never the painted physical collider.
        // The actor stops all animators when the window is hidden/detached.
        val actor=HomeWingActor(host,
            left[chosen],right[chosen],bodies[chosen])
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

        place(stage,footerShortcut("ACHIEVEMENTS",
            "Pogledaj svoja postignuća",BopaviUiIconDrawable.Kind.RECORDS,
            onAchievements),HomeReferenceLayout.achievements,true)
        place(stage,footerShortcut("LEADERBOARD",
            "Pogledaj svoje rekorde",BopaviUiIconDrawable.Kind.LEADERBOARD,
            onLeaderboard),HomeReferenceLayout.leaderboard,true)
        place(stage,footerShortcut("SOUND",
            "Otvori zvučne postavke",BopaviUiIconDrawable.Kind.SOUND,
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

/** Layered candy hoops, sky glints and breeze trails behind live Home controls. */
internal class HomeOrbitDecoration(host:Activity):View(host){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}

    private fun sparkle(canvas:Canvas,x:Float,y:Float,r:Float){
        path.reset()
        path.moveTo(x,y-r*1.6f)
        path.quadTo(x+r*.18f,y-r*.19f,x+r,y)
        path.quadTo(x+r*.18f,y+r*.19f,x,y+r*1.6f)
        path.quadTo(x-r*.18f,y+r*.19f,x-r,y)
        path.quadTo(x-r*.18f,y-r*.19f,x,y-r*1.6f)
        path.close()
        paint.reset()
        paint.isAntiAlias=true
        paint.color=0x99b6eeff.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=7f
        canvas.drawPath(path,paint)
        paint.style=Paint.Style.FILL
        paint.color=Color.WHITE
        canvas.drawPath(path,paint)
    }

    private fun star(canvas:Canvas,x:Float,y:Float,r:Float){
        val shape=Path()
        for(i in 0 until 10){
            val theta=-Math.PI/2.0 + i*Math.PI/5.0
            val radius=if(i%2==0)r else r*.47f
            val px=x+radius*kotlin.math.cos(theta).toFloat()
            val py=y+radius*kotlin.math.sin(theta).toFloat()
            if(i==0)shape.moveTo(px,py) else shape.lineTo(px,py)
        }
        shape.close()
        paint.reset()
        paint.isAntiAlias=true
        paint.color=0xff945318.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=8f
        canvas.drawPath(shape,paint)
        paint.style=Paint.Style.FILL
        paint.shader=android.graphics.LinearGradient(x,y-r,x,y+r,
            0xfffff89c.toInt(),0xffffa400.toInt(),
            android.graphics.Shader.TileMode.CLAMP)
        canvas.drawPath(shape,paint)
        paint.shader=null
        paint.color=0xaaffffff.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=2.5f
        canvas.drawLine(x-r*.25f,y-r*.40f,x+r*.23f,y-r*.37f,paint)
    }

    private fun ring(canvas:Canvas,x:Float,y:Float,rx:Float,ry:Float,angle:Float){
        val saved=canvas.save()
        canvas.rotate(angle,x,y)
        val oval=RectF(x-rx,y-ry,x+rx,y+ry)
        paint.reset()
        paint.isAntiAlias=true
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=rx*.16f
        paint.color=0x66032866
        canvas.drawOval(RectF(oval.left+9f,oval.top+12f,
            oval.right+9f,oval.bottom+12f),paint)
        paint.strokeWidth=rx*.15f
        paint.color=0xffad601c.toInt()
        canvas.drawOval(oval,paint)
        paint.strokeWidth=rx*.12f
        paint.color=0xffffe6b2.toInt()
        canvas.drawOval(oval,paint)
        paint.strokeWidth=rx*.105f
        paint.color=0xffd94746.toInt()
        paint.pathEffect=android.graphics.DashPathEffect(floatArrayOf(
            rx*.28f,rx*.20f),0f)
        canvas.drawOval(oval,paint)
        paint.pathEffect=null
        paint.strokeWidth=rx*.035f
        paint.color=0xfffffbcd.toInt()
        canvas.drawArc(oval,206f,138f,false,paint)
        canvas.restoreToCount(saved)
    }

    override fun onDraw(canvas:Canvas){
        val saved=canvas.save()
        canvas.scale(width/941f,height/1672f)
        paint.reset()
        paint.isAntiAlias=true
        paint.color=0x88e7ffff.toInt()
        paint.style=Paint.Style.STROKE
        paint.strokeCap=Paint.Cap.ROUND
        paint.strokeWidth=10f
        path.reset()
        path.moveTo(90f,848f)
        path.cubicTo(208f,708f,255f,857f,380f,820f)
        canvas.drawPath(path,paint)
        paint.color=0x88ffffff.toInt()
        paint.strokeWidth=4f
        canvas.drawArc(RectF(110f,475f,830f,911f),25f,81f,false,paint)
        // Hero reference: one large right candy hoop and two receding hoops.
        ring(canvas,809f,744f,143f,170f,-7f)
        ring(canvas,626f,818f,52f,69f,-11f)
        ring(canvas,530f,883f,29f,43f,-17f)
        star(canvas,803f,577f,34f)
        star(canvas,622f,757f,14f)
        sparkle(canvas,264f,417f,13f)
        sparkle(canvas,124f,726f,10f)
        sparkle(canvas,182f,791f,6f)
        canvas.restoreToCount(saved)
    }
}
