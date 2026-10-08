package com.brendigo.bopavi

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Fixed-coordinate game simulation; Android Canvas is presentation only. */
class GameSimulation(initialLevel: LevelEngine.Level, val endless: Boolean = false, initialShield: Int = 0, initialMagnet: Float = 0f, initialOrdinal:Long = initialLevel.number.toLong()) {
    var level = initialLevel
        private set
    var totalPassed = 0
        private set
    var y = 366f; private set
    var velocity = 0f; private set
    var time = 0f; private set
    var distance = 0f; private set
    var passed = 0; private set
    var coins = 0; private set
    var stars = 0; private set
    var shield = initialShield.coerceIn(0,2); private set
    var magnetTime = initialMagnet.coerceIn(0f,12f); private set
    var collectPulse = 0f; private set
    var impactPulse = 0f; private set
    var completionCount = 0; private set
    var completedLevelNumber = 0; private set
    var displayLevel = initialOrdinal;private set
    var completedOrdinal = 0L;private set
    var invulnerable = 0f; private set
    var flaps = 0; private set
    var active = false; private set
    var finished = false; private set
    var won = false; private set
    private var collectedCoins = BooleanArray(level.gates.size)
    private var collectedStars = BooleanArray(level.gates.size)
    private var collectedPowers = BooleanArray(level.gates.size)
    fun coinVisible(index: Int) = !collectedCoins[index]
    fun starVisible(index: Int) = !collectedStars[index]
    fun powerVisible(index: Int) = !collectedPowers[index]
    val birdX = 126f
    // Body sprite is about 49px wide; a 16px circle missed visible pillar caps.
    val radius = 23f
    fun flap() {
        if (finished) return
        active = true
        velocity = -255f
        flaps++
    }
    fun step(delta: Float) {
        if (!active || finished) return
        val dt = delta.coerceIn(0f, 0.034f)
        time += dt; invulnerable = max(0f, invulnerable - dt); magnetTime = max(0f, magnetTime - dt); collectPulse = max(0f,collectPulse-dt); impactPulse = max(0f,impactPulse-dt)
        velocity = min(365f, velocity + (685f + level.wind) * dt)
        y += velocity * dt
        distance += level.speed * dt
        if (y < radius + 5f || y > 753f - radius) { damage(); y = y.coerceIn(radius + 5f, 753f - radius); return }
        for (i in passed until level.gates.size) {
            val gate = level.gates[i]; val x = gate.x - distance
            if (x > birdX + radius + 115f) break
            val bounds = LevelEngine.opening(gate, time)
            // Collide with the actually DRAWN pillar cap (x-7 .. x+width+5), not
            // only the thinner central shaft. Resolve impact before pickups.
            if (x - 7f < birdX + radius && x + gate.width + 5f > birdX - radius &&
                (y - radius < bounds.top || y + radius > bounds.bottom)) {
                damage()
                if (finished) return
            }
            val cx = x + gate.width * .5f
            val cy = (bounds.top + bounds.bottom) * .5f
            val magnetRange = if (magnetTime > 0f) 108f else 23f
            if(gate.coin && !collectedCoins[i] && abs(cx-birdX) < magnetRange && abs(cy-y) < magnetRange) {coins++;collectedCoins[i]=true;collectPulse=.36f}
            if(gate.star && !collectedStars[i] && abs(cx+35f-birdX) < magnetRange && abs(cy-25f-y) < magnetRange) {stars++;collectedStars[i]=true;collectPulse=.36f}
            if(gate.power!=0 && !collectedPowers[i] && abs(cx+39f-birdX) < 30f && abs(cy+39f-y) < 30f) {
                if(gate.power==1)shield=min(2,shield+1) else magnetTime=5f
                collectedPowers[i]=true;collectPulse=.36f
            }
            if (x + gate.width < birdX - radius && i == passed) {
                passed++; totalPassed++
                if (passed == level.gates.size) {
                    if(endless) {
                        completedLevelNumber = level.number;completedOrdinal=displayLevel;completionCount++
                        displayLevel=(displayLevel+1).coerceAtMost(Long.MAX_VALUE-2)
                        level = LevelEngine.createStream(level.world,displayLevel)
                        distance = 0f; passed = 0
                        collectedCoins = BooleanArray(level.gates.size)
                        collectedStars = BooleanArray(level.gates.size)
                        collectedPowers = BooleanArray(level.gates.size)
                    } else { won = true; finished = true; active = false }
                    return
                }
            }
        }
    }
    private fun damage() {
        if (invulnerable > 0f) return
        if (shield > 0) { shield--; invulnerable = 1.25f; impactPulse = .65f; velocity = -90f }
        else { finished = true; active = false; won = false }
    }
    fun rating(): Int = if(!won)0 else (1+min(2,stars / max(1,level.gates.size/4)))
    fun score(): Int = totalPassed * 100 + coins * 10 + stars * 25
}
