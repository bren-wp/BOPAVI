package com.brendigo.bopavi

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Fixed-coordinate game simulation; Android Canvas is presentation only. */
class GameSimulation(initialLevel: LevelEngine.Level, val endless: Boolean = false, initialShield: Int = 0, initialMagnet: Float = 0f, difficulty: Int = 1, initialOrdinal:Long = initialLevel.number.toLong()) {
    val difficulty = difficulty.coerceIn(0,2)
    private val speedFactor = when(this.difficulty) { 0 -> .73f; 2 -> 1.30f; else -> 1f }
    private val gravityFactor = when(this.difficulty) { 0 -> .84f; 2 -> 1.15f; else -> 1f }
    var level = initialLevel
        private set
    var totalPassed = 0
        private set
    var y = 366f; private set
    var velocity = 0f; private set
    var time = 0f; private set
    var distance = 0f; private set
    // World coordinates grow beyond Float's pixel precision during marathon runs.
    // Retain public Float distance for scenery, but resolve collision geometry in Double.
    private var preciseDistance = 0.0
    private var levelOrigin = 0.0
    private var nextLevel:LevelEngine.Level? = null
    private var nextOrigin = 0.0
    /** Incoming gates retain the same world coordinates before and after promotion. */
    fun gateX(gate: LevelEngine.Gate):Float = WorldCoordinates.screenX(gate.x, levelOrigin, preciseDistance)
    /** Continuous distance-based HUD ratio, not a staircase of completed gates. */
    val levelProgress:Float get() {
        val last = level.gates.lastOrNull() ?: return 0f
        val finish = last.x.toDouble() + last.width.toDouble() + radius - birdX
        if (finish <= 0.0) return 1f
        return ((preciseDistance - levelOrigin) / finish).toFloat().coerceIn(0f,1f)
    }
    fun upcomingGates():List<LevelEngine.Gate> = nextLevel?.gates ?: emptyList()
    fun upcomingGateX(gate:LevelEngine.Gate):Float = WorldCoordinates.screenX(gate.x, nextOrigin, preciseDistance)
    private fun prepareNext() {
        if(!endless || displayLevel>=Long.MAX_VALUE-3L){nextLevel=null;return}
        val generated=LevelEngine.createStream(level.world,displayLevel+1)
        var previous=level.gates.last().center
        val upcoming=generated.copy(gates=generated.gates.map { gate ->
            val center=gate.center.coerceIn(previous-82f,previous+82f)
            previous=center
            gate.copy(center=center)
        })
        // A level boundary is not a gap or a special shortened/lengthened beat.
        // Match the current level's exact 218..228 px gate cadence so the next
        // visible obstacle is positioned in the same uninterrupted world stream.
        val ordinarySpacing=if(level.gates.size>=2)
            level.gates.last().x-level.gates[level.gates.lastIndex-1].x else 228f
        val transitionSpacing=ordinarySpacing
        nextOrigin=levelOrigin+level.gates.last().x.toDouble()+transitionSpacing.toDouble()-upcoming.gates.first().x.toDouble()
        nextLevel=upcoming
    }
    var levelTransition = 0f; private set
    var passed = 0; private set
    var coins = 0; private set
    /** Run-only berry count, never spendable virtual coins. */
    var foodCollected = 0; private set
    var stars = 0; private set
    /** Separate current-level pickups from the lifetime marathon score. */
    var levelStars = 0; private set
    var completedLevelStars = 0; private set
    var shield = initialShield.coerceIn(0,3); private set
    var magnetTime = initialMagnet.coerceIn(0f,18f); private set
    var collectPulse = 0f; private set
    var impactPulse = 0f; private set
    // Presentation-only impulse: responsive wing beat, no effect on collision or score.
    var flapPulse = 0f; private set
    var completionCount = 0; private set
    var displayLevel = initialOrdinal;private set
    var completedOrdinal = 0L;private set
    var invulnerable = 0f; private set
    var flaps = 0; private set
    var active = false; private set
    var finished = false; private set
    var won = false; private set
    init { prepareNext() }
    private var collectedFood = BooleanArray(level.gates.size)
    private var collectedCoins = BooleanArray(level.gates.size)
    private var collectedStars = BooleanArray(level.gates.size)
    private var collectedPowers = BooleanArray(level.gates.size)
    fun foodVisible(index: Int) = index in collectedFood.indices && !collectedFood[index]
    fun coinVisible(index: Int) = !collectedCoins[index]
    fun starVisible(index: Int) = !collectedStars[index]
    fun powerVisible(index: Int) = !collectedPowers[index]
    val birdX = 126f
    // Tight body-only hitbox; wings and transparent sprite padding never collide.
    val radius = 18f
    fun flap() {
        if (finished) return
        active = true
        velocity = -255f
        flapPulse = .24f
        flaps++
    }
    fun step(delta: Float) {
        if (!active || finished) return
        // Preserve short frame stalls; a long background interruption is bounded.
        if (!delta.isFinite() || delta <= 0f) return
        var remaining = min(delta, 0.10f)
        // Bound semi-implicit Euler integration to 120Hz on every display.
        // 30Hz used to jump ~5px relative to 120Hz within 600ms because
        // frame time itself was the physics slice; preserve impulse/gravity.
        while (remaining > 0.000001f && !finished) {
            val slice = min(remaining, 1f / 120f)
            advance(slice)
            remaining -= slice
        }
    }
    private fun advance(dt: Float) {
        val previousY=y
        val previousTime=time
        time += dt; levelTransition = max(0f,levelTransition-dt); invulnerable = max(0f, invulnerable - dt); magnetTime = max(0f, magnetTime - dt); collectPulse = max(0f,collectPulse-dt); impactPulse = max(0f,impactPulse-dt); flapPulse = max(0f,flapPulse-dt)
        // All eight worlds use the same tap impulse, gravity and terminal speed.
        velocity = min(365f, velocity + (685f * gravityFactor + level.wind) * dt)
        y += velocity * dt
        preciseDistance += (level.speed * speedFactor * dt).toDouble()
        distance = preciseDistance.toFloat()
        if (y < radius + 5f || y > 753f - radius) { damage(); y = y.coerceIn(radius + 5f, 753f - radius); return }
        for (i in passed until level.gates.size) {
            val gate = level.gates[i]; val x = gateX(gate)
            if (x > birdX + radius + 115f) break
            val bounds = LevelEngine.opening(gate, time, difficulty)
            // Check the circular body against the separately painted shaft and
            // lip. The old full-height cap AABB killed Bopi near empty corners.
            if (PillarCollision.bodyHitsPillar(birdX,y,radius,x,gate.width,bounds)) {
                damage()
                if (finished) return
            }
            val cx = x + gate.width * .5f
            val cy = (bounds.top + bounds.bottom) * .5f
            // Sweep the real fruit/body relative motion across this physics
            // slice. Endpoint-only contact missed fast edge grazes on 30Hz
            // devices; the collider and safe gate opening stay unchanged.
            if(endless && gate.coin && !collectedFood[i]) {
                val oldOpening=LevelEngine.opening(gate,previousTime,difficulty)
                val oldCy=(oldOpening.top+oldOpening.bottom)*.5f
                val travel=level.speed*speedFactor*dt
                if(BerryCollision.touched(
                    cx+travel-birdX,oldCy-previousY,
                    cx-birdX,cy-y,radius+13f)) {
                    collectedFood[i]=true;foodCollected++;collectPulse=.36f
                }
            }
            val magnetRange = if (magnetTime > 0f) 108f else 23f
            if(!endless && gate.coin && !collectedCoins[i] && abs(cx-birdX) < magnetRange && abs(cy-y) < magnetRange) {coins++;collectedCoins[i]=true;collectPulse=.36f}
            if(!endless && gate.star && !collectedStars[i] && abs(cx+35f-birdX) < magnetRange && abs(cy-25f-y) < magnetRange) {stars++;levelStars++;collectedStars[i]=true;collectPulse=.36f}
            if(!endless && gate.power!=0 && !collectedPowers[i] && abs(cx+39f-birdX) < 30f && abs(cy+39f-y) < 30f) {
                if(gate.power==1)shield=min(3,shield+1) else magnetTime=max(magnetTime,5f)
                collectedPowers[i]=true;collectPulse=.36f
            }
            if (x + gate.width < birdX - radius && i == passed) {
                passed++; totalPassed++
                if (passed == level.gates.size) {
                    completedLevelStars=levelStars.coerceIn(0,3)
                    if(endless) {
                        completedOrdinal=displayLevel;completionCount++
                        displayLevel=(displayLevel+1).coerceAtMost(Long.MAX_VALUE-2)
                        // Upcoming gates have already entered the viewport.
                        // Promote the exact cached world position; no spawn or jump.
                        val incoming=nextLevel ?: LevelEngine.createStream(level.world,displayLevel)
                        levelOrigin=nextOrigin
                        level=incoming
                        prepareNext()
                        levelTransition=.85f
                        passed = 0
                        levelStars = 0
                        collectedFood = BooleanArray(level.gates.size)
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
    fun score(): Int = (totalPassed.toLong() * 100L + (if(endless) 0L else coins.toLong() * 10L + stars.toLong() * 25L)).coerceIn(0L,100_000_000L).toInt()
}

/** Screen-space projection must subtract in Double before the final Float conversion. */
internal object WorldCoordinates {
    fun screenX(gateX:Float, origin:Double, travelled:Double):Float =
        (gateX.toDouble() + origin - travelled).toFloat()
}

/** Exact circle-vs-painted-rectangle contact, shared geometrically with Swift.
 * The wide overhang exists only in the 28px/27px visible lip bands, never
 * all the way to the top or bottom of the world.
 */
internal object PillarCollision {
    fun bodyHitsPillar(birdX:Float,birdY:Float,radius:Float,gateX:Float,
                       gateWidth:Float,opening:LevelEngine.Opening):Boolean {
        if (!birdX.isFinite() || !birdY.isFinite() || !radius.isFinite() ||
            !gateX.isFinite() || !gateWidth.isFinite() || !opening.top.isFinite() ||
            !opening.bottom.isFinite() || radius<=0f || gateWidth<=0f || opening.top>=opening.bottom) return false
        val right=gateX+gateWidth
        return touchesRectangle(birdX,birdY,radius,gateX,-25f,right,opening.top) ||
            touchesRoundedBand(birdX,birdY,radius,gateX-7f,opening.top-28f,right+5f,opening.top) ||
            touchesRectangle(birdX,birdY,radius,gateX,opening.bottom,right,825f) ||
            touchesRoundedBand(birdX,birdY,radius,gateX-7f,opening.bottom,right+5f,opening.bottom+27f)
    }
    /** Exact circle vs 7px-radius painted lip, not its transparent AABB corners. */
    private fun touchesRoundedBand(cx:Float,cy:Float,r:Float,left:Float,top:Float,
                                   right:Float,bottom:Float):Boolean {
        val corner=7f
        val hw=(right-left)*.5f; val hh=(bottom-top)*.5f
        val qx=abs(cx-(left+right)*.5f)-(hw-corner)
        val qy=abs(cy-(top+bottom)*.5f)-(hh-corner)
        return kotlin.math.hypot(max(0f,qx),max(0f,qy))+min(max(qx,qy),0f)-corner<r
    }
    private fun touchesRectangle(cx:Float,cy:Float,r:Float,left:Float,top:Float,
                                 right:Float,bottom:Float):Boolean {
        val dx=maxOf(0f,left-cx,cx-right)
        val dy=maxOf(0f,top-cy,cy-bottom)
        return dx*dx+dy*dy < r*r
    }
}

/** Segment-to-point contact of a moving berry relative to Bopi's body.
 * No heap allocations, sprite bounds, magnetic pickup or virtual currency.
 */
internal object BerryCollision {
    fun touched(startX:Float,startY:Float,endX:Float,endY:Float,contactRadius:Float):Boolean {
        if(!startX.isFinite() || !startY.isFinite() || !endX.isFinite() ||
            !endY.isFinite() || !contactRadius.isFinite() || contactRadius<=0f) return false
        val dx=endX-startX
        val dy=endY-startY
        val lengthSquared=dx*dx+dy*dy
        val t=if(lengthSquared>0f) ((-startX*dx-startY*dy)/lengthSquared).coerceIn(0f,1f) else 0f
        val nearestX=startX+dx*t
        val nearestY=startY+dy*t
        return nearestX*nearestX+nearestY*nearestY<=contactRadius*contactRadius
    }
}
