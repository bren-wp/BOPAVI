import Foundation

@main
struct SwiftSaves {
    static func main() throws {
        let store=ProgressStore()
        let backup:[String:Any] = ["format":"bopavi-save","exportVersion":3,"save":[
            "version":3,"maxWorld":2,"frontiers":[31,15,4,1,1,1,1,1],"coins":314,
            "owned":["bopi","sunny"],"skin":"sunny","endlessBest":57,"wins":29,
            "worldBest":[120,210,300,0,0,0,0,0],"lessMotion":true
        ] as [String:Any]]
        let bytes=try JSONSerialization.data(withJSONObject:backup)
        try store.importData(bytes)
        precondition(store.frontier(0)==31 && store.frontier(1)==15 && store.frontier(2)==4)
        precondition(store.maxWorld()==2 && store.coins()==314 && store.skinIndex()==1)
        precondition(store.endlessBest()==57 && store.wins()==29)
        let exported=try store.exportData()
        try store.importData(exported)
        precondition(store.frontier(0)==31 && store.best(2)==300)
        var invalid=backup
        if var s=backup["save"] as? [String:Any] {
            s["frontiers"]=[1,1,1,1,1,1,1,1_048_578]
            invalid["save"]=s
        }
        var rejected=false
        do {try store.importData(try JSONSerialization.data(withJSONObject:invalid))} catch {rejected=true}
        precondition(rejected)
        let old:[String:Any]=["format":"bopavi-save","exportVersion":2,"save":["version":2,"unlocked":361,"coins":44]]
        try store.importData(JSONSerialization.data(withJSONObject:old))
        precondition(store.frontier(0)==361 && store.maxWorld()==1 && store.frontier(1)==1 && store.coins()==44)
        let v5:[String:Any] = ["format":"bopavi-save","exportVersion":5,"save":[
            "version":5,"maxWorld":0,"frontiers":[5,1,1,1,1,1,1,1],
            "streamFrontiers":["5","1","1","1","1","1","1","1"],
            "coins":100,"owned":["bopi"],"skin":"bopi",
            "worldBest":[0,0,0,0,0,0,0,0],"perks":[0,0],"collectibles":[0,0,0,0,0,0,0,0]
        ] as [String:Any]]
        try store.importData(JSONSerialization.data(withJSONObject:v5))
        precondition(store.streamFrontier(0)==5)
        precondition(store.completeLevel(world:0,number:5,score:200)==20)
        precondition(store.completeLevel(world:0,number:5,score:200)==0)
        precondition(store.streamFrontier(0)==6 && store.coins()==120)
        precondition(store.buyPerk(0) && store.coins()==40 && store.perkCount(0)==1)
        let used=store.consumePerks()
        precondition(used.shield==1 && store.perkCount(0)==0)
        let again=store.consumePerks()
        precondition(again.shield==0)
        let roundtrip=try store.exportData()
        try store.importData(roundtrip)
        precondition(store.streamFrontier(0)==6 && store.coins()==40)
        print("PASS: Swift v0.2-v0.5 migration, first-clear-only coins, purchase/consume, save round-trip and invalid backup rejection")
    }
}
