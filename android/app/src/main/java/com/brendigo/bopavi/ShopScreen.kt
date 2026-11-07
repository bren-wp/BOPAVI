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

    /**
     * Preserve a real native Button underneath a separately rendered price
     * capsule. Android text/compound drawable autosizing must never silently
     * remove the price on a physically narrow 941px QA viewport.
     *
     * The decorative row has NO click/keyboard/a11y action. A tap goes through
     * to the existing Button -> onBuy -> ProgressStore transaction.
     */
    private fun priceButton(price:Int,gold:Boolean,
                            accessibility:String,click:()->Unit):View {
        val shell=FrameLayout(host).apply {
            isClickable=false
            isFocusable=false
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val real=button("",gold,true,click).apply {
            background=Bopavi3DTheme.button(
                if(gold)Bopavi3DTheme.Finish.GOLD
                else Bopavi3DTheme.Finish.GREEN)
            contentDescription=accessibility
            setPadding(0,0,0,0)
            elevation=dp(1).toFloat()
        }
        shell.addView(real,FrameLayout.LayoutParams(-1,-1))
        val label=LinearLayout(host).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
            isClickable=false
            isFocusable=false
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            // The native button has a slight press shadow. This row must be
            // above that shadow, otherwise the coins remain but price vanishes.
            elevation=dp(2).toFloat()
        }
        val symbol=ImageView(host).apply {
            setImageDrawable(HomeHeroIconDrawable(
                BopaviUiIconDrawable.Kind.COIN,dp(21)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            isClickable=false
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        label.addView(symbol,LinearLayout.LayoutParams(dp(24),dp(24)))
        label.addView(message(NumberFormat.getIntegerInstance().format(price),
            17,if(gold)0xff672918.toInt() else Color.WHITE).apply {
            maxLines=1
            isClickable=false
            isFocusable=false
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setShadowLayer(2f,0f,2f,if(gold)0x55ffeb9b else 0xff174526.toInt())
        },LinearLayout.LayoutParams(-2,-1).apply{leftMargin=dp(5)})
        shell.addView(label,FrameLayout.LayoutParams(-1,-1))
        return shell
    }

    private fun perkCard(item:ShopItemState,onBuy:(Int)->Unit):View {
        // A native product card with four independent text/image regions,
        // not a flattened product screenshot. Existing six offline purchases
        // retain the exact ProgressStore index, price and stored inventory.
        val card=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(dp(5),dp(6),dp(5),dp(8))
            background=ShopCreamCardDrawable()
            elevation=dp(4).toFloat()
            contentDescription="OWNED: ${item.owned}/99. "+item.accessibility
        }
        card.addView(message(item.storefrontTitle,15,0xff5f260e.toInt()).apply{
            maxLines=2
            setShadowLayer(1.5f,0f,1.5f,0x66f8ba72)
        },LinearLayout.LayoutParams(-1,0,.72f))
        val row=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        // Flexible weights fill the reference card instead of multiplying
        // hardcoded dp icon widths by a device's physical pixel density.
        // The art stays fully inside the cream panel at every screen scale.
        row.addView(ImageView(host).apply{
            setImageDrawable(HomeHeroIconDrawable(
                if(item.isShield)BopaviUiIconDrawable.Kind.SHIELD
                else BopaviUiIconDrawable.Kind.MAGNET,dp(52)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(0,-1,2.15f))
        row.addView(message(item.iconTierLabel,15,0xffffac17.toInt()).apply{
            setShadowLayer(2f,0f,2f,0xff6f300b.toInt())
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(0,-1,.85f))
        card.addView(row,LinearLayout.LayoutParams(-1,0,1.55f))
        card.addView(message(item.perkEffect,13,0xff723d20.toInt()).apply {
                maxLines=2
        },LinearLayout.LayoutParams(-1,0,.65f))
        // Keep the true inventory count accessible without adding a fifth
        // visible row that the approved six-card storefront does not have.
        // Reference buy capsules stay saturated emerald even for a new
        // player. Tapping an unaffordable item gives truthful feedback;
        // it never mutates coins or bypasses ProgressStore.buyPerk().
        val buy=priceButton(item.price,false,item.accessibility+
            if(item.canBuy)". Buy for ${item.price} coins"
            else ". Tap for purchase requirements"){
            when {
                item.soldOut -> android.widget.Toast.makeText(host,
                    "Inventory full (99).",android.widget.Toast.LENGTH_SHORT).show()
                !item.affordable -> android.widget.Toast.makeText(host,
                    "Not enough coins.",android.widget.Toast.LENGTH_SHORT).show()
                else -> onBuy(item.index)
            }
        }
        card.addView(buy,LinearLayout.LayoutParams(-1,0,.90f).apply{
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
                intArrayOf(0x15082348,0x055cbfeb,0x39041d46))
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
            text=""
        },24f,18f,119f,99f,minTouch=true)
        // Match the owner arrow silhouette, without replacing the actual
        // HOME action with a decorative non-clickable screenshot.
        stage.place(ReferenceArrowOverlayView(host).apply {
            // Native blue back Button is elevated to 4dp; the arrow was
            // underneath its background and therefore invisible in QA.
            elevation=dp(6).toFloat()
        },24f,18f,119f,99f)
        stage.place(ShopEmbossedTitle(host),
            266f,136f,409f,154f)
        val coins=progress.coins()
        val stars=(0 until LevelEngine.WORLD_COUNT).sumOf{progress.worldStars(it)}
        // Two glass counters retain only REAL offline values. Their green
        // reference plus buttons are functional native touch surfaces: coin
        // bonus claims only genuine earned coins, while the star button
        // explains the actual level-based reward (no fictitious gems).
        stage.place(FrameLayout(host).apply {
            background=plate(0xff2a91db.toInt(),navy,0xffacebff.toInt(),22)
        },458f,20f,214f,78f)
        stage.place(message(NumberFormat.getIntegerInstance().format(coins),
            27).apply {
            contentDescription="Earned coins: $coins"
            isFocusable=true
        },520f,31f,101f,53f)
        stage.place(ImageView(host).apply {
            setImageDrawable(HomeHeroIconDrawable(
                BopaviUiIconDrawable.Kind.COIN,dp(31)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },461f,21f,59f,63f)
        stage.place(ShopPlusControl(host,
            if(progress.bonusCoinsAvailable()>0)"Claim earned coins"
            else "How to earn coins") {
            if(progress.bonusCoinsAvailable()>0)onClaim()
            else android.widget.Toast.makeText(host,
                "Earn coins by completing levels.",android.widget.Toast.LENGTH_SHORT).show()
        },598f,0f,96f,99f,minTouch=true)

        stage.place(FrameLayout(host).apply {
            background=plate(0xff2a91db.toInt(),navy,0xffacebff.toInt(),22)
        },695f,20f,220f,78f)
        stage.place(message(NumberFormat.getIntegerInstance().format(stars),
            27).apply {
            contentDescription="Earned stars: $stars"
            isFocusable=true
        },762f,31f,102f,53f)
        stage.place(ImageView(host).apply {
            setImageDrawable(BopaviUiIconDrawable(
                BopaviUiIconDrawable.Kind.GEM,0xfff554ef.toInt(),dp(34)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },694f,22f,65f,65f)
        stage.place(ShopPlusControl(host,"How to earn stars") {
            android.widget.Toast.makeText(host,
                "Earn stars by completing levels.",android.widget.Toast.LENGTH_SHORT).show()
        },833f,0f,96f,99f,minTouch=true)
        stage.place(ImageView(host).apply {
            setImageResource(R.drawable.bopiportrait0)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        // Bopi fills the illustrated counter opening rather than looking
        // like a tiny floating badge. Preserve the full source square and
        // let transparent wings/scarf reach the owner's shelf silhouette.
        },130f,235f,570f,420f)

        // A *real* featured perk; no fake gems, payment or random skin.
        val items=ShopItemState.fromStore(progress)
        val featured=items.last()
        stage.place(FrameLayout(host).apply {
            background=ShopFeaturedPanelDrawable()
            contentDescription="Featured: "+featured.accessibility
            // A raised background sorts above later sibling labels on Android.
            // Keep the purple panel at Z=0 so native item text/icon/CTA show.
        },ShopReferenceLayout.featureLeft,ShopReferenceLayout.featureTop,
            ShopReferenceLayout.featureWidth,ShopReferenceLayout.featureHeight)
        stage.place(message("FEATURED!",23).apply {
            background=plate(0xffff4b86.toInt(),0xffab0341.toInt(),
                0xffffafc5.toInt(),8)
            setShadowLayer(2f,1f,3f,0xff6b0632.toInt())
        },70f,578f,275f,62f)
        stage.place(message(featured.storefrontTitle,32,0xffffe680.toInt()),
            74f,642f,466f,71f)
        stage.place(message(featured.perkEffect.uppercase(),19),
            77f,707f,464f,47f)
        // The featured panel already exposes the real owned quantity to
        // TalkBack; avoid an extra visible tier line across the reference art.
        stage.place(ImageView(host).apply {
            setImageDrawable(HomeHeroIconDrawable(
                if(featured.isShield)BopaviUiIconDrawable.Kind.SHIELD
                else BopaviUiIconDrawable.Kind.MAGNET,dp(95)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },516f,593f,343f,253f)
        stage.place(priceButton(featured.price,true,
            "Featured "+featured.accessibility){
            if(featured.canBuy)onBuy(featured.index)
            else android.widget.Toast.makeText(host,
                if(featured.soldOut)"Inventory full (99)." else "Not enough coins.",
                android.widget.Toast.LENGTH_SHORT).show()
        },96f,771f,322f,84f,minTouch=true)
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

        // Entirely passive flower/leaf foreground, preserving all touches.
        stage.place(ShopForegroundFoliage(host),0f,0f,941f,1672f)

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
