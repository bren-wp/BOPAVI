package com.brendigo.bopavi

fun main() {
    var pass=0;var attempts=0
    val cases=listOf(1,2,30,60,61,90,120,179,240,300,360,361,1001,99999,524289,1048576)
    for(w in 0 until 8)for(n in cases){
        val sim=GameSimulation(LevelEngine.create(w,n))
        for(tick in 0 until 4000){
            if(sim.finished)break
            val goal=sim.level.gates[sim.passed].center
            if(!sim.active || sim.y>goal+8 && sim.velocity > -120)sim.flap()
            sim.step(1f/60f)
        }
        attempts++
        if(sim.won)pass++ else println("FAILED PILOT world=$w level=$n passed=${sim.passed} y=${sim.y} time=${sim.time}")
    }
    println("PILOT RESULTS: $pass/$attempts")
}
