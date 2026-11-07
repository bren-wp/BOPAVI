package com.brendigo.bopavi

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
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
            textSize=20f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(gold)0xff63300a.toInt() else Color.WHITE)
            background=Bopavi3DTheme.button(if(gold)Bopavi3DTheme.Finish.GOLD else Bopavi3DTheme.Finish.SKY)
            setAutoSizeTextTypeUniformWithConfiguration(12,20,1,
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
        // A native product card with four independent text/image regions,
        // not a flattened product screenshot. Existing six offline purchases
        // retain the exact ProgressStore index, price and stored inventory.
        val card=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(dp(5),dp(6),dp(5),dp(8))
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.CREAM)
            elevation=dp(4).toFloat()
            contentDescription=item.accessibility
        }
        card.addView(message(item.storefrontTitle,18,0xff5f260e.toInt()).apply{
            maxLines=2
            setShadowLayer(1.5f,0f,1.5f,0x66f8ba72)
        },LinearLayout.LayoutParams(-1,0,.58f))
        val row=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        row.addView(ImageView(host).apply{
            setImageDrawable(HomeHeroIconDrawable(
                if(item.isShield)BopaviUiIconDrawable.Kind.SHIELD
                else BopaviUiIconDrawable.Kind.MAGNET,dp(86)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(dp(68),dp(68)))
        row.addView(message(item.iconTierLabel,20,0xffffac17.toInt()).apply{
            setShadowLayer(2f,0f,2f,0xff6f300b.toInt())
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(dp(38),dp(47)))
        card.addView(row,LinearLayout.LayoutParams(-1,0,1.03f))
        card.addView(message(item.perkEffect,13,0xff723d20.toInt()).apply {
                maxLines=2
        },LinearLayout.LayoutParams(-1,0,.54f))
        card.addView(message("OWNED: ${item.owned}/99  •  TIER ${item.tier}",12,
            0xff6e5141.toInt()),LinearLayout.LayoutParams(-1,0,.38f))
        val buy=button(NumberFormat.getIntegerInstance().format(item.price),false,item.canBuy){
            onBuy(item.index)
        }.apply{
            background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.GREEN)
            setTextColor(Color.WHITE)
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                HomeHeroIconDrawable(BopaviUiIconDrawable.Kind.COIN,dp(26)),
                null,null,null)
            compoundDrawablePadding=dp(3)
            gravity=Gravity.CENTER
            contentDescription=item.accessibility+
                if(item.canBuy)". Buy for ${item.price} coins"
                else ". Purchase unavailable"
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

        // Fixed reference coordinates cannot fit native 48dp touch targets
        // on a 720px window, in landscape or at large font scaling.
        if(!ShopReferenceLayout.supports(
                host.resources.displayMetrics.widthPixels,
                host.resources.displayMetrics.heightPixels,
                host.resources.configuration.orientation==
                    Configuration.ORIENTATION_PORTRAIT,
                host.resources.configuration.fontScale)) {
            val scroll=ScrollView(host).apply {
                isFillViewport=true
                clipToPadding=false
                setOnApplyWindowInsetsListener { _,insets->
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
                setPadding(dp(12),dp(12),dp(12),dp(24))
            }
            column.addView(button("‹  HOME",false,click=onBack).apply{
                contentDescription="HOME"
            },LinearLayout.LayoutParams(-1,dp(56)))
            column.addView(message("SHOP",45),LinearLayout.LayoutParams(-1,dp(74)))
            val saved=progress.coins()
            column.addView(message("COINS  •  "+
                NumberFormat.getIntegerInstance().format(saved),24).apply {
                background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
                contentDescription="Earned coins: $saved"
            },LinearLayout.LayoutParams(-1,dp(61)).apply {
                topMargin=dp(6);bottomMargin=dp(10)
            })
            val stock=ShopItemState.fromStore(progress)
            val featuredItem=stock.last()
            column.addView(message("FEATURED: "+featuredItem.name.uppercase(),24).apply{
                background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.VIOLET)
                contentDescription="Featured: "+featuredItem.accessibility
            },LinearLayout.LayoutParams(-1,dp(67)))
            val earned=progress.bonusCoinsAvailable()
            if(earned>0){
                column.addView(button("CLAIM +$earned",true,click=onClaim),
                    LinearLayout.LayoutParams(-1,dp(52)).apply {topMargin=dp(7)})
            }
            for(item in stock){
                column.addView(perkCard(item,onBuy),
                    LinearLayout.LayoutParams(-1,dp(200)).apply {
                        topMargin=dp(10)
                    })
            }
            scroll.addView(column)
            root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
            scroll.requestApplyInsets()
            return root
        }
        val stage=LevelMapStage(host)
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        // Full storefront artwork is a separate passive native Canvas layer.
        // Dynamic prices, avatar, balances and buttons remain Android Views.
        stage.place(ShopStorefrontDecor(host),0f,0f,941f,1672f)
        // Native header, real locally saved counters and original character.
        stage.place(button("‹",false,click=onBack).apply{
            background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.SKY)
            contentDescription="HOME"
        },24f,18f,119f,99f,minTouch=true)
        stage.place(message("SHOP",83,0xffffdf43.toInt()).apply {
            setShadowLayer(4f,2f,6f,0xff6f230c.toInt())
        },291f,143f,359f,139f)
        val coins=progress.coins()
        val stars=(0 until LevelEngine.WORLD_COUNT).sumOf{progress.worldStars(it)}
        stage.place(message(NumberFormat.getIntegerInstance().format(coins),
            27).apply{
            background=plate(0xff2a91db.toInt(),navy,0xffacebff.toInt(),22)
            contentDescription="Earned coins: $coins"
            isFocusable=true
            setPadding(dp(24),0,dp(3),0)
        },466f,20f,201f,78f)
        stage.place(ImageView(host).apply {
            setImageDrawable(HomeHeroIconDrawable(
                BopaviUiIconDrawable.Kind.COIN,dp(31)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },463f,21f,60f,63f)
        stage.place(message(NumberFormat.getIntegerInstance().format(stars),
            27).apply{
            background=plate(0xff2a91db.toInt(),navy,0xffacebff.toInt(),22)
            contentDescription="Earned stars: $stars"
            isFocusable=true
            setPadding(dp(24),0,dp(3),0)
        },695f,20f,220f,78f)
        stage.place(ImageView(host).apply {
            setImageDrawable(BopaviUiIconDrawable(
                BopaviUiIconDrawable.Kind.GEM,0xfff554ef.toInt(),dp(34)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },694f,22f,65f,65f)
        stage.place(ImageView(host).apply {
            setImageResource(R.drawable.bopiportrait0)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },335f,289f,280f,270f)

        // A *real* featured perk; no fake gems, payment or random skin.
        val items=ShopItemState.fromStore(progress)
        val featured=items.last()
        stage.place(FrameLayout(host).apply {
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.VIOLET)
            contentDescription="Featured: "+featured.accessibility
            elevation=dp(8).toFloat()
        },ShopReferenceLayout.featureLeft,ShopReferenceLayout.featureTop,
            ShopReferenceLayout.featureWidth,ShopReferenceLayout.featureHeight)
        stage.place(message("FEATURED!",27),70f,578f,275f,62f)
        stage.place(message(featured.storefrontTitle,32,0xffffe680.toInt()),
            74f,642f,466f,71f)
        stage.place(message(featured.perkEffect.uppercase(),19),
            77f,707f,464f,47f)
        stage.place(message("TIER ${featured.tier}  •  OWNED ${featured.owned}/99",15),
            77f,728f,464f,32f)
        stage.place(ImageView(host).apply {
            setImageDrawable(HomeHeroIconDrawable(
                if(featured.isShield)BopaviUiIconDrawable.Kind.SHIELD
                else BopaviUiIconDrawable.Kind.MAGNET,dp(95)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },633f,614f,213f,159f)
        stage.place(button(featured.buttonLabel,true,featured.canBuy){
            onBuy(featured.index)
        }.apply {
            contentDescription="Featured "+featured.accessibility
        },111f,769f,402f,85f,minTouch=true)
        val bonus=progress.bonusCoinsAvailable()
        if(bonus>0) {
            stage.place(button("CLAIM +$bonus",true,click=onClaim).apply {
                contentDescription="Claim $bonus earned coins"
            },574f,770f,283f,85f,minTouch=true)
        }

        for(item in ShopItemState.fromStore(progress)){
            val row=item.index/3
            val col=item.index%3
            stage.place(perkCard(item,onBuy),
                ShopReferenceLayout.columns[col],
                ShopReferenceLayout.rows[row],
                ShopReferenceLayout.cardWidth,
                ShopReferenceLayout.cardHeight)
        }

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
