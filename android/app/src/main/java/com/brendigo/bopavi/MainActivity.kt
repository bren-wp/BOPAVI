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
    // Dark navy remains readable over every stop of the gold CTA gradients.
    private val primaryButtonInk = 0xff061b49.toInt()
    private val electric = 0xff00d9ff.toInt()
    // Three arcade pilots preserve all historical save-backed skin identifiers.
    private val pilotNames=arrayOf("Bopi","Sova","Lisac")
    private val pilotSpriteIds=intArrayOf(0,9,10)
    private val portraitDrawableIds=intArrayOf(R.drawable.bopiportrait0,R.drawable.bopiportrait9,R.drawable.bopiportrait10)
    // Results show the world from the actual completed flight, not a fixed
    // forest image if gameplay is later started in a different biome.
    private val resultWorldDrawableIds=intArrayOf(
        R.drawable.world0,R.drawable.world1,R.drawable.world2,R.drawable.world3,
        R.drawable.world4,R.drawable.world5,R.drawable.world6,R.drawable.world7)
    private var selectedPilotChoice=0
    private fun d(n:Int):Int = (resources.displayMetrics.density*n+.5f).toInt()
    private fun uiIcon(kind:BopaviUiIconDrawable.Kind,color:Int,sizeDp:Int=23):BopaviUiIconDrawable =
        BopaviUiIconDrawable(kind,color,d(sizeDp))
    private fun actionKind(label:String):BopaviUiIconDrawable.Kind = when {
        label.contains("PONOVO") -> BopaviUiIconDrawable.Kind.RESTART
        label.contains("IZLAZ") -> BopaviUiIconDrawable.Kind.EXIT
        label.contains("POČETNA") -> BopaviUiIconDrawable.Kind.HOME
        else -> BopaviUiIconDrawable.Kind.PLAY
    }
    /** Keep the real 56dp Pause button, with a font-independent native icon. */
    private fun showPauseIcon(button:Button?,resume:Boolean) {
        if(button==null)return
        button.text=""
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
            uiIcon(if(resume)BopaviUiIconDrawable.Kind.PLAY
                   else BopaviUiIconDrawable.Kind.PAUSE,Color.WHITE,25),
            null,null,null)
        button.gravity=Gravity.CENTER
        button.contentDescription=if(resume)"Nastavi let"
            else "Pauziraj igru"
    }
    /** One inset policy for Home and Results on every supported API (26–36).
     * The illustrated background remains edge-to-edge, while controls are
     * padded away from navigation gestures and physical display cutouts.
     * Older Android versions do not have WindowInsets.Type; they must not
     * silently fall back to zero padding on compact/notched devices.
     */
    @Suppress("DEPRECATION")
    private fun applySafeViewportInsets(view:View,insets:android.view.WindowInsets):android.view.WindowInsets {
        if(Build.VERSION.SDK_INT>=30) {
            val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                or android.view.WindowInsets.Type.displayCutout())
            view.setPadding(bars.left,bars.top,bars.right,bars.bottom)
        } else {
            val cutoutLeft:Int
            val cutoutTop:Int
            val cutoutRight:Int
            val cutoutBottom:Int
            if(Build.VERSION.SDK_INT>=28) {
                // Lint must see the API-level guard around each DisplayCutout
                // accessor. Android 8.0/8.1 has navigation insets, but no cutout.
                val cutout=insets.displayCutout
                cutoutLeft=cutout?.safeInsetLeft ?: 0
                cutoutTop=cutout?.safeInsetTop ?: 0
                cutoutRight=cutout?.safeInsetRight ?: 0
                cutoutBottom=cutout?.safeInsetBottom ?: 0
            } else {
                cutoutLeft=0;cutoutTop=0;cutoutRight=0;cutoutBottom=0
            }
            view.setPadding(
                maxOf(insets.systemWindowInsetLeft,cutoutLeft),
                maxOf(insets.systemWindowInsetTop,cutoutTop),
                maxOf(insets.systemWindowInsetRight,cutoutRight),
                maxOf(insets.systemWindowInsetBottom,cutoutBottom)
            )
        }
        return insets
    }

    /** True edge-to-edge on API 30+, with a compatibility path on API 26–29.
     * Home and Results own their safe viewport insets. The flight HUD uses
     * GameView/FlightOverlayLayout insets, so never pad the whole game root.
     * The window itself draws behind the transparent navigation area; no
     * deprecated status/navigation-bar color setters are required.
     */
    @Suppress("DEPRECATION")
    private fun applyPremiumSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
    override fun onCreate(state: Bundle?) {
        setTheme(R.style.BopaviTheme)
        super.onCreate(state)
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
    /** Android large-screen rotation must not recreate a live simulation.
     * GameView.onSizeChanged recomputes the world and camera; the Pause
     * button has its own layout/insets listener. Native scrolling menus
     * remeasure automatically on orientation and window-size changes.
     */
    override fun onConfigurationChanged(newConfig:android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        applyPremiumSystemBars()
        window.decorView.requestApplyInsets()
        gameView?.requestLayout()
        gameView?.invalidate()
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
            showPauseIcon(gamePauseButton,true)
        }
        if(::sound.isInitialized) sound.pause()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        if(::sound.isInitialized && selectedScreen=="game" &&
            gameView?.game?.active == false && gameView?.game?.finished == false &&
            gameView?.paused == false) sound.resume()
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
            text=s.substringAfter("  ",s)
            if(s.startsWith("♛  ")) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    uiIcon(if(s.contains("PAUZA"))BopaviUiIconDrawable.Kind.PAUSE
                        else BopaviUiIconDrawable.Kind.RECORDS,color,25),null,null,null)
                compoundDrawablePadding=d(7)
                contentDescription=text.toString()
            }
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
            this.text=text.substringAfter("  ",text)
            setTextColor(if(primary)primaryButtonInk else Color.WHITE)
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                uiIcon(actionKind(text),if(primary)primaryButtonInk else Color.WHITE,24),
                null,null,null)
            compoundDrawablePadding=d(9)
            gravity=Gravity.CENTER
            textSize=18f;isAllCaps=false
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            letterSpacing=.03f
            if(!primary) setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),0xaa07244d.toInt())
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
            text=caption
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                uiIcon(BopaviUiIconDrawable.Kind.HOME,Color.WHITE,23),null,null,null)
            compoundDrawablePadding=d(9)
            contentDescription=caption
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
                // A fixed 48dp row clips Croatian multi-line labels at large
                // font scales. Allow the switch and its full caption to grow
                // together without ever shrinking the native hit area.
                minHeight=d(64)
                setPadding(d(6),d(9),d(6),d(9))
                maxLines=4
                contentDescription=caption
                thumbTintList=ColorStateList.valueOf(0xff9ff6df.toInt())
                trackTintList=ColorStateList.valueOf(0xff286d95.toInt())
                setOnCheckedChangeListener{_,value->onChange(value)}
            },LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT))
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
                buttonTintList=ColorStateList.valueOf(0xff80eeed.toInt())
                minHeight=d(52)
                setPadding(d(6),d(8),d(6),d(8))
            },android.widget.RadioGroup.LayoutParams(
                -1,android.widget.RadioGroup.LayoutParams.WRAP_CONTENT))
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
            // Avoid redundant disk writes, including repeated D-pad presses at
            // a 0%/100% limit. A touch drag commits only once on release.
            var lastSavedVolume=initial
            fun persistVolume(value:Int) {
                val safe=value.coerceIn(0,100)
                if(safe==lastSavedVolume)return
                if(music) this@MainActivity.progress.setMusicVolume(safe)
                else this@MainActivity.progress.setEffectsVolume(safe)
                lastSavedVolume=safe
            }
            column.addView(android.widget.SeekBar(this).apply {
                max=100
                progress=initial
                contentDescription=caption
                progressTintList=ColorStateList.valueOf(0xff4cdddc.toInt())
                thumbTintList=ColorStateList.valueOf(0xffffde76.toInt())
                progressBackgroundTintList=ColorStateList.valueOf(0xff395d7d.toInt())
                // The 48dp LayoutParams size below works on API26+. ProgressBar's
                // minHeight setter is API29-only and was rejected by Android Lint.
                setOnSeekBarChangeListener(object:android.widget.SeekBar.OnSeekBarChangeListener {
                    private var touchTracking=false
                    override fun onProgressChanged(bar:android.widget.SeekBar?,value:Int,fromUser:Boolean){
                        label.text=caption+": "+value+" %"
                        if(fromUser) {
                            if(music) sound.musicVolume=value/100f
                            else sound.effectsVolume=value/100f
                            // TalkBack, keyboard and D-pad change the SeekBar
                            // without onStopTrackingTouch: save them now.
                            if(VolumeGesturePolicy.commitAsProgressChanges(fromUser,touchTracking))
                                persistVolume(value)
                        }
                    }
                    override fun onStartTrackingTouch(bar:android.widget.SeekBar?){
                        touchTracking=true
                    }
                    override fun onStopTrackingTouch(bar:android.widget.SeekBar?){
                        touchTracking=false
                        // Commit a continuous swipe once, with its final value.
                        bar?.progress?.let(::persistVolume)
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
            .setTitle("OPCIJE IGRE")
            .setCustomTitle(premiumDialogHeader("OPCIJE IGRE",BopaviUiIconDrawable.Kind.SETTINGS))
            .setView(scroll)
            .setPositiveButton("GOTOVO",null)
            .show().also { polishGameDialog(it) }
    }

    /** Match both in-game overlays to the midnight-blue/cyan Home palette. */
    private fun premiumDialogHeader(text:String,kind:BopaviUiIconDrawable.Kind):TextView = TextView(this).apply {
        this.text=text
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            uiIcon(kind,0xff9be8ff.toInt(),24),null,null,null)
        compoundDrawablePadding=d(9)
        textSize=21f
        gravity=Gravity.CENTER
        typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
        setTextColor(Color.WHITE)
        setPadding(d(18),d(18),d(18),d(14))
        background=gradient(0xff17558b.toInt(),0xff103466.toInt(),22).apply {
            setStroke(d(1),0xff60dafe.toInt())
        }
    }
    private fun polishGameDialog(dialog:android.app.AlertDialog) {
        dialog.window?.setBackgroundDrawable(
            gradient(0xff103d71.toInt(),0xff051a36.toInt(),25).apply {
                setStroke(d(2),0xff65ddf9.toInt())
            })
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(0xff9be8ff.toInt())
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
            Triple("Najbolji rezultat",progress.bestPoints().toString(),BopaviUiIconDrawable.Kind.STAR),
            Triple("Najviše hrane u jednom letu",progress.bestFood().toString(),BopaviUiIconDrawable.Kind.BERRY),
            Triple("Najviše prođenih prepreka",progress.endlessBest().toString(),BopaviUiIconDrawable.Kind.GATE),
            Triple("Odigrani letovi",progress.totalRuns().toString(),BopaviUiIconDrawable.Kind.FLIGHT),
            Triple("Ukupni zamasi krilima",progress.totalFlaps().toString(),BopaviUiIconDrawable.Kind.WING)
        )
        for((caption,value,kind) in rows){
            stats.addView(TextView(this).apply{
                text=caption+"\n"+value
                textSize=17f
                typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL)
                setTextColor(0xffe6f7ff.toInt())
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    uiIcon(kind,if(kind==BopaviUiIconDrawable.Kind.BERRY)0xffff6179.toInt()
                        else 0xffffdd72.toInt(),24),null,null,null)
                compoundDrawablePadding=d(13)
                contentDescription="$caption: $value"
                setPadding(d(6),d(8),d(6),d(8))
            },LinearLayout.LayoutParams(-1,-2))
        }
        stats.addView(TextView(this).apply{
            text="Rekordi se čuvaju samo na ovom uređaju. Brisanje podataka aplikacije briše i rekorde."
            textSize=12f
            setTextColor(0xffe6f7ff.toInt())
            setPadding(0,d(12),0,0)
        })
        android.app.AlertDialog.Builder(this)
            .setTitle("MOJI REKORDI")
            .setCustomTitle(premiumDialogHeader("MOJI REKORDI",BopaviUiIconDrawable.Kind.RECORDS))
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
        val pilotHeading=TextView(this).apply{
            text="ODABERI LIKA · "+PilotAbility.fromChoice(selectedPilotChoice).shortTitle
            textSize=13f
            letterSpacing=.03f
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            setTextColor(0xffe7f8ff.toInt())
            gravity=Gravity.CENTER
            setAutoSizeTextTypeUniformWithConfiguration(10,13,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            contentDescription="Odaberi lika. "+pilotNames[selectedPilotChoice]+
                ". "+PilotAbility.fromChoice(selectedPilotChoice).shortTitle
        }
        content.addView(pilotHeading,LinearLayout.LayoutParams(-1,d(32)))
        val pilots=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        // Rebind three existing cards in place. Rebuilding Home on every choice
        // caused image decoding, extra view allocations and visual flashes.
        fun refreshPilotCards(){
            heroPortrait.setImageResource(portraitDrawableIds[selectedPilotChoice])
            pilotHeading.text="ODABERI LIKA · "+PilotAbility.fromChoice(selectedPilotChoice).shortTitle
            pilotHeading.contentDescription="Odaberi lika. "+pilotNames[selectedPilotChoice]+
                ". "+PilotAbility.fromChoice(selectedPilotChoice).shortTitle
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
                item.contentDescription=(if(selected)"Odabrani lik: " else "Odaberi lika: ")+
                    pilotNames[i]+". "+PilotAbility.fromChoice(i).description
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
                contentDescription=(if(selected)"Odabrani lik: " else "Odaberi lika: ")+
                    pilotNames[index]+". "+PilotAbility.fromChoice(index).description
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
                this.text=text.substringAfter("  ",text)
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    uiIcon(if(text.contains("OPCIJE"))BopaviUiIconDrawable.Kind.SETTINGS
                        else BopaviUiIconDrawable.Kind.RECORDS,Color.WHITE,21),
                    null,null,null)
                compoundDrawablePadding=d(6)
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
                background=RippleDrawable(ColorStateList.valueOf(0x337eecff),
                    GradientDrawable().apply{setColor(Color.TRANSPARENT);cornerRadius=d(12).toFloat()},null)
                setOnClickListener{sound.effect("click");click()}
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
            applySafeViewportInsets(v,insets)
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
        title(content,"Let je pauziran · "+game.game.pilotAbility.shortTitle,16,0xffbce6ff.toInt())
        title(content,game.game.pilotAbility.description,13,0xffbce6ff.toInt())
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
                this.text=text.substringAfter("  ",text)
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    uiIcon(actionKind(text),if(style==0)primaryButtonInk else Color.WHITE,24),
                    null,null,null)
                compoundDrawablePadding=d(9)
                gravity=Gravity.CENTER
                isAllCaps=false;textSize=18f
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(if(style==0)primaryButtonInk else Color.WHITE)
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
            showPauseIcon(gamePauseButton,false)
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
            if(gameView===game && selectedScreen=="game" && window.decorView.hasWindowFocus()) {
                game.paused=false
                showPauseIcon(gamePauseButton,false)
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
        // Preserve the same inset contract as Home/Results when opening Flight.
        // Hide only the status bar; do not trigger immersive navigation popups.
        applyPremiumSystemBars()
        selectedScreen="game"
        val simulation=GameSimulation(LevelEngine.createStream(0,1L),true,0,0f,
            progress.difficulty(),1L,PilotAbility.fromChoice(selectedPilotChoice))
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
            text="";textSize=23f;setTextColor(Color.WHITE)
            showPauseIcon(this,false)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),gradient(0xff254c79.toInt(),0xff132b51.toInt(),18),null)
            elevation=d(5).toFloat()
            contentDescription="Pauziraj igru"
            visibility=View.INVISIBLE
            setOnClickListener{
                if(!game.game.active || game.game.finished || selectedScreen!="game")return@setOnClickListener
                if(game.paused) {
                    // The OS backgrounded this run. ▶ resumes in one action,
                    // without opening an extra pause dialog over a paused game.
                    game.paused=false
                    showPauseIcon(pauseButton,false)
                    sound.resume()
                    return@setOnClickListener
                }
                game.paused=true;sound.pause()
                showPremiumPause(game)

            }
        }
        pauseButton=pause
        gamePauseButton=pause
        frame.addView(pause,FrameLayout.LayoutParams(d(56),d(56),Gravity.TOP or Gravity.RIGHT))
        // The upper-right third HUD panel contains real lives. A pause button
        // positioned at 24dp covers those hearts on some aspect ratios.
        // Compute margins from the SAME HUD scale and status/cutout inset as
        // GameView, rather than guessing a phone-specific fixed margin.
        var flightSafeTop=0
        var flightSafeRight=0
        fun fitPauseButton() {
            val w=frame.width.takeIf{it>0} ?: resources.displayMetrics.widthPixels
            val h=frame.height.takeIf{it>0} ?: resources.displayMetrics.heightPixels
            val lp=pause.layoutParams as FrameLayout.LayoutParams
            val top=FlightOverlayLayout.pauseTopPx(
                w,h,resources.displayMetrics.density,flightSafeTop)
            val right=FlightOverlayLayout.pauseRightPx(
                resources.displayMetrics.density,flightSafeRight)
            if(lp.topMargin!=top || lp.rightMargin!=right) {
                lp.topMargin=top;lp.rightMargin=right
                pause.layoutParams=lp
            }
        }
        fitPauseButton()
        // Re-evaluate if the window is resized (including split-screen).
        frame.addOnLayoutChangeListener { _,_,_,_,_,_,_,_,_->fitPauseButton() }
        if (Build.VERSION.SDK_INT >= 28) {
            frame.setOnApplyWindowInsetsListener { _, insets ->
                if (Build.VERSION.SDK_INT >= 30) {
                    val safe=insets.getInsets(android.view.WindowInsets.Type.statusBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    flightSafeTop=safe.top
                    flightSafeRight=safe.right
                } else {
                    @Suppress("DEPRECATION")
                    val statusTop=insets.systemWindowInsetTop
                    flightSafeTop=maxOf(statusTop,insets.displayCutout?.safeInsetTop ?: 0)
                    flightSafeRight=insets.displayCutout?.safeInsetRight ?: 0
                }
                fitPauseButton()
                insets
            }
        }
        showNativeView(frame)
        frame.requestApplyInsets()
    }
    private fun showResult(g:GameSimulation){
        // Results are a static screen: release the looping decoder and music
        // audio focus before the independently mixed finish sound effect.
        sound.stop()
        val previousRecord=progress.bestPoints()
        val newRecord=g.score()>previousRecord && g.score()>0
        sound.effect(if(g.won)"level" else "hit")
        val headline="REZULTAT"
        val resultWorld=g.level.world.coerceIn(0,LevelEngine.WORLD_COUNT-1)
        val resultBackdrop=resultWorldDrawableIds[resultWorld]
        progress.recordRun(g)
        gameView?.paused=true;gameView=null;gamePauseButton=null;selectedScreen="result"
        @Suppress("DEPRECATION")
        applyPremiumSystemBars()
        val root=FrameLayout(this).apply{setBackgroundColor(0xff1d89cb.toInt())}
        val backdrop=ImageView(this).apply{
            setImageResource(resultBackdrop);scaleType=ImageView.ScaleType.CENTER_CROP
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
        // Safe bounds match Home, including pre-Android-15 navigation bars.
        // Draw backgrounds full bleed, but never place real actions under
        // notches or gesture system controls.
        fitted.setOnApplyWindowInsetsListener { view, insets ->
            applySafeViewportInsets(view,insets)
        }
        // The result hero renders the ACTUAL selected pilot above the played
        // world. Separate live image layers replace the generic static banner.
        val artwork=FrameLayout(this).apply{
            background=gradient(0xff218fe3.toInt(),0xff08346b.toInt(),24)
            clipToOutline=true
            contentDescription=pilotNames[selectedPilotChoice]+" u svijetu "+LevelEngine.names[resultWorld]+
                ". Sposobnost: "+g.pilotAbility.shortTitle
        }
        artwork.addView(ImageView(this).apply{
            setImageResource(resultBackdrop)
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
        artwork.addView(TextView(this).apply {
            text="MOĆ  ·  "+g.pilotAbility.shortTitle
            textSize=13f
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            gravity=Gravity.CENTER
            setTextColor(0xffffe7a1.toInt())
            background=gradient(0xdd063064.toInt(),0xd4031938.toInt(),9)
            contentDescription="Sposobnost lika: "+g.pilotAbility.shortTitle+
                ". "+g.pilotAbility.description
        },FrameLayout.LayoutParams(-1,d(29),Gravity.BOTTOM))
        panel.addView(artwork,LinearLayout.LayoutParams(-1,d(174)).apply{bottomMargin=d(5)})
        title(panel,headline,31,Color.WHITE)
        if(newRecord) {
            val recordBanner=TextView(this).apply {
                text="NOVI REKORD!"
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    uiIcon(BopaviUiIconDrawable.Kind.RECORDS,0xffffecaa.toInt(),24),null,null,null)
                compoundDrawablePadding=d(8)
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
        fun resultCard(heading:String,iconKind:BopaviUiIconDrawable.Kind,
                       iconTint:Int,value:String,color:Int):TextView{
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
                setPadding(d(3),d(10),d(3),d(10))
                background=gradient(0xf5083570.toInt(),0xf5031738.toInt(),19).apply{
                    setStroke(d(2),0xff37cfff.toInt())
                }
                elevation=d(4).toFloat()
            }
            card.addView(ImageView(this).apply {
                setImageDrawable(uiIcon(iconKind,iconTint,29))
                scaleType=ImageView.ScaleType.CENTER_INSIDE
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
        resultCard("REZULTAT",BopaviUiIconDrawable.Kind.STAR,gold,g.score().toString(),gold)
        resultCard("HRANA",BopaviUiIconDrawable.Kind.BERRY,0xffff6179.toInt(),
            g.foodCollected.toString(),0xffffd5d7.toInt())
        val recordSummary=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(d(10),d(4),d(10),d(4))
            background=gradient(0xf0063978.toInt(),0xf0041a3c.toInt(),21)
        }
        fun recordSummaryRow(label:String,value:String,kind:BopaviUiIconDrawable.Kind,tint:Int) {
            recordSummary.addView(TextView(this).apply{
                text="$label: $value"
                textSize=16f
                gravity=Gravity.CENTER
                setTextColor(Color.WHITE)
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setAutoSizeTextTypeUniformWithConfiguration(12,16,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                setCompoundDrawablesRelativeWithIntrinsicBounds(uiIcon(kind,tint,19),null,null,null)
                compoundDrawablePadding=d(7)
                contentDescription="$label: $value"
            },LinearLayout.LayoutParams(-1,d(30)))
        }
        recordSummaryRow("NAJBOLJI REZULTAT",progress.bestPoints().toString(),
            BopaviUiIconDrawable.Kind.STAR,gold)
        recordSummaryRow("NAJVIŠE HRANE",progress.bestFood().toString(),
            BopaviUiIconDrawable.Kind.BERRY,0xffff6179.toInt())
        panel.addView(recordSummary,LinearLayout.LayoutParams(-1,d(68)).apply{topMargin=d(10)})
        action(panel,"▶  PONOVO"){
            startGame()
        }
        backAction(panel,"POČETNA"){
            showHome()
        }
        showNativeView(root)
        fitted.requestApplyInsets()
    }

    // Android 13+ uses the registered OnBackInvokedCallback above; this
    // Activity override remains solely for API 26–32 hardware/system Back.
    // AGP 9 lint cannot infer the runtime-guarded callback registration.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Suppress("DEPRECATION")
    override fun onBackPressed() = navigateBack()

    private fun navigateBack() {
        if(selectedScreen=="game") {
            val current=gameView?.game
            when {
                current == null -> showHome()
                current.finished -> showResult(current)
                current.active -> {
                    // Back always offers NASTAVI / PONOVO / IZLAZ, even when
                    // the foreground pause button now resumes in one touch.
                    val view=gameView
                    if(view!=null && view.paused) showPremiumPause(view)
                    else gamePauseButton?.performClick()
                } // Do not discard live runs.
                else -> showHome() // No extra character or level selection
            }
        } else if(selectedScreen=="home") {
            if (Build.VERSION.SDK_INT >= 33) finish() else super.onBackPressed()
        } else showHome()
    }
}
