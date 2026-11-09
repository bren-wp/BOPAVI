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
 * Finite-win reference composition on the 941 × 1672 logical canvas.
 * The hero and world are bundled game assets; stars, chest and buttons are
 * drawn locally, and remain native accessible views instead of a bitmap mock.
 */
internal class LevelCompleteScreen(private val host:Activity) {
    private val navy=0xff082b61.toInt()
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()
    private fun panel(top:Int,bottom:Int,border:Int,radius:Int)=
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(top,bottom)).apply{
            cornerRadius=dp(radius).toFloat()
            setStroke(dp(3),border)
        }

    private fun label(message:String,size:Float,color:Int):TextView =
        TextView(host).apply{
            text=message
            textSize=size
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(color)
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(13,size.toInt(),1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            setShadowLayer(dp(3).toFloat(),0f,dp(3).toFloat(),navy)
        }

    private fun button(label:String,gold:Boolean,onClick:()->Unit):Button=
        Button(host).apply{
            text=label
            isAllCaps=false
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(gold)0xff582c08.toInt() else Color.WHITE)
            textSize=30f
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(17,30,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            minimumHeight=dp(48)
            background=ResultActionEnamelDrawable(gold)
            if(gold || label=="HOME"){
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    HomeHeroIconDrawable(
                        if(gold)BopaviUiIconDrawable.Kind.PLAY
                        else BopaviUiIconDrawable.Kind.HOME,dp(26)),
                    null,null,null)
                compoundDrawablePadding=dp(6)
            }
            contentDescription=label
            elevation=dp(5).toFloat()
            setOnClickListener{onClick()}
        }

    // The owner victory reference has two reward pictograms above their
    // live values, inside ONE cream reward plate. Do not use three stacked
    // rectangular cards or pretend the game has a spendable gem wallet.
    private fun status(labelText:String,value:String,color:Int,
                       icon:BopaviUiIconDrawable.Kind):View =
        LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(dp(5),dp(3),dp(5),dp(2))
            contentDescription=labelText+": "+value
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            addView(ImageView(host).apply {
                setImageDrawable(HomeHeroIconDrawable(icon,dp(57)))
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,0,1.25f))
            addView(label(value,26f,color).apply {
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,0,.60f))
            addView(label(labelText,14f,navy).apply {
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,0,.40f))
        }

    fun create(backdrop:Int,pilot:Int,data:LevelCompleteState,
               onNext:()->Unit,onHome:()->Unit,onShare:()->Unit):View{
        val root=FrameLayout(host).apply{
            setBackgroundColor(0xff2a97d7.toInt())
        }
        root.addView(ImageView(host).apply{
            setImageResource(backdrop)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x55002050,0x117fcfff,0x66032048))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))

        // Owner reference: LEVEL and COMPLETE! are two separately embossed
        // real TextViews; wings sit behind them, not over the touch targets.
        stage.place(ResultWingDecoration(host),139f,129f,663f,177f)
        stage.place(ResultEmblemLabel(host,"LEVEL",89f,
            ResultEmblemLabel.Tone.ICE).apply {
            contentDescription="Level ${data.level} complete!"
        },194f,91f,553f,133f)
        stage.place(ResultEmblemLabel(host,"COMPLETE!",88f,
            ResultEmblemLabel.Tone.GOLD).apply {
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },112f,204f,718f,160f)
        val stars=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
            contentDescription=data.stars.toString()+" of 3 stars earned"
            isFocusable=true
        }
        for(index in 0..2){
            stars.addView(LevelCompleteStar(host,index<data.stars),
                LinearLayout.LayoutParams(0,-1,1f))
        }
        // Pilot now appears ABOVE the earned star medals, matching the
        // owner's actual layout. The star count stays the true level rating.
        // Victory shows the FULL saved pilot with original wing/body layers,
        // not a static face icon. This is the identical active hero art used
        // on Home and Skins; the stars remain genuine persisted achievements.
        val heroLayers=when(pilot){
            R.drawable.bopiportrait9 -> Triple(
                R.drawable.bopileft9,R.drawable.bopiright9,R.drawable.bopi9)
            R.drawable.bopiportrait10 -> Triple(
                R.drawable.bopileft10,R.drawable.bopiright10,R.drawable.bopi10)
            else -> Triple(
                R.drawable.bopileft0,R.drawable.bopiright0,R.drawable.bopi0)
        }
        stage.place(HomeWingActor(host,heroLayers.first,heroLayers.second,
            heroLayers.third,allowMotion=android.animation.ValueAnimator.areAnimatorsEnabled()),
            209f,348f,523f,523f)
        stage.place(stars,259f,811f,423f,142f)

        val statRow=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
            // Preserve score for assistive users without adding an extra
            // non-reference visual tile. Every on-screen value remains true.
            contentDescription="Coins picked up: ${data.collectedText}. "+
                "Stars earned: ${data.rating}. Score: ${data.scoreText}"
            isFocusable=true
            setPadding(dp(4),0,dp(4),0)
        }
        statRow.addView(status("COINS",data.collectedText,
            0xffcb6d00.toInt(),BopaviUiIconDrawable.Kind.CROWN_COIN),
            LinearLayout.LayoutParams(0,-1,1f))
        statRow.addView(status("STARS",data.rating,
            0xffed9500.toInt(),BopaviUiIconDrawable.Kind.STAR),
            LinearLayout.LayoutParams(0,-1,1f))
        // The reference uses one large cream reward area, not three floating
        // unrelated cards. Each statistic still has live Android text.
        stage.place(View(host).apply {
            background=LevelCompleteRewardPlateDrawable()
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },100f,929f,741f,278f)
        // Large native collectible icons and a raised reward chest share
        // the same single plate, as in the owner-supplied 941px illustration.
        stage.place(statRow,151f,975f,422f,187f)
        stage.place(LevelCompleteChest(host),567f,975f,222f,192f)
        val reward=label(data.rewardText,25f,
            if(data.justAwardedCoins>0)0xffffea90.toInt() else Color.WHITE).apply {
            contentDescription=if(data.justAwardedCoins>0)
                "Milestone bonus awarded: "+data.justAwardedCoins+" coins"
            else "No milestone coins awarded for this clear"
        }
        stage.place(reward,547f,1158f,269f,45f)

        stage.place(HomePlayWings(host),113f,1234f,714f,151f)
        stage.place(button(if(data.nextAvailable)"NEXT LEVEL" else "LEVEL MAP",
            true,onNext),151f,1234f,639f,151f,minTouch=true)
        stage.place(button("HOME",false,onHome),
            150f,1405f,301f,119f,minTouch=true)
        stage.place(button("SHARE",false,onShare),
            490f,1405f,301f,119f,minTouch=true)

        stage.setOnApplyWindowInsetsListener{ _,insets ->
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
        return root
    }
}

/** A deterministic outline and shine; no emoji-font star inconsistencies. */
internal class LevelCompleteStar(host:Activity,private val earned:Boolean):View(host){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val star=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val widthF=width.toFloat()
        val heightF=height.toFloat()
        val radius=kotlin.math.min(widthF,heightF)*.47f
        val cx=widthF*.5f;val cy=heightF*.5f
        star.reset()
        for(point in 0..9){
            val a=point*kotlin.math.PI/5.0-kotlin.math.PI/2.0
            val r=radius*(if(point%2==0)1f else .49f)
            val x=cx+kotlin.math.cos(a).toFloat()*r
            val y=cy+kotlin.math.sin(a).toFloat()*r
            if(point==0)star.moveTo(x,y) else star.lineTo(x,y)
        }
        star.close()
        // 3D lower extrusion shares the real earned/empty star state.
        val depth=canvas.save()
        canvas.translate(0f,radius*.11f)
        paint.style=Paint.Style.FILL
        paint.color=if(earned)0xff98521a.toInt() else 0xff344660.toInt()
        canvas.drawPath(star,paint)
        canvas.restoreToCount(depth)
        paint.color=if(earned)0xffffd241.toInt() else 0xff7b92ac.toInt()
        canvas.drawPath(star,paint)
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=radius*.08f
        paint.color=if(earned)0xffb46c0a.toInt() else 0xff203e68.toInt()
        canvas.drawPath(star,paint)
        if(earned) {
            paint.color=0xbaffffff.toInt()
            paint.strokeWidth=radius*.045f
            canvas.drawLine(cx-radius*.31f,cy-radius*.35f,
                cx-radius*.05f,cy-radius*.52f,paint)
        }
        paint.style=Paint.Style.FILL
    }
}

/** A closed decorative chest. Rewards are already granted on first clear. */
internal class LevelCompleteChest(host:Activity):View(host){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val outline=Path()
    init{importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onDraw(canvas:Canvas){
        val save=canvas.save()
        canvas.scale(width/265f,height/184f)
        paint.style=Paint.Style.FILL
        paint.color=0x66021c4d
        canvas.drawOval(18f,157f,249f,182f,paint)
        paint.color=0xff673a2b.toInt()
        canvas.drawRoundRect(27f,75f,238f,166f,15f,15f,paint)
        paint.color=0xffb66b35.toInt()
        canvas.drawRoundRect(31f,82f,234f,158f,12f,12f,paint)
        paint.color=0xff6d382d.toInt()
        canvas.drawRoundRect(25f,37f,240f,107f,32f,32f,paint)
        paint.color=0xffc28a42.toInt()
        canvas.drawRoundRect(32f,43f,233f,98f,29f,29f,paint)
        paint.color=0xffffd55e.toInt()
        canvas.drawRoundRect(118f,49f,147f,164f,5f,5f,paint)
        canvas.drawRoundRect(32f,106f,233f,119f,4f,4f,paint)
        paint.color=0xff5f3b23.toInt()
        canvas.drawRoundRect(124f,94f,141f,130f,6f,6f,paint)
        // Local wood grain and gold specular bezel; this is a closed chest
        // indicating rewards already recorded, not a second claim action.
        paint.color=0x88ffe2a1.toInt()
        canvas.drawRoundRect(44f,52f,112f,58f,3f,3f,paint)
        canvas.drawRoundRect(164f,52f,218f,58f,3f,3f,paint)
        paint.color=0xfff6cf63.toInt()
        canvas.drawRoundRect(118f,86f,147f,97f,4f,4f,paint)
        paint.color=0xff844425.toInt()
        canvas.drawRoundRect(120f,118f,145f,130f,3f,3f,paint)
        paint.color=0xaafff3ce.toInt()
        canvas.drawRoundRect(120f,55f,124f,83f,2f,2f,paint)
        canvas.restoreToCount(save)
    }
}
