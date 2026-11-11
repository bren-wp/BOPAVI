package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

/**
 * Authentic, offline player progression across all eight BOPAVI worlds.
 * Accessible native views; no cloud leaderboard, fabricated awards or
 * screenshot-backed touch targets.
 */
internal class ProgressOverviewScreen(
    private val host:Activity,
    private val store:ProgressStore
) {
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()

    private fun text(message:String,size:Float,color:Int=Color.WHITE):TextView =
        TextView(host).apply{
            text=message
            textSize=size
            setTextColor(color)
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            gravity=Gravity.CENTER_VERTICAL
            includeFontPadding=true
            isAllCaps=false
        }

    private fun button(label:String,finish:Bopavi3DTheme.Finish,onPress:()->Unit):Button =
        Button(host).apply {
            text=label
            isAllCaps=false
            textSize=17f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(Color.WHITE)
            background=Bopavi3DTheme.panel(finish)
            minHeight=dp(48)
            minimumHeight=dp(48)
            contentDescription=label
            setOnClickListener{onPress()}
        }

    private fun worldCard(world:Int):View {
        val frontier=store.frontier(world)
        val completed=ProgressOverviewMetrics.completedLevels(frontier)
        val stars=store.worldStars(world).coerceIn(0,60)
        val panel=LinearLayout(host).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(14),dp(12),dp(14),dp(14))
            background=Bopavi3DTheme.panel(
                if(world==store.chosenWorld())
                    Bopavi3DTheme.Finish.GOLD else Bopavi3DTheme.Finish.CREAM)
            contentDescription=ProgressOverviewMetrics.detail(world,frontier,stars)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        val header=LinearLayout(host).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        header.addView(text("${world+1}. ${ProgressOverviewMetrics.worldName(world)}",16f,
            0xff082b61.toInt()).apply {
                setAutoSizeTextTypeUniformWithConfiguration(12,16,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                maxLines=2
            },LinearLayout.LayoutParams(0,dp(54),1f))
        header.addView(text("★ $stars/60",16f,0xff75421b.toInt()).apply {
            gravity=Gravity.END or Gravity.CENTER_VERTICAL
        },LinearLayout.LayoutParams(dp(85),dp(54)))
        panel.addView(header,LinearLayout.LayoutParams(-1,dp(54)))
        panel.addView(text("Dovršene razine: $completed",14f,
            0xff173b64.toInt()),LinearLayout.LayoutParams(-1,dp(32)))
        panel.addView(ProgressBar(host,null,android.R.attr.progressBarStyleHorizontal).apply{
            max=100
            progress=ProgressOverviewMetrics.firstTwentyStarPercent(stars)
            progressTintList=ColorStateList.valueOf(0xffffa910.toInt())
            progressBackgroundTintList=ColorStateList.valueOf(0xff9bafc5.toInt())
            contentDescription="$stars od 60 zvjezdica na prvih 20 razina"
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
        },LinearLayout.LayoutParams(-1,dp(10)).apply {
            topMargin=dp(8)
            bottomMargin=dp(8)
        })
        panel.addView(text("Zvjezdice s prvih 20 razina",12f,
            0xff365477.toInt()),LinearLayout.LayoutParams(-1,dp(28)))
        return panel
    }

    fun create(onBack:()->Unit,onMap:()->Unit):View {
        val root=FrameLayout(host).apply {
            setBackgroundColor(0xff168cda.toInt())
        }
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply {
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x880c3374.toInt(),0x334baedb,0x990b2453.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val column=LinearLayout(host).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(18),dp(18),dp(18),dp(28))
        }
        val actions=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        actions.addView(button("HOME",Bopavi3DTheme.Finish.SKY,onBack),
            LinearLayout.LayoutParams(0,dp(57),1f))
        actions.addView(button("LEVEL MAP",Bopavi3DTheme.Finish.GREEN,onMap),
            LinearLayout.LayoutParams(0,dp(57),1f).apply{marginStart=dp(10)})
        column.addView(actions,LinearLayout.LayoutParams(-1,dp(57)))

        column.addView(text("NAPREDAK",32f).apply{
            gravity=Gravity.CENTER
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
            contentDescription="Napredak igrača"
            elevation=dp(5).toFloat()
        },LinearLayout.LayoutParams(-1,dp(88)).apply{topMargin=dp(18)})

        val stats=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(18),dp(12),dp(18),dp(12))
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
        }
        stats.addView(text("NAJBOLJI REZULTAT   ${store.bestPoints()}",16f),
            LinearLayout.LayoutParams(-1,dp(31)))
        stats.addView(text("POBJEDE   ${store.wins()}    •    LETOVI   ${store.totalRuns()}",15f),
            LinearLayout.LayoutParams(-1,dp(31)))
        stats.addView(text("NOVČIĆI   ${store.coins()}    •    ZAMASI KRILA   ${store.totalFlaps()}",14f),
            LinearLayout.LayoutParams(-1,dp(31)))
        column.addView(stats,LinearLayout.LayoutParams(-1,dp(119)).apply{
            topMargin=dp(14)
            bottomMargin=dp(8)
        })

        for(world in 0 until LevelEngine.WORLD_COUNT){
            column.addView(worldCard(world),
                LinearLayout.LayoutParams(-1,dp(192)).apply{
                    topMargin=dp(10)
                })
        }
        column.addView(text("Napredak je spremljen lokalno na ovom uređaju.",12f)
            .apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,dp(48)))

        val scroll=ScrollView(host).apply {
            isFillViewport=true
            clipToPadding=false
            addView(column)
            setOnApplyWindowInsetsListener { _, insets ->
                if(android.os.Build.VERSION.SDK_INT>=30){
                    val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    setPadding(safe.left,safe.top,safe.right,safe.bottom)
                } else {
                    @Suppress("DEPRECATION")
                    setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
                }
                insets
            }
        }
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
        scroll.requestApplyInsets()
        return root
    }
}
