#!/usr/bin/env python3
"""Source-level wallet/restore contract; device race tests remain required."""
from pathlib import Path

store=(Path(__file__).resolve().parents[1]/
       "android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt").read_text()
purchase=store.split("    @Synchronized fun selectOrBuy(",1)[1].split("    fun endlessBest()",1)[0]
assert "if(owned(index)) return prefs.edit().putInt(\"skin_index\",index).commit()" in purchase
assert '.putInt("coins",' in purchase and '.putInt("owned_mask",' in purchase
assert ".commit()" in purchase and ".apply()" not in purchase

milestone=store.split("    @Synchronized private fun advanceCompletedLevel(",1)[1].split("    val perkNames=",1)[0]
assert "number != streamFrontier(world)" in milestone
assert 'putLong("stream_frontier_$world",number+1)' in milestone
assert 'putInt("frontier_$world",' in milestone
assert 'e.putInt("coins",' in milestone and ').commit())reward else 0' in milestone
assert 'wins().coerceIn(0,99_999_999)+1' in milestone
assert ".apply()" not in milestone

perks=store.split("    @Synchronized fun consumePerks()",1)[1].split("    /** Earned star rating",1)[0]
assert "return if(editor.commit())selected else Pair(0,0f)" in perks
assert ".apply()" not in perks

restore=store.split("    @Synchronized fun importJson(",1)[1]
assert "require(v in 1..6)" in restore
assert "require(contents.length <= 550000)" in restore
assert 'check(e.commit()) { "Spremanje vraćenog napretka nije uspjelo." }' in restore
assert ".apply()" not in restore
print("PASS: purchase, milestone, consumable and v1-v6 restore transaction source guards")
