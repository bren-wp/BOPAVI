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
import java.text.NumberFormat

/**
 * Owner's reference-style Skins composition at 941×1672.
 *
 * One native selected hero, three historical pilots and a genuine fourth
 * Star Pilot earned through the existing Orion cosmetic skin.
 * Each selectable card and equipped action remains a real Android view.
 */
internal class PilotSelectionScreen(
    private val host:Activity,
    private val progress:ProgressStore
) {
    private fun dp(n:Int)=(n*host.resources.displayMetrics.density+.5f).toInt()
    private val navy=0xff09295b.toInt()
    private fun text(label:String,size:Float,color:Int=Color.WHITE):TextView =
        TextView(host).apply {
            text=label
            textSize=size
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(color)
            gravity=Gravity.CENTER
            // Android rejects minSize == maxSize for uniform auto-size.
            // The 12sp card-status labels have a fixed size instead of a
            // nonsensical 12..12 interval that crashes Skins on API 35.
            if(size.toInt()>12) {
                setAutoSizeTextTypeUniformWithConfiguration(12,size.toInt(),1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
            }
            maxLines=2
        }

    private fun action(label:String,primary:Boolean,onClick:()->Unit):Button =
        Button(host).apply {
            text=label
            isAllCaps=false
            textSize=25f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(if(primary)0xff67320d.toInt() else Color.WHITE)
            background=Bopavi3DTheme.button(if(primary)
                Bopavi3DTheme.Finish.GOLD else Bopavi3DTheme.Finish.SKY)
            gravity=Gravity.CENTER
            minimumHeight=dp(48)
            setAutoSizeTextTypeUniformWithConfiguration(15,25,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            elevation=dp(5).toFloat()
            contentDescription=label
            setOnClickListener{onClick()}
        }

    /** Round native navigation buttons, never screenshot-driven hotspots. */
    private fun cycleArrow(label:String,description:String,onClick:()->Unit):Button =
        Button(host).apply {
            // The white outlined chevron is painted into the native button
            // background, avoiding a font-dependent ‹/› substitution.
            text=""
            textSize=43f
            isAllCaps=false
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x66ffffff),
                SkinsCarouselArrowDrawable(right=label=="›"),null)
            elevation=dp(5).toFloat()
            minimumHeight=dp(48)
            contentDescription=description
            isClickable=true
            isFocusable=true
            setOnClickListener { onClick() }
        }

    fun create(choice:Int,onChoose:(Int)->Unit,onUnlock:()->Unit,
               onShop:()->Unit,onBack:()->Unit):View {
        val selected=PilotSelectionModel.safeChoice(choice)
        val root=FrameLayout(host).apply{setBackgroundColor(0xff138cdd.toInt())}
        root.addView(ImageView(host).apply {
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(host).apply {
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x22022148,0x00269be5,0x88021c43.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val stage=LevelMapStage(host)
        val dm=host.resources.displayMetrics
        if(host.resources.configuration.orientation==
                android.content.res.Configuration.ORIENTATION_PORTRAIT &&
            dm.widthPixels>=860 && dm.heightPixels>=1480 &&
            host.resources.configuration.fontScale<1.25f) {
            // Keep the complete owner art and native hero/card geometry on
            // the same full-bleed camera; safe-inset fallback remains active
            // for accessible touch targets near a notch.
            stage.useShopFullBleedReferenceCamera()
        }
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))
        // Decorative wood sign, ropes, grass and layered stone podium;
        // actual pilot choice and wallets remain native and clickable.
        stage.place(SkinsReferenceDecor(host),0f,0f,941f,1672f,
            referenceAligned=true)

        // Unified enamel header, with a genuine Back button over one surface.
        stage.place(FrameLayout(host).apply {
            background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.SKY)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },19f,17f,335f,105f)
        stage.place(action("",false,onBack).apply {
            contentDescription="Back to Home"
            background=android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
            elevation=0f
        },23f,21f,112f,96f,minTouch=true)
        stage.place(ReferenceArrowOverlayView(host).apply{
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            isClickable=false
            elevation=dp(6).toFloat()
        },23f,21f,112f,96f)
        stage.place(text("SKINS",39f).apply {
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setShadowLayer(dp(3).toFloat(),0f,dp(3).toFloat(),
                0xff092b68.toInt())
        },137f,21f,204f,96f)

        // Two live blue wallet capsules. These are real earned coins and
        // ratings, never fake spendable gems. Green + opens the actual Shop.
        val coins=progress.coins()
        val totalStars=(0 until LevelEngine.WORLD_COUNT).sumOf{
            progress.worldStars(it)
        }
        fun wallet(value:Int,description:String):TextView=text(
            NumberFormat.getIntegerInstance().format(value),25f
        ).apply {
            contentDescription="$description: $value"
            maxLines=1
            setAutoSizeTextTypeUniformWithConfiguration(14,25,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
        }
        stage.place(FrameLayout(host).apply{
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },454f,21f,214f,76f)
        stage.place(ImageView(host).apply{
            setImageDrawable(HomeHeroIconDrawable(
                BopaviUiIconDrawable.Kind.COIN,dp(31)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },455f,23f,63f,65f)
        stage.place(wallet(coins,"Earned coins"),522f,27f,104f,60f)
        stage.place(ShopPlusControl(host,"Open Shop for coins"){onShop()},
            591f,1f,98f,98f,minTouch=true)

        stage.place(FrameLayout(host).apply{
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },694f,21f,220f,76f)
        stage.place(ImageView(host).apply{
            setImageDrawable(BopaviUiIconDrawable(
                BopaviUiIconDrawable.Kind.STAR,0xffffcf3d.toInt(),dp(34)))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },695f,23f,65f,65f)
        stage.place(wallet(totalStars,"Earned stars"),762f,27f,111f,60f)
        stage.place(ShopPlusControl(host,"Open Shop for rewards"){onShop()},
            830f,1f,98f,98f,minTouch=true)

        val item=PilotSelectionModel.pilots[selected]
        val skins=intArrayOf(R.drawable.bopiportrait0,
            R.drawable.bopiportrait9,R.drawable.bopiportrait10,R.drawable.bopiportrait8)

        // Wood-colored hanging-name plaque is decorative but the selected
        // title remains a real, accessible TextView in the hierarchy.
        stage.place(SkinsEmbossedName(host,item.title),
            PilotSelectionLayout.bannerLeft,PilotSelectionLayout.bannerTop,
            PilotSelectionLayout.bannerWidth,PilotSelectionLayout.bannerHeight)
        stage.place(text(item.shortName,20f,0xffffeec3.toInt()),389f,253f,162f,53f)

        // The original transparent chosen hero hovers over the separately
        // painted stone platform; no flat oval hides the layered podium.
        // The owner's large floating Skins hero is the COMPLETE winged
        // aviator, not the face-only portrait used for small selection cards.
        // These original transparent 512×512 layers align exactly and share
        // the same saved choice-to-sprite mapping as Splash/Home/Gameplay.
        val heroId=PilotSelectionModel.spriteIdForChoice(selected)
        val heroBodies=intArrayOf(R.drawable.bopi0,R.drawable.bopi9,R.drawable.bopi10,R.drawable.bopi8)
        val heroLeftWings=intArrayOf(
            R.drawable.bopileft0,R.drawable.bopileft9,R.drawable.bopileft10,R.drawable.bopileft8)
        val heroRightWings=intArrayOf(
            R.drawable.bopiright0,R.drawable.bopiright9,R.drawable.bopiright10,R.drawable.bopiright8)
        val heroChoice=when(heroId){0->0;9->1;10->2;else->3}
        stage.place(HomeWingActor(host,
            heroLeftWings[heroChoice],heroRightWings[heroChoice],
            heroBodies[heroChoice],allowMotion=!progress.lessMotion()),
            PilotSelectionLayout.heroLeft,PilotSelectionLayout.heroTop,
            PilotSelectionLayout.heroSize,PilotSelectionLayout.heroSize)
        val pilotCount=PilotSelectionModel.pilots.size
        val orionOwned=progress.owned(8)
        fun chooseOrUnlock(index:Int) {
            if(PilotSelectionModel.locked(index,orionOwned))onUnlock()
            else onChoose(index)
        }
        stage.place(cycleArrow("‹","Previous hero"){
            chooseOrUnlock((selected+pilotCount-1)%pilotCount)
        },26f,613f,112f,112f,minTouch=true)
        stage.place(cycleArrow("›","Next hero"){
            chooseOrUnlock((selected+1)%pilotCount)
        },803f,613f,112f,112f,minTouch=true)

        // Four real native cards, with Orion locked until owned(8).
        for(candidate in PilotSelectionModel.pilots) {
            val index=candidate.id
            val isSelected=PilotSelectionModel.selected(selected,index)
            val isLocked=PilotSelectionModel.locked(index,orionOwned)
            val card=LinearLayout(host).apply {
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER_HORIZONTAL
                setPadding(dp(5),dp(5),dp(5),dp(7))
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    SkinsCardEnamelDrawable(isSelected,isLocked),null)
                isClickable=true
                isFocusable=true
                minimumHeight=dp(48)
                contentDescription=PilotSelectionModel.accessibility(
                    selected,index,orionOwned) +
                    if(isLocked)" — ${progress.costs[8]} coins" else ""
                setOnClickListener{chooseOrUnlock(index)}
                elevation=dp(if(isSelected)9 else 4).toFloat()
            }
            // Use only the illustrated portrait region from the user's
            // supplied Skins reference. The live native card still owns
            // its label, status, purchase confirmation and click handler.
            card.addView(OwnerSkinsPortraitView(host,index,skins[index]).apply {
                alpha=if(isLocked).78f else 1f
            },LinearLayout.LayoutParams(-1,0,1.70f))
            card.addView(text(candidate.title.lowercase()
                .split(" ").joinToString(" "){word->
                    word.replaceFirstChar { ch->ch.titlecase() }
                },19f,if(isSelected)navy else Color.WHITE).apply{
                // Card captions are at most two lines in the 941px owner
                // screenshot; previously 69px for two 19sp lines clipped.
                maxLines=2
                isSingleLine=false
                includeFontPadding=false
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,0,.82f))
            // Lock state and its genuine coin price remain in TalkBack and
            // the native purchase dialog. The owner card uses a lock glyph,
            // not an extra tiny price caption that clipped beneath the name.
            card.addView(text("",11f,Color.WHITE).apply{
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,0,.08f))
            val left=PilotSelectionLayout.cardLefts[index]
            stage.place(card,left,PilotSelectionLayout.cardTop,
                PilotSelectionLayout.cardWidth,PilotSelectionLayout.cardHeight,
                minTouch=true)
            if(isLocked) {
                // Passive overlay, no touch capture; the actual card is live.
                stage.place(LevelMapLockGlyph(host),left+154f,
                    PilotSelectionLayout.cardTop+8f,50f,54f)
            } else if(isSelected) {
                stage.place(ImageView(host).apply {
                    setImageDrawable(SkinsEquippedBadgeDrawable())
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    isClickable=false
                },left+158f,PilotSelectionLayout.cardTop+4f,55f,55f)
            }
        }

        val selectedInfo=LinearLayout(host).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(9),dp(8),dp(9),dp(8))
            background=SkinsSelectedInfoDrawable()
            contentDescription="Selected "+item.title+". "+item.ability.description
            val details=LinearLayout(host).apply {
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER_VERTICAL
                addView(text(item.title.lowercase().split(" ").joinToString(" "){
                    word->word.replaceFirstChar { ch->ch.titlecase() }
                },30f,Color.WHITE).apply {
                    gravity=Gravity.START or Gravity.CENTER_VERTICAL
                    setPadding(dp(11),0,0,0)
                },LinearLayout.LayoutParams(-1,0,.42f))
                addView(text(if(selected==0)
                    "The original hero, always ready for a new adventure!"
                    else item.ability.description,
                    18f,0xffe2f8ff.toInt()).apply {
                    gravity=Gravity.START or Gravity.CENTER_VERTICAL
                    setPadding(dp(11),0,0,0)
                },LinearLayout.LayoutParams(-1,0,.58f))
            }
            addView(details,LinearLayout.LayoutParams(0,-1,.62f))
            addView(Button(host).apply{
                text=if(PilotSelectionModel.locked(selected,orionOwned))
                    "UNLOCK" else "EQUIP"
                isAllCaps=false
                textSize=30f
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(Color.WHITE)
                background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.GREEN)
                contentDescription=(if(PilotSelectionModel.locked(selected,orionOwned))
                    "Unlock " else "Equip ")+item.title
                isClickable=true
                isFocusable=true
                minimumHeight=dp(48)
                elevation=dp(3).toFloat()
                setOnClickListener {
                    // Keep the bottom CTA behind the same owned(8) gate as
                    // the native selection cards and carousel navigation.
                    if(PilotSelectionModel.locked(selected,orionOwned))onUnlock()
                    else onChoose(selected)
                }
            },LinearLayout.LayoutParams(0,-1,.38f).apply{
                leftMargin=dp(4)
            })
        }
        stage.place(selectedInfo,30f,1299f,881f,193f)
        // The reference ends at the genuine selected-info panel; no extra
        // misleading "FREE • UNLOCKED" footer under the earnable fourth hero.

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
