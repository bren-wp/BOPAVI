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
import android.view.ViewGroup
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
    private var platformBackCallback: android.window.OnBackInvokedCallback? = null
    private var selectedScreen = "home"
    private val blue = 0xff0f3570.toInt()
    private val textColor = Color.WHITE
    private val gold = 0xffffc24d.toInt()
    private val ocean = 0xff081e45.toInt()
    private val electric = 0xff00d9ff.toInt()
    private val sunset = 0xffff8a00.toInt()
    private val worldAccents = intArrayOf(0xff3bd48f.toInt(),0xffffbb66.toInt(),0xffa5ecff.toInt(),0xffff8163.toInt(),0xffffd891.toInt(),0xffb39afa.toInt(),0xff65e8e1.toInt(),0xffbcb1ff.toInt())
    private fun d(n:Int):Int = (resources.displayMetrics.density*n+.5f).toInt()
    override fun onCreate(state: Bundle?) {
        setTheme(R.style.BopaviTheme)
        super.onCreate(state)
        window.statusBarColor=0xff09264a.toInt()
        window.navigationBarColor=0xff09264a.toInt()
        window.decorView.systemUiVisibility=0
        progress=ProgressStore(this);sound=Soundscape(this)
        sound.enabled=progress.soundEnabled()
        // Android 16 no longer routes predictive Back through onBackPressed().
        if (Build.VERSION.SDK_INT >= 33) {
            val callback = android.window.OnBackInvokedCallback { navigateBack() }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
            platformBackCallback = callback
        }
        showHome()
    }
    private fun showNativeView(root: View) {
        setContentView(root)
        // Android 15 edge-to-edge: reserve system-bar insets for physical controls.
        if (android.os.Build.VERSION.SDK_INT >= 35 && (root !is FrameLayout || selectedScreen=="home")) {
            // Apply insets once to menu containers. Immersive gameplay must not
            // repeatedly change the FrameLayout padding as system bars animate.
            root.setOnApplyWindowInsetsListener { v, insets ->
                val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars())
                if(v.paddingLeft!=bars.left || v.paddingTop!=bars.top ||
                    v.paddingRight!=bars.right || v.paddingBottom!=bars.bottom) {
                    v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
                }
                insets
            }
            root.requestApplyInsets()
        }
    }
    override fun onPause() {
        // An idle preview has no visible pause button; only pause active flights.
        if(gameView?.game?.active == true) gameView?.paused = true
        sound.pause()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        if(gameView?.game?.active == false && gameView?.paused == false) sound.resume()
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
        gravity=Gravity.CENTER
        setPadding(d(16),d(13),d(16),d(13))
        background=gradient(0xff192d50.toInt(),0xff203c65.toInt(),24)
        contentDescription=label
    }
    private fun base(label:String,subtitle:String):LinearLayout {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=0
        gameView?.paused=true;gameView=null;gamePauseButton=null;sound.stop();selectedScreen=label
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
            setPadding(d(18),d(18),d(18),d(28))
        }
        val scroll=ScrollView(this).apply{
            isFillViewport=true;isVerticalScrollBarEnabled=false
            clipToPadding=false
            addView(body)
        }
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
        if(Build.VERSION.SDK_INT>=35){
            scroll.setOnApplyWindowInsetsListener{v,insets->
                val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars())
                v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
                insets
            }
        }
        showNativeView(root)
        ImageView(this).apply {
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }.also{body.addView(it,LinearLayout.LayoutParams(-1,d(94)))}
        if(label!="BOPAVI")title(body,label,31,Color.WHITE)
        if(subtitle.isNotEmpty() && label!="BOPAVI")title(body,subtitle,14,0xffc9edff.toInt())
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
    private fun back(parent:LinearLayout,onClick:()->Unit) = action(parent,"‹  Natrag",false,onClick)
    private fun worldTile(parent:LinearLayout,world:Int,onClick:()->Unit){
        val name=LevelEngine.names[world]
        val row=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            background=gradient(0xff203c5a.toInt(),0xff10233d.toInt(),20).apply {
                if(world==progress.chosenWorld()) setStroke(d(3),worldAccents[world])
            }
            setPadding(d(16),d(14),d(16),d(14))
            elevation=d(3).toFloat()
            isClickable=true;isFocusable=true
            contentDescription="$name, otključano${if(world==progress.chosenWorld()) ", odabrano" else ""}"
            setOnClickListener{sound.effect("click");onClick()}
        }
        val preview=ImageView(this).apply{
            val art=intArrayOf(R.drawable.world0,R.drawable.world1,R.drawable.world2,R.drawable.world3,
                R.drawable.world4,R.drawable.world5,R.drawable.world6,R.drawable.world7)
            setImageResource(art[world])
            scaleType=ImageView.ScaleType.CENTER_CROP
            background=gradient(0xff126ca9.toInt(),0xff0e2b57.toInt(),17)
            clipToOutline=true
            contentDescription="Prikaz svijeta $name"
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        row.addView(preview,LinearLayout.LayoutParams(-1,d(150)).apply{bottomMargin=d(9)})
        row.addView(TextView(this).apply{
            text="${if(world==progress.chosenWorld()) "✓ " else ""}${LevelEngine.collectibleIcons[world]}  $name   ↗"
            textSize=15f;setTextColor(worldAccents[world]);typeface=Typeface.DEFAULT_BOLD
            gravity=Gravity.CENTER_HORIZONTAL
        })
        row.addView(TextView(this).apply{
            text="Level ${progress.streamFrontier(world)} · ${LevelEngine.collectibles[world]}"
            textSize=12f;setTextColor(0xffd0e6f5.toInt());gravity=Gravity.CENTER_HORIZONTAL
            setPadding(0,d(5),0,0)
        })
        parent.addView(row,LinearLayout.LayoutParams(0,-2,1f).apply{
            setMargins(d(4),d(5),d(4),d(7))
        })
    }
    private fun showHome(){
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=0
        gameView?.paused=true;gameView=null;gamePauseButton=null;sound.stop();selectedScreen="home"
        val root=FrameLayout(this).apply{setBackgroundColor(0xff086ab7.toInt())}
        root.addView(ImageView(this).apply {
            setImageResource(R.drawable.splash)
            scaleType=ImageView.ScaleType.CENTER_CROP
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        },FrameLayout.LayoutParams(-1,-1))
        root.addView(View(this).apply{
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x55052251,0x001967ae,0x001967ae,0xbb03112e.toInt()))
        },FrameLayout.LayoutParams(-1,-1))
        val scroll=ScrollView(this).apply{
            isFillViewport=true;isVerticalScrollBarEnabled=false
            clipToPadding=false
        }
        val layout=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(22),d(12),d(22),d(16))
        }
        scroll.addView(layout)
        root.addView(scroll,FrameLayout.LayoutParams(-1,-1))
        // Preserve the artwork logo, keep score and coin values live.
        val wallets=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        wallets.addView(chip("🏆  ${progress.bestPoints()}"),
            LinearLayout.LayoutParams(0,-2,1f).apply{rightMargin=d(6)})
        wallets.addView(chip("●  ${progress.coins()}"),
            LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=d(6)})
        layout.addView(wallets,LinearLayout.LayoutParams(-1,-2))
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
        val entries=listOf(
            Triple("♙","PROFIL",0),
            Triple("★","ZADACI",1),
            Triple("✦","KOLEKCIJA",2),
            Triple("▣","TRGOVINA",3)
        )
        for((symbol,label,index) in entries){
            val tab=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER
                isClickable=true;isFocusable=true
                contentDescription=label
                background=RippleDrawable(ColorStateList.valueOf(0x33ffffff),
                    gradient(0xff1358a5.toInt(),0xff071e46.toInt(),14),null)
                addView(TextView(this@MainActivity).apply{
                    text=symbol;textSize=25f;setTextColor(if(index==3)gold else Color.WHITE)
                    gravity=Gravity.CENTER
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(29)))
                addView(TextView(this@MainActivity).apply{
                    text=label;textSize=11f;setTextColor(Color.WHITE)
                    gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(21)))
                setOnClickListener{
                    sound.effect("click")
                    when(index){
                        0->showSettings()
                        1->showAchievements()
                        2->showSkins()
                        else->showPerks()
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
                val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars())
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
            contentDescription="Level $n, ${LevelKind.name(kind)}, ${if(!unlocked)"zaključan" else if(completed)"dovršen" else "otključan"}"
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
            text=if(!unlocked)"🔒" else if(completed)"✓  ${LevelKind.icon(kind)}" else "★  ${LevelKind.icon(kind)}"
            textSize=17f;gravity=Gravity.CENTER
            setTextColor(if(current)gold else Color.WHITE)
            setShadowLayer(d(2).toFloat(),0f,d(2).toFloat(),Color.BLACK)
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        frame.addView(badge,FrameLayout.LayoutParams(-1,d(34),Gravity.BOTTOM))
        parent.addView(frame,LinearLayout.LayoutParams(0,d(86),1f).apply{
            setMargins(d(3),d(4),d(3),d(4))
        })
    }
    private fun showLevels(world:Int,page:Int){
        currentWorld=world;progress.chooseWorld(world)
        val b=base("LEVELI","Odaberite level · ${LevelEngine.names[world]}")
        val maxNumber=progress.frontier(world).coerceAtMost(LevelEngine.LEVELS_PER_WORLD)
        val safePage=page.coerceIn(1,LevelEngine.LEVELS_PER_WORLD)
        val start=((safePage-1)/20)*20+1
        val zone=LevelEngine.create(world,start).zone
        small(b,"ZONA $zone · LEVELI $start–${(start+19).coerceAtMost(LevelEngine.LEVELS_PER_WORLD)}")
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
        if(start+20<=maxNumber){
            val next=LinearLayout(this)
            pager.addView(next,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=d(4)})
            action(next,"SLJEDEĆIH 20 →",true){showLevels(world,start+20)}
        }
        sectionHeading(b,"NASTAVI ILI ODABERI LEVEL")
        small(b,"Otključano do levela $maxNumber")
        action(b,"▶  NASTAVI LET"){showPilotPicker(world,progress.streamFrontier(world))}
        val input=EditText(this).apply{
            inputType=android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true);hint="Broj otključanog levela"
            setTextColor(Color.WHITE);setHintTextColor(0xffb2d6ff.toInt())
            setText(safePage.toString());gravity=Gravity.CENTER
            background=gradient(0xff082c61.toInt(),0xff07183d.toInt(),18)
        }
        b.addView(input,LinearLayout.LayoutParams(-1,d(52)))
        action(b,"▶  POKRENI ODABRANI LEVEL",false){
            val n=input.text.toString().toIntOrNull()
            if(n==null||n !in 1..maxNumber)Toast.makeText(this,
                "Level mora biti otključan i unutar raspona.",Toast.LENGTH_LONG).show()
            else showPilotPicker(world,n.toLong())
        }
        back(b){showWorlds()}
    }
    private fun characterGallery(parent:LinearLayout,refresh:()->Unit) {
        val portraits=intArrayOf(R.drawable.bopi0,R.drawable.bopi1,R.drawable.bopi2,
            R.drawable.bopi3,R.drawable.bopi4,R.drawable.bopi5,R.drawable.bopi6,R.drawable.bopi7,R.drawable.bopi8)
        for(start in progress.skinNames.indices step 3) {
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            parent.addView(row,LinearLayout.LayoutParams(-1,-2))
            for(i in start until minOf(start+3,progress.skinNames.size)) {
                val selected=progress.skin()==i
                val owned=progress.owned(i)
                val cost=progress.costs[i]
                val status=if(selected)"✓ ODABRAN" else if(owned)"DOSTUPAN" else if(i>=7)"PREMIUM · $cost KOVANICA" else "$cost KOVANICA"
                val card=LinearLayout(this).apply {
                    orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL
                    isClickable=true;isFocusable=true
                    val face=gradient(if(selected)0xffffa51c.toInt() else 0xff0b5db9.toInt(),
                        if(selected)0xffb55c00.toInt() else 0xff071e46.toInt(),17)
                    face.setStroke(d(if(selected)3 else 1),
                        if(selected)gold else 0xff5680b4.toInt())
                    background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),face,null)
                    contentDescription="${progress.skinNames[i]}, $status"
                    setPadding(d(3),d(5),d(3),d(5))
                    setOnClickListener {
                        sound.effect("click")
                        if(owned) {
                            progress.selectOrBuy(i);refresh()
                        } else if(progress.coins()<cost) {
                            Toast.makeText(this@MainActivity,"Nedovoljno kovanica za ${progress.skinNames[i]}.",
                                Toast.LENGTH_SHORT).show()
                        } else {
                            android.app.AlertDialog.Builder(this@MainActivity)
                                .setTitle("Otključati ${progress.skinNames[i]}?")
                                .setMessage("Potrošit ćeš $cost osvojenih kovanica. Potvrdi otključavanje.")
                                .setNegativeButton("ODUSTANI",null)
                                .setPositiveButton("OTKLJUČAJ"){_,_ ->
                                    if(progress.selectOrBuy(i))refresh()
                                }.show()
                        }
                    }
                }
                card.addView(ImageView(this).apply {
                    setImageResource(portraits[i]);scaleType=ImageView.ScaleType.FIT_CENTER
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(84)))
                card.addView(TextView(this).apply {
                    text=progress.skinNames[i];textSize=13f
                    typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE);gravity=Gravity.CENTER
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(21)))
                card.addView(TextView(this).apply {
                    text=status;textSize=10f;gravity=Gravity.CENTER
                    setTextColor(if(selected)gold else 0xffbce6ff.toInt())
                    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },LinearLayout.LayoutParams(-1,d(23)))
                row.addView(card,LinearLayout.LayoutParams(0,d(138),1f).apply{
                    setMargins(d(2),d(5),d(2),d(5))
                })
            }
            // Nine characters occupy three real, keyboard-accessible columns.
        }
    }
    /** Mandatory pre-flight character choice; boosts are consumed only on the first flap. */
    private fun showPilotPicker(world:Int,number:Long) {
        currentWorld=world;currentLevel=number
        val b=base("ODABERI LIKA","Svaki let započinje tvojim izborom letača")
        val idx=progress.skin()
        val portraits=intArrayOf(R.drawable.bopi0,R.drawable.bopi1,R.drawable.bopi2,
            R.drawable.bopi3,R.drawable.bopi4,R.drawable.bopi5,R.drawable.bopi6,R.drawable.bopi7,R.drawable.bopi8)
        val portrait=ImageView(this).apply {
            setImageResource(portraits[idx])
            scaleType=ImageView.ScaleType.FIT_CENTER
            background=gradient(0xff23699d.toInt(),0xff102f61.toInt(),25)
            contentDescription="Pregled lika ${progress.skinNames[idx]}"
            setPadding(d(18),d(8),d(18),d(8))
        }
        b.addView(portrait,LinearLayout.LayoutParams(-1,d(166)).apply{bottomMargin=d(8)})
        title(b,progress.skinNames[idx],25,gold)
        small(b,when(idx) {
            6 -> "Portantin: krilati čovječuljak sa zaštitnom opremom i suputnikom."
            7 -> "Noa: premium nebeski istraživač s električno plavim krilima, vizir-naočalama i zvjezdanim oklopom."
            8 -> "Any: premium čarobnica s ružičastim krilima, zvjezdanom tijarom i ljubičastom haljinom."
            else -> "Izaberi svog letača. Odabir se čuva na uređaju."
        })
        small(b,"Dostupno: ${progress.coins()} kovanica · ${LevelEngine.names[world]} · Level $number")
        action(b,"▶  POLETI S ${progress.skinNames[idx].uppercase()}"){startGame(world,number)}
        sectionHeading(b,"ODABERI SVOG LETAČA")
        characterGallery(b){showPilotPicker(world,number)}
        back(b){showLevels(world,number.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())}
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
        val game=GameView(this,simulation,progress.lessMotion(),progress.skin(),progress.hapticEnabled(),
            onFinished={if(selectedScreen=="game" && gameView?.game === it)showResult(it)},
            onLevelCompleted={completed ->
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
            onShieldImpact={sound.effect("hit")})
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
                android.app.AlertDialog.Builder(this@MainActivity).setTitle("Pauza · Level ${game.game.displayLevel}")
                    .setItems(arrayOf("Nastavi let",
                        if(progress.soundEnabled()) "🔇 Isključi zvuk" else "🔊 Uključi zvuk",
                        "Mapa svjetova","Oprema za kovanice","Izgled Bopija","Zvuk i prikaz")) { _,choice ->
                        sound.effect("click")
                        when(choice){
                            0 -> {game.paused=false;sound.resume()}
                            1 -> {
                                val enabled=!progress.soundEnabled()
                                progress.setSoundEnabled(enabled)
                                sound.enabled=enabled
                                game.paused=false
                                if(enabled)sound.resume()
                            }
                            2 -> showWorlds()
                            3 -> showPerks()
                            4 -> showSkins()
                            5 -> showSettings()
                        }
                    }.setOnCancelListener{game.paused=false;sound.resume()}.show()
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
        sound.effect("hit")
        val headline=ResultHeadline.label(g.score(),progress.bestPoints(),g.won)
        progress.recordRun(g)
        currentLevel=g.displayLevel
        gameView?.paused=true;gameView=null;gamePauseButton=null;selectedScreen="result"
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=0
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
            setPadding(d(24),d(28),d(24),d(26))
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
        val artwork=ImageView(this).apply{
            setImageResource(R.drawable.hero)
            scaleType=ImageView.ScaleType.CENTER_CROP
            background=gradient(0xff218fe3.toInt(),0xff74d9fa.toInt(),24)
            clipToOutline=true
            contentDescription="Bopi iznad čarobnih otoka"
        }
        panel.addView(artwork,LinearLayout.LayoutParams(-1,d(166)).apply{bottomMargin=d(9)})
        title(panel,headline,31,Color.WHITE)
        val stats=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(d(18),d(16),d(18),d(17))
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xf5083773.toInt(),0xf5031638.toInt())).apply{
                cornerRadius=d(22).toFloat();setStroke(d(2),0xff49d7ff.toInt())
            }
            elevation=d(5).toFloat()
        }
        val score=TextView(this).apply{
            text="${g.score()} BODOVA";textSize=38f;setTextColor(gold)
            gravity=Gravity.CENTER;typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            contentDescription="Rezultat ${g.score()}"
        }
        stats.addView(score,LinearLayout.LayoutParams(-1,-2))
        fun detail(value:String){
            stats.addView(TextView(this).apply{
                text=value;textSize=16f;gravity=Gravity.CENTER
                setTextColor(Color.WHITE);setPadding(0,d(5),0,d(5))
                typeface=Typeface.create("sans-serif-medium",Typeface.BOLD)
            })
        }
        detail("🏆  Najbolji rezultat: ${progress.bestPoints()}")
        detail("Level $currentLevel · Ukupno prolaza: ${g.totalPassed}")
        detail("Težina: ${progress.difficultyNames[g.difficulty]} · ${progress.playerName()}")
        detail("${LevelEngine.collectibleIcons[currentWorld]}  ${g.coins+g.stars}   ·   ● ${progress.coins()} kovanica")
        panel.addView(stats,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(9);bottomMargin=d(15)})
        sectionHeading(panel,"✦  NAGRADE I NAPREDAK")
        small(panel,"●  Osvojeno u letu: ${g.coins+g.stars} · Ukupno: ${progress.coins()} kovanica")
        action(panel,"▶  PONOVO"){showPilotPicker(currentWorld,currentLevel)}
        action(panel,"LOKALNA LJESTVICA",false){showLeaderboard()}
        action(panel,"🛍  TRGOVINA KOVANICAMA",false){showPerks()}
        action(panel,"MAPA SVJETOVA",false){showWorlds()}
        action(panel,"POČETNI EKRAN",false){showHome()}
        showNativeView(root)
    }
    private fun showPerks(){
        val b=base("TRGOVINA","Za kovanice osvojene igrom — bez stvarnog novca")
        title(b,"●  ${progress.coins()} KOVANICA",24,gold)
        small(b,"Za svakih novih 1.000 bodova najboljeg rezultata dobivaš 1 kovanicu.")
        val bonus=progress.bonusCoinsAvailable()
        if(bonus>0)action(b,"🎁  PREUZMI $bonus KOVANICA ZA BODOVE",false){
            val earned=progress.claimBonusCoins()
            if(earned>0){sound.effect("purchase");showPerks()}
        }

        small(b,"Osvajaj kovanice prelaskom nagradnih levela i biraj opremu za svoj sljedeći let.")
        for(n in 0..1){
            val explanation=if(n==0)"Čuva Bopija od jednog sudara" else "Privlači kovanice i predmete 8 sekundi"
            small(b,"${if(n==0) "🛡 ŠTIT" else "🧲 MAGNET"} · $explanation\nU torbi: ${progress.perkCount(n)}")
            action(b,"KUPI ${if(n==0) "ŠTIT" else "MAGNET"} · ${progress.perkPrices[n]} KOVANICA",false){
                if(progress.buyPerk(n)){sound.effect("purchase");showPerks()}
                else Toast.makeText(this,"Nedovoljno kovanica ili je zaliha puna.",Toast.LENGTH_SHORT).show()
            }
        }
        small(b,"Kupljena oprema automatski se koristi na početku sljedećeg leta.")
        action(b,"🎨  LIKOVI",false){showSkins()}
        back(b){showSettings()}
    }
    private fun showSkins(){
        val b=base("LIKOVI","Devet letača, uključujući Nou i Any")
        small(b,"Stanje: ${progress.coins()} kovanica")
        characterGallery(b){showSkins()}
        back(b){showSettings()}
    }
    private fun showLeaderboard(){
        val b=base("LJESTVICA","Najbolji stvarni rezultati na ovom uređaju")
        val entries=progress.leaderboard()
        if(entries.isEmpty()) small(b,"Još nema rezultata. Odigraj let i osvoji bodove!")
        for((index,item) in entries.withIndex()){
            small(b,"${index+1}. ${item.name} · ${item.score} bodova")
            small(b,"${LevelEngine.names[item.world]} · ${progress.difficultyNames[item.difficulty]} · ${item.gates} prolaza")
        }
        action(b,"POSTIGNUĆA",false){showAchievements()}
        back(b){showSettings()}
    }
    private fun showAchievements(){
        val b=base("POSTIGNUĆA","Tvoj napredak spremljen je samo na uređaju")
        small(b,"Dovršeni leveli: ${((0..7).sumOf { (progress.frontier(it)-1).toLong() })}")
        small(b,"Pobjede: ${progress.wins()}  ·  Pokušaji bez pobjede: ${progress.deaths()}")
        val furthest=(0..7).maxByOrNull { progress.streamFrontier(it) } ?: 0
        small(b,"Svijet u kojem si najdalje napredovao: ${LevelEngine.names[furthest]}")
        for(w in 0..7)small(b,"${LevelEngine.names[w]} · najbolji rezultat ${progress.best(w)}")
        back(b){showSettings()}
    }
    private fun showSettings(){
        val b=base("POSTAVKE","Prilagodi igru svom stilu")
        sectionHeading(b,"IZGLED I ZVUK")
        val low=Switch(this).apply{text="Nježnije animacije";setTextColor(Color.WHITE);isChecked=progress.lessMotion();setOnCheckedChangeListener{_,v->progress.setLessMotion(v)}}
        low.thumbTintList=ColorStateList.valueOf(gold)
        low.trackTintList=ColorStateList.valueOf(0xff1681df.toInt())
        low.setPadding(d(12),d(10),d(12),d(10))
        low.background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
        b.addView(low,LinearLayout.LayoutParams(-1,d(54)).apply{bottomMargin=d(6)})
        val audio=Switch(this).apply{text="Glazba i zvučni efekti";setTextColor(Color.WHITE);isChecked=progress.soundEnabled();setOnCheckedChangeListener{_,v->progress.setSoundEnabled(v);sound.enabled=v}}
        audio.thumbTintList=ColorStateList.valueOf(gold)
        audio.trackTintList=ColorStateList.valueOf(0xff1681df.toInt())
        audio.setPadding(d(12),d(10),d(12),d(10))
        audio.background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
        b.addView(audio,LinearLayout.LayoutParams(-1,d(54)).apply{bottomMargin=d(6)})
        val haptic=Switch(this).apply {
            text="Vibracije pri igranju"
            setTextColor(Color.WHITE)
            isChecked=progress.hapticEnabled()
            setOnCheckedChangeListener { _,checked -> progress.setHapticEnabled(checked) }
        }
        haptic.thumbTintList=ColorStateList.valueOf(gold)
        haptic.trackTintList=ColorStateList.valueOf(0xff1681df.toInt())
        haptic.setPadding(d(12),d(10),d(12),d(10))
        haptic.background=gradient(0xee082856.toInt(),0xee031632.toInt(),16)
        b.addView(haptic,LinearLayout.LayoutParams(-1,d(54)))
        sectionHeading(b,"IGRAČ I TEŽINA")
        small(b,"TEŽINA IGRE — utječe na brzinu i gravitaciju")
        val modes=android.widget.RadioGroup(this).apply{orientation=LinearLayout.VERTICAL}
        for(mode in 0..2) {
            val item=android.widget.RadioButton(this).apply {
                text=progress.difficultyNames[mode]
                setTextColor(Color.WHITE)
                textSize=17f
                buttonTintList=ColorStateList.valueOf(gold)
                background=gradient(0xb0082856.toInt(),0xb0031632.toInt(),13)
                setPadding(d(12),0,d(6),0)
                id=View.generateViewId()
                isChecked=progress.difficulty()==mode
                setOnClickListener { progress.setDifficulty(mode) }
            }
            modes.addView(item,LinearLayout.LayoutParams(-1,d(48)).apply{bottomMargin=d(5)})
        }
        b.addView(modes,LinearLayout.LayoutParams(-1,-2))
        small(b,"Težina se primjenjuje na sljedeći let. Dosadašnji napredak ostaje spremljen.")
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
        b.addView(player,LinearLayout.LayoutParams(-1,d(52)))
        action(b,"SPREMI IME",false){
            progress.setPlayerName(player.text.toString())
            Toast.makeText(this,"Ime je spremljeno na uređaju.",Toast.LENGTH_SHORT).show()
        }
        sectionHeading(b,"DODATNE OPCIJE")
        action(b,"🐤  LIKOVI",false){showSkins()}
        action(b,"🛍  TRGOVINA KOVANICAMA",false){showPerks()}
        action(b,"🏆  LOKALNA LJESTVICA",false){showLeaderboard()}
        sectionHeading(b,"PODACI I PRIVATNOST")
        small(b,"BOPAVI — Mali let, velika avantura. Stvorio Brendigo.")
        small(b,"Bez oglasa i kupnje stvarnim novcem. Tvoj napredak ostaje na uređaju.")
        action(b,"SPREMI KOPIJU NAPRETKA",false){
            val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json";putExtra(Intent.EXTRA_TITLE,"bopavi-save.json")}
            startActivityForResult(intent,42)
        }
        action(b,"VRATI NAPREDAK IZ KOPIJE",false){
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json"}
            startActivityForResult(intent,43)
        }
        back(b){showHome()}
    }
    @Deprecated("Activity result callbacks are used for a dependency-free Android sample")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK || data?.data==null)return
        try {
            if(requestCode==42){contentResolver.openOutputStream(data.data!!)?.bufferedWriter(Charsets.UTF_8)?.use{it.write(progress.exportJson())}}
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
                progress.importJson(text);sound.enabled=progress.soundEnabled()
            }
            Toast.makeText(this,"Napredak uspješno ${if(requestCode==42)"izvezen" else "uvezen"}.",Toast.LENGTH_LONG).show()
            if(requestCode==43)showHome()
        }catch(e:Exception){Toast.makeText(this,"Pogreška: ${e.message}",Toast.LENGTH_LONG).show()}
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
        } else showHome()
    }
}
