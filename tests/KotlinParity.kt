package com.brendigo.bopavi

fun main() {
    val numbers = listOf(1,2,3,4,30,60,61,360,361,1001,100000,524289,1048576)
    var checked = 0
    for (w in 0 until 8) {
        val seen=HashSet<String>()
        for (n in 1..6000 step 2) {
            val l=LevelEngine.create(w,n)
            val sig=LevelEngine.signature(l)
            check(seen.add(sig)) { "duplicate $w:$n" }
            check(l.gates.size in 11..15)
            check(l.speed in 140f..212f)
            check(l.gates.all {it.gap>=165f})
            check(l.gates.all {LevelEngine.opening(it,100f).let {o->o.bottom-o.top>=145f} })
            checked++
        }
        for(n in numbers) {
            val l=LevelEngine.create(w,n)
            val fmt=java.lang.String.format(java.util.Locale.US,"%.4f",l.speed)
            val gap=java.lang.String.format(java.util.Locale.US,"%.4f",l.gates[0].gap)
            println("V|$w|$n|${l.zone}|${l.type}|${l.gates.size}|$fmt|$gap|${LevelEngine.signature(l)}")
            check(l.gates.take(10).mapIndexed {i,g -> (((g.center-332f)/26f).toInt() shl (i*2)) }.fold(0) {a,b->a or b} == n-1)
        }
    }
    for ((level,reward) in mapOf(0 to 0,1 to 0,4 to 0,5 to 20,10 to 20,15 to 50,30 to 100,60 to 200,120 to 200,121 to 0)) {
        check(LevelEngine.milestoneReward(level)==reward) { "reward $level" }
    }
    val streams=longArrayOf(1_048_577L, 1_048_583L, 2_097_153L, 10_000_000_000L)
    for(ordinal in streams){
        val x=LevelEngine.createStream(4,ordinal)
        check(x.gates.size>=11 && x.gates.all{LevelEngine.opening(it,123f).let {o->o.bottom-o.top>=145f}})
        println("S|4|$ordinal|${LevelEngine.signature(x)}")
    }
    val continuous=GameSimulation(LevelEngine.createStream(0,1_048_576L),endless=true,initialOrdinal=1_048_576L)
    check(continuous.displayLevel==1_048_576L)
    val s=GameSimulation(LevelEngine.create(0,1))
    check(!s.active);s.flap();check(s.active && s.flaps==1)
    repeat(70){s.step(1f/60f)}
    check(s.time>0f);check(s.y.isFinite())
    println("TEST|KOTLIN|OK|$checked|${numbers.size*8}")
}
