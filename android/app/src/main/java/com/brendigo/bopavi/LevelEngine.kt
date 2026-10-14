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
                x = 560f + i * (242f - zone * 2), center = center,
                // A rhythm of tall/short pillars: the opening changes per obstacle,
                // not by an almost invisible 0–3px. Intro gates stay forgiving.
                // Gap >=164 before difficulty/movement; hard mode remains >=145.
                gap = (gap + (r.next() - .5f) *
                    (if(i < 2) 12f else 30f + zone * 2f) +
                    (if(i % 5 == 0) 8f else 0f)).coerceAtLeast(164f),
                // Nine seeded widths. Even the widest pillar leaves a reachable horizontal approach.
                width = 48f + ((i * 5 + (r.next() * 9f).toInt()) % 9) * 5f,
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
    val codes:List<String> = catalog.map { it[0] }
    val names:List<String> = catalog.map { it[1] }
    fun normalized(code:String):String=if(code in codes)code else "en"
    fun name(code:String):String=names[codes.indexOf(normalized(code))]
    // Legacy UI text keys are retained while English becomes the default
    // presentation language. Replacement is longest-first to avoid overlap.
    private val english=listOf(
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
    fun text(original:String,code:String):String {
        val locale=normalized(code)
        var result=original
        for((source,value) in english)result=result.replace(source,value)
        if(locale=="en")return result
        if(locale=="hr")return original
        val row=catalog[codes.indexOf(locale)]
        for(i in keys.indices)result=result.replace(keys[i],row[i+2])
        return result
    }
}
