import Foundation

/// Progress uses tiny fixed-size arrays; Android/HTML backups can be imported explicitly.
final class ProgressStore {
    private let defaults = UserDefaults.standard
    func frontier(_ world: Int) -> Int { max(1,min(BopaviCore.levelsPerWorld+1,defaults.integer(forKey:"frontier_\(world)") == 0 ? 1 : defaults.integer(forKey:"frontier_\(world)"))) }
    func streamFrontier(_ world:Int)->Int {
        guard (0..<8).contains(world) else{return 1}
        let value=defaults.integer(forKey:"stream_frontier_\(world)")
        return max(1,min(Int.max-2,value == 0 ? frontier(world) : value))
    }
    func chosenWorld()->Int {max(0,min(maxWorld(),defaults.integer(forKey:"chosen_world")))}
    func chooseWorld(_ world:Int){if (0...maxWorld()).contains(world){defaults.set(world,forKey:"chosen_world")}}
    func maxWorld() -> Int { 7 /* All worlds open; preserve legacy save data. */ }
    func coins() -> Int { max(0,min(100_000_000,defaults.integer(forKey:"coins"))) }
    // One bonus coin per newly achieved 1,000-point record milestone.
    // The points remain on the local leaderboard; only unclaimed milestones pay out.
    func bestPoints()->Int {
        let worldRecord=(0..<8).map{max(0,min(100_000_000,best($0)))}.max() ?? 0
        let runRecord=leaderboard().map{$0.score}.max() ?? 0
        return max(0,min(100_000_000,max(worldRecord,runRecord)))
    }
    func bonusCoinsAvailable()->Int {
        let claimed=max(0,min(100_000,defaults.integer(forKey:"score_coins_claimed")))
        return max(0,min(bestPoints()/1000-claimed,100_000_000-coins()))
    }
    @discardableResult func claimBonusCoins()->Int {
        let awarded=bonusCoinsAvailable()
        guard awarded>0 else{return 0}
        let claimed=max(0,min(100_000,defaults.integer(forKey:"score_coins_claimed")))
        defaults.set(coins()+awarded,forKey:"coins")
        defaults.set(claimed+awarded,forKey:"score_coins_claimed")
        return awarded
    }
    func best(_ world:Int) -> Int { defaults.integer(forKey:"best_\(world)") }
    let skins=["bopi","sunny","berry","luna","mint","shadow"]
    let skinNames=["Bopi","Sunny","Berry","Luna","Mint","Shadow"]
    let costs=[0,60,80,110,130,160]
    func skinIndex()->Int { min(5,max(0,defaults.integer(forKey:"skin_index"))) }
    func owned(_ index:Int)->Bool { (0...5).contains(index) && ((defaults.integer(forKey:"owned_mask") == 0 ? 1 : defaults.integer(forKey:"owned_mask")) & (1 << index)) != 0 }
    @discardableResult func selectOrBuy(_ index:Int)->Bool {
        guard (0...5).contains(index) else {return false}
        if owned(index) {defaults.set(index,forKey:"skin_index");return true}
        if coins()<costs[index] {return false}
        defaults.set(coins()-costs[index],forKey:"coins")
        defaults.set((defaults.integer(forKey:"owned_mask") == 0 ? 1 : defaults.integer(forKey:"owned_mask")) | (1 << index),forKey:"owned_mask")
        defaults.set(index,forKey:"skin_index");return true
    }
    func endlessBest()->Int{defaults.integer(forKey:"endless_best")}
    func wins()->Int{defaults.integer(forKey:"wins")}
    func deaths()->Int{defaults.integer(forKey:"deaths")}
    func recordRun(_ game:GameSimulation){
        defaults.set(min(100_000_000,defaults.integer(forKey:"collectibles_\(game.level.world)")+game.coins+game.stars),forKey:"collectibles_\(game.level.world)")
        defaults.set(min(100_000_000,defaults.integer(forKey:"flaps")+game.flaps),forKey:"flaps")
        if game.endless {
            defaults.set(max(endlessBest(),game.totalPassed),forKey:"endless_best")
            defaults.set(defaults.integer(forKey:"endless_runs")+1,forKey:"endless_runs")
        }
        defaults.set(game.won ? wins()+1 : wins(),forKey:"wins")
        defaults.set(game.won ? deaths() : deaths()+1,forKey:"deaths")
        saveLocalScore(game)
    }
    var lessMotion: Bool {
        get { defaults.bool(forKey:"less_motion") }
        set { defaults.set(newValue,forKey:"less_motion") }
    }
    @discardableResult func completeLevel(world:Int,number:Int,score:Int)->Int {
        guard (0..<8).contains(world), number == streamFrontier(world),
              number > 0 && number < Int.max-2 else {return 0}
        let reward=BopaviCore.milestoneReward(number)
        defaults.set(number+1,forKey:"stream_frontier_\(world)")
        defaults.set(min(number+1,BopaviCore.levelsPerWorld+1),forKey:"frontier_\(world)")
        defaults.set(max(best(world),score),forKey:"best_\(world)")
        defaults.set(wins()+1,forKey:"wins")
        defaults.set(min(100_000_000,coins()+reward),forKey:"coins")
        return reward
    }
    let perkNames=["Početni štit","Početni magnet"]
    let perkPrices=[80,65]
    func perkCount(_ index:Int)->Int { (0..<2).contains(index) ? min(99,max(0,defaults.integer(forKey:"perk_\(index)"))) : 0 }
    @discardableResult func buyPerk(_ index:Int)->Bool {
        guard (0..<2).contains(index), perkCount(index)<99, coins()>=perkPrices[index] else {return false}
        defaults.set(coins()-perkPrices[index],forKey:"coins")
        defaults.set(perkCount(index)+1,forKey:"perk_\(index)")
        return true
    }
    func consumePerks()->(shield:Int,magnet:Float) {
        let a=perkCount(0),b=perkCount(1)
        defaults.set(max(0,a-1),forKey:"perk_0")
        defaults.set(max(0,b-1),forKey:"perk_1")
        return (a>0 ? 1 : 0,b>0 ? 8 : 0)
    }
    func collectibles(_ world:Int)->Int { (0..<8).contains(world) ? max(0,defaults.integer(forKey:"collectibles_\(world)")) : 0 }
    var playerName:String {
        get { defaults.string(forKey:"player_name") ?? "Igrač" }
        set {
            let allowed = newValue.trimmingCharacters(in:.whitespacesAndNewlines).filter {
                $0.isLetter || $0.isNumber || $0 == " " || $0 == "_" || $0 == "-"
            }
            defaults.set(allowed.isEmpty ? "Igrač" : String(allowed.prefix(24)),forKey:"player_name")
        }
    }
    struct LocalScore {
        let name:String
        let score:Int
        let world:Int
        let difficulty:Int
        let gates:Int
    }
    let difficultyNames=["Opušteno","Standardno","Izazovno"]
    var difficulty:Int {
        get { min(2,max(0,defaults.object(forKey:"difficulty") as? Int ?? 1)) }
        set { defaults.set(min(2,max(0,newValue)),forKey:"difficulty") }
    }
    func leaderboard()->[LocalScore] {
        guard let text=defaults.string(forKey:"local_leaderboard"),
              let data=text.data(using:.utf8),
              let list=(try? JSONSerialization.jsonObject(with:data)) as? [[String:Any]] else { return [] }
        return list.prefix(10).compactMap { row in
            guard let world=row["world"] as? Int, (0..<8).contains(world),
                  let mode=row["difficulty"] as? Int, (0...2).contains(mode),
                  let score=row["score"] as? Int, (0...100_000_000).contains(score),
                  let gates=row["gates"] as? Int, (0...100_000_000).contains(gates) else { return nil }
            return LocalScore(name:String((row["name"] as? String ?? "Igrač").prefix(24)),
                              score:score,world:world,difficulty:mode,gates:gates)
        }.sorted { $0.score == $1.score ? $0.gates > $1.gates : $0.score > $1.score }
    }
    private func scoreDictionary(_ row:LocalScore)->[String:Any] {
        ["name":row.name,"score":row.score,"world":row.world,
         "difficulty":row.difficulty,"gates":row.gates]
    }
    private func saveLocalScore(_ game:GameSimulation) {
        guard game.score()>0 else {return}
        let entry=LocalScore(name:playerName,score:game.score(),world:game.level.world,
                             difficulty:game.difficulty,gates:game.totalPassed)
        let top=(leaderboard()+[entry]).sorted {
            $0.score == $1.score ? $0.gates > $1.gates : $0.score > $1.score
        }.prefix(10).map(scoreDictionary)
        if let data=try? JSONSerialization.data(withJSONObject:top),
           let text=String(data:data,encoding:.utf8) {
            defaults.set(text,forKey:"local_leaderboard")
        }
    }
    var hapticEnabled:Bool {
        get { defaults.object(forKey:"haptic_enabled") as? Bool ?? true }
        set { defaults.set(newValue,forKey:"haptic_enabled") }
    }
    var soundEnabled:Bool {
        get {defaults.object(forKey:"sound_enabled") as? Bool ?? true}
        set {defaults.set(newValue,forKey:"sound_enabled")}
    }
    func exportData() throws -> Data {
        let save:[String:Any] = ["version":5,"frontiers":(0..<8).map { frontier($0) },
                                 "streamFrontiers":(0..<8).map { String(streamFrontier($0)) },"maxWorld":maxWorld(),"chosenWorld":chosenWorld(),
                                 "playerName":playerName,"difficulty":difficulty,"leaderboard":leaderboard().map(scoreDictionary),"coins":coins(),"scoreCoinsClaimed":max(0,min(100_000,defaults.integer(forKey:"score_coins_claimed"))),"lessMotion":lessMotion,"worldBest":(0..<8).map{best($0)},
                                 "owned":(0..<6).filter{owned($0)}.map{skins[$0]},"skin":skins[skinIndex()],
                                 "lastDaily":defaults.string(forKey:"last_daily") ?? "",
                                 "wins":wins(),"deaths":deaths(),"flaps":defaults.integer(forKey:"flaps"),
                                 "endlessBest":endlessBest(),"endlessRuns":defaults.integer(forKey:"endless_runs"),
                                 "perks":(0..<2).map{perkCount($0)},"collectibles":(0..<8).map{collectibles($0)},"soundEnabled":soundEnabled,"hapticEnabled":hapticEnabled]
        return try JSONSerialization.data(withJSONObject:["format":"bopavi-save","exportVersion":5,"save":save],options:[.prettyPrinted,.sortedKeys])
    }
    func importData(_ data:Data) throws {
        guard data.count <= 550_000,
              let body = try JSONSerialization.jsonObject(with:data) as? [String:Any],
              body["format"] as? String == "bopavi-save",
              let s = body["save"] as? [String:Any],
              let version=s["version"] as? Int, (1...5).contains(version) else { throw NSError(domain:"BOPAVI",code:1,userInfo:[NSLocalizedDescriptionKey:"Neispravna BOPAVI sigurnosna kopija."]) }
        var frontiers=Array(repeating:1,count:8)
        var maxWorld=0
        if version >= 3 {
            guard let f=s["frontiers"] as? [Int], f.count==8, f.allSatisfy({(1...BopaviCore.levelsPerWorld+1).contains($0)}),
                  let maxW=s["maxWorld"] as? Int, (0...7).contains(maxW) else { throw NSError(domain:"BOPAVI",code:2,userInfo:[NSLocalizedDescriptionKey:"Neispravan raspon levela."]) }
            frontiers=f;maxWorld=maxW
        } else {
            let old=min(2880,max(1,s["unlocked"] as? Int ?? 1))
            maxWorld=(old-1)/360
            for w in 0..<maxWorld { frontiers[w]=361 }
            frontiers[maxWorld]=(old-1)%360+1
        }
        let c=s["coins"] as? Int ?? 0
        guard (0...100_000_000).contains(c) else { throw NSError(domain:"BOPAVI",code:3,userInfo:[NSLocalizedDescriptionKey:"Neispravno stanje kovanica."]) }
        let importedClaimed=s["scoreCoinsClaimed"] as? Int ?? 0
        guard (0...100_000).contains(importedClaimed) else {
            throw NSError(domain:"BOPAVI",code:7,userInfo:[NSLocalizedDescriptionKey:"Neispravna nagrada za bodove."])
        }
        defaults.set(maxWorld,forKey:"max_world");defaults.set(min(maxWorld,max(0,s["chosenWorld"] as? Int ?? maxWorld)),forKey:"chosen_world");defaults.set(c,forKey:"coins")
        // An old backup must not re-award coins already redeemed on this device.
        let priorClaimed=max(0,min(100_000,defaults.integer(forKey:"score_coins_claimed")))
        defaults.set(max(priorClaimed,importedClaimed),forKey:"score_coins_claimed")
        var streamFrontiers=frontiers
        if version>=5,let strs=s["streamFrontiers"] as? [String] {
            guard strs.count==8 else {throw NSError(domain:"BOPAVI",code:5,userInfo:[NSLocalizedDescriptionKey:"Neispravan nastavak levela."])}
            for w in 0..<8 {
                guard let n=Int(strs[w]),n>=frontiers[w],n<Int.max-2 else {throw NSError(domain:"BOPAVI",code:6,userInfo:[NSLocalizedDescriptionKey:"Neispravan broj levela."])}
                streamFrontiers[w]=n
            }
        }
        for w in 0..<8 {defaults.set(frontiers[w],forKey:"frontier_\(w)");defaults.set(streamFrontiers[w],forKey:"stream_frontier_\(w)")}
        if let scores=s["worldBest"] as? [Int], scores.count==8 {
            for w in 0..<8 { defaults.set(min(100_000_000,max(0,scores[w])),forKey:"best_\(w)") }
        }
        var mask=1
        if let ownedList=s["owned"] as? [String]{for item in ownedList {if let index=skins.firstIndex(of:item){mask |= 1 << index}}}
        defaults.set(mask,forKey:"owned_mask")
        let selected=skins.firstIndex(of:s["skin"] as? String ?? "bopi") ?? 0
        defaults.set((mask & (1 << selected)) != 0 ? selected : 0,forKey:"skin_index")
        for stat in ["wins","deaths","flaps","endlessBest","endlessRuns"] {
            let value=min(100_000_000,max(0,s[stat] as? Int ?? 0))
            defaults.set(value,forKey:stat == "endlessBest" ? "endless_best" : stat == "endlessRuns" ? "endless_runs" : stat)
        }
        if let d=s["lastDaily"] as? String,d.range(of:"^\\d{4}-\\d{2}-\\d{2}$",options:.regularExpression) != nil {
            defaults.set(d,forKey:"last_daily")
        }
        if let arr=s["perks"] as? [Int],arr.count==2 {for n in 0..<2 {defaults.set(min(99,max(0,arr[n])),forKey:"perk_\(n)")}}
        if let arr=s["collectibles"] as? [Int],arr.count==8 {for n in 0..<8 {defaults.set(min(100_000_000,max(0,arr[n])),forKey:"collectibles_\(n)")}}
        playerName = s["playerName"] as? String ?? "Igrač"
        difficulty = s["difficulty"] as? Int ?? 1
        let rawBoard=s["leaderboard"] as? [[String:Any]] ?? []
        let cleanBoard=Array(rawBoard.prefix(10)).compactMap { row -> [String:Any]? in
            guard let world=row["world"] as? Int, (0..<8).contains(world),
                  let mode=row["difficulty"] as? Int, (0...2).contains(mode),
                  let score=row["score"] as? Int, (0...100_000_000).contains(score),
                  let gates=row["gates"] as? Int, (0...100_000_000).contains(gates) else {return nil}
            let rawName=row["name"] as? String ?? "Igrač"
            let name=String(rawName.prefix(24).filter {
                $0.isLetter || $0.isNumber || $0 == " " || $0 == "_" || $0 == "-"
            })
            return ["name":name.isEmpty ? "Igrač" : name,"score":score,"world":world,
                    "difficulty":mode,"gates":gates]
        }
        if let bytes=try? JSONSerialization.data(withJSONObject:cleanBoard),
           let text=String(data:bytes,encoding:.utf8) {
            defaults.set(text,forKey:"local_leaderboard")
        }
        soundEnabled = s["soundEnabled"] as? Bool ?? true
        hapticEnabled = s["hapticEnabled"] as? Bool ?? true
        lessMotion = s["lessMotion"] as? Bool ?? false
    }
}
