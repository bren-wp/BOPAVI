package com.brendigo.bopavi

import org.junit.Assert.*
import org.junit.Test

/** Deterministic gameplay contract, independent of Android framework or rendering. */
class LevelEngineTest {

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

    @Test fun resultHeadlinesUseOnlyRealEarlierLocalRecords() {
        assertEquals("LET ZAVRŠEN!",ResultHeadline.label(0,0,false))
        assertEquals("LET ZAVRŠEN!",ResultHeadline.label(120,120,false))
        assertEquals("LET ZAVRŠEN!",ResultHeadline.label(80,120,false))
        assertEquals("NOVI REKORD!",ResultHeadline.label(121,120,false))
        assertEquals("LEVEL DOVRŠEN!",ResultHeadline.label(121,120,true))
        assertEquals("LEVEL DOVRŠEN!",ResultHeadline.label(0,0,true))
    }

    @Test fun namedLevelTypesMatchTheActualProceduralGenerator() {
        val cases=listOf(1 to 0, 14 to 0, 15 to 1, 29 to 0,
            30 to 2, 45 to 1, 59 to 0, 60 to 3,
            61 to 0, 75 to 1, 90 to 2, 120 to 3, 360 to 3)
        val titles=listOf("Normalni","Izazovni","Bonus","Elitni")
        val icons=listOf("●","⚡","✦","♛")
        for (world in 0 until LevelEngine.WORLD_COUNT) {
            for ((number,type) in cases) {
                assertEquals("world=$world level=$number",type,LevelEngine.create(world,number).type)
                assertEquals(titles[type],LevelKind.name(type))
                assertEquals(icons[type],LevelKind.icon(type))
            }
        }
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
        val near=level.gates[0].copy(x=154f,center=510f,gap=160f,movement=0f,coin=false,star=false,power=0)
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
        val near=level.gates[0].copy(x=154f,center=510f,gap=160f,movement=0f,coin=false,star=false,power=0)
        val flight=GameSimulation(level.copy(gates=listOf(near),speed=140f,wind=0f),initialShield=1)
        flight.flap()
        flight.step(1f/60f)
        assertFalse(flight.finished)
        assertEquals(0,flight.shield)
        assertTrue(flight.invulnerable>0f)
    }
    @Test fun illustratedBirdSurvivesSafeCapOverlap() {
        val level=LevelEngine.create(0,2)
        val safe=level.gates[0].copy(x=154f,center=366f,gap=220f,movement=0f,coin=false,star=false,power=0)
        val run=GameSimulation(level.copy(gates=listOf(safe),speed=140f,wind=0f))
        run.flap();run.step(1f/60f)
        assertFalse("Do not kill bird when its body remains inside visible opening",run.finished)
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
        // The per-level gate interval is smaller in tougher zones. Incoming
        // obstacles must inherit that interval instead of a fixed 242px gap.
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
        assertEquals(0f,game.levelTransition,0f)
        assertTrue(game.active)
        assertFalse(game.finished)
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
        val obstacle = level.gates[0].copy(x=154f, center=510f, gap=160f, movement=0f, coin=false, star=false, power=0)
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
}
