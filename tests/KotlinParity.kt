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

    // Regression from a real device video: drawn cap enters the bird hitbox
    // before the pillar shaft, and the run must end within this frame.
    run {
        val level=LevelEngine.create(0,2)
        val hit=level.gates[0].copy(x=154f,center=510f,gap=160f,movement=0f,coin=false,star=false,power=0)
        val custom=level.copy(gates=listOf(hit),speed=140f,wind=0f)
        val direct=GameSimulation(custom)
        direct.flap();direct.step(1f/60f)
        check(direct.finished && !direct.won && direct.passed==0) {"collider missed visible cap"}
        val shielded=GameSimulation(custom,initialShield=1)
        shielded.flap();shielded.step(1f/60f)
        check(!shielded.finished && shielded.shield==0) {"shield must absorb one collision"}
    }
    run {
        val level=LevelEngine.create(0,2)
        val safe=level.gates[0].copy(x=154f,center=366f,gap=220f,movement=0f,coin=false,star=false,power=0)
        val run=GameSimulation(level.copy(gates=listOf(safe),speed=140f,wind=0f))
        run.flap();run.step(1f/60f)
        check(!run.finished) {"False collision inside safe gap"}
    }
    run {
        val level=LevelEngine.create(0,2)
        val safe=level.gates[0].copy(x=20f,center=366f,gap=220f,movement=0f,coin=false,star=false,power=0)
        val game=GameSimulation(level.copy(gates=listOf(safe),speed=140f,wind=0f),endless=true)
        game.flap();game.step(1f/60f)
        check(game.completionCount==1 && game.displayLevel==3L)
        check(game.distance>0 && kotlin.math.abs(game.gateX(game.level.gates[0])-300f)<0.01f)
        check(game.active && !game.finished)
    }
    run {
        val base=LevelEngine.create(0,2)
        val smooth=GameSimulation(base)
        val delayed=GameSimulation(base)
        smooth.flap();delayed.flap()
        repeat(6){smooth.step(1f/60f)}
        delayed.step(0.10f)
        check(kotlin.math.abs(smooth.distance-delayed.distance)<.02f)
        check(kotlin.math.abs(smooth.y-delayed.y)<3f)
        val paused=GameSimulation(base)
        paused.flap();paused.step(8f)
        check(kotlin.math.abs(paused.time-.10f)<.0001f)
        val invalid=GameSimulation(base)
        invalid.flap();invalid.step(Float.NaN)
        check(invalid.time==0f && invalid.distance==0f)
    }
    // Exact scene-depth contract, with no change to the flight simulation.
    for (layer in 0..2) {
        val expected=floatArrayOf(70f,150f,240f)[layer]
        check(kotlin.math.abs(ParallaxScenery.offset(1000f,layer,false)-expected)<.001f)
        check(ParallaxScenery.offset(1000f,layer,true)==0f)
        check(ParallaxScenery.offset(Float.NaN,layer,false)==0f)
        check(ParallaxScenery.offset(Float.POSITIVE_INFINITY,layer,false)==0f)
        val off=ParallaxScenery.offset(999999f,layer,false)
        check(off>=0f && off<696f)
    }
    // v0.1.20: a new tap triggers a 240 ms cosmetic upstroke.
    run {
        val b=GameSimulation(LevelEngine.create(0,2))
        check(b.flapPulse==0f)
        b.flap()
        check(b.flapPulse==.24f && b.flaps==1)
        b.step(.08f)
        check(kotlin.math.abs(b.flapPulse-.16f)<.0001f)
        b.step(Float.NaN)
        check(kotlin.math.abs(b.flapPulse-.16f)<.0001f)
        b.flap()
        check(b.flapPulse==.24f && b.flaps==2)
        repeat(3){b.step(.10f)}
        check(b.flapPulse==0f && !b.finished)
    }
    // v0.1.21: local personal record must compare against the prior best.
    check(ResultHeadline.label(0,0,false)=="LET ZAVRŠEN!")
    check(ResultHeadline.label(100,100,false)=="LET ZAVRŠEN!")
    check(ResultHeadline.label(99,100,false)=="LET ZAVRŠEN!")
    check(ResultHeadline.label(101,100,false)=="NOVI REKORD!")
    check(ResultHeadline.label(101,100,true)=="LEVEL DOVRŠEN!")
    // v0.1.22: all four level-type badges follow the real generator.
    for ((number,kind) in listOf(1 to 0,15 to 1,30 to 2,60 to 3,75 to 1,90 to 2,120 to 3,360 to 3)) {
        check(LevelEngine.create(0,number).type==kind)
        check(LevelKind.name(kind)==listOf("Normalni","Izazovni","Bonus","Elitni")[kind])
        check(LevelKind.icon(kind)==listOf("●","⚡","✦","♛")[kind])
    }
    // v0.1.23: idle intro must not auto-run physics or count a flap.
    run {
        val idle=GameSimulation(LevelEngine.create(0,1))
        check(!idle.active && idle.flaps==0 && idle.time==0f)
        idle.step(.1f)
        check(!idle.active && idle.flaps==0 && idle.distance==0f)
        idle.flap()
        check(idle.active && idle.flaps==1)
        idle.flap()
        check(idle.active && idle.flaps==2)
    }
    println("TEST|KOTLIN|OK|$checked|${numbers.size*8}")
}
