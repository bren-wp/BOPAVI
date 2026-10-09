import Foundation

// Identical gameplay data model and deterministic generator on iOS and Android.
enum BopaviCore {
    static let worldCount = 8
    static let levelsPerWorld = 1_048_576
    static let totalLevels = worldCount * levelsPerWorld
    static let names = ["Zelene livade", "Sunčana plaža", "Ledeni vrhovi", "Vulkan", "Nebeski hram", "Mračna noć", "Kristalna šuma", "Svemirski let"]
    static let collectibles = ["Zvjezdice", "Školjke", "Pahulje", "Iskre", "Perje", "Mjesečev prah", "Kristali", "Zvjezdani prah"]
    static let collectibleIcons = ["★", "◉", "❄", "✦", "❖", "☾", "◆", "✧"]
    static let hazards = ["trnje", "valovi", "sige", "lava", "stupovi", "sjene", "kristali", "meteori"]
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
            gates.append(Gate(x:560 + Float(i*(242-zone*2)), center:center,
                              gap:gap-rng.next()*3, width:64,
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
    static func opening(_ gate: Gate, _ time: Float) -> Opening {
        let frequency:Float = gate.kind == 2 ? 0.65 : gate.kind == 4 ? 1.48 : gate.kind == 5 ? 0.48 : gate.kind == 7 ? 1.27 : 1.1
        let delta = gate.movement > 0 ? sin(time*frequency + gate.phase)*gate.movement : 0
        let tide:Float = gate.kind == 1 ? sin(time*0.75+gate.phase)*5 : gate.kind == 5 ? sin(time*0.38+gate.phase)*5 : 0
        let center = gate.center + delta + tide
        let pulse:Float = gate.movement>0 && (gate.kind == 3 || gate.kind == 6 || gate.kind == 7) ? (sin(time*1.35+gate.phase)+1)*2.5 : 0
        let half = max(72.5, gate.gap*0.5-pulse)
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

final class GameSimulation {
    private(set) var level: BopaviCore.Level
    let difficulty:Int
    private var speedFactor:Float { difficulty == 0 ? 0.9 : (difficulty == 2 ? 1.12 : 1) }
    private var gravityFactor:Float { difficulty == 0 ? 0.87 : (difficulty == 2 ? 1.12 : 1) }
    let endless: Bool
    private(set) var totalPassed=0
    private(set) var y: Float = 366
    private(set) var velocity: Float = 0
    private(set) var time: Float = 0
    private(set) var distance: Float = 0
    private var levelOrigin: Float = 0
    func gateX(_ gate:BopaviCore.Gate)->Float { gate.x+levelOrigin-distance }
    private(set) var levelTransition: Float = 0
    private(set) var passed = 0
    private(set) var coins = 0
    private(set) var stars = 0
    private(set) var shield = 0
    private(set) var magnetTime: Float = 0
    private(set) var collectPulse: Float = 0
    private(set) var impactPulse: Float = 0
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
    // Match the visible Bopi body and pillar cap widths.
    let radius: Float = 23
    var completionCount = 0
    var completedLevelNumber = 0
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
    }
    func flap() {
        guard !finished else { return }
        active = true; velocity = -255; flaps += 1
    }
    func step(_ delta: Float) {
        guard active && !finished else { return }
        let dt = min(0.034, max(0, delta))
        time += dt; levelTransition=max(0,levelTransition-dt); invulnerable = max(0, invulnerable-dt); magnetTime = max(0, magnetTime-dt); collectPulse = max(0,collectPulse-dt); impactPulse = max(0,impactPulse-dt)
        velocity = min(365, velocity+(685*gravityFactor+level.wind)*dt)
        y += velocity*dt
        distance += level.speed*speedFactor*dt
        if y < radius+5 || y > 753-radius { damage(); y = min(753-radius, max(radius+5, y)); return }
        for i in passed..<level.gates.count {
            let gate = level.gates[i]; let x = gateX(gate)
            if x > birdX+radius+115 { break }
            let bounds = BopaviCore.opening(gate, time)
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
            if gate.star && !collectedStars[i] && abs(cx+35-birdX)<magnetRange && abs(cy-25-y)<magnetRange {stars += 1;collectedStars[i]=true;collectPulse=0.36}
            if gate.power != 0 && !collectedPowers[i] && abs(cx+39-birdX)<30 && abs(cy+39-y)<30 {
                if gate.power==1 {shield=min(2,shield+1)} else {magnetTime=5}
                collectedPowers[i]=true;collectPulse=0.36
            }
            if x+gate.width < birdX-radius && i == passed {
                passed += 1; totalPassed += 1
                if passed == level.gates.count {
                    if endless {
                        completedLevelNumber=level.number;completedOrdinal=displayLevel;completionCount += 1
                        displayLevel=min(Int.max-2,displayLevel+1)
                        let next=BopaviCore.createStream(level.world,displayLevel)
                        // World position remains monotonic across level transitions.
                        levelOrigin=distance+300-next.gates[0].x
                        level=next
                        levelTransition=0.78
                        passed=0
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
    func rating() -> Int { !won ? 0 : 1+min(2,stars/max(1,level.gates.count/4)) }
    func score() -> Int { totalPassed*100 + coins*10 + stars*25 }
}
