package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

/** Deterministic gameplay contract, independent of Android framework or rendering. */
class LevelEngineTest {

    @Test fun premiumShopBoostsHaveRealThreeHitAndEighteenSecondCapacity() {
        val stage=LevelEngine.create(0,1)
        val premium=GameSimulation(stage,initialShield=3,initialMagnet=18f)
        assertEquals("Third shield charge must survive construction",3,premium.shield)
        assertEquals("The full purchased magnet duration must be available",18f,premium.magnetTime,0f)
        val capped=GameSimulation(stage,initialShield=999,initialMagnet=999f)
        assertEquals("Shields must remain bounded to the strongest purchased tier",3,capped.shield)
        assertEquals("Magnets must remain bounded to the strongest purchased tier",18f,capped.magnetTime,0f)
        premium.flap()
        premium.step(1f/60f)
        assertTrue("Magnet timer advances only during real flight",premium.magnetTime in 17f..18f)
        assertEquals(3,premium.shield)
        val pickup=stage.gates.first().copy(x=55f,center=325f,gap=240f,width=64f,
            movement=0f,phase=0f,coin=false,star=false,power=2)
        val pickupFlight=GameSimulation(stage.copy(gates=listOf(pickup),speed=140f,wind=0f),
            initialMagnet=18f)
        pickupFlight.flap()
        pickupFlight.step(1f/60f)
        assertFalse("A safe magnet pickup must not end the flight",pickupFlight.finished)
        assertTrue("A five-second pickup must not shorten an 18-second paid magnet",
            pickupFlight.magnetTime>17f)
    }


    @Test fun endlessLivesAreCappedAndSingleContactCannotDrainTwoLives() {
        val base=LevelEngine.create(0,2)
        val danger=base.gates.first().copy(x=110f,center=260f,gap=164f,movement=0f,coin=false,star=false,power=0)
        val run=GameSimulation(base.copy(gates=listOf(danger),speed=140f,wind=0f),endless=true)
        assertEquals(3,run.lives)
        run.flap()
        run.step(1f/60f)
        assertEquals(2,run.lives)
        assertFalse(run.finished)
        assertTrue(run.invulnerable>1f)
        run.step(.1f)
        assertEquals("Invulnerability prevents double damage within the same cliff",2,run.lives)
        val legacy=GameSimulation(base.copy(gates=listOf(danger),speed=140f,wind=0f),endless=false)
        legacy.flap();legacy.step(1f/60f)
        assertTrue("Non-endless legacy mode retains one-hit failure",legacy.finished)
    }

    @Test fun heartsAreRareAndHiddenWhenLivesFull() {
        val run=GameSimulation(LevelEngine.create(0,1),endless=true)
        assertEquals(3,run.maxLives)
        assertEquals(0,run.heartsCollected)
        assertFalse(run.heartVisible(16))
        val heartIndex=(0 until run.level.gates.size).firstOrNull { run.isHeartPosition(it) }
        if(heartIndex!=null) assertFalse(run.heartVisible(heartIndex))
        val upcoming=(0 until run.upcomingGates().size).filter { run.isHeartPosition(it,true) }
        assertTrue("Procedural heart rhythm must be sparse",upcoming.size<=2)
    }


    @Test fun streamingRegenerationIsMovedAwayFromExactBoundary() {
        val template=LevelEngine.create(0,3)
        val last=template.gates.first().copy(x=25f,center=366f,gap=290f,
            coin=false,star=false,power=0,movement=0f)
        // A single-gate deterministic test still promotes the cached
        // upcoming world without resetting distance, speed or active state.
        val flight=GameSimulation(template.copy(gates=listOf(last)),endless=true)
        val xBefore=flight.upcomingGateX(flight.upcomingGates().first())
        flight.flap()
        // A variable-width cap must actually clear birdX-radius before
        // promotion; one 60Hz frame is too short for this synthetic gate.
        flight.step(.1f)
        assertEquals(4L,flight.displayLevel)
        assertEquals(xBefore-flight.distance,flight.gateX(flight.level.gates.first()),.01f)
        assertEquals(0,flight.upcomingGates().size)
        // Regeneration is deliberately deferred until halfway through
        // the next segment, not the first frame after promotion.
        flight.step(1f/60f)
        assertTrue("Preloading the subsequent batch must not interrupt the flight",
            flight.active && !flight.finished)
        assertEquals(0,flight.upcomingGates().size)
    }

    @Test fun originalCroatianWorldNamesRemainAvailable() {
        assertEquals(8,LevelEngine.names.size)
        assertEquals("Zeleni otoci",LevelEngine.names.first())
        assertEquals("Zvjezdani bezdani",LevelEngine.names.last())
    }

    @Test fun everyWorldUsesTapImpulseWithoutDragSteering() {
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            val game=GameSimulation(LevelEngine.create(world,1))
            assertFalse(game.active)
            assertEquals(18f,game.radius,0f)
            game.flap()
            assertEquals(-255f,game.velocity,0f)
            assertEquals(1,game.flaps)
            val before=game.y
            game.step(1f/60f)
            assertTrue("Tap must produce upward travel in world $world",game.y < before)
            game.flap()
            assertEquals(2,game.flaps)
        }
    }

    @Test fun proceduralGatesKeepVariedWidthsAndTraversableHeights() {
        for (world in 0 until LevelEngine.WORLD_COUNT) for (number in listOf(1,30,61,360,1001)) {
            val level=LevelEngine.create(world,number)
            assertEquals(level.gates,LevelEngine.create(world,number).gates)
            assertEquals("First obstacle already visible at world=$world level=$number",420f,level.gates.first().x,0f)
            assertEquals(setOf(64f,73f,82f,91f),level.gates.map { it.width }.toSet())
            assertTrue(level.gates.all { it.width in 64f..91f })
            val gaps=level.gates.map { it.gap }
            assertTrue("Each level needs recognizable tall/short pillars",gaps.max()-gaps.min() >= 9f)
            assertTrue(gaps.all { it >= 164f })
            assertTrue(level.gates.all { LevelEngine.opening(it,0f).let { opening ->
                opening.bottom - opening.top >= 2*18f+100f
            } })
            assertTrue(level.gates.zipWithNext().all { (one,two) ->
                kotlin.math.abs(one.center - two.center) <= 208f
            })
        }
    }

    @Test fun firstTenGatesVaryHeightWithoutUnreachableJumps() {
        val expected=listOf(312f,267f,217f,250f,302f,352f,410f,457f,409f,351f)
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            val level=LevelEngine.create(world,1)
            val centers=level.gates.take(10).map { it.center }
            assertEquals("First level must not look like ten identical columns",expected,centers)
            assertTrue(centers.toSet().size>=9)
            assertTrue(centers.zipWithNext().all { (a,b) -> kotlin.math.abs(a-b) <= 115f })
            // A constant-altitude autopilot must hit a physical pillar on
            // ALL three difficulty settings, not merely a decorative cap.
            for(mode in 0..2) {
                val mouths=level.gates.take(10).map{LevelEngine.opening(it,0f,mode)}
                val requiredMin=mouths.maxOf{it.top+18f}
                val requiredMax=mouths.minOf{it.bottom-18f}
                assertTrue("World $world difficulty $mode has a straight safe lane",
                    requiredMin>requiredMax)
            }
            for (number in listOf(2,3,4,30,361,4096,524289,1048576)) {
                val other=LevelEngine.create(world,number)
                assertTrue("All generated gates must be reachable",other.gates.zipWithNext().all{
                    (a,b) -> kotlin.math.abs(a.center-b.center)<=208f
                })
            }
        }
    }

    @Test fun difficultyModesHavePronouncedOpeningDifferencesAndVisibleHeightChanges() {
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            val level = LevelEngine.create(world, 1)
            val centers = level.gates.take(10).map { it.center }
            assertTrue("No constant-altitude straight lane", centers.max() - centers.min() >= 230f)
            for (gate in level.gates) {
                val easy = LevelEngine.opening(gate, 0f, 0)
                val normal = LevelEngine.opening(gate, 0f, 1)
                val hard = LevelEngine.opening(gate, 0f, 2)
                val easyGap = easy.bottom - easy.top
                val normalGap = normal.bottom - normal.top
                val hardGap = hard.bottom - hard.top
                assertTrue("Easy must be visibly wider", easyGap - normalGap >= 53.8f)
                assertTrue("Hard must be meaningfully tighter", normalGap - hardGap >= 18.9f)
                assertTrue("Hard must retain a flyable corridor", hardGap >= 144.9f)
            }
        }
    }

    @Test fun threeDifficultiesChangeRealRenderableAndCollidableOpenings() {
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            for(number in listOf(1, 60, 121, 240, 360, 1048576)) {
                for(gate in LevelEngine.create(world,number).gates) {
                    for(t in listOf(0f,1.25f,7f,15f)) {
                        val easy=LevelEngine.opening(gate,t,0)
                        val normal=LevelEngine.opening(gate,t,1)
                        val hard=LevelEngine.opening(gate,t,2)
                        val easyGap=easy.bottom-easy.top
                        val normalGap=normal.bottom-normal.top
                        val hardGap=hard.bottom-hard.top
                        assertTrue("Easy < Normal world=$world level=$number",easyGap >= normalGap)
                        assertTrue("Normal < Hard world=$world level=$number",normalGap >= hardGap)
                        assertTrue("Hard opening must stay fair",hardGap >= 144.9f)
                        assertTrue(hard.top>0f && easy.bottom<755f)
                        assertEquals(normal.top, LevelEngine.opening(gate,t).top,0f)
                    }
                }
            }
        }
        // Existing 18px body radius must not be shrunk or enlarged for this change.
        val prototype=LevelEngine.create(0,1)
        val g=prototype.gates[0].copy(x=148f,center=290f,gap=164f,
            movement=0f,coin=false,star=false,power=0)
        val single=prototype.copy(gates=listOf(g),speed=140f,wind=0f)
        val easyFlight=GameSimulation(single,difficulty=0)
        val hardFlight=GameSimulation(single,difficulty=2)
        easyFlight.flap();hardFlight.flap()
        easyFlight.step(1f/60f);hardFlight.step(1f/60f)
        assertFalse("Easy flight fits the actual mouth",easyFlight.finished)
        assertTrue("Hard flight hits the visibly narrower bottom lip",hardFlight.finished)
        assertEquals(18f, easyFlight.radius,0f)
        assertEquals(18f, hardFlight.radius,0f)
    }

    @Test fun seamlessLevelJunctionNeverCreatesAnEmptyVisibleCourse() {
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            for (number in listOf(1,2,62,122,182,242,302,360,1048576)) {
                val level=LevelEngine.create(world,number)
                val game=GameSimulation(level,endless=true)
                val last=level.gates.last()
                val interval=last.x-level.gates[level.gates.lastIndex-1].x
                val seam=interval
                val firstNext=game.upcomingGateX(game.upcomingGates().first())
                assertEquals(seam,firstNext-game.gateX(last),.01f)
                assertTrue(seam in 218f..228f) // Identical in-level / cross-level rhythm.
                assertEquals(0f,game.levelProgress,0f)
                game.flap()
                game.step(.05f)
                assertTrue("world $world level $number",game.levelProgress in 0.0001f..0.9999f)
            }
        }
    }

    @Test fun eachTapRetriggersCosmeticWingbeatWithoutChangingPhysics() {
        val flight=GameSimulation(LevelEngine.create(0,2))
        assertEquals(0f,flight.flapPulse,0f)
        flight.flap()
        assertEquals(.24f,flight.flapPulse,0f)
        assertEquals(-255f,flight.velocity,0f)
        flight.step(.08f)
        assertEquals(.16f,flight.flapPulse,.0001f)
        val velocity=flight.velocity
        val distance=flight.distance
        flight.step(Float.NaN)
        flight.step(-1f)
        assertEquals(.16f,flight.flapPulse,.0001f)
        assertEquals(velocity,flight.velocity,0f)
        assertEquals(distance,flight.distance,0f)
        flight.flap()
        assertEquals(.24f,flight.flapPulse,0f)
        assertEquals(-255f,flight.velocity,0f)
        repeat(3){flight.step(.10f)}
        assertEquals(0f,flight.flapPulse,.00001f)
        assertFalse(flight.finished)
        assertEquals(2,flight.flaps)
    }

    @Test fun levelTypeGenerationIsStableWithoutRemovedMenuHelpers() {
        val cases=listOf(1 to 0,15 to 1,30 to 2,60 to 3,61 to 0)
        for(world in 0 until LevelEngine.WORLD_COUNT)
            for((number,type) in cases)
                assertEquals(type,LevelEngine.create(world,number).type)
    }

    @Test fun initialFlightRemainsInactiveUntilFirstTap() {
        // UI must not show the pause affordance while the flight is still idle.
        val flight=GameSimulation(LevelEngine.create(0,1))
        assertFalse(flight.active)
        assertEquals(0,flight.flaps)
        assertEquals(0f,flight.distance,0f)
        flight.step(.1f)
        assertFalse(flight.active)
        assertEquals(0f,flight.time,0f)
        assertEquals(0f,flight.distance,0f)
        flight.flap()
        assertTrue(flight.active)
        assertEquals(1,flight.flaps)
        flight.flap()
        assertEquals(2,flight.flaps)
        assertTrue(flight.active)
    }

    @Test fun visiblePillarCapCollisionKillsImmediatelyWithoutShield() {
        // The front edge of the wide cap overlaps Bopi, though the thin shaft does not.
        val level=LevelEngine.create(0,2)
        val near=level.gates[0].copy(x=148f,center=456f,gap=160f,movement=0f,coin=false,star=false,power=0)
        val synthetic=level.copy(gates=listOf(near),speed=140f,wind=0f)
        val flight=GameSimulation(synthetic)
        flight.flap()
        flight.step(1f/60f)
        assertTrue("Visible cap overlap must end the run in the same simulation step",flight.finished)
        assertFalse(flight.won)
        assertEquals(0,flight.passed)
    }

    @Test fun shieldAbsorbsExactlyOneCapImpact() {
        val level=LevelEngine.create(0,2)
        val near=level.gates[0].copy(x=148f,center=456f,gap=160f,movement=0f,coin=false,star=false,power=0)
        val flight=GameSimulation(level.copy(gates=listOf(near),speed=140f,wind=0f),initialShield=1)
        flight.flap()
        flight.step(1f/60f)
        assertFalse(flight.finished)
        assertEquals(0,flight.shield)
        assertTrue(flight.invulnerable>0f)
    }
    @Test fun circularBodyIgnoresEmptyLipCornerButTouchesVisibleGeometry() {
        val safeCorner=LevelEngine.Opening(top=410f,bottom=570f)
        assertFalse("Corner AABB must not count as real body contact",
            PillarCollision.bodyHitsPillar(126f,362f,18f,148f,73f,safeCorner))
        assertTrue("Bird touching the painted upper lip must be hit",
            PillarCollision.bodyHitsPillar(126f,362f,18f,148f,73f,LevelEngine.Opening(376f,536f)))
        assertTrue("Bird touching a solid shaft must be hit",
            PillarCollision.bodyHitsPillar(126f,362f,18f,135f,73f,LevelEngine.Opening(410f,570f)))
        assertTrue("Bird touching the painted lower lip must be hit",
            PillarCollision.bodyHitsPillar(126f,362f,18f,148f,73f,LevelEngine.Opening(190f,355f)))
        assertFalse("Exact edge tangent is not a body overlap",
            PillarCollision.bodyHitsPillar(126f,300f,18f,144f,73f,LevelEngine.Opening(376f,536f)))
    }

    @Test fun roundedRockLipRejectsTransparentCornerButAcceptsOnePixelPenetration() {
        // Drawn upper lip: roundRect(left=100, top=200, right=188, bottom=228, corner=7).
        // Previous cap AABB considers (88,188) a hit (16.97px from rectangle),
        // whereas the actual rounded corner is 19.87px away (> Bopi radius 18).
        val opening=LevelEngine.Opening(228f,450f)
        assertFalse("Transparent upper roundRect corner must not kill Bopi",
            PillarCollision.bodyHitsPillar(88f,188f,18f,107f,76f,opening))
        assertFalse("1-2px outside actual upper rounded corner is safe",
            PillarCollision.bodyHitsPillar(88f,190f,18f,107f,76f,opening))
        assertTrue("Real penetration into rounded upper lip must register",
            PillarCollision.bodyHitsPillar(88f,192f,18f,107f,76f,opening))
        assertFalse("Transparent lower rounded corner must be safe",
            PillarCollision.bodyHitsPillar(88f,489f,18f,107f,76f,opening))
        assertTrue("Real lower rounded lip must register",
            PillarCollision.bodyHitsPillar(88f,484f,18f,107f,76f,opening))
        // Upper/lower straight outer lip edge tangency, 1px inside, 2px outside.
        assertFalse("Exact visible lip tangency is not penetration",
            PillarCollision.bodyHitsPillar(82f,214f,18f,107f,76f,opening))
        assertTrue("1px physical overlap is a real hit",
            PillarCollision.bodyHitsPillar(83f,214f,18f,107f,76f,opening))
        assertFalse("2px margin from cap must remain safe",
            PillarCollision.bodyHitsPillar(80f,214f,18f,107f,76f,opening))
        assertFalse("Lower cap tangency is also safe",
            PillarCollision.bodyHitsPillar(82f,463.5f,18f,107f,76f,opening))
        assertTrue("Lower cap 1px penetration is a hit",
            PillarCollision.bodyHitsPillar(83f,463.5f,18f,107f,76f,opening))
        assertFalse("Transparent wing/aura outside body circle cannot hit",
            PillarCollision.bodyHitsPillar(70f,338f,18f,107f,76f,opening))
        assertFalse("Invalid body geometry cannot be a hit",
            PillarCollision.bodyHitsPillar(Float.NaN,188f,18f,107f,76f,opening))
        assertFalse(PillarCollision.bodyHitsPillar(88f,188f,0f,107f,76f,opening))
    }

    @Test fun movingRockOpeningIsFiniteAndBoundsAgreeAcrossFrameRates() {
        for(world in 0 until LevelEngine.WORLD_COUNT)
            for(difficulty in 0..2) {
                val gate=LevelEngine.create(world,120).gates[5]
                for(hz in listOf(30,60,90,120)) {
                    var previous=LevelEngine.opening(gate,0f,difficulty)
                    for(frame in 1..hz*2) {
                        val next=LevelEngine.opening(gate,frame.toFloat()/hz,difficulty)
                        assertTrue(next.top.isFinite() && next.bottom.isFinite())
                        assertTrue(next.bottom-next.top >= 144.9f)
                        assertTrue("Moving mouth may not teleport",
                            kotlin.math.abs(next.top-previous.top) < 2f &&
                            kotlin.math.abs(next.bottom-previous.bottom) < 2f)
                        previous=next
                    }
                }
            }
    }
    @Test fun interiorSweptFlightCorridorStaysFreeWithoutExpensiveCapSearch() {
        val from=LevelEngine.Opening(245f,453f)
        val to=LevelEngine.Opening(253f,445f)
        for(x in listOf(100f,125f,149f)) {
            assertFalse("A swept body wholly in the open mouth never touches rock",
                PillarCollision.sweptBodyHitsPillar(126f,336f,345f,18f,
                    x+2f,x,76f,from,to))
        }
        // A moving body entering the upper lip is still a true collision.
        val fixed=LevelEngine.Opening(245f,453f)
        assertTrue(PillarCollision.sweptBodyHitsPillar(126f,265f,244f,18f,
            110f,108f,76f,fixed,fixed))
    }

    @Test fun conservativeRockSweepCatchesSingleSliceRoundedCornerTunneling() {
        val o=LevelEngine.Opening(228f,450f)
        // At 120Hz the real-moving rock can shift 1.5px horizontally while a
        // flap shifts Bopi 1.87px vertically: both endpoints are safe, middle
        // of the relative segment intersects the 7px rounded lip by 0.01px.
        val startX=146.2422f;val endX=144.7422f
        val startY=192.2966f;val endY=190.42694f
        assertFalse(PillarCollision.bodyHitsPillar(126f,startY,18f,startX,76f,o))
        assertFalse(PillarCollision.bodyHitsPillar(126f,endY,18f,endX,76f,o))
        assertTrue("Missing the moving rock inside the physics slice is tunneling",
            PillarCollision.sweptBodyHitsPillar(126f,startY,endY,18f,
                startX,endX,76f,o,o))
        assertFalse("A parallel genuinely clear near-miss must stay safe",
            PillarCollision.sweptBodyHitsPillar(126f,startY-.12f,endY-.12f,18f,
                startX,endX,76f,o,o))
        // Mouth motion is accounted for in the *same relative coordinates*.
        val moved=LevelEngine.Opening(228.2f,450.2f)
        assertTrue(PillarCollision.sweptBodyHitsPillar(126f,startY,endY+.2f,18f,
            startX,endX,76f,o,moved))
        assertFalse(PillarCollision.sweptBodyHitsPillar(Float.NaN,startY,endY,18f,
            startX,endX,76f,o,moved))
    }

    @Test fun stationaryLipTangencyIsNotAnUnboundedCcdSearch() {
        val mouth=LevelEngine.Opening(228f,450f)
        // Bird x=82 is exactly r18 away from the leftmost cap edge x=100.
        // An unchanged 120Hz sweep must use the exact static predicate rather
        // than exploring ~2^20 zero-clearance intervals.
        assertFalse(PillarCollision.bodyHitsPillar(82f,214f,18f,107f,76f,mouth))
        assertFalse(PillarCollision.sweptBodyHitsPillar(82f,214f,214f,18f,
            107f,107f,76f,mouth,mouth))
        // One real pixel of inward rock motion still becomes a true contact.
        assertTrue(PillarCollision.sweptBodyHitsPillar(82f,214f,214f,18f,
            107f,106f,76f,mouth,mouth))
    }

    @Test fun flightDoesNotDieBesideCapOutsideItsPaintedHeight() {
        val level=LevelEngine.create(0,2)
        val lipTooHigh=level.gates[0].copy(x=148f,center=490f,gap=160f,
            movement=0f,coin=false,star=false,power=0)
        val flight=GameSimulation(level.copy(gates=listOf(lipTooHigh),speed=140f,wind=0f))
        flight.flap()
        flight.step(1f/60f)
        assertFalse("No collision where only the old infinite cap AABB existed",flight.finished)
    }

    @Test fun illustratedBirdSurvivesSafeCapOverlap() {
        val level=LevelEngine.create(0,2)
        val safe=level.gates[0].copy(x=148f,center=366f,gap=220f,movement=0f,coin=false,star=false,power=0)
        val run=GameSimulation(level.copy(gates=listOf(safe),speed=140f,wind=0f))
        run.flap();run.step(1f/60f)
        assertFalse("Do not kill bird when its body remains inside visible opening",run.finished)
    }
    @Test fun physicsReplayAgreesAt30_60_90_120Hz() {
        // First 600ms are free of gates/bounds; test the integrator, not rendering.
        val runs = listOf(30,60,90,120).map { hz ->
            GameSimulation(LevelEngine.create(0,2)).apply {
                flap()
                repeat(hz*3/5) { step(1f/hz) }
            }
        }
        val reference=runs.last()
        assertEquals("Shared 120Hz replay y after 600ms",338f,reference.y,0.5f)
        assertEquals("Shared body hitbox",18f,reference.radius,0f)
        for ((i,sim) in runs.withIndex()) {
            assertFalse("Premature death at ${listOf(30,60,90,120)[i]}Hz",sim.finished)
            assertEquals("Time must be 600ms",0.6f,sim.time,0.0005f)
            assertEquals("Impulse/gravity unchanged",156f,sim.velocity,0.05f)
            assertEquals("Frame-rate flight drift",reference.y,sim.y,0.75f)
            assertEquals("Frame-rate scroll drift",reference.distance,sim.distance,0.03f)
            assertEquals(0,sim.score())
        }
        val rapid=GameSimulation(LevelEngine.create(0,2))
        rapid.flap(); rapid.step(1f/120f); rapid.flap()
        assertEquals(2,rapid.flaps)
        assertEquals(-255f,rapid.velocity,0f)
        val dead=GameSimulation(LevelEngine.create(0,2))
        dead.flap()
        repeat(720) {
            val scoreBefore=dead.score()
            dead.step(1f/120f)
            assertTrue(dead.score()>=scoreBefore)
            assertTrue(dead.y.isFinite() && dead.velocity.isFinite())
        }
        val last=dead.time
        if (dead.finished) {
            dead.step(.1f)
            assertEquals("Dead flights do not advance",last,dead.time,0f)
        }
        val invalid=GameSimulation(LevelEngine.create(0,2))
        invalid.flap(); invalid.step(Float.NaN); invalid.step(Float.POSITIVE_INFINITY)
        assertEquals(0f,invalid.time,0f)
    }

    @Test fun sameMotionAcross60_90_120HzFrames() {
        val level=LevelEngine.create(0,2)
        val at60=GameSimulation(level)
        val at90=GameSimulation(level)
        val at120=GameSimulation(level)
        at60.flap();at90.flap();at120.flap()
        repeat(36){at60.step(1f/60f)}
        repeat(54){at90.step(1f/90f)}
        repeat(72){at120.step(1f/120f)}
        assertFalse(at60.finished)
        assertFalse(at90.finished)
        assertFalse(at120.finished)
        assertEquals(at60.distance,at90.distance,0.2f)
        assertEquals(at60.distance,at120.distance,0.2f)
        // Semi-implicit integration permits small bounded displacement differences.
        assertEquals(at60.y,at90.y,4f)
        assertEquals(at60.y,at120.y,4f)
    }

    @Test fun shortDisplayStallsKeepFlightTimeAndRejectInvalidDeltas() {
        val level = LevelEngine.create(0, 2)
        val smooth = GameSimulation(level)
        val stalled = GameSimulation(level)
        smooth.flap(); stalled.flap()
        repeat(6) { smooth.step(1f / 60f) }
        stalled.step(0.10f)
        assertFalse(smooth.finished)
        assertFalse(stalled.finished)
        assertEquals(smooth.distance, stalled.distance, 0.02f)
        assertEquals(smooth.velocity, stalled.velocity, 0.1f)
        assertEquals(smooth.y, stalled.y, 3f)
        val interrupted = GameSimulation(level)
        interrupted.flap(); interrupted.step(10f)
        assertEquals(0.10f, interrupted.time, 0.0001f)
        val invalid = GameSimulation(level)
        invalid.flap(); invalid.step(Float.NaN); invalid.step(-1f)
        assertEquals(0f, invalid.time, 0f)
        assertEquals(0f, invalid.distance, 0f)
    }

    @Test fun streamingKeepsRealGateRhythmAcrossEveryChallengeZone() {
        // The next level begins at exactly the normal inter-gate cadence.
        for(world in 0 until LevelEngine.WORLD_COUNT) {
            for(number in listOf(2,62,122,182,242,302,360)) {
                val level=LevelEngine.create(world,number)
                val stream=GameSimulation(level,endless=true)
                val oldLast=stream.gateX(level.gates.last())
                val lastSpacing=level.gates.last().x-level.gates[level.gates.lastIndex-1].x
                val firstNext=stream.upcomingGateX(stream.upcomingGates().first())
                assertEquals("world=$world level=$number",lastSpacing,firstNext-oldLast,0.001f)
                assertEquals(0f,stream.distance,0f)
                assertFalse(stream.active)
            }
        }
        val normal=GameSimulation(LevelEngine.create(0,122))
        assertTrue("Single-level mode must not precompute unnecessary gates",normal.upcomingGates().isEmpty())
    }


    @Test fun threeDepthSceneryIsDeterministicAndSafeForReducedMotion() {
        val expected=floatArrayOf(70f,150f,240f)
        for (layer in 0..2) {
            assertEquals(expected[layer],ParallaxScenery.offset(1000f,layer,false),0.001f)
            assertEquals(0f,ParallaxScenery.offset(1000f,layer,true),0f)
            assertEquals(0f,ParallaxScenery.offset(Float.NaN,layer,false),0f)
            assertEquals(0f,ParallaxScenery.offset(Float.POSITIVE_INFINITY,layer,false),0f)
            for (distance in listOf(0f,10f,12345f,999999f)) {
                val d=ParallaxScenery.offset(distance,layer,false)
                assertTrue("Parallax wrap must remain bounded",d>=0f && d<696f)
                assertEquals(d,ParallaxScenery.offset(distance,layer,false),0f)
            }
        }
    }

    @Test fun marathonGatesKeepSubpixelCollisionPrecisionBeyondFloatRange() {
        val origin=33_554_432.0 // 2^25: Float cannot represent a 0.25px change.
        assertEquals(559.75f,WorldCoordinates.screenX(560f,origin,origin+.25),.0001f)
        assertEquals(809.75f,WorldCoordinates.screenX(560f,origin+250,origin+.25),.0001f)
        val sim=GameSimulation(LevelEngine.create(0,62),endless=true)
        val current=sim.level.gates.first()
        val upcoming=sim.upcomingGates().first()
        val spacing=sim.level.gates.last().x-sim.level.gates[sim.level.gates.lastIndex-1].x
        assertEquals(spacing,sim.upcomingGateX(upcoming)-sim.gateX(sim.level.gates.last()),.001f)
        assertEquals(current.x,sim.gateX(current),.0001f)
    }

    @Test fun nextLevelArrivesWithoutDistanceResetOrBlankTransition() {
        val level=LevelEngine.create(0,2)
        val safe=level.gates.first().copy(x=20f,center=366f,gap=220f,movement=0f,coin=false,star=false,power=0)
        val game=GameSimulation(level.copy(gates=listOf(safe),speed=140f,wind=0f),endless=true)
        val previewX=game.upcomingGateX(game.upcomingGates().first())
        game.flap(); game.step(1f/60f)
        assertEquals(1,game.completionCount)
        assertEquals(3L,game.displayLevel)
        assertEquals("New endless level must begin at zero completed gates",0,game.passed)
        assertEquals("Global completed gates must never reset between levels",1,game.totalPassed)
        assertTrue("Global distance must remain monotonic",game.distance>0f)
        assertEquals("Upcoming obstacle must never jump at the boundary",previewX-game.distance,game.gateX(game.level.gates[0]),0.01f)
        assertTrue("The completion pulse can decay within the remaining 120Hz substep",
            game.levelTransition in .83f.. .85f)
        game.step(.05f)
        assertTrue("The brief level badge fades while flight remains live",game.levelTransition>0f && game.levelTransition<.85f)
        assertTrue(game.active)
        assertFalse(game.finished)
    }


    @Test fun proceduralSpeedChangesNeverSnapAcrossInvisibleBatchBoundaries() {
        // A single safe gate lets us cross an internal boundary in one real step.
        // Incoming obstacle positions remain precomputed in continuous world space.
        val template=LevelEngine.create(0,2)
        val safe=template.gates.first().copy(
            x=20f,center=366f,gap=300f,movement=0f,coin=false,star=false,power=0)
        val slow=template.copy(gates=listOf(safe),speed=70f,wind=0f)
        val run=GameSimulation(slow,endless=true)
        val nextBefore=run.upcomingGateX(run.upcomingGates().first())
        assertEquals(70f,run.currentScrollSpeed,0.001f)
        run.flap()
        run.step(1f/60f)
        assertEquals(3L,run.displayLevel)
        // Promotion is completed during the first of two 120Hz slices.
        // The next slice already begins a bounded ~0.71px/s acceleration.
        val speedAfterBoundary=run.currentScrollSpeed
        assertTrue(speedAfterBoundary>=70f && speedAfterBoundary<71f)
        assertEquals(nextBefore-run.distance,run.gateX(run.level.gates.first()),0.01f)
        // The following slice must advance by at most 85px/s², not snap
        // immediately from 70px/s to the next 140px/s batch target.
        val travelledBefore=run.distance
        run.step(1f/120f)
        val nextSpeed=run.currentScrollSpeed
        assertTrue(nextSpeed>speedAfterBoundary)
        assertTrue(nextSpeed-speedAfterBoundary<0.72f)
        assertTrue("Moving obstacles must follow the exact smoothed displacement",
            run.distance-travelledBefore in 0.55f..0.61f)
        run.step(1f/120f)
        assertTrue(run.currentScrollSpeed>nextSpeed)
        assertTrue(run.active && !run.finished)
    }

    @Test fun worldsStayIndependentAndHaveSafeGaps() {
        assertEquals(8, LevelEngine.WORLD_COUNT)
        assertEquals(8, LevelEngine.names.toSet().size)
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            for (n in listOf(1, 5, 15, 30, 60, 360, 1024, LevelEngine.LEVELS_PER_WORLD)) {
                val level = LevelEngine.create(world,n)
                assertEquals(world,level.world)
                assertTrue(level.gates.size >= 11)
                assertTrue(level.gates.all { it.kind == world })
                for(gate in level.gates) {
                    for(t in listOf(0f, 1.25f, 2.5f, 8f)) {
                        val opening = LevelEngine.opening(gate,t)
                        assertTrue("Gap too tight: w=$world level=$n",opening.bottom-opening.top >= 144.9f)
                        assertTrue(opening.top > 0f && opening.bottom < 755f)
                    }
                }
            }
        }
    }

    @Test fun levelIdentityRemainsStableAndUnique() {
        val seen = HashSet<String>()
        for (n in 1..256) {
            val first=LevelEngine.create(0,n)
            val repeat=LevelEngine.create(0,n)
            val sig=LevelEngine.signature(first)
            assertEquals(sig,LevelEngine.signature(repeat))
            assertTrue("Collision at level $n",seen.add(sig))
        }
        val nextCycle=LevelEngine.createStream(0,LevelEngine.LEVELS_PER_WORLD.toLong()+1)
        assertEquals(0,nextCycle.world)
        assertNotEquals(LevelEngine.signature(LevelEngine.create(0,1)),LevelEngine.signature(nextCycle))
    }

    @Test fun coinsAreEarnedOnlyOnMilestones() {
        assertEquals(0,LevelEngine.milestoneReward(1))
        assertEquals(20,LevelEngine.milestoneReward(5))
        assertEquals(50,LevelEngine.milestoneReward(15))
        assertEquals(100,LevelEngine.milestoneReward(30))
        assertEquals(200,LevelEngine.milestoneReward(60))
        assertEquals(0,LevelEngine.milestoneReward(61))
    }


    @Test fun movingHazardsKeepSafeGeometryWithoutPerFrameAllocation() {
        for (world in listOf(3, 6, 7)) {
            val gate = LevelEngine.create(world, 120).gates[0].copy(movement=16f)
            for (frame in 0..720) {
                val bounds = LevelEngine.opening(gate, frame / 120f)
                assertTrue(bounds.top.isFinite() && bounds.bottom.isFinite())
                assertTrue("Animated opening must remain traversable", bounds.bottom - bounds.top >= 144.9f)
            }
        }
    }

    @Test fun shieldDoesNotConsumeMultipleChargesFromOneContinuousImpact() {
        val level = LevelEngine.create(0, 2)
        val obstacle = level.gates[0].copy(x=148f, center=510f, gap=160f, movement=0f, coin=false, star=false, power=0)
        val game = GameSimulation(level.copy(gates=listOf(obstacle), speed=140f, wind=0f), initialShield=2)
        game.flap()
        repeat(8) { game.step(1f / 120f) }
        assertEquals("One ongoing collision may only consume one shield charge", 1, game.shield)
        assertFalse(game.finished)
    }

    @Test fun difficultyModesAdjustPhysicsButRetainSafeOpening() {
        val level=LevelEngine.create(0,2)
        val easy=GameSimulation(level,difficulty=0)
        val normal=GameSimulation(level,difficulty=1)
        val hard=GameSimulation(level,difficulty=2)
        for(game in listOf(easy,normal,hard)){game.flap();game.step(1f/60f)}
        assertTrue(easy.distance < normal.distance && normal.distance < hard.distance)
        assertTrue(easy.y < normal.y && normal.y < hard.y)
        assertEquals(level.gates.size,easy.level.gates.size)
        assertEquals(level.gates.size,hard.level.gates.size)
        assertEquals(2,GameSimulation(level,difficulty=42).difficulty)
    }

    @Test fun marathonScoreIsCappedWithoutIntegerOverflow() {
        val game=GameSimulation(LevelEngine.create(0,1))
        val field=GameSimulation::class.java.getDeclaredField("totalPassed")
        field.isAccessible=true
        field.setInt(game,Int.MAX_VALUE)
        assertEquals("Marathon score must remain valid for local leaderboard",100_000_000,game.score())
    }

    @Test fun virtualCoinsFromRecordPointsArePaidOnceAndNeverOverflow() {
        assertEquals(5,ProgressStore.claimableRecordCoins(5000,0,0))
        assertEquals(0,ProgressStore.claimableRecordCoins(5000,5,0))
        assertEquals(4,ProgressStore.claimableRecordCoins(9000,5,0))
        assertEquals(0,ProgressStore.claimableRecordCoins(-5000,0,0))
        assertEquals(0,ProgressStore.claimableRecordCoins(5000,0,100000000))
        assertEquals(1,ProgressStore.claimableRecordCoins(5000,4,99999999))
        assertEquals(100000,ProgressStore.claimableRecordCoins(100000000,0,0))
    }

    @Test fun boundaryValuesCannotGenerateInvalidLevels() {
        for(world in 0..7) {
            assertEquals(1,LevelEngine.create(world,1).number)
            assertEquals(LevelEngine.LEVELS_PER_WORLD,LevelEngine.create(world,LevelEngine.LEVELS_PER_WORLD).number)
        }
    }
    @Test fun endlessFoodRequiresRealTouchAndNeverBecomesCurrency() {
        val prototype=LevelEngine.create(0,1)
        val near=prototype.gates.first().copy(x=94f,center=366f,gap=240f,width=64f,
            movement=0f,coin=true,star=false,power=0)
        val safe=prototype.copy(gates=listOf(near),speed=140f,wind=0f)
        val game=GameSimulation(safe,endless=true)
        assertTrue(game.foodVisible(0))
        game.flap()
        game.step(1f/60f)
        assertEquals(1,game.foodCollected)
        assertFalse(game.foodVisible(0))
        assertEquals("Fruit must never add legacy coins",0,game.coins)
        assertEquals("Passing gates alone earns score; food is counted separately",0,game.score())
        game.step(1f/60f)
        assertEquals("One berry must never be counted twice",1,game.foodCollected)

        val missed=GameSimulation(safe.copy(gates=listOf(near.copy(center=260f,gap=340f))),endless=true)
        missed.flap()
        missed.step(1f/60f)
        assertFalse("Non-contact must not collect fruit",missed.finished)
        assertEquals(0,missed.foodCollected)
        assertTrue(missed.foodVisible(0))
    }


    @Test fun sweptBerryContactCatchesFrameBoundaryGrazesWithoutMagnet() {
        assertTrue("The berry crosses Bopi between physics slices",
            BerryCollision.touched(40f,0f,-40f,0f,31f))
        assertFalse("Close pass outside Bopi's actual body remains missed",
            BerryCollision.touched(40f,32f,-40f,32f,31f))
        assertTrue("Exact tangent contact counts",
            BerryCollision.touched(40f,31f,-40f,31f,31f))
        assertTrue("Already overlapping fruit counts without movement",
            BerryCollision.touched(0f,0f,0f,0f,31f))
        assertFalse("Invalid motion cannot grant any food",
            BerryCollision.touched(Float.NaN,0f,0f,0f,31f))
    }

    @Test fun foodIsPhysicalOneShotAndSafeUnderInvalidFrameTimes() {
        val cases=listOf(floatArrayOf(40f,0f,-40f,0f,31f),floatArrayOf(40f,31f,-40f,31f,31f),floatArrayOf(40f,31.02f,-40f,31.02f,31f),floatArrayOf(0f,0f,0f,0f,31f),floatArrayOf(32f,0f,32f,0f,31f),floatArrayOf(400f,0f,-400f,0f,31f),floatArrayOf(400f,32f,-400f,32f,31f))
        val expected=listOf(true,true,false,true,false,true,false)
        for((i,values) in cases.withIndex()) assertEquals(expected[i],
            BerryCollision.touched(values[0],values[1],values[2],values[3],values[4]))
        for(value in listOf(Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY)) {
            assertFalse(BerryCollision.touched(value,0f,0f,0f,31f))
            assertFalse(BerryCollision.touched(0f,0f,0f,0f,value))
        }
        val level=LevelEngine.create(0,1)
        val berry=level.gates.first().copy(x=94f,center=366f,gap=240f,
            width=64f,movement=0f,coin=true,star=false,power=0)
        repeat(2) {
            val game=GameSimulation(level.copy(gates=listOf(berry),speed=140f,wind=0f),endless=true)
            game.flap();game.step(0f);game.step(Float.NaN)
            assertEquals(0,game.foodCollected)
            game.step(1f/120f)
            assertEquals(1,game.foodCollected)
            assertEquals(0,game.coins);assertEquals(0,game.score())
            repeat(40){game.step(1f/120f)}
            assertEquals(1,game.foodCollected)
        }
    }

    @Test fun beginnerObstaclesFormGentleRhythmIncludingNextStream() {
        for(world in 0 until LevelEngine.WORLD_COUNT)for(n in listOf(1,2,35,61,360)) {
            val l=LevelEngine.create(world,n)
            assertTrue(l.gates.zipWithNext().all { (a,b) ->
                kotlin.math.abs(a.center-b.center)<=115f
            })
            val game=GameSimulation(l,endless=true)
            val all=listOf(l.gates.last().center)+game.upcomingGates().map { it.center }
            assertTrue("Next stream must avoid abrupt up/down reversal", all.zipWithNext().all{(a,b)->
                kotlin.math.abs(a-b)<=82f+0.001f
            })
        }
    }

}
