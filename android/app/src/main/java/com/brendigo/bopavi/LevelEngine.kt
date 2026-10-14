package com.brendigo.bopavi

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Platform-independent gameplay rules. Every level is generated on demand. */
object LevelEngine {
    const val WORLD_COUNT = 8
    const val LEVELS_PER_WORLD = 1_048_576
    const val TOTAL_LEVELS = WORLD_COUNT * LEVELS_PER_WORLD
    val names = listOf("Zeleni otoci", "Kristalno nebo", "Lava vrhovi", "Noćni dvorci", "Biserno more", "Nebeske kule", "Čarobna šuma", "Zvjezdani bezdani")
    val collectibles = listOf("Zvjezdice", "Kristali", "Iskre", "Mjesečev prah", "Školjke", "Perje", "Vilinski prah", "Zvjezdani prah")
    val collectibleIcons = listOf("★", "◆", "✦", "☾", "◉", "❖", "✧", "✧")
    val hazards = listOf("trnje", "kristali", "lava", "sjene", "valovi", "stupovi", "trnje", "meteori")
    data class Gate(val x: Float, val center: Float, val gap: Float, val width: Float, val movement: Float, val phase: Float, val kind: Int, val coin: Boolean, val star: Boolean, val power: Int)
    data class Level(val world: Int, val number: Int, val zone: Int, val speed: Float, val wind: Float, val gates: List<Gate>, val type: Int)
    data class Opening(val top: Float, val bottom: Float)
    private class Random32(seed: UInt) {
        var state = if (seed == 0u) 0x6d2b79f5u else seed
        fun next(): Float {
            var x = state
            x = x xor (x shl 13); x = x xor (x shr 17); x = x xor (x shl 5)
            state = x
            return ((x shr 8).toInt() and 0xffffff) / 16777216f
        }
    }
    fun create(world: Int, number: Int): Level {
        require(world in 0 until WORLD_COUNT && number in 1..LEVELS_PER_WORLD)
        val phase = (number - 1) % 360
        val zone = phase / 60 // six rotating challenge tiers every 360 levels
        val epoch = (number - 1) / 360
        val type = if (phase % 60 == 59) 3 else if (phase % 30 == 29) 2 else if (phase % 15 == 14) 1 else 0
        val r = Random32((number.toUInt() * 0x9e3779b9u) xor ((world + 11).toUInt() * 0x85ebca6bu))
        val gap = max(168f, 205f - 5f * zone - world * 1.2f - (epoch % 7) * 0.7f + (if(type == 1) 8f else 0f))
        val speed = min(212f, 140f + 8f * zone + world * 2f + (epoch % 6) * 1.4f + (if(type == 2) 5f else 0f))
        val count = 11 + (epoch % 4) + (if (type == 3) 1 else 0)
        val gates = ArrayList<Gate>(count)
        var last = 355f
        for (i in 0 until count) {
            // Ten 2-bit digits give an injective signature for all 2^20 level numbers.
            // No random position is added to these ten centers.
            val center = if (i < 10) 332f + (((number - 1) ushr (i * 2)) and 3) * 26f
                else (last + (r.next() - 0.5f) * 90f).coerceIn(235f, 465f)
            last = center
            val active = (zone >= 2 && i >= 5 && (i + number + world) % 3 == 0) || (type == 2 && i >= 4 && i % 3 == 0) || (world == 4 && i >= 4 && i % 4 == 0)
            gates += Gate(
                x = 420f + i * (228f - zone * 2), center = center,
                // A rhythm of tall/short pillars: the opening changes per obstacle,
                // not by an almost invisible 0–3px. Intro gates stay forgiving.
                // Gap >=164 before difficulty/movement; hard mode remains >=145.
                gap = (gap + (r.next() - .5f) *
                    (if(i < 2) 12f else 30f + zone * 2f) +
                    (if(i % 5 == 0) 8f else 0f)).coerceAtLeast(164f),
                // One substantial, consistent pillar thickness across all eight worlds.
                // Difficulty instead varies top/bottom heights, opening and timing.
                width = 76f,
                movement = if (active) 7f + r.next() * 9f else 0f,
                phase = r.next() * 6.283185f,
                kind = world, coin = i % 3 != 2 || type == 1,
                star = i % 4 == 2 || type == 1,
                power = if (i >= 2 && (number + i + world) % 11 == 0) 1 else if (i >= 2 && (number + i + world) % 13 == 0) 2 else 0
            )
        }
        return Level(world, number, zone + 1, speed, if (zone >= 4 && world % 2 == 1) 13f else 0f, gates, type)
    }
    /** Stream any positive ordinal; later cycles remix gate centers instead of ending a world. */
    fun createStream(world:Int,ordinal:Long):Level {
        require(ordinal>0 && ordinal<Long.MAX_VALUE-1)
        val mapped=((ordinal-1) % LEVELS_PER_WORLD).toInt()+1
        val cycle=(ordinal-1)/LEVELS_PER_WORLD
        val base=create(world,mapped)
        if(cycle==0L)return base
        val salt=(cycle%4096L).toInt()
        val remixed=base.gates.mapIndexed{index,gate ->
            val diff=((salt*37L+index*11L)%25L-12L).toFloat()
            gate.copy(center=(gate.center+diff).coerceIn(220f,475f),phase=gate.phase+salt*.017f)
        }
        return base.copy(gates=remixed)
    }
    fun milestoneReward(number:Long):Int = when {
        number <= 0L -> 0
        number % 60L == 0L -> 200
        number % 30L == 0L -> 100
        number % 15L == 0L -> 50
        number % 5L == 0L -> 20
        else -> 0
    }
    /** The exact same difficulty-aware opening is used by drawing and collision.
     * Easy opens the corridor, Hard tightens it; Bopi's radius never changes.
     */
    fun opening(gate: Gate, time: Float, difficulty: Int = 1): Opening {
        val frequency = when(gate.kind) { 2 -> 0.65f; 4 -> 1.48f; 5 -> 0.48f; 7 -> 1.27f; else -> 1.1f }
        val delta = if (gate.movement > 0f) sin(time * frequency + gate.phase) * gate.movement else 0f
        // Beach tides move gently; lava/crystal/meteor gates pulsate after activation.
        val tide = when(gate.kind) { 1 -> sin(time*.75f + gate.phase)*5f; 5 -> sin(time*.38f + gate.phase)*5f; else -> 0f }
        val middle = gate.center + delta + tide
        val pulse = if(gate.movement > 0f && (gate.kind == 3 || gate.kind == 6 || gate.kind == 7))
            (sin(time * 1.35f + gate.phase)+1f)*2.5f else 0f
        // The same geometry drives drawing and collision; ≥145px always open.
        val difficultyPadding = when(difficulty.coerceIn(0,2)) {
            0 -> 18f
            2 -> -16f
            else -> 0f
        }
        val half = max(72.5f, (gate.gap + difficultyPadding) * .5f - pulse)
        return Opening(middle - half, middle + half)
    }
    fun signature(level: Level): String = level.gates.take(10).joinToString(":") { it.center.toInt().toString() }
    /** Fixed first-clear grants. No repeat-farming, no in-app payments. */
    fun milestoneReward(number:Int):Int = milestoneReward(number.toLong())
}


/** Presentation-only deterministic drift shared with Swift. Never modifies collisions. */
internal object ParallaxScenery {
    private const val PERIOD = 696f // Four tiles of 174 logical pixels.
    fun offset(distance: Float, layer: Int, reducedMotion: Boolean): Float {
        if (reducedMotion || !distance.isFinite()) return 0f
        val speed = when(layer) { 0 -> .07f; 1 -> .15f; else -> .24f }
        val travelled = (distance * speed) % PERIOD
        return ((travelled + PERIOD) % PERIOD)
    }
}

/** A truthful result headline based on the best score BEFORE saving this run. */
internal object ResultHeadline {
    fun label(score:Int, previousBest:Int, won:Boolean):String = when {
        won -> "LEVEL DOVRŠEN!"
        score > 0 && score > previousBest -> "NOVI REKORD!"
        else -> "LET ZAVRŠEN!"
    }
}

/** Names/icons for the generator's four existing level types, with no gameplay changes. */
internal object LevelKind {
    fun name(type:Int):String = when(type) {
        1 -> "Izazovni"
        2 -> "Bonus"
        3 -> "Elitni"
        else -> "Normalni"
    }
    fun icon(type:Int):String = when(type) {
        1 -> "⚡"
        2 -> "✦"
        3 -> "♛"
        else -> "●"
    }
}

/** Offline UI locale catalog (26 choices; the remaining long-form pages use English fallback). */
object GameLanguages {
    private val keys=listOf("PLAY","WORLDS","SETTINGS","PROFILE","TASKS","COLLECTION","SHOP","LANGUAGE","SOUND","PAUSE")
    private val rows=listOf(
        "en|English|Play|Worlds|Settings|Profile|Tasks|Collection|Shop|Language|Sound|Pause",
        "hr|Hrvatski|Igraj|Svjetovi|Postavke|Profil|Zadaci|Kolekcija|Trgovina|Jezik|Zvuk|Pauza",
        "de|Deutsch|Spielen|Welten|Einstellungen|Profil|Aufgaben|Sammlung|Shop|Sprache|Ton|Pause",
        "fr|Français|Jouer|Mondes|Paramètres|Profil|Missions|Collection|Boutique|Langue|Son|Pause",
        "es|Español|Jugar|Mundos|Ajustes|Perfil|Tareas|Colección|Tienda|Idioma|Sonido|Pausa",
        "it|Italiano|Gioca|Mondi|Impostazioni|Profilo|Compiti|Collezione|Negozio|Lingua|Audio|Pausa",
        "pt|Português|Jogar|Mundos|Definições|Perfil|Tarefas|Coleção|Loja|Idioma|Som|Pausa",
        "nl|Nederlands|Spelen|Werelden|Instellingen|Profiel|Taken|Verzameling|Winkel|Taal|Geluid|Pauze",
        "pl|Polski|Graj|Światy|Ustawienia|Profil|Zadania|Kolekcja|Sklep|Język|Dźwięk|Pauza",
        "cs|Čeština|Hrát|Světy|Nastavení|Profil|Úkoly|Sbírka|Obchod|Jazyk|Zvuk|Pauza",
        "sk|Slovenčina|Hrať|Svety|Nastavenia|Profil|Úlohy|Zbierka|Obchod|Jazyk|Zvuk|Pauza",
        "sl|Slovenščina|Igraj|Svetovi|Nastavitve|Profil|Naloge|Zbirka|Trgovina|Jezik|Zvok|Premor",
        "hu|Magyar|Játék|Világok|Beállítások|Profil|Feladatok|Gyűjtemény|Bolt|Nyelv|Hang|Szünet",
        "ro|Română|Joacă|Lumi|Setări|Profil|Sarcini|Colecție|Magazin|Limbă|Sunet|Pauză",
        "bg|Български|Играй|Светове|Настройки|Профил|Задачи|Колекция|Магазин|Език|Звук|Пауза",
        "el|Ελληνικά|Παίξε|Κόσμοι|Ρυθμίσεις|Προφίλ|Αποστολές|Συλλογή|Κατάστημα|Γλώσσα|Ήχος|Παύση",
        "tr|Türkçe|Oyna|Dünyalar|Ayarlar|Profil|Görevler|Koleksiyon|Mağaza|Dil|Ses|Duraklat",
        "uk|Українська|Грати|Світи|Налаштування|Профіль|Завдання|Колекція|Магазин|Мова|Звук|Пауза",
        "sr|Српски|Играј|Светови|Подешавања|Профил|Задаци|Колекција|Продавница|Језик|Звук|Пауза",
        "bs|Bosanski|Igraj|Svjetovi|Postavke|Profil|Zadaci|Kolekcija|Trgovina|Jezik|Zvuk|Pauza",
        "sv|Svenska|Spela|Världar|Inställningar|Profil|Uppdrag|Samling|Butik|Språk|Ljud|Paus",
        "nb|Norsk|Spill|Verdener|Innstillinger|Profil|Oppgaver|Samling|Butikk|Språk|Lyd|Pause",
        "da|Dansk|Spil|Verdener|Indstillinger|Profil|Opgaver|Samling|Butik|Sprog|Lyd|Pause",
        "fi|Suomi|Pelaa|Maailmat|Asetukset|Profiili|Tehtävät|Kokoelma|Kauppa|Kieli|Ääni|Tauko",
        "ja|日本語|プレイ|ワールド|設定|プロフィール|タスク|コレクション|ショップ|言語|サウンド|一時停止",
        "ko|한국어|플레이|월드|설정|프로필|과제|컬렉션|상점|언어|사운드|일시정지"
    )
    private val catalog=rows.map { it.split("|") }

    /** Eight actually localized gameplay labels for every advertised picker locale.
     * Precompute once per GameView so no dictionaries are built inside 60 Hz draw.
     */
    private val flightKeys=listOf("SCORE","DIST","LEVEL","NEW STAGE","GATES","FLIGHT","TAP TO FLY","Bopi is flying")
    private val flightRows=listOf(
        "en|SCORE|DIST|LEVEL|NEW STAGE|GATES|FLIGHT|TAP TO FLY|Bopi is flying",
        "hr|BODOVI|PUT|RAZINA|NOVA RAZINA|PROLAZI|LET|DODIRNI ZA LET|Bopi leti",
        "de|PUNKTE|STRECKE|LEVEL|NEUE STUFE|TORE|FLUG|ZUM FLIEGEN TIPPEN|Bopi fliegt",
        "fr|SCORE|DIST.|NIVEAU|NOUVEAU NIVEAU|PORTES|VOL|TOUCHE POUR VOLER|Bopi vole",
        "es|PUNTOS|DIST.|NIVEL|NIVEL NUEVO|PUERTAS|VUELO|TOCA PARA VOLAR|Bopi vuela",
        "it|PUNTI|DIST.|LIVELLO|NUOVO LIVELLO|VARCHI|VOLO|TOCCA PER VOLARE|Bopi vola",
        "pt|PONTOS|DIST.|NÍVEL|NOVO NÍVEL|PASSAGENS|VOO|TOQUE PARA VOAR|Bopi voa",
        "nl|SCORE|AFST.|LEVEL|NIEUW LEVEL|POORTEN|VLUCHT|TIK OM TE VLIEGEN|Bopi vliegt",
        "pl|PUNKTY|DYST.|POZIOM|NOWY POZIOM|BRAMY|LOT|DOTKNIJ, ABY LECIEĆ|Bopi leci",
        "cs|BODY|VZDÁL.|ÚROVEŇ|NOVÁ ÚROVEŇ|BRÁNY|LET|KLEPNI PRO LET|Bopi letí",
        "sk|BODY|VZD.|ÚROVEŇ|NOVÁ ÚROVEŇ|BRÁNY|LET|ŤUKNI PRE LET|Bopi letí",
        "sl|TOČKE|RAZD.|STOPNJA|NOVA STOPNJA|PREHODI|LET|DOTAKNI SE ZA LET|Bopi leti",
        "hu|PONT|TÁV.|SZINT|ÚJ SZINT|KAPUK|REPÜLÉS|ÉRINTSD MEG|Bopi repül",
        "ro|SCOR|DIST.|NIVEL|NIVEL NOU|PORȚI|ZBOR|ATINGE PENTRU ZBOR|Bopi zboară",
        "bg|ТОЧКИ|РАЗСТ.|НИВО|НОВО НИВО|ПОРТИ|ПОЛЕТ|ДОКОСНИ ЗА ПОЛЕТ|Бопи лети",
        "el|ΣΚΟΡ|ΑΠΟΣΤ.|ΕΠΙΠΕΔΟ|ΝΕΟ ΕΠΙΠΕΔΟ|ΠΥΛΕΣ|ΠΤΗΣΗ|ΠΑΤΑ ΓΙΑ ΠΤΗΣΗ|Ο Μπόπι πετά",
        "tr|PUAN|MES.|SEVİYE|YENİ SEVİYE|KAPILAR|UÇUŞ|UÇMAK İÇİN DOKUN|Bopi uçuyor",
        "uk|БАЛИ|ВІДСТ.|РІВЕНЬ|НОВИЙ РІВЕНЬ|ВОРОТА|ПОЛІТ|ТОРКНИСЬ ДЛЯ ПОЛЬОТУ|Бопі летить",
        "sr|ПОЕНИ|УДАЉ.|НИВО|НОВИ НИВО|ПРОЛАЗИ|ЛЕТ|ДОДИРНИ ЗА ЛЕТ|Бопи лети",
        "bs|BODOVI|PUT|NIVO|NOVI NIVO|PROLAZI|LET|DODIRNI ZA LET|Bopi leti",
        "sv|POÄNG|DIST.|NIVÅ|NY NIVÅ|PORTAR|FLYG|TRYCK FÖR ATT FLYGA|Bopi flyger",
        "nb|POENG|DIST.|NIVÅ|NYTT NIVÅ|PORTER|FLYG|TRYKK FOR Å FLY|Bopi flyr",
        "da|POINT|DIST.|NIVEAU|NYT NIVEAU|PORTE|FLYV|TRYK FOR AT FLYVE|Bopi flyver",
        "fi|PISTEET|MATKA|TASO|UUSI TASO|PORTIT|LENTO|NAPAUTA LENTÄÄKSESI|Bopi lentää",
        "ja|スコア|距離|レベル|新しいレベル|ゲート|飛行|タップして飛ぶ|ボピが飛行中",
        "ko|점수|거리|레벨|새로운 단계|통로|비행|탭하여 비행|보피가 날고 있어요"
    ).map { it.split("|") }
    fun hudPack(code:String):Map<String,String> {
        val row=flightRows[codes.indexOf(normalized(code))]
        return flightKeys.mapIndexed { i,key -> key to row[i+1] }.toMap()
    }
    val codes:List<String> = catalog.map { it[0] }
    val names:List<String> = catalog.map { it[1] }
    fun normalized(code:String):String=if(code in codes)code else "en"
    fun name(code:String):String=names[codes.indexOf(normalized(code))]
    // Legacy UI text keys are retained while English becomes the default
    // presentation language. Replacement is longest-first to avoid overlap.
    private val english=listOf(
        "SVE NAGRADE" to "ALL AWARDS",
        "Najbolji rezultat" to "Best score",
        "Osvojene kovanice" to "Coins earned",
        "Dovršeni leveli" to "Levels completed",
        "Pobjede" to "Wins",
        "PRVI LET" to "FIRST FLIGHT",
        "ISTRAŽIVAČ" to "EXPLORER",
        "SAKUPLJAČ" to "COLLECTOR",
        "1 pobjeda" to "1 victory",
        "5 svjetova" to "5 worlds",
        "500 predmeta" to "500 collectibles",
        "10 pobjeda" to "10 victories",
        "osvojeno" to "earned",
        "zaključano" to "locked",
        "PRIVATNOST" to "PRIVACY",
        "IGRAČ" to "PLAYER",
        "★  SVE NAGRADE" to "★  ALL AWARDS",
        "Zeleni otoci" to "Green Islands",
        "Kristalno nebo" to "Crystal Sky",
        "Lava vrhovi" to "Lava Peaks",
        "Noćni dvorci" to "Night Castles",
        "Biserno more" to "Pearl Sea",
        "Nebeske kule" to "Sky Towers",
        "Čarobna šuma" to "Enchanted Forest",
        "Zvjezdani bezdani" to "Star Abyss",
        "LET ZAVRŠEN!" to "FLIGHT OVER!",
        "LEVEL DOVRŠEN!" to "LEVEL COMPLETE!",
        "PRIKUPLJENE\nKOVANICE" to "COLLECTED\nCOINS",
        "UKUPNO\nPROLAZA" to "TOTAL\nGATES",
        "BONUS\nBODOVA" to "BONUS\nPOINTS",
        "PRIKUPLJENE KOVANICE" to "COLLECTED COINS",
        "UKUPNO PROLAZA" to "TOTAL GATES",
        "Najbolji rezultat:" to "Personal best:",
        "Ukupno prolaza:" to "Total gates:",
        "Level " to "Level ",
        "REZULTAT" to "SCORE",
        "KOVANICE" to "COINS",
        "ZVIJEZDE" to "STARS",
        "REKORD" to "RECORD",
        "NAGRADE" to "REWARDS",
        "TVOJI REZULTATI" to "YOUR RESULTS",
        "LJESTVICA" to "LEADERBOARD",
        "PODIJELI" to "SHARE",
        "MAPA SVJETOVA" to "WORLD MAP",
        "PREUZMI" to "CLAIM",
        "Novi osobni rekord:" to "New personal best:",
        " bodova" to " points",
        " kovanica" to " coins",
        "svijet " to "world ",
        "otključano" to "unlocked",
        "otključan" to "unlocked",
        "zaključan" to "locked",
        "dovršen" to "completed",
        "zvjezdice " to "stars ",
        "upravljenje dodirom" to "tap controls",
        "upravljanje dodirom" to "tap controls",
        "odabrano" to "selected",
        "prikupljeno " to "collected ",
        "rekord " to "record ",
        "ZONA " to "ZONE ",
        "STRANICA " to "PAGE ",
        "ZAKLJUČANI LEVELI" to "LOCKED LEVELS",
        "ODABERITE LEVEL" to "SELECT LEVEL",
        "Odaberite level" to "Select a level",
        "TAP ZA ZAMAH" to "TAP TO FLAP",
        "NORMALNI" to "NORMAL",
        "IZAZOVNI" to "CHALLENGE",
        "ELITNI" to "ELITE",
        "Razina" to "Level",
        "Početna" to "Home",
        "Težina" to "Difficulty",
        "Opušteno" to "Easy",
        "Standardno" to "Normal",
        "Izazovno" to "Hard",
        "BONUS BODOVA" to "BONUS POINTS",
        "ZA REKORD" to "FOR YOUR RECORD",
        "ZA BODOVE" to "FOR POINTS",
        "Prvo dovrši" to "Complete first",
        "Igrač" to "Player",
        "Vrijeme" to "Time",
        "REKORDI" to "RECORDS",
        "KOVANICA ZA REKORD" to "RECORD COINS",
        "OPREMA ZA BOPIJEV LET" to "EQUIPMENT FOR BOPI FLIGHT",
        "KUPI ŠTIT" to "BUY SHIELD",
        "KUPI MAGNET" to "BUY MAGNET",
        "UČITAVANJE..." to "LOADING...",
        "Osam različitih avantura" to "Eight distinct adventures",
        "PREGLED ZAKLJUČANIH LEVELA" to "LOCKED LEVEL PREVIEW",
        "Otključano do levela" to "Unlocked through level",
        "Broj otključanog levela" to "Unlocked level number",
        "Level mora biti otključan i unutar raspona." to "The level must be unlocked and in range.",
        "Ovi leveli su zaključani. Dovrši prethodne za otključavanje." to "These levels are locked. Complete earlier levels to unlock them.",
        "VRATI NA OTKLJUČANE LEVELE" to "RETURN TO UNLOCKED LEVELS",
        "Prvo dovrši prethodni level." to "Complete the previous level first.",
        "Prvo dovrši prethodni level" to "Complete the previous level first",
        "Pauza je dostupna nakon početka leta; izbjegavaj prepreke i skupljaj nagrade." to "Pause is available once you start flying. Dodge obstacles and collect rewards.",
        "Za svakih novih 1.000 bodova najboljeg rezultata dobivaš 1 kovanicu." to "Earn one coin for each additional 1,000 points in your best score.",
        "Osvajaj kovanice prelaskom nagradnih levela i biraj opremu za svoj sljedeći let." to "Earn coins by completing reward levels and prepare equipment for your next flight.",
        "Privlači predmete tijekom 8 sekundi." to "Attract collectibles for eight seconds.",
        "Kupljena oprema automatski se koristi na početku sljedećeg leta." to "Purchased equipment activates automatically at the start of your next flight.",
        "Tvoj letački dnevnik i osobni rezultati" to "Your flight journal and personal records",
        "Prvi rekord čeka tvoj sljedeći let!" to "Your first record awaits your next flight!",
        "TEŽINA IGRE — utječe na brzinu i gravitaciju" to "GAME DIFFICULTY — affects speed and gravity",
        "Težina se primjenjuje na sljedeći let. Dosadašnji napredak ostaje spremljen." to "Difficulty changes apply to the next flight. Your progress stays saved.",
        "Glazba i zvučni efekti" to "Music and sound effects",
        "Bez oglasa, praćenja i kupnje stvarnim novcem." to "No ads, tracking or real-money purchases.",
        "Ime igrača je promijenjeno." to "Player name updated.",
        "Kopiju nije moguće otvoriti ili spremiti." to "Unable to open or save the backup.",
        "Nježnije animacije" to "Gentler animations",
        "Uravnotežen izazov" to "Balanced challenge",
        "Opušten let" to "Relaxed flight",
        "Uspješno" to "Successful",
        "Stanje novčanika:" to "Wallet balance:",
        "Letački profil," to "Flight profile,",
        "Odabrani letač:" to "Selected pilot:",
        "Težina igre" to "Game difficulty",
        "Težina:" to "Difficulty:",
        "BOPAVI, učitavanje igre" to "BOPAVI, loading game",
        "Pogledaj sva postignuća" to "View all achievements",
        "Dovršeni leveli" to "Completed levels",
        "Pokušaji bez pobjede:" to "Unsuccessful attempts:",
        "Pobjede:" to "Victories:",
        "Pronađi" to "Find",
        "kupnja nije moguća" to "purchase unavailable",
        "Kupnja nije moguća" to "Purchase unavailable",
        "Prilagodi igru svom stilu" to "Make the game your own",
        "ZVJEZDICE" to "STARS",
        "Zvjezdice" to "Stars",
        "KVANICE" to "COINS",
        "Brzina" to "Speed",
        "UPRAVLJANJE" to "HANDLING",
        "Početni štit" to "Starting shield",
        "Početni magnet" to "Starting magnet",
        "SVI REZULTATI" to "ALL RESULTS",
        " isključeno" to " off",
        " uključeno" to " on",
        " isključena" to " off",
        " uključena" to " on",
        "zaključano" to "locked",
        "otključano" to "unlocked",
        "kovanica" to "coins",
        "Kovanica" to "Coins",
        "najbolji rezultat" to "best score",
        "prikupljeno" to "collected",
        "rekord" to "record",
        "od 30" to "out of 30",
        "Otvorite levele" to "Open levels",
        "Otvori levele" to "Open levels",
        "ZADACI I POSTIGNUĆA" to "TASKS & ACHIEVEMENTS",
        "TRGOVINA KOVANICAMA" to "COIN SHOP",
        "NAPREDNE POSTAVKE" to "ADVANCED SETTINGS",
        "MANJE ANIMACIJA" to "REDUCED MOTION",
        "POLITIKA PRIVATNOSTI" to "PRIVACY POLICY",
        "UVJETI KORIŠTENJA" to "TERMS OF USE",
        "POČETNA" to "HOME",
        "POVRATAK" to "RETURN",
        "SPREMI KOPIJU NAPRETKA" to "EXPORT PROGRESS",
        "VRATI NAPREDAK IZ KOPIJE" to "IMPORT PROGRESS",
        "SPREMI IME" to "SAVE NAME",
        "UREDI PROFIL" to "EDIT PROFILE",
        "MOJE NAGRADE" to "MY AWARDS",
        "MOJI NAJBOLJI SVJETOVI" to "MY TOP WORLDS",
        "MOJI REKORDI" to "MY RECORDS",
        "MOJA POSTIGNUĆA" to "MY ACHIEVEMENTS",
        "IGRAČ I TEŽINA" to "PLAYER & DIFFICULTY",
        "PODACI I PRIVATNOST" to "DATA & PRIVACY",
        "IZGLED I ZVUK" to "APPEARANCE & SOUND",
        "DODATNE OPCIJE" to "EXTRA OPTIONS",
        "ODABERI LIKA" to "CHOOSE A PILOT",
        "ODABERI SVJET" to "CHOOSE A WORLD",
        "RAZINE" to "LEVELS",
        "LEVELI" to "LEVELS",
        "RAZINA" to "LEVEL",
        "OSTVARENI REZULTAT" to "YOUR SCORE",
        "OSOBNI REKORD" to "PERSONAL BEST",
        "NOVI REKORD" to "NEW RECORD",
        "DOVRŠENO" to "COMPLETED",
        "POREDAK" to "RANKING",
        "PRETHODNIH" to "PREVIOUS",
        "SLJEDEĆIH" to "NEXT",
        "SLJEDEĆI" to "NEXT",
        "PRETHODNI" to "PREVIOUS",
        "NASTAVI LET" to "CONTINUE FLIGHT",
        "POLETI" to "FLY",
        "POČETNI ŠTIT" to "STARTING SHIELD",
        "POČETNI MAGNET" to "STARTING MAGNET",
        "PONOVO" to "RETRY",
        "IZLAZ" to "EXIT",
        "NATRAG" to "BACK",
        "KONTROLE" to "CONTROLS",
        "MUZIKA" to "MUSIC",
        "GLAZBA" to "MUSIC",
        "VIBRACIJA" to "VIBRATION",
        "VIBRACIJE" to "VIBRATION",
        "EFEKTI" to "EFFECTS",
        "UKLJ." to "ON",
        "ISKL." to "OFF",
        "PROČITAJ" to "READ",
        "POSTIGNUĆA" to "ACHIEVEMENTS",
        "REKORDI" to "RECORDS",
        "KOLEKCIJA" to "COLLECTION",
        "ZADACI" to "TASKS",
        "PROFIL" to "PROFILE",
        "TRGOVINA" to "SHOP",
        "SVJETOVI" to "WORLDS",
        "POSTAVKE" to "SETTINGS",
        "IGRAJ" to "PLAY",
        "JEZIK" to "LANGUAGE",
        "ZVUK" to "SOUND",
        "PAUZA" to "PAUSE",
        "KOVANICE" to "COINS",
        "KOVANICA" to "COINS",
        "BODOVA" to "POINTS",
        "NAGRADE" to "REWARDS",
        "OSVOJENO" to "EARNED",
        "ZAKLJUČANO" to "LOCKED",
        "DOSTUPNO" to "AVAILABLE",
        "LAGANO" to "EASY",
        "NORMALNO" to "NORMAL",
        "ZAHTJEVNO" to "HARD",
        "DODIRNI ZA LET" to "TAP TO FLY"
    )
    private val sortedEnglish=english.sortedByDescending { it.first.length }
    fun text(original:String,code:String):String {
        val locale=normalized(code)
        var result=original
        for((source,value) in sortedEnglish)result=result.replace(source,value)
        if(locale=="en")return result
        if(locale=="hr")return original
        val row=catalog[codes.indexOf(locale)]
        for(i in keys.indices)result=result.replace(keys[i],row[i+2])
        return result
    }
}
