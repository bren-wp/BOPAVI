package com.brendigo.bopavi

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.os.Bundle
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
    private var selectedScreen = "home"
    private val blue = 0xff0f3570.toInt()
    private val textColor = Color.WHITE
    private val gold = 0xffffce77.toInt()
    private val ocean = 0xff17315c.toInt()
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
        gameView?.paused = true
        sound.pause()
        super.onPause()
    }
    override fun onDestroy() {
        gameView?.paused = true
        if (::sound.isInitialized) sound.close()
        super.onDestroy()
    }
    private fun gradient(a:Int,b:Int,rad:Int=27):GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(a,b)).apply{
        cornerRadius=d(rad).toFloat()
        setStroke(d(1),0x66c6eaff)
    }
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
        gameView?.paused=true;gameView=null;sound.stop();selectedScreen=label
        val body=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(d(20),d(22),d(20),d(30))
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xff0a427b.toInt(),0xff096aaf.toInt(),0xff2b9ed9.toInt()))
        }
        val scroll=ScrollView(this).apply {
            isFillViewport=true;isVerticalScrollBarEnabled=false
            addView(body)
        }
        showNativeView(scroll)
        if(label!="BOPAVI")title(body,label,32,gold)
        if(subtitle.isNotEmpty() && label!="BOPAVI")title(body,subtitle,15,0xffbfe0f5.toInt())
        return body
    }
    private fun title(parent:LinearLayout,s:String,size:Int,color:Int=textColor) {
        parent.addView(TextView(this).apply {
            text=s
            textSize=size.toFloat()
            setTextColor(color)
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            gravity=Gravity.CENTER
            setPadding(d(6),d(10),d(6),d(10))
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
            textSize=17f;isAllCaps=false
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            letterSpacing=.025f
            setAutoSizeTextTypeUniformWithConfiguration(12,17,1,android.util.TypedValue.COMPLEX_UNIT_SP)
            val normal=if(primary) GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xffc5ff7a.toInt(),0xff6cec43.toInt(),0xff13b742.toInt())).apply{
                    cornerRadius=d(31).toFloat()
                    setStroke(d(2),0xffd5ffab.toInt())
                } else gradient(0xff257ce0.toInt(),0xff123f9a.toInt(),29)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),normal,null)
            elevation=d(5).toFloat()
            contentDescription=text
            setOnClickListener{sound.effect("click");onClick()}
        }
        parent.addView(button,LinearLayout.LayoutParams(-1,d(if(primary)64 else 56)).apply {setMargins(0,d(6),0,d(6))})
    }
    private fun back(parent:LinearLayout,onClick:()->Unit) = action(parent,"‹  Natrag",false,onClick)
    private fun worldTile(parent:LinearLayout,world:Int,onClick:()->Unit){
        val name=LevelEngine.names[world]
        val row=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            background=gradient(0xff203c5a.toInt(),0xff10233d.toInt(),20)
            setPadding(d(16),d(14),d(16),d(14))
            elevation=d(3).toFloat()
            isClickable=true;isFocusable=true
            contentDescription="$name, otključano"
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
            text="${LevelEngine.collectibleIcons[world]}  $name   ↗"
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
        gameView?.paused=true;gameView=null;sound.stop();selectedScreen="home"
        // Full-bleed illustrated home, rather than a small banner in a dark scroll page.
        val background=FrameLayout(this).apply {setBackgroundColor(0xff123b6e.toInt())}
        val scene=ImageView(this).apply {
            setImageResource(R.drawable.splash)
            scaleType=ImageView.ScaleType.CENTER_CROP
            contentDescription="Ilustrirani BOPAVI svijet s Bopijem"
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        background.addView(scene,FrameLayout.LayoutParams(-1,-1))
        val shading=View(this).apply{
            this.background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x330a52a5,0x000b6cbb,0x000b6cbb,0x22043979))
        }
        background.addView(shading,FrameLayout.LayoutParams(-1,-1))
        val layout=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(22),d(14),d(22),d(28))
        }
        background.addView(layout,FrameLayout.LayoutParams(-1,-1))
        // The portrait background already contains the logo and tagline. No duplicates.
        layout.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        // Two compact counters: actual stored best score and earned coins.
        val bestScore=progress.bestPoints()
        background.addView(chip("🏆  $bestScore"),FrameLayout.LayoutParams(-2,-2,
            Gravity.TOP or Gravity.LEFT).apply{setMargins(d(16),d(16),0,0)})
        background.addView(chip("●  ${progress.coins()}"),FrameLayout.LayoutParams(-2,-2,
            Gravity.TOP or Gravity.RIGHT).apply{setMargins(0,d(16),d(17),0)})
        // Three clear home actions, with IGRAJ dominant and two equal shortcuts.
        // Leave the illustrated logo and Bopi free of extra text or opaque tiles.
        action(layout,"▶  IGRAJ"){
            val world=progress.chosenWorld()
            startGame(world,progress.streamFrontier(world))
        }
        val shortcuts=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
        }
        layout.addView(shortcuts,LinearLayout.LayoutParams(-1,-2))
        val worldsColumn=LinearLayout(this)
        val settingsColumn=LinearLayout(this)
        shortcuts.addView(worldsColumn,LinearLayout.LayoutParams(0,-2,1f).apply{rightMargin=d(6)})
        shortcuts.addView(settingsColumn,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=d(6)})
        action(worldsColumn,"🌍  SVJETOVI",false){showWorlds()}
        action(settingsColumn,"⚙  POSTAVKE",false){showSettings()}
        showNativeView(background)
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
        val b=base(LevelEngine.names[world],"Odaberi otključani level")
        val maxNumber=progress.frontier(world).coerceAtMost(LevelEngine.LEVELS_PER_WORLD)
        val safePage=page.coerceIn(1,LevelEngine.LEVELS_PER_WORLD)
        small(b,"Otključano do levela $maxNumber")
        action(b,"▶  NASTAVI LET"){startGame(world,progress.streamFrontier(world))}
        val input=EditText(this).apply{
            inputType=android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true);hint="Broj otključanog levela"
            setTextColor(Color.WHITE);setHintTextColor(0xffb2d6ff.toInt());setText(safePage.toString());gravity=Gravity.CENTER
            background=gradient(0xff183964.toInt(),0xff254e84.toInt())
        }
        b.addView(input,LinearLayout.LayoutParams(-1,d(52)))
        action(b,"▶  POKRENI ODABRANI LEVEL") {
            val n=input.text.toString().toIntOrNull()
            if(n==null||n !in 1..maxNumber)Toast.makeText(this,"Level mora biti otključan i unutar raspona.",Toast.LENGTH_LONG).show()
            else startGame(world,n.toLong())
        }
        val start=((safePage-1)/20)*20+1
        for(r in 0 until 5){
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            for(col in 0 until 4){
                val n=start+r*4+col
                if(n>LevelEngine.LEVELS_PER_WORLD)continue
                val unlocked=n<=maxNumber
                val cell=Button(this).apply{
                    text=if(!unlocked)"🔒\n$n" else if(n<maxNumber)"★\n$n" else "▶\n$n"
                    setTextColor(if(unlocked)Color.WHITE else 0xff91a8c6.toInt())
                    textSize=14f;isAllCaps=false
                    typeface=Typeface.DEFAULT_BOLD
                    background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                        if(unlocked)gradient(0xff268cf0.toInt(),0xff174aa8.toInt(),16)
                        else gradient(0xff20395c.toInt(),0xff122641.toInt(),16),null)
                    contentDescription="Level $n, ${if(unlocked) "otključan" else "zaključan"}"
                    setOnClickListener{
                        if(unlocked){sound.effect("click");startGame(world,n.toLong())}
                        else Toast.makeText(this@MainActivity,"Prvo dovrši prethodni level.",Toast.LENGTH_SHORT).show()
                    }
                }
                row.addView(cell,LinearLayout.LayoutParams(0,d(63),1f).apply{setMargins(d(3),d(3),d(3),d(3))})
            }
            if(row.childCount>0)b.addView(row,LinearLayout.LayoutParams(-1,-2))
        }
        if(start>1)action(b,"← Prethodnih 20",false){showLevels(world,start-20)}
        if(start+20<=maxNumber)action(b,"Sljedećih 20 →",false){showLevels(world,start+20)}
        back(b){showWorlds()}
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
        val boosts=progress.consumePerks()
        val simulation=GameSimulation(LevelEngine.createStream(world,number),true,boosts.first,boosts.second,progress.difficulty(),number)
        val frame=FrameLayout(this).apply{setBackgroundColor(0xff092044.toInt())}
        sound.startWorld(world)
        val game=GameView(this,simulation,progress.lessMotion(),progress.skin(),progress.hapticEnabled(),
            onFinished={showResult(it)},
            onLevelCompleted={completed ->
                val amount=progress.completeLevel(world,completed,simulation.score())
                sound.effect("level")
                if(amount>0)Toast.makeText(this,"Level $completed: +$amount kovanica!",Toast.LENGTH_SHORT).show()
            },
            onFlap={sound.effect("tap")},
            onCollect={sound.effect("collect")},
            onShieldImpact={sound.effect("hit")})
        gameView=game;frame.addView(game,FrameLayout.LayoutParams(-1,-1))
        val pause=Button(this).apply{
            text="Ⅱ";textSize=23f;setTextColor(Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),gradient(0xff254c79.toInt(),0xff132b51.toInt(),18),null)
            elevation=d(5).toFloat()
            contentDescription="Izbornik tijekom igre"
            setOnClickListener{
                game.paused=true;sound.pause()
                android.app.AlertDialog.Builder(this@MainActivity).setTitle("Pauza")
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
        frame.addView(pause,FrameLayout.LayoutParams(d(56),d(56),Gravity.TOP or Gravity.RIGHT).apply{setMargins(0,d(24),d(15),0)})
        showNativeView(frame)
    }
    private fun showResult(g:GameSimulation){
        sound.effect("hit")
        progress.recordRun(g)
        currentLevel=g.displayLevel
        gameView?.paused=true;gameView=null;selectedScreen="result"
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
        val artwork=ImageView(this).apply{
            setImageResource(R.drawable.hero)
            scaleType=ImageView.ScaleType.CENTER_CROP
            background=gradient(0xff218fe3.toInt(),0xff74d9fa.toInt(),24)
            clipToOutline=true
            contentDescription="Bopi iznad čarobnih otoka"
        }
        panel.addView(artwork,LinearLayout.LayoutParams(-1,d(166)).apply{bottomMargin=d(9)})
        title(panel,"LET ZAVRŠEN!",31,0xffffdc62.toInt())
        val stats=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(d(18),d(16),d(18),d(17))
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xfffffff9.toInt(),0xffe6f5ff.toInt())).apply{
                cornerRadius=d(22).toFloat();setStroke(d(2),0xffffd779.toInt())
            }
            elevation=d(5).toFloat()
        }
        val score=TextView(this).apply{
            text="${g.score()} BODOVA";textSize=38f;setTextColor(0xff0b3777.toInt())
            gravity=Gravity.CENTER;typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            contentDescription="Rezultat ${g.score()}"
        }
        stats.addView(score,LinearLayout.LayoutParams(-1,-2))
        fun detail(value:String){
            stats.addView(TextView(this).apply{
                text=value;textSize=16f;gravity=Gravity.CENTER
                setTextColor(0xff17477e.toInt());setPadding(0,d(5),0,d(5))
                typeface=Typeface.create("sans-serif-medium",Typeface.BOLD)
            })
        }
        detail("Level $currentLevel   ·   Prolazi ${g.passed}/${g.level.gates.size}")
        detail("Težina: ${progress.difficultyNames[g.difficulty]} · ${progress.playerName()}")
        detail("${LevelEngine.collectibleIcons[currentWorld]}  ${g.coins+g.stars}   ·   ● ${progress.coins()} kovanica")
        panel.addView(stats,LinearLayout.LayoutParams(-1,-2).apply{topMargin=d(9);bottomMargin=d(15)})
        action(panel,"▶  PONOVO"){startGame(currentWorld,currentLevel)}
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
        action(b,"🎨  BOJE BOPIJA",false){showSkins()}
        back(b){showSettings()}
    }
    private fun showSkins(){
        val b=base("LIKOVI","Skupljaj kovanice i otključaj nove Bopijeve boje")
        small(b,"Stanje: ${progress.coins()} kovanica")
        for(i in 0..5){
            val label=when {progress.skin()==i->"✓ ODABRAN";progress.owned(i)->"OTKLJUČAN";else->"${progress.costs[i]} kovanica"}
            action(b,"🐦 ${progress.skinNames[i]}  ·  $label",progress.skin()==i){
                if(!progress.selectOrBuy(i))Toast.makeText(this,"Nema dovoljno kovanica.",Toast.LENGTH_LONG).show()
                showSkins()
            }
        }
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
        val b=base("POSTAVKE","Sve opcije, jednostavno na jednom mjestu")
        sectionHeading(b,"IZGLED I ZVUK")
        val low=Switch(this).apply{text="Nježnije animacije";setTextColor(Color.WHITE);isChecked=progress.lessMotion();setOnCheckedChangeListener{_,v->progress.setLessMotion(v)}}
        b.addView(low)
        val audio=Switch(this).apply{text="Glazba i zvučni efekti";setTextColor(Color.WHITE);isChecked=progress.soundEnabled();setOnCheckedChangeListener{_,v->progress.setSoundEnabled(v);sound.enabled=v}}
        b.addView(audio)
        val haptic=Switch(this).apply {
            text="Vibracije pri igranju"
            setTextColor(Color.WHITE)
            isChecked=progress.hapticEnabled()
            setOnCheckedChangeListener { _,checked -> progress.setHapticEnabled(checked) }
        }
        b.addView(haptic)
        sectionHeading(b,"IGRAČ I TEŽINA")
        small(b,"TEŽINA IGRE — utječe na brzinu i gravitaciju")
        val modes=android.widget.RadioGroup(this).apply{orientation=LinearLayout.VERTICAL}
        for(mode in 0..2) {
            val item=android.widget.RadioButton(this).apply {
                text=progress.difficultyNames[mode]
                setTextColor(Color.WHITE)
                textSize=17f
                id=View.generateViewId()
                isChecked=progress.difficulty()==mode
                setOnClickListener { progress.setDifficulty(mode) }
            }
            modes.addView(item,LinearLayout.LayoutParams(-1,d(48)))
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
        }
        b.addView(player,LinearLayout.LayoutParams(-1,d(52)))
        action(b,"SPREMI IME",false){
            progress.setPlayerName(player.text.toString())
            Toast.makeText(this,"Ime je spremljeno na uređaju.",Toast.LENGTH_SHORT).show()
        }
        sectionHeading(b,"DODATNE OPCIJE")
        action(b,"🐤  IZGLED BOPIJA",false){showSkins()}
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
            if(requestCode==43){val input=contentResolver.openInputStream(data.data!!) ?: error("Datoteka nije dostupna.");val text=input.bufferedReader().use{it.readText().take(550001)};progress.importJson(text);sound.enabled=progress.soundEnabled()}
            Toast.makeText(this,"Napredak uspješno ${if(requestCode==42)"izvezen" else "uvezen"}.",Toast.LENGTH_LONG).show()
            if(requestCode==43)showHome()
        }catch(e:Exception){Toast.makeText(this,"Pogreška: ${e.message}",Toast.LENGTH_LONG).show()}
    }
    @Deprecated("Back navigation compatibility")
    override fun onBackPressed(){if(selectedScreen=="game"){gameView?.paused=true;showWorlds()}else if(selectedScreen=="BOPAVI")super.onBackPressed() else showHome()}
}
