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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.NumberFormat

/**
 * Offline six-perk store. Each card is a real Android component, never a
 * screenshot or a fake in-app payment. Purchase callbacks remain MainActivity's
 * existing ProgressStore.buyPerk(index) transaction.
 */
internal class ShopScreen(
    private val host:Activity,
    private val progress:ProgressStore
) {
    private val navy=0xff092b5d.toInt()
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()
    private fun plate(top:Int,bottom:Int,border:Int,radius:Int)=
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(top,bottom)).apply{
            cornerRadius=dp(radius).toFloat()
            setStroke(dp(3),border)
        }
    private fun message(s:String,px:Int,color:Int=Color.WHITE):TextView =
        TextView(host).apply{
            text=s
            textSize=px.toFloat()
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(color)
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(12,px,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
        }
    private fun button(s:String,gold:Boolean,available:Boolean=true,click:()->Unit):Button =
        Button(host).apply{
            text=s
            isAllCaps=false
            textSize=24f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(gold)0xff63300a.toInt() else Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                if(gold)plate(0xffffe75a.toInt(),0xffff9404.toInt(),0xfffff5b0.toInt(),25)
                else plate(0xff50d8ff.toInt(),0xff0864c8.toInt(),0xffacf2ff.toInt(),25),
                null)
            setAutoSizeTextTypeUniformWithConfiguration(14,24,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            minimumHeight=dp(48)
            isEnabled=available
            elevation=dp(4).toFloat()
            setOnClickListener{
                if(!isEnabled)return@setOnClickListener
                // Prevent repeated click events against the same stale view
                // while MainActivity processes the saved purchase and rebinds.
                isEnabled=false
                click()
                if(isAttachedToWindow)isEnabled=true
            }
        }

    private fun perkCard(item:ShopItemState,onBuy:(Int)->Unit):View {
        val card=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(dp(10),dp(9),dp(10),dp(10))
            background=plate(0xfffff5df.toInt(),0xffefd3a7.toInt(),
                0xffbc7a3a.toInt(),22)
            elevation=dp(4).toFloat()
            contentDescription=item.accessibility
        }
        val row=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        row.addView(ImageView(host).apply{
            setImageDrawable(BopaviUiIconDrawable(
                if(item.isShield)BopaviUiIconDrawable.Kind.SHIELD
                else BopaviUiIconDrawable.Kind.MAGNET,
                if(item.isShield)0xff128dda.toInt() else 0xffba52fc.toInt(),dp(66)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(dp(43),dp(43)))
        row.addView(message("TIER ${item.tier}",18,navy),
            LinearLayout.LayoutParams(0,-1,1f))
        card.addView(row,LinearLayout.LayoutParams(-1,0,1.0f))
        card.addView(message(item.name.uppercase(),20,navy),
            LinearLayout.LayoutParams(-1,0,.52f))
        card.addView(message("OWNED: ${item.owned}/99",17,0xff244c73.toInt()),
            LinearLayout.LayoutParams(-1,0,.40f))
        val buy=button(item.buttonLabel,item.canBuy,item.canBuy){
            onBuy(item.index)
        }.apply{
            contentDescription=item.accessibility+
                if(item.canBuy)". Buy for ${item.price} coins" else ". Purchase unavailable"
        }
        card.addView(buy,LinearLayout.LayoutParams(-1,0,1.10f).apply{
            topMargin=dp(4)
        })
        return card
    }

    fun create(onBuy:(Int)->Unit,onClaim:()->Unit,onBack:()->Unit):View {
        val root=FrameLayout(host).apply{setBackgroundColor(0xff178dd8.toInt())}
        root.addView(ImageView(host).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x7c082348,0x225cbfeb,0xaa041d46.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))

        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        stage.place(message("SHOP",83),239f,73f,463f,125f)
        val coins=progress.coins()
        stage.place(message("COINS  •  "+
            NumberFormat.getIntegerInstance().format(coins),32).apply{
            background=plate(0xff2a91db.toInt(),navy,0xffacebff.toInt(),24)
            contentDescription="Earned coins: $coins"
            isFocusable=true
        },241f,220f,459f,108f)
        val bonus=progress.bonusCoinsAvailable()
        if(bonus>0){
            stage.place(button("CLAIM +$bonus EARNED COINS",true,click=onClaim),
                169f,350f,603f,87f,minTouch=true)
        }else{
            stage.place(message("OFFLINE POWER-UPS",25,0xffffefb2.toInt()),
                229f,359f,483f,72f)
        }

        for(item in ShopItemState.fromStore(progress)){
            val row=item.index/2
            val col=item.index%2
            stage.place(perkCard(item,onBuy),
                ShopReferenceLayout.columns[col],
                ShopReferenceLayout.rows[row],
                ShopReferenceLayout.cardWidth,
                ShopReferenceLayout.cardHeight)
        }
        stage.place(button("HOME",false,click=onBack),
            197f,ShopReferenceLayout.homeTop,547f,ShopReferenceLayout.homeHeight,
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
