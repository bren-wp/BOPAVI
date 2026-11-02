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
 * One native selected hero above three real playable character thumbnails:
 * no locked fake skins, sample wallets, bitmap UI overlays or network calls.
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
            setAutoSizeTextTypeUniformWithConfiguration(12,size.toInt(),1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
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

    fun create(choice:Int,onChoose:(Int)->Unit,onBack:()->Unit):View {
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
        root.addView(stage,FrameLayout.LayoutParams(-1,-1))

        // Both counters show genuine locally-earned currency only.
        stage.place(action("‹",false,onBack).apply{
            contentDescription="Back to Home"
        },18f,16f,123f,99f,minTouch=true)
        stage.place(text("SKINS",41f).apply{
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
        },148f,18f,215f,99f)
        val coins=progress.coins()
        val totalStars=(0 until LevelEngine.WORLD_COUNT).sumOf{
            progress.worldStars(it)
        }
        fun wallet(label:String,count:Int):TextView=text(
            "$label  "+NumberFormat.getIntegerInstance().format(count),20f
        ).apply {
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
            contentDescription="$label: $count"
        }
        stage.place(wallet("COINS",coins),466f,21f,203f,72f)
        stage.place(wallet("STARS",totalStars),696f,21f,215f,72f)

        val item=PilotSelectionModel.pilots[selected]
        val skins=intArrayOf(R.drawable.bopiportrait0,
            R.drawable.bopiportrait9,R.drawable.bopiportrait10)

        // Wood-colored hanging-name plaque is decorative but the selected
        // title remains a real, accessible TextView in the hierarchy.
        stage.place(text(item.title,38f,Color.WHITE).apply {
            background=Bopavi3DTheme.custom(
                0xffb87a42.toInt(),0xff6c3918.toInt(),0xffffdfac.toInt())
            contentDescription=item.title
        },PilotSelectionLayout.bannerLeft,PilotSelectionLayout.bannerTop,
            PilotSelectionLayout.bannerWidth,PilotSelectionLayout.bannerHeight)
        stage.place(text(item.shortName,23f,0xffffeec3.toInt()),354f,266f,233f,52f)

        // Genuine transparent original character portrait; the platform is
        // a separate native enamel oval and never a fake clickable screenshot.
        stage.place(View(host).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xffffec9b.toInt(),0xffb37338.toInt())).apply {
                shape=GradientDrawable.OVAL
                setStroke(dp(5),0xfffff4c9.toInt())
            }
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },PilotSelectionLayout.platformLeft,PilotSelectionLayout.platformTop,
            PilotSelectionLayout.platformWidth,PilotSelectionLayout.platformHeight)
        // The owner's large floating Skins hero is the COMPLETE winged
        // aviator, not the face-only portrait used for small selection cards.
        // These original transparent 512×512 layers align exactly and share
        // the same saved choice-to-sprite mapping as Splash/Home/Gameplay.
        val heroId=PilotSelectionModel.spriteIdForChoice(selected)
        val heroBodies=intArrayOf(R.drawable.bopi0,R.drawable.bopi9,R.drawable.bopi10)
        val heroLeftWings=intArrayOf(
            R.drawable.bopileft0,R.drawable.bopileft9,R.drawable.bopileft10)
        val heroRightWings=intArrayOf(
            R.drawable.bopiright0,R.drawable.bopiright9,R.drawable.bopiright10)
        val heroChoice=when(heroId){0->0;9->1;else->2}
        stage.place(HomeWingActor(host,
            heroLeftWings[heroChoice],heroRightWings[heroChoice],
            heroBodies[heroChoice],allowMotion=!progress.lessMotion()),
            PilotSelectionLayout.heroLeft,PilotSelectionLayout.heroTop,
            PilotSelectionLayout.heroSize,PilotSelectionLayout.heroSize)

        // Three genuinely available pilots. The reference illustrates four
        // cosmetic examples, but a fictitious fourth item would break the
        // actual saved three-pilot selection semantics.
        for(candidate in PilotSelectionModel.pilots) {
            val index=candidate.id
            val isSelected=PilotSelectionModel.selected(selected,index)
            val card=LinearLayout(host).apply {
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER_HORIZONTAL
                setPadding(dp(6),dp(5),dp(6),dp(9))
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    Bopavi3DTheme.panel(if(isSelected)
                        Bopavi3DTheme.Finish.CREAM else
                        Bopavi3DTheme.Finish.SKY),null)
                isClickable=true;isFocusable=true
                minimumHeight=dp(48)
                contentDescription=PilotSelectionModel.accessibility(selected,index)
                setOnClickListener{onChoose(index)}
                elevation=dp(if(isSelected)9 else 4).toFloat()
            }
            card.addView(ImageView(host).apply {
                setImageResource(skins[index])
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,0,1.55f))
            card.addView(text(candidate.title,19f,if(isSelected)navy else Color.WHITE),
                LinearLayout.LayoutParams(-1,0,.38f))
            card.addView(text(if(isSelected)"✓ EQUIPPED" else "EQUIP",
                17f,if(isSelected)0xff247128.toInt() else Color.WHITE),
                LinearLayout.LayoutParams(-1,0,.27f))
            stage.place(card,PilotSelectionLayout.cardLefts[index],
                PilotSelectionLayout.cardTop,
                PilotSelectionLayout.cardWidth,PilotSelectionLayout.cardHeight,
                minTouch=true)
        }

        val selectedInfo=LinearLayout(host).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(15),dp(8),dp(12),dp(7))
            background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)
            addView(text(item.title,30f,Color.WHITE),
                LinearLayout.LayoutParams(-1,0,.48f))
            addView(text(item.ability.shortTitle+" — "+item.ability.description,
                    19f,0xffe2f8ff.toInt()),
                LinearLayout.LayoutParams(-1,0,.52f))
            contentDescription="Selected "+item.title+". "+item.ability.description
        }
        stage.place(selectedInfo,49f,1315f,842f,150f)
        // This is informative rather than a nonfunctional purchasable CTA.
        stage.place(text(PilotSelectionModel.priceLabel(),19f,Color.WHITE).apply{
            contentDescription="All pilots are free and unlocked"
        },296f,1477f,350f,57f)

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
