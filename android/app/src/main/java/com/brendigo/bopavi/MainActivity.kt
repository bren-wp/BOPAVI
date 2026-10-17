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
    private var currentWorld = 0
    private var currentLevel = 1L
    private var gameView: GameView? = null
    private var platformBackCallback: android.window.OnBackInvokedCallback? = null
    private var selectedScreen = "home"
    private val textColor = Color.WHITE
    private val gold = 0xffffc24d.toInt()
    private val electric = 0xff00d9ff.toInt()
    // Share the nine saved-pilot resource IDs between native screens.
    // All nine are playable; the chosen pilot must remain consistent in UI.
    private val portraitDrawableIds=intArrayOf(
        R.drawable.bopiportrait0,R.drawable.bopiportrait1,R.drawable.bopiportrait2,
        R.drawable.bopiportrait3,R.drawable.bopiportrait4,R.drawable.bopiportrait5,
        R.drawable.bopiportrait6,R.drawable.bopiportrait7,R.drawable.bopiportrait8)
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
        // The operating system displays the launch window. Never show a
        // second in-app splash or fake progress screen.
        progress=ProgressStore(this)
        sound=Soundscape(this)
        sound.enabled=progress.soundEnabled()
        sound.musicVolume=progress.musicVolume()/100f
        sound.effectsVolume=progress.effectsVolume()/100f
        if(Build.VERSION.SDK_INT>=33) {
            val callback=android.window.OnBackInvokedCallback { navigateBack() }
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
        if(gameView?.game?.active == true) gameView?.paused = true
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
    private fun premiumBackground():GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(
            // Preserve rich world art behind the genuine controls. The lower
            // veil remains strongest where the accessible navigation sits.
            0x16031837,0x2c061b3e,0x8604132e.toInt()))

    /** Decorative breathing for the existing illustrated world layers.
     *  Only scenery moves: buttons, counters and hit areas never transform.
     *  Respect both the in-game Reduced Motion preference and Android's
     *  global animator switch. No timers survive a detached menu.
     */
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
    private fun small(parent:LinearLayout,s:String){title(parent,s,16,0xffbce6ff.toInt())}
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
        gameView?.paused=true;gameView=null
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
                intArrayOf(0x28031a50,0x001d80bf,0x18052250,0xb9031635.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        val fitted=FitViewportColumn(this)
        val content=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(22),d(8),d(22),d(20))
        }
        fitted.addView(content)
        root.addView(fitted,FrameLayout.LayoutParams(-1,-1))
        // Historical scores remain stored and visible; no wallet or purchase UI.
        val record=TextView(this).apply{
            text=ui("★  NAJBOLJI REZULTAT: ${progress.bestPoints()}")
            textSize=15f
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity=Gravity.CENTER
            background=gradient(0xdb073b83.toInt(),0xec031b44.toInt(),23).apply{
                setStroke(d(2),electric)
            }
            contentDescription=ui("Najbolji rezultat ${progress.bestPoints()}")
        }
        content.addView(record,LinearLayout.LayoutParams(-1,d(47)))
        content.addView(ImageView(this).apply{
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(154)).apply{topMargin=d(14)})
        content.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[progress.skin()])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(310)).apply{topMargin=d(3)})
        content.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        action(content,"▶  IGRAJ"){
            // Preserve the legacy chosen world as the opening biome, while
            // removing world/level selection and starting one endless flight.
            startGame(progress.chosenWorld(),1L)
        }
        small(content,if(progress.languageCode()=="hr")
            "Dodirni zaslon za zamah. Leti što dalje!"
            else "Tap to flap. Fly as far as you can!")
        showNativeView(root)
        fitted.setOnApplyWindowInsetsListener{v,insets->
            val bars=if(Build.VERSION.SDK_INT>=30)
                insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
            else null
            val left=bars?.left?:0
            val top=bars?.top?:0
            val right=bars?.right?:0
            val bottom=bars?.bottom?:0
            if(v.paddingLeft!=left||v.paddingTop!=top||
                v.paddingRight!=right||v.paddingBottom!=bottom){
                v.setPadding(left,top,right,bottom)
            }
            insets
        }
        fitted.requestApplyInsets()
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
            setImageResource(portraitDrawableIds[progress.skin()])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(154)))
        title(content,if(progress.languageCode()=="hr")"♛  PAUZA" else "♛  PAUSE",32,Color.WHITE)
        small(content,if(progress.languageCode()=="hr")"Let je zaustavljen" else "Flight paused")
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
        command(if(progress.languageCode()=="hr")"▶  NASTAVI" else "▶  CONTINUE",0){game.paused=false;sound.resume()}
        command("↻  PONOVO",1){startGame(currentWorld,currentLevel)}
        command("↪  IZLAZ",2){showHome()}
        small(content,if(progress.languageCode()=="hr")
            "Dodirni zaslon za zamah. Nastavi, pokušaj ponovno ili se vrati."
            else "Tap to flap. Resume, retry or return Home.")
        val fitted=FitViewportColumn(this).apply{addView(content)}
        dialog.setView(fitted)
        dialog.setOnCancelListener{game.paused=false;sound.resume()}
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Measured live buttons, no scroll even when inline pause options open.
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels-d(24)).coerceAtMost(d(420)),
            (resources.displayMetrics.heightPixels-d(48)).coerceAtLeast(1))
    }
    private fun startGame(world:Int,number:Long){
        // Hide only the status bar: IMMERSIVE_STICKY + HIDE_NAVIGATION
        // triggers Android's full-screen onboarding popup on the first game.
        // Keep the gesture navigation strip dark (theme sets its color) instead.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        currentWorld=world;currentLevel=number;selectedScreen="game"
        val simulation=GameSimulation(LevelEngine.createStream(world,number),true,0,0f,progress.difficulty(),number)
        val frame=FrameLayout(this).apply{setBackgroundColor(0xff092044.toInt())}
        sound.startWorld(world)
        // The idle flight preview is not gameplay. Do not expose pause until
        // the first actual flap; never reuse a pause control across screens.
        var pauseButton:Button?=null
        val game=GameView(this,simulation,progress.lessMotion(),progress.skin(),progress.hapticEnabled(),
            onFinished={if(selectedScreen=="game" && gameView?.game === it)showResult(it)},
            onLevelCompleted={ _ -> }, // endless flight, no level rewards
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
            walletCoins={progress.coins()},languageCode=progress.languageCode())
        gameView=game;frame.addView(game,FrameLayout.LayoutParams(-1,-1))
        val pause=Button(this).apply{
            text="Ⅱ";textSize=23f;setTextColor(Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),gradient(0xff254c79.toInt(),0xff132b51.toInt(),18),null)
            elevation=d(5).toFloat()
            contentDescription="Izbornik tijekom igre"
            visibility=View.INVISIBLE
            setOnClickListener{
                if(!game.game.active || game.game.finished || selectedScreen!="game")return@setOnClickListener
                game.paused=true;sound.pause()
                showPremiumPause(game)

            }
        }
        pauseButton=pause
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
    /** One terminal result, saved locally; no shop, level or achievement routes. */
    private fun showResult(g:GameSimulation){
        val previousBest=progress.bestPoints()
        val newRecord=g.score()>previousBest && g.score()>0
        sound.effect("hit")
        progress.recordRun(g)
        gameView?.paused=true;gameView=null
        selectedScreen="result"
        applyPremiumSystemBars()
        val root=FrameLayout(this).apply{setBackgroundColor(0xff0a4b88.toInt())}
        root.addView(ImageView(this).apply{
            setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x55052250,0x44032557,0xd1041436.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        val fit=FitViewportColumn(this)
        val panel=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(19),d(12),d(19),d(18))
        }
        fit.addView(panel)
        root.addView(fit,FrameLayout.LayoutParams(-1,-1))
        panel.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[progress.skin()])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(198)))
        title(panel,if(newRecord)"♛  NOVI REKORD!" else "REZULTAT",32,Color.WHITE)
        if(newRecord)small(panel,if(progress.languageCode()=="hr")
            "Tvoj najbolji let!" else "Your best flight!")
        val stats=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(d(14),d(12),d(14),d(12))
            background=gradient(0xf50b4c9e.toInt(),0xf5031b43.toInt(),26).apply{
                setStroke(d(3),electric)
            }
        }
        panel.addView(stats,LinearLayout.LayoutParams(-1,-2).apply{
            topMargin=d(15);bottomMargin=d(14)
        })
        fun stat(label:String,value:String){
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL
                gravity=Gravity.CENTER_VERTICAL
            }
            val name=TextView(this).apply{
                text=ui(label);textSize=17f
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                setTextColor(Color.WHITE)
            }
            row.addView(name,LinearLayout.LayoutParams(0,d(56),1f))
            row.addView(TextView(this).apply{
                text=value;textSize=24f;setTextColor(gold)
                gravity=Gravity.CENTER_VERTICAL or Gravity.END
                typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
                contentDescription=ui("$label $value")
            },LinearLayout.LayoutParams(-2,d(56)))
            stats.addView(row,LinearLayout.LayoutParams(-1,-2))
        }
        stat("★  REZULTAT",g.score().toString())
        stat("♛  PROLAZI",g.totalPassed.toString())
        stat("🏆  NAJBOLJI REZULTAT",progress.bestPoints().toString())
        action(panel,"▶  PONOVO"){startGame(currentWorld,1L)}
        action(panel,"⌂  POČETNA",false){showHome()}
        showNativeView(root)
        fit.setOnApplyWindowInsetsListener{view,insets->
            if(Build.VERSION.SDK_INT>=30){
                val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
                view.setPadding(safe.left,safe.top,safe.right,safe.bottom)
            }
            insets
        }
        fit.requestApplyInsets()
    }

    @Deprecated("Android Back compatibility")
    override fun onBackPressed() = navigateBack()

    private fun navigateBack(){
        val active=gameView
        if(selectedScreen=="game" && active!=null){
            if(active.game.active && !active.game.finished) {
                active.paused=true
                sound.pause()
                showPremiumPause(active)
            } else showHome()
        } else if(selectedScreen=="home") {
            finish()
        } else showHome()
    }
}
