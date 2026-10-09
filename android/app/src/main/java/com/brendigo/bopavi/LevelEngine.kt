package com.brendigo.bopavi

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Platform-independent gameplay rules. Every level is generated on demand. */
object LevelEngine {
    const val WORLD_COUNT = 8
    const val LEVELS_PER_WORLD = 1_048_576
    const val TOTAL_LEVELS = WORLD_COUNT * LEVELS_PER_WORLD
    val names = listOf("Zelene livade", "Sunčana plaža", "Ledeni vrhovi", "Vulkan", "Nebeski hram", "Mračna noć", "Kristalna šuma", "Svemirski let")
    val collectibles = listOf("Zvjezdice", "Školjke", "Pahulje", "Iskre", "Perje", "Mjesečev prah", "Kristali", "Zvjezdani prah")
    val collectibleIcons = listOf("★", "◉", "❄", "✦", "❖", "☾", "◆", "✧")
    val hazards = listOf("trnje", "valovi", "sige", "lava", "stupovi", "sjene", "kristali", "meteori")
    data class Gate(val x: Float, val center: Float, val gap: Float, val width: Float, val movement: Float, val phase: Float, val kind: Int, val coin: Boolean, val star: Boolean, val power: Int)
    data class Level(val world: Int, val number: Int, val zone: Int, val speed: Float, val wind: Float, val gates: List<Gate>, val type: Int)
    data class Opening(val top: Float, val bottom: Float)
    private class Random32(seed: UInt) {
        var state = if (seed == 0u) 0x6d2b79f5u else seed
        fun next(): Float {
            var x = state
            x = x xor (x shl 13); x = x xor (x shr 17); x = x xor (x shl 5)
            state = x
            return ((x shr 8).toInt() and 0xffffff) / 16777216f
        }
    }
    fun create(world: Int, number: Int): Level {
        require(world in 0 until WORLD_COUNT && number in 1..LEVELS_PER_WORLD)
        val phase = (number - 1) % 360
        val zone = phase / 60 // six rotating challenge tiers every 360 levels
        val epoch = (number - 1) / 360
        val type = if (phase % 60 == 59) 3 else if (phase % 30 == 29) 2 else if (phase % 15 == 14) 1 else 0
        val r = Random32((number.toUInt() * 0x9e3779b9u) xor ((world + 11).toUInt() * 0x85ebca6bu))
        val gap = max(168f, 205f - 5f * zone - world * 1.2f - (epoch % 7) * 0.7f + (if(type == 1) 8f else 0f))
        val speed = min(212f, 140f + 8f * zone + world * 2f + (epoch % 6) * 1.4f + (if(type == 2) 5f else 0f))
        val count = 11 + (epoch % 4) + (if (type == 3) 1 else 0)
        val gates = ArrayList<Gate>(count)
        var last = 355f
        for (i in 0 until count) {
            // Ten 2-bit digits give an injective signature for all 2^20 level numbers.
            // No random position is added to these ten centers.
            val center = if (i < 10) 332f + (((number - 1) ushr (i * 2)) and 3) * 26f
                else (last + (r.next() - 0.5f) * 90f).coerceIn(235f, 465f)
            last = center
            val active = (zone >= 2 && i >= 5 && (i + number + world) % 3 == 0) || (type == 2 && i >= 4 && i % 3 == 0) || (world == 4 && i >= 4 && i % 4 == 0)
            gates += Gate(
                x = 560f + i * (242f - zone * 2), center = center,
                gap = gap - r.next() * 3f, width = 64f,
                movement = if (active) 7f + r.next() * 9f else 0f,
                phase = r.next() * 6.283185f,
                kind = world, coin = i % 3 != 2 || type == 1,
                star = i % 4 == 2 || type == 1,
                power = if (i >= 2 && (number + i + world) % 11 == 0) 1 else if (i >= 2 && (number + i + world) % 13 == 0) 2 else 0
            )
        }
        return Level(world, number, zone + 1, speed, if (zone >= 4 && world % 2 == 1) 13f else 0f, gates, type)
    }
    /** Stream any positive ordinal; later cycles remix gate centers instead of ending a world. */
    fun createStream(world:Int,ordinal:Long):Level {
        require(ordinal>0 && ordinal<Long.MAX_VALUE-1)
        val mapped=((ordinal-1) % LEVELS_PER_WORLD).toInt()+1
        val cycle=(ordinal-1)/LEVELS_PER_WORLD
        val base=create(world,mapped)
        if(cycle==0L)return base
        val salt=(cycle%4096L).toInt()
        val remixed=base.gates.mapIndexed{index,gate ->
            val diff=((salt*37L+index*11L)%25L-12L).toFloat()
            gate.copy(center=(gate.center+diff).coerceIn(220f,475f),phase=gate.phase+salt*.017f)
        }
        return base.copy(gates=remixed)
    }
    fun milestoneReward(number:Long):Int = when {
        number <= 0L -> 0
        number % 60L == 0L -> 200
        number % 30L == 0L -> 100
        number % 15L == 0L -> 50
        number % 5L == 0L -> 20
        else -> 0
    }
    fun opening(gate: Gate, time: Float): Opening {
        val frequency = when(gate.kind) { 2 -> 0.65f; 4 -> 1.48f; 5 -> 0.48f; 7 -> 1.27f; else -> 1.1f }
        val delta = if (gate.movement > 0f) sin(time * frequency + gate.phase) * gate.movement else 0f
        // Beach tides move gently; lava/crystal/meteor gates pulsate after activation.
        val tide = when(gate.kind) { 1 -> sin(time*.75f + gate.phase)*5f; 5 -> sin(time*.38f + gate.phase)*5f; else -> 0f }
        val middle = gate.center + delta + tide
        val pulse = if(gate.movement > 0f && (gate.kind == 3 || gate.kind == 6 || gate.kind == 7))
            (sin(time * 1.35f + gate.phase)+1f)*2.5f else 0f
        // The same geometry drives drawing and collision; ≥145px always open.
        val half = max(72.5f, gate.gap * .5f - pulse)
        return Opening(middle - half, middle + half)
    }
    fun signature(level: Level): String = level.gates.take(10).joinToString(":") { it.center.toInt().toString() }
    /** Fixed first-clear grants. No repeat-farming, no in-app payments. */
    fun milestoneReward(number:Int):Int = milestoneReward(number.toLong())
}


/** Presentation-only deterministic drift shared with Swift. Never modifies collisions. */
internal object ParallaxScenery {
    private const val PERIOD = 696f // Four tiles of 174 logical pixels.
    fun offset(distance: Float, layer: Int, reducedMotion: Boolean): Float {
        if (reducedMotion || !distance.isFinite()) return 0f
        val speed = when(layer) { 0 -> .07f; 1 -> .15f; else -> .24f }
        val travelled = (distance * speed) % PERIOD
        return ((travelled + PERIOD) % PERIOD)
    }
}

/** A truthful result headline based on the best score BEFORE saving this run. */
internal object ResultHeadline {
    fun label(score:Int, previousBest:Int, won:Boolean):String = when {
        won -> "LEVEL DOVRŠEN!"
        score > 0 && score > previousBest -> "NOVI REKORD!"
        else -> "LET ZAVRŠEN!"
    }
}
