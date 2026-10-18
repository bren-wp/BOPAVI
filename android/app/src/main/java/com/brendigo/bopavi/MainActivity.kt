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
    private val blue = 0xff0f3570.toInt()
    private val textColor = Color.WHITE
    private val gold = 0xffffc24d.toInt()
    private val electric = 0xff00d9ff.toInt()
    // Share the nine saved-pilot resource IDs between native screens.
    // All nine are playable; the chosen pilot must remain consistent in UI.
    private val portraitDrawableIds=intArrayOf(R.drawable.bopiportrait0)
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
                if(progress.languageCode()=="hr")"Nastavi ili izađi iz pauze" else "Open paused game controls"
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
    private fun startSceneryDrift(scene:ImageView){
        if(progress.lessMotion() || !android.animation.ValueAnimator.areAnimatorsEnabled()) return
        val drift=android.animation.ObjectAnimator.ofPropertyValuesHolder(
            scene,
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X,1.015f,1.045f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y,1.015f,1.045f)
        ).apply{
            duration=12500L
            repeatMode=android.animation.ValueAnimator.REVERSE
            repeatCount=android.animation.ValueAnimator.INFINITE
            interpolator=android.view.animation.AccelerateDecelerateInterpolator()
        }
        scene.addOnAttachStateChangeListener(object:View.OnAttachStateChangeListener{
            override fun onViewAttachedToWindow(v:View){drift.start()}
            override fun onViewDetachedFromWindow(v:View){drift.cancel()}
        })
    }

    private fun ui(value:String):String=GameLanguages.text(value,progress.languageCode())
    /**
     * Four short 2×4 pages rather than Android's 26-row scrolling list.
     * Native clickable, minimum-48dp targets and a persistent selected state.
     * Everything fits inside even a compact phone without scroll gestures.
     */
    private fun title(parent:LinearLayout,s:String,size:Int,color:Int=textColor) {
        parent.addView(TextView(this).apply {
            text=ui(s)
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
            this.text=ui(text);setTextColor(Color.WHITE)
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
            contentDescription=ui(text)
            setOnClickListener{sound.effect("click");onClick()}
        }
        parent.addView(button,LinearLayout.LayoutParams(-1,d(if(primary)68 else 58)).apply{
            setMargins(0,d(5),0,d(5))
        })
    }
    /** One shared, compact 48dp upper-left Back affordance for every native menu. */
    private fun showHome(){
        applyPremiumSystemBars()
        gameView?.paused=true;gameView=null;gamePauseButton=null
        sound.stop();selectedScreen="home"
        val root=FrameLayout(this).apply{setBackgroundColor(0xff086ab7.toInt())}
        root.addView(ImageView(this).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            startSceneryDrift(this)
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x12011c52,0x001b84c8,0xbb031536.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        // The simplified premium experience has ONE accessible action: fly.
        // Keep the logo and selected pilot independent of scenery artwork.
        val viewport=FitViewportColumn(this).apply{
            clipChildren=false;clipToPadding=false
        }
        val content=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(23),d(18),d(23),d(24))
        }
        viewport.addView(content)
        root.addView(viewport,FrameLayout.LayoutParams(-1,-1))
        content.addView(ImageView(this).apply{
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(156)))
        content.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[0])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(325)).apply{topMargin=d(14)})
        content.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        title(content,if(progress.languageCode()=="hr")"MALI LETOVI · VELIKE PRIČE"
            else "SMALL FLIGHTS · BIG STORIES",16,Color.WHITE)
        action(content,if(progress.languageCode()=="hr")"▶  IGRAJ" else "▶  PLAY"){
            startGame()
        }
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
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(18),d(15),d(18),d(19))
            background=gradient(0xff106bd2.toInt(),0xff031936.toInt(),26).apply{
                setStroke(d(3),electric)
            }
        }


        content.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[0])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(154)))
        title(content,if(progress.languageCode()=="hr")"♛  PAUZA" else "♛  PAUSE",32,Color.WHITE)
        title(content,if(progress.languageCode()=="hr")"Let je pauziran" else "Flight paused",16,0xffbce6ff.toInt())
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
                this.text=ui(text);isAllCaps=false;textSize=18f
                typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
                setTextColor(Color.WHITE)
                setAutoSizeTextTypeUniformWithConfiguration(12,18,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),face,null)
                elevation=d(5).toFloat()
                contentDescription=ui(text)
                setOnClickListener{sound.effect("click");dialog.dismiss();action()}
            }
            content.addView(b,LinearLayout.LayoutParams(-1,d(57)).apply{
                setMargins(d(8),d(4),d(8),d(4))
            })
        }
        command(if(progress.languageCode()=="hr")"▶  NASTAVI" else "▶  CONTINUE",0){
            game.paused=false
            gamePauseButton?.text="Ⅱ"
            gamePauseButton?.contentDescription=if(progress.languageCode()=="hr")"Pauziraj igru" else "Pause game"
            sound.resume()
        }
        command("↻  PONOVO",1){startGame()}
        command("↪  IZLAZ",2){showHome()}
        // The streamlined Pause contains only Continue, Retry and Exit.
        // Changes to saved audio/accessibility preferences are preserved.
        val fitted=FitViewportColumn(this).apply{addView(content)}
        dialog.setView(fitted)
        dialog.setOnCancelListener{
            // Cancelling Pause resumes only the still-current game.
            if(gameView===game && selectedScreen=="game") {
                game.paused=false
                gamePauseButton?.text="Ⅱ"
                gamePauseButton?.contentDescription=if(progress.languageCode()=="hr")"Pauziraj igru" else "Pause game"
                sound.resume()
            }
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Measured live buttons, no scroll even when inline pause options open.
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels-d(24)).coerceAtMost(d(420)),
            (resources.displayMetrics.heightPixels-d(48)).coerceAtLeast(1))
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
        val simulation=GameSimulation(LevelEngine.createStream(0,1L),true,0,0f,1,1L)
        val frame=FrameLayout(this).apply{setBackgroundColor(0xff092044.toInt())}
        sound.startWorld(0)
        // The idle flight preview is not gameplay. Do not expose pause until
        // the first actual flap; never reuse a pause control across screens.
        var pauseButton:Button?=null
        val game=GameView(this,simulation,progress.lessMotion(),0,progress.hapticEnabled(),
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
            languageCode=progress.languageCode())
        gameView=game;frame.addView(game,FrameLayout.LayoutParams(-1,-1))
        val pause=Button(this).apply{
            text="Ⅱ";textSize=23f;setTextColor(Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),gradient(0xff254c79.toInt(),0xff132b51.toInt(),18),null)
            elevation=d(5).toFloat()
            contentDescription=if(progress.languageCode()=="hr")"Pauziraj igru" else "Pause game"
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
        val headline=if(progress.languageCode()=="hr") "REZULTAT" else "RESULT"
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
            orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL
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
            contentDescription=if(progress.languageCode()=="hr")
                "Bopi u svijetu Zeleni otoci" else "Bopi in Green Islands"
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
            setImageResource(R.drawable.bopiportrait0)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,d(186),Gravity.CENTER))
        panel.addView(artwork,LinearLayout.LayoutParams(-1,d(174)).apply{bottomMargin=d(5)})
        title(panel,headline,31,Color.WHITE)
        if(newRecord) {
            val recordBanner=TextView(this).apply {
                text=ui("♛  NOVI REKORD!")
                textSize=19f
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(0xffffecaa.toInt())
                gravity=Gravity.CENTER
                letterSpacing=.06f
                background=gradient(0xffb55b09.toInt(),0xff542c06.toInt(),20).apply{
                    setStroke(d(2),0xffffca46.toInt())
                }
                contentDescription=ui("Novi osobni rekord: ${g.score()} bodova")
            }
            panel.addView(recordBanner,LinearLayout.LayoutParams(-1,d(46)).apply{
                topMargin=d(3);bottomMargin=d(5)
            })
        }
        // Three genuine values from this completed run, never screenshot text.
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
                text=ui(heading);textSize=11f;gravity=Gravity.CENTER
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
                contentDescription=ui("$heading $value")
            }
            card.addView(stat,LinearLayout.LayoutParams(-1,d(34)))
            summary.addView(card,LinearLayout.LayoutParams(0,d(116),1f).apply{
                setMargins(d(3),d(3),d(3),d(3))
            })
            return stat
        }
        resultCard("REZULTAT","★",g.score().toString(),gold)
        resultCard(if(progress.languageCode()=="hr")"HRANA" else "FOOD",
            "🍓",g.foodCollected.toString(),0xffffd5d7.toInt())
        panel.addView(TextView(this).apply{
            val best=progress.bestPoints()
            text=if(progress.languageCode()=="hr")
                "★  NAJBOLJI REZULTAT: $best\n🍓  NAJVIŠE HRANE: ${progress.bestFood()}"
                else "★  BEST SCORE: $best\n🍓  MOST FOOD: ${progress.bestFood()}"
            textSize=16f;gravity=Gravity.CENTER;setTextColor(Color.WHITE)
            setAutoSizeTextTypeUniformWithConfiguration(13,16,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            contentDescription=text
            setPadding(d(10),d(11),d(10),d(11))
            background=gradient(0xf0063978.toInt(),0xf0041a3c.toInt(),21)
        },LinearLayout.LayoutParams(-1,d(60)).apply{topMargin=d(12)})
        action(panel,if(progress.languageCode()=="hr")"▶  PONOVO" else "▶  RETRY"){
            startGame()
        }
        action(panel,if(progress.languageCode()=="hr")"⌂  POČETNA" else "⌂  HOME",false){
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
