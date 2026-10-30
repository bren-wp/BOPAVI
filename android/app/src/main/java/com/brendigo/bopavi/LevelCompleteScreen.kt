package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                if(gold)panel(0xffffe863.toInt(),0xffff970a.toInt(),
                    0xffb64d02.toInt(),33)
                else panel(0xff56dbff.toInt(),0xff0754bc.toInt(),
                    0xffc0efff.toInt(),31),null)
            contentDescription=label
            elevation=dp(5).toFloat()
            setOnClickListener{onClick()}
        }

    private fun status(labelText:String,value:String,color:Int):View =
        LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(dp(9),dp(8),dp(9),dp(8))
            background=panel(0xfffcf1d9.toInt(),0xffe6c9ab.toInt(),
                0xffba8345.toInt(),18)
            contentDescription=labelText+": "+value
            isFocusable=true
            addView(label(value,28f,color),LinearLayout.LayoutParams(-1,0,1f))
            addView(label(labelText,13f,navy),LinearLayout.LayoutParams(-1,dp(30)))
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

        stage.place(label("LEVEL COMPLETE!",63f,Color.WHITE),
            80f,82f,781f,146f)
        stage.place(label("LEVEL "+data.level.toString(),26f,
            0xfffff0b5.toInt()),331f,223f,279f,56f)
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
        stage.place(stars,152f,295f,637f,169f)

        stage.place(ImageView(host).apply {
            setImageResource(pilot)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },218f,457f,505f,467f)

        val statRow=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
            setPadding(dp(8),0,dp(8),0)
        }
        statRow.addView(status("STARS",data.rating,
            0xffed9500.toInt()),LinearLayout.LayoutParams(0,-1,1f).apply {
            marginEnd=dp(8)
        })
        statRow.addView(status("COINS PICKED UP",data.collectedText,
            0xffcb6d00.toInt()),LinearLayout.LayoutParams(0,-1,1f).apply{
            marginEnd=dp(8)
        })
        statRow.addView(status("SCORE",data.scoreText,
            0xff1677c3.toInt()),LinearLayout.LayoutParams(0,-1,1f))
        stage.place(statRow,105f,923f,731f,153f)

        stage.place(LevelCompleteChest(host),338f,1069f,265f,184f)
        val reward=label(data.rewardText,25f,
            if(data.justAwardedCoins>0)0xffffea90.toInt() else Color.WHITE).apply {
            contentDescription=if(data.justAwardedCoins>0)
                "Milestone bonus awarded: "+data.justAwardedCoins+" coins"
            else "No milestone coins awarded for this clear"
        }
        stage.place(reward,206f,1243f,529f,69f)

        stage.place(button(if(data.nextAvailable)"NEXT LEVEL" else "LEVEL MAP",
            true,onNext),151f,1337f,639f,116f,minTouch=true)
        stage.place(button("HOME",false,onHome),
            159f,1484f,286f,113f,minTouch=true)
        stage.place(button("SHARE",false,onShare),
            496f,1484f,286f,113f,minTouch=true)

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
        paint.style=Paint.Style.FILL
        paint.color=if(earned)0xffffd241.toInt() else 0xff7b92ac.toInt()
        canvas.drawPath(star,paint)
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=radius*.08f
        paint.color=if(earned)0xffb46c0a.toInt() else 0xff203e68.toInt()
        canvas.drawPath(star,paint)
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
        canvas.restoreToCount(save)
    }
}
