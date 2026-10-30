package com.brendigo.bopavi

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.graphics.Canvas
import android.view.WindowInsets
import android.os.Build
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.RadialGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sin

/** Native GPU-backed Android Canvas. No HTML, Chromium, WebView or network activity. */
class GameView(context: Context, val game: GameSimulation, private val reducedMotion: Boolean, private val skinIndex: Int, private val hapticEnabled: Boolean, private val onFinished: (GameSimulation) -> Unit, private val onLevelCompleted:(Long)->Unit = {}, private val onFlap:()->Unit = {}, private val onCollect:()->Unit = {}, private val onShieldImpact:()->Unit = {}, qaCaptureFirstFlap:Boolean = false, qaCollisionOverlay:Boolean = false) : View(context) {
    // QA-only freeze after a REAL first flap. The drawn view remains the actual
    // GameSimulation with truthful HUD, while slow emulator screenshot IPC runs.
    // Android release builds cannot enable this even via an injected Intent.
    private val qaCaptureEnabled = qaCaptureFirstFlap &&
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0)
    private var qaWaitingForCapture = false
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    // Keep only a cached lives accessibility label: no score text or
    // expensive formatting on the 60/90/120 Hz render path.
    private var lastHudAccessibility = ""
    // Reused, immutable sprite destination: no RectF per rendered frame.
    private val hudPortraitRect=RectF(22f,17f,66f,61f)
    // Updated only when Android dispatches insets or the viewport changes.
    // A center-camera cutout does not overlap the left HUD on normal phones.
    private var hudTopSafeInsetPx=0
    private val headerTypeface=Typeface.create("sans-serif-black",Typeface.BOLD)
    // One validated skin index for the body and both detached wing sprites.
    private val selectedSkin=skinIndex.coerceIn(0,10) // All pilots have the same verified 18px collider.
    private val pilotName=listOf("Bopi","Sunny","Berry","Luna","Mint","Shadow","Vihor","Iskra","Orion","Sova","Lisac")[selectedSkin]
    private val birdSprites = intArrayOf(R.drawable.bopi0,R.drawable.bopi1,R.drawable.bopi2,R.drawable.bopi3,R.drawable.bopi4,R.drawable.bopi5,R.drawable.bopi6,R.drawable.bopi7,R.drawable.bopi8,R.drawable.bopi9,R.drawable.bopi10)
    private var birdBitmap = BitmapFactory.decodeResource(resources,birdSprites[selectedSkin],
        BitmapFactory.Options().apply { inScaled=false })
    // Reference pilot is only enabled after build-time manifest and checksum
    // validation. This optional art never alters the same 18px body collider.
    private val referenceBopi0=selectedSkin==0 &&
        resources.getIdentifier("bopireference0","drawable",context.packageName)!=0
    // At 142 logical px (vs 124), Bopi more closely matches the provided
    // screenshot's 29%-wide bird without returning to oversized 176px art.
    private val birdSize=142f
    private val birdRect=RectF(-birdSize*.5f,-birdSize*.5f,birdSize*.5f,birdSize*.5f)
    // Shoulder pivots are image-specific. Imported 224x192 screenshot layers
    // have actual joints at (106,118) and (157,129), measured from (112,96).
    private val wingLeftPivotX=when{
        referenceBopi0 -> -6f*birdSize/224f
        selectedSkin>=6 -> -12f*birdSize/160f
        else -> -16f*birdSize/100f
    }
    private val wingLeftPivotY=when{
        referenceBopi0 -> 22f*birdSize/224f
        selectedSkin>=6 -> 4f*birdSize/160f
        else -> 5f*birdSize/100f
    }
    private val wingRightPivotX=when{
        referenceBopi0 -> 45f*birdSize/224f
        selectedSkin>=6 -> 12f*birdSize/160f
        else -> 15f*birdSize/100f
    }
    private val wingRightPivotY=when{
        referenceBopi0 -> 33f*birdSize/224f
        selectedSkin>=6 -> 4f*birdSize/160f
        else -> -4f*birdSize/100f
    }
    private val leftWings=intArrayOf(R.drawable.bopileft0,R.drawable.bopileft1,
        R.drawable.bopileft2,R.drawable.bopileft3,R.drawable.bopileft4,R.drawable.bopileft5,R.drawable.bopileft6,R.drawable.bopileft7,R.drawable.bopileft8,R.drawable.bopileft9,R.drawable.bopileft10)
    private val rightWings=intArrayOf(R.drawable.bopiright0,R.drawable.bopiright1,
        R.drawable.bopiright2,R.drawable.bopiright3,R.drawable.bopiright4,R.drawable.bopiright5,R.drawable.bopiright6,R.drawable.bopiright7,R.drawable.bopiright8,R.drawable.bopiright9,R.drawable.bopiright10)
    private var wingLeftBitmap=BitmapFactory.decodeResource(resources,leftWings[selectedSkin],
        BitmapFactory.Options().apply{inScaled=false})
    private var wingRightBitmap=BitmapFactory.decodeResource(resources,rightWings[selectedSkin],
        BitmapFactory.Options().apply{inScaled=false})
    private val worldBitmaps=intArrayOf(R.drawable.world0,R.drawable.world1,
        R.drawable.world2,R.drawable.world3,R.drawable.world4,R.drawable.world5,
        R.drawable.world6,R.drawable.world7)
    private var worldBitmap=BitmapFactory.decodeResource(resources,worldBitmaps[game.level.world],
        BitmapFactory.Options().apply{inScaled=false})
    // Cover the ENTIRE screen with the actual biome illustration on tall phones.
    // Unlike stretching the gameplay simulation, aspect-fill only crops distant
    // background artwork; bird/gates/HUD remain in the stable 480x800 world.
    private val worldCoverRect=RectF()
    // Reused mutable destination rect for art-only camera drift. These never
    // affect the stable 480×800 flight simulation, hitboxes or HUD placement.
    private val worldDriftRect=RectF()
    // A six-percent cover overscan guarantees opaque pixels even at maximum
    // camera offset on a tall Pixel or wide tablet (no transparent sky strips).
    private val worldOverscan=1.06f
    private val worldHorizontalDrift=.018f
    private val worldVerticalDrift=.008f
    // Prebuilt world-specific sunlight, moonshine and crystal glow.
    // Cached shaders are reused across frames; no bitmap or shader creation while drawing.
    private val glowHues=intArrayOf(0xdaffaf,0xffe6a6,0xc8f6ff,0xffb178,0xfff1cc,0xb7a0ff,0xa4fff4,0xbcb3ff)
    private val glowX=floatArrayOf(350f,390f,395f,360f,380f,396f,370f,365f)
    private val glowY=floatArrayOf(180f,132f,154f,180f,146f,169f,181f,170f)
    private val glowShaders=Array(8) { world ->
        RadialGradient(glowX[world],glowY[world],185f,
            // Match the translucent-center-to-transparent-edge iOS glow.
            intArrayOf(0x47000000 or (glowHues[world] and 0x00ffffff),
                glowHues[world] and 0x00ffffff),
            floatArrayOf(0f,1f),Shader.TileMode.CLAMP)
    }

    // One cached cyan/gold radial halo gives the animated pilot more depth.
    // The shader stays in logical bird coordinates and never changes hitboxes.
    private val birdAuraShader=RadialGradient(0f,0f,53f,
        intArrayOf(0x62fff3bb,0x3a2da9ff,0x002da9ff),
        floatArrayOf(0f,.55f,1f),Shader.TileMode.CLAMP)

    private val pickupHues=intArrayOf(0xffffc83b.toInt(),0xffffba83.toInt(),0xffa5efff.toInt(),0xffff9836.toInt(),0xfffff1ad.toInt(),0xffc5adff.toInt(),0xff89f7ef.toInt(),0xffc3a6ff.toInt())
    private val feather = intArrayOf(0xff39b5fc.toInt(),0xffffc73e.toInt(),0xffff6883.toInt(),0xff9e86f6.toInt(),0xff45daad.toInt(),0xff6676a8.toInt())
    private val skyA = intArrayOf(0xff159df7.toInt(),0xff18b5e7.toInt(),0xff418ddc.toInt(),0xff6e287e.toInt(),0xff45aaf6.toInt(),0xff131a4b.toInt(),0xff123969.toInt(),0xff0b123f.toInt())
    private val skyB = intArrayOf(0xffd0f8ff.toInt(),0xffffe3b2.toInt(),0xffedfbff.toInt(),0xffffa36d.toInt(),0xffffe9b6.toInt(),0xff7461bc.toInt(),0xff60f6d5.toInt(),0xff5955a9.toInt())
    private val pillars = intArrayOf(0xff4c9165.toInt(),0xfff5a65b.toInt(),0xff8ad8f5.toInt(),0xffe65b35.toInt(),0xffe9d9b5.toInt(),0xff57459a.toInt(),0xff5fdddc.toInt(),0xff7973f3.toInt())
    private val pillarDark = intArrayOf(0xff1f533c.toInt(),0xffbd7153.toInt(),0xff4282ad.toInt(),0xff912f35.toInt(),0xff9d8d80.toInt(),0xff241b60.toInt(),0xff247b9b.toInt(),0xff373192.toInt())

    // Material palettes are immutable flight-scoped data, not two new arrays
    // allocated for every obstacle on every animation frame.
    private val biomeReliefAccents=intArrayOf(0xff87ef82.toInt(),0xffbd74ff.toInt(),
        0xffff8b39.toInt(),0xffffd782.toInt(),0xff6ff4ec.toInt(),
        0xffffe7a0.toInt(),0xff6ceab4.toInt(),0xffbb92ff.toInt())
    private val biomeReliefGlints=intArrayOf(0xffd4ffb0.toInt(),0xff7ef4ff.toInt(),
        0xffffcf70.toInt(),0xffffefa8.toInt(),0xffffb6e7.toInt(),
        0xffffffff.toInt(),0xffffa7e7.toInt(),0xff85dfff.toInt())

    // Per-world pollen, embers, snow, stardust and sea sparkle. Drawn rather than loaded.
    private val atmosphereHues=intArrayOf(0xffffedab.toInt(),0xffffdea0.toInt(),0xffeaffff.toInt(),
        0xffffbd67.toInt(),0xffd3fff1.toInt(),0xffcab7ff.toInt(),0xff8dfff1.toInt(),0xffc5d1ff.toInt())
    private val foregroundHues=intArrayOf(0xff63e1a1.toInt(),0xffffce81.toInt(),0xffa6e8ff.toInt(),
        0xffff834f.toInt(),0xffe0fff0.toInt(),0xffa395ec.toInt(),0xff6ef0da.toInt(),0xff8fa5f1.toInt())
    // Extend visual rock shafts through the camera letterbox on tall displays.
    // The physical 480x800 simulation/collision bounds never change.
    private var pillarRenderTop=-25f
    private var pillarRenderBottom=825f
    private var viewportSky: LinearGradient? = null
    override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int){
        super.onSizeChanged(w,h,oldw,oldh)
        // Drop elapsed vsync time across a rotation, without resetting scores.
        frameClock.reset()
        viewportSky=LinearGradient(0f,0f,0f,h.toFloat().coerceAtLeast(1f),
            skyA[game.level.world],skyB[game.level.world],Shader.TileMode.CLAMP)
        // The camera is uniformly fitted, but tall displays show additional
        // scenery above/below that 800-unit canvas. Rock shafts must continue
        // through those regions rather than visibly float in open sky.
        val cameraScale=min(w/480f,h/800f)
        val overflow=if(cameraScale>0f)
            ((h/cameraScale-800f)*.5f).coerceAtLeast(0f) else 0f
        pillarRenderTop=-25f-overflow
        pillarRenderBottom=825f+overflow
        updateWorldCover(w,h)
        if(Build.VERSION.SDK_INT>=30)requestApplyInsets()
    }
    override fun onApplyWindowInsets(insets:WindowInsets):WindowInsets {
        if(Build.VERSION.SDK_INT>=30){
            val scale=(width/480f).coerceAtMost(height/640f)
            val originX=(width-480f*scale)*.5f
            val hudLeft=originX+(FlightHudLayout.avatarX-25f)*scale
            val hudRight=originX+FlightHudLayout.lives.right*scale
            val cutout=insets.displayCutout
            val cutoutOverlaps=cutout?.boundingRects?.any { rect ->
                rect.bottom>0 &&
                    FlightHudLayout.intersectsHudX(rect.left,rect.right,hudLeft,hudRight)
            }==true
            // statusBars() returns zero once the immersive bar is hidden.
            hudTopSafeInsetPx=maxOf(insets.getInsets(WindowInsets.Type.statusBars()).top,
                if(cutoutOverlaps)cutout?.safeInsetTop ?: 0 else 0)
        } else hudTopSafeInsetPx=0
        return super.onApplyWindowInsets(insets)
    }
    private fun updateWorldCover(w:Int,h:Int){
        worldBitmap?.let { bmp ->
            if(w>0 && h>0 && bmp.width>0 && bmp.height>0){
                val cover=max(w.toFloat()/bmp.width,h.toFloat()/bmp.height)*
                    worldOverscan
                val fullWidth=bmp.width*cover
                val fullHeight=bmp.height*cover
                worldCoverRect.set((w-fullWidth)*.5f,(h-fullHeight)*.5f,
                    (w+fullWidth)*.5f,(h+fullHeight)*.5f)
            }
        }
    }
    private val collisionOverlayEnabled=qaCollisionOverlay &&
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0
    // First frame of a flight is a baseline; idle waiting is never physics time.
    private val frameClock=FlightFrameClock()
    // Refresh only on window/lifecycle transitions, never through a binder call
    // inside the 60/90/120 Hz rendering hot path.
    private val powerManager=context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var powerSaveMode=false
    private var powerSaveReceiverRegistered=false
    private val powerSaveReceiver=object:BroadcastReceiver(){
        override fun onReceive(context:Context?,intent:Intent?) {
            if(intent?.action==PowerManager.ACTION_POWER_SAVE_MODE_CHANGED){
                // The system may toggle saver without hiding the Activity.
                // Never poll the PowerManager from inside onDraw().
                refreshPowerSaveMode()
                if(isAttachedToWindow && game.active && !paused)postInvalidateOnAnimation()
            }
        }
    }
    private fun refreshPowerSaveMode(){ powerSaveMode=powerManager?.isPowerSaveMode==true }
    private var sent = false
    private var completedSeen=0
    private var pickupSeen=0
    private var shieldImpactNotified=false
    private var lastAccessibilityLives=-1
    var paused = false
        set(v) {
            if(field==v)return
            field = v
            frameClock.reset()
            postInvalidateOnAnimation()
        }
    private fun flightHint():String = if(selectedSkin==0)
        "Bopi leti. Dodirni za zamah krilima."
        else "$pilotName leti. Dodirni za zamah krilima."
    init {
        isClickable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = "Dodirni za let: "+pilotName
        p.isFilterBitmap = true
    }
    // Paint.color contains its OWN ARGB alpha. Resetting p.alpha to 255 here
    // accidentally made glass shadows, pillar bevels, fading effects and
    // missing-life hearts opaque, flattening the artwork across all biomes.
    // setColor restores the intended alpha after a temporary stroke override.
    private fun fill(color: Int) {
        p.shader=null
        p.color=color
        p.style=Paint.Style.FILL
        p.strokeWidth=1f
    }
    private fun rect(c: Canvas,l:Float,t:Float,r:Float,b:Float,color:Int,round:Float=0f){fill(color);c.drawRoundRect(l,t,r,b,round,round,p)}
    private fun oval(c:Canvas,l:Float,t:Float,r:Float,b:Float,color:Int){fill(color);c.drawOval(l,t,r,b,p)}
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int,center:Boolean=false){fill(color);p.textSize=size;p.typeface=headerTypeface;p.textAlign=if(center) Paint.Align.CENTER else Paint.Align.LEFT;c.drawText(s,x,y,p)}
    override fun onDraw(canvas:Canvas) {
        super.onDraw(canvas)
        // Never return before drawing a complete frame. A draw request may arrive
        // sooner than the target interval (gesture, layout, OS redraw); skipping it
        // caused intermittent blank/stale frames on real 60/90/120 Hz devices.
        val now=System.nanoTime()
        // Use actual vsync-to-vsync elapsed time at 60/90/120 Hz. The previous
        // 3/4-of-60Hz threshold updated physics at only ~45 Hz on 90 Hz phones.
        if(game.active && !paused && !qaWaitingForCapture && !game.finished) {
            // System.nanoTime() elapsed while the preview was idle is NOT
            // a physics delta. Likewise the first frame after a pause or
            // QA still capture just establishes a fresh clock baseline.
            frameClock.tick(now,running=true)?.let { game.step(it) }
            if(game.completionCount>completedSeen) {
                completedSeen=game.completionCount
                onLevelCompleted(game.completedOrdinal)
            }
            // Restored hearts are full collectibles, with the same sound and
            // haptic event as berries; no event when merely drawing an item.
            val pickups=game.foodCollected+game.coins+game.stars+game.heartsCollected
            if(pickups>pickupSeen) {
                pickupSeen=pickups
                if(hapticEnabled)performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onCollect()
            }
            // Rising-edge notification: one impact effect per consumed shield,
            // never one per frame while the invulnerability pulse remains active.
            if(game.impactPulse>0f && !shieldImpactNotified) {
                shieldImpactNotified=true
                if(hapticEnabled)performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onShieldImpact()
            } else if(game.impactPulse<=0f) {
                shieldImpactNotified=false
            }
        } else frameClock.reset()
        // Background extends through the display's letterbox regions. World physics
        // remains in the unchanged 480x800 coordinate system without stretching.
        canvas.drawColor(skyA[game.level.world])
        viewportSky?.let { background ->
            fill(Color.WHITE)
            p.shader=background
            canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),p)
            p.shader=null
        }
        // The painted environment has a slow independent camera layer.
        // All dynamic birds, cliffs, pickups and tappable controls stay in
        // their original collision coordinate system. The precomputed 6%
        // overscan is larger than both motion amplitudes on every aspect
        // ratio; reduced motion freezes the camera perfectly centered.
        if(worldBitmap!=null && !worldCoverRect.isEmpty()) {
            worldDriftRect.set(worldCoverRect)
            if(!reducedMotion && game.active && !game.finished) {
                val horizontal=ParallaxScenery.wave(game.preciseTravel,.0011)*
                    width*worldHorizontalDrift
                val vertical=ParallaxScenery.wave(game.preciseTravel,.00045)*
                    height*worldVerticalDrift
                worldDriftRect.offset(horizontal,vertical)
            }
            fill(Color.WHITE)
            canvas.drawBitmap(worldBitmap,null,worldDriftRect,p)
        }
        val scale=min(width/480f,height/800f)
        canvas.save()
        canvas.translate((width-480f*scale)/2,(height-800f*scale)/2)
        canvas.scale(scale,scale)
        if(worldBitmap==null) drawBackground(canvas)
        drawWorldLighting(canvas)
        drawParallaxIslands(canvas)
        // Light atmospheric layer moves independently of the static painted backdrop.
        // Reuses one Paint and generates no bitmaps or sprite allocations per frame.
        val drift=if(reducedMotion)0f else ParallaxScenery.wrapped(game.preciseTravel,.075,580f)
        val mist=if(game.level.world==5 || game.level.world==7)0x228cbbff else 0x38ffffff
        for(i in 0..3) {
            val x=((i*174f+75f-drift+580f)%580f)-95f
            val y=146f+(i%3)*148f
            oval(canvas,x,y,x+106f,y+24f,mist)
            oval(canvas,x+24f,y-12f,x+74f,y+24f,mist)
        }
        drawWorldAtmosphere(canvas)
        // Upcoming gates enter from the right BEFORE the previous level ends.
        // Cache is produced by the simulation; no per-frame level generation.
        // The hot drawing path needs no iterator/IndexedValue wrappers.
        val upcoming=game.upcomingGates()
        for(i in upcoming.indices) {
            val g=upcoming[i]
            val x=game.upcomingGateX(g)
            if(x >= -100f && x<=550f)drawGate(canvas,g,x,i,true)
        }
        for(i in game.level.gates.indices) {
            val g=game.level.gates[i];val x=game.gateX(g)
            if(x < -100f || x>550f)continue
            drawGate(canvas,g,x,i)
        }
        drawBird(canvas)
        if(collisionOverlayEnabled) drawCollisionDebugOverlay(canvas)
        // The gameplay camera and HUD are different coordinate spaces. On tall
        // phones the 480x800 flight camera is letterboxed vertically; keep the
        // actual wallet, level progress and powers pinned to the physical top.
        if(!game.active && !game.finished) {
            drawFlightReadyTutorial(canvas)
        }
        // Native modal owns pause controls; no second painted faux pause panel.
        canvas.restore()
        if(!game.finished) {
            // Reference ready-state already includes the real life HUD. Render
            // it both before the first flap and during active flight.
            val hudScale=(width/480f).coerceAtMost(height/640f)
            canvas.save()
            // Inset is cached at lifecycle boundaries; no binder/cutout work on
            // the 60/90/120 Hz render path and no unnecessary center-notch shift.
            canvas.translate((width-480f*hudScale)*.5f,hudTopSafeInsetPx+4f*hudScale)
            canvas.scale(hudScale,hudScale)
            drawHud(canvas)
            canvas.restore()
        }
        // The illustrated world already owns its entire 480x800 canvas.
        // A former opaque ground stripe hid the lower floating-island scenery,
        // including on narrow and notched phones. Letterboxing is filled above.
        if(game.finished && !sent){sent=true;post{if(isAttachedToWindow)onFinished(game)}}
        // The OS vsync callback supports 60/90/120Hz without an arbitrary
        // 14ms throttle. Reduced motion stays at roughly 30fps to save work.
        // Pause, exit, background and QA still capture schedule no new frames.
        if(isAttachedToWindow && game.active && !paused && !game.finished && !qaWaitingForCapture) {
            val delay=GameFramePacing.redrawDelayMillis(reducedMotion,powerSaveMode)
            if(delay!=null) postInvalidateDelayed(delay) else postInvalidateOnAnimation()
        }
    }

    /** Tiny animated details above the painted landscape, behind collidable obstacles.
     * Pure Canvas primitives: no per-frame bitmap decoding, allocation or physics changes.
     * Motion freezes entirely with the reduced-motion accessibility preference.
     */

    /** Blend painted backdrops into real-time primitives with a soft biome glow.
     * Stable under reduced-motion and cached once per GameView instance.
     */
    private fun drawWorldLighting(c:Canvas) {
        val world=game.level.world
        p.shader=glowShaders[world]
        p.color=Color.WHITE
        p.alpha=255
        c.drawCircle(glowX[world],glowY[world],185f,p)
        p.shader=null
    }
    // Three animated depths sit over the painted environment, behind every gate.
    // Geometry is illustrative only: it cannot change physics or hitboxes.
    // All values are computed from distance; no sprite, Path or shader allocations.
    private val islandTopHues = intArrayOf(0xff83e575.toInt(),0xffffd98a.toInt(),
        0xffc3f3ff.toInt(),0xffffac58.toInt(),0xffffe7ae.toInt(),
        0xffaa95e6.toInt(),0xff71edda.toInt(),0xffbcb4ff.toInt())
    private val islandRockHues = intArrayOf(0xff9e7e70.toInt(),0xffb88a69.toInt(),
        0xff83b9d4.toInt(),0xff914455.toInt(),0xffb39b88.toInt(),
        0xff51417f.toInt(),0xff4578a9.toInt(),0xff55538f.toInt())
    private fun drawParallaxIslands(c:Canvas) {
        val world = game.level.world
        for(layer in 0..2) {
            val drift=ParallaxScenery.offset(game.preciseTravel,layer,reducedMotion)
            val width=83f-layer*9f
            val height=38f+layer*13f
            val alpha=if(layer==0)78 else if(layer==1)105 else 132
            val rock=(alpha shl 24) or (islandRockHues[world] and 0x00ffffff)
            val top=(alpha shl 24) or (islandTopHues[world] and 0x00ffffff)
            val gleam=(min(145,alpha+24) shl 24) or 0x00ffffff
            for(i in 0..8) {
                val x=i*174f+layer*53f-drift-118f
                if(x < -110f || x > 530f) continue
                val y=416f+layer*92f+(i%2)*26f
                path.reset()
                path.moveTo(x+6f,y+5f)
                path.lineTo(x+width-6f,y+5f)
                path.lineTo(x+width*.70f,y+height)
                path.lineTo(x+width*.42f,y+height+11f)
                path.lineTo(x+width*.18f,y+height*.75f)
                path.close()
                fill(rock);c.drawPath(path,p)
                oval(c,x-5f,y-11f,x+width+5f,y+11f,top)
                rect(c,x+13f,y-9f,x+width-13f,y-5f,gleam,2f)
                if(world==0 || world==4) {
                    rect(c,x+width*.52f,y+15f,x+width*.55f,y+height+8f,
                        (min(120,alpha) shl 24) or 0x00b9f4ff,2f)
                }
            }
        }
    }
    private fun drawWorldAtmosphere(c:Canvas) {
        val world=game.level.world
        val dustDrift=if(reducedMotion)0f else
            ParallaxScenery.wrapped(game.preciseTravel,.11,560f)
        val nearDrift=if(reducedMotion)0f else
            ParallaxScenery.wrapped(game.preciseTravel,.31,680f)
        val t=if(reducedMotion)0f else game.time
        val dust=atmosphereHues[world]
        for(i in 0 until 12) {
            val x=((i*113f+29f-dustDrift)%560f+560f)%560f-45f
            val shimmer=if(reducedMotion)0f else sin(t*(1.45f+(i%3)*.19f)+i*.87f)
            val y=175f+((i*83)%430).toFloat()+shimmer*4f
            fill(dust)
            p.alpha=if(reducedMotion)108 else (106f+42f*shimmer).toInt().coerceIn(45,150)
            c.drawCircle(x,y,2.3f+(i%3)*.65f,p)
            if(i%4==0) {
                p.alpha=27
                c.drawCircle(x,y,8f,p)
            }
        }
        val near=foregroundHues[world]
        for(i in 0 until 9) {
            val x=((i*85f+32f-nearDrift)%680f+680f)%680f-60f
            val y=700f+(i%3)*12f
            val sway=if(reducedMotion)0f else sin(t*1.75f+i.toFloat())*3f
            fill(near)
            p.alpha=83
            p.style=Paint.Style.STROKE
            p.strokeWidth=2.5f
            c.drawLine(x,y+21f,x+sway+6f,y-5f,p)
            p.style=Paint.Style.FILL
            p.alpha=110
            c.drawOval(x+sway-1f,y-9f,x+sway+15f,y+1f,p)
        }
        p.alpha=255
        p.style=Paint.Style.FILL
    }
    private fun drawBackground(c:Canvas){
        val w=game.level.world;val t=if(reducedMotion)0f else game.time
        val night=w==5||w==7
        // Parallax: three very cheap shape layers; no bitmaps allocated while drawing.
        for(layer in 0..2){
            val par=if(reducedMotion)0f else
                ParallaxScenery.wrapped(game.preciseTravel,(.08+layer*.12),154f)
            for(i in -1..(if(reducedMotion)2 else 4)){
                val x=(i*154f+layer*67f-par)
                val y=70f+layer*108f+(i and 1)*27f
                val a=if(night)0x667d9fcf else 0xccebfaff.toInt()
                oval(c,x,y,x+91f,y+28f,a);oval(c,x+19f,y-17f,x+67f,y+29f,a)
            }
        }
        val ridge=if(night)0xff22265e.toInt() else if(w==3)0xffa35455.toInt() else 0xff83bfbb.toInt()
        val ridgeDrift=if(reducedMotion)0f else
            ParallaxScenery.wrapped(game.preciseTravel,.055,127f)
        for(i in -1..5){
            val x=i*127f-ridgeDrift
            path.reset();path.moveTo(x,753f);path.lineTo(x+55f,619f);path.lineTo(x+108f,753f);path.close();fill(ridge);c.drawPath(path,p)
            if(w==2) { path.reset();path.moveTo(x+39f,650f);path.lineTo(x+55f,619f);path.lineTo(x+73f,650f);path.close();fill(Color.WHITE);c.drawPath(path,p) }
        }
        drawWorldDetails(c,w)
        rect(c,0f,751f,480f,800f,if(night)0xff171f53.toInt() else 0xff64c881.toInt())
        rect(c,0f,752f,480f,760f,if(night)0xff5d70ca.toInt() else 0xffa8e889.toInt())
        if(w==7)for(i in 0..19){val x=((i*83+17)%470).toFloat();val y=((i*113+31)%510).toFloat();oval(c,x,y,x+2f,y+2f,Color.WHITE)}
    }
    /** Separate low-cost silhouettes for all eight worlds; no bitmap allocations per frame. */
    private fun drawWorldDetails(c:Canvas,w:Int){
        val shift=if(reducedMotion)0f else
            ParallaxScenery.wrapped(game.preciseTravel,.035,180f)
        when(w){
            0 -> for(i in 0..3){
                val x=i*166f-shift
                val y=505f+(i%2)*31f
                // Floating grass islands instead of unrelated flat green ellipses.
                path.reset();path.moveTo(x,y+12f);path.lineTo(x+104f,y+12f)
                path.lineTo(x+70f,y+78f);path.lineTo(x+44f,y+98f);path.lineTo(x+20f,y+58f);path.close()
                fill(0xffa78970.toInt());c.drawPath(path,p)
                oval(c,x-5f,y-10f,x+109f,y+24f,0xff279f65.toInt())
                oval(c,x+3f,y-15f,x+99f,y+11f,0xff8be66f.toInt())
                rect(c,x+28f,y+24f,x+35f,y+65f,0x998feaff.toInt(),3f)
                for(j in 0..2)oval(c,x+14f+j*29f,y-16f,x+32f+j*29f,y-6f,0xffbafb87.toInt())
            }
            1 -> {
                oval(c,365f,100f,432f,167f,0xffffec98.toInt())
                for(i in 0..4){val x=i*138f-shift;oval(c,x,675f,x+108f,689f,0x885ae7fa.toInt());oval(c,x+28f,697f,x+115f,708f,0x66ffffff)}
                rect(c,370f,550f,382f,721f,0xffac8555.toInt(),5f)
                for(j in 0..4){val yy=568f+j*3f;oval(c,331f+j*8f,yy-58f,397f+j*7f,yy+17f,0xff56be81.toInt())}
            }
            2 -> for(i in 0..8){val x=(i*79f+33f-shift*.6f+480f)%480f;val y=135f+i%4*111f;path.reset();path.moveTo(x-10f,y);path.lineTo(x+10f,y);path.moveTo(x,y-10f);path.lineTo(x,y+10f);fill(0xb5ffffff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=3f;c.drawPath(path,p);p.style=Paint.Style.FILL}
            3 -> for(i in 0..4){val x=i*137f-shift;path.reset();path.moveTo(x,690f);path.lineTo(x+51f,575f);path.lineTo(x+90f,690f);path.close();fill(0xff633047.toInt());c.drawPath(path,p);oval(c,x+38f,593f,x+63f,609f,0xffffb24b.toInt());rect(c,x+43f,620f,x+49f,699f,0xfff58135.toInt(),4f)}
            4 -> for(i in 0..3){val x=i*165f-shift;rect(c,x+38f,532f,x+62f,734f,0xffe2d5a3.toInt(),6f);rect(c,x+23f,522f,x+77f,542f,0xffffe3a7.toInt(),3f);rect(c,x+23f,713f,x+77f,733f,0xffffe3a7.toInt(),3f)}
            5 -> {oval(c,358f,103f,425f,170f,0xffffe1b2.toInt());oval(c,374f,90f,442f,159f,0xff131a4b.toInt());for(i in 0..12){val x=(i*119+37)%470f;val y=(i*77+128)%590f;oval(c,x,y,x+4f,y+4f,0xffffd89a.toInt())}}
            6 -> for(i in 0..5){val x=i*104f-shift*.8f;path.reset();path.moveTo(x+15f,733f);path.lineTo(x+40f,594f-i%2*25f);path.lineTo(x+69f,733f);path.close();fill(if(i%2==0)0xff5be4d7.toInt() else 0xffb68bee.toInt());c.drawPath(path,p);path.reset();path.moveTo(x+40f,594f-i%2*25f);path.lineTo(x+40f,720f);fill(0xaaffffff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=2f;c.drawPath(path,p);p.style=Paint.Style.FILL}
            7 -> {oval(c,324f,110f,453f,239f,0xffba79d9.toInt());oval(c,347f,122f,422f,168f,0x886cdfeb.toInt());fill(0x99f8e3ff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=7f;c.drawOval(303f,142f,473f,206f,p);p.style=Paint.Style.FILL;for(i in 0..11){val x=(i*103+31)%460f;val y=(i*173+61)%500f;oval(c,x,y,x+3f,y+3f,0xffffffff.toInt())}}
        }
    }
    /** Moving obstacle warning glints stay strictly inside existing cap geometry.
     * These are cosmetic: actual opening/collision values are never modified.
     * Reduced motion preserves stationary cues instead of animated flashing.
     */
    private fun drawMovingGateRimCues(c:Canvas,g:LevelEngine.Gate,x:Float,top:Float,bottom:Float) {
        if(g.movement<=0f) return
        val pulse=if(reducedMotion)0.5f else (sin(game.time*2.3f+g.phase)+1f)*0.5f
        val offset=if(reducedMotion)4.5f else pulse*9f
        val alpha=(70f+125f*pulse).toInt().coerceIn(70,195)
        val color=(alpha shl 24) or (pickupHues[g.kind] and 0x00ffffff)
        val right=x+g.width-23f-offset
        // Top lip occupies [top-28,top], bottom lip occupies [bottom,bottom+27].
        rect(c,x+8f+offset,top-22f,x+21f+offset,top-17f,color,2f)
        rect(c,right,top-22f,right+13f,top-17f,color,2f)
        rect(c,x+8f+offset,bottom+15f,x+21f+offset,bottom+20f,color,2f)
        rect(c,right,bottom+15f,right+13f,bottom+20f,color,2f)
    }
    /** Bevelled gold crown coins use real collectible visibility and a single
     * reusable Path/Paint. No animation-loop asset or shader allocations.
     * The actual pickup radius stays defined exclusively by GameSimulation.
     */
    private fun drawRockFacets(c:Canvas,x:Float,w:Float,start:Float,end:Float){
        if(end-start<34f)return
        val span=end-start
        path.reset()
        path.moveTo(x+4f,start+6f)
        path.lineTo(x+w*.39f,start+11f)
        path.lineTo(x+w*.61f,start+span*.56f)
        path.lineTo(x+w*.24f,end-9f)
        path.lineTo(x+4f,end-16f)
        path.close()
        fill(0x3cffffff);c.drawPath(path,p)
        path.reset()
        path.moveTo(x+w*.61f,start+6f)
        path.lineTo(x+w-16f,start+4f)
        path.lineTo(x+w-16f,end-13f)
        path.lineTo(x+w*.40f,end-7f)
        path.lineTo(x+w*.61f,start+span*.56f)
        path.close()
        fill(0x59081836);c.drawPath(path,p)
        // Short nonuniform stratum lines and highlights: no fake hitboxes.
        path.reset()
        path.moveTo(x+10f,start+span*.32f)
        path.lineTo(x+w*.33f,start+span*.37f)
        path.lineTo(x+w*.49f,start+span*.35f)
        fill(0x64ffffff);p.style=Paint.Style.STROKE;p.strokeWidth=2f
        c.drawPath(path,p);p.style=Paint.Style.FILL
    }
    /** Layered theme relief confined to the same solid pillar collision regions.
     * No art intrudes into the playable gap and no per-frame bitmap allocations.
     * Distinct materials: moss, crystal, lava, moonlit stone, coral, marble,
     * enchanted roots and stardust. Both caps are anchored to the live opening.
     */
    private fun drawBiomeGateRelief(c:Canvas,x:Float,w:Float,top:Float,bottom:Float,world:Int,seed:Int) {
        val accent=biomeReliefAccents[world]
        val glint=biomeReliefGlints[world]
        // Stable gate-specific rock mineral patterns; no clock-based noise or hitbox changes.
        val motif=((seed%3)+3)%3
        for(side in 0..1) {
            val edge=if(side==0)top else bottom
            val sign=if(side==0)-1f else 1f
            // Three offset crystalline/stone faces read as real volume rather
            // than straight, flat, repeated green/white rectangles.
            for(i in 0..2) {
                val left=x+8f+i*(w-27f)/3f
                val right=(left+(w-20f)/3f+6f).coerceAtMost(x+w-7f)
                val depth=9f+((i+motif)%3)*4f
                path.reset()
                path.moveTo(left,edge+sign*5f)
                path.lineTo((left+right)*.5f,edge+sign*(depth+6f))
                path.lineTo(right,edge+sign*5f)
                path.lineTo((left+right)*.5f,edge+sign*2f)
                path.close()
                val isGlint=(i+motif)%3==1
                fill(if(isGlint)glint else accent)
                p.alpha=if(isGlint)210 else 145
                c.drawPath(path,p);p.alpha=255
            }
            // Biome identity inside the obstacle, not in the unobstructed gap.
            val inner=edge+sign*(32f+motif*4f)
            if(inner in 20f..730f){
                if(world==2 || world==1 || world==7) {
                    for(i in 0..1){
                        path.reset()
                        val cx=x+17f+i*(w-32f)
                        path.moveTo(cx-7f,inner)
                        path.lineTo(cx+1f,inner+sign*23f)
                        path.lineTo(cx+8f,inner+sign*3f)
                        path.close()
                        fill(if((i+motif)%2==0)glint else accent);p.alpha=210
                        c.drawPath(path,p);p.alpha=255
                    }
                } else {
                    for(i in 0..2) {
                        val cx=x+13f+i*(w-28f)/2f
                        oval(c,cx-5f,inner-4f,cx+5f,inner+4f,if((i+motif)%3==1)glint else accent)
                    }
                }
            }
        }
    }
    /** Seeded fractured island silhouettes instead of identical screen-height
     * rectangular columns. The solid mouth remains aligned with the existing
     * collision cap, while the distant sides taper and fracture into the biome.
     * All points stay inside the established shaft collider; no new hitboxes.
     */
    private fun drawCliffShaft(c:Canvas,x:Float,w:Float,start:Float,end:Float,
                               shadow:Int,stone:Int,seed:Int) {
        if(end-start<6f)return
        val span=end-start
        // Variable-width shafts: keep every fracture inside the true physical
        // body; the 54-unit minimum must not inherit an old 76-unit edge.
        val steps=6
        path.reset()
        for(i in 0..steps) {
            val y=start+span*i/steps
            val nearMouth=if(seed<0)i==steps else i==0
            val inset=if(nearMouth)1f else 3f+(((seed*11+i*7)%6+6)%6)
            val lx=x+inset
            if(i==0)path.moveTo(lx,y) else path.lineTo(lx,y)
        }
        for(i in steps downTo 0) {
            val y=start+span*i/steps
            val nearMouth=if(seed<0)i==steps else i==0
            val inset=if(nearMouth)1f else 3f+(((seed*3+i*13)%6+6)%6)
            path.lineTo(x+w-inset,y)
        }
        path.close()
        fill(shadow)
        c.drawPath(path,p)
        // Large angled mineral face gives each cliff a unique lit silhouette.
        path.reset()
        val left=x+12f+(kotlin.math.abs(seed)%3)*3f
        path.moveTo(left,start+span*.09f)
        path.lineTo(x+w*.53f,start+span*.20f)
        path.lineTo(x+w*.67f,end-span*.11f)
        path.lineTo(left+4f,end-span*.05f)
        path.close()
        fill(stone);p.alpha=196;c.drawPath(path,p);p.alpha=255
        // Three offset facets break the long monolithic column read. Their
        // outlines are wholly inside the original solid collider.
        for(facet in 0..2) {
            val yy=start+span*(.16f+facet*.22f)
            val tip=(yy+span*.12f).coerceAtMost(end-2f)
            val xx=x+9f+((kotlin.math.abs(seed*7+facet*13)%11)).toFloat()
            path.reset()
            path.moveTo(xx,yy)
            path.lineTo((xx+w*.30f).coerceAtMost(x+w-7f),yy+span*.035f)
            path.lineTo((xx+w*.15f).coerceAtMost(x+w-9f),tip)
            path.close()
            fill(if(facet==1)0x990a203e.toInt() else 0x77edfaff)
            c.drawPath(path,p)
        }
    }
    /**
     * Continuous rock shoulders visibly attach each procedural obstacle to
     * the TOP or BOTTOM of the flight camera. Unlike floating decorative
     * sprites, distant roots must stay within the x..x+width shaft hitbox.
     * Only lip bands project sideways (x-7..x+width+5) and have a matching
     * physical rounded-cap collider; no decorative phantom obstacles.
     * No bitmap allocations or per-frame state.
     */
    private fun drawRootedCliffBases(c:Canvas,x:Float,w:Float,top:Float,bottom:Float,
                                     seed:Int,shadow:Int,stone:Int){
        val left=x; val right=x+w
        val upperEnd=(top-38f).coerceAtMost(172f).coerceAtLeast(0f)
        if(upperEnd>8f){
            path.reset()
            path.moveTo(left,-12f)
            path.lineTo(right,-12f)
            path.lineTo(right,upperEnd*.63f)
            path.lineTo(x+w-4f,upperEnd)
            path.lineTo(x+9f,upperEnd*.86f)
            path.lineTo(left,upperEnd*.42f)
            path.close()
            fill(shadow);c.drawPath(path,p)
            path.reset()
            path.moveTo(x+3f,0f)
            path.lineTo(x+w*.72f,0f)
            path.lineTo(x+w*.55f,upperEnd*.64f)
            path.lineTo(x+10f,upperEnd*.38f)
            path.close()
            fill(stone);p.alpha=205;c.drawPath(path,p);p.alpha=255
        }
        val lowerStart=(bottom+38f).coerceAtLeast(624f).coerceAtMost(785f)
        if(lowerStart<780f){
            path.reset()
            path.moveTo(left,lowerStart+(seed%4)*2f)
            path.lineTo(x+8f,lowerStart)
            path.lineTo(x+w-4f,lowerStart+9f)
            path.lineTo(right,lowerStart)
            path.lineTo(right,826f)
            path.lineTo(left,826f)
            path.close()
            fill(shadow);c.drawPath(path,p)
            path.reset()
            path.moveTo(x+8f,lowerStart+10f)
            path.lineTo(x+w*.63f,lowerStart+24f)
            path.lineTo(x+w*.77f,826f)
            path.lineTo(x+5f,826f)
            path.close()
            fill(stone);p.alpha=205;c.drawPath(path,p);p.alpha=255
        }
    }
    /** Biome-colored fissure planes keep variable-width pillars visually substantial.
     * Seeded from each gate's phase: no randomness or bitmap work per frame.
     * This overlay remains inside each actual pillar collision shaft; the
     * opening, bird collider and cap faces are strictly unchanged.
     */
    private fun drawRockStrata(c:Canvas,x:Float,w:Float,start:Float,end:Float,
                               seed:Int,shadow:Int,glow:Int) {
        val height=end-start
        if(height<70f)return
        val count=(height/94f).toInt().coerceIn(1,7)
        for(i in 0 until count) {
            val jitter=((seed.toLong()*17L+i*23L).and(0x7L)).toFloat()
            val y=start+18f+i*(height-35f)/count
            val left=x+12f+jitter*.5f
            val mid=x+w*.44f
            val right=x+w-13f-jitter*.4f
            path.reset()
            path.moveTo(left,y)
            path.lineTo(mid,y+12f)
            path.lineTo(right,y+6f)
            path.lineTo(mid+4f,y+28f)
            path.close()
            fill(shadow);p.alpha=142
            c.drawPath(path,p);p.alpha=255
            path.reset()
            path.moveTo(left+2f,y+1f)
            path.lineTo(mid,y+11f)
            path.lineTo(right-3f,y+5f)
            fill(glow)
            p.style=Paint.Style.STROKE;p.strokeWidth=1.5f;p.alpha=135
            c.drawPath(path,p)
            p.style=Paint.Style.FILL;p.alpha=255
        }
    }
    /** Native illustrated forest berry, shared by corridor and safe-area HUD.
     * Its red body is inside the 13px food collider; foliage is decorative only.
     */
    private fun drawBerry(c:Canvas,cx:Float,cy:Float,r:Float){
        oval(c,cx-r+2f,cy-r*.65f,cx+r+2f,cy+r+3f,0x66301530)
        oval(c,cx-r,cy-r*.85f,cx+r,cy+r,0xffa51f45.toInt())
        oval(c,cx-r+2f,cy-r*.73f,cx+r-3f,cy+r-3f,0xfff0445b.toInt())
        oval(c,cx-r*.62f,cy-r*.53f,cx-r*.12f,cy-r*.08f,0x99ffe2ab.toInt())
        rect(c,cx-1.5f,cy-r-5f,cx+1.5f,cy-r*.5f,0xff734a2d.toInt(),1f)
        path.reset()
        path.moveTo(cx,cy-r*.86f)
        path.quadTo(cx+r*.52f,cy-r*1.75f,cx+r*.82f,cy-r*.92f)
        path.quadTo(cx+r*.44f,cy-r*.50f,cx,cy-r*.86f)
        fill(0xff5de18b.toInt());c.drawPath(path,p)
    }
    /** Grassy cap foliage is constrained to the solid stone lip.
     * Collision still uses the original identical rounded 7px cap geometry.
     */
    private fun drawMossLedge(c:Canvas,x:Float,w:Float,y:Float,seed:Int) {
        rect(c,x-5f,y+6f,x+w+3f,y+14f,0xff286d41.toInt(),5f)
        rect(c,x-4f,y+4f,x+w+2f,y+9f,0xff75cf69.toInt(),4f)
        for(i in 0..5){
            val gx=x+3f+(w-6f)*i/6f
            val rise=(seed+i*7)%5
            // Little round blades read as lush turf at both 60 and 120Hz.
            oval(c,gx,y+2f+rise,gx+11f,y+10f+rise,0xff42ab5d.toInt())
            if(i%2==0) oval(c,gx+3f,y+4f+rise,gx+9f,y+8f+rise,0xffb2ed7a.toInt())
        }
    }
    /** Real finite coins (gate.coin), drawn around the existing pickup centre.
     * Every shade belongs to the collectible, never to the collision shaft. */
    private fun drawGoldenCoin(c:Canvas,x:Float,y:Float) {
        oval(c,x-16f,y-12f,x+16f,y+18f,0x66082040)
        oval(c,x-15f,y-16f,x+15f,y+14f,0xffaa4d09.toInt())
        oval(c,x-12f,y-14f,x+12f,y+10f,0xffffb318.toInt())
        oval(c,x-10f,y-13f,x+10f,y+7f,0xffffe25c.toInt())
        oval(c,x-8f,y-10f,x+8f,y+6f,0xffffc536.toInt())
        // Crown emblem on the actual coin; not a phantom spendable gem.
        path.reset()
        path.moveTo(x-6f,y-3f);path.lineTo(x-3f,y+2f)
        path.lineTo(x,y-5f);path.lineTo(x+3f,y+2f)
        path.lineTo(x+6f,y-3f);path.lineTo(x+5f,y+6f)
        path.lineTo(x-5f,y+6f);path.close()
        fill(0xffa95504.toInt());c.drawPath(path,p)
        oval(c,x-10f,y-12f,x-5f,y-8f,0xccffffff.toInt())
    }
    /** The existing gate.star is represented as a sapphire crystal, matching
     * the reference. Collision/magnet semantics remain 'star' internally. */
    private fun drawBlueCrystal(c:Canvas,x:Float,y:Float) {
        oval(c,x-19f,y-20f,x+19f,y+19f,0x4484faff)
        path.reset()
        path.moveTo(x,y-21f);path.lineTo(x+15f,y)
        path.lineTo(x,y+22f);path.lineTo(x-15f,y);path.close()
        fill(0xff07559f.toInt());c.drawPath(path,p)
        path.reset()
        path.moveTo(x,y-18f);path.lineTo(x+12f,y)
        path.lineTo(x,y+16f);path.lineTo(x-12f,y);path.close()
        fill(0xff2dbfff.toInt());c.drawPath(path,p)
        path.reset()
        path.moveTo(x,y-18f);path.lineTo(x-12f,y)
        path.lineTo(x,y+16f);path.close()
        fill(0xff8eeaff.toInt());c.drawPath(path,p)
        path.reset()
        path.moveTo(x,y-14f);path.lineTo(x+5f,y-3f)
        path.lineTo(x,y+11f);path.lineTo(x-5f,y-3f);path.close()
        fill(0xccffffff.toInt());c.drawPath(path,p)
    }
    /** The icon sits at the real finite pickup power coordinate. A shield or
     * magnet is only rendered while the corresponding item is uncollected. */
    private fun drawPowerPickup(c:Canvas,x:Float,y:Float,kind:Int) {
        oval(c,x-17f,y-17f,x+17f,y+17f,0x66300a62)
        oval(c,x-14f,y-14f,x+14f,y+14f,0xff083a9d.toInt())
        oval(c,x-11f,y-11f,x+11f,y+11f,0xff64e4ff.toInt())
        if(kind==1) {
            path.reset();path.moveTo(x,y-10f);path.lineTo(x+8f,y-6f)
            path.lineTo(x+6f,y+5f);path.lineTo(x,y+10f)
            path.lineTo(x-6f,y+5f);path.lineTo(x-8f,y-6f);path.close()
            fill(0xffffe16a.toInt());c.drawPath(path,p)
        } else {
            // U-shaped magnet, cyan poles and real power type.
            fill(0xffff4479.toInt());p.style=Paint.Style.STROKE
            p.strokeWidth=5f
            path.reset();path.moveTo(x-7f,y-6f);path.lineTo(x-7f,y+3f)
            path.quadTo(x,y+15f,x+7f,y+3f);path.lineTo(x+7f,y-6f)
            c.drawPath(path,p);p.style=Paint.Style.FILL
            rect(c,x-10f,y-9f,x-4f,y-4f,0xffffffff.toInt(),1f)
            rect(c,x+4f,y-9f,x+10f,y-4f,0xffffffff.toInt(),1f)
        }
    }

    /** Small native heart sprite, with no extra bitmap allocations. */
    private fun drawHeartPickup(c:Canvas,cx:Float,cy:Float){
        path.reset()
        path.moveTo(cx,cy+11f)
        path.cubicTo(cx-23f,cy-3f,cx-14f,cy-18f,cx,cy-9f)
        path.cubicTo(cx+14f,cy-18f,cx+23f,cy-3f,cx,cy+11f)
        fill(0xfff45477.toInt());c.drawPath(path,p)
        oval(c,cx-9f,cy-10f,cx-3f,cy-5f,0x99ffffff.toInt())
    }
    /** Stateless course strokes: no per-gate lambda or array allocation. */
    private fun drawStoneCourses(c:Canvas,x:Float,yStart:Float,yEnd:Float,
                                 shade:Int,seam:Int,forest:Boolean) {
        var row=yStart
        var n=0
        while(row+16f<yEnd && n<12){
            rect(c,x+8f+(n%3)*4f,row,x+42f+(n%2)*6f,row+3f,seam,2f)
            if(n%2==0){
                path.reset();path.moveTo(x+25f,row+5f)
                path.lineTo(x+18f,row+18f);path.lineTo(x+34f,row+29f)
                fill(shade);p.style=Paint.Style.STROKE;p.strokeWidth=2.5f
                p.alpha=125;c.drawPath(path,p);p.style=Paint.Style.FILL;p.alpha=255
            }
            if(forest && n%3==0){
                oval(c,x+34f,row+5f,x+44f,row+18f,0xff67d87d.toInt())
            }
            row+=57f;n++
        }
    }
    /** World-colored beveling stays entirely inside the real rounded collision lip. */
    private fun drawPolishedGateLip(c:Canvas,x:Float,w:Float,y:Float) {
        // Thin highlight, shaded vertical edges and underside add depth without
        // making the collision opening look narrower than the actual hitbox.
        rect(c,x-3f,y+3f,x+w+1f,y+7f,0x66ffffff,2f)
        rect(c,x-3f,y+8f,x+2f,y+22f,0x44ffffff,2f)
        rect(c,x+w-2f,y+7f,x+w+2f,y+23f,0x880c2747.toInt(),2f)
        rect(c,x-3f,y+23f,x+w+2f,y+26f,0x990a2948.toInt(),2f)
    }
    /** Decorative sapphire/gold inlays stay entirely inside the existing
     * top/bottom 27–28px physical cap, keeping collision silhouettes exact.
     * All biomes reuse the same royal-blue/gold trim from the reference. */
    private fun drawGateCapInlay(c:Canvas,x:Float,w:Float,y:Float,kind:Int) {
        val middle=x+w*.5f
        val cobalt=if(kind==0)0xff1464a2.toInt() else pillarDark[kind]
        // Two polished bolts and a faceted seal are painted ONLY in solid stone.
        oval(c,x+4f,y+9f,x+11f,y+16f,0xffffdc67.toInt())
        oval(c,x+w-11f,y+9f,x+w-4f,y+16f,0xffffdc67.toInt())
        path.reset()
        path.moveTo(middle,y+5f)
        path.lineTo(middle+9f,y+14f)
        path.lineTo(middle,y+23f)
        path.lineTo(middle-9f,y+14f)
        path.close()
        fill(0xff533114.toInt());c.drawPath(path,p)
        path.reset()
        path.moveTo(middle,y+7f)
        path.lineTo(middle+7f,y+14f)
        path.lineTo(middle,y+21f)
        path.lineTo(middle-7f,y+14f)
        path.close()
        fill(cobalt);c.drawPath(path,p)
        path.reset()
        path.moveTo(middle,y+8f)
        path.lineTo(middle-6f,y+14f)
        path.lineTo(middle,y+18f)
        path.close()
        fill(0xffb6f5ff.toInt());c.drawPath(path,p)
    }
    /**
     * Collision-faithful upper/lower hanging mace, drawn only at actual physical
     * hazard locations. Spikes remain within the 18-unit circular contact rim.
     */
    private fun drawSkyMace(c:Canvas,g:LevelEngine.Gate,x:Float,
                            index:Int,a:LevelEngine.Opening) {
        if(!SkyMaceHazard.enabled(g,index))return
        val cx=SkyMaceHazard.bobX(g,x,game.time)
        val cy=SkyMaceHazard.bobY(g,a,game.time,index)
        val anchorX=x+g.width*.5f
        val anchorY=if(index%2==0)a.top-7f else a.bottom+7f
        fill(0xff44314c.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=5f
        p.strokeCap=Paint.Cap.ROUND
        c.drawLine(anchorX,anchorY,cx,cy,p)
        fill(0xffffbd4d.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=1.8f
        c.drawLine(anchorX,anchorY,cx,cy,p)
        p.style=Paint.Style.FILL
        p.strokeCap=Paint.Cap.BUTT
        // The dark rim itself is physical, not just invisible spike tips.
        oval(c,cx-18f,cy-18f,cx+18f,cy+18f,0xff15213f.toInt())
        oval(c,cx-15f,cy-15f,cx+15f,cy+15f,0xff58627e.toInt())
        for(i in 0..7) {
            val angle=i*0.78539816f+game.time*.45f
            val dx=cos(angle)
            val dy=sin(angle)
            val px=-dy
            val py=dx
            path.reset()
            path.moveTo(cx+dx*13f+px*5f,cy+dy*13f+py*5f)
            path.lineTo(cx+dx*18f,cy+dy*18f)
            path.lineTo(cx+dx*13f-px*5f,cy+dy*13f-py*5f)
            path.close()
            fill(0xffffca58.toInt())
            c.drawPath(path,p)
        }
        oval(c,cx-8f,cy-9f,cx+6f,cy+5f,0xff38425f.toInt())
        oval(c,cx-9f,cy-11f,cx-4f,cy-7f,0xffc7e6ff.toInt())
    }
    private fun drawGate(c:Canvas,g:LevelEngine.Gate,x:Float,index:Int,incoming:Boolean=false){
        val a=LevelEngine.opening(g,game.time,game.difficulty)
        val w=g.width
        val color=pillars[g.kind]
        val shade=pillarDark[g.kind]
        val cap=if(g.kind==0)0xff55ce6c.toInt() else color
        val top=a.top;val bottom=a.bottom
        val cliffSeed=(g.phase*1024f).toInt()+index*31+g.kind*59+1
        drawRootedCliffBases(c,x,w,top,bottom,cliffSeed,shade,color)
        drawCliffShaft(c,x,w,pillarRenderTop,top,shade,color,-cliffSeed)
        rect(c,x-7f,top-28f,x+w+5f,top,cap,7f)
        drawPolishedGateLip(c,x,w,top-28f)
        drawGateCapInlay(c,x,w,top-28f,g.kind)
        if(g.kind==0) drawMossLedge(c,x,w,top-28f,cliffSeed)
        drawCliffShaft(c,x,w,bottom,pillarRenderBottom,shade,color,cliffSeed)
        rect(c,x-7f,bottom,x+w+5f,bottom+27f,cap,7f)
        drawPolishedGateLip(c,x,w,bottom)
        drawGateCapInlay(c,x,w,bottom,g.kind)
        if(g.kind==0) drawMossLedge(c,x,w,bottom,cliffSeed)
        drawRockFacets(c,x,w,pillarRenderTop+7f,top-30f)
        drawRockFacets(c,x,w,bottom+28f,pillarRenderBottom-14f)
        // Rock strata follow each world's material, not a generic flat
        // white column. They never project into the traversable gap.
        val stratumGlint=biomeReliefGlints[g.kind]
        drawRockStrata(c,x,w,pillarRenderTop+11f,top-29f,cliffSeed,shade,stratumGlint)
        drawRockStrata(c,x,w,bottom+28f,pillarRenderBottom-12f,-cliffSeed,shade,stratumGlint)
        // A restrained side light replaces dozens of noisy flecks/flowers.
        // Geometry and biome strata remain unchanged for collision fairness.
        val shaftLight=0x55ffffff
        rect(c,x+6f,pillarRenderTop+12f,x+10f,top-30f,shaftLight,2f)
        rect(c,x+6f,bottom+29f,x+10f,pillarRenderBottom-14f,shaftLight,2f)
        drawMovingGateRimCues(c,g,x,top,bottom)
        drawBiomeGateRelief(c,x,w,top,bottom,g.kind,cliffSeed)
        // Stone courses, mineral seams and edge lighting add biome-specific
        // depth without textures decoded or allocated inside the frame loop.
        val seam=when(g.kind){
            0->0xff685e5e.toInt();1->0xffb57852.toInt()
            2->0xffe7fbff.toInt();3->0xffff973e.toInt()
            4->0xffffe9ab.toInt();5->0xffa693ed.toInt()
            6->0xff89fff0.toInt();else->0xffb7b2ff.toInt()
        }
        // Avoid creating a closure for every on-screen pillar, every frame.
        // Exact same seeded relief and coordinates as the previous renderer.
        drawStoneCourses(c,x,pillarRenderTop+36f,top-34f,shade,seam,g.kind==0)
        drawStoneCourses(c,x,bottom+34f,pillarRenderBottom-16f,shade,seam,g.kind==0)
        rect(c,x-3f,top-27f,x+w+2f,top-23f,0x88ffffff.toInt(),2f)
        rect(c,x-3f,bottom+3f,x+w+2f,bottom+8f,0x66ffffff,2f)
        // Vegetated stone relief in the collision-solid lip/shaft, never in the gap.
        if(g.kind==0) {
            for(j in 0..3) {
                val fx=x+10f+(w-24f)*j/3f
                oval(c,fx-5f,top-24f,fx+9f,top-12f,0xff2f965a.toInt())
                oval(c,fx-3f,top-23f,fx+7f,top-16f,0xff79ed86.toInt())
                oval(c,fx-4f,bottom+8f,fx+6f,bottom+18f,0xff40a768.toInt())
            }
            // Sparse tiny flowers and climbing leaves, arranged by the pillar seed.
            for(j in 0..2) {
                val yy=bottom+47f+j*81f+(cliffSeed%13)
                if(yy+22f<791f) {
                    val fx=x+15f+(j%2)*27f
                    oval(c,fx-9f,yy-4f,fx+6f,yy+11f,0xff387451.toInt())
                    oval(c,fx-3f,yy-7f,fx+11f,yy+5f,0xff72d887.toInt())
                    oval(c,fx+3f,yy-4f,fx+8f,yy+1f,0xffffe2a1.toInt())
                }
            }
        }
        // An approaching *moving* pillar gets an unmistakable stationary
        // warning badge INSIDE its solid upper shaft (never the open corridor).
        // Position follows the real animated opening; reduced-motion mode has
        // no flashing. Static columns and already-passed pillars get no badge.
        if(FlightHudLayout.movingWarningVisible(g.movement,x,game.birdX) && top>72f){
            val cx=x+w*.5f
            val badgeY=top-46f
            rect(c,cx-11f,badgeY-13f,cx+11f,badgeY+11f,0xffffd876.toInt(),6f)
            text(c,"!",cx,badgeY+6f,18f,0xff122647.toInt(),true)
        }
        // This is an actual gameplay obstacle: the same pose is swept in GameSimulation.
        drawSkyMace(c,g,x,index,a)
        val mid=x+w*.5f
        val cy=(a.top+a.bottom)*.5f
        if(game.endless) {
            if(game.heartVisible(index,incoming)) drawHeartPickup(c,mid,cy)
            else if(game.berryVisible(index,incoming)) drawBerry(c,mid,cy,13f)
        } else {
            // Never paint rewards which have already been collected. The
            // positions match GameSimulation's existing magnet/contact math.
            if(game.coinVisible(index,incoming))drawGoldenCoin(c,mid,cy)
            if(game.crystalVisible(index,incoming))drawBlueCrystal(c,mid+35f,cy-25f)
            if(game.powerVisible(index,incoming))drawPowerPickup(c,mid+39f,cy+39f,g.power)
        }
    }
    /** Bursts follow actual game pickup/shield events, not an arbitrary animation timer.
     * Deterministic Canvas particles are bounded and allocate no sprites each frame.
     * Reduced-motion users keep the existing calm feedback rings, without sparks.
     */
    private fun drawFeedbackSparkles(c:Canvas) {
        if(reducedMotion) return
        val pickup=game.collectPulse
        val hit=game.impactPulse
        if(pickup<=0f && hit<=0f) return
        if(pickup>0f) {
            val progress=(1f-pickup/.36f).coerceIn(0f,1f)
            val radius=24f+42f*progress
            fill(pickupHues[game.level.world])
            p.alpha=(210f*(1f-progress)).toInt().coerceIn(0,210)
            for(i in 0 until 10) {
                val angle=i*6.2831853f/10f
                val x=game.birdX+cos(angle)*radius
                val y=game.y+sin(angle)*radius
                c.drawCircle(x,y,2.9f+(i%3)*.65f,p)
            }
        }
        if(hit>0f) {
            val progress=(1f-hit/.65f).coerceIn(0f,1f)
            val radius=33f+48f*progress
            fill(0xffb8efff.toInt())
            p.alpha=(230f*(1f-progress)).toInt().coerceIn(0,230)
            p.style=Paint.Style.STROKE
            p.strokeWidth=2.8f
            p.strokeCap=Paint.Cap.ROUND
            for(i in 0 until 12) {
                val angle=i*6.2831853f/12f
                val dx=cos(angle)
                val dy=sin(angle)
                c.drawLine(game.birdX+dx*radius,game.y+dy*radius,
                           game.birdX+dx*(radius+9f),game.y+dy*(radius+9f),p)
            }
        }
        p.alpha=255
        p.style=Paint.Style.FILL
        p.strokeCap=Paint.Cap.BUTT
    }
    /** A tap pushes a short, world-colored gust away from Bopi's wings.
     * Uses the real flap event rather than a looping decorative timer.
     * The burst is cosmetic and deliberately disabled for reduced motion.
     */
    private fun drawFlapWake(c:Canvas) {
        if(reducedMotion || game.flapPulse<=0f) return
        val strength=(game.flapPulse/.24f).coerceIn(0f,1f)
        val progress=1f-strength
        val hue=pickupHues[game.level.world] and 0x00ffffff
        val radius=28f+27f*progress
        fill(((105f*strength).toInt().coerceIn(0,105) shl 24) or hue)
        p.style=Paint.Style.STROKE
        p.strokeWidth=2.5f
        c.drawCircle(game.birdX,game.y,radius,p)
        p.style=Paint.Style.FILL
        for(i in 0 until 7) {
            val x=game.birdX-25f-i*9f-30f*progress
            val y=game.y+(i%3-1)*19f+sin(game.time*13f+i*1.7f)*4f
            fill(((190f*strength).toInt().coerceIn(0,190) shl 24) or hue)
            c.drawCircle(x,y,2.4f+(i%3)*.7f,p)
        }
    }
    private fun drawBird(c:Canvas){
        if(game.collectPulse>0f){
            val portion=game.collectPulse/.36f
            fill(0xffffe69c.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=if(reducedMotion)2f else 3f
            p.alpha=(190f*portion).toInt().coerceIn(0,190)
            c.drawCircle(game.birdX,game.y,28f+(1f-portion)*38f,p)
            p.style=Paint.Style.FILL;p.alpha=255
        }
        if(game.impactPulse>0f){
            val portion=game.impactPulse/.65f
            fill(if(game.lastImpactLostLife)0xffffa1b2.toInt() else 0xffb8efff.toInt())
            p.style=Paint.Style.STROKE;p.strokeWidth=5f
            p.alpha=(210f*portion).toInt().coerceIn(0,210)
            c.drawCircle(game.birdX,game.y,35f+(1f-portion)*28f,p)
            p.style=Paint.Style.FILL;p.alpha=255
        }
        drawFeedbackSparkles(c)
        drawFlapWake(c)
        // Keep a calm, unmistakable grace-period outline when a heart is
        // lost. Continuous light replaces unpleasant full-sprite blinking.
        if(game.invulnerable>0f && game.lastImpactLostLife) {
            fill(0xfffa7797.toInt())
            p.style=Paint.Style.STROKE
            p.strokeWidth=2.3f
            p.alpha=if(reducedMotion)105 else (65f+35f*
                kotlin.math.sin(game.time*3.5f)).toInt().coerceIn(30,130)
            c.drawCircle(game.birdX,game.y,49f,p)
            p.style=Paint.Style.FILL
            p.alpha=255
        }
        // Flight ribbons keep the sprite visually connected to the wind and world.
        // No temporary bitmaps or Paint instances in the render loop.
        if(!reducedMotion && game.active && !game.finished) {
            for(i in 0 until 5) {
                val x=game.birdX-19f-i*16f
                val y=game.y+(i%3-1)*13f+sin(game.time*5f+i)*3f
                fill(0xffb4edff.toInt())
                p.style=Paint.Style.STROKE
                p.strokeWidth=2.8f-i*.32f
                p.strokeCap=Paint.Cap.ROUND
                p.alpha=130-i*18
                path.reset();path.moveTo(x,y)
                path.quadTo(x-13f,y-5f,x-27f-i*2f,y+2f)
                c.drawPath(path,p)
            }
            p.style=Paint.Style.FILL;p.strokeCap=Paint.Cap.BUTT;p.alpha=255
        }
        if(game.magnetTime>0f)oval(c,game.birdX-34f,game.y-34f,game.birdX+34f,game.y+34f,0x444fdfff)
        if(game.shield>0){
            fill(0xff9fefff.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=3f
            c.drawCircle(game.birdX,game.y,48f+sin(game.time*5f)*2f,p)
            p.style=Paint.Style.FILL
        }
        c.save();c.translate(game.birdX,game.y)
        // Soft sky-blue/gold light remains behind the real moving wings and body.
        // A flap enlarges only the light, not the sprite or collision radius.
        val auraImpulse=if(reducedMotion)0f else (game.flapPulse/.24f).coerceIn(0f,1f)
        c.save()
        val auraScale=1f+auraImpulse*.13f
        c.scale(auraScale,auraScale)
        fill(Color.WHITE)
        p.shader=birdAuraShader
        c.drawCircle(0f,0f,53f,p)
        p.shader=null
        c.restore()
        // Every pilot remains front-facing like Bopi, including Sova and Lisac.
        c.rotate(PilotFlightPose.pitchDegrees(game.velocity,reducedMotion))
        // Keep the head, goggles and scarf stable; only detached wings flap.
        if (birdBitmap != null) {
            fill(Color.WHITE)
            // Immediate upward wing response on touch, then a 240 ms return.
            val flap=PilotFlightPose.wingDegrees(game.time,game.flapPulse,reducedMotion)
            wingLeftBitmap?.let { wing ->
                c.save();c.rotate(flap,wingLeftPivotX,wingLeftPivotY)
                c.drawBitmap(wing,null,birdRect,p);c.restore()
            }
            wingRightBitmap?.let { wing ->
                c.save();c.rotate(-flap,wingRightPivotX,wingRightPivotY)
                c.drawBitmap(wing,null,birdRect,p);c.restore()
            }
            c.drawBitmap(birdBitmap,null,birdRect,p)
            c.restore()
            return
        }
        // Safe fallback if a damaged installation lacks sprite resources.
        // Compute fallback-only wing phase after the normal bitmap renderer
        // returns, rather than doing extra sine work on every normal frame.
        val flapStrength=if(reducedMotion)0f else (game.flapPulse/.24f).coerceIn(0f,1f)
        val phase=if(reducedMotion)0f else sin(game.time*19f)+flapStrength*.65f
        path.reset();path.moveTo(-12f,11f);path.lineTo(-35f-3f*phase,22f);path.lineTo(-29f,5f);path.close();fill(0xffef385b.toInt());c.drawPath(path,p)
        oval(c,-30f,0f,-8f,16f,0xff1678d8.toInt())
        c.save();c.rotate(phase*26f,-13f,0f);oval(c,-28f,-4f,8f,16f,0xff0c78dc.toInt());oval(c,-25f,-6f,4f,5f,0xff4ac3ff.toInt());c.restore()
        oval(c,-23f,-24f,26f,25f,0xff095cc8.toInt())
        oval(c,-20f,-25f,23f,23f,feather[skinIndex.coerceIn(0,5)])
        oval(c,-13f,5f,19f,25f,Color.WHITE)
        oval(c,-14f,-17f,-2f,-10f,0x80ffffff.toInt())
        oval(c,18f,0f,25f,7f,0xffffa9ad.toInt())
        oval(c,-7f,-16f,7f,3f,Color.WHITE);oval(c,6f,-15f,20f,4f,Color.WHITE)
        val blink=if(!reducedMotion && (game.time%4.7f)>4.57f) 3f else 13f
        oval(c,-2f,-blink,5f,4f,0xff10224f.toInt());oval(c,10f,-blink,17f,4f,0xff10224f.toInt())
        path.reset();path.moveTo(11f,5f);path.lineTo(30f,9f);path.lineTo(13f,17f);path.close();fill(0xffffb321.toInt());c.drawPath(path,p)
        rect(c,-18f,7f,21f,14f,0xffff5b61.toInt(),3f)
        // bronze flying goggles with glints
        rect(c,-23f,-24f,22f,-17f,0xff7d451f.toInt(),3f)
        oval(c,-16f,-37f,5f,-16f,0xffa65d2b.toInt());oval(c,-12f,-34f,2f,-20f,0xff8cdeff.toInt())
        oval(c,3f,-36f,25f,-16f,0xffa65d2b.toInt());oval(c,7f,-32f,20f,-20f,0xff8cdeff.toInt())
        c.restore()
    }
    /** Reference-sized ready-to-fly callout. World physics and touch targets
     * remain unchanged: the whole GameView still responds to the first tap.
     * Display only while idle; no faux HUD after an actual flap.
     */
    private fun drawFlightReadyTutorial(c:Canvas) {
        val a=FlightReadyOverlayLayout
        rect(c,a.left+1.5f,a.top+3f,a.right+1.5f,a.bottom+3f,0x44051b44,22f)
        rect(c,a.left,a.top,a.right,a.bottom,0xa7193f80.toInt(),22f)
        fill(0xff8bdfff.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=1.5f
        c.drawRoundRect(a.left+1f,a.top+1f,a.right-1f,a.bottom-1f,21f,21f,p)
        p.style=Paint.Style.FILL

        // Original Croatian instructions stay available. English Android
        // locale uses the short one-line wording in the approved reference.
        val croatian=resources.configuration.locales.get(0).language=="hr"
        if(croatian) {
            drawTutorialText(c,"DODIRNI ZA LET",240f,624f,17f)
            text(c,"DODIR = ZAMAH KRILIMA",240f,642f,10f,0xffd3f5ff.toInt(),true)
        } else {
            drawTutorialHand(c,163f,626f)
            drawTutorialText(c,"Tap to fly",a.textCenterX,a.textBaselineY,21f)
        }
    }

    private fun drawTutorialText(c:Canvas,message:String,cx:Float,
                                 baseline:Float,size:Float) {
        fill(0xff082859.toInt())
        p.typeface=headerTypeface
        p.textAlign=Paint.Align.CENTER
        p.textSize=size
        p.strokeJoin=Paint.Join.ROUND
        p.style=Paint.Style.STROKE
        p.strokeWidth=3f
        c.drawText(message,cx,baseline,p)
        p.style=Paint.Style.FILL
        p.color=Color.WHITE
        c.drawText(message,cx,baseline,p)
        p.textAlign=Paint.Align.LEFT
    }

    /** Tiny local vector hand/click rays; no imported icons or per-frame bitmaps. */
    private fun drawTutorialHand(c:Canvas,x:Float,y:Float) {
        path.reset()
        path.moveTo(x-4f,y+14f)
        path.lineTo(x-12f,y+2f)
        path.quadTo(x-14f,y-2f,x-10f,y-4f)
        path.quadTo(x-7f,y-5f,x-5f,y-1f)
        path.lineTo(x-5f,y-13f)
        path.quadTo(x-4f,y-17f,x,y-17f)
        path.quadTo(x+4f,y-16f,x+4f,y-12f)
        path.lineTo(x+4f,y-1f)
        path.quadTo(x+8f,y-5f,x+11f,y-2f)
        path.lineTo(x+15f,y+9f)
        path.quadTo(x+13f,y+17f,x+4f,y+18f)
        path.close()
        fill(Color.WHITE)
        c.drawPath(path,p)
        fill(0xff063064.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=2.4f
        c.drawPath(path,p)
        c.drawLine(x-12f,y-16f,x-17f,y-21f,p)
        c.drawLine(x-18f,y-7f,x-23f,y-9f,p)
        c.drawLine(x+10f,y-15f,x+15f,y-20f,p)
        p.style=Paint.Style.FILL
    }

    /** Only the real lives remain in flight; the 480x800 physics remain unchanged. */
    private fun hudGlass(c:Canvas,l:Float,t:Float,r:Float,b:Float) {
        // Unified premium glass: restrained corner radius, thin highlight,
        // softer fill, and one subtle lower edge for separation from the sky.
        // Uses the existing Paint and primitives with zero shader allocations.
        rect(c,l+1f,t+3f,r-1f,b+2f,0x44031835,18f)
        rect(c,l,t,r,b,0xe8092949.toInt(),18f)
        fill(0xff8ae8f9.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=1.3f
        p.alpha=155
        c.drawRoundRect(l+1f,t+1f,r-1f,b-1f,17f,17f,p)
        p.style=Paint.Style.FILL
        p.alpha=255
        rect(c,l+15f,t+5f,r-15f,t+7f,0x82bfefff.toInt(),2f)
    }
    private fun drawHudHeart(c:Canvas,x:Float,y:Float,active:Boolean){
        path.reset()
        path.moveTo(x,y+7f)
        path.cubicTo(x-12f,y-1f,x-7f,y-10f,x,y-5f)
        path.cubicTo(x+7f,y-10f,x+12f,y-1f,x,y+7f)
        fill(if(active)0xffff4f51.toInt() else 0x552c4b73)
        c.drawPath(path,p)
        // Deep navy contour and a short enamel reflection match the reference;
        // rendering stays allocation-free and inactive hearts remain distinct.
        fill(if(active)0xff082e60.toInt() else 0xffa0b8d0.toInt())
        p.style=Paint.Style.STROKE
        p.strokeWidth=2.3f
        c.drawPath(path,p)
        p.style=Paint.Style.FILL
        if(active){
            fill(0xb6ffffff.toInt())
            p.style=Paint.Style.STROKE
            p.strokeWidth=1.3f
            p.strokeCap=Paint.Cap.ROUND
            c.drawLine(x-6f,y-4f,x-3f,y-5f,p)
            p.style=Paint.Style.FILL
            p.strokeCap=Paint.Cap.BUTT
        }
    }
    private fun drawHud(c:Canvas){
        // Minimal native HUD: lives only. Scores, berries and abilities remain
        // in GameSimulation for authentic results and saved history, never in flight.
        val lives=game.lives.coerceIn(0,game.maxLives)
        val h=FlightHudLayout
        hudGlass(c,h.lives.left,h.lives.top,h.lives.right,h.lives.bottom)
        // Bird portrait and three glossy hearts are the ONLY flight overlay.
        // No hidden/painted pause button, scores or wallet. Avatar is clipped
        // into its round bezel; the original selected pilot stays visible.
        oval(c,h.avatarX-25f,h.avatarY-25f,h.avatarX+25f,h.avatarY+25f,0xff052a67.toInt())
        oval(c,h.avatarX-23f,h.avatarY-23f,h.avatarX+23f,h.avatarY+23f,0xff78ddff.toInt())
        c.save()
        path.reset()
        path.addCircle(h.avatarX,h.avatarY,h.avatarRadius,Path.Direction.CW)
        c.clipPath(path)
        birdBitmap?.let {
            fill(Color.WHITE)
            c.drawBitmap(it,null,hudPortraitRect,p)
        } ?: oval(c,h.avatarX-20f,h.avatarY-19f,h.avatarX+20f,h.avatarY+22f,
            0xff239bdf.toInt())
        c.restore()
        // Reference has no horizontal pink life bar; communicate final-life
        // urgency via the avatar's tiny rim glint instead.
        val rim=FlightHudReadability.lifeCountColor(lives,game.maxLives)
        oval(c,h.avatarX-18f,h.avatarY-22f,h.avatarX-10f,h.avatarY-20f,rim)
        for(i in 0 until game.maxLives.coerceAtMost(h.heartCenters.size)) {
            c.save()
            c.scale(1.17f,1.17f,h.heartCenters[i],h.heartBaseline)
            drawHudHeart(c,h.heartCenters[i],h.heartBaseline,i<lives)
            c.restore()
        }
        if(lives!=lastAccessibilityLives) {
            lastAccessibilityLives=lives
            val lifeNotice=if(FlightHudReadability.isFinalLife(lives,game.maxLives))
                " Posljednji život." else ""
            lastHudAccessibility="${flightHint()} Životi: $lives/${game.maxLives}.$lifeNotice"
            contentDescription=lastHudAccessibility
        }
    }
    /** Developer QA outlines in the shared logical 480x800 world; no gameplay effect. */
    private fun drawCollisionDebugOverlay(c:Canvas){
        fill(0xffffd54f.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=2f
        c.drawCircle(game.birdX,game.y,game.radius,p)
        for(i in game.level.gates.indices){
            val g=game.level.gates[i];val x=game.gateX(g)
            if(x< -100f || x>550f)continue
            val o=LevelEngine.opening(g,game.time,game.difficulty)
            fill(0xffef61d9.toInt());p.style=Paint.Style.STROKE;p.strokeWidth=1.5f
            c.drawRect(x,-25f,x+g.width,o.top,p)
            c.drawRect(x,o.bottom,x+g.width,825f,p)
            fill(0xff28e6ff.toInt());p.style=Paint.Style.STROKE
            c.drawRoundRect(x-7f,o.top-28f,x+g.width+5f,o.top,7f,7f,p)
            c.drawRoundRect(x-7f,o.bottom,x+g.width+5f,o.bottom+27f,7f,7f,p)
            if(game.endless && game.berryVisible(i)){
                fill(0xff67f78d.toInt());p.style=Paint.Style.STROKE
                c.drawCircle(x+g.width*.5f,(o.top+o.bottom)*.5f,13f,p)
            }
        }
        p.style=Paint.Style.FILL;p.strokeWidth=1f
    }
    /** Every ACTION_DOWN is one flap; holding and moving cannot steer. */
    override fun onTouchEvent(event:MotionEvent):Boolean {
        if(event.actionMasked==MotionEvent.ACTION_DOWN) performClick()
        return true
    }
    override fun performClick():Boolean {
        super.performClick()
        if(!paused && !game.finished){
            if(qaWaitingForCapture) {
                // A second genuine touch resumes the *same* run, not a replay.
                // No synthetic score, forced collision, or screenshot is used.
                qaWaitingForCapture=false
                frameClock.reset()
                game.flap()
            } else {
                val firstFlap=!game.active
                if(firstFlap) frameClock.reset()
                game.flap()
                if(firstFlap && game.active) {
                    if(qaCaptureEnabled) qaWaitingForCapture=true
                }
            }
            // Keep the same accessible lives/flap label instead of only
            // the pilot name after each tap; reuse the HUD snapshot instead.
            contentDescription=lastHudAccessibility.ifEmpty { flightHint() }
            onFlap()
            invalidate()
        }
        return true
    }
    override fun onAttachedToWindow(){
        super.onAttachedToWindow()
        refreshPowerSaveMode()
        if(!powerSaveReceiverRegistered){
            // This is an exclusively system-owned protected broadcast.
            context.registerReceiver(powerSaveReceiver,
                IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
            powerSaveReceiverRegistered=true
        }
        // Reattachment is legal after OS window transitions: never draw a recycled Bitmap.
        val opts=BitmapFactory.Options().apply { inScaled=false }
        if(birdBitmap==null) birdBitmap=BitmapFactory.decodeResource(resources,birdSprites[selectedSkin],opts)
        if(wingLeftBitmap==null) wingLeftBitmap=BitmapFactory.decodeResource(resources,leftWings[selectedSkin],opts)
        if(wingRightBitmap==null) wingRightBitmap=BitmapFactory.decodeResource(resources,rightWings[selectedSkin],opts)
        if(worldBitmap==null) worldBitmap=BitmapFactory.decodeResource(resources,worldBitmaps[game.level.world],opts)
        updateWorldCover(width,height)
        invalidate()
    }
    override fun onWindowVisibilityChanged(visibility:Int){
        super.onWindowVisibilityChanged(visibility)
        if(visibility==VISIBLE){
            // Returning from background also refreshes any battery-saver change.
            refreshPowerSaveMode()
            frameClock.reset()
        }
    }
    override fun onDetachedFromWindow(){
        if(powerSaveReceiverRegistered){
            context.unregisterReceiver(powerSaveReceiver)
            powerSaveReceiverRegistered=false
        }
        // Release native pixel buffer references after each Retry/Exit.
        // Avoid explicit recycle(): RenderThread may still own display lists.
        paused=true
        frameClock.reset()
        birdBitmap=null;wingLeftBitmap=null;wingRightBitmap=null;worldBitmap=null
        super.onDetachedFromWindow()
    }
}
