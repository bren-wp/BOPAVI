package com.brendigo.bopavi

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/** Fully native Android activity, controls and GPU Canvas game surface. */
class MainActivity : Activity() {
    private lateinit var progress: ProgressStore
    private lateinit var sound: Soundscape
    private var gameView: GameView? = null
    private var gamePauseButton: Button? = null
    private var platformBackCallback: android.window.OnBackInvokedCallback? = null
    private var selectedScreen = "home"
    private val textColor = Color.WHITE
    private val gold = 0xffffc24d.toInt()
    private val electric = 0xff00d9ff.toInt()
    // Three arcade pilots preserve all historical save-backed skin identifiers.
    private val pilotNames=arrayOf("Bopi","Sova","Lisac")
    private val pilotSpriteIds=intArrayOf(0,9,10)
    private val portraitDrawableIds=intArrayOf(R.drawable.bopiportrait0,R.drawable.bopiportrait9,R.drawable.bopiportrait10)
    private var selectedPilotChoice=0
    private fun d(n:Int):Int = (resources.displayMetrics.density*n+.5f).toInt()
    /** Full-bleed game menus, with separately reserved camera cutout insets. */
    @Suppress("DEPRECATION")
    private fun applyPremiumSystemBars() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
    override fun onCreate(state: Bundle?) {
        setTheme(R.style.BopaviTheme)
        super.onCreate(state)
        window.statusBarColor=0xff09264a.toInt()
        window.navigationBarColor=0xff09264a.toInt()
        applyPremiumSystemBars()
        // A single OS-managed launch transition, followed directly by Home.
        // No second BOPAVI splash and no artificial loading percentage.
        progress=ProgressStore(this)
        selectedPilotChoice=progress.arcadePilot()
        sound=Soundscape(this)
        sound.enabled=progress.soundEnabled()
        sound.musicVolume=progress.musicVolume()/100f
        sound.effectsVolume=progress.effectsVolume()/100f
        if(Build.VERSION.SDK_INT>=33){
            val callback=android.window.OnBackInvokedCallback{navigateBack()}
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,callback)
            platformBackCallback=callback
        }
        showHome()
    }
    private fun showNativeView(root: View) {
        // Keep the scene artwork edge-to-edge. Safe-area-fitted menus (home,
        // every submenu and results) handle cutout/nav insets for controls.
        // Padding the root FrameLayout as well creates duplicate insets and
        // unwanted solid bands around the background artwork on API 35/36.
        setContentView(root)
    }
    override fun onPause() {
        // An idle preview has no visible pause button; only pause active flights.
        if(gameView?.game?.active == true) {
            gameView?.paused = true
            gamePauseButton?.text = "▶"
            gamePauseButton?.contentDescription =
                "Nastavi ili izađi iz pauze"
        }
        if(::sound.isInitialized) sound.pause()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        if(::sound.isInitialized && gameView?.game?.active == false && gameView?.paused == false) sound.resume()
    }
    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= 33) {
            platformBackCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            platformBackCallback = null
        }
        gameView?.paused = true
        if (::sound.isInitialized) sound.close()
        super.onDestroy()
    }
    // BOPAVI premium visual language: royal blue, cyan outlines, gold CTA.
    private fun gradient(a:Int,b:Int,rad:Int=27):GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(a,b)).apply{
            cornerRadius=d(rad).toFloat()
            setStroke(d(1),0x9959d6ff.toInt())
        }
    /** Native accessible text treatment shared by Home, Pause and Results. */
    private fun title(parent:LinearLayout,s:String,size:Int,color:Int=textColor) {
        parent.addView(TextView(this).apply {
            text=s
            textSize=size.toFloat()
            setTextColor(color)
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            gravity=Gravity.CENTER
            setPadding(d(6),d(11),d(6),d(11))
            // Shared illustrated-title treatment from the approved UI board:
            // a recessed midnight-blue body, blue illuminated lip and gold
            // accent. Keep the caption native for accessibility/localization.
            if(size>=26) {
                background=GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xff1b9eff.toInt(),0xff0759bf.toInt(),0xff061b49.toInt())).apply {
                    cornerRadius=d(24).toFloat()
                    setStroke(d(2),0xff72e8ff.toInt())
                }
                elevation=d(5).toFloat()
                letterSpacing=.035f
            }
            setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),0xff001936.toInt())
            setAutoSizeTextTypeUniformWithConfiguration((size*.72f).toInt(),size,1,android.util.TypedValue.COMPLEX_UNIT_SP)
        },LinearLayout.LayoutParams(-1,-2))
    }
    private fun action(parent:LinearLayout,text:String,primary:Boolean=true,muted:Boolean=false,onClick:()->Unit) {
        val button=Button(this).apply {
            this.text=text;setTextColor(Color.WHITE)
            textSize=18f;isAllCaps=false
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            letterSpacing=.03f
            setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),0xaa07244d.toInt())
            setAutoSizeTextTypeUniformWithConfiguration(12,18,1,android.util.TypedValue.COMPLEX_UNIT_SP)
            val face=if(primary) GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xffffdf50.toInt(),0xffffad17.toInt(),0xffff7a00.toInt())).apply{
                    cornerRadius=d(33).toFloat()
                    setStroke(d(3),0xffffed9e.toInt())
                } else if(muted) GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xff91b7e3.toInt(),0xff436eaa.toInt(),0xff203960.toInt())).apply{
                    cornerRadius=d(32).toFloat()
                    setStroke(d(2),0xffd9edff.toInt())
                } else GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xff23c8fc.toInt(),0xff087dec.toInt(),0xff064cb4.toInt())).apply{
                    cornerRadius=d(32).toFloat()
                    setStroke(d(2),0xff7eecff.toInt())
                }
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),face,null)
            elevation=d(7).toFloat()
            contentDescription=text
            setOnClickListener{sound.effect("click");onClick()}
        }
        parent.addView(button,LinearLayout.LayoutParams(-1,d(if(primary)68 else 58)).apply{
            setMargins(0,d(5),0,d(5))
        })
    }
    /** Distinct native, high-contrast Back affordance; never a painted, inert icon. */
    private fun backAction(parent:LinearLayout,caption:String,onClick:()->Unit) {
        val face=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xff246fba.toInt(),0xff0a376d.toInt())).apply {
            cornerRadius=d(29).toFloat()
            setStroke(d(2),0xffa3eaff.toInt())
        }
        val back=Button(this).apply {
            text="←  $caption"
            contentDescription="Povratak na početnu"
            isAllCaps=false
            textSize=18f
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            letterSpacing=.025f
            setTextColor(Color.WHITE)
            setAutoSizeTextTypeUniformWithConfiguration(14,18,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),face,null)
            minHeight=d(56)
            elevation=d(3).toFloat()
            setOnClickListener { sound.effect("click"); onClick() }
        }
        parent.addView(back,LinearLayout.LayoutParams(-1,d(58)).apply {
            setMargins(0,d(5),0,d(5))
        })
    }
    /** Full legal documents are bundled offline and are not abbreviated.
     * Native text rendering needs no WebView, JavaScript or network access.
     */
    private fun showLegalNotice(title:String,file:String) {
        val markup=assets.open("legal/$file").bufferedReader(Charsets.UTF_8).use {
            it.readText()
        }
        val body=TextView(this).apply {
            text=android.text.Html.fromHtml(markup,android.text.Html.FROM_HTML_MODE_COMPACT)
            // Enable official links and support e-mail inside the full policy.
            // Source HTML is bundled and validated by the CI URL allowlist.
            movementMethod=android.text.method.LinkMovementMethod.getInstance()
            linksClickable=true
            textSize=15f
            setTextColor(0xff173451.toInt())
            setPadding(d(18),d(10),d(18),d(12))
        }
        val scrolling=android.widget.ScrollView(this).apply {
            isFillViewport=false
            setBackgroundColor(0xfff7fbff.toInt())
            addView(body)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(scrolling)
            .setPositiveButton("Zatvori",null)
            .show()
    }
    private fun showPrivacyNotice() {
        showLegalNotice("Politika privatnosti","privacy.html")
    }
    private fun showTermsNotice() {
        showLegalNotice("Uvjeti korištenja","terms.html")
    }

    /**
     * Native Home overlay, not a new navigation route. Existing preferences
     * keep their historical keys and stay local; all changes are opt-in.
     */
    private fun showQuickOptions(){
        val column=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(d(20),d(8),d(20),d(16))
            background=gradient(0xff154c8d.toInt(),0xff061d42.toInt(),23).apply {
                setStroke(d(2),0xff60dafe.toInt())
            }
        }
        fun heading(value:String){
            column.addView(TextView(this).apply{
                text=value
                textSize=16f
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(0xffe6f7ff.toInt())
                setPadding(0,d(14),0,d(7))
            })
        }
        fun toggle(caption:String,enabled:Boolean,onChange:(Boolean)->Unit){
            column.addView(android.widget.Switch(this).apply {
                text=caption
                textSize=15f
                setTextColor(0xffe6f7ff.toInt())
                isChecked=enabled
                setOnCheckedChangeListener{_,value->onChange(value)}
            },LinearLayout.LayoutParams(-1,d(48)))
        }
        heading("Težina leta")
        val difficulties=android.widget.RadioGroup(this).apply {
            orientation=LinearLayout.VERTICAL
        }
        val labels=progress.difficultyNames
        val radioIds=IntArray(labels.size){View.generateViewId()}
        for(index in labels.indices) {
            difficulties.addView(android.widget.RadioButton(this).apply {
                id=radioIds[index]
                text=labels[index]
                textSize=15f
                setTextColor(0xffe6f7ff.toInt())
                contentDescription="Težina: "+labels[index]
            },android.widget.RadioGroup.LayoutParams(-1,d(45)))
        }
        difficulties.check(radioIds[progress.difficulty()])
        difficulties.setOnCheckedChangeListener{_,id->
            val index=radioIds.indexOf(id)
            if(index>=0){
                progress.setDifficulty(index)
                sound.effect("click")
            }
        }
        column.addView(difficulties)
        column.addView(TextView(this).apply{
            text="Težina i smanjeni pokret primjenjuju se od sljedećeg leta."
            textSize=12f
            setTextColor(0xffe6f7ff.toInt())
        })
        heading("Zvuk i pristupačnost")
        toggle("Zvuk uključen",progress.soundEnabled()){enabled->
            progress.setSoundEnabled(enabled)
            sound.enabled=enabled
        }
        toggle("Vibracija pri igranju",progress.hapticEnabled()){enabled->
            progress.setHapticEnabled(enabled)
        }
        toggle("Smanjeni pokret i manje osvježavanja",progress.lessMotion()){enabled->
            progress.setLessMotion(enabled)
        }
        fun volumeSlider(caption:String,initial:Int,music:Boolean){
            val label=TextView(this).apply {
                text=caption+": "+initial+" %"
                textSize=14f
                setTextColor(0xffe6f7ff.toInt())
                setPadding(0,d(8),0,0)
            }
            column.addView(label)
            column.addView(android.widget.SeekBar(this).apply {
                max=100
                progress=initial
                contentDescription=caption
                // The 48dp LayoutParams size below works on API26+. ProgressBar's
                // minHeight setter is API29-only and was rejected by Android Lint.
                setOnSeekBarChangeListener(object:android.widget.SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(bar:android.widget.SeekBar?,value:Int,fromUser:Boolean){
                        label.text=caption+": "+value+" %"
                        if(fromUser) {
                            if(music) sound.musicVolume=value/100f
                            else sound.effectsVolume=value/100f
                        }
                    }
                    override fun onStartTrackingTouch(bar:android.widget.SeekBar?){}
                    override fun onStopTrackingTouch(bar:android.widget.SeekBar?){
                        val value=bar?.progress ?: return
                        // One local preference write per finished gesture.
                        if(music) this@MainActivity.progress.setMusicVolume(value)
                        else this@MainActivity.progress.setEffectsVolume(value)
                    }
                })
            },LinearLayout.LayoutParams(-1,d(48)))
        }
        volumeSlider("Glazba",progress.musicVolume(),true)
        volumeSlider("Zvučni efekti",progress.effectsVolume(),false)
        val scroll=android.widget.ScrollView(this).apply{
            isFillViewport=false
            addView(column)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("⚙  OPCIJE IGRE")
            .setView(scroll)
            .setPositiveButton("GOTOVO",null)
            .show().also { polishGameDialog(it) }
    }

    /** Match both in-game overlays to the midnight-blue/cyan Home palette. */
    private fun polishGameDialog(dialog:android.app.AlertDialog) {
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(0xff23aee8.toInt())
    }
    /** Read-only on-device records: no network, accounts or score submissions. */
    private fun showLocalRecords(){
        val stats=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(d(20),d(16),d(20),d(18))
            background=gradient(0xff154c8d.toInt(),0xff061d42.toInt(),23).apply {
                setStroke(d(2),0xff60dafe.toInt())
            }
        }
        val rows=listOf(
            "★  Najbolji rezultat" to progress.bestPoints().toString(),
            "🍓  Najviše hrane u jednom letu" to progress.bestFood().toString(),
            "▥  Najviše prođenih prepreka" to progress.endlessBest().toString(),
            "↗  Odigrani letovi" to progress.totalRuns().toString(),
            "♧  Ukupni zamasi krilima" to progress.totalFlaps().toString()
        )
        for((caption,value) in rows){
            stats.addView(TextView(this).apply{
                text=caption+"\n"+value
                textSize=17f
                typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL)
                setTextColor(0xffe6f7ff.toInt())
                setPadding(d(6),d(7),d(6),d(7))
            },LinearLayout.LayoutParams(-1,-2))
        }
        stats.addView(TextView(this).apply{
            text="Rekordi se čuvaju samo na ovom uređaju. Brisanje podataka aplikacije briše i rekorde."
            textSize=12f
            setTextColor(0xffe6f7ff.toInt())
            setPadding(0,d(12),0,0)
        })
        android.app.AlertDialog.Builder(this)
            .setTitle("★  MOJI REKORDI")
            .setView(android.widget.ScrollView(this).apply{addView(stats)})
            .setPositiveButton("ZATVORI",null)
            .show().also { polishGameDialog(it) }
    }

    private fun showHome(){
        applyPremiumSystemBars()
        gameView?.paused=true;gameView=null;gamePauseButton=null
        sound.stop();selectedScreen="home"
        val root=FrameLayout(this).apply{setBackgroundColor(0xff086ab7.toInt())}
        root.addView(ImageView(this).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x12011c52,0x001b84c8,0xbb031536.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        // One main gameplay action plus a smaller, accessible privacy notice.
        // Keep original logo and Bopi art independent of backdrop artwork.
        val viewport=FitViewportColumn(this).apply{
            clipChildren=false;clipToPadding=false
        }
        val content=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(23),d(10),d(23),d(10))
        }
        viewport.addView(content)
        root.addView(viewport,FrameLayout.LayoutParams(-1,-1))
        // Reserve a full-size 48dp hit target for both secondary actions and
        // both legal links, including the 640dp compact emulator viewport.
        val compactDisplay=resources.configuration.screenHeightDp<770
        val tinyDisplay=resources.configuration.screenHeightDp<610
        content.addView(ImageView(this).apply{
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(if(tinyDisplay)78 else if(compactDisplay)94 else 134)))
        val heroPortrait=ImageView(this).apply{
            setImageResource(portraitDrawableIds[selectedPilotChoice])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        content.addView(heroPortrait,
            LinearLayout.LayoutParams(-1,d(if(tinyDisplay)110 else if(compactDisplay)145 else 230))
                .apply{topMargin=d(if(tinyDisplay)0 else if(compactDisplay)3 else 10)})
        content.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        // The original logo already contains this slogan. A duplicate title
        // consumed ~40dp and shrank native privacy/terms hitboxes below 48dp.
        action(content,"▶  IGRAJ"){startGame()}
        content.addView(TextView(this).apply{
            text="ODABERI LIKA"
            textSize=13f
            letterSpacing=.07f
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            setTextColor(0xffe7f8ff.toInt())
            gravity=Gravity.CENTER
        },LinearLayout.LayoutParams(-1,d(25)))
        val pilots=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        // Rebind three existing cards in place. Rebuilding Home on every choice
        // caused image decoding, extra view allocations and visual flashes.
        fun refreshPilotCards(){
            heroPortrait.setImageResource(portraitDrawableIds[selectedPilotChoice])
            for(i in 0 until pilots.childCount) {
                val selected=i==selectedPilotChoice
                val item=pilots.getChildAt(i) as LinearLayout
                val plate=gradient(
                    if(selected)0xff246f94.toInt() else 0xb5093765.toInt(),
                    if(selected)0xff0a3b69.toInt() else 0xaa061f45.toInt(),16)
                plate.setStroke(d(if(selected)2 else 1),
                    if(selected)0xffffdd72.toInt() else 0x8859d6ff.toInt())
                item.background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),plate,null)
                item.isSelected=selected
                item.contentDescription=(if(selected)"Odabrani lik: " else "Odaberi lika: ")+pilotNames[i]
                (item.getChildAt(1) as TextView).setTextColor(
                    if(selected)0xffffe7a1.toInt() else Color.WHITE)
            }
        }
        for(index in pilotNames.indices){
            val selected=index==selectedPilotChoice
            val item=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER
                val plate=gradient(
                    if(selected)0xff246f94.toInt() else 0xb5093765.toInt(),
                    if(selected)0xff0a3b69.toInt() else 0xaa061f45.toInt(),16)
                plate.setStroke(d(if(selected)2 else 1),
                    if(selected)0xffffdd72.toInt() else 0x8859d6ff.toInt())
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),plate,null)
                isClickable=true
                isFocusable=true
                isSelected=selected
                contentDescription=(if(selected)"Odabrani lik: " else "Odaberi lika: ")+pilotNames[index]
                setOnClickListener {
                    if(selectedPilotChoice!=index) {
                        selectedPilotChoice=index
                        progress.chooseArcadePilot(index)
                        sound.effect("click")
                        refreshPilotCards()
                    }
                }
            }
            item.addView(ImageView(this).apply{
                setImageResource(portraitDrawableIds[index])
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(if(compactDisplay)57 else 69)))
            item.addView(TextView(this).apply{
                text=pilotNames[index]
                gravity=Gravity.CENTER
                textSize=13f
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(if(selected)0xffffe7a1.toInt() else Color.WHITE)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(23)))
            pilots.addView(item,LinearLayout.LayoutParams(0,d(if(compactDisplay)87 else 99),1f).apply{
                setMargins(d(3),0,d(3),0)
            })
        }
        content.addView(pilots,LinearLayout.LayoutParams(-1,-2).apply{
            topMargin=d(5);bottomMargin=d(4)
        })
        // Two compact secondary actions; IGRAJ stays the single main CTA.
        val extras=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        fun secondaryChoice(text:String,description:String,onTap:()->Unit){
            val face=gradient(0xff286f9e.toInt(),0xff103b70.toInt(),16).apply{
                setStroke(d(1),0xff80dfff.toInt())
            }
            extras.addView(TextView(this).apply{
                this.text=text
                textSize=14f
                gravity=Gravity.CENTER
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(Color.WHITE)
                contentDescription=description
                background=RippleDrawable(ColorStateList.valueOf(0x33ffffff),face,null)
                isClickable=true
                isFocusable=true
                setOnClickListener{sound.effect("click");onTap()}
            },LinearLayout.LayoutParams(0,d(48),1f).apply{
                setMargins(d(3),0,d(3),0)
            })
        }
        secondaryChoice("⚙  OPCIJE","Otvori opcije igre"){showQuickOptions()}
        secondaryChoice("★  REKORDI","Pogledaj svoje rekorde"){showLocalRecords()}
        content.addView(extras,LinearLayout.LayoutParams(-1,d(48)).apply {
            topMargin=d(3);bottomMargin=d(2)
        })
        val legal=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        fun legalLink(label:String,description:String,click:()->Unit):TextView =
            TextView(this).apply {
                text=label
                textSize=12f
                gravity=Gravity.CENTER
                setTextColor(0xffe4f7ff.toInt())
                paintFlags=paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
                typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL)
                contentDescription=description
                isClickable=true
                isFocusable=true
                setOnClickListener{click()}
            }
        val privacy=legalLink("Politika privatnosti","Pročitaj politiku privatnosti"){
            showPrivacyNotice()
        }
        legal.addView(privacy,LinearLayout.LayoutParams(0,d(48),1f))
        val terms=legalLink("Uvjeti korištenja","Pročitaj uvjete korištenja"){
            showTermsNotice()
        }
        legal.addView(terms,LinearLayout.LayoutParams(0,d(48),1f))
        content.addView(legal,LinearLayout.LayoutParams(-1,d(48)))
        showNativeView(root)
        viewport.setOnApplyWindowInsetsListener{v,insets->
            if(Build.VERSION.SDK_INT>=30){
                val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
                v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            }
            insets
        }
        viewport.requestApplyInsets()
    }

    private fun showPremiumPause(game:GameView){
        val dialog=android.app.AlertDialog.Builder(this).create()
        val content=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(d(18),d(15),d(18),d(19))
            background=gradient(0xff106bd2.toInt(),0xff031936.toInt(),26).apply{
                setStroke(d(3),electric)
            }
        }


        content.addView(ImageView(this).apply{
            // Keep the actual player visible when pausing Sova or Lisac.
            setImageResource(portraitDrawableIds[selectedPilotChoice])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(154)))
        title(content,"♛  PAUZA",32,Color.WHITE)
        title(content,"Let je pauziran",16,0xffbce6ff.toInt())
        fun command(text:String,style:Int,action:()->Unit){
            val face=when(style){
                0 -> gradient(0xffffdd58.toInt(),0xffff8700.toInt(),30).apply{
                    setStroke(d(3),0xffffe6a0.toInt())
                }
                1 -> gradient(0xff23c8fc.toInt(),0xff064ab5.toInt(),30).apply{
                    setStroke(d(2),0xff83efff.toInt())
                }
                else -> gradient(0xff7593be.toInt(),0xff20365f.toInt(),30).apply{
                    setStroke(d(2),0xffaec9f3.toInt())
                }
            }
            val b=Button(this).apply {
                this.text=text;isAllCaps=false;textSize=18f
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(Color.WHITE)
                setAutoSizeTextTypeUniformWithConfiguration(12,18,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),face,null)
                elevation=d(5).toFloat()
                contentDescription=text
                setOnClickListener{sound.effect("click");dialog.dismiss();action()}
            }
            content.addView(b,LinearLayout.LayoutParams(-1,d(57)).apply{
                setMargins(d(8),d(4),d(8),d(4))
            })
        }
        command("▶  NASTAVI",0){
            game.paused=false
            gamePauseButton?.text="Ⅱ"
            gamePauseButton?.contentDescription="Pauziraj igru"
            sound.resume()
        }
        command("↻  PONOVO",1){startGame()}
        command("←  IZLAZ",2){showHome()}
        // The streamlined Pause contains only Continue, Retry and Exit.
        // Changes to saved audio/accessibility preferences are preserved.
        val fitted=FitViewportColumn(this).apply{addView(content)}
        dialog.setView(fitted)
        dialog.setOnCancelListener{
            // Cancelling Pause resumes only the still-current game.
            if(gameView===game && selectedScreen=="game") {
                game.paused=false
                gamePauseButton?.text="Ⅱ"
                gamePauseButton?.contentDescription="Pauziraj igru"
                sound.resume()
            }
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Measured live buttons, no scroll even when inline pause options open.
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels-d(24)).coerceAtMost(d(420)),
            minOf((resources.displayMetrics.heightPixels-d(48)).coerceAtLeast(1),d(600)))
    }
    private fun startGame(){
        // Hide only the status bar: IMMERSIVE_STICKY + HIDE_NAVIGATION
        // triggers Android's full-screen onboarding popup on the first game.
        // Keep the gesture navigation strip dark (theme sets its color) instead.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        selectedScreen="game"
        val simulation=GameSimulation(LevelEngine.createStream(0,1L),true,0,0f,progress.difficulty(),1L)
        val frame=FrameLayout(this).apply{setBackgroundColor(0xff092044.toInt())}
        sound.startWorld(0)
        // The idle flight preview is not gameplay. Do not expose pause until
        // the first actual flap; never reuse a pause control across screens.
        var pauseButton:Button?=null
        val game=GameView(this,simulation,progress.lessMotion(),pilotSpriteIds[selectedPilotChoice],progress.hapticEnabled(),
            onFinished={if(selectedScreen=="game" && gameView?.game === it)showResult(it)},
            onFlap={sound.effect("tap")},
            onFlightStarted={
                if(selectedScreen=="game" && gameView?.game === simulation) {
                    // Charge exactly once when the player actually starts flying.
                    pauseButton?.visibility=View.VISIBLE
                }
            },
            onCollect={sound.effect("collect")},
            onShieldImpact={sound.effect("hit")},
            qaCaptureFirstFlap=intent?.getBooleanExtra("bopavi_qa_capture_first_flap",false)==true,
            qaCollisionOverlay=intent?.getBooleanExtra("bopavi_qa_collision_overlay",false)==true)
        gameView=game;frame.addView(game,FrameLayout.LayoutParams(-1,-1))
        val pause=Button(this).apply{
            text="Ⅱ";textSize=23f;setTextColor(Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),gradient(0xff254c79.toInt(),0xff132b51.toInt(),18),null)
            elevation=d(5).toFloat()
            contentDescription="Pauziraj igru"
            visibility=View.INVISIBLE
            setOnClickListener{
                if(!game.game.active || game.game.finished || selectedScreen!="game")return@setOnClickListener
                game.paused=true;sound.pause()
                showPremiumPause(game)

            }
        }
        pauseButton=pause
        gamePauseButton=pause
        frame.addView(pause,FrameLayout.LayoutParams(d(56),d(56),Gravity.TOP or Gravity.RIGHT).apply{setMargins(0,d(24),d(15),0)})
        // Keep the pause button clear of notches/cutouts on edge-to-edge phones.
        if (Build.VERSION.SDK_INT >= 35) {
            frame.setOnApplyWindowInsetsListener { _, insets ->
                val cutout = insets.getInsets(android.view.WindowInsets.Type.displayCutout())
                val params = pause.layoutParams as FrameLayout.LayoutParams
                val top = maxOf(d(24),cutout.top+d(8))
                val right = maxOf(d(15),cutout.right+d(10))
                if (params.topMargin!=top || params.rightMargin!=right) {
                    params.topMargin=top;params.rightMargin=right
                    pause.layoutParams=params
                }
                insets
            }
        }
        showNativeView(frame)
    }
    private fun showResult(g:GameSimulation){
        val previousRecord=progress.bestPoints()
        val newRecord=g.score()>previousRecord && g.score()>0
        sound.effect(if(g.won)"level" else "hit")
        val headline="REZULTAT"
        progress.recordRun(g)
        gameView?.paused=true;gameView=null;gamePauseButton=null;selectedScreen="result"
        @Suppress("DEPRECATION")
        applyPremiumSystemBars()
        val root=FrameLayout(this).apply{setBackgroundColor(0xff1d89cb.toInt())}
        val backdrop=ImageView(this).apply{
            setImageResource(R.drawable.world0);scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        root.addView(backdrop,FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x3403183a,0x320a3464,0x90061c42.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        val fitted=FitViewportColumn(this)
        val panel=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
            setPadding(d(18),d(12),d(18),d(18))
        }
        fitted.addView(panel)
        root.addView(fitted,FrameLayout.LayoutParams(-1,-1))
        // Android 16 enforces edge-to-edge: keep result actions out of the
        // gesture-navigation/status-bar regions, while art remains full bleed.
        if (Build.VERSION.SDK_INT >= 35) {
            fitted.setOnApplyWindowInsetsListener { view, insets ->
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars())
                view.setPadding(bars.left,bars.top,bars.right,bars.bottom)
                insets
            }
        }
        // The result hero renders the ACTUAL selected pilot above the played
        // world. Separate live image layers replace the generic static banner.
        val artwork=FrameLayout(this).apply{
            background=gradient(0xff218fe3.toInt(),0xff08346b.toInt(),24)
            clipToOutline=true
            contentDescription=pilotNames[selectedPilotChoice]+" u svijetu Zeleni otoci"
        }
        artwork.addView(ImageView(this).apply{
            setImageResource(R.drawable.world0)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        artwork.addView(View(this).apply {
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x003da8ee,0xa4032355.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        artwork.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[selectedPilotChoice])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,d(186),Gravity.CENTER))
        panel.addView(artwork,LinearLayout.LayoutParams(-1,d(174)).apply{bottomMargin=d(5)})
        title(panel,headline,31,Color.WHITE)
        if(newRecord) {
            val recordBanner=TextView(this).apply {
                text="♛  NOVI REKORD!"
                textSize=19f
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(0xffffecaa.toInt())
                gravity=Gravity.CENTER
                letterSpacing=.06f
                background=gradient(0xffb55b09.toInt(),0xff542c06.toInt(),20).apply{
                    setStroke(d(2),0xffffca46.toInt())
                }
                contentDescription="Novi osobni rekord: ${g.score()} bodova"
            }
            panel.addView(recordBanner,LinearLayout.LayoutParams(-1,d(46)).apply{
                topMargin=d(3);bottomMargin=d(5)
            })
        }
        // Genuine values from this completed run, never screenshot text.
        val summary=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        panel.addView(summary,LinearLayout.LayoutParams(-1,-2).apply{
            topMargin=d(5);bottomMargin=d(5)
        })
        fun resultCard(heading:String,icon:String,value:String,color:Int):TextView{
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
                setPadding(d(3),d(10),d(3),d(10))
                background=gradient(0xf5083570.toInt(),0xf5031738.toInt(),19).apply{
                    setStroke(d(2),0xff37cfff.toInt())
                }
                elevation=d(4).toFloat()
            }
            card.addView(TextView(this).apply{
                text=icon;textSize=29f;gravity=Gravity.CENTER
                setTextColor(gold)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(37)))
            card.addView(TextView(this).apply{
                text=heading;textSize=11f;gravity=Gravity.CENTER
                typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE)
                setAutoSizeTextTypeUniformWithConfiguration(9,12,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(34)))
            val stat=TextView(this).apply{
                text=value;textSize=23f;gravity=Gravity.CENTER
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(color)
                setAutoSizeTextTypeUniformWithConfiguration(16,23,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                contentDescription="$heading $value"
            }
            card.addView(stat,LinearLayout.LayoutParams(-1,d(34)))
            summary.addView(card,LinearLayout.LayoutParams(0,d(116),1f).apply{
                setMargins(d(3),d(3),d(3),d(3))
            })
            return stat
        }
        resultCard("REZULTAT","★",g.score().toString(),gold)
        resultCard("HRANA",
            "🍓",g.foodCollected.toString(),0xffffd5d7.toInt())
        panel.addView(TextView(this).apply{
            val best=progress.bestPoints()
            text="★  NAJBOLJI REZULTAT: $best\n🍓  NAJVIŠE HRANE: ${progress.bestFood()}"
            textSize=16f;gravity=Gravity.CENTER;setTextColor(Color.WHITE)
            setAutoSizeTextTypeUniformWithConfiguration(13,16,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            contentDescription=text
            setPadding(d(10),d(11),d(10),d(11))
            background=gradient(0xf0063978.toInt(),0xf0041a3c.toInt(),21)
        },LinearLayout.LayoutParams(-1,d(60)).apply{topMargin=d(12)})
        action(panel,"▶  PONOVO"){
            startGame()
        }
        backAction(panel,"POČETNA"){
            showHome()
        }
        showNativeView(root)
    }

    override fun onBackPressed() = navigateBack()

    private fun navigateBack() {
        if(selectedScreen=="game") {
            val current=gameView?.game
            when {
                current == null -> showHome()
                current.finished -> showResult(current)
                current.active -> gamePauseButton?.performClick() // Do not discard live runs.
                else -> showHome() // No extra character or level selection
            }
        } else if(selectedScreen=="home") {
            if (Build.VERSION.SDK_INT >= 33) finish() else super.onBackPressed()
        } else showHome()
    }
}
