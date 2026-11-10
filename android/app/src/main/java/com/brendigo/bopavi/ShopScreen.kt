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
     * Price is rendered by the actual clickable Android Button rather than
     * by a passive sibling overlay. The old decorative overlay disappeared
     * on GPU-rendered 941px captures, leaving blank gold/green capsules.
     * Native text, symbol and click now share a single guaranteed draw pass.
     */
    private fun priceButton(price:Int,gold:Boolean,
                            accessibility:String,click:()->Unit):View {
        return button(NumberFormat.getIntegerInstance().format(price),
            gold,true,click).apply {
            textSize=if(gold)26f else 22f
            setAutoSizeTextTypeUniformWithConfiguration(17,if(gold)26 else 22,
                1,android.util.TypedValue.COMPLEX_UNIT_SP)
            // The owner-sized 270px card uses weighted rows; native Button's
            // default vertical insets previously clipped the price baseline.
            setPadding(dp(4),0,dp(4),0)
            minimumHeight=0
            background=Bopavi3DTheme.button(
                if(gold)Bopavi3DTheme.Finish.GOLD
                else Bopavi3DTheme.Finish.GREEN)
            setTextColor(if(gold)0xff5b240c.toInt() else Color.WHITE)
            setSingleLine(true)
            maxLines=1
            val coin=HomeHeroIconDrawable(
                BopaviUiIconDrawable.Kind.COIN,dp(22))
            coin.setBounds(0,0,dp(25),dp(25))
            setCompoundDrawablesRelative(coin,null,null,null)
            compoundDrawablePadding=dp(7)
            gravity=Gravity.CENTER
            contentDescription=accessibility
            elevation=dp(3).toFloat()
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
            background=ShopCreamCardDrawable()
            elevation=dp(4).toFloat()
            contentDescription="OWNED: ${item.owned}/99. "+item.accessibility
        }
        card.addView(message(item.storefrontTitle,17,0xff5f260e.toInt()).apply{
            maxLines=2
            setShadowLayer(1.5f,0f,1.5f,0x66f8ba72)
        },LinearLayout.LayoutParams(-1,0,.65f))
        // Product art now occupies the full card width, as in the reference.
        // Tier remains a small passive corner badge instead of squeezing the
        // actual shield/magnet into a narrow horizontal row.
        val illustration=FrameLayout(host).apply {
            clipChildren=true
            clipToPadding=true
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        illustration.addView(ShopPerkArtwork(host,item),
            FrameLayout.LayoutParams(-1,-1))
        illustration.addView(message(item.iconTierLabel,15,
            0xffffae22.toInt()).apply {
            maxLines=1
            setShadowLayer(2f,0f,2f,0xff633112.toInt())
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(dp(45),dp(40),
            Gravity.TOP or Gravity.RIGHT).apply{
            topMargin=dp(2);rightMargin=dp(3)
        })
        card.addView(illustration,LinearLayout.LayoutParams(-1,0,1.50f))
        card.addView(message(item.perkEffect,14,0xff723d20.toInt()).apply {
                maxLines=2
        },LinearLayout.LayoutParams(-1,0,.48f))
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
        card.addView(buy,LinearLayout.LayoutParams(-1,0,1.30f).apply{
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
        // On the portrait reference camera the entire Shop must occupy the
        // original 941×1672 artwork grid, not a safe-inset-scaled miniature.
        // Interactive buttons retain the native cutout safety fallback.
        stage.useShopFullBleedReferenceCamera()
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        // Full storefront artwork is a separate passive native Canvas layer.
        // Dynamic prices, avatar, balances and buttons remain Android Views.
        stage.place(ShopStorefrontDecor(host),0f,0f,941f,1672f)
        // Native header, real locally saved counters and original character.
        // One native clickable control paints its own enamel and arrow,
        // avoiding the displaced twin-button look on API36.
        stage.place(ShopBackControl(host,onBack),
            24f,18f,119f,99f,minTouch=true)
        // SHOP is a static brand inscription. Display the illustrated
        // title from the owner's supplied 941x1672 reference, while wallets,
        // product text, real buy actions and navigation remain native Views.
        stage.place(ImageView(host).apply {
            setImageResource(R.drawable.bopavi_owner_shop_title)
            scaleType=ImageView.ScaleType.FIT_XY
            contentDescription="SHOP"
            isClickable=false
            isFocusable=false
        },183f,123f,575f,201f)
        val coins=progress.coins()
        val stars=(0 until LevelEngine.WORLD_COUNT).sumOf{progress.worldStars(it)}
        // Two glass counters retain only REAL offline values (coins and stars). Their green
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
        // An earned STAR balance must not use a GEM pictogram: BOPAVI has
        // no spendable gem wallet, and implying one would mislead players.
        stage.place(ImageView(host).apply {
            setImageDrawable(BopaviUiIconDrawable(
                BopaviUiIconDrawable.Kind.STAR,0xffffcf3d.toInt(),dp(34)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },694f,22f,65f,65f)
        stage.place(ShopPlusControl(host,"How to earn stars") {
            android.widget.Toast.makeText(host,
                "Earn stars by completing levels.",android.widget.Toast.LENGTH_SHORT).show()
        },833f,0f,96f,99f,minTouch=true)
        stage.place(ImageView(host).apply {
            setImageResource(R.drawable.bopiportrait0)
            scaleType=ImageView.ScaleType.FIT_CENTER
            // Reference Bopi leans across the counter rather than appearing
            // as a small badge; enlarge the actual local transparent sprite.
            scaleX=1.26f
            scaleY=1.26f
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        // Bopi fills the illustrated counter opening rather than looking
        // like a tiny floating badge. Preserve the full source square and
        // let transparent wings/scarf reach the owner's shelf silhouette.
        },105f,211f,630f,460f)
        // A separate raised wood lip obscures the lower character edge,
        // so Bopi is BEHIND the counter rather than floating over its front.
        // The strip is purely decorative and never intercepts purchases.
        stage.place(ShopCounterFront(host),23f,546f,895f,36f)

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
        // Golden Bopi is the reference hero's real, local, full-winged
        // variant. It is decorative: the CTA still explicitly purchases
        // the featured genuine magnet/shield perk, NOT an unimplemented skin.
        stage.place(ImageView(host).apply {
            setImageResource(R.drawable.bopiportrait1)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },410f,573f,510f,296f)
        stage.place(ImageView(host).apply {
            setImageDrawable(HomeHeroIconDrawable(
                if(featured.isShield)BopaviUiIconDrawable.Kind.SHIELD
                else BopaviUiIconDrawable.Kind.MAGNET,dp(40)))
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },805f,768f,66f,63f)
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
