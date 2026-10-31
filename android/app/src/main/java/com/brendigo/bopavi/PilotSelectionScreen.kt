package com.brendigo.bopavi

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
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
import android.widget.Button

/**
 * The interactive three-pilot gallery, not a flattened screenshot.
 * Source 941x1672 reference positions are scaled uniformly by LevelMapStage;
 * buttons and cards remain real accessible Android views.
 */
internal class PilotSelectionScreen(
    private val host:Activity,
    private val progress:ProgressStore
) {
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()
    private val navy=0xff09295b.toInt()
    private val cyan=0xffa6eeff.toInt()
    private fun plate(top:Int,bottom:Int,outline:Int,radius:Int):GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(top,bottom)).apply{
            cornerRadius=dp(radius).toFloat()
            setStroke(dp(3),outline)
        }
    private fun text(label:String,size:Float,color:Int=Color.WHITE):TextView =
        TextView(host).apply{
            this.text=label
            textSize=size
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(color)
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(12,size.toInt(),1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
        }
    private fun action(label:String,primary:Boolean,onClick:()->Unit):Button =
        Button(host).apply{
            text=label
            isAllCaps=false
            textSize=25f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(primary)0xff67320d.toInt() else Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                if(primary)plate(0xffffe65c.toInt(),0xffff9405.toInt(),
                    0xfffff2b8.toInt(),33)
                else plate(0xff4fddff.toInt(),0xff0759c5.toInt(),cyan,30),null)
            gravity=Gravity.CENTER
            minimumHeight=dp(48)
            setAutoSizeTextTypeUniformWithConfiguration(15,25,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            elevation=dp(5).toFloat()
            contentDescription=label
            setOnClickListener{onClick()}
        }

    fun create(choice:Int,onChoose:(Int)->Unit,onBack:()->Unit):View {
        val selected=PilotSelectionModel.safeChoice(choice)
        val root=FrameLayout(host).apply{setBackgroundColor(0xff138cdd.toInt())}
        root.addView(ImageView(host).apply {
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x77022148,0x22269be5,0x99021c43.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        stage.place(action("‹ HOME",false,onBack),27f,37f,187f,100f,minTouch=true)
        stage.place(text("SKINS",77f),210f,138f,521f,143f)
        stage.place(text("CHOOSE YOUR PILOT",31f,0xffffe89b.toInt()),
            190f,279f,561f,76f)

        // Saved balance only. All three gameplay characters remain free.
        val balance=text("◉  "+progress.coins(),24f).apply{
            background=plate(0xff2876c4.toInt(),navy,cyan,22)
            contentDescription="Earned coins: "+progress.coins()
        }
        stage.place(balance,688f,42f,215f,82f)
        val skins=intArrayOf(R.drawable.bopiportrait0,
            R.drawable.bopiportrait9,R.drawable.bopiportrait10)

        for(item in PilotSelectionModel.pilots) {
            val index=item.id
            val isSelected=PilotSelectionModel.selected(selected,index)
            val card=LinearLayout(host).apply {
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER_HORIZONTAL
                setPadding(dp(9),dp(10),dp(9),dp(13))
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    if(isSelected)plate(0xfffff6da.toInt(),0xffffd67a.toInt(),
                        0xffffab17.toInt(),28)
                    else plate(0xffe4f7ff.toInt(),0xff80c9fa.toInt(),
                        0xff58b9ee.toInt(),28),null)
                isClickable=true
                isFocusable=true
                minimumHeight=dp(48)
                contentDescription=PilotSelectionModel.accessibility(selected,index)
                setOnClickListener{if(!isSelected)onChoose(index)}
                elevation=dp(if(isSelected)9 else 4).toFloat()
            }
            card.addView(ImageView(host).apply {
                setImageResource(skins[index])
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,0,1.4f))
            card.addView(text(item.shortName,29f,navy),
                LinearLayout.LayoutParams(-1,0,.32f))
            card.addView(text(item.title,19f,navy),
                LinearLayout.LayoutParams(-1,0,.30f))
            card.addView(text(item.ability.shortTitle,17f,0xff215f99.toInt()),
                LinearLayout.LayoutParams(-1,0,.32f))
            card.addView(text(PilotSelectionModel.priceLabel(),14f,0xff176b42.toInt()),
                LinearLayout.LayoutParams(-1,0,.27f))
            val status=text(if(isSelected)"✓ EQUIPPED" else "EQUIP",
                20f,if(isSelected)0xff226c13.toInt() else 0xff135aa5.toInt())
            status.background=plate(
                if(isSelected)0xffb5f18a.toInt() else 0xffdaf1ff.toInt(),
                if(isSelected)0xff73cb59.toInt() else 0xff92d8f8.toInt(),
                if(isSelected)0xff388e35.toInt() else 0xff307db3.toInt(),16)
            card.addView(status,LinearLayout.LayoutParams(-1,0,.37f))
            stage.place(card,PilotSelectionLayout.cardLefts[index],
                PilotSelectionLayout.cardTop,
                PilotSelectionLayout.cardWidth,PilotSelectionLayout.cardHeight,
                minTouch=true)
        }

        val equipped=PilotSelectionModel.pilots[selected]
        stage.place(text(equipped.shortName+" • "+equipped.ability.shortTitle,
                30f,0xffffe9a2.toInt()),106f,1150f,729f,77f)
        val ability=text(equipped.ability.description,23f).apply{
            setPadding(dp(12),dp(5),dp(12),dp(5))
            background=plate(0xc51a62a9.toInt(),0xe207275a.toInt(),cyan,25)
            contentDescription="Ability: "+equipped.ability.description
        }
        stage.place(ability,120f,1247f,701f,114f)
        stage.place(action("HOME",true,onBack),187f,1448f,567f,131f,
            minTouch=true)

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
        return root
    }
}
