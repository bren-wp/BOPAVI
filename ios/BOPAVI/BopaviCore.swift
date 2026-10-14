import Foundation

// Identical gameplay data model and deterministic generator on iOS and Android.
enum BopaviCore {
    static let worldCount = 8
    static let levelsPerWorld = 1_048_576
    static let totalLevels = worldCount * levelsPerWorld
    static let names = ["Zeleni otoci", "Kristalno nebo", "Lava vrhovi", "Noćni dvorci", "Biserno more", "Nebeske kule", "Čarobna šuma", "Zvjezdani bezdani"]
    static let collectibles = ["Zvjezdice", "Kristali", "Iskre", "Mjesečev prah", "Školjke", "Perje", "Vilinski prah", "Zvjezdani prah"]
    static let collectibleIcons = ["★", "◆", "✦", "☾", "◉", "❖", "✧", "✧"]
    static let hazards = ["trnje", "kristali", "lava", "sjene", "valovi", "stupovi", "trnje", "meteori"]
    struct Gate {
        let x: Float, center: Float, gap: Float, width: Float
        let movement: Float, phase: Float
        let kind: Int, coin: Bool, star: Bool, power: Int
    }
    struct Level {
        let world: Int, number: Int, zone: Int
        let speed: Float, wind: Float
        let gates: [Gate]
        let type: Int
    }
    struct Opening { let top: Float, bottom: Float }
    struct Random32 {
        private var state: UInt32
        init(_ seed: UInt32) { state = seed == 0 ? 0x6d2b79f5 : seed }
        mutating func next() -> Float {
            var x = state
            x ^= x << 13; x ^= x >> 17; x ^= x << 5
            state = x
            return Float((x >> 8) & 0x00ff_ffff) / 16_777_216
        }
    }
    static func create(_ world: Int, _ number: Int) -> Level {
        precondition((0..<worldCount).contains(world) && (1...levelsPerWorld).contains(number))
        let phase = (number-1) % 360
        let zone = phase/60
        let epoch = (number-1)/360
        let type = phase%60 == 59 ? 3 : phase%30 == 29 ? 2 : phase%15 == 14 ? 1 : 0
        var rng = Random32(UInt32(truncatingIfNeeded: number) &* 0x9e3779b9 ^ UInt32(world+11) &* 0x85ebca6b)
        let gap = max(168, 205 - Float(5*zone) - Float(world)*1.2 - Float(epoch%7)*0.7 + (type == 1 ? 8 : 0))
        let speed = min(212, 140 + Float(8*zone) + Float(world)*2 + Float(epoch%6)*1.4 + (type == 2 ? 5 : 0))
        let count = 11 + epoch%4 + (type == 3 ? 1 : 0)
        var gates: [Gate] = []; gates.reserveCapacity(count)
        var last: Float = 355
        for i in 0..<count {
            let center: Float
            if i < 10 { center = 332 + Float(((number - 1) >> (i*2)) & 3)*26 }
            else { center = min(465, max(235, last + (rng.next() - 0.5)*90)) }
            last = center
            let active = (zone >= 2 && i >= 5 && (i+number+world)%3 == 0) || (type == 2 && i >= 4 && i%3 == 0) || (world == 4 && i >= 4 && i%4 == 0)
            gates.append(Gate(x:420 + Float(i*(228-zone*2)), center:center,
                              // Same per-obstacle gap rhythm and RNG call order as Kotlin.
                              gap:max(164,gap+(rng.next()-0.5)*(i < 2 ? 12 : 30+Float(zone)*2)+(i%5 == 0 ? 8 : 0)),
                              width:76,
                              movement:active ? 7+rng.next()*9 : 0, phase:rng.next()*6.283185,
                              kind:world, coin:i%3 != 2 || type == 1, star:i%4 == 2 || type == 1,
                              power:i >= 2 && (number+i+world)%11 == 0 ? 1 : i >= 2 && (number+i+world)%13 == 0 ? 2 : 0))
        }
        return Level(world:world, number:number, zone:zone+1, speed:speed,
                     wind:zone >= 4 && world%2 == 1 ? 13 : 0, gates:gates, type:type)
    }
    /// Unbounded numbered progression, with deterministic remixing after the first cycle.
    static func createStream(_ world:Int,_ ordinal:Int)->Level {
        precondition(ordinal>0 && ordinal<Int.max-1)
        let mapped=(ordinal-1)%levelsPerWorld+1
        let cycle=(ordinal-1)/levelsPerWorld
        let base=create(world,mapped)
        if cycle==0 {return base}
        let salt=cycle%4096
        let remixed=base.gates.enumerated().map{ (index,gate) in
            let diff=Float((Int64(salt)*37+Int64(index)*11)%25-12)
            return Gate(x:gate.x,center:min(475,max(220,gate.center+diff)),gap:gate.gap,width:gate.width,
                        movement:gate.movement,phase:gate.phase+Float(salt)*0.017,
                        kind:gate.kind,coin:gate.coin,star:gate.star,power:gate.power)
        }
        return Level(world:base.world,number:base.number,zone:base.zone,speed:base.speed,wind:base.wind,gates:remixed,type:base.type)
    }
    // Shared rendering/collision corridor; the playable bird never scales.
    static func opening(_ gate: Gate, _ time: Float, difficulty: Int = 1) -> Opening {
        let frequency:Float = gate.kind == 2 ? 0.65 : gate.kind == 4 ? 1.48 : gate.kind == 5 ? 0.48 : gate.kind == 7 ? 1.27 : 1.1
        let delta = gate.movement > 0 ? sin(time*frequency + gate.phase)*gate.movement : 0
        let tide:Float = gate.kind == 1 ? sin(time*0.75+gate.phase)*5 : gate.kind == 5 ? sin(time*0.38+gate.phase)*5 : 0
        let center = gate.center + delta + tide
        let pulse:Float = gate.movement>0 && (gate.kind == 3 || gate.kind == 6 || gate.kind == 7) ? (sin(time*1.35+gate.phase)+1)*2.5 : 0
        let difficultyPadding:Float = difficulty <= 0 ? 18 : difficulty >= 2 ? -16 : 0
        let half = max(72.5, (gate.gap+difficultyPadding)*0.5-pulse)
        return Opening(top:center-half, bottom:center+half)
    }
    static func milestoneReward(_ number:Int)->Int {
        guard number>0 else {return 0}
        if number%60==0 {return 200}
        if number%30==0 {return 100}
        if number%15==0 {return 50}
        if number%5==0 {return 20}
        return 0
    }
    static func signature(_ level: Level) -> String {
        level.gates.prefix(10).map { String(Int($0.center)) }.joined(separator:":")
    }
}

/** UI metadata for the four existing generator types; no gameplay changes. */
enum LevelKind {
    static func name(_ type:Int)->String {
        switch type {
        case 1: return "Izazovni"
        case 2: return "Bonus"
        case 3: return "Elitni"
        default: return "Normalni"
        }
    }
    static func icon(_ type:Int)->String {
        switch type {
        case 1: return "⚡"
        case 2: return "✦"
        case 3: return "♛"
        default: return "●"
        }
    }
}

/// Presentation-only parallax: no gameplay state, allocations or randomness.
enum ParallaxScenery {
    private static let period:Float = 696 // Four tiles of 174 logical points.
    static func offset(_ distance:Float,layer:Int,reducedMotion:Bool)->Float {
        guard !reducedMotion && distance.isFinite else {return 0}
        let speed:Float = layer == 0 ? 0.07 : (layer == 1 ? 0.15 : 0.24)
        let travelled=(distance*speed).truncatingRemainder(dividingBy:period)
        return (travelled+period).truncatingRemainder(dividingBy:period)
    }
}

/// The displayed record must be compared BEFORE a run is stored locally.
enum ResultHeadline {
    static func label(score:Int,previousBest:Int,won:Bool)->String {
        if won { return "LEVEL DOVRŠEN!" }
        if score > 0 && score > previousBest { return "NOVI REKORD!" }
        return "LET ZAVRŠEN!"
    }
}

/// Project world coordinates to the screen before narrowing to Float.
enum WorldCoordinates {
    static func screenX(_ gateX:Float,origin:Double,travelled:Double)->Float {
        Float(Double(gateX)+origin-travelled)
    }
}

final class GameSimulation {
    private(set) var level: BopaviCore.Level
    let difficulty:Int
    private var speedFactor:Float { difficulty == 0 ? 0.80 : (difficulty == 2 ? 1.24 : 1) }
    private var gravityFactor:Float { difficulty == 0 ? 0.87 : (difficulty == 2 ? 1.12 : 1) }
    let endless: Bool
    private(set) var totalPassed=0
    private(set) var y: Float = 366
    private(set) var velocity: Float = 0
    private(set) var time: Float = 0
    private(set) var distance: Float = 0
    // Retain a Double world position for precise collision geometry after long flights.
    // The public Float distance continues to drive existing presentation effects.
    private var preciseDistance:Double = 0
    private var levelOrigin:Double = 0
    private var nextLevel:BopaviCore.Level?
    private var nextOrigin:Double = 0
    func gateX(_ gate:BopaviCore.Gate)->Float {WorldCoordinates.screenX(gate.x,origin:levelOrigin,travelled:preciseDistance)}
    /// Display-only progress through the real generated course.
    var levelProgress:Float {
        guard let last=level.gates.last else{return 0}
        let finish=Double(last.x)+Double(last.width)+Double(radius-birdX)
        if finish<=0{return 1}
        return min(1,max(0,Float((preciseDistance-levelOrigin)/finish)))
    }
    var upcomingGates:[BopaviCore.Gate] {nextLevel?.gates ?? []}
    func upcomingGateX(_ gate:BopaviCore.Gate)->Float {WorldCoordinates.screenX(gate.x,origin:nextOrigin,travelled:preciseDistance)}
    private func prepareNext() {
        guard endless && displayLevel<Int.max-3 else {nextLevel=nil;return}
        let next=BopaviCore.createStream(level.world,displayLevel+1)
        // Keep the identical 218..228 px obstacle cadence across levels:
        // no longer or shorter transition beat, camera jump or visible void.
        let last=level.gates.count-1
        let ordinarySpacing:Float=last>0 ? level.gates[last].x-level.gates[last-1].x : 228
        let transitionSpacing=ordinarySpacing
        nextOrigin=levelOrigin+Double(level.gates[last].x)+Double(transitionSpacing)-Double(next.gates[0].x)
        nextLevel=next
    }
    private(set) var levelTransition: Float = 0
    private(set) var passed = 0
    private(set) var coins = 0
    private(set) var stars = 0
    /// Reset per-level stars only after a real completed level; marathon stars stay cumulative.
    private(set) var levelStars = 0
    private(set) var completedLevelStars = 0
    private(set) var shield = 0
    private(set) var magnetTime: Float = 0
    private(set) var collectPulse: Float = 0
    private(set) var impactPulse: Float = 0
    // Presentation-only impulse for the physical wing beat; no collision changes.
    private(set) var flapPulse: Float = 0
    private(set) var invulnerable: Float = 0
    private(set) var flaps = 0
    private(set) var active = false
    private(set) var finished = false
    private(set) var won = false
    private var collectedCoins:[Bool]
    private var collectedStars:[Bool]
    private var collectedPowers:[Bool]
    func coinVisible(_ index:Int) -> Bool { !collectedCoins[index] }
    func starVisible(_ index:Int) -> Bool { !collectedStars[index] }
    func powerVisible(_ index:Int) -> Bool { !collectedPowers[index] }
    let birdX: Float = 126
    // Body-only hitbox, reduced together with art without counting moving wings.
    let radius: Float = 18
    var completionCount = 0
    var displayLevel:Int
    var completedOrdinal=0
    init(_ level: BopaviCore.Level, endless:Bool = false, initialShield:Int = 0, initialMagnet:Float = 0, difficulty:Int = 1, initialOrdinal:Int? = nil) {
        self.level = level
        self.difficulty = min(2,max(0,difficulty))
        self.displayLevel = initialOrdinal ?? level.number
        self.endless = endless
        self.shield = min(2,max(0,initialShield))
        self.magnetTime = min(12,max(0,initialMagnet))
        collectedCoins=Array(repeating:false,count:level.gates.count)
        collectedStars=Array(repeating:false,count:level.gates.count)
        collectedPowers=Array(repeating:false,count:level.gates.count)
        prepareNext()
    }
    func flap() {
        guard !finished else { return }
        active = true; velocity = -255; flapPulse = 0.24; flaps += 1
    }
    func step(_ delta: Float) {
        guard active && !finished else { return }
        // Keep parity with Android: replay up to 100 ms after short frame stalls.
        guard delta.isFinite && delta > 0 else { return }
        var remaining = min(delta, 0.10)
        while remaining > 0.000001 && !finished {
            let slice = min(remaining, 0.034)
            advance(slice)
            remaining -= slice
        }
    }
    private func advance(_ dt: Float) {
        time += dt; levelTransition=max(0,levelTransition-dt); invulnerable = max(0, invulnerable-dt); magnetTime = max(0, magnetTime-dt); collectPulse = max(0,collectPulse-dt); impactPulse = max(0,impactPulse-dt); flapPulse = max(0,flapPulse-dt)
        // Same physics and flap control as the Android simulation in every world.
        velocity = min(365, velocity+(685*gravityFactor+level.wind)*dt)
        y += velocity*dt
        preciseDistance += Double(level.speed*speedFactor*dt)
        distance = Float(preciseDistance)
        if y < radius+5 || y > 753-radius { damage(); y = min(753-radius, max(radius+5, y)); return }
        for i in passed..<level.gates.count {
            let gate = level.gates[i]; let x = gateX(gate)
            if x > birdX+radius+115 { break }
            let bounds = BopaviCore.opening(gate, time,difficulty:difficulty)
            // Same hit area as the visible cap, including its left and right overhang.
            if x-7 < birdX+radius && x+gate.width+5 > birdX-radius &&
                (y-radius < bounds.top || y+radius > bounds.bottom) {
                damage()
                if finished { return }
            }
            let cx=x+gate.width*0.5
            let cy=(bounds.top+bounds.bottom)*0.5
            let magnetRange:Float = magnetTime>0 ? 108 : 23
            if gate.coin && !collectedCoins[i] && abs(cx-birdX)<magnetRange && abs(cy-y)<magnetRange {coins += 1;collectedCoins[i]=true;collectPulse=0.36}
            if gate.star && !collectedStars[i] && abs(cx+35-birdX)<magnetRange && abs(cy-25-y)<magnetRange {stars += 1;levelStars += 1;collectedStars[i]=true;collectPulse=0.36}
            if gate.power != 0 && !collectedPowers[i] && abs(cx+39-birdX)<30 && abs(cy+39-y)<30 {
                if gate.power==1 {shield=min(2,shield+1)} else {magnetTime=5}
                collectedPowers[i]=true;collectPulse=0.36
            }
            if x+gate.width < birdX-radius && i == passed {
                passed += 1; totalPassed += 1
                if passed == level.gates.count {
                    completedLevelStars=min(3,max(0,levelStars))
                    if endless {
                        completedOrdinal=displayLevel;completionCount += 1
                        displayLevel=min(Int.max-2,displayLevel+1)
                        // Upcoming gates are already on screen before promotion.
                        let incoming=nextLevel ?? BopaviCore.createStream(level.world,displayLevel)
                        levelOrigin=nextOrigin
                        level=incoming
                        prepareNext()
                        levelTransition=0.85
                        passed=0
                        levelStars=0
                        collectedCoins=Array(repeating:false,count:level.gates.count)
                        collectedStars=Array(repeating:false,count:level.gates.count)
                        collectedPowers=Array(repeating:false,count:level.gates.count)
                    } else { won = true; finished = true; active = false }
                    return
                }
            }
        }
    }
    private func damage() {
        if invulnerable > 0 { return }
        if shield > 0 { shield -= 1; invulnerable = 1.25; impactPulse=0.65; velocity = -90 }
        else { finished = true; active = false; won = false }
    }
    func score() -> Int { Int(min(100_000_000, max(0,Int64(totalPassed)*100 + Int64(coins)*10 + Int64(stars)*25))) }
}

/// Offline UI locale catalog. English is the default and safe fallback for
/// longer notices while localizations are completed and reviewed.
enum GameLanguages {
    private static let keys=["PLAY","WORLDS","SETTINGS","PROFILE","TASKS","COLLECTION","SHOP","LANGUAGE","SOUND","PAUSE"]
    private static let rows:[String]=[
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
    ]
    private static let catalog=rows.map{$0.components(separatedBy:"|")}

    /// Localized live gameplay vocabulary. Built once per renderer, never per frame.
    private static let flightKeys=["SCORE","DIST","LEVEL","NEW STAGE","GATES","FLIGHT","TAP TO FLY","Bopi is flying"]
    private static let flightRows:[String]=[
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
    ]
    static func hudPack(_ code:String)->[String:String] {
        let row=flightRows[codes.firstIndex(of:normalized(code)) ?? 0].components(separatedBy:"|")
        var labels=[String:String]()
        for index in flightKeys.indices {labels[flightKeys[index]]=row[index+1]}
        return labels
    }
    static let codes=catalog.map{$0[0]}
    static let names=catalog.map{$0[1]}
    static func normalized(_ code:String)->String{codes.contains(code) ? code : "en"}
    static func name(_ code:String)->String{names[codes.firstIndex(of:normalized(code)) ?? 0]}
    private static let english:[(String,String)]=[
        ("NASTAVI ILI ODABERI LEVEL","CONTINUE OR CHOOSE A LEVEL"),
        ("PREGLED ZAKLJUČANIH LEVELA","LOCKED LEVELS PREVIEW"),
        ("VRATI NA OTKLJUČANE LEVELE","RETURN TO UNLOCKED LEVELS"),
        ("POKRENI ODABRANI LEVEL","START SELECTED LEVEL"),
        ("Ovi leveli su zaključani. Dovrši prethodne za otključavanje.","These levels are locked. Complete earlier levels to unlock them."),
        ("Level mora biti otključan i unutar raspona.","Choose an unlocked level within the allowed range."),
        ("Odaberi otključan level iz raspona","Choose an unlocked level from"),
        ("Odaberite level","Choose a level"),
        ("Broj otključanog levela","Unlocked level number"),
        ("Otključano do levela","Unlocked through level"),
        ("Prvo dovrši prethodni level","Complete the previous level first"),
        ("Nedostupan level","Unavailable level"),
        ("TAP ZA ZAMAH","TAP TO FLAP"),
        ("ZAKLJUČANI LEVELI","LOCKED LEVELS"),
        ("IZAZOVNI","CHALLENGE"),
        ("NORMALNI","NORMAL"),
        ("ELITNI","ELITE"),
        ("STRANICA","PAGE"),
        ("ZONA","ZONE"),
        ("POKRENI","START"),
        ("ODUSTANI","CANCEL"),
        ("dovršen","completed"),
        ("otključan","unlocked"),
        ("zaključan","locked"),
        ("zvjezdice","stars"),
        ("od 3","out of 3"),
        ("Pokreni level","Start level"),
        ("Tvoj napredak kroz svih osam svjetova","Your progress across all eight worlds"),
        ("NAPREDAK PO SVJETOVIMA","WORLD PROGRESS"),
        ("Tvojih deset najboljih letova","Your ten best flights"),
        ("SVE NAGRADE","ALL AWARDS"),
        ("Najbolji rezultat","Best score"),
        ("Osvojene kovanice","Coins earned"),
        ("Dovršeni leveli","Levels completed"),
        ("Pobjede","Wins"),
        ("PRVI LET","FIRST FLIGHT"),
        ("ISTRAŽIVAČ","EXPLORER"),
        ("SAKUPLJAČ","COLLECTOR"),
        ("1 pobjeda","1 victory"),
        ("5 svjetova","5 worlds"),
        ("500 predmeta","500 collectibles"),
        ("10 pobjeda","10 victories"),
        ("osvojeno","earned"),
        ("zaključano","locked"),
        ("PRIVATNOST","PRIVACY"),
        ("IGRAČ","PLAYER"),
        ("★  SVE NAGRADE","★  ALL AWARDS"),
        ("Zeleni otoci","Green Islands"),
        ("Kristalno nebo","Crystal Sky"),
        ("Lava vrhovi","Lava Peaks"),
        ("Noćni dvorci","Night Castles"),
        ("Biserno more","Pearl Sea"),
        ("Nebeske kule","Sky Towers"),
        ("Čarobna šuma","Enchanted Forest"),
        ("Zvjezdani bezdani","Star Abyss"),
        ("LET ZAVRŠEN!","FLIGHT OVER!"),
        ("LEVEL DOVRŠEN!","LEVEL COMPLETE!"),
        ("PRIKUPLJENE\nKOVANICE","COLLECTED\nCOINS"),
        ("UKUPNO\nPROLAZA","TOTAL\nGATES"),
        ("BONUS\nBODOVA","BONUS\nPOINTS"),
        ("PRIKUPLJENE KOVANICE","COLLECTED COINS"),
        ("UKUPNO PROLAZA","TOTAL GATES"),
        ("Najbolji rezultat:","Personal best:"),
        ("Ukupno prolaza:","Total gates:"),
        ("Level ","Level "),
        ("REZULTAT","SCORE"),
        ("KOVANICE","COINS"),
        ("ZVIJEZDE","STARS"),
        ("REKORD","RECORD"),
        ("NAGRADE","REWARDS"),
        ("TVOJI REZULTATI","YOUR RESULTS"),
        ("LJESTVICA","LEADERBOARD"),
        ("PODIJELI","SHARE"),
        ("MAPA SVJETOVA","WORLD MAP"),
        ("PREUZMI","CLAIM"),
        ("Novi osobni rekord:","New personal best:"),
        (" bodova"," points"),
        (" kovanica"," coins"),
        ("svijet ","world "),
        ("otključano","unlocked"),
        ("otključan","unlocked"),
        ("zaključan","locked"),
        ("dovršen","completed"),
        ("zvjezdice ","stars "),
        ("upravljenje dodirom","tap controls"),
        ("upravljanje dodirom","tap controls"),
        ("odabrano","selected"),
        ("prikupljeno ","collected "),
        ("rekord ","record "),
        ("ZONA ","ZONE "),
        ("STRANICA ","PAGE "),
        ("ZAKLJUČANI LEVELI","LOCKED LEVELS"),
        ("ODABERITE LEVEL","SELECT LEVEL"),
        ("Odaberite level","Select a level"),
        ("TAP ZA ZAMAH","TAP TO FLAP"),
        ("NORMALNI","NORMAL"),
        ("IZAZOVNI","CHALLENGE"),
        ("ELITNI","ELITE"),
        ("Razina","Level"),
        ("Početna","Home"),
        ("Težina","Difficulty"),
        ("Opušteno","Easy"),
        ("Standardno","Normal"),
        ("Izazovno","Hard"),
        ("BONUS BODOVA","BONUS POINTS"),
        ("ZA REKORD","FOR YOUR RECORD"),
        ("ZA BODOVE","FOR POINTS"),
        ("Prvo dovrši","Complete first"),
        ("Igrač","Player"),
        ("Vrijeme","Time"),
        ("REKORDI","RECORDS"),
        ("KOVANICA ZA REKORD","RECORD COINS"),
        ("OPREMA ZA BOPIJEV LET","EQUIPMENT FOR BOPI FLIGHT"),
        ("KUPI ŠTIT","BUY SHIELD"),
        ("KUPI MAGNET","BUY MAGNET"),
        ("UČITAVANJE...","LOADING..."),
        ("Osam različitih avantura","Eight distinct adventures"),
        ("PREGLED ZAKLJUČANIH LEVELA","LOCKED LEVEL PREVIEW"),
        ("Otključano do levela","Unlocked through level"),
        ("Broj otključanog levela","Unlocked level number"),
        ("Level mora biti otključan i unutar raspona.","The level must be unlocked and in range."),
        ("Ovi leveli su zaključani. Dovrši prethodne za otključavanje.","These levels are locked. Complete earlier levels to unlock them."),
        ("VRATI NA OTKLJUČANE LEVELE","RETURN TO UNLOCKED LEVELS"),
        ("Prvo dovrši prethodni level.","Complete the previous level first."),
        ("Prvo dovrši prethodni level","Complete the previous level first"),
        ("Pauza je dostupna nakon početka leta; izbjegavaj prepreke i skupljaj nagrade.","Pause is available once you start flying. Dodge obstacles and collect rewards."),
        ("Za svakih novih 1.000 bodova najboljeg rezultata dobivaš 1 kovanicu.","Earn one coin for each additional 1,000 points in your best score."),
        ("Osvajaj kovanice prelaskom nagradnih levela i biraj opremu za svoj sljedeći let.","Earn coins by completing reward levels and prepare equipment for your next flight."),
        ("Privlači predmete tijekom 8 sekundi.","Attract collectibles for eight seconds."),
        ("Kupljena oprema automatski se koristi na početku sljedećeg leta.","Purchased equipment activates automatically at the start of your next flight."),
        ("Tvoj letački dnevnik i osobni rezultati","Your flight journal and personal records"),
        ("Prvi rekord čeka tvoj sljedeći let!","Your first record awaits your next flight!"),
        ("TEŽINA IGRE — utječe na brzinu i gravitaciju","GAME DIFFICULTY — affects speed and gravity"),
        ("Težina se primjenjuje na sljedeći let. Dosadašnji napredak ostaje spremljen.","Difficulty changes apply to the next flight. Your progress stays saved."),
        ("Glazba i zvučni efekti","Music and sound effects"),
        ("Bez oglasa, praćenja i kupnje stvarnim novcem.","No ads, tracking or real-money purchases."),
        ("Ime igrača je promijenjeno.","Player name updated."),
        ("Kopiju nije moguće otvoriti ili spremiti.","Unable to open or save the backup."),
        ("Nježnije animacije","Gentler animations"),
        ("Uravnotežen izazov","Balanced challenge"),
        ("Opušten let","Relaxed flight"),
        ("Uspješno","Successful"),
        ("Stanje novčanika:","Wallet balance:"),
        ("Letački profil,","Flight profile,"),
        ("Odabrani letač:","Selected pilot:"),
        ("Težina igre","Game difficulty"),
        ("Težina:","Difficulty:"),
        ("BOPAVI, učitavanje igre","BOPAVI, loading game"),
        ("Pogledaj sva postignuća","View all achievements"),
        ("Dovršeni leveli","Completed levels"),
        ("Pokušaji bez pobjede:","Unsuccessful attempts:"),
        ("Pobjede:","Victories:"),
        ("Pronađi","Find"),
        ("kupnja nije moguća","purchase unavailable"),
        ("Kupnja nije moguća","Purchase unavailable"),
        ("Prilagodi igru svom stilu","Make the game your own"),
        ("ZVJEZDICE","STARS"),
        ("Zvjezdice","Stars"),
        ("KVANICE","COINS"),
        ("Brzina","Speed"),
        ("UPRAVLJANJE","HANDLING"),
        ("Početni štit","Starting shield"),
        ("Početni magnet","Starting magnet"),
        ("SVI REZULTATI","ALL RESULTS"),
        (" isključeno"," off"),
        (" uključeno"," on"),
        (" isključena"," off"),
        (" uključena"," on"),
        ("zaključano","locked"),
        ("otključano","unlocked"),
        ("kovanica","coins"),
        ("Kovanica","Coins"),
        ("najbolji rezultat","best score"),
        ("prikupljeno","collected"),
        ("rekord","record"),
        ("od 30","out of 30"),
        ("Otvorite levele","Open levels"),
        ("Otvori levele","Open levels"),
        ("ZADACI I POSTIGNUĆA","TASKS & ACHIEVEMENTS"),
        ("TRGOVINA KOVANICAMA","COIN SHOP"),
        ("NAPREDNE POSTAVKE","ADVANCED SETTINGS"),
        ("MANJE ANIMACIJA","REDUCED MOTION"),
        ("POLITIKA PRIVATNOSTI","PRIVACY POLICY"),
        ("UVJETI KORIŠTENJA","TERMS OF USE"),
        ("POČETNA","HOME"),
        ("POVRATAK","RETURN"),
        ("SPREMI KOPIJU NAPRETKA","EXPORT PROGRESS"),
        ("VRATI NAPREDAK IZ KOPIJE","IMPORT PROGRESS"),
        ("SPREMI IME","SAVE NAME"),
        ("UREDI PROFIL","EDIT PROFILE"),
        ("MOJE NAGRADE","MY AWARDS"),
        ("MOJI NAJBOLJI SVJETOVI","MY TOP WORLDS"),
        ("MOJI REKORDI","MY RECORDS"),
        ("MOJA POSTIGNUĆA","MY ACHIEVEMENTS"),
        ("IGRAČ I TEŽINA","PLAYER & DIFFICULTY"),
        ("PODACI I PRIVATNOST","DATA & PRIVACY"),
        ("IZGLED I ZVUK","APPEARANCE & SOUND"),
        ("DODATNE OPCIJE","EXTRA OPTIONS"),
        ("ODABERI LIKA","CHOOSE A PILOT"),
        ("ODABERI SVJET","CHOOSE A WORLD"),
        ("RAZINE","LEVELS"),
        ("LEVELI","LEVELS"),
        ("RAZINA","LEVEL"),
        ("OSTVARENI REZULTAT","YOUR SCORE"),
        ("OSOBNI REKORD","PERSONAL BEST"),
        ("NOVI REKORD","NEW RECORD"),
        ("DOVRŠENO","COMPLETED"),
        ("POREDAK","RANKING"),
        ("PRETHODNIH","PREVIOUS"),
        ("SLJEDEĆIH","NEXT"),
        ("SLJEDEĆI","NEXT"),
        ("PRETHODNI","PREVIOUS"),
        ("NASTAVI LET","CONTINUE FLIGHT"),
        ("POLETI","FLY"),
        ("POČETNI ŠTIT","STARTING SHIELD"),
        ("POČETNI MAGNET","STARTING MAGNET"),
        ("PONOVO","RETRY"),
        ("IZLAZ","EXIT"),
        ("NATRAG","BACK"),
        ("KONTROLE","CONTROLS"),
        ("MUZIKA","MUSIC"),
        ("GLAZBA","MUSIC"),
        ("VIBRACIJA","VIBRATION"),
        ("VIBRACIJE","VIBRATION"),
        ("EFEKTI","EFFECTS"),
        ("UKLJ.","ON"),
        ("ISKL.","OFF"),
        ("PROČITAJ","READ"),
        ("POSTIGNUĆA","ACHIEVEMENTS"),
        ("REKORDI","RECORDS"),
        ("KOLEKCIJA","COLLECTION"),
        ("ZADACI","TASKS"),
        ("PROFIL","PROFILE"),
        ("TRGOVINA","SHOP"),
        ("SVJETOVI","WORLDS"),
        ("POSTAVKE","SETTINGS"),
        ("IGRAJ","PLAY"),
        ("JEZIK","LANGUAGE"),
        ("ZVUK","SOUND"),
        ("PAUZA","PAUSE"),
        ("KOVANICE","COINS"),
        ("KOVANICA","COINS"),
        ("BODOVA","POINTS"),
        ("NAGRADE","REWARDS"),
        ("OSVOJENO","EARNED"),
        ("ZAKLJUČANO","LOCKED"),
        ("DOSTUPNO","AVAILABLE"),
        ("LAGANO","EASY"),
        ("NORMALNO","NORMAL"),
        ("ZAHTJEVNO","HARD"),
        ("DODIRNI ZA LET","TAP TO FLY")
    ]
    private static let sortedEnglish=english.sorted {$0.0.count > $1.0.count}
    static func text(_ original:String,_ code:String)->String {
        let locale=normalized(code)
        var result=original
        for (source,value) in sortedEnglish {result=result.replacingOccurrences(of:source,with:value)}
        if locale=="en" {return result}
        if locale=="hr" {return original}
        let row=catalog[codes.firstIndex(of:locale) ?? 0]
        for i in keys.indices {result=result.replacingOccurrences(of:keys[i],with:row[i+2])}
        return result
    }
}
