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
        print("TEST|SWIFT|OK|\(checked)|\(numbers.count*8)")
    }
}
