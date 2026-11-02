package com.brendigo.bopavi

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Loss-only native BOPAVI result screen. Coordinate targets follow the
 * owner's 941×1672 Game Over reference; no screenshot is used as a UI.
 * Buttons and row values stay real Android views for accessibility.
 */
internal class GameOverScreen(private val host:Activity) {
    private fun dp(px:Int)=((px*host.resources.displayMetrics.density)+.5f).toInt()
    private val navy=0xff092451.toInt()
    private val gold=0xffffbe23.toInt()

    private fun statRow(label:String,value:String,icon:BopaviUiIconDrawable.Kind,
                        accent:Int):View {
        val row=LinearLayout(host).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(7),0,dp(8),0)
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.CREAM)
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
        },LinearLayout.LayoutParams(dp(35),dp(37)).apply{
            marginEnd=dp(6)
        })
        row.addView(TextView(host).apply{
            text=label
            textSize=22f
            maxLines=1
            includeFontPadding=false
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            setTextColor(navy)
            setAutoSizeTextTypeUniformWithConfiguration(13,22,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            gravity=Gravity.CENTER_VERTICAL
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(0,-1,1f))
        row.addView(TextView(host).apply{
            text=value
            textSize=30f
            maxLines=1
            includeFontPadding=false
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(0xffa5490b.toInt())
            setShadowLayer(dp(2).toFloat(),0f,dp(2).toFloat(),0x440a214f)
            setAutoSizeTextTypeUniformWithConfiguration(16,30,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            gravity=Gravity.END or Gravity.CENTER_VERTICAL
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(dp(114),-1))
        return row
    }

    private fun action(label:String,primary:Boolean,onClick:()->Unit):Button =
        Button(host).apply {
            text=label
            isAllCaps=false
            textSize=32f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(primary)0xff60300b.toInt() else Color.WHITE)
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(18,32,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            background=Bopavi3DTheme.button(if(primary)Bopavi3DTheme.Finish.GOLD else Bopavi3DTheme.Finish.SKY)
            contentDescription=label
            elevation=dp(6).toFloat()
            minimumHeight=dp(48)
            setOnClickListener{onClick()}
        }

    fun create(worldDrawable:Int,pilotDrawable:Int,stats:GameOverStats,
               onRetry:()->Unit,onHome:()->Unit):View {
        val root=FrameLayout(host).apply{setBackgroundColor(0xff278bdb.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(worldDrawable)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x55042255,0x1178e1ff,0x55032149))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        stage.place(ResultWingDecoration(host),145f,177f,650f,165f)
        stage.place(GameOverCrown(host),431f,53f,79f,76f)
        stage.place(ResultEmblemLabel(host,"GAME",97f,
            ResultEmblemLabel.Tone.ICE).apply {
            contentDescription="Game Over"
        },171f,129f,599f,146f)
        stage.place(ResultEmblemLabel(host,"OVER",103f,
            ResultEmblemLabel.Tone.CORAL).apply {
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },153f,267f,635f,166f)

        // Painted floating grass/stone podium stays behind the actual Bopi.
        stage.place(GameOverPodium(host),168f,728f,605f,216f)
        stage.place(ImageView(host).apply{
            setImageResource(pilotDrawable)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },224f,431f,494f,445f)

        val statistics=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(12),dp(14),dp(12),dp(14))
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.CREAM)
            contentDescription="Game Over statistics"
        }
        val rows=listOf(
            statRow("Distance",stats.distance,BopaviUiIconDrawable.Kind.GATE,
                0xffed5434.toInt()),
            statRow(stats.collectedLabel,stats.collected,
                if(stats.collectedLabel=="Food Collected")
                    BopaviUiIconDrawable.Kind.BERRY
                else BopaviUiIconDrawable.Kind.COIN,
                gold),
            statRow(stats.thirdLabel,stats.third,
                if(stats.thirdLabel=="Stars Collected")
                    BopaviUiIconDrawable.Kind.STAR
                else BopaviUiIconDrawable.Kind.RECORDS,
                0xffbf37f5.toInt())
        )
        for(item in rows){
            statistics.addView(item,LinearLayout.LayoutParams(-1,0,1f).apply {
                setMargins(0,dp(3),0,dp(3))
            })
        }
        stage.place(statistics,123f,927f,695f,310f)

        stage.place(action("RETRY",true,onRetry),183f,1233f,575f,150f,
            minTouch=true)
        stage.place(action("HOME",false,onHome),241f,1404f,459f,124f,
            minTouch=true)
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
internal class GameOverCrown(host:Activity):View(host) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val shape=Path()
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val save=canvas.save()
        canvas.scale(width/79f,height/76f)
        shape.reset()
        shape.moveTo(8f,18f);shape.lineTo(22f,31f)
        shape.lineTo(40f,4f);shape.lineTo(56f,31f)
        shape.lineTo(72f,18f);shape.lineTo(65f,63f)
        shape.lineTo(16f,63f);shape.close()
        // Raised black/bronze extrusion follows the same crown silhouette.
        val extrusion=canvas.save()
        canvas.translate(0f,6f)
        p.style=Paint.Style.FILL
        p.color=0xff4f260b.toInt()
        canvas.drawPath(shape,p)
        canvas.restoreToCount(extrusion)
        p.color=0xfff18018.toInt()
        canvas.drawPath(shape,p)
        p.color=0xffffbf16.toInt()
        canvas.drawPath(shape,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=5f
        p.color=0xff072957.toInt();canvas.drawPath(shape,p)
        // Gold upper facets and specular light; purely decorative artwork.
        p.strokeWidth=3f
        p.color=0xfffff0ad.toInt()
        canvas.drawLine(13f,20f,25f,36f,p)
        canvas.drawLine(25f,36f,40f,10f,p)
        canvas.drawLine(40f,10f,54f,34f,p)
        p.style=Paint.Style.FILL
        p.color=0xfffff7d0.toInt()
        canvas.drawCircle(39f,13f,3f,p)
        canvas.drawCircle(10f,20f,2.6f,p)
        canvas.drawCircle(71f,20f,2.6f,p)
        canvas.restoreToCount(save)
    }
}

internal class GameOverPodium(host:Activity):View(host){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val rock=Path()
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val save=canvas.save()
        canvas.scale(width/605f,height/216f)
        rock.reset()
        rock.moveTo(22f,55f);rock.lineTo(575f,55f)
        rock.lineTo(552f,174f);rock.lineTo(428f,214f)
        rock.lineTo(169f,212f);rock.lineTo(45f,168f)
        rock.close()
        p.color=0xff886f74.toInt();p.style=Paint.Style.FILL
        canvas.drawPath(rock,p)
        p.color=0xff423e5a.toInt();p.style=Paint.Style.STROKE
        p.strokeWidth=8f;canvas.drawPath(rock,p)
        // Deterministic stone facets: no extra sprites, allocations or
        // fake pickups. The grassy top remains above the shaded rock faces.
        p.style=Paint.Style.FILL
        for(i in 0..7){
            val x=55f+i*62f
            rock.reset()
            rock.moveTo(x,85f)
            rock.lineTo(x+46f,85f)
            rock.lineTo(x+31f,178f+(i%2)*10f)
            rock.lineTo(x+8f,160f)
            rock.close()
            p.color=if(i%2==0)0x664b364d else 0x667ec6c4
            canvas.drawPath(rock,p)
        }
        p.color=0xff448a2b.toInt()
        canvas.drawOval(12f,11f,591f,103f,p)
        p.color=0xff8bdf42.toInt()
        canvas.drawOval(31f,4f,572f,84f,p)
        p.color=0xffc0ef66.toInt()
        canvas.drawOval(66f,5f,539f,48f,p)
        for(i in 0..12) {
            val x=41f+i*43f
            val y=32f+(i%3)*12f
            p.color=Color.WHITE
            canvas.drawCircle(x-4f,y,5f,p)
            canvas.drawCircle(x+4f,y,5f,p)
            canvas.drawCircle(x,y-4f,5f,p)
            canvas.drawCircle(x,y+4f,5f,p)
            p.color=0xffffd73d.toInt();canvas.drawCircle(x,y,3f,p)
        }
        canvas.restoreToCount(save)
    }
}
