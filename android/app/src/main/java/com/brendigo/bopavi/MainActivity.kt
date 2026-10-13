package com.brendigo.bopavi

import android.app.Activity
import android.content.Intent
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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/** Fully native Android activity, controls and GPU Canvas game surface. */
class MainActivity : Activity() {
    private lateinit var progress: ProgressStore
    private lateinit var sound: Soundscape
    private var currentWorld = 0
    private var currentLevel = 1L
    private var gameView: GameView? = null
    private var gamePauseButton: Button? = null
    private var warmedHomeArt: android.graphics.Bitmap? = null
    private var platformBackCallback: android.window.OnBackInvokedCallback? = null
    private var selectedScreen = "home"
    private enum class MenuOrigin { HOME, SETTINGS, PROFILE, SHOP, RECORDS }
    private var menuBackAction:(()->Unit)?=null
    private var recordsReturnToSettings=false
    private val blue = 0xff0f3570.toInt()
    private val textColor = Color.WHITE
    private val gold = 0xffffc24d.toInt()
    private val ocean = 0xff081e45.toInt()
    private val electric = 0xff00d9ff.toInt()
    // One shared drawable lookup table for legacy pilot-save art. Bopi is
    // the only active pilot; avoid allocating the same nine-entry array in
    // Home, pause, Settings and Profile on every screen transition.
    private val portraitDrawableIds=intArrayOf(
        R.drawable.bopiportrait0,R.drawable.bopiportrait1,R.drawable.bopiportrait2,
        R.drawable.bopiportrait3,R.drawable.bopiportrait4,R.drawable.bopiportrait5,
        R.drawable.bopiportrait6,R.drawable.bopiportrait7,R.drawable.bopiportrait8)
    private val worldAccents = intArrayOf(0xff3bd48f.toInt(),0xff7feafe.toInt(),0xffff8163.toInt(),0xffb39afa.toInt(),0xff65e8e1.toInt(),0xffffd891.toInt(),0xffb4ed73.toInt(),0xffbcb1ff.toInt())
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
        showSplash()
        // Prepare the REAL HomeArt bitmap off the UI thread. The illustrated
        // splash remains visible only while startup resources are being loaded,
        // never for an arbitrary delay or a fabricated percentage.
        Thread({
            val loaded=runCatching {
                android.graphics.BitmapFactory.decodeResource(
                    resources,R.drawable.homeback,
                    android.graphics.BitmapFactory.Options().apply{inScaled=false})
            }.getOrNull()
            runOnUiThread {
                if(isFinishing || isDestroyed) {
                    loaded?.recycle()
                    return@runOnUiThread
                }
                warmedHomeArt=loaded
                progress=ProgressStore(this)
                sound=Soundscape(this)
                sound.enabled=progress.soundEnabled()
                sound.musicVolume=progress.musicVolume()/100f
                sound.effectsVolume=progress.effectsVolume()/100f
                // Android 16 predictive Back requires an explicit callback.
                if (Build.VERSION.SDK_INT >= 33) {
                    val callback = android.window.OnBackInvokedCallback { navigateBack() }
                    onBackInvokedDispatcher.registerOnBackInvokedCallback(
                        android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
                    platformBackCallback = callback
                }
                showHome()
            }
        },"BOPAVI-startup-assets").start()
    }
    /** Non-scrolling startup stage: live indeterminate loading, never a fake bar. */
    private fun showSplash() {
        selectedScreen="splash"
        val root=FrameLayout(this).apply{setBackgroundColor(0xff146bb7.toInt())}
        root.addView(ImageView(this).apply{
            setImageResource(R.drawable.splash)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        val footer=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(d(18),d(11),d(18),d(13))
            background=gradient(0xe5082c63.toInt(),0xef031937.toInt(),20).apply{
                setStroke(d(2),electric)
            }
        }
        footer.addView(TextView(this).apply{
            text="UČITAVANJE..."
            textSize=13f
            typeface=Typeface.DEFAULT_BOLD
            letterSpacing=.09f
            gravity=Gravity.CENTER
            setTextColor(Color.WHITE)
            contentDescription="BOPAVI, učitavanje igre"
        },LinearLayout.LayoutParams(-1,d(29)))
        footer.addView(android.widget.ProgressBar(
            this,null,android.R.attr.progressBarStyleHorizontal).apply{
            isIndeterminate=true
            indeterminateTintList=ColorStateList.valueOf(gold)
            progressBackgroundTintList=ColorStateList.valueOf(0xff153d73.toInt())
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(9)).apply{topMargin=d(3)})
        val position=FrameLayout.LayoutParams(d(252),-2,Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply{
            bottomMargin=d(45)
        }
        root.addView(footer,position)
        root.setOnApplyWindowInsetsListener{_,insets->
            if(Build.VERSION.SDK_INT>=30){
                val bars=insets.getInsets(android.view.WindowInsets.Type.navigationBars()
                    or android.view.WindowInsets.Type.displayCutout())
                val params=footer.layoutParams as FrameLayout.LayoutParams
                val bottom=bars.bottom+d(30)
                if(params.bottomMargin!=bottom){params.bottomMargin=bottom;footer.layoutParams=params}
            }
            insets
        }
        showNativeView(root)
    }
    private fun showNativeView(root: View) {
        // Keep the scene artwork edge-to-edge. Individual ScrollViews (home,
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
            0x66031837,0x99061b3e.toInt(),0xdd04132e.toInt()))

    private fun chip(label:String):TextView = TextView(this).apply {
        text=label
        setTextColor(gold)
        textSize=16f
        typeface=Typeface.create("sans-serif-medium",Typeface.BOLD)
        setSingleLine(true)
        setAutoSizeTextTypeUniformWithConfiguration(11,16,1,
            android.util.TypedValue.COMPLEX_UNIT_SP)
        gravity=Gravity.CENTER
        setPadding(d(16),d(13),d(16),d(13))
        background=gradient(0xff192d50.toInt(),0xff203c65.toInt(),24)
        contentDescription=label
    }
    private fun base(label:String,subtitle:String):LinearLayout {
        @Suppress("DEPRECATION")
        applyPremiumSystemBars()
        menuBackAction=null;gameView?.paused=true;gameView=null;gamePauseButton=null;sound.stop();selectedScreen=label
        // Real world art is a full-bleed layer; all interactive labels remain real
        // views instead of baking fake scores, buttons or progress into a PNG.
        val root=FrameLayout(this).apply{setBackgroundColor(ocean)}
        val scenes=intArrayOf(R.drawable.world0,R.drawable.world1,R.drawable.world2,R.drawable.world3,
            R.drawable.world4,R.drawable.world5,R.drawable.world6,R.drawable.world7)
        root.addView(ImageView(this).apply{
            setImageResource(scenes[currentWorld.coerceIn(0,7)])
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{background=premiumBackground()},FrameLayout.LayoutParams(-1,-1))
        val body=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(d(if(label=="PROFIL")12 else 18),d(if(label=="PROFIL")7 else 18),d(if(label=="PROFIL")12 else 18),d(if(label=="PROFIL")7 else 28))
        }
        if(label=="TRGOVINA" || label=="MOJI REKORDI" || label=="PROFIL" || label=="POSTIGNUĆA"){
            // Finite menus fit physical safe areas instead of hiding content
            // in a long scroller. Do not apply a double root inset.
            val fitted=FitViewportColumn(this)
            fitted.addView(body)
            root.addView(fitted,FrameLayout.LayoutParams(-1,-1))
            fitted.setOnApplyWindowInsetsListener{v,insets->
                if(Build.VERSION.SDK_INT>=30){
                    val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
                }
                insets
            }
        } else {
            val scroll=ScrollView(this).apply{
                isFillViewport=true;isVerticalScrollBarEnabled=false
                clipToPadding=false
                addView(body)
            }
            root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
            if(Build.VERSION.SDK_INT>=35){
                scroll.setOnApplyWindowInsetsListener{v,insets->
                    val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        or android.view.WindowInsets.Type.displayCutout())
                    v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
                    insets
                }
            }
        }
        showNativeView(root)
        if(label=="POSTAVKE") {
            // Genuine chosen-pilot art beside the wordmark, not a static
            // screenshot or a fake character. Matches the reference header.
            val hero=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL
                gravity=Gravity.CENTER_VERTICAL
            }
            hero.addView(ImageView(this).apply {
                setImageResource(R.drawable.logo)
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(0,d(110),1f))


            hero.addView(ImageView(this).apply {
                setImageResource(portraitDrawableIds[0])
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(d(117),d(117)))
            body.addView(hero,LinearLayout.LayoutParams(-1,d(118)))
        } else {
            ImageView(this).apply {
                setImageResource(R.drawable.logo)
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }.also{body.addView(it,LinearLayout.LayoutParams(-1,d(if(label=="PROFIL")60 else 94)))}
        }
        if(label!="BOPAVI")title(body,label,if(label=="PROFIL")25 else 31,Color.WHITE)
        if(subtitle.isNotEmpty() && label!="BOPAVI")title(body,subtitle,if(label=="PROFIL")11 else 14,0xffc9edff.toInt())
        return body
    }
    private fun title(parent:LinearLayout,s:String,size:Int,color:Int=textColor) {
        parent.addView(TextView(this).apply {
            text=s
            textSize=size.toFloat()
            setTextColor(color)
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            gravity=Gravity.CENTER
            setPadding(d(6),d(11),d(6),d(11))
            if(size>=26)background=gradient(0xee064e9a.toInt(),0xee062b61.toInt(),24).apply {
                setStroke(d(2),0xff50d7ff.toInt())
            }
            setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),0xff001936.toInt())
            setAutoSizeTextTypeUniformWithConfiguration((size*.72f).toInt(),size,1,android.util.TypedValue.COMPLEX_UNIT_SP)
        },LinearLayout.LayoutParams(-1,-2))
    }
    private fun small(parent:LinearLayout,s:String){title(parent,s,16,0xffbce6ff.toInt())}
    private fun sectionHeading(parent:LinearLayout,heading:String){
        val label=TextView(this).apply{
            text=heading;setTextColor(Color.WHITE);textSize=15f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            letterSpacing=.06f
            gravity=Gravity.CENTER_VERTICAL or Gravity.START
            setPadding(d(18),d(11),d(16),d(11))
            background=gradient(0xff224e89.toInt(),0xff153760.toInt(),23)
            contentDescription=heading
        }
        parent.addView(label,LinearLayout.LayoutParams(-1,-2).apply{
            topMargin=d(17);bottomMargin=d(7)
        })
    }

    /** Single-viewport Profile headings, keeping accessibility and live views. */
    private fun profileHeading(parent:LinearLayout,heading:String) {
        parent.addView(TextView(this).apply{
            text=heading;setTextColor(Color.WHITE);textSize=14f
            typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER_VERTICAL
            setPadding(d(13),0,d(12),0)
            background=gradient(0xf70b3878.toInt(),0xf6061c41.toInt(),15).apply{
                setStroke(d(1),electric)
            }
            contentDescription=heading
        },LinearLayout.LayoutParams(-1,d(33)).apply{topMargin=d(4);bottomMargin=d(4)})
    }
    private fun action(parent:LinearLayout,text:String,primary:Boolean=true,onClick:()->Unit) {
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
    private fun back(parent:LinearLayout,onClick:()->Unit) {
        menuBackAction=onClick
        action(parent,"‹  Natrag",false,onClick)
    }
    private fun worldTile(parent:LinearLayout,world:Int,onClick:()->Unit){
        val name=LevelEngine.names[world]
        val row=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            background=gradient(0xff114987.toInt(),0xff031b3a.toInt(),20).apply {
                setStroke(d(if(world==progress.chosenWorld())3 else 2),
                    if(world==progress.chosenWorld())gold else worldAccents[world])
            }
            setPadding(d(7),d(8),d(7),d(10))
            elevation=d(if(world==progress.chosenWorld())8 else 4).toFloat()
            isClickable=true;isFocusable=true
            isSelected=world==progress.chosenWorld()
            foreground=RippleDrawable(ColorStateList.valueOf(0x33ffffff),null,null)
            // Each world is playable. All values come from the existing save.
            contentDescription="$name, otključano, level ${progress.streamFrontier(world)}, " +
                "prikupljeno ${progress.collectibles(world)}, rekord ${progress.best(world)}, " +
                "zvjezdice ${progress.worldStars(world)} od 30" +
                ", upravljanje dodirom" +
                (if(world==progress.chosenWorld()) ", odabrano" else "")
            setOnClickListener{sound.effect("click");onClick()}
        }
        val artPanel=FrameLayout(this).apply{
            background=gradient(0xff0b5ca8.toInt(),0xff071b3f.toInt(),17)
            clipToOutline=true
        }
        val art=intArrayOf(R.drawable.world0,R.drawable.world1,R.drawable.world2,R.drawable.world3,
            R.drawable.world4,R.drawable.world5,R.drawable.world6,R.drawable.world7)
        artPanel.addView(ImageView(this).apply{
            setImageResource(art[world])
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        // Reference-style 2×4 cards: genuine metrics and labels overlay the
        // lower illustration. No duplicated text rows below every image.
        artPanel.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x00071c38,0x17071c38,0xe903122f.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        artPanel.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x0004162f,0xf5021329.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,d(70),Gravity.BOTTOM))
        val badge=TextView(this).apply{
            text="♛  ${world+1}"
            setTextColor(Color.WHITE);textSize=15f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            gravity=Gravity.CENTER
            setPadding(d(7),0,d(7),0)
            background=gradient(if(world==progress.chosenWorld())0xffd78b08.toInt()
                else 0xff0d80e1.toInt(),0xff061f55.toInt(),18).apply{
                setStroke(d(2),if(world==progress.chosenWorld())gold else electric)
            }
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        artPanel.addView(badge,FrameLayout.LayoutParams(d(64),d(36),Gravity.TOP or Gravity.LEFT).apply{
            setMargins(d(8),d(8),0,0)
        })
        // Mode badge mirrors the real touch handler; card remains one tap target.
        artPanel.addView(TextView(this).apply{
            text="● TAP"
            textSize=10f;typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE)
            gravity=Gravity.CENTER
            background=gradient(0xff1763a7.toInt(),0xff092450.toInt(),14).apply{setStroke(d(1),electric)}
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(d(85),d(25),Gravity.TOP or Gravity.LEFT).apply{
            setMargins(d(8),d(47),0,0)
        })
        artPanel.addView(TextView(this).apply{
            text="›";textSize=31f;setTextColor(Color.WHITE)
            gravity=Gravity.CENTER
            background=gradient(0xff0bbcff.toInt(),0xff0650bc.toInt(),21)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(d(38),d(38),Gravity.BOTTOM or Gravity.RIGHT).apply{
            setMargins(0,0,d(8),d(13))
        })
        // Title and progress are both live overlays; the entire card remains
        // one accessible tap target even on small phones.
        artPanel.addView(TextView(this).apply{
            text=name.uppercase()
            textSize=15f;setTextColor(Color.WHITE)
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            gravity=Gravity.CENTER_VERTICAL or Gravity.LEFT
            setSingleLine(true)
            setAutoSizeTextTypeUniformWithConfiguration(11,15,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),Color.BLACK)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,d(31),Gravity.BOTTOM).apply{
            leftMargin=d(10);rightMargin=d(8);bottomMargin=d(29)
        })
        artPanel.addView(TextView(this).apply{
            text="★ ${progress.worldStars(world)}/30  ·  ♛ ${progress.best(world)}"
            textSize=13f;setTextColor(0xffffd166.toInt())
            gravity=Gravity.CENTER_VERTICAL or Gravity.LEFT
            typeface=Typeface.DEFAULT_BOLD;setSingleLine(true)
            setAutoSizeTextTypeUniformWithConfiguration(9,13,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            setShadowLayer(d(2).toFloat(),0f,d(1).toFloat(),0xff00142e.toInt())
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,d(25),Gravity.BOTTOM).apply{
            leftMargin=d(10);rightMargin=d(50);bottomMargin=d(3)
        })
        row.addView(artPanel,LinearLayout.LayoutParams(-1,d(132)))
        parent.addView(row,LinearLayout.LayoutParams(0,-2,1f).apply{
            setMargins(d(4),d(5),d(4),d(7))
        })
    }
    private fun showHome(){
        @Suppress("DEPRECATION")
        applyPremiumSystemBars()
        menuBackAction=null;gameView?.paused=true;gameView=null;gamePauseButton=null;sound.stop();selectedScreen="home"
        val root=FrameLayout(this).apply{setBackgroundColor(0xff086ab7.toInt())}
        root.addView(ImageView(this).apply {
            val art=warmedHomeArt
            if(art!=null && !art.isRecycled) setImageBitmap(art)
            else setImageResource(R.drawable.homeback)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x55052251,0x001967ae,0x001967ae,0xbb03112e.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        // The first screen has no scroll gesture. On compact/notched devices
        // live controls are fitted as one coherent viewport rather than cropped.
        val scroll=FitViewportColumn(this).apply {
            clipToPadding=false
            clipChildren=false
        }
        val layout=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(22),d(12),d(22),d(16))
        }
        scroll.addView(layout)
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
        // All four header elements are accessible live actions, never
        // screenshots or non-clickable mock currency. Use the saved pilot.
        val wallets=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        val avatar=ImageView(this).apply{


            setImageResource(portraitDrawableIds[0])
            scaleType=ImageView.ScaleType.FIT_CENTER
            background=GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xff126bd9.toInt(),0xff031b44.toInt())).apply{
                shape=GradientDrawable.OVAL
                setStroke(d(2),gold)
            }
            clipToOutline=true
            setPadding(d(3),d(3),d(3),d(3))
            isClickable=true;isFocusable=true
            contentDescription="PROFIL · Bopi"
            setOnClickListener{sound.effect("click");showProfile()}
        }
        wallets.addView(avatar,LinearLayout.LayoutParams(d(50),d(50)).apply{
            rightMargin=d(5)
        })
        val best=chip("🏆  ${progress.bestPoints()}").apply{
            setTextColor(Color.WHITE)
            setPadding(d(4),d(9),d(4),d(9))
            isClickable=true;isFocusable=true
            contentDescription="ZADACI I POSTIGNUĆA · najbolji rezultat ${progress.bestPoints()}"
            setOnClickListener{sound.effect("click");showAchievements(MenuOrigin.HOME)}
        }
        wallets.addView(best,LinearLayout.LayoutParams(0,d(48),1f).apply{
            rightMargin=d(4)
        })
        val coins=chip("♛  ${progress.coins()}").apply{
            setTextColor(gold)
            setPadding(d(4),d(9),d(4),d(9))
            isClickable=true;isFocusable=true
            contentDescription="TRGOVINA · ${progress.coins()} osvojenih kovanica"
            setOnClickListener{sound.effect("click");showPerks(MenuOrigin.HOME)}
        }
        wallets.addView(coins,LinearLayout.LayoutParams(0,d(48),1f).apply{
            rightMargin=d(4)
        })
        val gear=TextView(this).apply{
            text="⚙";textSize=27f;gravity=Gravity.CENTER
            setTextColor(Color.WHITE)
            background=gradient(0xff147bdc.toInt(),0xff05265c.toInt(),18).apply{
                setStroke(d(2),electric)
            }
            isClickable=true;isFocusable=true
            contentDescription="POSTAVKE"
            setOnClickListener{sound.effect("click");showSettings()}
        }
        wallets.addView(gear,LinearLayout.LayoutParams(d(48),d(48)))
        layout.addView(wallets,LinearLayout.LayoutParams(-1,d(52)))
        layout.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        action(layout,"▶  IGRAJ"){
            val world=progress.chosenWorld()
            showPilotPicker(world,progress.streamFrontier(world))
        }
        action(layout,"🌍  SVJETOVI",false){showWorlds()}
        action(layout,"⚙  POSTAVKE",false){showSettings()}
        val navigation=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            setPadding(d(5),d(7),d(5),d(6))
            background=gradient(0xf3062a64.toInt(),0xfa041736.toInt(),23).apply{
                setStroke(d(2),electric)
            }
        }
        val entries=listOf("PROFIL","ZADACI","REKORDI","TRGOVINA")
        val footerIcons=intArrayOf(R.drawable.nav_profile,R.drawable.nav_tasks,
            R.drawable.nav_collection,R.drawable.nav_shop)
        for((index,label) in entries.withIndex()){
            val tab=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER
                isClickable=true;isFocusable=true
                contentDescription=label
                background=RippleDrawable(ColorStateList.valueOf(0x33ffffff),
                    gradient(0xff1358a5.toInt(),0xff071e46.toInt(),14),null)
                addView(ImageView(this@MainActivity).apply{
                    setImageResource(footerIcons[index])
                    scaleType=ImageView.ScaleType.FIT_CENTER
                    setPadding(d(1),d(1),d(1),d(1))
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(29)))
                addView(TextView(this@MainActivity).apply{
                    text=label;textSize=11f;setTextColor(Color.WHITE)
                    gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD
                    setSingleLine(true)
                    setAutoSizeTextTypeUniformWithConfiguration(8,11,1,
                        android.util.TypedValue.COMPLEX_UNIT_SP)
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(21)))
                setOnClickListener{
                    sound.effect("click")
                    when(index){
                        0->showProfile()
                        1->showAchievements(MenuOrigin.HOME)
                        2->showLeaderboard()
                        else->showPerks(MenuOrigin.HOME)
                    }
                }
            }
            navigation.addView(tab,LinearLayout.LayoutParams(0,d(62),1f).apply{
                setMargins(d(2),0,d(2),0)
            })
        }
        layout.addView(navigation,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(15)})
        showNativeView(root)
        if(Build.VERSION.SDK_INT>=35){
            scroll.setOnApplyWindowInsetsListener{v,insets->
                val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())
                v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
                insets
            }
            scroll.requestApplyInsets()
        }
    }

    private fun showWorlds(){
        val b=base("SVJETOVI","Osam različitih avantura")
        for(line in 0..3) {
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.TOP
            }
            b.addView(row,LinearLayout.LayoutParams(-1,-2))
            for(col in 0..1){
                val w=line*2+col
                worldTile(row,w){showLevels(w,1)}
            }
        }
        back(b){showHome()}
    }
    private fun showLevels(world:Int,page:Int){
        currentWorld=world;progress.chooseWorld(world)
        val b=base("LEVELI","Odaberite level · ${LevelEngine.names[world]} · " +
            "TAP ZA ZAMAH")
        val maxNumber=progress.frontier(world).coerceAtMost(LevelEngine.LEVELS_PER_WORLD)
        val safePage=page.coerceIn(1,LevelEngine.LEVELS_PER_WORLD)
        val start=((safePage-1)/20)*20+1
        val zone=LevelEngine.create(world,start).zone
        val pageNumber=(start-1)/20+1
        val pageCount=(LevelEngine.LEVELS_PER_WORLD-1)/20+1
        small(b,"ZONA $zone · LEVELI $start–${(start+19).coerceAtMost(LevelEngine.LEVELS_PER_WORLD)}")
        small(b,"STRANICA $pageNumber / $pageCount" +
            if(start>maxNumber)" · ZAKLJUČANI LEVELI" else " · ★ ${progress.worldStars(world)}/30")
        small(b,"● NORMALNI · ⚡ IZAZOVNI · ✦ BONUS · ♛ ELITNI")
        for(r in 0 until 5){
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            for(col in 0 until 4){
                val n=start+r*4+col
                if(n>LevelEngine.LEVELS_PER_WORLD)continue
                val kind=LevelEngine.create(world,n).type
                levelTile(row,world,n,maxNumber,kind)
            }
            if(row.childCount>0)b.addView(row,LinearLayout.LayoutParams(-1,-2))
        }
        val pager=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        b.addView(pager,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(8)})
        if(start>1){
            val prev=LinearLayout(this)
            pager.addView(prev,LinearLayout.LayoutParams(0,-2,1f).apply{rightMargin=d(4)})
            action(prev,"← PRETHODNIH 20",false){showLevels(world,start-20)}
        }
        if(start+20<=LevelEngine.LEVELS_PER_WORLD){
            val next=LinearLayout(this)
            pager.addView(next,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=d(4)})
            action(next,"SLJEDEĆIH 20 →",true){showLevels(world,start+20)}
        }
        sectionHeading(b,if(start<=maxNumber)"NASTAVI ILI ODABERI LEVEL"
            else "PREGLED ZAKLJUČANIH LEVELA")
        small(b,"Otključano do levela $maxNumber")
        // The continue CTA is available only on pages containing at least one
        // unlocked tile. A preview-only page cannot expose a launch action.
        if(start<=maxNumber){
        action(b,"▶  NASTAVI LET"){showPilotPicker(world,progress.streamFrontier(world))}
        val input=EditText(this).apply{
            inputType=android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true);hint="Broj otključanog levela"
            setTextColor(Color.WHITE);setHintTextColor(0xffb2d6ff.toInt())
            setText(maxNumber.coerceIn(start,(start+19).coerceAtMost(LevelEngine.LEVELS_PER_WORLD)).toString());gravity=Gravity.CENTER
            background=gradient(0xff082c61.toInt(),0xff07183d.toInt(),18)
        }
        b.addView(input,LinearLayout.LayoutParams(-1,d(52)))
        action(b,"▶  POKRENI ODABRANI LEVEL",false){
            val n=input.text.toString().toIntOrNull()
            if(n==null||n !in 1..maxNumber)Toast.makeText(this,
                "Level mora biti otključan i unutar raspona.",Toast.LENGTH_LONG).show()
            else showPilotPicker(world,n.toLong())
        }
        }else{
            small(b,"Ovi leveli su zaključani. Dovrši prethodne za otključavanje.")
            action(b,"↶ VRATI NA OTKLJUČANE LEVELE",false){
                showLevels(world,maxNumber)
            }
        }
        back(b){showWorlds()}
    }
    /**
     * A fully interactive illustrated tile, never a pasted screenshot.
     * The lock/checkmark comes from the same persisted frontier as gameplay.
     */
    private fun levelTile(parent:LinearLayout,world:Int,n:Int,frontier:Int,kind:Int){
        val unlocked=n<=frontier
        val completed=n<frontier
        val current=n==frontier
        val frame=FrameLayout(this).apply{
            isClickable=true;isFocusable=true
            background=gradient(0xff0b6db9.toInt(),0xff071a43.toInt(),16).apply{
                setStroke(d(if(current)3 else 1),if(current)gold else 0xff65cfff.toInt())
            }
            clipToOutline=true
            elevation=d(if(current)6 else 2).toFloat()
            contentDescription="Level $n, ${LevelKind.name(kind)}, ${if(!unlocked)"zaključan" else if(completed)"dovršen" else "otključan"}, " +
                "zvjezdice ${progress.bestLevelStars(world,n.toLong())} od 3"
            foreground=RippleDrawable(ColorStateList.valueOf(0x55ffffff),null,
                GradientDrawable().apply{cornerRadius=d(16).toFloat();setColor(Color.WHITE)})
            setOnClickListener{
                if(unlocked){sound.effect("click");showPilotPicker(world,n.toLong())}
                else Toast.makeText(this@MainActivity,"Prvo dovrši prethodni level.",Toast.LENGTH_SHORT).show()
            }
        }
        val art=intArrayOf(R.drawable.world0,R.drawable.world1,R.drawable.world2,R.drawable.world3,
            R.drawable.world4,R.drawable.world5,R.drawable.world6,R.drawable.world7)
        frame.addView(ImageView(this).apply{
            setImageResource(art[world]);scaleType=ImageView.ScaleType.CENTER_CROP
            alpha=if(unlocked)0.95f else 0.36f
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        frame.addView(View(this).apply {
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(
                0x99052245.toInt(),0x11002144,if(unlocked)0xcc05244e.toInt() else 0xee08172e.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        val level=TextView(this).apply{
            text=n.toString();textSize=22f;gravity=Gravity.CENTER
            setTextColor(if(unlocked)Color.WHITE else 0xffa6b4ce.toInt())
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),Color.BLACK)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        frame.addView(level,FrameLayout.LayoutParams(-1,d(35),Gravity.TOP))
        val badge=TextView(this).apply{
            text=if(!unlocked)"🔒" else if(completed)"✓" else LevelKind.icon(kind)
            textSize=17f;gravity=Gravity.CENTER
            setTextColor(if(current)gold else Color.WHITE)
            setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),Color.BLACK)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        frame.addView(badge,FrameLayout.LayoutParams(
            if(unlocked)d(31) else -1,d(34),
            if(unlocked)Gravity.TOP or Gravity.RIGHT else Gravity.CENTER))
        val starCount=progress.bestLevelStars(world,n.toLong())
        val rating=TextView(this).apply{
            text="★".repeat(starCount)+"☆".repeat(3-starCount)
            textSize=16f;gravity=Gravity.CENTER
            setTextColor(if(starCount>0)gold else 0xffadc9ed.toInt())
            setShadowLayer(d(1).toFloat(),0f,d(2).toFloat(),Color.BLACK)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        frame.addView(rating,FrameLayout.LayoutParams(-1,d(24),Gravity.BOTTOM))
        parent.addView(frame,LinearLayout.LayoutParams(0,d(86),1f).apply{
            setMargins(d(3),d(4),d(3),d(4))
        })
    }
    /** Enter the selected world immediately. No mandatory character gallery. */
    private fun showPilotPicker(world:Int,number:Long) {
        currentWorld=world;currentLevel=number
        startGame(world,number)
    }
    /**
     * Custom native premium pause UI, not an image overlay. The simulation
     * remains frozen for the full lifetime of this modal. Cancel resumes;
     * explicit retry/exit/settings transitions never accidentally resume.
     */
    private fun showPremiumPause(game:GameView){
        val dialog=android.app.AlertDialog.Builder(this).create()
        val content=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(18),d(15),d(18),d(19))
            background=gradient(0xff0876db.toInt(),0xff031936.toInt(),26).apply{
                setStroke(d(3),electric)
            }
        }


        content.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[0])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(128)))
        title(content,"♛  PAUZA",32,Color.WHITE)
        small(content,"Pauza · Level ${game.game.displayLevel}")
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
        command("▶  NASTAVI",0){game.paused=false;sound.resume()}
        command("↻  PONOVO",1){startGame(currentWorld,currentLevel)}
        command("↪  IZLAZ",2){showHome()}
        // Editing audio/haptics in a paused flight MUST NOT navigate to
        // showSettings(): that route clears the canvas and abandons the run.
        // These are genuine persisted controls, inside the current pause modal.
        val pauseSettings=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(d(10),d(10),d(10),d(9))
            background=gradient(0xff103e7e.toInt(),0xff04152f.toInt(),19).apply{
                setStroke(d(2),electric)
            }
            visibility=View.GONE
            isFocusable=true
            contentDescription="Postavke u pauzi"
        }
        sectionHeading(pauseSettings,"POSTAVKE U PAUZI")
        volumeSlider(pauseSettings,"♫  Glazba",progress.musicVolume()){
            progress.setMusicVolume(it)
            sound.musicVolume=it/100f
            sound.pause()
        }
        volumeSlider(pauseSettings,"◖  Zvukovi",progress.effectsVolume()){
            progress.setEffectsVolume(it)
            sound.effectsVolume=it/100f
            sound.pause()
        }
        val pauseHaptic=Switch(this).apply{
            text="Vibracije"
            textSize=16f
            setTextColor(Color.WHITE)
            isChecked=progress.hapticEnabled()
            thumbTintList=ColorStateList.valueOf(gold)
            trackTintList=ColorStateList.valueOf(0xff1681df.toInt())
            setPadding(d(12),d(10),d(12),d(10))
            background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
            setOnCheckedChangeListener{_,checked->progress.setHapticEnabled(checked)}
        }
        pauseSettings.addView(pauseHaptic,LinearLayout.LayoutParams(-1,d(56)))
        small(pauseSettings,"Promjene se spremaju odmah. Let ostaje pauziran.")
        val footer=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            background=gradient(0xff052c62.toInt(),0xff041631.toInt(),17)
            setPadding(d(3),d(5),d(3),d(5))
        }
        fun quick(icon:String,label:String,onClick:()->Unit){
            val btn=Button(this).apply{
                val iconState=when(label){
                    "ZVUK" -> if(progress.soundEnabled())"✓" else "✕"
                    "MUZIKA" -> if(progress.musicVolume()>0)"✓" else "✕"
                    else -> ""
                }
                text="$icon\n$label $iconState".trimEnd();isAllCaps=false;textSize=10f
                setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD
                setAutoSizeTextTypeUniformWithConfiguration(9,12,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    gradient(0xff1269b9.toInt(),0xff08234b.toInt(),13),null)
                contentDescription=label
                setOnClickListener{
                    sound.effect("click");onClick()
                    if(label=="ZVUK"||label=="MUZIKA"){
                        val active=if(label=="ZVUK")progress.soundEnabled() else progress.musicVolume()>0
                        text="$icon\n$label " + if(active)"✓" else "✕"
                        contentDescription=label + if(active)" uključeno" else " isključeno"
                    }
                }
            }
            footer.addView(btn,LinearLayout.LayoutParams(0,d(62),1f).apply{
                setMargins(d(2),0,d(2),0)
            })
        }
        quick("◖","ZVUK"){
            val enabled=!progress.soundEnabled()
            progress.setSoundEnabled(enabled);sound.enabled=enabled
            sound.pause() // Toggling audio must never resume a paused flight.
            Toast.makeText(this,if(enabled)"Zvuk uključen" else "Zvuk isključen",
                Toast.LENGTH_SHORT).show()
        }
        quick("♫","MUZIKA"){
            val level=progress.toggleMusicVolume()
            sound.musicVolume=level/100f
            sound.pause() // Changing audio must never resume a paused flight.
            Toast.makeText(this,if(level==0)"Glazba isključena" else "Glazba uključena",
                Toast.LENGTH_SHORT).show()
        }
        quick("✦","KONTROLE"){
            android.app.AlertDialog.Builder(this)
                .setTitle("Kontrole leta")
                .setMessage("Dodirni zaslon za svaki zamah krilima u svih osam svjetova. " +
                    "Pauza je dostupna nakon početka leta; izbjegavaj prepreke i skupljaj nagrade.")
                .setPositiveButton("U REDU",null).show()
        }
        quick("⚙","POSTAVKE"){
            pauseSettings.visibility=if(pauseSettings.visibility==View.VISIBLE)View.GONE else View.VISIBLE
            if(pauseSettings.visibility==View.VISIBLE){
                pauseSettings.requestFocus()
                pauseSettings.post{
                    (content.parent as? ScrollView)?.smoothScrollTo(0,pauseSettings.bottom)
                }
            }
            // Never dismiss the pause dialog or restart simulation here.
            sound.pause()
        }
        content.addView(footer,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(12)})
        content.addView(pauseSettings,LinearLayout.LayoutParams(-1,-2).apply{
            topMargin=d(8)
        })
        val scroll=ScrollView(this).apply{
            isVerticalScrollBarEnabled=false
            addView(content)
        }
        dialog.setView(scroll)
        dialog.setOnCancelListener{game.paused=false;sound.resume()}
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels-d(24)).coerceAtMost(d(420)), -2)
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
        val boosts=progress.previewPerks()
        val simulation=GameSimulation(LevelEngine.createStream(world,number),true,boosts.first,boosts.second,progress.difficulty(),number)
        val frame=FrameLayout(this).apply{setBackgroundColor(0xff092044.toInt())}
        sound.startWorld(world)
        // The idle flight preview is not gameplay. Do not expose pause until
        // the first actual flap; never reuse a pause control across screens.
        var pauseButton:Button?=null
        val game=GameView(this,simulation,progress.lessMotion(),0,progress.hapticEnabled(),
            onFinished={if(selectedScreen=="game" && gameView?.game === it)showResult(it)},
            onLevelCompleted={completed ->
                progress.recordLevelStars(world,completed,simulation.completedLevelStars)
                val amount=progress.completeLevel(world,completed,simulation.score())
                sound.effect("level")
                if(amount>0)Toast.makeText(this,"Level $completed: +$amount kovanica!",Toast.LENGTH_SHORT).show()
            },
            onFlap={sound.effect("tap")},
            onFlightStarted={
                if(selectedScreen=="game" && gameView?.game === simulation) {
                    // Charge exactly once when the player actually starts flying.
                    progress.consumePerks()
                    pauseButton?.visibility=View.VISIBLE
                }
            },
            onCollect={sound.effect("collect")},
            onShieldImpact={sound.effect("hit")},
            qaCaptureFirstFlap=intent?.getBooleanExtra("bopavi_qa_capture_first_flap",false)==true,
            walletCoins={progress.coins()})
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
        val headline=ResultHeadline.label(g.score(),previousRecord,g.won)
        progress.recordRun(g)
        currentLevel=g.displayLevel
        menuBackAction=null;gameView?.paused=true;gameView=null;gamePauseButton=null;selectedScreen="result"
        @Suppress("DEPRECATION")
        applyPremiumSystemBars()
        val root=FrameLayout(this).apply{setBackgroundColor(0xff1d89cb.toInt())}
        val scenes=intArrayOf(R.drawable.world0,R.drawable.world1,R.drawable.world2,R.drawable.world3,
            R.drawable.world4,R.drawable.world5,R.drawable.world6,R.drawable.world7)
        val backdrop=ImageView(this).apply{
            setImageResource(scenes[currentWorld]);scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        root.addView(backdrop,FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x9903183a.toInt(),0x660a3464,0xcc061c42.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        val scroll=ScrollView(this).apply{
            isFillViewport=true;isVerticalScrollBarEnabled=false
            clipToPadding=false
        }
        val panel=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(18),d(12),d(18),d(18))
        }
        scroll.addView(panel)
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
        // Android 16 enforces edge-to-edge: keep result actions out of the
        // gesture-navigation/status-bar regions, while art remains full bleed.
        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener { view, insets ->
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars())
                view.setPadding(bars.left,bars.top,bars.right,bars.bottom)
                insets
            }
        }
        // The result hero renders the ACTUAL selected pilot above the played
        // world. Separate live image layers replace the generic static banner.
        val pilotIndex=0
        val pilotPortraits=intArrayOf(
            R.drawable.bopiportrait0,R.drawable.bopiportrait1,R.drawable.bopiportrait2,
            R.drawable.bopiportrait3,R.drawable.bopiportrait4,R.drawable.bopiportrait5,
            R.drawable.bopiportrait6,R.drawable.bopiportrait7,R.drawable.bopiportrait8
        )
        val artwork=FrameLayout(this).apply{
            background=gradient(0xff218fe3.toInt(),0xff08346b.toInt(),24)
            clipToOutline=true
            contentDescription="Odabrani letač: ${progress.skinNames[pilotIndex]}, " +
                "svijet ${LevelEngine.names[currentWorld]}"
        }
        artwork.addView(ImageView(this).apply{
            setImageResource(scenes[currentWorld])
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        artwork.addView(View(this).apply {
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x003da8ee,0xa4032355.toInt()))
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        artwork.addView(ImageView(this).apply{
            setImageResource(pilotPortraits[pilotIndex])
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,d(186),Gravity.CENTER))
        panel.addView(artwork,LinearLayout.LayoutParams(-1,d(144)).apply{bottomMargin=d(5)})
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
        resultCard("PRIKUPLJENE\nKOVANICE","●",g.coins.toString(),0xffb8edff.toInt())
        resultCard("UKUPNO\nPROLAZA","⚑",g.totalPassed.toString(),Color.WHITE)
        // Compact accessible details, rather than three large padded cards
        // pushing Retry and result navigation below the viewport.
        panel.addView(TextView(this).apply{
            text="Najbolji rezultat: ${progress.bestPoints()}\n" +
                "Level $currentLevel · Ukupno prolaza: ${g.totalPassed}\n" +
                "Težina: ${progress.difficultyNames[g.difficulty]} · ${progress.playerName()}"
            textSize=14f;setTextColor(Color.WHITE)
            typeface=Typeface.create("sans-serif-medium",Typeface.BOLD)
            gravity=Gravity.CENTER;setLineSpacing(d(3).toFloat(),1f)
            setPadding(d(3),d(6),d(3),d(5))
        },LinearLayout.LayoutParams(-1,-2))
        val balance=TextView(this).apply{
            text="Stanje novčanika: ${progress.coins()} kovanica"
            textSize=15f;gravity=Gravity.CENTER;setTextColor(0xffc6eeff.toInt())
            contentDescription=text
        }
        panel.addView(balance,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(4)})
        sectionHeading(panel,"♛  NAGRADE")
        // No fake diamonds, chests, paid bonuses or unlocked character claims.
        // Every icon has a verifiable offline state behind its number.
        val rewardPanel=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            setPadding(d(3),d(7),d(3),d(7))
            background=gradient(0xff073b7f.toInt(),0xff031a3c.toInt(),23).apply{
                setStroke(d(2),0xff3eceff.toInt())
            }
        }
        panel.addView(rewardPanel,LinearLayout.LayoutParams(-1,-2))
        fun reward(label:String,icon:String,value:String):TextView{
            val box=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
                setPadding(d(2),d(7),d(2),d(7))
                background=gradient(0xff0b4f9b.toInt(),0xff031937.toInt(),15).apply{
                    setStroke(d(1),0xff408bca.toInt())
                }
            }
            box.addView(TextView(this).apply{
                text=icon;gravity=Gravity.CENTER;textSize=25f
                setTextColor(gold) // Never render black reward glyphs on navy.
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(35)))
            box.addView(TextView(this).apply{
                text=label;textSize=10f;gravity=Gravity.CENTER
                typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE)
                setAutoSizeTextTypeUniformWithConfiguration(9,11,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(27)))
            val count=TextView(this).apply{
                text=value;textSize=17f;gravity=Gravity.CENTER
                setTextColor(0xffbceeff.toInt())
                typeface=Typeface.DEFAULT_BOLD
                setAutoSizeTextTypeUniformWithConfiguration(12,17,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                contentDescription="$label $value"
            }
            box.addView(count,LinearLayout.LayoutParams(-1,d(28)))
            rewardPanel.addView(box,LinearLayout.LayoutParams(0,d(99),1f).apply{
                setMargins(d(2),0,d(2),0)
            })
            return count
        }
        reward("KOVANICE","●","×${g.coins}")
        reward("ZVIJEZDE","★","×${g.stars}")
        val bonus=progress.bonusCoinsAvailable()
        val bonusCounter=reward("BONUS\nBODOVA","♛","+$bonus")
        reward("REKORD","🏆",progress.bestPoints().toString())
        if(bonus>0){
            action(panel,"🎁  PREUZMI $bonus KOVANICA ZA REKORD",false){
                val awarded=progress.claimBonusCoins()
                if(awarded>0){
                    sound.effect("purchase")
                    bonusCounter.text="+0"
                    bonusCounter.contentDescription="BONUS BODOVA 0"
                    balance.text="Stanje novčanika: ${progress.coins()} kovanica"
                    balance.contentDescription=balance.text
                    Toast.makeText(this,"Preuzeto $awarded kovanica.",Toast.LENGTH_SHORT).show()
                }
            }
        }
        action(panel,"▶  PONOVO"){showPilotPicker(currentWorld,currentLevel)}
        // Compact footer follows the reference, while all three actions work.
        val footer=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        panel.addView(footer,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(6)})
        fun footerAction(caption:String,callback:()->Unit){
            val button=Button(this).apply{
                text=caption;isAllCaps=false;textSize=13f
                typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE)
                setAutoSizeTextTypeUniformWithConfiguration(10,14,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                background=RippleDrawable(ColorStateList.valueOf(0x44ffffff),
                    gradient(0xff367fd0.toInt(),0xff112e64.toInt(),17).apply{
                        setStroke(d(2),0xff92d8ff.toInt())
                    },null)
                contentDescription=caption
                setOnClickListener{sound.effect("click");callback()}
            }
            footer.addView(button,LinearLayout.LayoutParams(0,d(57),1f).apply{
                setMargins(d(3),d(4),d(3),d(4))
            })
        }
        footerAction("▥\nLJESTVICA"){showLeaderboard()}
        footerAction("⌂\nPOVRATAK"){showHome()}
        footerAction("↗\nPODIJELI"){
            val message="BOPAVI · Rezultat ${g.score()} · Ukupno prolaza: ${g.totalPassed}"
            val share=android.content.Intent(android.content.Intent.ACTION_SEND).apply{
                type="text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT,message)
            }
            startActivity(android.content.Intent.createChooser(share,"Podijeli rezultat"))
        }
        action(panel,"MAPA SVJETOVA",false){showWorlds()}
        action(panel,"🛍  TRGOVINA KOVANICAMA",false){showPerks(MenuOrigin.HOME)}
        showNativeView(root)
    }
    private fun showPerks(){showPerks(MenuOrigin.SETTINGS)}
    private fun showPerks(origin:MenuOrigin){
        val b=base("TRGOVINA","")
        title(b,"♛  ${progress.coins()} KOVANICA",24,gold)
        small(b,"Za kovanice osvojene igrom — bez stvarnog novca.")
        small(b,"Za svakih novih 1.000 bodova najboljeg rezultata dobivaš 1 kovanicu.")
        val bonus=progress.bonusCoinsAvailable()
        if(bonus>0)action(b,"🎁  PREUZMI $bonus KOVANICA ZA BODOVE",false){
            val earned=progress.claimBonusCoins()
            if(earned>0){sound.effect("purchase");showPerks(origin)}
        }

        small(b,"Osvajaj kovanice prelaskom nagradnih levela i biraj opremu za svoj sljedeći let.")
        sectionHeading(b,"OPREMA ZA BOPIJEV LET")
        // Two compact, symmetrical native cards on one screen.
        val cardRow=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        for(n in 0..1){
            val name=if(n==0)"ŠTIT" else "MAGNET"
            val explanation=if(n==0)"Upija jedan stvarni sudar." else "Privlači predmete tijekom 8 sekundi."
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER_HORIZONTAL
                setPadding(d(8),d(10),d(8),d(9))
                background=gradient(0xff0b4587.toInt(),0xff041a3c.toInt(),20).apply{
                    setStroke(d(2),if(n==0)0xffffd56a.toInt() else electric)
                }
            }
            card.addView(TextView(this).apply{
                text=if(n==0)"🛡" else "🧲"
                gravity=Gravity.CENTER;textSize=34f
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(57)))
            card.addView(TextView(this).apply{
                text=name;gravity=Gravity.CENTER
                textSize=19f;typeface=Typeface.DEFAULT_BOLD
                setTextColor(if(n==0)gold else 0xffbcefff.toInt())
                contentDescription="$name, cijena ${progress.perkPrices[n]} kovanica"
            },LinearLayout.LayoutParams(-1,d(31)))
            card.addView(TextView(this).apply{
                text="$explanation\nU torbi: ${progress.perkCount(n)} · ${progress.perkPrices[n]} ♛"
                textSize=12f;setTextColor(Color.WHITE)
                gravity=Gravity.CENTER;maxLines=3
                setPadding(0,d(4),0,d(4))
            })
            action(card,"KUPI $name",false){
                if(progress.buyPerk(n)){sound.effect("purchase");showPerks(origin)}
                else Toast.makeText(this,"Nedovoljno kovanica ili je zaliha puna.",Toast.LENGTH_SHORT).show()
            }
            cardRow.addView(card,LinearLayout.LayoutParams(0,-2,1f).apply{
                setMargins(d(3),d(3),d(3),d(3))
            })
        }
        b.addView(cardRow,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=d(6)})
        small(b,"Kupljena oprema automatski se koristi na početku sljedećeg leta.")
        small(b,"Bopi je jedini pilot. Kovanice služe isključivo za stvarnu opremu u igri.")
        b.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        back(b){if(origin==MenuOrigin.HOME)showHome() else showSettings()}
    }
    /** The offline PROFILE is built from actual saved pilot data and game art. */
    private fun showProfile(){
        val b=base("PROFIL","Tvoj letački dnevnik i osobni rezultati")
        val topBack=TextView(this).apply{
            text="‹  POČETNA";textSize=13f;setTextColor(Color.WHITE)
            typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER
            background=gradient(0xff1368b6.toInt(),0xff072657.toInt(),14)
            isClickable=true;isFocusable=true
            contentDescription="Natrag na početnu"
            setOnClickListener{showHome()}
        }
        b.addView(topBack,LinearLayout.LayoutParams(-1,d(33)))
        val skin=0


        val hero=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
            setPadding(d(7),d(6),d(7),d(6))
            background=gradient(0xf40b4189.toInt(),0xf3051b40.toInt(),24).apply{
                setStroke(d(2),electric)
            }
            contentDescription="Letački profil, "+progress.playerName()+", lik "+progress.skinNames[skin]
        }
        val portraitBox=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
            background=gradient(0xff126bd1.toInt(),0xff041d48.toInt(),20).apply{
                setStroke(d(2),gold)
            }
        }
        portraitBox.addView(ImageView(this).apply{
            setImageResource(portraitDrawableIds[skin]);scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(111)))
        portraitBox.addView(TextView(this).apply{
            text="LEVEL "+progress.streamFrontier(progress.chosenWorld())
            textSize=11f;setTextColor(gold);gravity=Gravity.CENTER
            typeface=Typeface.DEFAULT_BOLD
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(20)))
        // A real saved-world star gauge, not fabricated XP or a distance bar.
        val profileWorld=progress.chosenWorld()
        val profileStars=progress.worldStars(profileWorld).coerceIn(0,30)
        val profileStarGauge=android.widget.ProgressBar(this,null,
            android.R.attr.progressBarStyleHorizontal).apply{
            max=30;progress=profileStars
            progressTintList=ColorStateList.valueOf(gold)
            progressBackgroundTintList=ColorStateList.valueOf(0xff103d7d.toInt())
            contentDescription="Zvjezdice svijeta ${LevelEngine.names[profileWorld]}: ${profileStars} od 30"
        }
        portraitBox.addView(profileStarGauge,
            LinearLayout.LayoutParams(-1,d(9)).apply{
                leftMargin=d(9);rightMargin=d(9);topMargin=d(4)
            })
        portraitBox.addView(TextView(this).apply{
            text="★  ${profileStars}/30 · ${LevelEngine.names[profileWorld]}"
            gravity=Gravity.CENTER;textSize=10f;setTextColor(Color.WHITE)
            setSingleLine(true)
            setAutoSizeTextTypeUniformWithConfiguration(8,10,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },LinearLayout.LayoutParams(-1,d(20)))
        hero.addView(portraitBox,LinearLayout.LayoutParams(0,-2,0.42f).apply{rightMargin=d(7)})
        val stats=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        stats.addView(TextView(this).apply{
            text=progress.playerName().uppercase()
            textSize=20f;setTextColor(Color.WHITE)
            typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
            setSingleLine(true)
            setAutoSizeTextTypeUniformWithConfiguration(11,20,1,
                android.util.TypedValue.COMPLEX_UNIT_SP)
        },LinearLayout.LayoutParams(-1,d(24)))
        stats.addView(TextView(this).apply{
            text=progress.skinNames[skin]+" · pilot"
            textSize=12f;setTextColor(0xffc5e9ff.toInt())
        },LinearLayout.LayoutParams(-1,d(19)))
        stats.addView(TextView(this).apply{
            text="TVOJI REZULTATI";textSize=11f;setTextColor(gold)
            typeface=Typeface.DEFAULT_BOLD
        },LinearLayout.LayoutParams(-1,d(17)))
        val completed=(0..7).sumOf{(progress.frontier(it)-1).toLong()}
        val rows=listOf(
            "★  Najbolji rezultat" to progress.bestPoints().toString(),
            "●  Osvojene kovanice" to progress.coins().toString(),
            "♛  Dovršeni leveli" to completed.toString(),
            "⚑  Pobjede" to progress.wins().toString())
        for((caption,value) in rows){
            val tile=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
                setPadding(d(5),d(5),d(5),d(5))
                background=gradient(0xff12549a.toInt(),0xff071e45.toInt(),13)
                contentDescription=caption+": "+value
            }
            tile.addView(TextView(this).apply{
                text=caption;setTextColor(Color.WHITE);textSize=11f
                setSingleLine(true)
                setAutoSizeTextTypeUniformWithConfiguration(8,11,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(0,-2,1f))
            tile.addView(TextView(this).apply{
                text=value;setTextColor(gold);textSize=14f;typeface=Typeface.DEFAULT_BOLD
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-2,-2).apply{leftMargin=d(3)})
            stats.addView(tile,LinearLayout.LayoutParams(-1,d(28)).apply{bottomMargin=d(2)})
        }
        hero.addView(stats,LinearLayout.LayoutParams(0,-2,0.58f))
        b.addView(hero,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(6)})
        // World name is visible in the real star-gauge caption.
        profileHeading(b,"MOJE NAGRADE")
        // Unlock badges from already-persisted achievements; never grant
        // phantom currency, XP, cloud rewards or fabricated milestones.
        val explored=(0..7).count{progress.frontier(it)>1}
        val collected=(0..7).sumOf{progress.collectibles(it).toLong()}
        val awards=listOf(
            Triple("✦","PRVI LET",progress.wins()>0),
            Triple("🌍","ISTRAŽIVAČ",explored>=5),
            Triple("♛","SAKUPLJAČ",collected>=500L),
            Triple("⚑","VETERAN",progress.wins()>=10))
        val badgeRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        for((icon,name,earned) in awards){
            val requirement=when(name){
                "PRVI LET" -> "1 pobjeda"
                "ISTRAŽIVAČ" -> "5 svjetova"
                "SAKUPLJAČ" -> "500 predmeta"
                else -> "10 pobjeda"
            }
            val tile=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
                setPadding(d(2),d(3),d(2),d(3))
                background=gradient(if(earned)0xff1355a5.toInt() else 0xff283852.toInt(),
                    0xff061d44.toInt(),15).apply{
                    setStroke(d(2),if(earned)gold else 0xff7183a6.toInt())
                }
                contentDescription=name+", cilj "+requirement+", "+if(earned)"osvojeno" else "zaključano"
            }
            tile.addView(TextView(this).apply{
                text=if(earned)icon else "🔒";textSize=24f;gravity=Gravity.CENTER
                setTextColor(gold);importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(25)))
            tile.addView(TextView(this).apply{
                text=name;textSize=10f;setTextColor(Color.WHITE);gravity=Gravity.CENTER
                setSingleLine(true);typeface=Typeface.DEFAULT_BOLD
                setAutoSizeTextTypeUniformWithConfiguration(8,10,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(19)))
            tile.addView(TextView(this).apply{
                text=requirement;textSize=9f;gravity=Gravity.CENTER
                setTextColor(0xffc5e9ff.toInt());setSingleLine(true)
                setAutoSizeTextTypeUniformWithConfiguration(8,9,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(18)))
            badgeRow.addView(tile,LinearLayout.LayoutParams(0,d(75),1f).apply{
                setMargins(d(2),0,d(2),0)
            })
        }
        b.addView(badgeRow,LinearLayout.LayoutParams(-1,-2))
        b.addView(TextView(this).apply{
            text="★  SVE NAGRADE  ›";textSize=13f;gravity=Gravity.CENTER
            setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD
            background=gradient(0xff156ac5.toInt(),0xff062d6d.toInt(),18)
            isClickable=true;isFocusable=true;contentDescription="Pogledaj sva postignuća"
            setOnClickListener{showAchievements(MenuOrigin.PROFILE)}
        },LinearLayout.LayoutParams(-1,d(37)))
        profileHeading(b,"MOJI NAJBOLJI SVJETOVI")
        val ranked=(0..7).sortedWith(compareByDescending<Int>{progress.worldStars(it)}
            .thenByDescending{progress.best(it)}.thenBy{it}).take(3)
        val worldArt=intArrayOf(R.drawable.world0,R.drawable.world1,R.drawable.world2,
            R.drawable.world3,R.drawable.world4,R.drawable.world5,R.drawable.world6,R.drawable.world7)
        val gallery=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        for((rank,world) in ranked.withIndex()){
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;isClickable=true;isFocusable=true
                background=gradient(0xff0d5aa4.toInt(),0xff061d40.toInt(),15).apply{
                    setStroke(d(2),electric)
                }
                contentDescription=LevelEngine.names[world]+", "+
                    progress.worldStars(world)+" od 30 zvjezdica; otvori levele"
                setOnClickListener{sound.effect("click");showLevels(world,1)}
            }
            val artFrame=FrameLayout(this)
            artFrame.addView(ImageView(this).apply{
                setImageResource(worldArt[world]);scaleType=ImageView.ScaleType.CENTER_CROP
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },FrameLayout.LayoutParams(-1,-1))
            artFrame.addView(TextView(this).apply{
                text="♛ ${rank+1}";textSize=12f;setTextColor(gold)
                gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD
                background=gradient(0xff123f88.toInt(),0xff071d44.toInt(),15).apply{
                    setStroke(d(1),gold)
                }
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },FrameLayout.LayoutParams(d(46),d(28),Gravity.TOP or Gravity.LEFT).apply{
                leftMargin=d(5);topMargin=d(5)
            })
            card.addView(artFrame,LinearLayout.LayoutParams(-1,d(59)))
            card.addView(TextView(this).apply{
                text=LevelEngine.names[world].uppercase();setTextColor(Color.WHITE)
                textSize=11f;gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD
                setSingleLine(true)
                setAutoSizeTextTypeUniformWithConfiguration(8,11,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(22)))
            card.addView(TextView(this).apply{
                text="★ "+progress.worldStars(world)+"/30"
                textSize=12f;setTextColor(gold);gravity=Gravity.CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(25)))
            gallery.addView(card,LinearLayout.LayoutParams(0,-2,1f).apply{
                setMargins(d(3),0,d(3),0)
            })
        }
        b.addView(gallery,LinearLayout.LayoutParams(-1,-2))
        action(b,"✎  UREDI PROFIL",true){showSettings()}
        // One permanent pilot Bopi already appears in accessible profile hero.
        // The same four native navigation actions as on the home screen.
        val footer=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            setPadding(d(4),d(6),d(4),d(6))
            background=gradient(0xf3062a64.toInt(),0xfa041736.toInt(),23).apply{
                setStroke(d(2),electric)
            }
        }
        val navNames=listOf("PROFIL","ZADACI","REKORDI","TRGOVINA")
        val navIcons=intArrayOf(R.drawable.nav_profile,R.drawable.nav_tasks,
            R.drawable.nav_collection,R.drawable.nav_shop)
        for(index in 0..3){
            val tab=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
                isClickable=true;isFocusable=true
                background=gradient(if(index==0)0xff9b6311.toInt() else 0xff12549c.toInt(),
                    0xff081e49.toInt(),14)
                contentDescription=navNames[index]+if(index==0)", odabrano" else ""
                addView(ImageView(this@MainActivity).apply{
                    setImageResource(navIcons[index]);scaleType=ImageView.ScaleType.FIT_CENTER
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(28)))
                addView(TextView(this@MainActivity).apply{
                    text=navNames[index];textSize=11f
                    gravity=Gravity.CENTER;setTextColor(Color.WHITE)
                    typeface=Typeface.DEFAULT_BOLD;setSingleLine(true)
                    setAutoSizeTextTypeUniformWithConfiguration(8,11,1,
                        android.util.TypedValue.COMPLEX_UNIT_SP)
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(18)))
                setOnClickListener{
                    sound.effect("click")
                    when(index){
                        0->showProfile()
                        1->showAchievements(MenuOrigin.PROFILE)
                        2->showLeaderboard()
                        else->showPerks(MenuOrigin.PROFILE)
                    }
                }
            }
            footer.addView(tab,LinearLayout.LayoutParams(0,d(50),1f).apply{
                setMargins(d(2),0,d(2),0)
            })
        }
        b.addView(footer,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(5)})
        menuBackAction={showHome()}
        // Hardware and gesture Back leave Profile; no duplicate bottom action.
    }

    private fun showLeaderboard(){
        showLeaderboardPage(0,selectedScreen=="POSTAVKE" || selectedScreen=="settings")
    }
    private fun showLeaderboardPage(page:Int,fromSettings:Boolean){
        recordsReturnToSettings=fromSettings
        val b=base("MOJI REKORDI","Tvojih deset najboljih letova")
        val entries=progress.leaderboard()
        val best=entries.firstOrNull()
        if(best!=null) {
            val banner=LinearLayout(this).apply {
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
                setPadding(d(16),d(16),d(16),d(17))
                background=gradient(0xff0b65bf.toInt(),0xff061c46.toInt(),24).apply{
                    setStroke(d(3),gold)
                }
                contentDescription="Osobni rekord: ${best.score} bodova"
            }
            title(banner,"♛  OSOBNI REKORD",18,gold)
            title(banner,"${best.score} BODOVA",29,Color.WHITE)
            b.addView(banner,LinearLayout.LayoutParams(-1,-2).apply{
                topMargin=d(12);bottomMargin=d(9)
            })
        } else small(b,"Prvi rekord čeka tvoj sljedeći let!")
        val perPage=2
        val pageCount=maxOf(1,(entries.size+perPage-1)/perPage)
        val current=page.coerceIn(0,pageCount-1)
        for((index,item) in entries.withIndex().drop(current*perPage).take(perPage)) {
            val highlighted=index==0
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
                setPadding(d(12),d(13),d(12),d(13))
                background=gradient(if(highlighted)0xff1459a2.toInt() else 0xff0a3672.toInt(),
                    0xff061a3b.toInt(),18).apply{
                    setStroke(d(if(highlighted)2 else 1),if(highlighted)gold else electric)
                }
                contentDescription="Let ${index+1}, ${item.score} bodova, ${LevelEngine.names[item.world]}, ${item.gates} prolaza"
            }
            val medal=TextView(this).apply{
                text=if(highlighted)"♛" else "${index+1}"
                textSize=if(highlighted)29f else 22f
                gravity=Gravity.CENTER;setTextColor(if(highlighted)gold else Color.WHITE)
                typeface=Typeface.DEFAULT_BOLD
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            card.addView(medal,LinearLayout.LayoutParams(d(52),d(52)))
            val info=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
            info.addView(TextView(this).apply{
                text="${item.score} BODOVA";textSize=20f;setTextColor(Color.WHITE)
                typeface=Typeface.DEFAULT_BOLD
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            })
            info.addView(TextView(this).apply{
                text="${LevelEngine.names[item.world]} · ${progress.difficultyNames[item.difficulty]}"
                textSize=12f;setTextColor(0xffd4eaff.toInt())
                setSingleLine(true)
                setAutoSizeTextTypeUniformWithConfiguration(9,12,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            })
            card.addView(info,LinearLayout.LayoutParams(0,-2,1f))
            card.addView(TextView(this).apply {
                text="⚑ ${item.gates}"
                textSize=13f;setTextColor(gold);typeface=Typeface.DEFAULT_BOLD
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-2,-2).apply{leftMargin=d(8)})
            b.addView(card,LinearLayout.LayoutParams(-1,d(75)).apply{bottomMargin=d(7)})
        }
        if(pageCount>1){
            val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            fun pageButton(label:String,enabled:Boolean,next:Int){
                val item=Button(this).apply{
                    text=label;isAllCaps=false;textSize=12f
                    setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD
                    background=gradient(if(enabled)0xff117bcf.toInt() else 0xff29456b.toInt(),
                        0xff05264e.toInt(),16).apply{setStroke(d(1),electric)}
                    isEnabled=enabled
                    setOnClickListener{showLeaderboardPage(next,fromSettings)}
                }
                nav.addView(item,LinearLayout.LayoutParams(0,d(49),1f).apply{
                    leftMargin=d(2);rightMargin=d(2)
                })
            }
            pageButton("‹ PRETHODNI",current>0,current-1)
            pageButton((current+1).toString()+"/"+pageCount.toString(),false,current)
            pageButton("SLJEDEĆI ›",current<pageCount-1,current+1)
            b.addView(nav,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=d(5)})
        }
        b.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        action(b,"★  MOJA POSTIGNUĆA",false){showAchievements(MenuOrigin.RECORDS)}
        back(b){if(fromSettings)showSettings() else showHome()}
    }
    private fun showAchievements(){showAchievements(MenuOrigin.SETTINGS)}

    private fun showAchievements(origin:MenuOrigin){
        val b=base("POSTIGNUĆA","Tvoj napredak kroz svih osam svjetova")
        val completed=(0..7).sumOf { (progress.frontier(it)-1).coerceAtLeast(0).toLong() }
        val summary=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER
            setPadding(d(8),d(6),d(8),d(6))
            background=gradient(0xff0a4e9d.toInt(),0xff051b43.toInt(),18).apply{
                setStroke(d(2),gold)
            }
            contentDescription="Dovršeni leveli: $completed, Pobjede: ${progress.wins()}, " +
                "Pokušaji bez pobjede: ${progress.deaths()}"
        }
        for((name,value) in listOf(
            "LEVELI" to "$completed",
            "POBJEDE" to "${progress.wins()}",
            "LETOVI" to "${progress.wins()+progress.deaths()}"
        )){
            summary.addView(TextView(this).apply{
                text="$name\n$value";textSize=15f
                typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER
                setTextColor(Color.WHITE)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(0,d(50),1f))
        }
        b.addView(summary,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(6);bottomMargin=d(7)})
        profileHeading(b,"NAPREDAK PO SVJETOVIMA")
        for(row in 0..3){
            val rowLayout=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
            }
            for(col in 0..1){
                val w=row*2+col
                val stars=progress.worldStars(w)
                val level=progress.streamFrontier(w)
                val record=progress.best(w)
                val card=LinearLayout(this).apply{
                    orientation=LinearLayout.VERTICAL
                    gravity=Gravity.CENTER_VERTICAL
                    setPadding(d(9),d(7),d(8),d(6))
                    background=gradient(
                        if(w==progress.chosenWorld())0xff175ea1.toInt() else 0xff0b3976.toInt(),
                        0xff03172e.toInt(),17).apply{
                        setStroke(d(2),if(w==progress.chosenWorld())gold else worldAccents[w])
                    }
                    isClickable=true;isFocusable=true
                    contentDescription="${LevelEngine.names[w]}, " +
                        "zvjezdice $stars od 30, level $level, rekord $record. Otvori levele"
                    setOnClickListener{sound.effect("click");showLevels(w,1)}
                }
                card.addView(TextView(this).apply{
                    text=LevelEngine.names[w].uppercase()
                    textSize=12f;typeface=Typeface.DEFAULT_BOLD
                    setTextColor(Color.WHITE);setSingleLine(true)
                    setAutoSizeTextTypeUniformWithConfiguration(9,12,1,
                        android.util.TypedValue.COMPLEX_UNIT_SP)
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(22)))
                card.addView(TextView(this).apply{
                    text="★ $stars/30  ·  ♛ $record"
                    setTextColor(gold);textSize=12f
                    typeface=Typeface.DEFAULT_BOLD
                    setSingleLine(true)
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(23)))
                rowLayout.addView(card,LinearLayout.LayoutParams(0,d(67),1f).apply{
                    leftMargin=d(3);rightMargin=d(3)
                })
            }
            b.addView(rowLayout,LinearLayout.LayoutParams(-1,d(72)))
        }
        b.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        back(b){
            when(origin){
                MenuOrigin.HOME -> showHome()
                MenuOrigin.PROFILE -> showProfile()
                MenuOrigin.RECORDS -> showLeaderboardPage(0,recordsReturnToSettings)
                else -> showSettings()
            }
        }
    }
    private fun volumeSlider(parent:LinearLayout,label:String,value:Int,onValue:(Int)->Unit){
        val panel=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(d(15),d(9),d(15),d(10))
            background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
        }
        val heading=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        val name=TextView(this).apply{
            text=label;setTextColor(Color.WHITE);textSize=16f
            typeface=Typeface.create("sans-serif-medium",Typeface.BOLD)
        }
        val percent=TextView(this).apply{
            text="$value%";setTextColor(gold);textSize=16f
            typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.RIGHT
        }
        heading.addView(name,LinearLayout.LayoutParams(0,-2,1f))
        heading.addView(percent,LinearLayout.LayoutParams(-2,-2))
        panel.addView(heading)
        val slider=android.widget.SeekBar(this).apply{
            max=100;progress=value.coerceIn(0,100)
            progressTintList=ColorStateList.valueOf(0xff10baf4.toInt())
            progressBackgroundTintList=ColorStateList.valueOf(0xff174477.toInt())
            thumbTintList=ColorStateList.valueOf(gold)
            contentDescription=label
            setOnSeekBarChangeListener(object:android.widget.SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(bar:android.widget.SeekBar,p:Int,fromUser:Boolean){
                    percent.text="$p%"
                    if(fromUser)onValue(p)
                }
                override fun onStartTrackingTouch(bar:android.widget.SeekBar){}
                override fun onStopTrackingTouch(bar:android.widget.SeekBar){}
            })
        }
        panel.addView(slider,LinearLayout.LayoutParams(-1,d(43)))
        parent.addView(panel,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=d(7)})
    }

    /** The compact seven-row Settings page uses real controls, never a mockup. */
    private fun showSettings(){
        @Suppress("DEPRECATION")
        applyPremiumSystemBars()
        menuBackAction={showHome()}
        gameView?.paused=true;gameView=null;gamePauseButton=null
        sound.stop();selectedScreen="settings"
        val root=FrameLayout(this).apply{setBackgroundColor(0xff188acb.toInt())}
        root.addView(ImageView(this).apply{
            setImageResource(R.drawable.world0)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x66092b65,0x66083770,0xbb04152e.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        val viewport=FitViewportColumn(this)
        val column=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(16),d(8),d(16),d(12))
        }
        viewport.addView(column)
        root.addView(viewport,FrameLayout.LayoutParams(-1,-1))
        val header=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
            setPadding(d(8),d(5),d(9),d(5))
            background=gradient(0xf40a3268.toInt(),0xf8051737.toInt(),21)
        }
        val backButton=TextView(this).apply{
            text="‹";textSize=32f;gravity=Gravity.CENTER
            setTextColor(Color.WHITE)
            background=gradient(0xff22b8fc.toInt(),0xff0759ca.toInt(),22).apply{
                setStroke(d(2),Color.WHITE)
            }
            contentDescription="Natrag na početnu"
            isClickable=true;isFocusable=true
            setOnClickListener{showHome()}
        }
        header.addView(backButton,LinearLayout.LayoutParams(d(46),d(46)))
        header.addView(TextView(this).apply{
            text="POSTAVKE";textSize=23f
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setTextColor(Color.WHITE);gravity=Gravity.CENTER
        },LinearLayout.LayoutParams(0,d(46),1f))
        column.addView(header,LinearLayout.LayoutParams(-1,d(61)))
        column.addView(TextView(this).apply{
            text="IZGLED I ZVUK";textSize=12f
            setTextColor(0xffe4f9ff.toInt())
            typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER
        },LinearLayout.LayoutParams(-1,d(29)))
        fun option(label:String,read:()->String,activate:()->Unit){
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
                setPadding(d(13),d(5),d(9),d(5))
                background=gradient(0xf3072e68.toInt(),0xf6031638.toInt(),18).apply{
                    setStroke(d(1),electric)
                }
                isClickable=true;isFocusable=true
            }
            row.addView(TextView(this).apply{
                text=label;textSize=14f;typeface=Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE);setSingleLine(true)
                setAutoSizeTextTypeUniformWithConfiguration(10,14,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
            },LinearLayout.LayoutParams(0,-2,1f))
            val status=TextView(this).apply{
                text=read();textSize=12f;typeface=Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE);gravity=Gravity.CENTER
                background=gradient(0xff0ba9ed.toInt(),0xff045ba8.toInt(),13).apply{
                    setStroke(d(1),0xffb0f3ff.toInt())
                }
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            row.addView(status,LinearLayout.LayoutParams(d(99),d(32)))
            row.contentDescription=label+", "+read()
            row.setOnClickListener{
                activate();status.text=read()
                row.contentDescription=label+", "+read()
            }
            column.addView(row,LinearLayout.LayoutParams(-1,d(53)).apply{
                bottomMargin=d(6)
            })
            // During exact-height measurement FitViewportColumn distributes
            // spare height between REAL controls. Compact phones keep the
            // intrinsic dimensions instead of clipping or forcing scrolling.
            column.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        }
        option("ZVUK",{if(progress.soundEnabled())"UKLJ." else "ISKL."}){
            val enabled=!progress.soundEnabled()
            progress.setSoundEnabled(enabled);sound.enabled=enabled
        }
        option("GLAZBA",{progress.musicVolume().toString()+"%"}){showSettingsDetails()}
        option("VIBRACIJA",{if(progress.hapticEnabled())"UKLJ." else "ISKL."}){
            progress.setHapticEnabled(!progress.hapticEnabled())
        }
        option("EFEKTI",{progress.effectsVolume().toString()+"%"}){showSettingsDetails()}
        option("MANJE ANIMACIJA",{if(progress.lessMotion())"UKLJ." else "ISKL."}){
            progress.setLessMotion(!progress.lessMotion())
        }
        option("JEZIK",{"HRVATSKI"}){
            Toast.makeText(this,"Trenutačno je dostupan hrvatski jezik.",Toast.LENGTH_SHORT).show()
        }
        option("PRIVATNOST",{"OTVORI"}){showSettingsDetails()}
        val advanced=TextView(this).apply{
            text="⚙  NAPREDNE POSTAVKE";textSize=16f
            setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD
            gravity=Gravity.CENTER
            background=gradient(0xff24a6ed.toInt(),0xff0754b8.toInt(),21).apply{
                setStroke(d(2),electric)
            }
            contentDescription="NAPREDNE POSTAVKE"
            isClickable=true;isFocusable=true
            setOnClickListener{showSettingsDetails()}
        }
        column.addView(advanced,LinearLayout.LayoutParams(-1,d(52)).apply{
            topMargin=d(5);bottomMargin=d(4)
        })
        root.setOnApplyWindowInsetsListener{_,insets->
            if(Build.VERSION.SDK_INT>=30){
                val safe=insets.getInsets(android.view.WindowInsets.Type.systemBars()
                    or android.view.WindowInsets.Type.displayCutout())
                viewport.setPadding(safe.left+d(8),safe.top+d(8),
                    safe.right+d(8),safe.bottom+d(8))
            }
            insets
        }
        showNativeView(root)
    }

    private fun showSettingsDetails(){
        val b=base("POSTAVKE","Prilagodi igru svom stilu")
        // The reference's tall royal-blue sheet is a *real* scrolling layout,
        // not a screenshot or image with fake slider/switch controls.
        val settingsPanel=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(d(13),d(12),d(13),d(18))
            background=gradient(0xf706326e.toInt(),0xfc041631.toInt(),27).apply{
                setStroke(d(2),0xff42cfff.toInt())
            }
            elevation=d(11).toFloat()
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        b.addView(settingsPanel,LinearLayout.LayoutParams(-1,-2).apply{
            topMargin=d(12);bottomMargin=d(10)
        })
        sectionHeading(settingsPanel,"IZGLED I ZVUK")
        volumeSlider(settingsPanel,"♫  Glazba",progress.musicVolume()){
            progress.setMusicVolume(it);sound.musicVolume=it/100f
        }
        volumeSlider(settingsPanel,"◖  Zvukovi",progress.effectsVolume()){
            progress.setEffectsVolume(it);sound.effectsVolume=it/100f
        }
        val haptic=Switch(this).apply {
            text="Vibracije"
            setTextColor(Color.WHITE)
            isChecked=progress.hapticEnabled()
            setOnCheckedChangeListener { _,checked -> progress.setHapticEnabled(checked) }
        }
        haptic.thumbTintList=ColorStateList.valueOf(gold)
        haptic.trackTintList=ColorStateList.valueOf(0xff1681df.toInt())
        haptic.setPadding(d(12),d(10),d(12),d(10))
        haptic.background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
        settingsPanel.addView(haptic,LinearLayout.LayoutParams(-1,d(54)).apply{bottomMargin=d(7)})
        sectionHeading(settingsPanel,"IGRAČ I TEŽINA")
        small(settingsPanel,"TEŽINA IGRE — utječe na brzinu i gravitaciju")
        // Three genuine selectable cards: reference-matched layout, existing
        // offline difficulty indices and no fake switches or baked-in text.
        val modeRow=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        val modeCards=ArrayList<LinearLayout>(3)
        val modeNames=arrayOf("LAGANO","NORMALNO","ZAHTJEVNO")
        val modeHints=arrayOf("Opušten let","Uravnotežen izazov","Za iskusne pilote")
        val modeArt=intArrayOf(R.drawable.bopiportrait0,R.drawable.bopiportrait6,
            R.drawable.bopiportrait3)
        fun refreshDifficultyCards(){
            val selected=progress.difficulty()
            for(i in 0..2){
                val chosen=i==selected
                modeCards[i].background=gradient(
                    if(chosen)0xff754318.toInt() else 0xff10427f.toInt(),
                    if(chosen)0xff31200e.toInt() else 0xff051c40.toInt(),19
                ).apply {setStroke(d(if(chosen)3 else 2),
                    if(chosen)0xffffc85b.toInt() else 0xff4489d5.toInt())}
                modeCards[i].elevation=d(if(chosen)8 else 2).toFloat()
                modeCards[i].contentDescription="Težina igre ${modeNames[i]}, " +
                    "${modeHints[i]}, ${if(chosen)"odabrano" else "dodirni za odabir"}"
                modeCards[i].isSelected=chosen
            }
        }
        for(mode in 0..2){
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER
                setPadding(d(3),d(7),d(3),d(9))
                isClickable=true;isFocusable=true
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
                setOnClickListener{
                    progress.setDifficulty(mode)
                    sound.effect("click")
                    refreshDifficultyCards()
                }
            }
            val crown=TextView(this).apply{
                text=if(mode==1)"♛" else "✦"
                gravity=Gravity.CENTER;textSize=19f
                setTextColor(0xffffc85b.toInt())
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            card.addView(crown,LinearLayout.LayoutParams(-1,d(23)))
            card.addView(ImageView(this).apply{
                setImageResource(modeArt[mode])
                scaleType=ImageView.ScaleType.FIT_CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(62)))
            card.addView(TextView(this).apply{
                text=modeNames[mode];gravity=Gravity.CENTER
                setTextColor(Color.WHITE);textSize=13f
                typeface=Typeface.DEFAULT_BOLD
                setAutoSizeTextTypeUniformWithConfiguration(10,14,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(24)))
            card.addView(TextView(this).apply{
                text=modeHints[mode];gravity=Gravity.CENTER
                setTextColor(0xffd8ebff.toInt());textSize=11f
                maxLines=2
                setAutoSizeTextTypeUniformWithConfiguration(9,11,1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },LinearLayout.LayoutParams(-1,d(36)))
            modeCards.add(card)
            modeRow.addView(card,LinearLayout.LayoutParams(0,d(170),1f).apply{
                setMargins(d(3),d(5),d(3),d(7))
            })
        }
        refreshDifficultyCards()
        settingsPanel.addView(modeRow,LinearLayout.LayoutParams(-1,-2))
        small(settingsPanel,"Težina se primjenjuje na sljedeći let. Dosadašnji napredak ostaje spremljen.")
        val player=EditText(this).apply {
            hint="Tvoje ime"
            setSingleLine(true)
            setText(progress.playerName())
            setTextColor(Color.WHITE)
            setHintTextColor(0xffb2d6ff.toInt())
            inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setPadding(d(14),0,d(14),0)
            background=gradient(0xff082b59.toInt(),0xff041b3b.toInt(),13)
        }
        settingsPanel.addView(player,LinearLayout.LayoutParams(-1,d(52)))
        action(settingsPanel,"SPREMI IME",false){
            progress.setPlayerName(player.text.toString())
            Toast.makeText(this,"Ime je promijenjeno.",Toast.LENGTH_SHORT).show()
        }
        sectionHeading(settingsPanel,"DODATNE OPCIJE")
        val low=Switch(this).apply{text="Nježnije animacije";setTextColor(Color.WHITE);isChecked=progress.lessMotion();setOnCheckedChangeListener{_,v->progress.setLessMotion(v)}}
        low.thumbTintList=ColorStateList.valueOf(gold)
        low.trackTintList=ColorStateList.valueOf(0xff1681df.toInt())
        low.setPadding(d(12),d(10),d(12),d(10))
        low.background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
        settingsPanel.addView(low,LinearLayout.LayoutParams(-1,d(54)).apply{bottomMargin=d(6)})
        val audio=Switch(this).apply{text="Glazba i zvučni efekti";setTextColor(Color.WHITE);isChecked=progress.soundEnabled();setOnCheckedChangeListener{_,v->progress.setSoundEnabled(v);sound.enabled=v}}
        audio.thumbTintList=ColorStateList.valueOf(gold)
        audio.trackTintList=ColorStateList.valueOf(0xff1681df.toInt())
        audio.setPadding(d(12),d(10),d(12),d(10))
        audio.background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
        settingsPanel.addView(audio,LinearLayout.LayoutParams(-1,d(54)).apply{bottomMargin=d(6)})

        small(settingsPanel,"Pilot: Bopi · sva oprema vrijedi za isti lik.")
        action(settingsPanel,"🛍  TRGOVINA KOVANICAMA",false){showPerks()}
        action(settingsPanel,"🏆  MOJI REKORDI",false){showLeaderboard()}
        sectionHeading(settingsPanel,"PODACI I PRIVATNOST")
        small(settingsPanel,"BOPAVI — Mali let, velika avantura. Stvorio Brendigo.")
        small(settingsPanel,"Bez oglasa, praćenja i kupnje stvarnim novcem.")
        action(settingsPanel,"SPREMI KOPIJU NAPRETKA",false){
            val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json";putExtra(Intent.EXTRA_TITLE,"bopavi-save.json")}
            startActivityForResult(intent,42)
        }
        action(settingsPanel,"VRATI NAPREDAK IZ KOPIJE",false){
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json"}
            startActivityForResult(intent,43)
        }
        back(b){showHome()}
    }
    @Deprecated("Document picker compatibility for existing Android installations")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=42 && requestCode!=43)return
        if(resultCode!=RESULT_OK || data?.data==null)return
        try {
            if(requestCode==42){
                val stream=contentResolver.openOutputStream(data.data!!)
                    ?: error("Izvoz nije dostupan.")
                stream.bufferedWriter(Charsets.UTF_8).use{it.write(progress.exportJson())}
            }
            if(requestCode==43){
                val input=contentResolver.openInputStream(data.data!!) ?: error("Datoteka nije dostupna.")
                // Reject oversized and hostile document-provider data while reading,
                // rather than loading the entire untrusted file into memory.
                val text=input.bufferedReader(Charsets.UTF_8).use { reader ->
                    val out=StringBuilder()
                    val chunk=CharArray(8192)
                    while(out.length<=550000) {
                        val count=reader.read(chunk)
                        if(count<0)break
                        out.append(chunk,0,count)
                    }
                    require(out.length<=550000){"Sigurnosna kopija je prevelika."}
                    out.toString()
                }
                progress.importJson(text)
                sound.enabled=progress.soundEnabled()
                sound.musicVolume=progress.musicVolume()/100f
                sound.effectsVolume=progress.effectsVolume()/100f
            }
            Toast.makeText(this,"Napredak uspješno ${if(requestCode==42)"izvezen" else "uvezen"}.",Toast.LENGTH_LONG).show()
            if(requestCode==43)showHome()
        }catch(e:Exception){
            // Do not expose provider paths or the contents of a private save.
            Toast.makeText(this,"Kopiju nije moguće otvoriti ili spremiti.",Toast.LENGTH_LONG).show()
        }
    }
    // The same navigation semantics apply to Android 8-12 hardware Back and
    // Android 13-16 predictive Back, without losing the current flight.
    @Deprecated("Back navigation compatibility on Android 12 and older")
    override fun onBackPressed() = navigateBack()

    private fun navigateBack() {
        if(selectedScreen=="game") {
            val current=gameView?.game
            when {
                current == null -> showHome()
                current.finished -> showResult(current)
                current.active -> gamePauseButton?.performClick() // Do not discard live runs.
                else -> showPilotPicker(currentWorld,currentLevel) // No purchased gear spent yet.
            }
        } else if(selectedScreen=="home") {
            if (Build.VERSION.SDK_INT >= 33) finish() else super.onBackPressed()
        } else menuBackAction?.invoke() ?: showHome()
    }
}
