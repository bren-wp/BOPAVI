package com.brendigo.bopavi

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Fixed-coordinate game simulation; Android Canvas is presentation only. */
class GameSimulation(initialLevel: LevelEngine.Level, val endless: Boolean = false, initialShield: Int = 0, initialMagnet: Float = 0f, difficulty: Int = 1, initialOrdinal:Long = initialLevel.number.toLong(), val pilotAbility:PilotAbility = PilotAbility.CLASSIC) {
    val difficulty = difficulty.coerceIn(0,2)
    private val speedFactor = (when(this.difficulty) { 0 -> .73f; 2 -> 1.30f; else -> 1f }) * pilotAbility.speedMultiplier
    private val gravityFactor = (when(this.difficulty) { 0 -> .84f; 2 -> 1.15f; else -> 1f }) * pilotAbility.gravityMultiplier
    var level = initialLevel
        private set
    // Keep world scrolling continuous when procedural obstacle batches switch
    // challenge zones. This affects only horizontal travel, never tap impulse,
    // gravity, collider size or the precomputed positions of incoming gates.
    private var scrollSpeed = initialLevel.speed * speedFactor
    internal val currentScrollSpeed:Float get() = scrollSpeed
    var totalPassed = 0
        private set
    var y = 366f; private set
    var velocity = 0f; private set
    var time = 0f; private set
    var distance = 0f; private set
    // World coordinates grow beyond Float's pixel precision during marathon runs.
    // Retain public Float distance for scenery, but resolve collision geometry in Double.
    private var preciseDistance = 0.0
    /** Draw-only scenery clock: keeps subpixel motion in marathon sessions. */
    internal val preciseTravel:Double get() = preciseDistance
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
    /** Endless-only forgiving arcade lives, never spendable currency. */
    var lives = if(endless)3 else 1; private set
    val maxLives = if(endless)3 else 1
    var heartsCollected = 0; private set
    // Lisac's one-shot pilot skill is local to this endless run, never saved
    // or restored at the next launch. All pilots retain the same 18px hitbox.
    var pilotGuardReady = endless && pilotAbility == PilotAbility.LISAC; private set
    var magnetTime = initialMagnet.coerceIn(0f,18f); private set
    var collectPulse = 0f; private set
    var impactPulse = 0f; private set
    /** Distinguishes lost-life feedback from a consumed legacy shield. */
    var lastImpactLostLife = false; private set
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
    private var collectedHearts = BooleanArray(level.gates.size)
    /** Every seventeenth gate in the uninterrupted stream has a rare heart.
     * Derive the global ordinal from completed gates, never from a segment
     * number or its variable length; preview and promoted coordinates agree.
     */
    private fun heartAt(index:Int,incoming:Boolean=false):Boolean {
        val base=(totalPassed-passed).toLong()
        val extra=if(incoming)level.gates.size.toLong() else 0L
        return endless && (base+extra+index+1L)%17L==0L
    }
    fun isHeartPosition(index:Int,incoming:Boolean=false):Boolean {
        val segment=if(incoming)nextLevel else level
        return segment!=null && index in segment.gates.indices && heartAt(index,incoming)
    }
    fun heartVisible(index:Int,incoming:Boolean=false):Boolean {
        if(lives>=maxLives)return false
        val segment=if(incoming)nextLevel else level
        return segment!=null && index in segment.gates.indices &&
            heartAt(index,incoming) &&
            (incoming || (!collectedHearts[index] && !collectedFood[index]))
    }
    /** One visible reward per gate. Full-life heart gates give normal berries;
     * once any reward is collected this gate cannot award a second item.
     */
    fun berryVisible(index:Int,incoming:Boolean=false):Boolean {
        val segment=if(incoming)nextLevel else level
        if(segment==null || index !in segment.gates.indices ||
            !segment.gates[index].coin)return false
        if(!incoming && (collectedFood[index] || collectedHearts[index]))return false
        return !heartAt(index,incoming) || lives>=maxLives
    }
    private var collectedCoins = BooleanArray(level.gates.size)
    private var collectedStars = BooleanArray(level.gates.size)
    private var collectedPowers = BooleanArray(level.gates.size)
    /** Render only real finite-level pickups. Appearance and the 23px/30px
     * pickup ranges come from the same gate/index as the simulation.
     * Read-only visibility never consumes an item or allocates per frame. */
    fun coinVisible(index:Int,incoming:Boolean=false):Boolean =
        !endless && !incoming && index in level.gates.indices &&
            level.gates[index].coin && !collectedCoins[index]
    fun crystalVisible(index:Int,incoming:Boolean=false):Boolean =
        !endless && !incoming && index in level.gates.indices &&
            level.gates[index].star && !collectedStars[index]
    fun powerVisible(index:Int,incoming:Boolean=false):Boolean =
        !endless && !incoming && index in level.gates.indices &&
            level.gates[index].power!=0 && !collectedPowers[index]
    /** No new pickup arrays on identical-size marathon batches. */
    private fun clearOrResize(flags:BooleanArray,size:Int):BooleanArray {
        if(flags.size!=size)return BooleanArray(size)
        flags.fill(false)
        return flags
    }
    fun foodVisible(index: Int) = index in collectedFood.indices && !collectedFood[index]
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
        // Prepare the next obstacle batch near the MIDPOINT of a level,
        // far before it enters the screen. Procedural work and new boolean
        // arrays must not happen in the precise frame a level is passed.
        if(endless && nextLevel==null && passed>=level.gates.size/2)prepareNext()
        val previousY=y
        val previousTime=time
        time += dt; levelTransition = max(0f,levelTransition-dt); invulnerable = max(0f, invulnerable - dt); magnetTime = max(0f, magnetTime - dt); collectPulse = max(0f,collectPulse-dt); impactPulse = max(0f,impactPulse-dt); flapPulse = max(0f,flapPulse-dt)
        // Every world shares the same tap impulse and 18px body collider.
        // Sova's 315-unit terminal descent gives distinct fall control without
        // modifying collision radius or obscuring actual pillar positions.
        velocity = min(pilotAbility.terminalFallSpeed,
            velocity + (685f * gravityFactor + level.wind) * dt)
        y += velocity * dt
        // Old batches could change their speed by a visible jump at promotion.
        // Accelerate gently toward the next batch's target, using the same
        // 120 Hz physics slices so 30/60/120 Hz devices agree on the movement.
        val targetSpeed=level.speed*speedFactor
        val maxStep=85f*dt
        scrollSpeed+=(targetSpeed-scrollSpeed).coerceIn(-maxStep,maxStep)
        preciseDistance += (scrollSpeed * dt).toDouble()
        distance = preciseDistance.toFloat()
        if (y < radius + 5f || y > 753f - radius) { damage(); y = y.coerceIn(radius + 5f, 753f - radius); return }
        for (i in passed until level.gates.size) {
            val gate = level.gates[i]; val x = gateX(gate)
            // Endless berries are picked up only by actual body contact.
            // Gates farther than their painted 7px lip overhang plus the 18px
            // bird radius cannot collide during this 120Hz slice; neither can
            // their centre-mounted berries (minimum generated gate width is 54px).
            // Legacy finite levels keep the wider 115px magnet look-ahead.
            // Avoid two Opening objects and swept-CCD work on distant gates.
            if (x > birdX + radius + (if(endless) 7f else 115f)) break
            val bounds = LevelEngine.opening(gate, time, difficulty)
            // Relative continuous collision sees a shallow pass through a rounded
            // cap even when both 120Hz endpoints are clear. Moving lip bounds
            // are weighted with Bopi/gate motion, not a larger hitbox.
            val oldBounds=LevelEngine.opening(gate,previousTime,difficulty)
            // Match the swept collider to the exact distance applied above.
            val travel=scrollSpeed*dt
            if (PillarCollision.sweptBodyHitsPillar(birdX,previousY,y,radius,
                    x+travel,x,gate.width,oldBounds,bounds)) {
                damage()
                if (finished) return
            }
            val cx = x + gate.width * .5f
            val cy = (bounds.top + bounds.bottom) * .5f
            // Sweep the real fruit/body relative motion across this physics
            // slice. Endpoint-only contact missed fast edge grazes on 30Hz
            // devices; the collider and safe gate opening stay unchanged.
            if(endless && heartAt(i) && !collectedHearts[i] &&
                    !collectedFood[i] && lives<maxLives) {
                val oldCy=(oldBounds.top+oldBounds.bottom)*.5f
                if(BerryCollision.touched(cx+travel-birdX,oldCy-previousY,
                        cx-birdX,cy-y,radius+13f)) {
                    collectedHearts[i]=true
                    lives=min(maxLives,lives+1)
                    heartsCollected++
                    collectPulse=.36f
                }
            }
            if(endless && gate.coin && !collectedFood[i] &&
                    !collectedHearts[i] && (!heartAt(i) || lives>=maxLives)) {
                val oldCy=(oldBounds.top+oldBounds.bottom)*.5f
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
                        // The successor was cached ahead of time. Postpone
                        // generating the next-next batch until midway through
                        // this new segment to avoid a one-frame seam stall.
                        nextLevel=null
                        levelTransition=.85f
                        passed = 0
                        levelStars = 0
                        // Reuse same-sized arrays on every marathon batch.
                        // Reset *all* pickup flags so rewards cannot leak to
                        // the next gate stream; resize only if gate count changes.
                        val gateCount=level.gates.size
                        collectedFood = clearOrResize(collectedFood,gateCount)
                        collectedHearts = clearOrResize(collectedHearts,gateCount)
                        collectedCoins = clearOrResize(collectedCoins,gateCount)
                        collectedStars = clearOrResize(collectedStars,gateCount)
                        collectedPowers = clearOrResize(collectedPowers,gateCount)
                    } else { won = true; finished = true; active = false }
                    return
                }
            }
        }
    }
    private fun damage() {
        if (invulnerable > 0f) return
        if (pilotGuardReady) {
            pilotGuardReady=false
            lastImpactLostLife=false
            invulnerable=1.25f
            impactPulse=.65f
            velocity=-90f
        } else if (shield > 0) {
            shield--
            lastImpactLostLife=false
            invulnerable = 1.25f; impactPulse = .65f; velocity = -90f
        } else {
            lives--
            lastImpactLostLife=true
            if(lives>0) {
                // A contact is one lost heart even during many physics slices.
                invulnerable=1.6f
                impactPulse=.65f
                velocity=-90f
            } else { finished = true; active = false; won = false }
        }
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
    /** Conservative continuous circle sweep against translating/animated rock.
     * Shape movement is linearly interpolated *within* the <=120Hz physics slice.
     * Endpoint-only collision misses shallow cap-corner chords. Interval bounds
     * avoid sampling-only tunneling without expanding the r18 body.
     */
    fun sweptBodyHitsPillar(birdX:Float,fromY:Float,toY:Float,radius:Float,
                            fromGateX:Float,toGateX:Float,gateWidth:Float,
                            fromOpening:LevelEngine.Opening,toOpening:LevelEngine.Opening):Boolean {
        if(!birdX.isFinite() || !fromY.isFinite() || !toY.isFinite() ||
            !radius.isFinite() || radius<=0f || !fromGateX.isFinite() ||
            !toGateX.isFinite() || !gateWidth.isFinite() || gateWidth<=0f ||
            !fromOpening.top.isFinite() || !fromOpening.bottom.isFinite() ||
            !toOpening.top.isFinite() || !toOpening.bottom.isFinite() ||
            fromOpening.top>=fromOpening.bottom || toOpening.top>=toOpening.bottom) return false
        // A stationary tangency must not recursively subdivide 2^20+ times.
        // With no relative movement, the single-frame predicate is exact.
        if (fromY == toY && fromGateX == toGateX &&
            fromOpening.top == toOpening.top &&
            fromOpening.bottom == toOpening.bottom) {
            return bodyHitsPillar(birdX, fromY, radius, fromGateX, gateWidth, fromOpening)
        }
        // All four actual shapes lie in this swept horizontal extent.
        if(birdX<min(fromGateX,toGateX)-7f-radius ||
           birdX>max(fromGateX,toGateX)+gateWidth+5f+radius)return false
        // Conservative swept vertical broad phase: interpolation of both
        // bird centre and animated mouth stays inside this intersection.
        // Neither rounded lip nor shaft can touch the radius-r body here.
        // Improves CPU/frame pacing without changing near-miss/hit semantics.
        val safeTop=max(fromOpening.top,toOpening.top)+radius
        val safeBottom=min(fromOpening.bottom,toOpening.bottom)-radius
        if(fromY>=safeTop && fromY<=safeBottom &&
           toY>=safeTop && toY<=safeBottom)return false
        val motionBound=abs(toGateX-fromGateX)+abs(toY-fromY)+
            max(abs(toOpening.top-fromOpening.top),abs(toOpening.bottom-fromOpening.bottom))+.0001f
        fun clear(t:Float):Float {
            val x=fromGateX+(toGateX-fromGateX)*t
            val y=fromY+(toY-fromY)*t
            val top=fromOpening.top+(toOpening.top-fromOpening.top)*t
            val bottom=fromOpening.bottom+(toOpening.bottom-fromOpening.bottom)*t
            val right=x+gateWidth
            return min(min(
                signedRect(y=y,cx=birdX,left=x,top=-25f,right=right,bottom=top),
                signedRound(birdX,y,x-7f,top-28f,right+5f,top)),
                min(signedRect(y=y,cx=birdX,left=x,top=bottom,right=right,bottom=825f),
                    signedRound(birdX,y,x-7f,bottom,right+5f,bottom+27f))) - radius
        }
        fun search(a:Float,da:Float,b:Float,db:Float,depth:Int):Boolean {
            if(da<0f || db<0f)return true
            // A signed distance to a moving convex shape is Lipschitz;
            // min across four shapes keeps the same conservative bound.
            if(da+db>motionBound*(b-a))return false
            if(depth>=20)return false // <0.00001 logical-unit unresolved chord
            val mid=(a+b)*.5f
            val dm=clear(mid)
            if(dm<0f)return true
            return search(a,da,mid,dm,depth+1) || search(mid,dm,b,db,depth+1)
        }
        return search(0f,clear(0f),1f,clear(1f),0)
    }
    private fun signedRect(y:Float,cx:Float,left:Float,top:Float,
                           right:Float,bottom:Float):Float {
        val dx=abs(cx-(left+right)*.5f)-(right-left)*.5f
        val dy=abs(y-(top+bottom)*.5f)-(bottom-top)*.5f
        return kotlin.math.hypot(max(0f,dx),max(0f,dy))+min(max(dx,dy),0f)
    }
    private fun signedRound(cx:Float,cy:Float,left:Float,top:Float,
                            right:Float,bottom:Float):Float {
        val qx=abs(cx-(left+right)*.5f)-((right-left)*.5f-7f)
        val qy=abs(cy-(top+bottom)*.5f)-((bottom-top)*.5f-7f)
        return kotlin.math.hypot(max(0f,qx),max(0f,qy))+min(max(qx,qy),0f)-7f
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
