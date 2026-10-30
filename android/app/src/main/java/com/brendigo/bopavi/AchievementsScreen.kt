package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

/** Real Android achievements gallery; every goal is a live local counter. */
internal class AchievementsScreen(private val host:Activity){
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()
    private val ink=0xff0a2856.toInt()
    private fun plate(a:Int,b:Int,c:Int):GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(a,b)).apply {
            cornerRadius=dp(20).toFloat()
            setStroke(dp(2),c)
        }
    private fun label(s:String,size:Int,color:Int):TextView=TextView(host).apply{
        text=s
        textSize=size.toFloat()
        setTextColor(color)
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        gravity=Gravity.CENTER
        setAutoSizeTextTypeUniformWithConfiguration(12,size,1,
            android.util.TypedValue.COMPLEX_UNIT_SP)
    }
    private fun kind(s:String)=when(s){
        "STAR"->BopaviUiIconDrawable.Kind.STAR
        "FLIGHT"->BopaviUiIconDrawable.Kind.FLIGHT
        "WING"->BopaviUiIconDrawable.Kind.WING
        "BERRY"->BopaviUiIconDrawable.Kind.BERRY
        "GATE"->BopaviUiIconDrawable.Kind.GATE
        "COIN"->BopaviUiIconDrawable.Kind.COIN
        "RESTART"->BopaviUiIconDrawable.Kind.RESTART
        else->BopaviUiIconDrawable.Kind.RECORDS
    }
    private fun card(g:AchievementGoal):View {
        val col=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(dp(9),dp(5),dp(9),dp(6))
            background=if(g.achieved)
                plate(0xfffff6d8.toInt(),0xffecca8f.toInt(),0xffdb9937.toInt())
            else plate(0xffedf7ff.toInt(),0xffbad2ec.toInt(),0xff7ab4df.toInt())
            isFocusable=true
            contentDescription=g.accessibility
        }
        val heading=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        heading.addView(ImageView(host).apply{
            setImageDrawable(BopaviUiIconDrawable(kind(g.icon),
                if(g.achieved)0xffd69413.toInt() else 0xff2b7ac2.toInt(),dp(32)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(dp(40),dp(40)))
        heading.addView(label(g.title,17,ink),LinearLayout.LayoutParams(0,-1,1f))
        col.addView(heading,LinearLayout.LayoutParams(-1,0,1f))
        col.addView(label(g.detail,13,0xff385574.toInt()),
            LinearLayout.LayoutParams(-1,0,.65f))
        col.addView(label("${g.current} / ${g.target}",18,ink),
            LinearLayout.LayoutParams(-1,0,.75f))
        col.addView(ProgressBar(host,null,android.R.attr.progressBarStyleHorizontal).apply{
            max=100
            progress=g.percent
            isIndeterminate=false
            progressTintList=ColorStateList.valueOf(
                if(g.achieved)0xffdca21e.toInt() else 0xff258fe0.toInt())
            progressBackgroundTintList=ColorStateList.valueOf(0xffbecad4.toInt())
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,dp(9)))
        col.addView(label(if(g.achieved)"✓ GOAL REACHED" else "IN PROGRESS",
            13,if(g.achieved)0xff23742c.toInt() else 0xff34618c.toInt()),
            LinearLayout.LayoutParams(-1,0,.65f))
        return col
    }
    private fun home(onBack:()->Unit)=Button(host).apply {
        text="HOME"
        isAllCaps=false
        textSize=26f
        minimumHeight=dp(48)
        typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        setTextColor(ink)
        background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
            plate(0xffffe76b.toInt(),0xffff9905.toInt(),0xffffebae.toInt()),null)
        elevation=dp(4).toFloat()
        contentDescription="HOME"
        setOnClickListener{onBack()}
    }
    fun create(snapshot:AchievementSnapshot,onBack:()->Unit):View {
        val goals=AchievementCatalog.goals(snapshot)
        val achieved=goals.count{it.achieved}
        val root=FrameLayout(host).apply{setBackgroundColor(0xff268fdd.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val portrait=ImageView(host).apply{
            setImageResource(R.drawable.bopiportrait0)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val dm=host.resources.displayMetrics
        val reference=dm.widthPixels>=860 && dm.heightPixels>=1510 &&
            host.resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT
        if(reference){
            val stage=LevelMapStage(host)
            root.addView(stage,FrameLayout.LayoutParams(-1,-1))
            stage.place(portrait,252f,34f,438f,290f)
            stage.place(label("ACHIEVEMENTS",65,Color.WHITE),75f,322f,791f,114f)
            stage.place(label("$achieved / 8 GOALS REACHED",29,
                0xffffe999.toInt()),218f,430f,506f,61f)
            goals.forEachIndexed{index,g->
                stage.place(card(g),AchievementsLayout.cols[index%2],
                    AchievementsLayout.rows[index/2],
                    AchievementsLayout.cardWidth,AchievementsLayout.cardHeight)
            }
            stage.place(home(onBack),208f,1481f,525f,117f,minTouch=true)
            stage.setOnApplyWindowInsetsListener{_,insets->
                if(Build.VERSION.SDK_INT>=30){
                    val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    stage.setSafe(safe.left,safe.top,safe.right,safe.bottom)
                }else{
                    @Suppress("DEPRECATION")
                    stage.setSafe(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
                }
                insets
            }
            stage.requestApplyInsets()
        }else{
            val scroll=ScrollView(host).apply{
                setOnApplyWindowInsetsListener{_,insets->
                    if(Build.VERSION.SDK_INT>=30){
                        val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                            or android.view.WindowInsets.Type.displayCutout())
                        setPadding(safe.left,safe.top,safe.right,safe.bottom)
                    }else{
                        @Suppress("DEPRECATION")
                        setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                            insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
                    }
                    insets
                }
            }
            val column=LinearLayout(host).apply{
                orientation=LinearLayout.VERTICAL
                setPadding(dp(13),dp(8),dp(13),dp(13))
            }
            column.addView(home(onBack),LinearLayout.LayoutParams(-1,dp(53)))
            column.addView(portrait,LinearLayout.LayoutParams(-1,dp(120)))
            column.addView(label("ACHIEVEMENTS",32,Color.WHITE),
                LinearLayout.LayoutParams(-1,dp(57)))
            column.addView(label("$achieved / 8 GOALS REACHED",18,0xffffe999.toInt()),
                LinearLayout.LayoutParams(-1,dp(43)))
            goals.forEach{g->
                column.addView(card(g),LinearLayout.LayoutParams(-1,dp(145)).apply{
                    bottomMargin=dp(8)
                })
            }
            scroll.addView(column)
            root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
            scroll.requestApplyInsets()
        }
        return root
    }
}
