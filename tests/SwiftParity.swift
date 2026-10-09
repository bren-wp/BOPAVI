import Foundation

@main
struct SwiftParity {
    static func main() {
        let numbers=[1,2,3,4,30,60,61,360,361,1001,100000,524289,1048576]
        var checked=0
        for w in 0..<8 {
            var seen=Set<String>()
            for n in stride(from:1,through:6000,by:2) {
                let l=BopaviCore.create(w,n),sig=BopaviCore.signature(BopaviCore.create(w,n))
                precondition(seen.insert(sig).inserted)
                precondition((11...15).contains(l.gates.count))
                precondition((140...212).contains(l.speed))
                precondition(l.gates.allSatisfy{$0.gap>=165})
                precondition(l.gates.allSatisfy {g in let o=BopaviCore.opening(g,100);return o.bottom-o.top >= 145})
                checked += 1
            }
            for n in numbers {
                let l=BopaviCore.create(w,n)
                print("V|\(w)|\(n)|\(l.zone)|\(l.type)|\(l.gates.count)|\(String(format:"%.4f",Double(l.speed)))|\(String(format:"%.4f",Double(l.gates[0].gap)))|\(BopaviCore.signature(l))")
                var reconstructed=0
                for (i,g) in l.gates.prefix(10).enumerated(){reconstructed |= Int((g.center-332)/26) << (i*2)}
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
        let continuous=GameSimulation(BopaviCore.createStream(0,1_048_576),endless:true,initialOrdinal:1_048_576)
        precondition(continuous.displayLevel==1_048_576)
        let s=GameSimulation(BopaviCore.create(0,1))
        precondition(!s.active)
        s.flap();precondition(s.active && s.flaps==1)
        for _ in 0..<70 {s.step(1/60)}
        precondition(s.time>0 && s.y.isFinite)

        // Identical visible-cap collision regression for UIKit simulation.
        do {
            let l=BopaviCore.create(0,2)
            let g=l.gates[0]
            let hit=BopaviCore.Gate(x:154,center:510,gap:160,width:g.width,movement:0,phase:g.phase,kind:g.kind,coin:false,star:false,power:0)
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
            let safe=BopaviCore.Gate(x:154,center:366,gap:220,width:g.width,movement:0,phase:g.phase,kind:g.kind,coin:false,star:false,power:0)
            let run=GameSimulation(BopaviCore.Level(world:0,number:2,zone:level.zone,speed:140,wind:0,gates:[safe],type:level.type))
            run.flap();run.step(1/60)
            precondition(!run.finished,"False collision inside safe gap")
        }
        do {
            let level=BopaviCore.create(0,2)
            let g=level.gates[0]
            let safe=BopaviCore.Gate(x:20,center:366,gap:220,width:g.width,movement:0,phase:g.phase,kind:g.kind,coin:false,star:false,power:0)
            let game=GameSimulation(BopaviCore.Level(world:0,number:2,zone:level.zone,speed:140,wind:0,gates:[safe],type:level.type),endless:true)
            game.flap();game.step(1/60)
            precondition(game.completionCount==1 && game.displayLevel==3)
            precondition(game.passed==0,"Seamless next level should reset only the gate counter")
            precondition(game.distance>0 && abs(game.gateX(game.level.gates[0])-300)<0.01)
            precondition(game.active && !game.finished)
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
        print("TEST|SWIFT|OK|\(checked)|\(numbers.count*8)")
    }
}
