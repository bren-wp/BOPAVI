#!/usr/bin/env python3
from pathlib import Path
from PIL import Image
import plistlib
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
assert len(list((root/'ios/BOPAVI').glob('*.swift'))) == 6
assert len(list((root/'android/app/src/main/java/com/brendigo/bopavi').glob('*.kt'))) == 6
for path in [root/'ios/BOPAVI/Info.plist',root/'ios/BOPAVI.xcodeproj/project.pbxproj',root/'android/app/src/main/AndroidManifest.xml']:
    assert path.exists(),path
plistlib.load(open(root/'ios/BOPAVI/Info.plist','rb'))
ET.parse(root/'android/app/src/main/AndroidManifest.xml')
for path in [root/'ios/BOPAVI/Assets.xcassets/AppIcon.appiconset/icon-1024.png',root/'android/app/src/main/res/drawable/app_icon.png']:
    im=Image.open(path);assert im.width>=512 and im.height>=512
for f in list((root/'android').rglob('*.kt'))+list((root/'ios').rglob('*.swift')):
    txt=f.read_text()
    assert 'android.webkit' not in txt and 'import WebKit' not in txt and 'WKWebView(' not in txt,(f,'web embed detected')
manifest=(root/'android/app/src/main/AndroidManifest.xml').read_text()
assert 'android.permission.INTERNET' not in manifest
assert 'android:allowBackup="false"' in manifest
assert 'BOPAVI' in manifest
for platform in ['android/app/src/main/res/raw','ios/BOPAVI/Audio']:
    audio=root/platform
    for name in [*(f'world_{i}' for i in range(8)), 'tap','collect','level','hit','purchase','click']:
        assert (audio/(name+'.wav')).is_file(),(platform,name)
for control in ['android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt','ios/BOPAVI/GameController.swift']:
    source=(root/control).read_text()
    home=source.split('private fun showHome()',1)[1].split('private fun showWorlds()',1)[0] if control.endswith('.kt') else source.split('private func showHome()',1)[1].split('private func showWorlds()',1)[0]
    for banned in ['BESKONAČNI','DNEVNA NAGRADA','1.048.576','8.388.608']:
        assert banned not in home,(control,banned)
# Validate platform/release version parity instead of hardcoding a stale version.
import re
gradle=(root/'android/app/build.gradle.kts').read_text()
xcode=(root/'ios/BOPAVI.xcodeproj/project.pbxproj').read_text()
workflow=(root/'.github/workflows/native-ci.yml').read_text()
android_versions=re.findall(r'versionName\s*=\s*"(\d+\.\d+\.\d+)"',gradle)
android_builds=re.findall(r'versionCode\s*=\s*(\d+)',gradle)
ios_versions=re.findall(r'MARKETING_VERSION\s*=\s*(\d+\.\d+\.\d+)',xcode)
ios_builds=re.findall(r'CURRENT_PROJECT_VERSION\s*=\s*(\d+)',xcode)
assert len(android_versions)==1 and len(android_builds)==1, "Android version missing or ambiguous"
assert len(ios_versions)>=2 and len(ios_builds)>=2, "iOS debug/release versions missing"
assert set(android_versions)==set(ios_versions), "Android/iOS marketing versions differ"
assert set(android_builds)==set(ios_builds), "Android/iOS build numbers differ"
assert f'gh release create v{android_versions[0]}' in workflow, "Release workflow tag mismatches builds"
print('PASS: native source inventory, 28 audio assets, three-action home, manifest, icons, no web engine/network permission')

# Animation feedback must exist on both game cores and rendering surfaces.
for path in ['android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt',
             'ios/BOPAVI/BopaviCore.swift',
             'android/app/src/main/java/com/brendigo/bopavi/GameView.kt',
             'ios/BOPAVI/GameCanvas.swift']:
    code=(root/path).read_text()
    assert 'collectPulse' in code and 'impactPulse' in code,path

# Premium home: one launch button, themed world cards, compact selector.
android=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
ios=(root/'ios/BOPAVI/GameController.swift').read_text()
assert 'worldTile(row,w)' in android and 'worldTile(world,in:row)' in ios
assert 'for(line in 0..3)' in android and 'for line in 0..<4' in ios
assert 'for(r in 0 until 5)' in android and 'for rowNumber in 0..<5' in ios
assert 'BopaviActionButton' in ios and 'RippleDrawable' in android
for code in [android,ios]:
    assert '▶  IGRAJ' in code
assert 'android.permission.INTERNET' not in manifest

# v0.1.19: three genuine animated environment depths on both native surfaces.
# No static screen replacement; effects are sourced from flight distance and
# frozen under reduced-motion while collision and score core stays unchanged.
android_render=(root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
ios_render=(root/'ios/BOPAVI/GameCanvas.swift').read_text()
android_levels=(root/'android/app/src/main/java/com/brendigo/bopavi/LevelEngine.kt').read_text()
ios_core=(root/'ios/BOPAVI/BopaviCore.swift').read_text()
assert 'private fun drawParallaxIslands(c:Canvas)' in android_render
assert 'private func drawParallaxIslands(_ c:CGContext)' in ios_render
assert 'drawParallaxIslands(canvas)' in android_render
assert 'drawParallaxIslands(c)' in ios_render
assert 'ParallaxScenery.offset(game.distance,layer,reducedMotion)' in android_render
assert 'ParallaxScenery.offset(game.distance,layer:layer,' in ios_render
assert 'internal object ParallaxScenery' in android_levels
assert 'enum ParallaxScenery' in ios_core
for src in [android_levels,ios_core]:
    for rate in ('0.07','0.15','0.24'):
        assert rate in src or rate.replace('0.','.') in src,(rate,src[:120])
assert '0x47000000 or (glowHues[world] and 0x00ffffff)' in android_render
assert 'glowHues[world] and 0x00ffffff)' in android_render
assert 'endRadius:185' in ios_render
# No world map locks (level progression can still unlock individual levels).
for source in (root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt',
               root/'ios/BOPAVI/GameController.swift'):
    menu=source.read_text()
    worlds=menu.split('private fun showWorlds()',1)[1].split('private fun showLevels(',1)[0] if source.suffix=='.kt' else menu.split('private func showWorlds()',1)[1].split('private func showLevels(',1)[0]
    assert '🔒' not in worlds and 'worldTile' in worlds

# v0.1.20: the real touch impulse drives the visual wingbeat on both OSes.
# Motion-sensitive users never receive a gust/wing animation.
for sim_path in ('android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt',
                 'ios/BOPAVI/BopaviCore.swift'):
    core=(root/sim_path).read_text()
    assert 'flapPulse' in core and '0.24' in core or '.24f' in core
    assert 'flapPulse' in core.split('fun flap()',1)[1] if sim_path.endswith('.kt') else 'flapPulse' in core.split('func flap()',1)[1]
for renderer_path in ('android/app/src/main/java/com/brendigo/bopavi/GameView.kt',
                      'ios/BOPAVI/GameCanvas.swift'):
    view=(root/renderer_path).read_text()
    assert 'drawFlapWake(' in view and view.count('drawFlapWake(')==2
    assert 'game.flapPulse' in view
    assert 'reducedMotion' in view.split('drawFlapWake(',1)[1]
    assert 'flapStrength' in view
assert 'sin(game.time*19f)*23f+flapStrength*17f' in android_render
assert 'flapStrength*17' in ios_render

# v0.1.21: names and local progress readable *outside* the world art.
android_menu=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
ios_menu=(root/'ios/BOPAVI/GameController.swift').read_text()
android_tile=android_menu.split('private fun worldTile(',1)[1].split('private fun showHome()',1)[0]
ios_tile=ios_menu.split('private func worldTile(',1)[1].split('private func showHome()',1)[0]
for card in (android_tile,ios_tile):
    assert 'chosenWorld()' in card
    assert 'streamFrontier(world)' in card
    assert 'collectibles[world]' in card
    assert 'otključano' in card
assert 'row.addView(preview' in android_tile
assert 'row.addView(TextView(this)' in android_tile
assert 'preview.heightAnchor.constraint(equalToConstant:137)' in ios_tile
assert 'headline.topAnchor.constraint(equalTo:tile.topAnchor,constant:150)' in ios_tile
assert 'detail.topAnchor.constraint(equalTo:headline.bottomAnchor' in ios_tile
assert 'tile.accessibilityLabel' in ios_tile
for source,signature in ((android_menu,'private fun showResult('),(ios_menu,'private func showResult(')):
    result=source.split(signature,1)[1]
    assert 'ResultHeadline.label(' in result
    assert result.index('ResultHeadline.label(')<result.index('progress.recordRun(g)')
    assert 'Najbolji rezultat:' in result
for core in (android_levels,ios_core):
    assert 'ResultHeadline' in core
    for text in ('NOVI REKORD!','LEVEL DOVRŠEN!','LET ZAVRŠEN!'):
        assert text in core

# v0.1.22: per-level type badges must come from each generated level.
android_levels_menu=android_menu.split('private fun showLevels(',1)[1].split('private fun startGame(',1)[0]
ios_levels_menu=ios_menu.split('private func showLevels(',1)[1].split('private func startGame(',1)[0]
assert 'internal object LevelKind' in android_levels and 'fun name(type:Int)' in android_levels
assert 'enum LevelKind' in ios_core and 'static func name(_ type:Int)' in ios_core
assert 'LevelEngine.create(world,n).type' in android_levels_menu
assert 'BopaviCore.create(world,n).type' in ios_levels_menu
assert 'LevelKind.icon(kind)' in android_levels_menu and 'LevelKind.icon(kind)' in ios_levels_menu
assert 'LevelKind.name(kind)' in android_levels_menu and 'LevelKind.name(kind)' in ios_levels_menu
assert 'ZONA $zone' in android_levels_menu and 'ZONA \\(zone)' in ios_levels_menu
assert 'NORMALNI · ⚡ IZAZOVNI' in android_levels_menu and 'NORMALNI · ⚡ IZAZOVNI' in ios_levels_menu
assert 'contentDescription=' in android_levels_menu and 'accessibilityLabel=' in ios_levels_menu
assert 'Pauza · Level' in android_menu and 'Pauza · Level' in ios_menu
assert 'ODABERI NASTAVI LET' in android_render and 'ODABERI NASTAVI LET' in ios_render
assert 'DODIRNI Ⅱ ZA NASTAVAK' not in android_render and 'DODIRNI Ⅱ ZA NASTAVAK' not in ios_render

# Regression from supplied phone capture: hitbox spans the visibly extended pillar caps.
android=(root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
ios=(root/'ios/BOPAVI/BopaviCore.swift').read_text()
assert 'x - 7f < birdX + radius' in android
assert 'x-7 < birdX+radius' in ios
assert 'val radius = 23f' in android and 'let radius: Float = 23' in ios
# Hero artwork must be an illustration, not a tiny screenshot/CTA embedded in a screenshot.
hero=(root/'docs/assets/hero.svg').read_text()
assert 'POLETI U AVANTURU!' not in hero and 'IGRAJ!' not in hero
assert 'LaunchArt' in (root/'ios/BOPAVI/Info.plist').read_text()
assert (root/'android/app/src/main/res/values-v31/styles.xml').exists()
assert (root/'android/app/src/main/res/drawable/bopavi_startup.xml').exists()

# Source and rendering integration for the illustrated game character.
assert 'R.drawable.bopi0' in (root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
assert 'UIImage(named:"Bopi' in (root/'ios/BOPAVI/GameCanvas.swift').read_text()
assert 'wing_shapes' in (root/'tools/generate_images.py').read_text()
assert 'original_wings' in (root/'tools/generate_images.py').read_text()
assert 'R.drawable.bopileft0' in (root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
assert 'UIImage(named:"BopiLeft' in (root/'ios/BOPAVI/GameCanvas.swift').read_text()
 
# Both cores preserve short valid frame stalls; iOS uses native refresh.
assert 'min(delta, 0.10f)' in (root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
assert 'min(delta, 0.10)' in (root/'ios/BOPAVI/BopaviCore.swift').read_text()
assert 'window?.screen.maximumFramesPerSecond' in (root/'ios/BOPAVI/GameCanvas.swift').read_text()
assert 'preferredFrameRateRange' in (root/'ios/BOPAVI/GameCanvas.swift').read_text()
assert plistlib.load(open(root/'ios/BOPAVI/Info.plist','rb'))['CADisableMinimumFrameDurationOnPhone'] is True

# The emulator may show a system Pixel Launcher ANR after boot; do not
# mistake it for a game bug, and never waive the real settings navigation.
qa_script=(root/'tools/qa_android_emulator.sh').read_text()
assert "recover_launcher_anr()" in qa_script
assert "Pixel Launcher isn't responding" in qa_script
assert "settings_ready=1" in qa_script
assert 'if [ "$settings_ready" -ne 1 ]; then' in qa_script
assert 'adb shell pidof com.brendigo.bopavi' in qa_script
assert 'capture android-settings-diagnostic' in qa_script
assert 'home_ready=1' in qa_script
assert 'if [ "$home_ready" -ne 1 ]; then' in qa_script
assert 'capture android-home-return-diagnostic' in qa_script
assert 'target: google_apis' in (root/'.github/workflows/native-ci.yml').read_text()
import subprocess
subprocess.run(['sh', '-n', str(root/'tools/qa_android_emulator.sh')],check=True)

# Frame-pacing regression from the supplied Android device recording.
renderer=(root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
assert 'postInvalidateDelayed(5); return' not in renderer
assert 'val elapsed=(now-lastFrame).coerceAtLeast(0L)' in renderer and 'postInvalidateOnAnimation()' in renderer
assert 'frameInterval*3/4' not in renderer
assert 'worldBitmap' in renderer and 'R.drawable.world0' in renderer
assert 'UIImage(named:"World' in (root/'ios/BOPAVI/GameCanvas.swift').read_text()
worlds=(root/'tools/generate_worlds.py').read_text()
assert 'THEMES =' in worlds and 'range(8)' in worlds and 'generate_worlds' in worlds
android=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
ios=(root/'ios/BOPAVI/GameController.swift').read_text()
assert 'R.drawable.splash' in android and 'CENTER_CROP' in android
assert 'UIImage(named:"LaunchArt")' in ios and 'scaleAspectFill' in ios
for source in (android,ios):
    assert '🔒' in source and '▶  IGRAJ' in source
ci=(root/'.github/workflows/native-ci.yml').read_text()
assert 'android-smoke:' in ci and 'xcrun simctl launch' in ci

# Regression from the first real iOS simulator screenshot: no giant zoomed Bopi
# and no second tagline above the portrait illustration.
for source in (android,ios):
    home=source.split('private fun showHome()',1)[1].split('private fun showWorlds()',1)[0] if 'private fun showHome()' in source else source.split('private func showHome()',1)[1].split('private func showWorlds()',1)[0]
    assert 'MALI LET, VELIKA AVANTURA' not in home

# No pause or distance reset at level boundaries.
andr=(root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
swift=(root/'ios/BOPAVI/BopaviCore.swift').read_text()
assert 'level.gates.last().x-level.gates[level.gates.lastIndex-1].x' in andr
assert 'level.gates[last].x-level.gates[last-1].x' in swift
assert 'nextOrigin=levelOrigin+level.gates.last().x+spacing-upcoming.gates.first().x' in andr
assert 'nextOrigin=levelOrigin+level.gates[last].x+spacing-next.gates[0].x' in swift
assert 'levelOrigin=nextOrigin' in andr and 'levelOrigin=nextOrigin' in swift
assert 'levelTransition=0f' in andr and 'levelTransition=0' in swift
assert 'distance = 0f; passed = 0' not in andr
assert 'distance=0;passed=0' not in swift

# v0.1.25: the generator must not silently replace the real zone interval
# with a magic constant; actual Kotlin/Swift parity tests exercise the spacing.
assert 'if(level.gates.size>=2)' in andr
assert 'let spacing:Float=last>0' in swift
# Keep fixed body raster dimensions; only separated wings may rotate.
assert 'c.scale(1f,squash)' not in (root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
assert 'c.scaleBy(x:1,y:1+phase*0.035)' not in (root/'ios/BOPAVI/GameCanvas.swift').read_text()

# Premium art and illustrated results must ship on Android/iOS.
assert '#ff871b' in (root/'docs/assets/logo.svg').read_text()
assert 'id="wood"' in (root/'docs/assets/logo.svg').read_text()
assert 'id="lens"' in (root/'docs/assets/hero.svg').read_text()
assert 'def island(' in (root/'tools/generate_images.py').read_text()
# Dynamic result headings live in the tested core, not hardcoded in the UI.
for core in (android_levels,ios_core):
    assert 'LET ZAVRŠEN!' in core
for controller in (android_menu,ios_menu):
    assert 'ResultHeadline.label(' in controller
assert 'fun courses(' in (root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
assert 'func courses(' in (root/'ios/BOPAVI/GameCanvas.swift').read_text()

# First play must not show Android's immersive tutorial over gameplay.
assert 'View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION' not in (root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text().replace('// triggers Android\'s full-screen onboarding popup','')
assert "uiautomator dump" in (root/'tools/qa_android_emulator.sh').read_text()

smoke=(root/'tools/qa_android_emulator.sh').read_text()
assert 'tap_play' in smoke and 'android-current-ui.xml' in smoke
assert 'Izbornik tijekom igre' in smoke
assert 'height*88/100' not in smoke
assert 'if: always()' in ci and 'qa/screenshots/*.xml' in ci


# iOS home wallet must have exactly one fixed height constraint.
ios_home=(root/'ios/BOPAVI/GameController.swift').read_text().split('private func showHome()',1)[1].split('private func showWorlds()',1)[0]
assert ios_home.count('wallet.heightAnchor.constraint(equalToConstant:') == 1

# All eight biomes are available immediately; a saved frontier is per-world.
android_save=(root/'android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt').read_text()
ios_save=(root/'ios/BOPAVI/ProgressStore.swift').read_text()
assert 'fun maxWorld(): Int = 7' in android_save
assert 'func maxWorld() -> Int { 7' in ios_save
for source in (android_save,ios_save):
    assert 'player_name' in source
    assert 'playerName' in source
for source in (android,ios):
    assert 'SPREMI IME' in source
    assert 'Stvorio Brendigo' in source

# v0.1.9 gameplay/difficulty and offline leaderboard parity + dead code audit.
android_sim=(root/"android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt").read_text()
ios_sim=(root/"ios/BOPAVI/BopaviCore.swift").read_text()
for sim in (android_sim,ios_sim):
    assert "gravityFactor" in sim and "speedFactor" in sim
    assert "rating()" not in sim, "Dead rating method was reintroduced"
for store in (android_save,ios_save):
    assert "local_leaderboard" in store and "leaderboard" in store
    assert "difficulty" in store
    assert "if(world==maxWorld()" not in store and "if world==maxWorld()" not in store
for ui in (android,ios):
    assert "LOKALNA LJESTVICA" in ui and "POSTIGNUĆA" in ui
    assert "Težina se primjenjuje na sljedeći let" in ui
assert "if(accessible)" not in android and "if accessible" not in ios

engine=(root/"android/app/src/main/java/com/brendigo/bopavi/LevelEngine.kt").read_text()
assert "private val offset" not in engine
assert "fun accessible(" not in engine

# v0.1.10: audio controls must be accessible without abandoning a live game.
android_menu=(root/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
ios_menu=(root/"ios/BOPAVI/GameController.swift").read_text()
android_canvas=(root/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
ios_canvas=(root/"ios/BOPAVI/GameCanvas.swift").read_text()
for source in (android_menu,ios_menu):
    assert "Isključi zvuk" in source and "Uključi zvuk" in source
    assert "progress.soundEnabled" in source
    assert "Težina:" in source
for canvas in (android_canvas,ios_canvas):
    assert "ODABERI NASTAVI LET" in canvas
    assert "PAUZA" in canvas
android_sim=(root/"android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt").read_text()
ios_sim=(root/"ios/BOPAVI/BopaviCore.swift").read_text()
assert "100_000_000L" in android_sim and "100_000_000" in ios_sim

# Hosted Android emulator occasionally shows a Pixel Launcher ANR (not an app ANR).
# Permit ONLY the exact known system dialog and keep real gameplay ANRs fatal.
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert 'Pixel Launcher isn\'t responding' in qa
assert 'android:id/aerr_wait' in qa
assert 'titles==["Pixel Launcher isn\'t responding"]' in qa
assert 'BOPAVI exited' in qa
assert 'capture android-home' in qa and 'home_captured' in qa

# Accessibility state must reflect an acknowledged flight on both platforms.
for surface in (android_canvas,ios_canvas):
    assert "Bopi leti" in surface
assert "flight_started=0" in qa and "first flight gesture acknowledged" in qa
assert "did not acknowledge the first flight gesture" in qa
assert "Pixel Launcher isn't responding" in qa

# v0.1.11: pickups can vibrate independently of audio, with backup parity.
for store in (android_save,ios_save):
    assert "haptic_enabled" in store
    assert '"hapticEnabled"' in store
for ui in (android_menu,ios_menu):
    assert "Vibracije pri igranju" in ui
assert "if(hapticEnabled)performHapticFeedback" in android_canvas
assert "if self?.progress.hapticEnabled == true" in ios_menu
assert "drawBoostHUD(c)" in ios_canvas
for surface in (android_canvas,ios_canvas):
    assert "ŠTIT ×" in surface and "MAGNET " in surface
workflow=(root/".github/workflows/native-ci.yml").read_text()
assert "tests/SwiftSaves.swift" in workflow
assert "Verify legacy save migration and haptics round-trip" in workflow

# v0.1.12: static illustration is complemented by real bounded runtime scenery.
# Both renderers apply equal cardinality, scrolling factors and reduced-motion guard.
android_world=android_canvas.split("private fun drawWorldAtmosphere(",1)[1].split("private fun drawBackground(",1)[0]
ios_world=ios_canvas.split("private func drawWorldAtmosphere(",1)[1].split("private func background(",1)[0]
assert "drawWorldAtmosphere(canvas)" in android_canvas
assert "drawWorldAtmosphere(c)" in ios_canvas
assert "atmosphereHues" in android_canvas and "foregroundHues" in android_canvas
assert "atmosphereHues" in ios_canvas and "foregroundHues" in ios_canvas
for source in (android_world,ios_world):
    assert "reducedMotion" in source and "distance" in source
    assert "12" in source and "9" in source
    assert "113" in source and "85" in source
    assert ".11" in source or "0.11" in source
    assert ".31" in source or "0.31" in source
    assert "time" in source
    assert "BitmapFactory" not in source and "UIImage(" not in source
for source in (android_canvas,ios_canvas):
    assert "game.active" in source and "game.finished" in source
    assert "107" in source and "27" in source
    assert "quadTo" in source or "addQuadCurve" in source
    assert "reducedMotion" in source

# Endlessly growing distance must not place iOS motes outside their wrap range.
# Swift truncatingRemainder requires the second positive modulo like Kotlin.
assert ".truncatingRemainder(dividingBy:560)+560)" in ios_world
assert ".truncatingRemainder(dividingBy:680)+680)" in ios_world
for distance in (0, 500, 50_000, 2_000_000):
    for i in range(12):
        x=((i*113+29-distance*0.11)%560+560)%560-45
        assert -45 <= x < 515
    for i in range(9):
        x=((i*85+32-distance*0.31)%680+680)%680-60
        assert -60 <= x < 620

# v0.1.13: pickups and shield impacts have matched deterministic visual feedback.
# The original calm pulse rings remain for reduced-motion users.
for source,signature in ((android_canvas,"private fun drawFeedbackSparkles("),
                         (ios_canvas,"private func drawFeedbackSparkles(")):
    assert source.count("drawFeedbackSparkles(") == 2
    assert signature in source
    effect=source.split(signature,1)[1].split("private ",1)[0]
    assert "reducedMotion" in effect
    assert "collectPulse" in effect and "impactPulse" in effect
    assert "pickupHues" in effect and "game.level.world" in effect
    assert "10" in effect and "12" in effect
    assert "6.2831853" in effect
    assert "BitmapFactory" not in effect and "UIImage(" not in effect
    assert "24" in effect and "42" in effect and "33" in effect and "48" in effect
assert "p.alpha=255" in android_canvas.split("private fun drawFeedbackSparkles(",1)[1].split("private fun drawBird(",1)[0]
assert "c.setLineCap(.butt)" in ios_canvas.split("private func drawFeedbackSparkles(",1)[1].split("private func bird(",1)[0]
# Opacity and radius must stay finite/non-negative throughout the event lifetime.
for duration,base,travel in ((0.36,24,42),(0.65,33,48)):
    for step in range(101):
        remaining=duration*step/100
        progress=max(0,min(1,1-remaining/duration))
        assert base<=base+travel*progress<=base+travel
        assert 0 <= (1-progress) <= 1

# v0.1.14: moving obstacles expose cosmetic lip accents without changing colliders.
android_gate=android_canvas.split("private fun drawMovingGateRimCues(",1)[1].split("private fun drawGate(",1)[0]
ios_gate=ios_canvas.split("private func drawMovingGateRimCues(",1)[1].split("private func gate(",1)[0]
for source in (android_gate,ios_gate):
    assert "movement<=0" in source
    assert "reducedMotion" in source
    assert "phase" in source and "2.3" in source
    assert "125" in source and "70" in source and "195" in android_gate
    assert "top-22" in source and "top-17" in android_gate
    assert "bottom+15" in source and "bottom+20" in android_gate
    assert "opening(" not in source and "collision" not in source.split("\n",1)[-1]
    assert "BitmapFactory" not in source and "UIImage(" not in source
assert "drawMovingGateRimCues(c,g,x,top,bottom)" in android_canvas
assert "drawMovingGateRimCues(c,g,x,top,bottom)" in ios_canvas
# World accent hues now match exactly between Android and iOS for gate cues and pickups.
import re
android_colors=re.search(r'private val pickupHues=intArrayOf\(([^\n]*)\)',android_canvas).group(1)
ios_colors=re.search(r'private let pickupHues:\[UInt32\]=\[([^]]*)\]',ios_canvas).group(1)
android_hues=[int(x.strip().split(".")[0],16)&0xffffff for x in android_colors.split(",")]
ios_hues=[int(x.strip(),16) for x in ios_colors.split(",")]
assert len(android_hues)==len(ios_hues)==8 and android_hues==ios_hues
# 64px generated pillars always contain both 13px cap hints, at any animation phase.
for width in (64,70,80):
    for step in range(101):
        offset=step*0.09
        strips=((8+offset,21+offset),(width-23-offset,width-10-offset))
        assert all(0 <= start < end <= width for start,end in strips)

# v0.1.15: blend static illustrations and live renderers with the same cached
# eight-world radial illumination. Static light remains calm in reduced-motion mode.
import re as _light_re
def _world_lighting_values(source, marker, opener, closer, parser):
    area=source.split(marker,1)[1].split(closer,1)[0]
    matched=_light_re.search(opener,area)
    assert matched is not None, marker
    return [parser(token.strip()) for token in matched.group(1).split(",")]
for array_name in ("glowHues","glowX","glowY"):
    a=_world_lighting_values(
        android_canvas, "private val "+array_name+"=", r'\(([^)]*)\)',
        "\n",lambda token:int(token,16) if array_name=="glowHues" else float(token[:-1]))
    b=_world_lighting_values(
        ios_canvas, "private let "+array_name+":", r'=\[([^]]*)\]',
        "\n",lambda token:float(token) if array_name!="glowHues" else int(token,16))
    assert len(a)==len(b)==8 and a==b, (array_name,a,b)
for source in (android_canvas,ios_canvas):
    assert source.count("drawWorldLighting(")==2
    assert "185" in source
    assert "drawWorldAtmosphere" in source
# Check only the static glow method, not the new independent parallax method.
android_light=android_canvas.split("private fun drawWorldLighting(",1)[1].split("private fun drawParallaxIslands(",1)[0]
ios_light=ios_canvas.split("private func drawWorldLighting(",1)[1].split("private func drawParallaxIslands(",1)[0]
assert "glowShaders[world]" in android_light and "p.shader=null" in android_light
assert "RadialGradient(" in android_canvas and "private val glowShaders=Array(8)" in android_canvas
assert "CGGradient(" in ios_canvas and "private lazy var glowGradients" in ios_canvas
assert "drawRadialGradient(" in ios_light
for light in (android_light,ios_light):
    assert "BitmapFactory" not in light and "UIImage(" not in light
    assert "new " not in light
# Ensure static glow is not linked to animation time or camera distance.
for light in (android_light,ios_light):
    assert "game.time" not in light and "game.distance" not in light

# v0.1.16: both renderers expose real per-level gate progress without a pause.
android_progress=android_canvas.split("private fun drawLevelProgress(",1)[1].split("private fun drawHud(",1)[0]
ios_progress=ios_canvas.split("private func drawLevelProgress(",1)[1].split("private func drawBoostHUD(",1)[0]
for source in (android_canvas,ios_canvas):
    assert source.count("drawLevelProgress(")==2
for source in (android_progress,ios_progress):
    assert "game.totalPassed" in source
    assert "lastProgressPassed" in source
    assert "PROLAZI UKUPNO" in source
    assert "Bopi leti. Prolazi ukupno" in source
    assert "160" in source
    assert "UIImage(" not in source and "BitmapFactory" not in source
    assert "game.passed" not in source
assert "260f*passed.toFloat()/total" not in android_progress
assert "260*CGFloat(passed)/CGFloat(total)" not in ios_progress
# No old-world gate progress resetting bar or level-flash overlay.
assert "if(game.levelTransition>0f)" not in android_render
assert "if game.levelTransition>0" not in ios_render
assert "upcomingGates().withIndex()" in android_render
assert "upcomingGates.enumerated()" in ios_render
assert "upcomingGateX" in android_render and "upcomingGateX" in ios_render

# v0.1.24: pre-flight choice and original editable Portantin art on both OSes.
port_svg=(root/'docs/assets/portantin.svg').read_text()
for element in ('wing-left','wing-right','Kirur','mask','maskicom','rukavic','Portantin'):
    assert element.lower() in port_svg.lower(),element
artgen=(root/'tools/generate_images.py').read_text()
for resource in ('bopi6','bopileft6','bopiright6','Bopi6','BopiLeft6','BopiRight6'):
    assert resource in artgen
assert "R.drawable.bopi6" in android_render and 'UIImage(named:"Bopi' in ios_render
android_picker=android_menu.split('private fun showPilotPicker(',1)[1].split('private fun startGame(',1)[0]
ios_picker=ios_menu.split('private func showPilotPicker(',1)[1].split('private func startGame(',1)[0]
for view in (android_picker,ios_picker):
    assert 'POLETI S' in view and 'ODABERI LIKA' in view
    assert 'Portantin' in view and 'characterGallery(' in view
    assert 'startGame(' in view
android_gallery=android_menu.split('private fun characterGallery(',1)[1].split('private fun showPilotPicker(',1)[0]
ios_gallery=ios_menu.split('private func characterGallery(',1)[1].split('private func showPilotPicker(',1)[0]
assert 'skinNames.indices step 2' in android_gallery
assert 'stride(from:0,to:progress.skinNames.count,by:2)' in ios_gallery
assert 'R.drawable.bopi6' in android_gallery
assert 'UIImage(named:"Bopi\\(i)")' in ios_gallery
assert 'selectOrBuy(i)' in android_gallery and 'selectOrBuy(i)' in ios_gallery
assert 'Otključati' in android_gallery and 'Otključati' in ios_gallery
assert 'Potrošit ćeš' in android_gallery and 'Potrošit ćeš' in ios_gallery
assert 'contentDescription=' in android_gallery and 'accessibilityLabel=' in ios_gallery
android_skins=android_menu.split('private fun showSkins()',1)[1].split('private fun showLeaderboard()',1)[0]
ios_skins=ios_menu.split('private func showSkins()',1)[1].split('private func showLeaderboard()',1)[0]
assert 'characterGallery(' in android_skins and 'characterGallery(' in ios_skins
assert 'showPilotPicker(world,number)' in android_picker and 'showPilotPicker(world,number)' in ios_picker
for source in (android_menu,ios_menu):
    home=source.split('private fun showHome()',1)[1].split('private fun showWorlds()',1)[0] if 'private fun showHome()' in source else source.split('private func showHome()',1)[1].split('private func showWorlds()',1)[0]
    assert 'showPilotPicker(' in home and 'startGame(' not in home
qa=(root/'tools/qa_android_emulator.sh').read_text()
assert 'tap_selected_pilot' in qa and 'android-pilot-picker' in qa
for path in ('android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt','ios/BOPAVI/ProgressStore.swift'):
    store=(root/path).read_text()
    assert 'portantin' in store and 'Portantin' in store
    assert 'skin_index' in store

# v0.1.23: never show gameplay controls on home or before the first flap.
# The visible pause is created per-session and revealed only by an actual
# inactive->active transition, not by selecting the IGRAJ menu action.
android_start=android_menu.split('private fun startGame(',1)[1].split('private fun showResult(',1)[0]
ios_start=ios_menu.split('private func startGame(',1)[1].split('private func showToast(',1)[0]
android_touch=android_render.split('override fun onTouchEvent(',1)[1].split('override fun performClick(',1)[0]
ios_touch=ios_render.split('override func touchesBegan(',1)[1].split('private func color(',1)[0]
assert 'val firstFlap=!game.active' in android_touch
assert 'if(firstFlap && game.active)onFlightStarted()' in android_touch
assert 'let firstFlap = !game.active' in ios_touch
assert 'if firstFlap && game.active {onFlightStarted?()}' in ios_touch
assert 'visibility=View.INVISIBLE' in android_start
assert 'pauseButton?.visibility=View.VISIBLE' in android_start
assert 'pause.isHidden=true' in ios_start
assert 'pause?.isHidden=false' in ios_start
assert 'counter.isHidden=true' in ios_start and 'counter?.isHidden=false' in ios_start
assert 'selectedScreen=="game" && gameView?.game === it' in android_start
assert 'self.canvas === gameCanvas' in ios_start
assert 'if(game.active && !game.finished)' in android_render
assert 'if game.active && !game.finished' in ios_render
for source in (android_menu.split('private fun showHome()',1)[1].split('private fun showWorlds()',1)[0],
               ios_menu.split('private func showHome()',1)[1].split('private func showWorlds()',1)[0]):
    assert 'Izbornik tijekom igre' not in source and 'Ⅱ' not in source
qa_lifecycle=(root/'tools/qa_android_emulator.sh').read_text()
assert 'verify_idle_preview' in qa_lifecycle
assert 'FAIL: pause button visible before first flap' in qa_lifecycle
assert 'FAIL: gameplay pause control leaked onto result screen' in qa_lifecycle
assert 'FAIL: pause did not appear after actual first flight flap' in qa_lifecycle

# v0.1.17: illustrated home shows exactly three deliberate actions, on both
# platforms, with IGRAJ first and with cosmetics/equipment under settings.
android_home=android_menu.split("private fun showHome()",1)[1].split("private fun showWorlds()",1)[0]
ios_home=ios_menu.split("private func showHome()",1)[1].split("private func showWorlds()",1)[0]
for home in (android_home,ios_home):
    assert home.count('▶  IGRAJ') == 1
    assert home.count('🌍  SVJETOVI') == 1
    assert home.count('⚙  POSTAVKE') == 1
    assert home.index('▶  IGRAJ') < home.index('🌍  SVJETOVI') < home.index('⚙  POSTAVKE')
    assert 'showWorlds()' in home and 'showSettings()' in home
    assert 'showPerks()' not in home and 'showSkins()' not in home
android_settings=android_menu.split("private fun showSettings()",1)[1].split("override fun onActivityResult",1)[0]
ios_settings=ios_menu.split("private func showSettings()",1)[1].split("func documentPicker(",1)[0]
for settings in (android_settings,ios_settings):
    for title in ("IZGLED I ZVUK","IGRAČ I TEŽINA","DODATNE OPCIJE","PODACI I PRIVATNOST",
                  "LIKOVI","TRGOVINA KOVANICAMA","LOKALNA LJESTVICA"):
        assert title in settings
    assert 'showSkins()' in settings and 'showPerks()' in settings
    assert 'showLeaderboard()' in settings and 'showHome()' in settings
    assert 'SPREMI IME' in settings and 'Vibracije pri igranju' in settings
    assert 'SPREMI KOPIJU NAPRETKA' in settings and 'VRATI NAPREDAK IZ KOPIJE' in settings

# Shield impact must generate exactly one callback per nonzero pulse,
# and a backgrounded iOS flight must not continue moving without confirmation.
android_feedback=android_canvas.split("if(game.impactPulse>0f && !shieldImpactNotified)",1)[1].split("} else lastFrame=0L",1)[0]
ios_feedback=ios_canvas.split("if game.impactPulse>0 && !shieldImpactNotified",1)[1].split("setNeedsDisplay()",1)[0]
for feedback in (android_feedback,ios_feedback):
    assert 'shieldImpactNotified=true' in feedback
    assert 'shieldImpactNotified=false' in feedback
    assert 'onShieldImpact' in feedback
assert 'HapticFeedbackConstants.LONG_PRESS' in android_feedback
assert 'hapticEnabled' in android_feedback
assert 'onShieldImpact={sound.effect("hit")}' in android_menu
assert 'UIImpactFeedbackGenerator(style:.medium)' in ios_menu
assert 'if self.progress.hapticEnabled' in ios_menu
assert 'UIApplication.willResignActiveNotification' in ios_menu
assert 'activeCanvas.paused=true' in ios_menu
assert 'sound.pause()' in ios_menu.split('private func pauseForInterruption(',1)[1].split('private func clear(',1)[0]

# v0.1.17: user-friendly premium navigation, genuine earned currency and scores.
for home in (android_home,ios_home):
    assert 'SVJETOVI' in home and 'POSTAVKE' in home
    assert '🏆' in home and 'coins()' in home
    assert 'bestPoints()' in home
    assert 'showPerks()' not in home and 'showSkins()' not in home
for ui in (android_menu,ios_menu):
    for label in ('TRGOVINA KOVANICAMA','SPREMI KOPIJU NAPRETKA',
                  'VRATI NAPREDAK IZ KOPIJE','Nježnije animacije',
                  'Tvoje ime','Bez oglasa i kupnje stvarnim novcem',
                  'KUPI ','KOVANICA','BODOVA','Stvorio Brendigo'):
        assert label in ui, label
    for deprecated in ('Smanji animacije (30 FPS)','(v0.1–v0.5)',
                       'Razvoj: Brendigo','telemetrije'):
        assert deprecated not in ui, deprecated
    assert 'showPerks()' in ui and 'showSkins()' in ui
android_shop=android_menu.split('private fun showPerks()',1)[1].split('private fun showSkins()',1)[0]
ios_shop=ios_menu.split('private func showPerks()',1)[1].split('private func showSkins()',1)[0]
for shop in (android_shop,ios_shop):
    assert 'Za kovanice osvojene igrom — bez stvarnog novca' in shop
    assert 'KUPI ' in shop and 'ŠTIT' in shop and 'MAGNET' in shop
    assert 'buyPerk(' in shop and 'showSkins()' in shop
    assert 'progress.coins()' in shop and 'showSettings()' in shop
assert 'cornerRadius=d(31)' in android_menu
assert 'gradient(0xff257ce0.toInt(),0xff123f9a.toInt(),29)' in android_menu
assert 'layer.cornerRadius=29' in ios_menu
assert 'gradient.cornerRadius=29' in ios_menu
assert 'SPREMI KOPIJU NAPRETKA' in android_settings and 'SPREMI KOPIJU NAPRETKA' in ios_settings
# No payment integrations; all transactions use the validated in-game wallet.
assert 'buyPerk(n)' in android_shop and 'buyPerk(n)' in ios_shop
assert 'StoreKit' not in ios_menu and 'BillingClient' not in android_menu

# Offline score-to-coin exchange: one reward per new 1000-point record threshold.
for store in (android_save,ios_save):
    for marker in ("bestPoints","bonusCoinsAvailable","claimBonusCoins",
                   "score_coins_claimed","scoreCoinsClaimed"):
        assert marker in store, marker
    assert "1000" in store or "1_000" in store
    assert '"coins"' in store and '"local_leaderboard"' in store
assert "claimableRecordCoins" in android_save
assert "claimableRecordCoins(5000,5,0)" in (root/'android/app/src/test/java/com/brendigo/bopavi/LevelEngineTest.kt').read_text()
swift_saves=(root/"tests/SwiftSaves.swift").read_text()
assert "store.claimBonusCoins()==0" in swift_saves
assert "try store.importData(claimedBackup)" in swift_saves
for menu_source in (android_menu,ios_menu):
    assert "Za svakih novih 1.000 bodova" in menu_source
    assert "PREUZMI " in menu_source and "KOVANICA ZA BODOVE" in menu_source
    assert "claimBonusCoins()" in menu_source
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert "tap_settings" in qa and "android-settings" in qa
assert "settings opens and returns to the three-button home" in qa

# v0.1.26: nine unique premium silhouettes, wing layers and offline inventory.
from xml.etree import ElementTree as ET
catalogs=[(root/'android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt').read_text(),
          (root/'ios/BOPAVI/ProgressStore.swift').read_text()]
for inventory in catalogs:
    for text in ('"Noa"','"Any"','"noa"','"any"','220','240','owned_mask','skin_index'):
        assert text in inventory,text
    assert '0..8' in inventory or '0...8' in inventory
for index,character in ((7,'noa'),(8,'any')):
    document=ET.parse(root/'docs/assets'/f'{character}.svg').getroot()
    assert document.attrib['viewBox']=='-65 -65 130 130'
    identifiers=[element.get('id') for element in document.iter()]
    assert identifiers.count('wing-left')==1 and identifiers.count('wing-right')==1
    assert f"'{character}'" in artgen
    for key in (f'R.drawable.bopi{index}',f'R.drawable.bopileft{index}',f'R.drawable.bopiright{index}'):
        assert key in android_render
    assert f'R.drawable.bopi{index}' in android_gallery
assert 'min(8,max(0,skinIndex))' in ios_render
assert '9 characters, 18 detached wings' in artgen
for index in ('7','8'):
    assert f'{index} ->' in android_picker and f'case {index}:' in ios_picker
for gallery in (android_gallery,ios_gallery):
    assert 'PREMIUM' in gallery and 'Potrošit ćeš' in gallery

# v0.1.27: loading the correct three sprite resources is mandatory in gameplay.
# v0.1.26 generated Noa/Any art but the actual renderer selected Portantin.
android_loader=android_render.split('private val selectedSkin=',1)[1].split('private val worldBitmaps=',1)[0]
assert android_loader.startswith('skinIndex.coerceIn(0,8)')
for source in ('birdSprites','leftWings','rightWings'):
    assert f'{source}[selectedSkin]' in android_loader,source
assert 'skinIndex.coerceIn(0,6)' not in android_loader
swift_loader=ios_render.split('init(game:GameSimulation, reducedMotion:Bool,skinIndex:Int)',1)[1].split('super.init(frame:',1)[0]
assert 'let selectedSkin=min(8,max(0,skinIndex))' in swift_loader
for asset in ('Bopi','BopiLeft','BopiRight'):
    assert f'{asset}\\(selectedSkin)' in swift_loader,asset
assert 'min(6,max(0,skinIndex))' not in swift_loader
print('PASS: nine pilot bodies and both matching animated wings on Android/iOS')

# v0.1.28: selected Noa/Any must resolve inside a full nine-image Android picker.
android_preview=android_picker.split('val portraits=intArrayOf(',1)[1].split(')',1)[0]
assert android_preview.count('R.drawable.bopi') == 9, 'Android premium pilot preview missing'
for index in range(9):
    assert f'R.drawable.bopi{index}' in android_preview
assert 'setImageResource(portraits[idx])' in android_picker
# Boosts are spent only on the first flap, not when viewing the idle game.
android_start=android_menu.split('private fun startGame(',1)[1].split('private fun showResult(',1)[0]
ios_start=ios_menu.split('private func startGame(',1)[1].split('private func showToast(',1)[0]
for source in (android_start,ios_start):
    assert 'progress.previewPerks()' in source
    assert 'progress.consumePerks()' in source.split('onFlightStarted',1)[1]
    assert 'val boosts=progress.consumePerks()' not in source
    assert 'let boosts=progress.consumePerks()' not in source
for source in (android_save,ios_save):
    assert 'previewPerks()' in source and 'consumePerks()' in source
assert 'if(gameView?.game?.active == true) gameView?.paused = true' in android_menu
assert 'if activeCanvas.game.active {activeCanvas.paused=true}' in ios_menu
assert 'resumeIdlePreview' in ios_menu
assert 'backgroundPause' not in ios_menu
assert 'store.previewPerks().magnet==8' in swift_saves
print('PASS: nine pilot previews, first-flap boost accounting, interruption-safe idle previews')

# v0.1.29: no iOS save mutations until invalid v5 streams are rejected.
ios_import=ios_save.split('func importData(_ data:Data) throws {',1)[1]
assert ios_import.index('guard let n=Int(strs[w])') < ios_import.index('defaults.set(maxWorld,forKey:"max_world")')
assert 'afterCorrupt==beforeCorrupt' in swift_saves
assert 'badSave["streamFrontiers"]' in swift_saves
# Marathon result must use the global gate count, which never resets between levels.
android_result=android_menu.split('private fun showResult(',1)[1].split('private fun showPerks()',1)[0]
ios_result=ios_menu.split('private func showResult(',1)[1].split('private func showPerks()',1)[0]
assert 'Ukupno prolaza: ${g.totalPassed}' in android_result
assert r'Ukupno prolaza: \(g.totalPassed)' in ios_result
assert 'Prolazi ${g.passed}' not in android_result
assert r'Prolazi \(g.passed)' not in ios_result
# Android hardware Back invokes the normal, already-tested pause dialog.
android_back=android_menu.split('override fun onBackPressed()',1)[1]
assert 'current.active -> gamePauseButton?.performClick()' in android_back
assert 'else -> showPilotPicker(currentWorld,currentLevel)' in android_back
assert 'else if(selectedScreen=="home") {' in android_back
assert 'if (Build.VERSION.SDK_INT >= 33) finish() else super.onBackPressed()' in android_back
assert 'gamePauseButton=pause' in android_start
print('PASS: rejected iOS backup, truthful global result, Android Back-to-pause')

# v0.1.29: Android emulator must recover only the exact Pixel Launcher ANR.
# It must still assert real scrollable pilot cards and fail on BOPAVI/system errors.
android_qa=(root/'tools/qa_android_emulator.sh').read_text()
gallery_qa=android_qa.split('def swipe_gallery(root,up=True):',1)[1].split('root=ET.parse(sys.argv[1])',1)[0]
assert 'for recovery_attempt in range(6):' in gallery_qa
assert 'Pixel Launcher isn\'t responding' in gallery_qa
assert "raise SystemExit(f'FAIL: character gallery remained non-scrollable" in gallery_qa
assert "candidates=[n for n in root.iter('node') if n.get('scrollable')=='true']" in gallery_qa
assert 'required={\'Portantin\',\'Noa\',\'Any\'}' in android_qa
print('PASS: Android launcher-ANR recovery does not bypass actual pilot gallery QA')

# v0.1.30 Google Play readiness: actual API level and signing workflow gates.
assert "compileSdk = 36" in gradle and "targetSdk = 36" in gradle
assert "versionCode = 31" in gradle and 'versionName = "0.1.30"' in gradle
assert 'applicationId = "com.brendigo.bopavi"' in gradle
assert 'android:appCategory="game"' in manifest
assert 'android:enableOnBackInvokedCallback="true"' in manifest
assert "OnBackInvokedDispatcher.PRIORITY_DEFAULT" in android
assert "registerOnBackInvokedCallback" in android and "unregisterOnBackInvokedCallback" in android
assert "private fun navigateBack()" in android and "override fun onBackPressed() = navigateBack()" in android
assert "BOPAVI_UPLOAD_KEYSTORE_PATH" in gradle
assert "System.getenv(\"BOPAVI_UPLOAD_STORE_PASSWORD\")" in gradle
assert "Signing environment" not in gradle  # No checked-in secret values.
play_workflow=(root/'.github/workflows/google-play-upload.yml').read_text()
assert "workflow_dispatch:" in play_workflow and "environment: google-play" in play_workflow
assert "BOPAVI_UPLOAD_KEYSTORE_B64" in play_workflow
assert "BOPAVI_UPLOAD_STORE_PASSWORD" in play_workflow
assert "BOPAVI_UPLOAD_KEY_ALIAS" in play_workflow
assert "BOPAVI_UPLOAD_KEY_PASSWORD" in play_workflow
assert "jarsigner" in (root/'tools/verify_play_release.py').read_text()
assert "BOPAVI-Google-Play-listing-v0.1.30.zip" in workflow
for name in ("GOOGLE-PLAY-PUBLISHING.md","play/STORE-LISTING-hr-HR.md",
             "play/GOOGLE-PLAY-DATA-SAFETY.md","play/privacy-policy.html",
             "play/RELEASE-CHECKLIST.md"):
    assert (root/'docs'/name).is_file(), name
artwork=(root/'tools/create_play_listing_assets.py').read_text()
for source in ("android-home.png","android-pilot-picker.png","android-gameplay-ready.png","android-result.png"):
    assert source in artwork and "qa/screenshots" in artwork
assert "ImageOps.fit" in artwork and "target_width * 16 // 9" in artwork
print("PASS: Play API36, real screenshots, signing isolation and publishing docs")
\n# Edge-to-edge release candidate: game controls and result actions avoid cutouts/system bars.\nassert "WindowInsets.Type.displayCutout()" in android\nassert "val top = maxOf(d(24),cutout.top+d(8))" in android\nassert "scroll.setOnApplyWindowInsetsListener" in android\nprint("PASS: Android16 game controls and result safe-area handling")\n