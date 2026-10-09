package com.brendigo.bopavi

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Offline wallet: coins are earned once, on designated newly completed levels. */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("bopavi_native_v4", Context.MODE_PRIVATE)
    fun frontier(world: Int): Int = prefs.getInt("frontier_$world", 1).coerceIn(1, LevelEngine.LEVELS_PER_WORLD + 1)
    fun streamFrontier(world:Int):Long {
        if(world !in 0..7)return 1L
        return prefs.getLong("stream_frontier_$world",frontier(world).toLong()).coerceIn(1L,Long.MAX_VALUE-2)
    }
    fun chosenWorld():Int = prefs.getInt("chosen_world",0).coerceIn(0,maxWorld())
    fun chooseWorld(world:Int){if(world in 0..maxWorld())prefs.edit().putInt("chosen_world",world).apply()}
    fun maxWorld(): Int = 7 // All eight worlds are available; stored progress remains untouched.
    fun coins(): Int = prefs.getInt("coins", 0).coerceIn(0,100000000)
    fun best(world: Int): Int = prefs.getInt("best_$world", 0)
    val skins = listOf("bopi", "sunny", "berry", "luna", "mint", "shadow")
    val skinNames = listOf("Bopi", "Sunny", "Berry", "Luna", "Mint", "Shadow")
    val costs = listOf(0, 60, 80, 110, 130, 160)
    fun skin(): Int = prefs.getInt("skin_index", 0).coerceIn(0, 5)
    fun owned(index: Int): Boolean = index in 0..5 && ((prefs.getInt("owned_mask", 1) ushr index) and 1) != 0
    fun selectOrBuy(index: Int): Boolean {
        if(index !in 0..5)return false
        if(owned(index)) {prefs.edit().putInt("skin_index",index).apply();return true}
        if(coins() < costs[index])return false
        prefs.edit().putInt("coins",coins()-costs[index]).putInt("owned_mask",prefs.getInt("owned_mask",1) or (1 shl index)).putInt("skin_index",index).apply()
        return true
    }
    fun endlessBest(): Int = prefs.getInt("endless_best",0)
    fun wins(): Int = prefs.getInt("wins",0)
    fun deaths(): Int = prefs.getInt("deaths",0)
    fun recordRun(game: GameSimulation) {
        val edit=prefs.edit()
        edit.putInt("collectibles_${game.level.world}", (prefs.getInt("collectibles_${game.level.world}",0).toLong()+game.coins+game.stars).coerceAtMost(100000000L).toInt())
        edit.putLong("flaps",(prefs.getLong("flaps",0)+game.flaps).coerceAtMost(100000000L))
        if(game.endless) {
            edit.putInt("endless_best",maxOf(endlessBest(),game.totalPassed))
            edit.putInt("endless_runs",prefs.getInt("endless_runs",0)+1)
        }
        if(game.won)edit.putInt("wins",wins()+1) else edit.putInt("deaths",deaths()+1)
        edit.apply()
    }
    fun lessMotion(): Boolean = prefs.getBoolean("less_motion", false)
    fun setLessMotion(value: Boolean) = prefs.edit().putBoolean("less_motion", value).apply()
    /** Returns reward; retries and previously completed levels never mint currency again. */
    fun completeLevel(world:Int, number:Long, score:Int):Int {
        if(world !in 0..7 || number != streamFrontier(world) || world > maxWorld() || number < 1L || number >= Long.MAX_VALUE-2) return 0
        val reward=LevelEngine.milestoneReward(number)
        val e=prefs.edit().putLong("stream_frontier_$world",number+1)
            .putInt("frontier_$world",(number+1).coerceAtMost(LevelEngine.LEVELS_PER_WORLD.toLong()+1).toInt())
        if(world==maxWorld() && number>=30 && world<7)e.putInt("max_world",world+1)
        e.putInt("best_$world",maxOf(best(world),score)).putInt("wins",wins()+1)
        e.putInt("coins",(coins().toLong()+reward).coerceAtMost(100000000L).toInt()).apply()
        return reward
    }
    val perkNames=listOf("Početni štit", "Početni magnet")
    val perkPrices=listOf(80,65)
    fun perkCount(index:Int):Int = if(index in 0..1) prefs.getInt("perk_$index",0).coerceIn(0,99) else 0
    fun buyPerk(index:Int):Boolean {
        if(index !in 0..1 || perkCount(index)>=99 || coins()<perkPrices[index]) return false
        prefs.edit().putInt("coins",coins()-perkPrices[index]).putInt("perk_$index",perkCount(index)+1).apply()
        return true
    }
    /** Bought perks are consumed once per new flight, including retry. */
    fun consumePerks():Pair<Int,Float> {
        val shield=perkCount(0); val magnet=perkCount(1)
        prefs.edit().putInt("perk_0",maxOf(0,shield-1)).putInt("perk_1",maxOf(0,magnet-1)).apply()
        return Pair(if(shield>0)1 else 0,if(magnet>0)8f else 0f)
    }
    fun collectibles(world:Int):Int = if(world in 0..7) prefs.getInt("collectibles_$world",0) else 0
    fun playerName(): String = prefs.getString("player_name", "Igrač") ?: "Igrač"
    fun setPlayerName(value: String) {
        val clean = value.trim().take(24).filter { it.isLetterOrDigit() || it == ' ' || it == '_' || it == '-' }
        prefs.edit().putString("player_name", clean.ifBlank { "Igrač" }).apply()
    }
    fun soundEnabled():Boolean = prefs.getBoolean("sound_enabled",true)
    fun setSoundEnabled(enabled:Boolean) {prefs.edit().putBoolean("sound_enabled",enabled).apply()}

    fun exportJson(): String {
        val s = JSONObject()
        s.put("version", 5)
        s.put("frontiers", JSONArray((0..7).map { frontier(it) }))
        s.put("streamFrontiers",JSONArray((0..7).map { streamFrontier(it).toString() }))
        s.put("maxWorld", maxWorld()); s.put("chosenWorld",chosenWorld()); s.put("playerName",playerName()); s.put("coins", coins()); s.put("lessMotion", lessMotion())
        s.put("worldBest", JSONArray((0..7).map { best(it) }))
        s.put("owned",JSONArray((0..5).filter{owned(it)}.map{skins[it]}))
        s.put("skin",skins[skin()]);s.put("lastDaily",prefs.getString("last_daily", ""))
        s.put("wins",wins());s.put("deaths",deaths());s.put("flaps",prefs.getLong("flaps",0L))
        s.put("endlessBest",endlessBest());s.put("endlessRuns",prefs.getInt("endless_runs",0))
        s.put("perks",JSONArray((0..1).map{perkCount(it)}))
        s.put("collectibles",JSONArray((0..7).map{collectibles(it)}));s.put("soundEnabled",soundEnabled())
        return JSONObject().put("format", "bopavi-save").put("exportVersion", 5).put("save", s).toString(2)
    }
    fun importJson(contents: String) {
        require(contents.length <= 550000) { "Datoteka je prevelika." }
        val body = JSONObject(contents)
        require(body.optString("format") == "bopavi-save")
        val s = body.getJSONObject("save")
        val v = s.optInt("version", 0)
        require(v in 1..5)
        val frontiers = IntArray(8) { 1 }
        var maxWorld = 0
        if (v >= 3) {
            val f = s.getJSONArray("frontiers")
            require(f.length() == 8)
            for (w in 0..7) { val value = f.getInt(w); require(value in 1..LevelEngine.LEVELS_PER_WORLD + 1); frontiers[w] = value }
            maxWorld = s.getInt("maxWorld"); require(maxWorld in 0..7)
        } else {
            val old = s.optInt("unlocked", 1).coerceIn(1, 2880)
            maxWorld = (old - 1) / 360
            for (w in 0 until maxWorld) frontiers[w] = 361
            frontiers[maxWorld] = (old - 1) % 360 + 1
        }
        val importedCoins = s.optInt("coins", 0); require(importedCoins in 0..100000000)
        val chosen=s.optInt("chosenWorld",maxWorld).coerceIn(0,maxWorld)
        val e = prefs.edit().putInt("max_world", maxWorld).putInt("chosen_world",chosen).putInt("coins", importedCoins)
        val streams=s.optJSONArray("streamFrontiers")
        val streamValues=LongArray(8){frontiers[it].toLong()}
        if(v>=5 && streams!=null){
            require(streams.length()==8)
            for(w in 0..7){
                val x=streams.getString(w).toLongOrNull() ?: error("Neispravan broj levela")
                require(x in frontiers[w].toLong()..(Long.MAX_VALUE-2))
                streamValues[w]=x
            }
        }
        for (w in 0..7){e.putInt("frontier_$w", frontiers[w]);e.putLong("stream_frontier_$w",streamValues[w])}
        val best = s.optJSONArray("worldBest")
        if (best != null && best.length() == 8) for (w in 0..7) e.putInt("best_$w", best.optInt(w, 0).coerceIn(0, 100000000))
        val ownedArray=s.optJSONArray("owned")
        var mask=1
        if(ownedArray!=null)for(i in 0 until ownedArray.length()){
            val ix=skins.indexOf(ownedArray.optString(i));if(ix>=0)mask=mask or (1 shl ix)
        }
        val selected=skins.indexOf(s.optString("skin","bopi"))
        e.putInt("owned_mask",mask).putInt("skin_index",if(selected>=0 && (mask and (1 shl selected))!=0)selected else 0)
        e.putInt("wins",s.optInt("wins",0).coerceIn(0,100000000))
        e.putInt("deaths",s.optInt("deaths",0).coerceIn(0,100000000))
        e.putLong("flaps",s.optLong("flaps",0).coerceIn(0L,100000000L))
        e.putInt("endless_best",s.optInt("endlessBest",0).coerceIn(0,100000000))
        e.putInt("endless_runs",s.optInt("endlessRuns",0).coerceIn(0,100000000))
        val daily=s.optString("lastDaily","")
        if(daily.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))e.putString("last_daily",daily)
        val perkData=s.optJSONArray("perks")
        if(perkData!=null && perkData.length()==2)for(j in 0..1)e.putInt("perk_$j",perkData.optInt(j,0).coerceIn(0,99))
        val collection=s.optJSONArray("collectibles")
        if(collection!=null && collection.length()==8)for(j in 0..7)e.putInt("collectibles_$j",collection.optInt(j,0).coerceIn(0,100000000))
        val restoredName = s.optString("playerName", "Igrač").trim().take(24).filter {
            it.isLetterOrDigit() || it == ' ' || it == '_' || it == '-'
        }.ifBlank { "Igrač" }
        e.putString("player_name", restoredName)
        e.putBoolean("sound_enabled",s.optBoolean("soundEnabled",true))
        e.putBoolean("less_motion", s.optBoolean("lessMotion", false)); e.apply()
    }
}
