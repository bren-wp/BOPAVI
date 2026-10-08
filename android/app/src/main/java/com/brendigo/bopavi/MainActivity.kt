package com.brendigo.bopavi

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
    private fun d(n:Int):Int = (resources.displayMetrics.density*n+.5f).toInt()
    override fun onCreate(state: Bundle?) { super.onCreate(state);progress=ProgressStore(this);sound=Soundscape(this);sound.enabled=progress.soundEnabled();showHome() }
    private fun showNativeView(root: View) {
        setContentView(root)
        // Android 15 edge-to-edge: reserve system-bar insets for physical controls.
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener { v, insets ->
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
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
    private fun gradient(a:Int,b:Int,rad:Int=20):GradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(a,b)).apply{cornerRadius=d(rad).toFloat();setStroke(d(2),0x6688daff)}
    private fun base(label:String,subtitle:String):LinearLayout {
        gameView?.paused=true;gameView=null;sound.stop();selectedScreen=label
        val body=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(d(18),d(16),d(18),d(20));background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xff071634.toInt(),0xff164881.toInt(),0xff1583bf.toInt())) }
        val scroll=ScrollView(this).apply {isFillViewport=true;addView(body)}
        showNativeView(scroll)
        title(body,label,31,0xffffc44a.toInt())
        title(body,subtitle,15,0xffd5efff.toInt())
        return body
    }
    private fun title(parent:LinearLayout,s:String,size:Int,color:Int=textColor) {
        parent.addView(TextView(this).apply {text=s;textSize=size.toFloat();setTextColor(color);typeface=Typeface.create("sans-serif-black",Typeface.BOLD);gravity=Gravity.CENTER;setPadding(d(3),d(10),d(3),d(10))},LinearLayout.LayoutParams(-1,-2))
    }
    private fun small(parent:LinearLayout,s:String){title(parent,s,16,0xffbce6ff.toInt())}
    private fun action(parent:LinearLayout,text:String,primary:Boolean=true,onClick:()->Unit) {
        val button=Button(this).apply {
            this.text=text;setTextColor(Color.WHITE);textSize=17f;isAllCaps=false;typeface=Typeface.DEFAULT_BOLD
            background=if(primary) gradient(0xff60d94e.toInt(),0xff109b50.toInt()) else gradient(0xff26aeef.toInt(),0xff1265af.toInt())
            setOnClickListener{sound.effect("click");onClick()}
        }
        parent.addView(button,LinearLayout.LayoutParams(-1,d(54)).apply {setMargins(0,d(6),0,d(6))})
    }
    private fun back(parent:LinearLayout,onClick:()->Unit) = action(parent,"‹  Natrag",false,onClick)
    private fun showHome(){
        val b=base("BOPAVI","Mali let, velika avantura")
        val hero=ImageView(this).apply{setImageResource(R.drawable.hero);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Bopi, plava ptica s pilotskim naočalama"}
        b.addView(hero,LinearLayout.LayoutParams(-1,d(180)))
        small(b,"Tapni, poleti i otkrivaj čudesne svjetove.")
        title(b,"● ${progress.coins()} kovanica",22,0xffffdd70.toInt())
        action(b,"▶  IGRAJ") {
            val w=progress.chosenWorld()
            startGame(w,progress.streamFrontier(w))
        }
    }
    private fun showWorlds(){
        val b=base("SVJETOVI","Odaberi svoj sljedeći let")
        for(w in 0 until 8){
            val accessible=w<=progress.maxWorld()
            action(b,"${w+1}. ${LevelEngine.names[w]}  ${if(accessible)"• ${progress.streamFrontier(w)-1} riješeno" else "🔒"}",accessible){
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
        for(n in start..minOf(start+19,LevelEngine.LEVELS_PER_WORLD)) {
            val open=n<=maxNumber
            action(b,"${if(open)"✦" else "🔒"}  Level $n  ·  Zona ${LevelEngine.create(world,n).zone}",false){if(open)startGame(world,n.toLong()) else Toast.makeText(this,"Level je zaključan.",Toast.LENGTH_SHORT).show()}
        }
        action(b,"← Prethodnih 20",false){showLevels(world,(start-20).coerceAtLeast(1))}
        action(b,"Sljedećih 20 →",false){showLevels(world,(start+20).coerceAtMost(LevelEngine.LEVELS_PER_WORLD))}
        back(b){showWorlds()}
    }
    private fun startGame(world:Int,number:Long){
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
            text="Ⅱ";textSize=23f;setTextColor(Color.WHITE);background=gradient(0xff246dd7.toInt(),0xff113f87.toInt());contentDescription="Izbornik tijekom igre"
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
        title(b,"🐦",52)
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
