import Foundation

@main
struct SwiftParity {
    static func main() {
        precondition(GameLanguages.codes.count == 26)
        precondition(Set(GameLanguages.codes).count == 26)
        precondition(GameLanguages.name("en") == "English")
        precondition(GameLanguages.normalized("unknown") == "en")
        precondition(GameLanguages.text("▶  IGRAJ","en") == "▶  PLAY")
        precondition(GameLanguages.text("⚙  POSTAVKE","en") == "⚙  SETTINGS")
        precondition(GameLanguages.text("⚙  POSTAVKE","hr") == "⚙  POSTAVKE")
        precondition(GameLanguages.text("NAPREDNE POSTAVKE","en") == "ADVANCED SETTINGS")
        for code in GameLanguages.codes {
            precondition(!GameLanguages.text("IGRAJ",code).isEmpty)
            precondition(!GameLanguages.name(code).isEmpty)
        }
        let terms=["SCORE","DIST","LEVEL","NEW STAGE","GATES",
                   "FLIGHT","TAP TO FLY","Bopi is flying"]
        for code in GameLanguages.codes {
            let pack=GameLanguages.hudPack(code)
            precondition(Set(pack.keys)==Set(terms), "Missing live HUD vocabulary: \(code)")
            precondition(pack.values.allSatisfy {!$0.isEmpty && !$0.contains("|")})
        }
        precondition(GameLanguages.hudPack("hr")["SCORE"]=="BODOVI")
        precondition(GameLanguages.hudPack("ja")["SCORE"]=="スコア")
        precondition(GameLanguages.hudPack("ko")["SCORE"]=="점수")
        precondition(GameLanguages.hudPack("invalid")==GameLanguages.hudPack("en"))
        let englishWorlds=["Green Islands","Crystal Sky","Lava Peaks",
                           "Night Castles","Pearl Sea","Sky Towers","Enchanted Forest","Star Abyss"]
        precondition(BopaviCore.names.map{GameLanguages.text($0,"en")} == englishWorlds)
        precondition(GameLanguages.text(BopaviCore.names[0],"hr")=="Zeleni otoci")
        precondition(GameLanguages.text("LET ZAVRŠEN!","en")=="FLIGHT OVER!")
        precondition(GameLanguages.text("LEVEL DOVRŠEN!","en")=="LEVEL COMPLETE!")
        precondition(GameLanguages.text("PRIKUPLJENE\nKOVANICE","en")=="COLLECTED\nCOINS")
        precondition(GameLanguages.text("UKUPNO\nPROLAZA","en")=="TOTAL\nGATES")
        precondition(GameLanguages.text("MOJI REKORDI","en")=="MY RECORDS")
        precondition(GameLanguages.text("TVOJI REZULTATI","en")=="YOUR RESULTS")
        let firstContours:[Float]=[322,252,192,242,367,447,507,382,252,187]
        for world in 0..<8 {
            let first=BopaviCore.create(world,1)
            precondition(Array(first.gates.prefix(10).map{$0.center})==firstContours,
                         "Level 1 rock shafts require visible natural height variance")
            precondition(zip(first.gates,first.gates.dropFirst()).allSatisfy{
                abs($0.0.center-$0.1.center)<=208
            })
            for mode in 0...2 {
                let mouths=first.gates.prefix(10).map{ BopaviCore.opening($0,0,difficulty:mode) }
                let requiredMin=mouths.map{$0.top+18}.max()!
                let requiredMax=mouths.map{$0.bottom-18}.min()!
                precondition(requiredMin>requiredMax,
                             "World \(world) difficulty \(mode) must not have a straight safe lane")
            }
        }
        let numbers=[1,2,3,4,30,60,61,360,361,1001,100000,524289,1048576]
        var checked=0
        for w in 0..<8 {
            var seen=Set<String>()
            for n in stride(from:1,through:6000,by:2) {
                let l=BopaviCore.create(w,n),sig=BopaviCore.signature(BopaviCore.create(w,n))
                precondition(seen.insert(sig).inserted)
                precondition((11...15).contains(l.gates.count))
                precondition(l.gates[0].x==420,"First obstacle must be visible when flight starts")
                precondition((140...212).contains(l.speed))
                precondition(l.gates.allSatisfy{$0.gap>=164 && (64...91).contains($0.width)})
                precondition(Set(l.gates.map{$0.gap}).count>=3)
                let gaps=l.gates.map{$0.gap}
                precondition((gaps.max() ?? 0)-(gaps.min() ?? 0) >= 1,
                             "Seeded gate heights must vary within each level")
                precondition(l.gates.allSatisfy {g in let o=BopaviCore.opening(g,100);return o.bottom-o.top >= 145})
                checked += 1
            }
            for n in numbers {
                let l=BopaviCore.create(w,n)
                print("V|\(w)|\(n)|\(l.zone)|\(l.type)|\(l.gates.count)|\(String(format:"%.4f",Double(l.speed)))|\(String(format:"%.4f",Double(l.gates[0].gap)))|\(BopaviCore.signature(l))|\(l.gates.prefix(5).map{String(Int($0.width))}.joined(separator:","))")
                var reconstructed=0
                let contour:[Float]=[-10,-80,-140,-90,35,115,175,50,-80,-145]
                for (i,g) in l.gates.prefix(10).enumerated(){
                    reconstructed |= Int((g.center-332-contour[i])/26) << (i*2)
                }
                precondition(reconstructed == n-1)
            }
        }
        for (level,reward) in [0:0,1:0,4:0,5:20,10:20,15:50,30:100,60:200,120:200,121:0] {
            precondition(BopaviCore.milestoneReward(level)==reward)
        }
        for ordinal in [1_048_577,1_048_583,2_097_153,10_000_000_000] {
            let x=BopaviCore.createStream(4,ordinal)
            precondition(x.gates.count>=11 && x.gates.allSatisfy {g in let o=BopaviCore.opening(g,123);return o.bottom-o.top>=145})
            print("S|4|\(ordinal)|\(BopaviCore.signature(x))")
        }
        // v0.1.82: 8-biome difficulty opening must match live simulation.
        for world in 0..<8 {
            for number in [1,60,121,240,360,1048576] {
                let gaps=BopaviCore.create(world,number).gates.map{$0.gap}
                precondition((gaps.max() ?? 0)-(gaps.min() ?? 0) >= 9)
                for gate in BopaviCore.create(world,number).gates {
                    for t in [Float(0),1.25,7,15] {
                        let easy=BopaviCore.opening(gate,t,difficulty:0)
                        let normal=BopaviCore.opening(gate,t,difficulty:1)
                        let hard=BopaviCore.opening(gate,t,difficulty:2)
                        precondition(easy.bottom-easy.top >= normal.bottom-normal.top)
                        precondition(normal.bottom-normal.top >= hard.bottom-hard.top)
                        precondition(hard.bottom-hard.top >= 144.9)
                        precondition(hard.top > 0 && easy.bottom < 755)
                        precondition(normal.top == BopaviCore.opening(gate,t).top)
                    }
                }
            }
        }
        do {
            let base=BopaviCore.create(0,1)
            let template=base.gates[0]
            let obstacle=BopaviCore.Gate(x:148,center:290,gap:164,width:template.width,
                movement:0,phase:template.phase,kind:template.kind,coin:false,star:false,power:0)
            let single=BopaviCore.Level(world:0,number:1,zone:1,speed:140,wind:0,
                                        gates:[obstacle],type:0)
            let easy=GameSimulation(single,difficulty:0)
            let hard=GameSimulation(single,difficulty:2)
            easy.flap();hard.flap();easy.step(1/60);hard.step(1/60)
            precondition(!easy.finished && hard.finished,
                         "Difficulty must alter actual lip collision, not only visuals")
            precondition(easy.radius==18 && hard.radius==18)
        }
        // All worlds use one touch-to-flap impulse, without drag steering.
        for world in 0..<8 {
            let flight=GameSimulation(BopaviCore.create(world,1))
            precondition(!flight.active && flight.radius == 18)
            flight.flap()
            precondition(flight.active && flight.flaps == 1 && flight.velocity == -255)
            let before=flight.y
            flight.step(0.016)
            precondition(flight.y < before)
            flight.flap()
            precondition(flight.flaps == 2)
        }
        let continuous=GameSimulation(BopaviCore.createStream(0,1_048_576),endless:true,initialOrdinal:1_048_576)
        precondition(continuous.displayLevel==1_048_576)
        let s=GameSimulation(BopaviCore.create(0,1))
        precondition(!s.active && s.levelProgress==0)
        s.flap();precondition(s.active && s.flaps==1)
        s.step(0.05)
        precondition(s.levelProgress>0 && s.levelProgress<1)
        for _ in 0..<67 {s.step(1/60)}
        precondition(s.time>0 && s.y.isFinite)

        // Identical visible-cap collision regression for UIKit simulation.
        do {
            let l=BopaviCore.create(0,2)
            let g=l.gates[0]
            let hit=BopaviCore.Gate(x:148,center:510,gap:160,width:g.width,movement:0,phase:g.phase,kind:g.kind,coin:false,star:false,power:0)
            let custom=BopaviCore.Level(world:0,number:2,zone:l.zone,speed:140,wind:0,gates:[hit],type:l.type)
            let direct=GameSimulation(custom)
            direct.flap();direct.step(1/60)
            precondition(direct.finished && !direct.won && direct.passed==0,"collider missed cap")
            let shielded=GameSimulation(custom,initialShield:1)
            shielded.flap();shielded.step(1/60)
            precondition(!shielded.finished && shielded.shield==0,"shield must absorb one impact")
        }
        do {
            let level=BopaviCore.create(0,2)
            let g=level.gates[0]
            let safe=BopaviCore.Gate(x:148,center:366,gap:220,width:g.width,movement:0,phase:g.phase,kind:g.kind,coin:false,star:false,power:0)
            let run=GameSimulation(BopaviCore.Level(world:0,number:2,zone:level.zone,speed:140,wind:0,gates:[safe],type:level.type))
            run.flap();run.step(1/60)
            precondition(!run.finished,"False collision inside safe gap")
        }
        do {
            let level=BopaviCore.create(0,2)
            let g=level.gates[0]
            let safe=BopaviCore.Gate(x:20,center:366,gap:220,width:g.width,movement:0,phase:g.phase,kind:g.kind,coin:false,star:false,power:0)
            let game=GameSimulation(BopaviCore.Level(world:0,number:2,zone:level.zone,speed:140,wind:0,gates:[safe],type:level.type),endless:true)
            let previewX=game.upcomingGateX(game.upcomingGates[0])
            game.flap();game.step(1/60)
            precondition(game.completionCount==1 && game.displayLevel==3)
            precondition(abs(game.levelTransition-0.85)<0.001,"Real level entry must trigger transient gauge highlight")
            precondition(game.passed==0,"Seamless next level should reset only the gate counter")
            precondition(game.distance>0 && abs(game.gateX(game.level.gates[0])-(previewX-game.distance))<0.01)
            precondition(game.active && !game.finished)
            precondition(game.totalPassed==1 && game.passed==0,"Global pass count reset across levels")
        }
        do {
            let level=BopaviCore.create(0,2)
            let easy=GameSimulation(level,difficulty:0)
            let normal=GameSimulation(level,difficulty:1)
            let hard=GameSimulation(level,difficulty:2)
            for sim in [easy,normal,hard] {sim.flap();sim.step(1/60)}
            precondition(easy.distance < normal.distance && normal.distance < hard.distance)
            precondition(easy.y < normal.y && normal.y < hard.y)
            precondition(easy.level.gates.count == hard.level.gates.count)
            precondition(GameSimulation(level,difficulty:42).difficulty == 2)
        }
        precondition(s.score() >= 0 && s.score() <= 100_000_000)
        do {
            let level=BopaviCore.create(0,2)
            let smooth=GameSimulation(level)
            let delayed=GameSimulation(level)
            smooth.flap();delayed.flap()
            for _ in 0..<6 {smooth.step(1/60)}
            delayed.step(0.10)
            precondition(abs(smooth.distance-delayed.distance)<0.02)
            precondition(abs(smooth.y-delayed.y)<3)
            let interrupted=GameSimulation(level)
            interrupted.flap();interrupted.step(8)
            precondition(abs(interrupted.time-0.10)<0.0001)
            let invalid=GameSimulation(level)
            invalid.flap();invalid.step(Float.nan)
            precondition(invalid.time==0 && invalid.distance==0)
        }
        // Deterministic visual parallax must match the Kotlin scene-depth contract.
        for layer in 0..<3 {
            let expected:[Float]=[70,150,240]
            precondition(abs(ParallaxScenery.offset(1000,layer:layer,reducedMotion:false)-expected[layer])<0.001)
            precondition(ParallaxScenery.offset(1000,layer:layer,reducedMotion:true)==0)
            precondition(ParallaxScenery.offset(Float.nan,layer:layer,reducedMotion:false)==0)
            precondition(ParallaxScenery.offset(Float.infinity,layer:layer,reducedMotion:false)==0)
            let off=ParallaxScenery.offset(999999,layer:layer,reducedMotion:false)
            precondition(off>=0 && off<696)
        }
        // v0.1.85: exact same in-level and cross-level gate rhythm.
        for world in 0..<8 {
            for number in [2,62,122,182,242,302,360] {
                let base=BopaviCore.create(world,number)
                let stream=GameSimulation(base,endless:true)
                let firstNext=stream.upcomingGateX(stream.upcomingGates[0])
                let oldLast=stream.gateX(base.gates[base.gates.count-1])
                let interval=base.gates[base.gates.count-1].x-base.gates[base.gates.count-2].x
                let expected=interval
                precondition(abs(firstNext-oldLast-expected)<0.01)
                precondition(expected>=218 && expected<=228)
            }
        }
        // v0.1.20: same bounded, retriggerable cosmetic wingbeat as Kotlin.
        do {
            let b=GameSimulation(BopaviCore.create(0,2))
            precondition(b.flapPulse==0)
            b.flap()
            precondition(b.flapPulse==0.24 && b.flaps==1)
            b.step(0.08)
            precondition(abs(b.flapPulse-0.16)<0.0001)
            b.step(Float.nan)
            precondition(abs(b.flapPulse-0.16)<0.0001)
            b.flap()
            precondition(b.flapPulse==0.24 && b.flaps==2)
            for _ in 0..<3 {b.step(0.10)}
            precondition(b.flapPulse==0 && !b.finished)
        }
        // v0.1.21: identical result headline logic on both native platforms.
        precondition(ResultHeadline.label(score:0,previousBest:0,won:false)=="LET ZAVRŠEN!")
        precondition(ResultHeadline.label(score:100,previousBest:100,won:false)=="LET ZAVRŠEN!")
        precondition(ResultHeadline.label(score:99,previousBest:100,won:false)=="LET ZAVRŠEN!")
        precondition(ResultHeadline.label(score:101,previousBest:100,won:false)=="NOVI REKORD!")
        precondition(ResultHeadline.label(score:101,previousBest:100,won:true)=="LEVEL DOVRŠEN!")
        // v0.1.22: verify the same four level-type labels as Kotlin.
        for (number,kind) in [(1,0),(15,1),(30,2),(60,3),(75,1),(90,2),(120,3),(360,3)] {
            precondition(BopaviCore.create(0,number).type==kind)
            precondition(LevelKind.name(kind)==["Normalni","Izazovni","Bonus","Elitni"][kind])
            precondition(LevelKind.icon(kind)==["●","⚡","✦","♛"][kind])
        }
        // v0.1.23: no active game or time progression before the first flap.
        do {
            let idle=GameSimulation(BopaviCore.create(0,1))
            precondition(!idle.active && idle.flaps==0 && idle.time==0)
            idle.step(0.1)
            precondition(!idle.active && idle.flaps==0 && idle.distance==0)
            idle.flap()
            precondition(idle.active && idle.flaps==1)
            idle.flap()
            precondition(idle.active && idle.flaps==2)
        }
        // A quarter-pixel offset after 2^25 world units must not disappear.
        let longRunOrigin=33_554_432.0
        precondition(abs(WorldCoordinates.screenX(560,origin:longRunOrigin,travelled:longRunOrigin+0.25)-559.75)<0.0001)
        precondition(abs(WorldCoordinates.screenX(560,origin:longRunOrigin+250,travelled:longRunOrigin+0.25)-809.75)<0.0001)
        print("TEST|SWIFT|OK|\(checked)|\(numbers.count*8)")
    }
}
