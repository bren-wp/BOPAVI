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
import java.io.BufferedReader
import java.io.InputStreamReader

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
        if (android.os.Build.VERSION.SDK_INT >= 35 && root !is FrameLayout) {
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
    private fun gradient(a:Int,b:Int,rad:Int=20):GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(a,b)).apply{
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
        background=gradient(0xff192d50.toInt(),0xff203c65.toInt(),18)
        contentDescription=label
    }
    private fun base(label:String,subtitle:String):LinearLayout {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=0
        gameView?.paused=true;gameView=null;sound.stop();selectedScreen=label
        val body=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(d(20),d(22),d(20),d(30))
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xff081326.toInt(),0xff122c4b.toInt(),0xff0b4261.toInt()))
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
    private fun action(parent:LinearLayout,text:String,primary:Boolean=true,onClick:()->Unit) {
        val button=Button(this).apply {
            this.text=text;setTextColor(Color.WHITE)
            textSize=17f;isAllCaps=false
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            letterSpacing=.025f
            setAutoSizeTextTypeUniformWithConfiguration(12,17,1,android.util.TypedValue.COMPLEX_UNIT_SP)
            val normal=if(primary) gradient(0xff93f952.toInt(),0xff19b747.toInt(),19)
                else gradient(0xff257ce0.toInt(),0xff123f9a.toInt(),19)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),normal,null)
            elevation=d(4).toFloat()
            contentDescription=text
            setOnClickListener{sound.effect("click");onClick()}
        }
        parent.addView(button,LinearLayout.LayoutParams(-1,d(58)).apply {setMargins(0,d(7),0,d(7))})
    }
    private fun back(parent:LinearLayout,onClick:()->Unit) = action(parent,"‹  Natrag",false,onClick)
    private fun worldTile(parent:LinearLayout,world:Int,onClick:()->Unit){
        val unlocked=world<=progress.maxWorld()
        val name=LevelEngine.names[world]
        val row=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            background=gradient(0xff203c5a.toInt(),0xff10233d.toInt(),20)
            setPadding(d(16),d(14),d(16),d(14))
            alpha=if(unlocked)1f else .58f
            elevation=d(3).toFloat()
            isClickable=true;isFocusable=true
            contentDescription="$name, "+if(unlocked)"otključano" else "zaključano"
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
        row.addView(preview,LinearLayout.LayoutParams(-1,d(118)).apply{bottomMargin=d(10)})
        row.addView(TextView(this).apply{
            text="${LevelEngine.collectibleIcons[world]}  $name   ${if(unlocked) "↗" else "🔒"}"
            textSize=19f;setTextColor(worldAccents[world]);typeface=Typeface.DEFAULT_BOLD
        })
        row.addView(TextView(this).apply{
            text=if(unlocked)"Level ${progress.streamFrontier(world)} · ${LevelEngine.collectibles[world]}" else "Otkrij novi svijet tijekom igranja"
            textSize=13f;setTextColor(0xffd0e6f5.toInt())
            setPadding(0,d(5),0,0)
        })
        parent.addView(row,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,d(7),0,d(7))})
    }
    private fun showHome(){
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=0
        gameView?.paused=true;gameView=null;sound.stop();selectedScreen="home"
        // Full-bleed illustrated home, rather than a small banner in a dark scroll page.
        val background=FrameLayout(this).apply {setBackgroundColor(0xff57c8f7.toInt())}
        val scene=ImageView(this).apply {
            setImageResource(R.drawable.hero)
            scaleType=ImageView.ScaleType.CENTER_CROP
            contentDescription="Nebeski krajolik s Bopijem i lebdećim otocima"
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        background.addView(scene,FrameLayout.LayoutParams(-1,-1))
        val shading=View(this).apply{
            this.background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x660a52a5,0x000b6cbb,0x330b6cbb,0xaa043979.toInt()))
        }
        background.addView(shading,FrameLayout.LayoutParams(-1,-1))
        val layout=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(d(22),d(14),d(22),d(28))
        }
        background.addView(layout,FrameLayout.LayoutParams(-1,-1))
        val logo=ImageView(this).apply {
            setImageResource(R.drawable.logo)
            scaleType=ImageView.ScaleType.FIT_CENTER
            contentDescription="BOPAVI — Mali let, velika avantura"
        }
        layout.addView(logo,LinearLayout.LayoutParams(-1,d(116)))
        val message=TextView(this).apply{
            text="MALI LET, VELIKA AVANTURA"
            gravity=Gravity.CENTER
            textSize=15f;setTextColor(Color.WHITE)
            typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
            setShadowLayer(5f,0f,d(2).toFloat(),0xff064277.toInt())
        }
        layout.addView(message,LinearLayout.LayoutParams(-1,d(40)))
        layout.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))
        layout.addView(chip("●  ${progress.coins()} kovanica"),LinearLayout.LayoutParams(-1,-2).apply{
            setMargins(0,0,0,d(12))
        })
        action(layout,"▶  IGRAJ"){
            val world=progress.chosenWorld()
            startGame(world,progress.streamFrontier(world))
        }
        showNativeView(background)
    }

    private fun showWorlds(){
        val b=base("SVJETOVI","Odaberi svoj sljedeći let")
        for(w in 0 until 8){
            val accessible=w<=progress.maxWorld()
            worldTile(b,w){
                if(accessible) showLevels(w,1) else Toast.makeText(this,"Dovrši 30 levela prethodnog svijeta.",Toast.LENGTH_LONG).show()
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
                if(n>maxNumber)continue
                val cell=Button(this).apply{
                    text="✦\n$n"
                    setTextColor(gold);textSize=14f;isAllCaps=false
                    typeface=Typeface.DEFAULT_BOLD
                    background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),gradient(0xff204d72.toInt(),0xff112942.toInt(),16),null)
                    contentDescription="Level $n"
                    setOnClickListener{sound.effect("click");startGame(world,n.toLong())}
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
        // Immersive gameplay removes the large pale system navigation strip seen in device videos.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        currentWorld=world;currentLevel=number;selectedScreen="game"
        val boosts=progress.consumePerks()
        val simulation=GameSimulation(LevelEngine.createStream(world,number),true,boosts.first,boosts.second,number)
        val frame=FrameLayout(this).apply{setBackgroundColor(0xff092044.toInt())}
        sound.startWorld(world)
        val game=GameView(this,simulation,progress.lessMotion(),progress.skin(),
            onFinished={showResult(it)},
            onLevelCompleted={completed ->
                val amount=progress.completeLevel(world,completed,simulation.score())
                sound.effect("level")
                if(amount>0)Toast.makeText(this,"Level $completed: +$amount kovanica!",Toast.LENGTH_SHORT).show()
            },
            onFlap={sound.effect("tap")},
            onCollect={sound.effect("collect")})
        gameView=game;frame.addView(game,FrameLayout.LayoutParams(-1,-1))
        val pause=Button(this).apply{
            text="Ⅱ";textSize=23f;setTextColor(Color.WHITE)
            background=RippleDrawable(ColorStateList.valueOf(0x55ffffff),gradient(0xff254c79.toInt(),0xff132b51.toInt(),18),null)
            elevation=d(5).toFloat()
            contentDescription="Izbornik tijekom igre"
            setOnClickListener{
                game.paused=true;sound.pause()
                android.app.AlertDialog.Builder(this@MainActivity).setTitle("Pauza")
                    .setItems(arrayOf("Nastavi let","Mapa svjetova","Oprema za kovanice","Izgled Bopija","Zvuk i prikaz")) { _,choice ->
                        sound.effect("click")
                        when(choice){
                            0 -> {game.paused=false;sound.resume()}
                            1 -> showWorlds()
                            2 -> showPerks()
                            3 -> showSkins()
                            4 -> showSettings()
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
        val b=base("Pokušaj ponovno",LevelEngine.names[currentWorld]+" · Level $currentLevel")
        title(b,if(g.shield>0)"✦  BOPI  ✦" else "🐦  BOPAVI  🐦",35,gold)
        small(b,"Prolazi: ${g.passed}/${g.level.gates.size} · Rezultat: ${g.score()}")
        small(b,"${LevelEngine.collectibles[currentWorld]}: ${g.coins+g.stars} · Kovanice: ${progress.coins()}")
        action(b,"▶  PONOVO"){startGame(currentWorld,currentLevel)}
        action(b,"OPREMA ZA KOVANICE",false){showPerks()}
        action(b,"MAPA SVJETOVA",false){showWorlds()}
        action(b,"POČETNI EKRAN",false){showHome()}
    }
    private fun showPerks(){
        val b=base("OPREMA","Pogodnosti kupuješ samo osvojenim kovanicama")
        small(b,"Tvoje kovanice: ${progress.coins()}")
        small(b,"Nagrada: svaki 5., 15., 30. i 60. prvi put dovršen level")
        for(n in 0..1){
            val explanation=if(n==0)"Zaštita od jednog udarca" else "Privlači predmete osam sekundi"
            small(b,"${progress.perkNames[n]} · $explanation · u zalihi ${progress.perkCount(n)}")
            action(b,"KUPI ZA ${progress.perkPrices[n]} KOVANICA",false){
                if(progress.buyPerk(n)){sound.effect("purchase");showPerks()}
                else Toast.makeText(this,"Nedovoljno kovanica ili je zaliha puna.",Toast.LENGTH_SHORT).show()
            }
        }
        small(b,"Kupljene pogodnosti uključuju se automatski pri sljedećem letu.")
        back(b){showWorlds()}
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
        back(b){showWorlds()}
    }
    private fun showAchievements(){
        val b=base("POSTIGNUĆA","Tvoj napredak spremljen je samo na uređaju")
        small(b,"Dovršeni leveli: ${((0..7).sumOf { (progress.frontier(it)-1).toLong() })}")
        small(b,"Pobjede: ${progress.wins()}  ·  Pokušaji bez pobjede: ${progress.deaths()}")
        small(b,"Svijet u kojem si najdalje napredovao: ${LevelEngine.names[progress.maxWorld()]}")
        for(w in 0..7)small(b,"${LevelEngine.names[w]} · najbolji rezultat ${progress.best(w)}")
        back(b){showWorlds()}
    }
    private fun showSettings(){
        val b=base("POSTAVKE","Privatnost, animacije i sigurnosna kopija")
        val low=Switch(this).apply{text="Smanji animacije (30 FPS)";setTextColor(Color.WHITE);isChecked=progress.lessMotion();setOnCheckedChangeListener{_,v->progress.setLessMotion(v)}}
        b.addView(low)
        val audio=Switch(this).apply{text="Glazba i zvučni efekti";setTextColor(Color.WHITE);isChecked=progress.soundEnabled();setOnCheckedChangeListener{_,v->progress.setSoundEnabled(v);sound.enabled=v}}
        b.addView(audio)
        small(b,"Bez oglasa, telemetrije, računa i mrežnih zahtjeva.")
        action(b,"IZVEZI NAPREDAK",false){
            val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json";putExtra(Intent.EXTRA_TITLE,"bopavi-save.json")}
            startActivityForResult(intent,42)
        }
        action(b,"UVEZI NAPREDAK (v0.1–v0.5)",false){
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json"}
            startActivityForResult(intent,43)
        }
        back(b){showWorlds()}
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
