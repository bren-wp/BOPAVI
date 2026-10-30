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
    private var pauseDialog: android.app.AlertDialog? = null
    private val shopPurchaseTapGate=ShopPurchaseTapGate()
    // Prevent a frozen, untappable flight after returning from another app.
    private var showPauseAfterBackground = false
    private var platformBackCallback: android.window.OnBackInvokedCallback? = null
    private var selectedScreen = "home"
    // Cancels an in-flight preflight callback when Back leaves Loading.
    private var loadingGeneration = 0
    private val textColor = Color.WHITE
    private val gold = 0xffffc24d.toInt()
    // Dark navy remains readable over every stop of the gold CTA gradients.
    private val primaryButtonInk = 0xff061b49.toInt()
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
    private var activeStage:Pair<Int,Long>? = null
    private fun d(n:Int):Int = (resources.displayMetrics.density*n+.5f).toInt()
    private fun uiIcon(kind:BopaviUiIconDrawable.Kind,color:Int,sizeDp:Int=23):BopaviUiIconDrawable =
        BopaviUiIconDrawable(kind,color,d(sizeDp))
    private fun actionKind(label:String):BopaviUiIconDrawable.Kind = when {
        label.contains("PONOVO") -> BopaviUiIconDrawable.Kind.RESTART
        label.contains("IZLAZ") -> BopaviUiIconDrawable.Kind.EXIT
        label.contains("POČETNA") -> BopaviUiIconDrawable.Kind.HOME
        else -> BopaviUiIconDrawable.Kind.PLAY
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
     * GameView insets, so never pad the whole game root.
     * The window itself draws behind the transparent navigation area; no
     * deprecated status/navigation-bar color setters are required.
     */
    @Suppress("DEPRECATION")
    private fun applyPremiumSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            // Window.getInsetsController() dereferences DecorView before
            // attachment and crashed Android 15/16 during Activity.onCreate.
            // View.getWindowInsetsController() is nullable until attached.
            // A posted action runs after setContentView has created the root.
            val decor=window.decorView
            decor.post {
                if(decor.isAttachedToWindow) {
                    decor.windowInsetsController?.hide(
                        android.view.WindowInsets.Type.statusBars())
                }
            }
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
        if(intent?.getBooleanExtra("bopavi_qa_loading_preview",false)==true) {
            // Screenshot-only QA entry: the very same native screen shown
            // during actual level preparation, without mock progress.
            selectedScreen="loading"
            showNativeView(FlightLoadingView(this,selectedPilotChoice))
        } else showHome()
    }
    /** Android large-screen rotation must not recreate a live simulation.
     * GameView.onSizeChanged recomputes the world and camera. Native scrolling menus
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
        // Preserve the run. Remember only a newly backgrounded live flight;
        // do not open a second dialog if the user already paused via Back.
        val active=gameView
        if(selectedScreen=="game" && active?.game?.active==true &&
            !active.game.finished && !active.paused) {
            showPauseAfterBackground=true
            active.paused=true
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
    override fun onWindowFocusChanged(hasFocus:Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Activity.onResume may precede the restoration of window focus.
        // Open the existing native dialog exactly once after focus returns.
        if(hasFocus && showPauseAfterBackground) {
            showPauseAfterBackground=false
            val active=gameView
            if(selectedScreen=="game" && active?.game?.active==true &&
                !active.game.finished && active.paused && !isFinishing) {
                showPremiumPause(active)
            }
        }
    }
    override fun onDestroy() {
        loadingGeneration++
        if (Build.VERSION.SDK_INT >= 33) {
            platformBackCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            platformBackCallback = null
        }
        pauseDialog?.dismiss()
        pauseDialog=null
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
        // Level Map stays reachable without adding an extra Home button that
        // conflicts with the supplied Home composition.
        lateinit var optionsDialog:android.app.AlertDialog
        heading("Razine")
        action(column,"LEVEL MAP",false){
            optionsDialog.dismiss()
            showLevels()
        }
        heading("Pravni dokumenti")
        fun legalButton(caption:String,description:String,onClick:()->Unit){
            column.addView(Button(this).apply{
                text=caption
                isAllCaps=false
                contentDescription=description
                textSize=16f
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(Color.WHITE)
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    gradient(0xff2376ac.toInt(),0xff0a386d.toInt(),20),null)
                minHeight=d(52)
                setOnClickListener{sound.effect("click");onClick()}
            },LinearLayout.LayoutParams(-1,d(56)).apply{topMargin=d(5)})
        }
        legalButton("Privacy Policy","Pročitaj politiku privatnosti"){showPrivacyNotice()}
        legalButton("Terms of Service","Pročitaj uvjete korištenja"){showTermsNotice()}
        val scroll=android.widget.ScrollView(this).apply{
            isFillViewport=false
            addView(column)
        }
        optionsDialog=android.app.AlertDialog.Builder(this)
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
        showPauseAfterBackground=false
        sound.stop()
        selectedScreen="records"
        val summary=LeaderboardSummary(
            progress.bestPoints(),progress.bestFood(),progress.endlessBest(),
            progress.totalRuns(),progress.totalFlaps())
        val scores=progress.leaderboard().map{
            LeaderboardEntry(it.name,it.score,it.world,it.difficulty,it.gates)
        }
        showNativeView(LeaderboardScreen(this).create(summary,scores,onBack={showHome()}))
    }

    /** Real navigation destinations backed exclusively by local state. */
    private fun showSkins(){
        showPauseAfterBackground=false
        sound.stop()
        selectedScreen="skins"
        val view=ReferenceMenus(this,progress).skins(selectedPilotChoice,
            onChoose={index->
                selectedPilotChoice=index
                progress.chooseArcadePilot(index)
                sound.effect("click")
                showSkins()
            },
            onBack={showHome()})
        showNativeView(view)
    }
    /** Native full-screen Settings; SOUND shortcut keeps the quick mixer. */
    private fun showSettingsScreen(){
        showPauseAfterBackground=false
        selectedScreen="settings"
        val page=SettingsScreen(this,progress).create(
            onBack={showHome()},
            onMusic={value->
                progress.setMusicVolume(value)
                sound.musicVolume=value/100f
            },
            onEffects={value->
                progress.setEffectsVolume(value)
                sound.effectsVolume=value/100f
            },
            onSound={value->
                progress.setSoundEnabled(value)
                sound.enabled=value
            },
            onHaptic={value->progress.setHapticEnabled(value)},
            onMotion={value->progress.setLessMotion(value)},
            onDifficulty={value->
                progress.setDifficulty(value)
                showSettingsScreen()
            },
            onPrivacy={showPrivacyNotice()},
            onTerms={showTermsNotice()})
        showNativeView(page)
    }
    private fun showShop(){
        showPauseAfterBackground=false
        sound.stop()
        selectedScreen="shop"
        val view=ReferenceMenus(this,progress).shop(
            onBuy={index->
                // Filter only a repeated same-product tap in a short window,
                // including across redraws. The real wallet transaction is
                // still performed once inside ProgressStore.
                if(shopPurchaseTapGate.allow(index,
                        android.os.SystemClock.elapsedRealtime())){
                    if(progress.buyPerk(index)){
                        sound.effect("collect")
                        showShop()
                    } else {
                        android.widget.Toast.makeText(this,
                            "Nema dovoljno novčića ili je inventar pun.",
                            android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onClaim={
                if(progress.claimBonusCoins()>0){
                    sound.effect("collect")
                    showShop()
                }
            },
            onBack={showHome()})
        showNativeView(view)
    }
    private fun showLevels(){
        showPauseAfterBackground=false
        sound.stop()
        selectedScreen="levels"
        val view=ReferenceMenus(this,progress).levels(progress.chosenWorld(),
            onWorld={world->progress.chooseWorld(world);showLevels()},
            onPlay={world,number->startLevel(world,number)},
            onBack={showHome()})
        showNativeView(view)
    }

    /** Read-only achievements: all eight values are from saved local runs. */
    private fun showLocalAchievements(){
        showPauseAfterBackground=false
        sound.stop()
        selectedScreen="achievements"
        val snapshot=AchievementSnapshot(
            wins=progress.wins(),runs=progress.totalRuns(),
            flaps=progress.totalFlaps(),food=progress.bestFood(),
            obstacles=progress.endlessBest(),score=progress.bestPoints(),
            meadows=progress.collectibles(0),deaths=progress.deaths())
        showNativeView(AchievementsScreen(this).create(snapshot,onBack={showHome()}))
    }

    private fun showHome(){
        loadingGeneration++
        activeStage=null
        showPauseAfterBackground=false
        applyPremiumSystemBars()
        gameView?.paused=true;gameView=null
        sound.stop();selectedScreen="home"
        val root=FrameLayout(this).apply{setBackgroundColor(0xff087bc9.toInt())}
        root.addView(ImageView(this).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x13022d74,0x007ed9fd,0x66051e3d))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val viewport=FitViewportColumn(this).apply{
            clipChildren=false;clipToPadding=false
        }
        val content=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(16),d(8),d(16),d(10))
        }
        viewport.addView(content)
        root.addView(viewport,FrameLayout.LayoutParams(-1,-1))
        val compactDisplay=resources.configuration.screenHeightDp<770
        val tinyDisplay=resources.configuration.screenHeightDp<610
        // Reference Home: the hero is the dominant center artwork. Keep
        // it larger on tall phones without reducing native 48dp tap targets.
        val heroHeight=if(compactDisplay)
            (resources.configuration.screenHeightDp-558).coerceIn(92,270)
        else (resources.configuration.screenHeightDp-468).coerceIn(225,320)

        // The reference has two small upper-right wallet capsules, not two
        // full-width score banners. Values always come from actual saved data.
        val resourcesBar=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.END or Gravity.CENTER_VERTICAL
        }
        fun resourceChip(label:String,count:Int,kind:BopaviUiIconDrawable.Kind,
                         destination:String,onClick:()->Unit){
            val chip=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL
                gravity=Gravity.CENTER_VERTICAL
                setPadding(d(5),0,d(5),0)
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    gradient(0xff2073ba.toInt(),0xff061e4c.toInt(),22).apply{
                        setStroke(d(2),0xff80d7ff.toInt())
                    },null)
                contentDescription="$label: $count. $destination"
                isClickable=true;isFocusable=true
                minimumHeight=d(48)
                setOnClickListener{sound.effect("click");onClick()}
            }
            chip.addView(ImageView(this).apply{
                setImageDrawable(uiIcon(kind,0xffffd44e.toInt(),25))
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(d(28),d(28)))
            chip.addView(TextView(this).apply{
                text=java.text.NumberFormat.getIntegerInstance().format(count)
                textSize=if(tinyDisplay)14f else 16f
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity=Gravity.CENTER
                maxLines=1
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(0,-1,1f))
            chip.addView(TextView(this).apply{
                text="+"
                textSize=21f
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity=Gravity.CENTER
                background=gradient(0xff72ee49.toInt(),0xff0f9e2b.toInt(),9)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(d(25),d(29)))
            resourcesBar.addView(chip,LinearLayout.LayoutParams(0,d(48),1f).apply{
                setMargins(d(3),0,d(3),0)
            })
        }
        resourceChip("COINS",progress.coins(),BopaviUiIconDrawable.Kind.COIN,
            "Otvori trgovinu"){showShop()}
        val stars=(0 until LevelEngine.WORLD_COUNT).sumOf { progress.worldStars(it) }
        resourceChip("STARS",stars,BopaviUiIconDrawable.Kind.STAR,
            "Otvori kartu razina"){showLevels()}
        content.addView(resourcesBar,LinearLayout.LayoutParams(
            (resources.displayMetrics.widthPixels*.51f).toInt(),d(49)).apply{
            gravity=Gravity.END
        })

        content.addView(ImageView(this).apply{
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(
            (resources.displayMetrics.widthPixels*.82f).toInt(),
            d(if(tinyDisplay)82 else if(compactDisplay)108 else 147)
        ).apply{
            gravity=Gravity.CENTER_HORIZONTAL
            topMargin=d(1)
        })
        content.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[selectedPilotChoice])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(
            heroHeight
        )).apply{topMargin=d(if(compactDisplay)0 else 7)})
        // The reference places PLAY immediately after the flying hero,
        // with no huge stretchable blank strip on tall 20:9 displays.
        action(content,"▶  PLAY"){startGame()}
        (content.getChildAt(content.childCount-1) as Button).apply{
            setTextColor(Color.WHITE)
            setShadowLayer(d(2).toFloat(),0f,d(3).toFloat(),0xff743c00.toInt())
            setAutoSizeTextTypeUniformWithConfiguration(18,26,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            textSize=if(tinyDisplay)20f else 26f
            (layoutParams as LinearLayout.LayoutParams).apply{
                width=(resources.displayMetrics.widthPixels*.67f).toInt()
                gravity=Gravity.CENTER_HORIZONTAL
                layoutParams=this
            }
        }

        val features=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        fun feature(label:String,description:String,top:Int,bottom:Int,
                    kind:BopaviUiIconDrawable.Kind,onClick:()->Unit){
            features.addView(TextView(this).apply{
                text=label
                textSize=if(tinyDisplay)16f else 19f
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity=Gravity.CENTER
                setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),0xff062354.toInt())
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    uiIcon(kind,Color.WHITE,24),null,null,null)
                compoundDrawablePadding=d(7)
                contentDescription=description
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(top,bottom)).apply{
                        cornerRadius=d(25).toFloat()
                        setStroke(d(3),0xffb4efff.toInt())
                    },null)
                elevation=d(4).toFloat()
                isClickable=true;isFocusable=true
                setOnClickListener{sound.effect("click");onClick()}
            },LinearLayout.LayoutParams(0,d(if(tinyDisplay)49 else 57),1f).apply{
                setMargins(d(4),d(3),d(4),d(3))
            })
        }
        feature("SKINS","Otvori izbor likova",0xffbc3bfa.toInt(),
            0xff5706b8.toInt(),BopaviUiIconDrawable.Kind.FLIGHT){showSkins()}
        feature("SHOP","Otvori trgovinu nadogradnji",0xff6de553.toInt(),
            0xff0a8d25.toInt(),BopaviUiIconDrawable.Kind.SHOP){showShop()}
        content.addView(features,LinearLayout.LayoutParams(
            (resources.displayMetrics.widthPixels*.72f).toInt(),
            d(if(tinyDisplay)55 else 64)).apply{
            gravity=Gravity.CENTER_HORIZONTAL
        })

        content.addView(TextView(this).apply{
            text="SETTINGS"
            textSize=if(tinyDisplay)18f else 21f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity=Gravity.CENTER
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                uiIcon(BopaviUiIconDrawable.Kind.SETTINGS,Color.WHITE,28),null,null,null)
            compoundDrawablePadding=d(9)
            background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                gradient(0xff36d7ff.toInt(),0xff075ac4.toInt(),25).apply{
                    setStroke(d(3),0xffb5edff.toInt())
                },null)
            contentDescription="Otvori opcije igre"
            elevation=d(4).toFloat()
            isClickable=true;isFocusable=true
            setOnClickListener{sound.effect("click");showSettingsScreen()}
        },LinearLayout.LayoutParams(
            (resources.displayMetrics.widthPixels*.54f).toInt(),
            d(if(tinyDisplay)49 else 57)).apply{
            gravity=Gravity.CENTER_HORIZONTAL
            topMargin=d(3);bottomMargin=d(5)
        })

        val footer=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        fun shortcut(label:String,kind:BopaviUiIconDrawable.Kind,
                     description:String,onClick:()->Unit){
            val group=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER
                isClickable=true;isFocusable=true
                contentDescription=description
                setOnClickListener{sound.effect("click");onClick()}
            }
            group.addView(ImageView(this).apply{
                setImageDrawable(uiIcon(kind,0xffffcd40.toInt(),35))
                scaleType=ImageView.ScaleType.CENTER_INSIDE
                background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xff2363b9.toInt(),0xff051c42.toInt())).apply{
                    shape=GradientDrawable.OVAL
                    setStroke(d(2),0xffffd979.toInt())
                }
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(d(if(tinyDisplay)43 else 53),
                d(if(tinyDisplay)43 else 53)))
            group.addView(TextView(this).apply{
                text=label
                textSize=if(tinyDisplay)10f else 11f
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity=Gravity.CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(21)))
            footer.addView(group,LinearLayout.LayoutParams(0,d(77),1f))
        }
        shortcut("ACHIEVEMENTS",BopaviUiIconDrawable.Kind.RECORDS,
            "Pogledaj svoja postignuća"){showLocalAchievements()}
        shortcut("LEADERBOARD",BopaviUiIconDrawable.Kind.LEADERBOARD,
            "Pogledaj svoje rekorde"){showLocalRecords()}
        shortcut("SOUND",BopaviUiIconDrawable.Kind.SOUND,
            "Otvori zvučne postavke"){showQuickOptions()}
        content.addView(footer,LinearLayout.LayoutParams(
            (resources.displayMetrics.widthPixels*.64f).toInt(),
            d(if(tinyDisplay)68 else if(compactDisplay)72 else 79)).apply{
            gravity=Gravity.CENTER_HORIZONTAL
            topMargin=d(if(compactDisplay)6 else 23)
        })
        // On tall displays the spare sky stays BELOW the controls; short
        // landscape/split-screen windows still scroll with full-size targets.
        content.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        showNativeView(root)
        viewport.setOnApplyWindowInsetsListener{v,insets->
            applySafeViewportInsets(v,insets)
        }
        viewport.requestApplyInsets()
    }

    private fun showPremiumPause(game:GameView){
        // Single modal only, whether triggered by Back or foreground restoration.
        if(gameView!==game || selectedScreen!="game" || game.game.finished ||
            pauseDialog?.isShowing==true || isFinishing || isDestroyed)return
        game.paused=true
        sound.pause()
        val compactPause=resources.configuration.screenHeightDp<690
        val dialog=android.app.AlertDialog.Builder(this).create()
        val content=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(d(17),d(13),d(17),d(18))
            background=gradient(0xff238ee9.toInt(),0xff061b45.toInt(),28).apply{
                setStroke(d(3),0xff84ecff.toInt())
            }
        }


        content.addView(ImageView(this).apply{
            // Full local Bopi, with both wings, also used by Home and Results.
            setImageResource(portraitDrawableIds[selectedPilotChoice])
            scaleType=ImageView.ScaleType.FIT_CENTER
            background=gradient(0xff78d9ff.toInt(),0xff1269be.toInt(),24).apply{
                setStroke(d(2),0xffccf9ff.toInt())
            }
            setPadding(d(5),d(3),d(5),d(3))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(if(compactPause)112 else 168)).apply{
            bottomMargin=d(7)
        })
        title(content,"♛  PAUZA",if(compactPause)27 else 32,Color.WHITE)
        title(content,"Let je pauziran · "+game.game.pilotAbility.shortTitle,16,0xffe4f5ff.toInt())
        if(!compactPause)title(content,game.game.pilotAbility.description,13,0xffc7eaff.toInt())
        fun command(text:String,style:Int,action:()->Unit){
            val face=when(style){
                0 -> gradient(0xffffdd58.toInt(),0xffff8700.toInt(),30).apply{
                    setStroke(d(3),0xffffe6a0.toInt())
                }
                1 -> gradient(0xff23c8fc.toInt(),0xff064ab5.toInt(),30).apply{
                    setStroke(d(2),0xff83efff.toInt())
                }
                else -> gradient(0xff26c9fc.toInt(),0xff0755bb.toInt(),30).apply{
                    setStroke(d(2),0xff9deaff.toInt())
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
            content.addView(b,LinearLayout.LayoutParams(-1,d(if(compactPause)55 else 63)).apply{
                setMargins(d(8),d(6),d(8),d(6))
            })
        }
        command("▶  NASTAVI",0){
            if(gameView===game && selectedScreen=="game" && !game.game.finished){
                game.paused=false
                sound.resume()
            }
        }
        command("↻  PONOVO",1){
            if(gameView===game && selectedScreen=="game" && !game.game.finished){
                when(val target=PauseReplayTarget.fromStage(activeStage)){
                    PauseReplayTarget.Endless -> startGame()
                    is PauseReplayTarget.Finite -> startLevel(target.world,target.level)
                }
            }
        }
        command("←  IZLAZ",2){
            if(gameView===game && selectedScreen=="game")showHome()
        }
        // The streamlined Pause contains only Continue, Retry and Exit.
        // Changes to saved audio/accessibility preferences are preserved.
        val fitted=FitViewportColumn(this).apply{addView(content)}
        dialog.setView(fitted)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener{
            if(pauseDialog===dialog)pauseDialog=null
        }
        dialog.setOnCancelListener{
            // Cancelling Pause resumes only the still-current game.
            if(gameView===game && selectedScreen=="game" && window.decorView.hasWindowFocus()) {
                game.paused=false
                sound.resume()
            }
        }
        pauseDialog=dialog
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Measured live buttons, no scroll even when inline pause options open.
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setLayout(
            minOf((resources.displayMetrics.widthPixels-d(24)).coerceAtLeast(1),
                (resources.displayMetrics.widthPixels*.80f).toInt(),d(420)),
            minOf((resources.displayMetrics.heightPixels-d(48)).coerceAtLeast(1),
                d(if(compactPause)480 else 590)))
    }
    private fun startGame(){
        activeStage=null
        prepareFlight(world=0,number=1L,endless=true)
    }

    private fun startLevel(world:Int,number:Long){
        if(world !in 0 until LevelEngine.WORLD_COUNT ||
            number<1L || number>progress.streamFrontier(world))return
        activeStage=Pair(world,number)
        prepareFlight(world,number,endless=false)
    }

    /**
     * Real Loading screen: level generation happens off the UI thread.
     * No staged percentage or arbitrary minimum delay. Consumable perks
     * are spent only once preparation succeeds and we're entering flight;
     * navigating Back before then cannot consume or reset save data.
     */
    private fun prepareFlight(world:Int,number:Long,endless:Boolean){
        if(selectedScreen=="loading")return
        val token=++loadingGeneration
        val chosenPilot=selectedPilotChoice
        selectedScreen="loading"
        showPauseAfterBackground=false
        applyPremiumSystemBars()
        val loading=FlightLoadingView(this,chosenPilot)
        showNativeView(loading)
        Thread({
            val generated=runCatching{LevelEngine.createStream(world,number)}
            runOnUiThread {
                if(token!=loadingGeneration || selectedScreen!="loading" ||
                    isDestroyed || isFinishing)return@runOnUiThread
                if(!hasWindowFocus()) {
                    // Never start audio or decode gameplay sprites while
                    // the Activity is backgrounded or the screen is locked.
                    showHome()
                    return@runOnUiThread
                }
                generated.onSuccess { level ->
                    val perks=progress.consumePerks()
                    val simulation=GameSimulation(level,endless,
                        perks.first,perks.second,progress.difficulty(),number,
                        PilotAbility.fromChoice(chosenPilot))
                    loading.completePreparation()
                    openFlight(simulation,world)
                }.onFailure { error ->
                    android.util.Log.e("BOPAVI","Level preparation failed",error)
                    showHome()
                    android.widget.Toast.makeText(this,
                        "Učitavanje razine nije uspjelo",android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        },"Bopavi-Level-Preparation").start()
    }
    private fun openFlight(simulation:GameSimulation,world:Int){
        showPauseAfterBackground=false
        applyPremiumSystemBars()
        selectedScreen="game"
        val frame=FrameLayout(this).apply{setBackgroundColor(0xff092044.toInt())}
        sound.startWorld(world)
        val game=GameView(this,simulation,progress.lessMotion(),
            pilotSpriteIds[selectedPilotChoice],progress.hapticEnabled(),
            onFinished={if(selectedScreen=="game" && gameView?.game===it)showResult(it)},
            onFlap={sound.effect("tap")},
            onCollect={sound.effect("collect")},
            onShieldImpact={sound.effect("hit")},
            qaCaptureFirstFlap=intent?.getBooleanExtra("bopavi_qa_capture_first_flap",false)==true,
            qaCollisionOverlay=intent?.getBooleanExtra("bopavi_qa_collision_overlay",false)==true)
        gameView=game
        frame.addView(game,FrameLayout.LayoutParams(-1,-1))
        showNativeView(frame)
    }
    private fun showResult(g:GameSimulation){
        showPauseAfterBackground=false
        // Results are a static screen: release the looping decoder and music
        // audio focus before the independently mixed finish sound effect.
        sound.stop()
        val previousRecord=progress.bestPoints()
        val newRecord=g.score()>previousRecord && g.score()>0
        sound.effect(if(g.won)"level" else "hit")
        val headline=if(g.won)"LEVEL COMPLETE!" else "GAME OVER"
        val resultWorld=g.level.world.coerceIn(0,LevelEngine.WORLD_COUNT-1)
        val resultBackdrop=resultWorldDrawableIds[resultWorld]
        progress.recordRun(g)
        val completed=activeStage
        var earnedReward=0
        if(g.won && completed!=null && !g.endless){
            progress.recordLevelStars(completed.first,completed.second,g.completedLevelStars)
            earnedReward=progress.completeLevelFromRecordedRun(
                completed.first,completed.second,g.score())
        }
        gameView?.paused=true;gameView=null;selectedScreen="result"
        @Suppress("DEPRECATION")
        applyPremiumSystemBars()
        if(!g.won){
            val stats=GameOverStats.fromRun(g.distance,g.coins,g.stars,
                g.foodCollected,g.score(),g.endless)
            val result=GameOverScreen(this).create(
                resultBackdrop,portraitDrawableIds[selectedPilotChoice],stats,
                onRetry={
                    val played=activeStage
                    if(played!=null)startLevel(played.first,played.second)
                    else startGame()
                },
                onHome={showHome()})
            showNativeView(result)
            return
        }
        if(g.won && completed!=null && !g.endless){
            val winData=LevelCompleteState.fromRun(
                completed.second,g.completedLevelStars,g.coins,g.score(),earnedReward)
            val screen=LevelCompleteScreen(this).create(
                resultBackdrop,portraitDrawableIds[selectedPilotChoice],winData,
                onNext={
                    if(winData.nextAvailable)startLevel(completed.first,completed.second+1L)
                    else showLevels()
                },
                onHome={showHome()},
                onShare={
                    val text=winData.shareText()
                    val intent=android.content.Intent(android.content.Intent.ACTION_SEND).apply{
                        type="text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT,text)
                    }
                    try {
                        startActivity(android.content.Intent.createChooser(intent,"Share BOPAVI result"))
                    }catch(_:android.content.ActivityNotFoundException){
                        android.widget.Toast.makeText(this,"Sharing unavailable",
                            android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
            showNativeView(screen)
            return
        }
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
        if(g.won && activeStage!=null) {
            action(panel,"▶  NEXT LEVEL"){
                val played=activeStage
                if(played!=null)startLevel(played.first,played.second+1L)
                else showLevels()
            }
        }
        action(panel,"▶  PONOVO"){
            val played=activeStage
            if(played!=null)startLevel(played.first,played.second) else startGame()
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
                    // Repeated system Back never stacks multiple pause windows.
                    gameView?.let { view ->
                        if(pauseDialog?.isShowing!=true)showPremiumPause(view)
                    }
                } // Do not discard live runs.
                else -> showHome() // No extra character or level selection
            }
        } else if(selectedScreen=="home") {
            if (Build.VERSION.SDK_INT >= 33) finish() else super.onBackPressed()
        } else showHome()
    }
}
