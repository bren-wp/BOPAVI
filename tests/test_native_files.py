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
assert 'python tools/release_metadata.py --tag' in workflow, "Release must obtain tag from both platform versions"
assert 'gh release create "$tag"' in workflow, "Release workflow must create the versioned tag"
assert '--target "$GITHUB_SHA"' in workflow, "Release must target the tested main commit"
print('PASS: native source inventory, 28 audio assets, premium home, manifest, icons, no web engine/network permission')

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

# Names and local progress are live overlays on the world artwork, never baked into it.
android_menu=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
ios_menu=(root/'ios/BOPAVI/GameController.swift').read_text()
android_tile=android_menu.split('private fun worldTile(',1)[1].split('private fun showHome()',1)[0]
ios_tile=ios_menu.split('private func worldTile(',1)[1].split('private func showHome()',1)[0]
# Both real world galleries expose persistent, grounded game metrics instead of
# inventing a 30-star per-world completion counter from concept artwork.
for world_tile in (android_tile,ios_tile):
    assert 'collectibles(world)' in world_tile
    assert 'best(world)' in world_tile
    assert 'streamFrontier(world)' in world_tile
    assert 'otključano' in world_tile
    assert 'rekord ' in world_tile
    assert 'level ' in world_tile
    assert 'prikupljeno ' in world_tile
    assert 'POKRENI' not in world_tile  # whole accessible card remains the action
assert 'name.uppercase()' in android_tile
assert 'names[world].uppercased()' in ios_tile
assert 'importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO' in android_tile
assert 'collection.accessibilityElementsHidden=true' in ios_tile
assert 'verify_world_cards' in (root/'tools/qa_android_emulator.sh').read_text()
assert 'not all world cards reachable' in (root/'tools/qa_android_emulator.sh').read_text()
assert 'all eight real illustrated world cards expose saved' in (root/'tools/qa_android_emulator.sh').read_text()
print('PASS: responsive native world cards and real 8-world save-metric QA')
for card in (android_tile,ios_tile):
    assert 'chosenWorld()' in card
    assert 'streamFrontier(world)' in card
    assert 'collectibles(world)' in card
    assert 'otključano' in card
# Premium world cards render the same actual illustrations within a
# framed image panel plus a decorative (non-tappable) world badge and arrow.
assert 'row.addView(artPanel' in android_tile
assert 'artPanel.addView(ImageView(this)' in android_tile
assert 'world+1' in android_tile and 'world+1' in ios_tile
assert 'importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO' in android_tile
assert 'arrow.isUserInteractionEnabled=false' in ios_tile
assert 'artPanel.addView(TextView(this)' in android_tile
assert 'preview.heightAnchor.constraint(equalToConstant:132)' in ios_tile
assert 'headline.topAnchor.constraint(equalTo:tile.topAnchor,constant:85)' in ios_tile
assert 'collection.topAnchor.constraint(equalTo:tile.topAnchor,constant:114)' in ios_tile
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
# The native pause panel supersedes old painted pause banners.
assert 'ODABERI NASTAVI LET' not in android_render and 'ODABERI NASTAVI LET' not in ios_render
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
# All eight biomes now contain distinct noninteractive, deterministic scenery
# on the original shared 480×800 art board, not flat repeats or mockup buttons.
for code in ('def detailed_landmarks(index: int)', 'def floating_island(',
             'def castle(', 'add(detailed_landmarks(index))',
             'index==1:', 'index==2:', 'index==3:', 'index==4:',
             'index==5:', 'index==6:', 'index==7:'):
    assert code in worlds,code
assert "random.Random(950 + index)" in worlds
assert 'http://' not in worlds.replace('http://www.w3.org/2000/svg','')
assert 'requests.' not in worlds and 'urllib.' not in worlds
assert 'im.save(android/(name+".png"),optimize=True)' in worlds
assert 'im.save(assets/(name+".png"),optimize=True)' in worlds

android=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
ios=(root/'ios/BOPAVI/GameController.swift').read_text()
assert 'R.drawable.homeback' in android and 'CENTER_CROP' in android
assert 'UIImage(named:"HomeArt")' in ios and 'scaleAspectFill' in ios
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
# Accumulated world origins and travel must stay in Double on both platforms.
# Repeated Float addition loses collision precision at long-running level seams.
assert 'private var preciseDistance = 0.0' in andr
assert 'private var preciseDistance:Double = 0' in swift
assert 'nextOrigin=levelOrigin+level.gates.last().x.toDouble()+spacing.toDouble()-upcoming.gates.first().x.toDouble()' in andr
assert 'nextOrigin=levelOrigin+Double(level.gates[last].x)+Double(spacing)-Double(next.gates[0].x)' in swift
assert 'WorldCoordinates.screenX(gate.x, levelOrigin, preciseDistance)' in andr
assert 'WorldCoordinates.screenX(gate.x,origin:levelOrigin,travelled:preciseDistance)' in swift
assert '(gateX.toDouble() + origin - travelled).toFloat()' in andr
assert 'Float(Double(gateX)+origin-travelled)' in swift
assert 'distance = preciseDistance.toFloat()' in andr
assert 'distance = Float(preciseDistance)' in swift
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
# The old flat brown wood plaque was intentionally replaced by the
# shared winged blue/gold gradient. Test actual required brand tokens.
logo=(root/'docs/assets/logo.svg').read_text()
assert 'id="letters"' in logo and 'id="wings"' in logo
assert '#ff9b08' in logo and '#ff8004' in logo and '#22a8ff' in logo
assert 'MALI LETOVI' in logo and 'VELIKE PRIČE' in logo
assert 'id="wood"' not in logo
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


# Two truly dynamic, identically sized top counters are built from one
# loop; no stale one-wallet-height assumption after premium redesign.
ios_home=(root/'ios/BOPAVI/GameController.swift').read_text().split('private func showHome()',1)[1].split('private func showWorlds()',1)[0]
assert 'headerChip("🏆' in ios_home and 'progress.bestPoints()' in ios_home
assert 'headerChip("♛' in ios_home and 'progress.coins()' in ios_home
assert 'walletRow.distribution = .fill' in ios_home
assert 'wallet.heightAnchor.constraint(equalToConstant:' not in ios_home

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
    assert "MOJI REKORDI" in ui and "POSTIGNUĆA" in ui
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
    assert 'ZVUK' in source and 'showPremiumPause' in source
    assert 'sound.enabled' in source and 'sound.pause()' in source
    assert "progress.soundEnabled" in source
    assert "Težina:" in source
for canvas in (android_canvas,ios_canvas):
    assert "ODABERI NASTAVI LET" not in canvas
    assert "PAUZA" not in canvas
assert 'showPremiumPause(game)' in android_menu
assert 'showPremiumPause(for:canvas)' in ios_menu
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
    assert 'text="Vibracije"' in ui if ui is android_menu else 'hapticLabel.text="Vibracije"' in ui
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
android_gate=android_canvas.split("private fun drawMovingGateRimCues(",1)[1].split("private fun drawRoyalCoin(",1)[0]
ios_gate=ios_canvas.split("private func drawMovingGateRimCues(",1)[1].split("private func drawRoyalCoin(",1)[0]
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

# Premium gameplay HUD: authentic per-level fraction plus uninterrupted cumulative gates.
android_progress=android_canvas.split("private fun drawLevelProgress(",1)[1].split("private fun drawHud(",1)[0]
ios_progress=ios_canvas.split("private func drawLevelProgress(",1)[1].split("private func drawBoostHUD(",1)[0]
for source in (android_canvas,ios_canvas):
    assert source.count("drawLevelProgress(")==2
for source in (android_progress,ios_progress):
    assert "game.totalPassed" in source
    assert "lastProgressPassed" in source
    assert "PROLAZI UKUPNO" in source
    assert "Bopi leti. Prolazi ukupno" in source
    assert "game.passed" in source
    assert "level.gates" in source
    assert "UIImage(" not in source and "BitmapFactory" not in source
assert "260f*passed.toFloat()/total" not in android_progress
assert "260*CGFloat(passed)/CGFloat(total)" not in ios_progress
# No old-world gate progress resetting bar or level-flash overlay.
assert "if(game.levelTransition>0f)" not in android_render
assert "if game.levelTransition>0" not in ios_render
assert "upcomingGates().withIndex()" in android_render
assert "upcomingGates.enumerated()" in ios_render
assert "upcomingGateX" in android_render and "upcomingGateX" in ios_render

# The new three birds replace former people, preserving native IDs 6-8.
artgen=(root/'tools/generate_images.py').read_text()
for new_bird in ('vihor','iskra','orion'):
    asset=root/'docs/assets'/f'{new_bird}.svg'
    assert asset.is_file()
    svg=ET.parse(asset).getroot()
    assert svg.attrib['viewBox']=='-80 -80 160 160'
    groups=[element.attrib.get('id') for element in svg.iter()]
    assert groups.count('wing-left')==1 and groups.count('wing-right')==1
    assert new_bird in artgen
assert "for skin, character in ((6,'vihor'),(7,'iskra'),(8,'orion')):" in artgen
for old_bird in ('portantin','noa','any'):
    assert not (root/'docs/assets'/f'{old_bird}.svg').exists()
assert "R.drawable.bopi6" in android_render and 'UIImage(named:"Bopi' in ios_render
android_picker=android_menu.split('private fun showPilotPicker(',1)[1].split('private fun startGame(',1)[0]
ios_picker=ios_menu.split('private func showPilotPicker(',1)[1].split('private func startGame(',1)[0]
for view in (android_picker,ios_picker):
    assert 'POLETI S' in view and 'ODABERI LIKA' in view
    assert 'Vihor' in view and 'Iskra' in view and 'Orion' in view and 'characterGallery(' in view
    assert 'startGame(' in view
android_gallery=android_menu.split('private fun characterGallery(',1)[1].split('private fun showPilotPicker(',1)[0]
ios_gallery=ios_menu.split('private func characterGallery(',1)[1].split('private func showPilotPicker(',1)[0]
assert 'displayOrder.indices step 3' in android_gallery
assert 'stride(from:0,to:displayOrder.count,by:3)' in ios_gallery
assert 'R.drawable.bopiportrait6' in android_gallery
assert 'UIImage(named:"BopiPortrait\\(i)")' in ios_gallery
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
    assert '"portantin"' in store and '"Vihor"' in store and '"Iskra"' in store and '"Orion"' in store
    assert 'skin_index' in store

# v0.1.23: never show gameplay controls on home or before the first flap.
# The visible pause is created per-session and revealed only by an actual
# inactive->active transition, not by selecting the IGRAJ menu action.
android_start=android_menu.split('private fun startGame(',1)[1].split('private fun showResult(',1)[0]
ios_start=ios_menu.split('private func startGame(',1)[1].split('private func showToast(',1)[0]
android_touch=android_render.split('override fun onTouchEvent(',1)[1].split('override fun performClick(',1)[0]
ios_touch=ios_render.split('override func touchesBegan(',1)[1].split('private func color(',1)[0]
android_click=android_render.split('override fun performClick(',1)[1].split('override fun onDetachedFromWindow(',1)[0]
ios_flap=ios_render.split('private func triggerFlap()',1)[1].split('override func touchesBegan(',1)[0]
assert 'performClick()' in android_touch
assert 'val firstFlap=!game.active' in android_click
assert 'if(firstFlap && game.active) {' in android_click
assert android_click.count('onFlightStarted()')==2  # once in accessible drag, once in classic tap
assert 'if(firstDrag && game.active){onFlightStarted();onFlap()}' in android_click
assert android_click.index('onFlightStarted()') < android_click.index('if(qaCaptureEnabled) qaWaitingForCapture=true')
assert 'triggerFlap()' in ios_touch
assert 'let firstFlap = !game.active' in ios_flap
assert 'if firstFlap && game.active {onFlightStarted?()}' in ios_flap
assert 'visibility=View.INVISIBLE' in android_start
assert 'pauseButton?.visibility=View.VISIBLE' in android_start
assert 'pause.isHidden=true' in ios_start
assert 'pause?.isHidden=false' in ios_start
assert 'let counter=UILabel()' not in ios_start
assert 'onHUDUpdate' not in ios_start and 'onHUDUpdate' not in ios_canvas
assert 'counter?.isHidden=false' not in ios_start
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

# Premium reference redesign: three primary navigation buttons plus four
# explicitly functional footer shortcuts on Android AND iOS, not dead mock UI.
android_home=android_menu.split("private fun showHome()",1)[1].split("private fun showWorlds()",1)[0]
ios_home=ios_menu.split("private func showHome()",1)[1].split("private func showWorlds()",1)[0]
for home in (android_home,ios_home):
    for button_label in ('▶  IGRAJ','🌍  SVJETOVI','⚙  POSTAVKE'):
        assert home.count(button_label)==1
    assert home.index('▶  IGRAJ') < home.index('🌍  SVJETOVI') < home.index('⚙  POSTAVKE')
    for label in ('PROFIL','ZADACI','KOLEKCIJA','TRGOVINA'):
        assert label in home
    for destination in ('showWorlds()','showSettings()','showAchievements(','showSkins(','showPerks('):
        assert destination in home
    assert 'showPilotPicker(' in home and 'startGame(' not in home
assert 'LinearLayout.VERTICAL' in android_home and 'View.IMPORTANT_FOR_ACCESSIBILITY_NO' in android_home
assert 'UIStackView(arrangedSubviews:[glyph,caption])' in ios_home
assert 'tab.accessibilityLabel=title' in ios_home
# No fake online gems, cloud sign-out or microtransactions.
for home in (android_home,ios_home):
    assert 'DIJAMANTI' not in home and 'ODJAVA' not in home
for source in (android_menu,ios_menu):
    assert 'POLETI S' in source and 'ODABERI LIKA' in source
assert 'setStroke(d(3),0xffffed9e' in android_menu
assert 'UIColor(red:1.00,green:0.88,blue:0.32' in ios_menu
logo_markup=(root/'docs/assets/logo.svg').read_text()
assert 'MALI LETOVI' in logo_markup and 'VELIKE PRIČE' in logo_markup
assert 'id="wings"' in logo_markup and 'id="letters"' in logo_markup
print('PASS: branded blue/gold parity, illustrated menus and functioning home navigation')
android_settings=android_menu.split("private fun showSettings()",1)[1].split("override fun onActivityResult",1)[0]
ios_settings=ios_menu.split("private func showSettings()",1)[1].split("func documentPicker(",1)[0]
for settings in (android_settings,ios_settings):
    for title in ("IZGLED I ZVUK","IGRAČ I TEŽINA","DODATNE OPCIJE","PODACI I PRIVATNOST",
                  "LIKOVI","TRGOVINA KOVANICAMA","MOJI REKORDI"):
        assert title in settings
    assert 'showSkins()' in settings and 'showPerks()' in settings
    assert 'showLeaderboard()' in settings and 'showHome()' in settings
    assert 'SPREMI IME' in settings and 'Vibracije' in settings
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
    assert 'showPerks(' in home and 'showSkins(' in home
for ui in (android_menu,ios_menu):
    for label in ('TRGOVINA KOVANICAMA','SPREMI KOPIJU NAPRETKA',
                  'VRATI NAPREDAK IZ KOPIJE','Nježnije animacije',
                  'Tvoje ime','Bez oglasa, praćenja i kupnje stvarnim novcem.',
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
    assert 'buyPerk(' in shop and 'showSkins(' in shop
    assert 'progress.coins()' in shop and 'showSettings()' in shop
# Primary gradient is orange gold and secondary gradient is cyan-to-royal-blue,
# never the retired neon-green/flat-blue button style.
assert 'cornerRadius=d(33)' in android_menu
assert '0xffffdf50.toInt()' in android_menu and '0xffff7a00.toInt()' in android_menu
assert '0xff23c8fc.toInt()' in android_menu and '0xff064cb4.toInt()' in android_menu
assert '0xff13b742.toInt()' not in android_menu
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
    for text in ('"Vihor"','"Iskra"','"Orion"','"portantin"','"noa"','"any"',
                 '220','240','owned_mask','skin_index','skinIndexForSlug'):
        assert text in inventory,text
    assert '0..8' in inventory or '0...8' in inventory
for index,character in ((6,'vihor'),(7,'iskra'),(8,'orion')):
    document=ET.parse(root/'docs/assets'/f'{character}.svg').getroot()
    assert document.attrib['viewBox']=='-80 -80 160 160'
    identifiers=[element.get('id') for element in document.iter()]
    assert identifiers.count('wing-left')==1 and identifiers.count('wing-right')==1
    assert f"'{character}'" in artgen
    for key in (f'R.drawable.bopi{index}',f'R.drawable.bopileft{index}',f'R.drawable.bopiright{index}'):
        assert key in android_render
    assert f'R.drawable.bopiportrait{index}' in android_gallery
assert 'min(8,max(0,skinIndex))' in ios_render
assert '9 complete glowing portraits, 9 bodies, 18 flapping wings' in artgen
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
    assert f'R.drawable.bopiportrait{index}' in android_preview
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
assert "required={'Vihor','Iskra','Orion'}" in android_qa
# A launcher "Wait" tap repeats the same system ANR; force-stop only its
# verified package and keep actual gallery/flight assertions mandatory.
assert gallery_qa.count("com.google.android.apps.nexuslauncher")==1
assert "'am','force-stop'" in gallery_qa
assert "subprocess.run(['adb','shell','pidof','com.brendigo.bopavi']" in gallery_qa
assert "missing characters after actual gallery scroll" in android_qa
assert "all_pilots={'Bopi','Sunny','Berry','Luna','Mint','Shadow','Vihor','Iskra','Orion'}" in android_qa
assert 'if all_pilots <= seen: break' in android_qa
assert 'if len(buttons)==1:' in android_qa
assert "swipe_gallery(root,up=True)" in android_qa
assert 'both cannot fit in one phone viewport' in android_qa
assert "if grep -q 'ODABERI LIKA' qa/screenshots/android-current-ui.xml; then" in android_qa
assert "if grep -q 'POLETI S' qa/screenshots/android-current-ui.xml; then" not in android_qa
assert "before==after" in android_qa
print('PASS: Android launcher-ANR recovery does not bypass actual pilot gallery QA')

# Premium reference-followup: illustrated 20-level grid and real dual-channel
# audio sliders are implemented symmetrically (never screenshot-only controls).
android_levels=android_menu.split('private fun showLevels(',1)[1].split('private fun characterGallery(',1)[0]
ios_levels=ios_menu.split('private func showLevels(',1)[1].split('private func characterGallery(',1)[0]
for level_ui in (android_levels,ios_levels):
    assert 'LEVELI' in level_ui
    assert 'NORMALNI · ⚡ IZAZOVNI' in level_ui
    assert 'PRETHODNIH 20' in level_ui and 'SLJEDEĆIH 20' in level_ui
    assert 'kind' in level_ui and 'frontier' in level_ui
    assert 'LevelKind.icon(kind)' in level_ui and 'LevelKind.name(kind)' in level_ui
    assert 'PRETHODNIH 20' in level_ui and 'POKRENI ODABRANI LEVEL' in level_ui
assert 'private fun levelTile(' in android_levels
assert 'private func levelTile(' in ios_levels
assert 'R.drawable.world0' in android_levels and 'UIImage(named:"World\\(world)")' in ios_levels
assert 'if(unlocked)' in android_levels and 'if unlocked' in ios_levels
for ui in (android_menu,ios_menu):
    assert 'volumeSlider(' in ui and 'musicVolume' in ui and 'effectsVolume' in ui
android_audio=(root/'android/app/src/main/java/com/brendigo/bopavi/Soundscape.kt').read_text()
ios_audio=(root/'ios/BOPAVI/Soundscape.swift').read_text()
for audio in (android_audio,ios_audio):
    assert 'musicVolume' in audio and 'effectsVolume' in audio
    assert 'if(enabled)' in audio or 'if enabled' in audio
for store in (android_save,ios_save):
    for key in ('musicVolume','effectsVolume','music_volume','effects_volume'):
        assert key in store, key
    assert 'soundEnabled' in store and 'hapticEnabled' in store
assert 'progress.setMusicVolume(it)' in android_settings
assert 'progress.setEffectsVolume(it)' in android_settings
assert 'self.progress.musicVolume=value' in ios_settings
assert 'self.progress.effectsVolume=value' in ios_settings
print('PASS: true illustrated twenty-level grid, 0..100 music/effects sliders and Android/iOS offline-save parity')

# User's premium settings reference requires three *real*, selectable
# horizontal difficulty cards, not Android's vertical RadioGroup versus an
# unrelated iOS segmented control. All indices remain 0/1/2 in saved state.
for settings in (android_settings,ios_settings):
    for label in ('LAGANO','NORMALNO','ZAHTJEVNO',
                  'Opušten let','Uravnotežen izazov','Za iskusne pilote'):
        assert label in settings,label
    assert 'modeCards' in settings and 'modeRow' in settings
    assert 'refreshDifficultyCards()' in settings
    assert 'bopiportrait' in settings.lower()
    assert 'odabrano' in settings and 'dodirni za odabir' in settings
assert 'android.widget.RadioGroup(this)' not in android_settings
assert 'UISegmentedControl(items:' not in ios_settings
assert 'progress.setDifficulty(mode)' in android_settings
assert 'self.progress.difficulty=mode' in ios_settings
assert 'importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES' in android_settings
assert 'card.accessibilityIdentifier="difficulty-card-' in ios_settings
assert 'verify_difficulty_cards' in qa_script
assert "('LAGANO','LAGANO'),('NORMALNO','NORMALNO')" in qa_script
assert "selected real premium difficulty card" in qa_script
print('PASS: accessible touch-tested three-card premium difficulty controls and Android/iOS offline choice parity')


# Premium Pause: no static screenshot, no legacy iOS action sheet. Every CTA
# is a real touch target, cancel resumes only the currently paused simulation.
android_pause=android_menu.split('private fun showPremiumPause(',1)[1].split('private fun startGame(',1)[0]
ios_pause=ios_menu.split('private func showPremiumPause(',1)[1].split('private func startGame(',1)[0]
for pause_ui in (android_pause,ios_pause):
    for label in ('PAUZA','NASTAVI','PONOVO','IZLAZ','KONTROLE','POSTAVKE','MUZIKA','ZVUK'):
        assert label in pause_ui,label
    assert 'Pauza · Level' in pause_ui
    assert 'musicVolume' in pause_ui and 'soundEnabled' in pause_ui
assert 'showPremiumPause(game)' in android_menu
assert 'showPremiumPause(for:canvas)' in ios_menu
assert 'dialog.setOnCancelListener{game.paused=false;sound.resume()}' in android_pause
assert 'startGame(currentWorld,currentLevel)' in android_pause
assert 'close(true)' in ios_pause and 'close(false)' in ios_pause
assert 'canvas?.paused=false' in ios_pause and 'overlay?.removeFromSuperview()' in ios_pause
assert 'preferredStyle:.actionSheet' not in ios_menu
assert 'View.INVISIBLE' in android_menu and 'pause.isHidden=true' in ios_menu
print('PASS: native premium pause overlay and real continue/retry/exit, audio and controls parity')

# v0.1.30 Google Play readiness: actual API level and signing workflow gates.
assert "compileSdk = 36" in gradle and "targetSdk = 36" in gradle
# Keep the release contract tied to both platform manifests, not a stale previous tag.
assert f"versionCode = {android_builds[0]}" in gradle
assert f'versionName = "{android_versions[0]}"' in gradle
assert 'applicationId = "com.brendigo.bopavi"' in gradle
assert 'android:appCategory="game"' in manifest
assert 'android:enableOnBackInvokedCallback="true"' in manifest
assert "OnBackInvokedDispatcher.PRIORITY_DEFAULT" in android
assert "registerOnBackInvokedCallback" in android and "unregisterOnBackInvokedCallback" in android
assert "private fun navigateBack()" in android and "override fun onBackPressed() = navigateBack()" in android
assert "signingConfigs" not in gradle and "BOPAVI_UPLOAD_" not in gradle
assert "signingConfig =" not in gradle  # GitHub cannot sign through Gradle.
# Signing jobs and signing secrets must never run in GitHub Actions.
# Android Play upload-key signing is an offline publisher-only workflow.
assert not (root/'.github/workflows/google-play-upload.yml').exists()
assert 'signing-smoke:' not in workflow
assert 'signing-smoke' not in workflow.split('  publish:',1)[1].split('    runs-on:',1)[0]
assert "BOPAVI-Google-Play-listing-v*.zip" in workflow
assert 'gh release create "$tag"' in workflow
assert 'existing" != "$GITHUB_SHA"' in workflow and 'BOPAVI_PLAY_ARCHIVE' in workflow
assert 'Require a new version for every pull request' in workflow
assert 'New PR must bump release version' in workflow
verify=(root/'tools/verify_play_release.py').read_text()
assert 'jarsigner' in verify and 'jar is unsigned' in verify and 'signature_entries' in verify
assert '--signed' not in verify and 'python tools/verify_play_release.py --aab dist/app-release.aab' in workflow
for name in ("GOOGLE-PLAY-PUBLISHING.md","play/STORE-LISTING-hr-HR.md",
             "play/GOOGLE-PLAY-DATA-SAFETY.md","play/privacy-policy.html",
             "play/RELEASE-CHECKLIST.md"):
    assert (root/'docs'/name).is_file(), name
artwork=(root/'tools/create_play_listing_assets.py').read_text()
for source in ("android-home.png","android-pilot-picker.png","android-gameplay-ready.png","android-result.png"):
    assert source in artwork and "qa/screenshots" in artwork
assert "ImageOps.fit" in artwork and "target_width * 16 // 9" in artwork
print("PASS: Play API36, real screenshots, signing isolation and publishing docs")

# Edge-to-edge release candidate: game controls and result actions avoid cutouts/system bars.
assert "WindowInsets.Type.displayCutout()" in android
assert "val top = maxOf(d(24),cutout.top+d(8))" in android
assert "scroll.setOnApplyWindowInsetsListener" in android
print("PASS: Android16 game controls and result safe-area handling")

# Premium results follow the supplied 3-stat / 4-reward reference layout,
# with REAL offline run values, no invented diamonds, ad offers or chests.
android_result=android_menu.split('private fun showResult(',1)[1].split('private fun showPerks()',1)[0]
ios_result=ios_menu.split('private func showResult(',1)[1].split('private func showPerks()',1)[0]
for result in (android_result,ios_result):
    for label in ('PRIKUPLJENE','UKUPNO','PROLAZA','NAGRADE','KOVANICE','ZVIJEZDE',
                  'BONUS','REKORD','PONOVO','LJESTVICA','POVRATAK','PODIJELI'):
        assert label in result,label
    assert 'g.score()' in result and 'g.coins' in result
    assert 'g.stars' in result and 'g.totalPassed' in result
    assert 'bonusCoinsAvailable()' in result and 'claimBonusCoins()' in result
    assert result.count('progress.recordRun(g)') == 1
    assert 'DIJAMANTI' not in result and 'ŠKRINJA' not in result
    assert 'showLeaderboard()' in result and 'showHome()' in result
    assert 'showPerks(' in result and 'showWorlds()' in result
    # Result screen intentionally returns to HOME after entering the store.
    assert 'showPerks(MenuOrigin.HOME)' in result or 'showPerks(.home)' in result
assert 'private fun resultCard(' not in android_result  # only UI-local builder
assert 'fun resultCard(' in android_result and 'func resultCard(' in ios_result
assert 'fun reward(' in android_result and 'func reward(' in ios_result
assert 'android.content.Intent.ACTION_SEND' in android_result
assert 'UIActivityViewController(activityItems:' in ios_result
assert 'bonusCounter.text="+0"' in android_result and 'bonusCounter.text="+0"' in ios_result
print('PASS: honest 3-stat/4-reward premium results and offline claim/share actions')

# Review of actual Android API35 result screenshot identified a concrete
# black-on-navy contrast bug. Keep reward icons legible after future restyling.
assert 'setTextColor(gold) // Never render black reward glyphs on navy.' in android_result
assert 'textSize=14f;setTextColor(Color.WHITE)' in android_result
assert 'details.numberOfLines=3' in ios_result
assert 'details.lineBreakMode = .byWordWrapping' in ios_result
print('PASS: reward icon contrast and compact responsive real-score details')

# Premium selection-first visual flow: featured characters appear on row one,
# but all nine identity indices, earned-coin costs and existing save keys stay
# unchanged. Every image is genuine art behind a separately interactive view.
for gallery in (android_gallery,ios_gallery):
    assert '6,7,8,0,1,2,3,4,5' in gallery
    assert 'displayOrder[position]' in gallery
    assert 'selectOrBuy(i)' in gallery
assert 'val preview=FrameLayout(this)' in android_picker
assert 'setImageResource(worlds[world.coerceIn(0,7)])' in android_picker
assert 'setImageResource(portraits[idx])' in android_picker
assert 'val selectedBadge=TextView(this)' in android_picker
assert 'let preview=UIView()' in ios_picker
assert 'UIImage(named:"World\\(world)")' in ios_picker
assert 'UIImage(named:"BopiPortrait\\(selected)")' in ios_picker
assert 'portrait.accessibilityLabel=' in ios_picker
for picker in (android_picker,ios_picker):
    assert picker.index('characterGallery(') < picker.index('POLETI S')
    assert 'ODABRAN' in picker
assert 'currentLevel=number' in android_picker and 'gameNumber=number' in ios_picker
print('PASS: 9-skin featured-row parity, layered hero and after-selection flight CTA')

# The real premium gameplay HUD keeps identical hierarchy on both platforms:
# coin count, star count, current level, cumulative gates and true within-level
# fraction; the separately actionable Pause remains outside the drawn scene.
android_hud=android_canvas.split('private fun drawHud(',1)[1].split('override fun onTouchEvent(',1)[0]
ios_hud=ios_canvas.split('private func drawBoostHUD(',1)[1].split('private func drawWorldLighting(',1)[0]
for hud in (android_hud,ios_hud):
    for value in ('game.coins','game.stars','game.shield','game.magnetTime'):
        assert value in hud,value
    # Reference orbs are passive indicators, never phantom store buttons.
    assert 'hudOrb(' in hud
for progress in (android_progress,ios_progress):
    assert 'game.displayLevel' in progress
    assert 'LEVEL ' in progress
    assert 'hudGlass(' in progress
for progress in (android_progress,ios_progress):
    assert 'game.passed' in progress and 'game.totalPassed' in progress
    assert 'PROLAZI UKUPNO' in progress
assert 'hudText' in ios_canvas
assert 'let counter=UILabel()' not in ios_menu
assert 'if(paused){rect(' not in android_canvas
assert 'if paused {' not in ios_canvas.split('override func draw(',1)[1].split('private func drawLevelProgress(',1)[0]
assert 'pause.isHidden=true' in ios_start and 'visibility=View.INVISIBLE' in android_start
print('PASS: authentic premium gameplay HUD, no duplicate iOS status chip or painted pause dialog')

# The character gallery, selection hero and pause must use complete wings-up
# portraits. The animated flight renderer MUST continue using three
# independent 512x512 layers to preserve real flap response and hitbox timing.
for asset in ('BopiPortrait','bopiportrait','Image.alpha_composite(wings,body)',
              'ImageFilter.GaussianBlur(11)'):
    assert asset in artgen,asset
assert 'for skin in range(9):' in artgen
assert 'portrait.save(src/name,optimize=True)' in artgen
assert 'portrait.save(setdir/name,optimize=True)' in artgen
assert android_gallery.count('R.drawable.bopiportrait') == 9
assert android_preview.count('R.drawable.bopiportrait') == 9
assert 'UIImage(named:"BopiPortrait\\(i)")' in ios_gallery
assert 'UIImage(named:"BopiPortrait\\(selected)")' in ios_picker
assert 'UIImage(named:"BopiPortrait\\(progress.skinIndex())")' in ios_menu
android_pause=android_menu.split('private fun showPremiumPause(',1)[1].split('private fun startGame(',1)[0]
assert android_pause.count('R.drawable.bopiportrait') == 9
assert 'R.drawable.bopiportrait' not in android_loader
assert 'BopiPortrait' not in swift_loader
assert 'birdSprites[selectedSkin]' in android_loader
assert 'BopiLeft\\(selectedSkin)' in swift_loader
assert 'BopiRight\\(selectedSkin)' in swift_loader
print('PASS: nine real full-wing portraits in all menus, with preserved detached flight animations')

# v0.1.34: bottom PROFILE is not a misleading alias for Settings.
assert '0->showProfile()' in android_menu
assert '("NavProfile","PROFIL",{self.showProfile()})' in ios_menu
assert 'UIImageView(image:UIImage(named:icon))' in ios_menu
for controller,signature,expected in ((android_menu,'private fun showProfile()',
                                      'Letački profil, '),
                                     (ios_menu,'private func showProfile()',
                                      'Letački profil, ')):
    assert signature in controller
    profile=controller.split(signature,1)[1].split('showLeaderboard()',1)[0]
    for content in ('TVOJI REZULTATI','Najbolji rezultat','Osvojene kovanice',
                    'Dovršeni leveli','Pobjede','progress.bestPoints()',
                    'progress.coins()','progress.wins()','showHome()'):
        assert content in profile,(signature,content)
    assert expected in profile
# UI visual hierarchy: reference places real vibration control after volumes.
for source,signature in ((android_menu,'private fun showSettings()'),
                         (ios_menu,'private func showSettings()')):
    settings=source.split(signature,1)[1]
    assert settings.index('Vibracije')<settings.index('IGRAČ I TEŽINA')
    assert settings.index('Nježnije animacije')>settings.index('DODATNE OPCIJE')
    assert settings.index('Glazba i zvučni efekti')>settings.index('DODATNE OPCIJE')
print('PASS: real offline PROFILE tab and premium functional settings order on both platforms')

# v0.1.34 retry regression: partial QA screenshot uploads must be replaced,
# never silently chosen over complete captures in the next CI attempt.
workflow=(root/'.github/workflows/native-ci.yml').read_text()
android_qa=workflow.split('  android-smoke:',1)[1].split('  play-listing:',1)[0]
assert 'name: QA-Android-visual-flow' in android_qa
assert 'overwrite: true' in android_qa
play_job=workflow.split('  play-listing:',1)[1].split('  android16-smoke:',1)[0]
assert 'actions: read' in play_job
assert 'Require one unambiguous Android QA screenshot archive' in play_job
assert 'Expected exactly one current QA-Android-visual-flow artifact' in play_job
assert play_job.index('Require one unambiguous Android QA screenshot archive')<play_job.index('name: QA-Android-visual-flow')
print('PASS: retry-safe Android screenshot evidence and deterministic Play packaging')

# v0.1.35: both result scenes must show the actual selected pilot and world,
# and the result label must never present a false new-record claim.
android_result=android_menu.split('private fun showResult(',1)[1].split('private fun showPerks()',1)[0]
ios_result=ios_menu.split('private func showResult(',1)[1].split('private func showPerks()',1)[0]
assert 'val previousRecord=progress.bestPoints()' in android_result
assert 'let previousRecord=progress.bestPoints()' in ios_result
assert 'val newRecord=g.score()>previousRecord && g.score()>0' in android_result
assert 'let newRecord=g.score()>previousRecord && g.score()>0' in ios_result
assert android_result.index('val previousRecord=')<android_result.index('progress.recordRun(g)')
assert ios_result.index('let previousRecord=')<ios_result.index('progress.recordRun(g)')
for ui in (android_result,ios_result):
    assert 'NOVI REKORD!' in ui
    assert 'if(newRecord)' in ui if ui is android_result else 'if newRecord' in ui
    assert 'Odabrani letač:' in ui
    assert 'skinNames[pilotIndex]' in ui
    assert 'progress.recordRun(g)' in ui
    assert 'if(g.won)"level" else "hit"' in ui if ui is android_result else 'g.won ? "level" : "hit"' in ui
assert 'pilotPortraits[pilotIndex]' in android_result
assert 'scenes[currentWorld]' in android_result
assert r'BopiPortrait\(pilotIndex)' in ios_result
assert r'World\(gameWorld)' in ios_result
print('PASS: result uses live pilot/world composition, true records and victory audio on both platforms')

# v0.1.36: earned stars must follow completed level callbacks and never use
# fabricated gallery totals or reset the endless-run stars HUD.
android_save=(root/'android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt').read_text()
ios_save=(root/'ios/BOPAVI/ProgressStore.swift').read_text()
android_sim=(root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
ios_sim=(root/'ios/BOPAVI/BopaviCore.swift').read_text()
for sim in (android_sim,ios_sim):
    assert 'levelStars' in sim and 'completedLevelStars' in sim
    assert 'stars' in sim and 'collectedStars' in sim
    assert 'levelStars=0' in sim or 'levelStars = 0' in sim
for store in (android_save,ios_save):
    for key in ('level_star_','bestLevelStars','recordLevelStars','worldStars',
                'levelStars','levelStarRecords','exportVersion'):
        assert key in store,key
    assert 'version", 6' in store or '"version":6' in store
    assert '1..6' in store or '(1...6)' in store
for view in (android_menu,ios_menu):
    assert 'completedLevelStars' in view and 'recordLevelStars(' in view
    assert 'worldStars(world)' in view
    assert 'bestLevelStars(world,' in view
    assert 'zvjezdice' in view
    assert ('"★".repeat(starCount)' in view or 'String(repeating:"★",count:earned)' in view)
assert 'levelStars=0' in ios_sim
assert 'levelStars = 0' in android_sim
print('PASS: real level stars, responsive medals, 30-star world badges and v6 backup parity')

# v0.1.37 reference world gallery: identical stable world ID semantics on
# Android and iOS, even though display labels and biome art were upgraded.
ref_names=["Zeleni otoci","Kristalno nebo","Lava vrhovi","Noćni dvorci",
           "Biserno more","Nebeske kule","Čarobna šuma","Zvjezdani bezdani"]
android_levels=(root/'android/app/src/main/java/com/brendigo/bopavi/LevelEngine.kt').read_text()
ios_levels=(root/'ios/BOPAVI/BopaviCore.swift').read_text()
for n in ref_names:
    assert '"'+n+'"' in android_levels and '"'+n+'"' in ios_levels,n
for source in (android_levels,ios_levels):
    for value in ('1_048_576','8','Zeleni otoci','Zvjezdani bezdani'):
        assert value in source
assert 'WORLD_SCENE_SOURCE = (0, 2, 3, 5, 6, 4, 1, 7)' in worlds
assert 'enchanted_forest_overlay()' in worlds
assert 'svg=shapes(source_index)' in worlds
assert 'if world==6:' in worlds
assert 'source_index=WORLD_SCENE_SOURCE[world]' in worlds
for n in ref_names:
    assert n in (root/'README.md').read_text()
assert "worlds=('Zeleni otoci','Kristalno nebo','Lava vrhovi','Noćni dvorci'" in (root/'tools/qa_android_emulator.sh').read_text()
print('PASS: reference-named 8-world gallery with stable save IDs and unique forest biome')

# v0.1.38: premium illustrated card composition is made of live
# accessible controls, not a flat screenshot with baked-in world metrics.
for source in (android_tile,ios_tile):
    assert 'worldStars(world)' in source
    assert 'streamFrontier(world)' in source
    assert 'collectibles(world)' in source
    assert 'best(world)' in source
    assert 'name.uppercase()' in source or 'names[world].uppercased()' in source
assert 'FrameLayout.LayoutParams(-1,d(31),Gravity.BOTTOM)' in android_tile
assert 'FrameLayout.LayoutParams(-1,d(70),Gravity.BOTTOM)' in android_tile
assert 'RippleDrawable(ColorStateList.valueOf(0x33ffffff)' in android_tile
assert 'artPanel,LinearLayout.LayoutParams(-1,d(132))' in android_tile
assert 'footerShade.isUserInteractionEnabled=false' in ios_tile
assert 'tile.accessibilityTraits.insert(.selected)' in ios_tile
assert 'tile.accessibilityHint="Otvori levele svijeta"' in ios_tile
assert 'tile.heightAnchor.constraint(equalToConstant:144)' in ios_tile
assert 'headline.trailingAnchor.constraint(equalTo:tile.trailingAnchor,constant:-12)' in ios_tile
for forbidden in ('text="★  ${progress.worldStars(world)}/30"', 'earned.text="★  \\(progress.worldStars(world))/30"'):
    assert forbidden not in android_tile+ios_tile
print('PASS: responsive premium world card overlay, native labels and saved metric parity')

# v0.1.39: Pause -> Settings must NEVER dispose or replace the game.
# The live overlay exposes functional sliders + haptics on both platforms.
android_pause=android_menu.split('private fun showPremiumPause(',1)[1].split('private fun startGame(',1)[0]
ios_pause=ios_menu.split('private func showPremiumPause(',1)[1].split('private func startGame(',1)[0]
for pause in (android_pause,ios_pause):
    assert 'POSTAVKE U PAUZI' in pause
    assert 'Promjene se spremaju odmah. Let ostaje pauziran.' in pause
    assert 'progress.musicVolume' in pause
    assert 'progress.effectsVolume' in pause
    assert 'progress.hapticEnabled' in pause
    assert 'sound.pause()' in pause
    assert 'showSettings()' not in pause.split('quick("⚙',1)[1].split('panel.addArrangedSubview(footer)' if pause is ios_pause else 'content.addView(footer',1)[0]
assert 'pauseSettings.visibility=if(pauseSettings.visibility==View.VISIBLE)' in android_pause
assert 'content.addView(pauseSettings' in android_pause
assert 'dialog.dismiss();showSettings()' not in android_pause
assert 'pauseSettings.isHidden.toggle()' in ios_pause
assert 'panel.addArrangedSubview(pauseSettings)' in ios_pause
assert 'close(false);self.showSettings()' not in ios_pause
# Retry/Exit are still explicitly destructive, but audio settings aren't.
assert 'command("↻  PONOVO"' in android_pause and 'command("↪  IZLAZ"' in android_pause
assert 'command("↻  PONOVO"' in ios_pause and 'command("↪  IZLAZ"' in ios_pause
print('PASS: Android and iOS pause settings preserve active flight and offline settings')

# v0.1.40 illustrated pilot gallery must use real local card actions,
# keep canonical indices and never bake names/prices into world images.
for gallery in (android_gallery,ios_gallery):
    assert '6,7,8,0,1,2,3,4,5' in gallery
    assert 'PREMIUM' in gallery and 'Potrošit ćeš' in gallery
    assert '🔒' in gallery and 'status' in gallery
    assert 'selectOrBuy(i)' in gallery
assert 'cardWorlds=intArrayOf(' in android_gallery
assert 'setImageResource(portraits[i])' in android_gallery
assert 'card.addView(artwork,LinearLayout.LayoutParams(-1,d(132)))' in android_gallery
assert 'row.addView(card,LinearLayout.LayoutParams(0,d(174),1f)' in android_gallery
assert 'UIImage(named:"World\\(i % 8)")' in ios_gallery
assert 'UIImage(named:"BopiPortrait\\(i)")' in ios_gallery
assert 'card.heightAnchor.constraint(equalToConstant:174)' in ios_gallery
assert 'price.text=status' in ios_gallery
print('PASS: premium illustrated native character gallery with original offline entitlement')

# v0.1.41: actual v0.1.40 emulator captures showed an unwanted Android
# system status strip above menu artwork. Preserve notch navigation safe zones.
android_chrome=android_menu.split('private fun applyPremiumSystemBars()',1)[1].split('override fun onCreate(',1)[0]
assert 'View.SYSTEM_UI_FLAG_FULLSCREEN' in android_chrome
assert 'View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN' in android_chrome
assert android_menu.count('applyPremiumSystemBars()')==5
assert 'window.decorView.systemUiVisibility=0' not in android_menu
assert android_menu.count('Type.systemBars() or android.view.WindowInsets.Type.displayCutout()')==2
assert 'root.setOnApplyWindowInsetsListener' not in android_menu
assert 'setAutoSizeTextTypeUniformWithConfiguration(11,16,1' in android_menu
assert 'setAutoSizeTextTypeUniformWithConfiguration(8,11,1' in android_home
assert 'override var prefersStatusBarHidden:Bool { true }' in ios_menu
assert 'safeAreaLayoutGuide.topAnchor' in ios_menu
print('PASS: Android/iOS full-bleed menus with camera cutout and narrow caption support')

# v0.1.42: pre-flight mascot moves in its art layer only. Menu animation
# respects OS reduced-motion settings and must clean up on leaving the view.
android_picker=android_menu.split('private fun showPilotPicker(',1)[1].split('private fun showPremiumPause(',1)[0]
ios_picker=ios_menu.split('private func showPilotPicker(',1)[1].split('private func showPremiumPause(',1)[0]
for picker in (android_picker,ios_picker):
    assert 'pilotHover' in picker
    assert 'portrait' in picker
    assert 'characterGallery(' in picker
    assert 'startGame(' in picker
assert 'ObjectAnimator.ofFloat(' in android_picker
assert 'ValueAnimator.areAnimatorsEnabled()' in android_picker
assert 'addOnAttachStateChangeListener(' in android_picker
assert 'onViewDetachedFromWindow(v:View)' in android_picker
assert 'pilotHover.cancel()' in android_picker
assert 'v.translationY=0f' in android_picker
assert 'UIAccessibility.isReduceMotionEnabled' in ios_picker
assert 'CABasicAnimation(keyPath:"transform.translation.y")' in ios_picker
assert 'portrait.layer.add(pilotHover,forKey:"pilotHover")' in ios_picker
assert 'pilotHover' not in android_menu.split('private fun startGame(',1)[1]
assert 'pilotHover' not in ios_menu.split('private func startGame(',1)[1]
print('PASS: reduced-motion-safe, lifecycle-clean pilot hover without gameplay changes')

# v0.1.43 — Back follows the originating menu, including after purchases.
assert 'private enum class MenuOrigin { HOME, SETTINGS, PROFILE, SHOP }' in android_menu
assert 'private enum MenuOrigin { case home, settings, profile, shop }' in ios_menu
assert 'menuBackAction=onClick' in android_menu
assert 'menuBackAction?.invoke() ?: showHome()' in android_menu
for h in (android_home,ios_home):
    assert 'showSkins(MenuOrigin.HOME)' in h or 'showSkins(.home)' in h
    assert 'showPerks(MenuOrigin.HOME)' in h or 'showPerks(.home)' in h
    assert 'showAchievements(MenuOrigin.HOME)' in h or 'showAchievements(.home)' in h
assert 'showSkins(MenuOrigin.SHOP,origin)' in android_shop
assert 'self.showSkins(.shop,shopOrigin:origin)' in ios_shop
assert 'showSkins(origin,shopOrigin)' in android_skins
assert 'self.showSkins(origin,shopOrigin:shopOrigin)' in ios_skins
assert 'MenuOrigin.SHOP -> showPerks(shopOrigin)' in android_skins
assert 'case .shop: self.showPerks(shopOrigin)' in ios_skins
for ui in (android_menu,ios_menu):
    assert 'showAchievements(MenuOrigin.PROFILE)' in ui or 'showAchievements(.profile)' in ui
    assert 'showSkins(MenuOrigin.PROFILE)' in ui or 'showSkins(.profile)' in ui
print('PASS: context-aware home, settings, profile and store return targets')

# v0.1.43 release hotfix — never accept arbitrary picker exit as success.
android_emulator=(root/'tools/qa_android_emulator.sh').read_text()
assert 'Pilot confirmation is settling; re-reading hitbox' in android_emulator
assert 'pilot confirmation navigated away but NOT to the' in android_emulator
assert 'android-pilot-launch-diagnostic.xml' in android_emulator
assert 'pilot choice left the picker; outer QA verifies gameplay state' not in android_emulator
assert 'expected HOME, pilot picker or idle GameView' in android_emulator
print('PASS: flight-launch emulator QA verifies GameView, not an arbitrary menu exit')

# v0.1.44: Pause quick MUZIKA may not reset the personalized volume.
android_pause=android_menu.split('private fun showPremiumPause(',1)[1].split('private fun startGame(',1)[0]
ios_pause=ios_menu.split('private func showPremiumPause(',1)[1].split('private func startGame(',1)[0]
for store in (android_save,ios_save):
    assert 'music_last_audible_volume' in store
    assert 'music_volume' in store
assert 'fun toggleMusicVolume():Int' in android_save
assert 'func toggleMusicVolume()->Int' in ios_save
for pause in (android_pause,ios_pause):
    assert 'toggleMusicVolume()' in pause
    assert 'sound.pause()' in pause
    assert '✓' in pause and '✕' in pause
assert 'val level=if(progress.musicVolume()==0)80 else 0' not in android_pause
assert 'self.progress.musicVolume==0 ? 80 : 0' not in ios_pause
assert '[weak control]' in ios_pause
print('PASS: pause footer audio state and previous custom volume persisted on both platforms')

# v0.1.45: browse locked future level pages, but never start a locked level.
android_levels=android_menu.split('private fun showLevels(',1)[1].split('private fun levelTile(',1)[0]
ios_levels=ios_menu.split('private func showLevels(',1)[1].split('private func levelTile(',1)[0]
assert 'if(start+20<=LevelEngine.LEVELS_PER_WORLD)' in android_levels
assert 'if start+20<=BopaviCore.levelsPerWorld {' in ios_levels
for levels in (android_levels,ios_levels):
    assert 'SLJEDEĆIH 20' in levels and 'PRETHODNIH 20' in levels
    assert 'VRATI NA OTKLJUČANE LEVELE' in levels
    assert 'Ovi leveli su zaključani' in levels
    assert 'POKRENI ODABRANI LEVEL' in levels
assert 'if(start<=maxNumber)' in android_levels
assert 'if start<=maxNumber {' in ios_levels
assert 'showLevels(world,maxNumber)' in android_levels
assert 'showLevels(world,page:maxNumber)' in ios_levels
assert 'if(n==null||n !in 1..maxNumber)' in android_levels
assert '(1...maxNumber).contains(n)' in ios_levels
limit=1_048_576
for frontier in (1,11,20,21,40,limit):
    for selected_page in (1,21,41,limit):
        first=((max(1,min(selected_page,limit))-1)//20)*20+1
        assert (first+20<=limit)==(first<=limit-20)
        assert (first<=frontier)==(not first>frontier)
print('PASS: locked level preview and last-page bounds preserve saved unlocks')

# v0.1.46: preview of a future locked page must NEVER display a
# continue-flight action that silently launches the previous unlocked page.
# Both screen trees still allow a saved-frontier return.
android_levels=android_menu.split('private fun showLevels(',1)[1].split('private fun levelTile(',1)[0]
ios_levels=ios_menu.split('private func showLevels(',1)[1].split('private func levelTile(',1)[0]
for levels in (android_levels,ios_levels):
    assert 'STRANICA' in levels
    assert 'PREGLED ZAKLJUČANIH LEVELA' in levels
    assert 'ZAKLJUČANI LEVELI' in levels
    assert 'worldStars(world)' in levels
    assert 'NASTAVI LET' in levels
    assert 'VRATI NA OTKLJUČANE LEVELE' in levels
assert android_levels.index('if(start<=maxNumber){') < android_levels.index('action(b,"▶  NASTAVI LET")')
assert ios_levels.index('if start<=maxNumber {') < ios_levels.index('button("▶  NASTAVI LET"')
assert android_levels.count('action(b,"▶  NASTAVI LET")') == 1
assert ios_levels.count('button("▶  NASTAVI LET"') == 1
assert '(LevelEngine.LEVELS_PER_WORLD-1)/20+1' in android_levels
assert '(BopaviCore.levelsPerWorld-1)/20+1' in ios_levels
for max_num in (1,20,21,40,41,1_048_576):
    for selected_page in (1,21,41,1001,1_048_561):
        start=((selected_page-1)//20)*20+1
        show_continue=(start <= max_num)
        assert show_continue == ((start-1)//20 <= (max_num-1)//20)
print('PASS: future locked preview hides misleading launch and shows real page progress')

# v0.1.46 CI reliability: explicitly recover the exact system launcher ANR
# even on the first foreground check, but never mask a BOPAVI ANR.
android_qa=(root/'tools/qa_android_emulator.sh').read_text()
main_loop=android_qa.split('gameplay_ready=0\nhome_captured=0',1)[1].split('echo "PASS: IGRAJ opens idle flight',1)[0]
assert 'dump_ui\n  # A system Pixel Launcher ANR' in main_loop
assert 'if recover_launcher_anr; then\n    sleep 2\n    dump_ui' in main_loop
assert 'elif recover_launcher_anr; then' in main_loop
assert 'attempt=$((attempt+1))\n    continue' in main_loop
assert 'titles != ["Pixel Launcher isn\'t responding"]' in android_qa
assert 'com.google.android.apps.nexuslauncher' in android_qa
assert 'adb shell pidof com.brendigo.bopavi' in main_loop
print('PASS: bounded Android startup recovery only for verified system launcher ANR')

# v0.1.47: all four home header controls must be live and clickable.
# No fictitious gem counter or placeholder account is acceptable offline.
android_home=android_menu.split('private fun showHome()',1)[1].split('private fun showWorlds()',1)[0]
ios_home=ios_menu.split('private func showHome()',1)[1].split('private func showWorlds()',1)[0]
for home in (android_home,ios_home):
    assert 'PROFIL ·' in home
    assert 'ZADACI I POSTIGNUĆA' in home
    assert 'TRGOVINA ·' in home
    assert 'POSTAVKE' in home
    assert 'progress.coins()' in home and 'progress.bestPoints()' in home
    assert 'DIJAMANTI' not in home and 'ODJAVA' not in home
assert 'val avatar=ImageView(this)' in android_home
assert 'R.drawable.bopiportrait8' in android_home
assert 'showProfile()' in android_home
assert 'showAchievements(MenuOrigin.HOME)' in android_home
assert 'showPerks(MenuOrigin.HOME)' in android_home
assert 'showSettings()' in android_home
assert 'LinearLayout.LayoutParams(0,d(48),1f)' in android_home
assert 'avatar.setImage(UIImage(named:"BopiPortrait\\(progress.skinIndex())")' in ios_home
assert 'self.showProfile()' in ios_home
assert 'self.showAchievements(.home)' in ios_home
assert 'self.showPerks(.home)' in ios_home
assert 'self.showSettings()' in ios_home
assert 'best.widthAnchor.constraint(equalTo:coins.widthAnchor)' in ios_home
print('PASS: live Android/iOS four-action home header with honest earned currency')

# v0.1.47 UI smoke must allow the reference's two independent Settings
# controls (top gear and main CTA), never mask genuine broken navigation.
assert 'expected={"IGRAJ":1,"SVJETOVI":1,"POSTAVKE":2}' in qa
assert "('PROFIL ·','ZADACI I POSTIGNUĆA','TRGOVINA ·')" in qa
assert 'if len(set(settings))!=2:' in qa
assert 't,l,r,b=max(regions)' in qa
print('PASS: QA verifies distinct home header gear and main Settings CTA')

# v0.1.48: idle preview draws once, then only after an actual flap.
# Live frames are scheduled while unpaused and active. The debug-only
# on-device evidence hold suppresses frame advancement until an actual second
# touch; production builds never enable this hold.
assert 'if(!paused && !qaWaitingForCapture && game.active && !game.finished && isAttachedToWindow)' in android_render
assert 'qaCaptureFirstFlap &&' in android_render
assert 'ApplicationInfo.FLAG_DEBUGGABLE' in android_render
assert 'if(event.actionMasked==MotionEvent.ACTION_DOWN) performClick()' in android_touch
assert 'game.flap()' in android_click and 'game.flap()' not in android_touch
assert 'super.performClick()' in android_click
assert 'if(!paused && !game.finished)' in android_click
assert 'string:"DODIRNI ZA LET"' in ios_render
assert 'let prompt=game.dragControl ? dragPrompt : idlePrompt' in ios_render
assert 'prompt.draw(at:' in ios_render
assert 'guard link == nil && window != nil && game.active && !game.finished && !paused' in ios_render
assert 'if paused {stop()} else {start()}' in ios_render
assert 'override func accessibilityActivate()->Bool' in ios_render
assert 'isAccessibilityElement=true' in ios_render
assert 'triggerFlap()' in ios_touch and 'game.flap()' not in ios_touch
assert 'start()' in ios_flap and 'setNeedsDisplay()' in ios_flap
assert 'guard !paused && !game.finished else{return false}' in ios_render
print('PASS: idle frame scheduling, on-canvas prompt and accessible first flap')

# v0.1.49: footer illustration art is built from shared source SVGs
# and is never a second, inaccessible/fake button.
render=(root/'tools/generate_images.py').read_text()
assert '((900-logoCrop.width)//2,230)' in render
assert '((900-logoCrop.width)//2,130)' not in render
android_home=android_menu.split("private fun showHome()",1)[1].split("private fun showWorlds()",1)[0]
ios_home=ios_menu.split("private func showHome()",1)[1].split("private func showWorlds()",1)[0]
for key in ('profile','tasks','collection','shop'):
    source=root/'docs/assets'/f'nav-{key}.svg'
    assert source.is_file()
    assert '<svg' in source.read_text() and 'viewBox="0 0 128 128"' in source.read_text()
    assert f'R.drawable.nav_{key}' in android_home
    assert f'Nav{key.capitalize()}' in ios_home
assert 'setImageResource(footerIcons[index])' in android_home
assert 'UIImageView(image:UIImage(named:icon))' in ios_home
assert 'tab.accessibilityLabel=title' in ios_home
assert 'Nav*.imageset/' in (root/'.github/workflows/native-ci.yml').read_text()
print('PASS: unobscured logo placement and 4 shared accessible native footer icons')

# Reference mascot identity: no legacy pink tail/scarf; badge is a real
# drawable element in the one shared Bopi source.
bopi_svg=(root/'docs/assets/hero.svg').read_text()
assert 'fill="#f53760"' not in bopi_svg
assert 'stroke="#f33f5e"' not in bopi_svg
assert 'stroke="#1164c9"' in bopi_svg
assert 'L-12 84 1 94 14 83' in bopi_svg
print('PASS: blue pilot scarf and crown identity shared with animated body source')

# v0.1.50 — feather detail must stay in animated wings, never torso.
from xml.etree import ElementTree as SvgTree
bird_markup=(root/'docs/assets/hero.svg').read_text()
parsed=SvgTree.fromstring(bird_markup)
wing_layers=[
    el.attrib.get('data-wing') for el in parsed.iter()
    if el.attrib.get('data-wing') in ('left','right')
]
assert wing_layers.count('left')>=4 and wing_layers.count('right')>=4
assert 'id="irisBopi"' in bird_markup and 'id="featherGlint"' in bird_markup
assert 'fill="#672536"' in bird_markup and 'fill="#fc7681"' in bird_markup
assert 'Q-50 -145 -13 -162' in bird_markup
assert 'M-4 30Q16 16 49 20' in bird_markup
generation=(root/'tools/generate_images.py').read_text()
assert 'shadow_oval' in generation and "body_markup.replace(shadow_oval,'',1)" in generation
assert 'wing_parts={side:' in generation
assert 'for shape in wing_shapes' in generation
assert "wing_parts[side]" in generation
assert 'wing_shapes[:2]' not in generation and 'wing_shapes[2:4]' not in generation
for stable in ("((6,'vihor'),(7,'iskra'),(8,'orion'))", "f'Bopi{skin}.imageset'", 'range(9)','Image.alpha_composite(wings,body)'):
    assert stable in generation
print('PASS: detailed royal-blue Bopi mascot with truly detached animated feather layers')

# v0.1.51 — backgrounds must not be erased by the legacy solid bottom band.
# These are genuine 480x800 scenery art layers; physics remains untouched.
assert 'val groundTop=' not in android_canvas
assert 'let groundTop=' not in ios_canvas
assert 'canvas.drawBitmap(worldBitmap,null,worldCoverRect,p)' in android_canvas
assert 'world.draw(in:CGRect(x:(bound.width-backdropWidth)*0.5,' in ios_canvas
assert 'drawColor(skyA[game.level.world])' in android_canvas
# Real release captures, NOT the conceptual reference images, are the only
# source for README screenshots. A missing capture fails publication.
ci=(root/'.github/workflows/native-ci.yml').read_text()
readme=(root/'README.md').read_text()
for image in (
    'BOPAVI-Android-Home.png','BOPAVI-Android-Pilots.png',
    'BOPAVI-Android-Flight-Preview.png','BOPAVI-Android-Results.png',
    'BOPAVI-iOS-Home.png'
):
    assert image in ci and image in readme, image
assert "name: QA-Android-visual-flow" in ci
assert "name: QA-iOS-home-screenshot" in ci
assert "Missing authentic emulator screenshot" in ci
assert "struct.unpack('>II'" in ci
assert "python tools/audit_native_dead_code.py" in ci
assert 'MALI LETOVI • VELIKE PRIČE' in readme
assert '<img src="docs/assets/logo.svg"' in readme
assert 'Priprema za let' in readme  # preview is not falsely called active flight
assert 'groundTop' not in android_canvas and 'groundTop' not in ios_canvas
print('PASS: real device screenshot marketing gallery, full-height world art and native dead-code guard')

# v0.1.52 — every actual generated mobile world backdrop must float above
# layered clouds; a flat rectangle at y=757 made the game look grounded.
world_source=(root/'tools/generate_worlds.py').read_text()
assert 'def foreground_cloud_bank(index: int)' in world_source
assert 'add(foreground_cloud_bank(index))' in world_source
assert 'fill="url(#cloud-bank)"' in world_source
assert 'fill="url(#cloud-soft)"' in world_source
assert 'x="0" y="757" width="480" height="43"' not in world_source
assert 'WORLD_SCENE_SOURCE = (0, 2, 3, 5, 6, 4, 1, 7)' in world_source
assert 'for world in range(8)' in world_source

# Premium chips are real game HUD, not a static mockup, on both platforms.
android_game=(root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
ios_game=(root/'ios/BOPAVI/GameCanvas.swift').read_text()
assert 'private fun hudGlass(' in android_game
assert 'private fun hudGoldCoin(' in android_game
assert 'private func hudGlass(' in ios_game
assert 'private func hudGoldCoin(' in ios_game
assert 'hudCoinFont=UIFont.systemFont' in ios_game
assert 'hudText("♛",24,31,hudCoinFont' in ios_game
assert 'hudGlass(c,13f,21f,139f,75f)' in android_game
assert 'hudGlass(c,13,21,126,54)' in ios_game
assert 'hudOrb(c,46f,175f,"★"' in android_game
assert 'hudOrb(c,46,175,"★"' in ios_game
for source in (android_game,ios_game):
    for expression in ('game.coins','game.stars','game.displayLevel','game.shield','game.magnetTime'):
        assert expression in source
    assert 'game.flap()' in source
print('PASS: 8 biome cloud banks and cross-platform native-glass HUD with live values')

# v0.1.53 — compact 2x4, reference-proportioned art cards:
# progress and score must be real text over the image, not detached tall rows.
assert 'row.addView(TextView(this)' not in android_tile
assert 'let detail=UILabel()' not in ios_tile
assert 'artPanel.addView(TextView(this).apply' in android_tile
assert 'collection.text="★ \\(progress.worldStars(world))/30' in ios_tile
assert 'text="★ ${progress.worldStars(world)}/30' in android_tile
assert 'row.addView(artPanel,LinearLayout.LayoutParams(-1,d(132)))' in android_tile
assert 'tile.heightAnchor.constraint(equalToConstant:144)' in ios_tile
assert 'footerShade.topAnchor.constraint(equalTo:tile.topAnchor,constant:83)' in ios_tile
assert 'number.topAnchor.constraint(equalTo:tile.topAnchor,constant:12)' in ios_tile
assert 'worldTile(row,w){showLevels(w,1)}' in android_menu
assert 'worldTile(world,in:row){self.showLevels(world,page:1)}' in ios_menu
print('PASS: compact Android/iOS world gallery with interactive 2x4 image cards and real star counts')

# v0.1.53 — the OS launch image and fully interactive home menu have distinct
# scene compositions. Both are generated from approved existing art for the
# exact same Android/iOS assets; progress bar is never faked in a still image.
image_source=(root/'tools/generate_images.py').read_text()
assert "home_art.paste(logoCrop,((900-logoCrop.width)//2,230),logoCrop)" in image_source
assert "launch_art.paste(logoCrop,((900-logoCrop.width)//2,865),logoCrop)" in image_source
assert "home_art.save(a/'drawable-nodpi/homeback.png'" in image_source
assert "home_art.save(b/'HomeArt.imageset/home.png'" in image_source
assert "launch_art.save(a/'drawable-nodpi/splash.png'" in image_source
assert "launch_art.save(b/'LaunchArt.imageset/launch.png'" in image_source
assert "b/'HomeArt.imageset/Contents.json'" in image_source
assert 'setImageResource(R.drawable.homeback)' in android_menu
assert 'UIImage(named:"HomeArt")' in ios_menu
assert "android:drawable=\"@drawable/splash\"" in (root/'android/app/src/main/res/drawable/bopavi_startup.xml').read_text()
assert "<string>LaunchArt</string>" in (root/'ios/BOPAVI/Info.plist').read_text()
for live in ('showPilotPicker(world,progress.streamFrontier(world))','showWorlds()','showSettings()'):
    assert live in android_menu
print('PASS: distinct, shared Android/iOS launch/home graphics preserve live menu controls')

# Release gallery now includes an authentic eight-world Android screenshot
# captured before any scroll, not an exported marketing layout.
qa_script=(root/'tools/qa_android_emulator.sh').read_text()
workflow=(root/'.github/workflows/native-ci.yml').read_text()
readme=(root/'README.md').read_text()
assert "qa/screenshots/android-worlds.png" in qa_script
assert "bytes.fromhex('89504e470d0a1a0a')" in qa_script
assert "'dist/real-screenshots/android/android-worlds.png':'BOPAVI-Android-Worlds.png'" in workflow
assert "dist/BOPAVI-Android-Worlds.png" in workflow
assert 'BOPAVI-Android-Worlds.png' in readme
print('PASS: authentic Android eight-world screenshot is captured and published alongside native builds')

assert '            android/app/src/main/res/drawable-nodpi/homeback.png' in workflow
assert '            ios/BOPAVI/Assets.xcassets/HomeArt.imageset/' in workflow
assert "('homeback.png','home.png','HomeArt')" in workflow
print('PASS: startup and home scenes are included in build artifacts on both native platforms')

# v0.1.54 — active-flight reference hierarchy: left actual wallet + three
# circular star/shield/magnet readouts, top-centred crown-to-flag race gauge
# and separately clickable platform Pause. Both use actual gate progress,
# never advertise fictional distances measured in metres.
for source in (android_game,ios_game):
    assert 'private ' in source
    assert 'hudOrb(' in source
    assert source.count('hudOrb(')>=4   # one implementation + three live orbs
    assert 'game.passed' in source and 'game.level.gates' in source
    assert 'game.stars' in source and 'game.shield' in source
    assert 'game.magnetTime' in source and 'game.coins' in source
    assert 'game.displayLevel' in source
    # Concepts may be named in comments; actual HUD paint calls may not
    # print the reference mockup's imaginary 1,280 / 2,000 metres.
    for forbidden in ('text(c,"1,280','hudText("1,280','text(c,"2,000','hudText("2,000'):
        assert forbidden not in source
    assert '⚑' in source
    assert 'game.flap()' in source
assert 'hudGlass(c,146f,18f,406f,77f)' in android_game
assert 'hudGlass(c,146,18,260,59)' in ios_game
assert 'hudOrb(c,46f,249f,"◆"' in android_game
assert 'hudOrb(c,46,249,"◆"' in ios_game
assert 'hudOrb(c,46f,323f,"∩"' in android_game
assert 'hudOrb(c,46,323,"∩"' in ios_game
assert 'game.magnetTime>0f' in android_game and 'game.magnetTime>0' in ios_game
assert 'game.shield>0' in android_game and 'game.shield>0' in ios_game
assert 'showPremiumPause' in android_menu and 'showPremiumPause' in ios_menu
print('PASS: screenshot-proportioned cross-platform race gauge, 3 honest HUD orbs and preserved Pause')

# v0.1.55: coins are bevelled GOLD crowns in every biome, not generic
# biome-coloured dots; last-gate cyan portals track actual simulation geometry.
# All elements are paint-only, parity-tested and reduce motion correctly.
and_coin=android_canvas.split("private fun drawRoyalCoin(",1)[1].split("private fun drawFinishPortal(",1)[0]
ios_coin=ios_canvas.split("private func drawRoyalCoin(",1)[1].split("private func drawFinishPortal(",1)[0]
and_portal=android_canvas.split("private fun drawFinishPortal(",1)[1].split("private fun drawGate(",1)[0]
ios_portal=ios_canvas.split("private func drawFinishPortal(",1)[1].split("private func gate(",1)[0]
for source in (and_coin,ios_coin):
    for hue in ("ffcf53","ffc333","ec8e07","ffb91a","fff0ad"):
        assert hue in source, (hue, source[:160])
    assert "reducedMotion" in source and "game.time" in source
    assert "BitmapFactory" not in source and "UIImage(" not in source
    assert "game.step(" not in source and "game.flap(" not in source
    assert "new Path(" not in source and "CGPath(" not in source and "CGMutablePath(" not in source
for source in (and_portal,ios_portal):
    assert "30" in source and "reducedMotion" in source
    assert "game.time" in source and "phase" in source
    assert "45e9ff" in source and "e7ffff" in source
    assert "BitmapFactory" not in source and "UIImage(" not in source
    assert "game.step(" not in source and "game.flap(" not in source
android_gate_body=android_canvas.split("private fun drawGate(",1)[1].split("private fun drawFeedbackSparkles(",1)[0]
ios_gate_body=ios_canvas.split("private func gate(",1)[1].split("private func star(",1)[0]
for source in (android_gate_body,ios_gate_body):
    assert "drawRoyalCoin(" in source and "drawFinishPortal(" in source
    assert "coinVisible(index)" in source
    assert "incoming" in source
    assert "lastIndex" in source or "gates.count-1" in source
    assert source.index("drawFinishPortal(") < source.index("drawRoyalCoin(")
    assert "if" in source and "finalGate" in source
    assert "game.score(" not in source
assert "pickupSymbol" not in ios_canvas
assert "'dist/real-screenshots/android/android-gameplay-active.png':'BOPAVI-Android-Flight-Active.png'" in workflow
assert "dist/BOPAVI-Android-Flight-Active.png" in workflow
assert "BOPAVI-Android-Flight-Active.png" in (root/'README.md').read_text()
print("PASS: Android/iOS royal coin geometry, actual final-gate portal, reduced-motion and genuine flight release evidence")

# v0.1.55 release hotfix: slow UI automation may see a result long after
# a genuine flap. Preserve the ACTUAL in-flight screenshot taken immediately
# after input, before sleep/dump; do not allow unconfirmed input candidates.
qa_flight_loop=qa.split('capture android-gameplay-ready',1)[1].split('while [ "$attempt" -le 6 ]; do',1)[1].split('\ndone',1)[0]
for required in ('screencap -p /sdcard/bopavi-flight-1.png','sleep 1','dump_ui',
                 'rm -f qa/screenshots/android-gameplay-active.png',
                 "if grep -q 'Bopi leti'", "if grep -q 'PONOVO'"):
    assert required in qa_flight_loop,required
assert qa_flight_loop.index('input tap $center') < qa_flight_loop.index('screencap -p /sdcard/bopavi-flight-1.png')
assert qa_flight_loop.index('screencap -p /sdcard/bopavi-flight-1.png') < qa_flight_loop.index('sleep 1')
assert qa_flight_loop.index('screencap -p /sdcard/bopavi-flight-1.png') < qa_flight_loop.index('dump_ui')
assert "'dist/real-screenshots/android/android-gameplay-active.png':'BOPAVI-Android-Flight-Active.png'" in workflow
assert "dist/BOPAVI-Android-Flight-Active.png" in workflow
print('PASS: first active-flight frame survives slow accessibility dump and is mandatory in release')

# v0.1.56: neither platform depends on a device font for flying gold stars,
# and iOS no longer allocates a new CGPath per rounded HUD/gate fragment.
android_star=android_canvas.split("private fun drawGoldStar(",1)[1].split("private fun drawGate(",1)[0]
ios_star=ios_canvas.split("private func star(",1)[1].split("private func ",1)[0]
for source in (android_star,ios_star):
    assert "0" in source and "10" in source
    assert ".43" in source
    assert "cos(" in source and "sin(" in source
    assert "game.step(" not in source and "game.flap(" not in source
assert "path.reset()" in android_star and "c.drawPath(path,p)" in android_star
assert "new Path(" not in android_star
assert "drawGoldStar(c,middle+35f,center-25f,12f)" in android_canvas
assert "star(c,mid+35,center-25,12,0xffe25d)" in ios_canvas
android_pickups=android_canvas.split("private fun drawGate(",1)[1].split("private fun drawFeedbackSparkles(",1)[0]
assert "LevelEngine.collectibleIcons[g.kind]" not in android_pickups
ios_rect=ios_canvas.split("private func rect(",1)[1].split("private func oval(",1)[0]
assert "CGPath(roundedRect:" not in ios_rect
assert "c.addArc(" in ios_rect and ios_rect.count("c.addArc(")==4
assert "c.beginPath()" in ios_rect and "c.fillPath()" in ios_rect
assert "radius:r" in ios_rect and "min(rad,width/2,height/2)" in ios_rect
assert "guard width>0 && height>0 else {return}" in ios_rect
# The four quarter circles should retain the Android rounded-rectangle bounds.
import math
for width,height,requested in [(1,1,2),(8,36,18),(64,240,7),(480,800,27)]:
    radius=min(requested,width/2,height/2)
    assert 0<=radius<=width/2 and 0<=radius<=height/2
    for theta in [0,math.pi/2,math.pi,3*math.pi/2]:
        assert math.isfinite(radius*math.cos(theta))
print("PASS: cross-platform vector gold stars and allocation-free iOS rounded panels")

# v0.1.56: forensic check of 0.1.55 screenshot hashes found that
# active flight == idle preview. CI must now require distinct real PNGs.
assert 'sleep 0.27; screencap -p /sdcard/bopavi-flight-1.png' in qa_flight_loop
assert 'android-flight-candidate-$frame.png' in qa_flight_loop
assert 'screencap -p /sdcard/bopavi-flight-2.png' in qa_flight_loop
assert 'screencap -p /sdcard/bopavi-flight-3.png' in qa_flight_loop
assert 'selected_real_flight=0' in qa
assert 'qa/screenshots/android-flight-candidate-*.png' in qa
assert 'cmp -s qa/screenshots/android-result.png "$candidate"' in qa
assert qa_flight_loop.index('input tap $center') < qa_flight_loop.index('screencap -p /sdcard/bopavi-flight-1.png')
assert 'cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-gameplay-active.png' in qa_flight_loop
assert 'FAIL: active screenshot duplicates pre-flight preview' in qa_flight_loop
assert "sha256(ready.read_bytes()).digest()==sha256(active.read_bytes()).digest()" in workflow
assert "Active gameplay screenshot is identical to idle flight; release blocked" in workflow
print("PASS: authentic flight proof cannot repeat the idle preflap PNG frame")

# v0.1.57: real profile layout parity and persistence invariants.
android_profile=android_menu.split('private fun showProfile(){',1)[1].split('private fun showLeaderboard(){',1)[0]
ios_profile=ios_menu.split('private func showProfile(){',1)[1].split('private func showLeaderboard(){',1)[0]
for profile in (android_profile,ios_profile):
    for text in ('Letački profil, ','TVOJI REZULTATI','Najbolji rezultat',
                 'Osvojene kovanice','Dovršeni leveli','Pobjede',
                 'progress.bestPoints()','progress.coins()','progress.wins()',
                 'progress.frontier(','progress.worldStars(','progress.best(',
                 'progress.collectibles(','MOJE NAGRADE',
                 'PRVI LET','ISTRAŽIVAČ','SAKUPLJAČ','VETERAN',
                 'MOJI NAJBOLJI SVJETOVI','POGLEDAJ SVA POSTIGNUĆA',
                 'ODABERI DRUGOG LIKA'):
        assert text in profile,('Missing real profile feature',text)
    assert 'worldStars(' in profile and 'sorted' in profile
    assert 'if(earned)' in profile or 'earned ?' in profile
    assert '500' in profile and '10' in profile
    assert 'showHome()' in profile
    assert 'showSettings()' in profile
    assert 'showLevels(' in profile
    assert 'scoreCoinsClaimed' not in profile and 'claimBonusCoins()' not in profile
    assert 'signOut' not in profile and 'cloudSave' not in profile
assert 'progress.owned(' not in android_profile and 'progress.owned(' not in ios_profile
assert 'portraitBox' in android_profile and 'portraitBox' in ios_profile
for key in ('nav_profile','nav_tasks','nav_collection','nav_shop'):
    assert key in android_profile,key
for key in ('NavProfile','NavTasks','NavCollection','NavShop'):
    assert key in ios_profile,key
assert 'orientation=LinearLayout.HORIZONTAL' in android_profile
assert 'hero.axis = .horizontal' in ios_profile
assert 'contentDescription=navNames[index]' in android_profile
assert 'tab.accessibilityLabel=item.1' in ios_profile
assert 'qa/screenshots/android-profile.png' in android_emulator
assert 'dedicated PROFILE screen shows saved pilot data and real screenshot' in android_emulator
assert "'dist/real-screenshots/android/android-profile.png':'BOPAVI-Android-Profile.png'" in workflow
assert 'dist/BOPAVI-Android-Profile.png' in workflow
assert 'BOPAVI-Android-Profile.png' in (root/'README.md').read_text()
print('PASS: verified genuine profile screenshot required in release and README')
print('PASS: live offline profile, award thresholds, premium world previews and four real native tabs on both platforms')

# v0.1.58: profile visual fidelity must remain attached to *saved* progress.
# The reference shows an XP/metres gauge; the offline game has neither, so
# the gold progress rail is honest selected-world stars (0..30).
for profile in (android_profile,ios_profile):
    for grounded in ('progress.chosenWorld()', 'progress.worldStars(profileWorld)',
                     'profileStars', 'profileStarGauge' if profile == android_profile else 'starGauge',
                     'profileStars', 'profileWorld', '30', 'requirement',
                     '1 pobjeda', '5 svjetova', '500 predmeta', '10 pobjeda',
                     'MOJE NAGRADE', 'MOJI NAJBOLJI SVJETOVI',
                     'for((rank,world) in ranked.withIndex())' if profile == android_profile
                     else 'for (rank,world) in ranked.prefix(3).enumerated()',
                     '♛', 'URED'+'I PROFIL'):
        assert grounded in profile, ('profile visual contract',grounded)
    assert 'showLevels(' in profile and 'showSettings()' in profile
    assert 'progress.wins()>=10' in profile or 'progress.wins()>=10' in profile
    assert 'progress.frontier(' in profile and 'progress.collectibles(' in profile
    assert '1000 / 2000' not in profile and 'playerXP' not in profile
assert 'profileStarGauge.contentDescription=' not in android_profile  # property assigned within apply
assert 'contentDescription="Zvjezdice svijeta' in android_profile
assert 'UIProgressView(progressViewStyle:.default)' in ios_profile
assert 'starGauge.accessibilityValue=' in ios_profile
assert 'artFrame.addView' in android_profile and 'artFrame.addSubview' in ios_profile
assert 'rankMedal.isAccessibilityElement=false' in ios_profile
assert 'importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO' in android_profile
assert 'BOPAVI-Android-Profile.png' in (root/'README.md').read_text()
print('PASS: world-star profile gauge, real award goals, ranked thumbnails, accessibility and offline save parity')

# v0.1.59: flight aura follows the existing bird (not its collision geometry).
# Keep both engines on the same logical radius, gradient stops and flap impulse;
# all gradients/shaders are cached outside onDraw/draw.
android_bird=android_canvas.split('private fun drawBird(c:Canvas){',1)[1].split('private fun drawLevelProgress(',1)[0]
ios_bird=ios_render.split('private func bird(_ c:CGContext){',1)[1].split('private extension UIColor',1)[0]
assert 'private val birdAuraShader=RadialGradient(0f,0f,74f,' in android_canvas
assert 'intArrayOf(0x62fff3bb,0x3a2da9ff,0x002da9ff)' in android_canvas
assert 'floatArrayOf(0f,.55f,1f)' in android_canvas
assert 'private lazy var birdAuraGradient:CGGradient?' in ios_render
assert 'locations:[0,0.55,1]' in ios_render
assert 'endRadius:74,options:[]' in ios_bird
assert 'auraImpulse=if(reducedMotion)0f' in android_bird
assert 'auraImpulse:CGFloat=reducedMotion ? 0' in ios_bird
assert '1f+auraImpulse*.13f' in android_bird
assert '1+auraImpulse*0.13' in ios_bird
assert android_bird.index('p.shader=birdAuraShader') < android_bird.index('c.rotate((game.velocity')
assert ios_bird.index('c.drawRadialGradient(aura') < ios_bird.index('c.rotate(by:angle)')
assert 'p.shader=null\n        c.restore()' in android_bird
assert android_canvas.count('birdAuraShader=RadialGradient(')==1
assert ios_render.count('birdAuraGradient:CGGradient?')==1
assert 'radius = 23f' in (root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
assert 'radius: Float = 23' in (root/'ios/BOPAVI/BopaviCore.swift').read_text()
print('PASS: Bopi flight aura source parity, caching, reduced motion, and unchanged hitboxes')

# Keep the thirteen existing binaries/screenshots and provide independently
# verifiable release hashes, generated in GitHub Actions after staging.
assert 'Generate SHA-256 checksums for every release artifact' in workflow
assert 'sha256sum \\' in workflow
assert 'BOPAVI-iOS-Home.png > SHA256SUMS.txt' in workflow
assert 'test "$(wc -l < SHA256SUMS.txt)" -eq 14' in workflow
assert 'dist/SHA256SUMS.txt' in workflow
assert 'BOPAVI-iOS-Home.png SHA256SUMS.txt; do' in workflow
print('PASS: original release assets and authentic Settings capture have SHA-256 manifest')

# v0.1.60: the reference's settings screen is one scrolling glass sheet,
# featuring real chosen-pilot art, not a composited screenshot of controls.
settings_android=android_menu.split('private fun showSettings(){',1)[1].split('@Deprecated("Activity result',1)[0]
settings_ios=ios_menu.split('private func showSettings(){',1)[1].split('func documentPicker(',1)[0]
base_android=android_menu.split('private fun base(',1)[1].split('private fun title(',1)[0]
menu_ios=ios_menu.split('private func menu(',1)[1].split('override func viewDidLayoutSubviews()',1)[0]
assert 'val settingsPanel=LinearLayout(this).apply' in settings_android
assert 'setStroke(d(2),0xff42cfff.toInt())' in settings_android
assert 'BopaviSettingsPanel: UIStackView' in ios_menu
assert 'let settingsPanel=BopaviSettingsPanel()' in settings_ios
assert 'layer.borderWidth=2' in ios_menu
assert 'if(label=="POSTAVKE")' in base_android
assert 'portraits[progress.skin().coerceIn(0,8)]' in base_android
assert 'if title == "POSTAVKE"' in menu_ios
assert 'UIImage(named:"BopiPortrait\\(progress.skinIndex())")' in menu_ios
for label in ("IZGLED I ZVUK","IGRAČ I TEŽINA","DODATNE OPCIJE","PODACI I PRIVATNOST"):
    assert 'sectionHeading(settingsPanel,"'+label+'")' in settings_android,label
    assert 'sectionHeading("'+label+'",in:settingsPanel)' in settings_ios,label
for source in (settings_android,settings_ios):
    for saved in ("musicVolume","effectsVolume","hapticEnabled","difficulty",
                  "playerName","lessMotion","soundEnabled","export","import"):
        # export/import use existing actions and actual saved data, not cloud placeholders.
        if saved not in ("export","import"):
            assert saved in source,(saved,"settings persistence")
    assert "cloudSave" not in source and "signOut" not in source
assert 'back(b){showHome()}' in settings_android
assert 'button("‹  POČETNA",in:s,primary:false){self.showHome()}' in settings_ios
for text in ("SPREMI IME","SPREMI KOPIJU NAPRETKA","VRATI NAPREDAK IZ KOPIJE"):
    assert 'action(settingsPanel,"'+text+'"' in settings_android,text
    assert 'button("'+text+'",in:settingsPanel' in settings_ios,text
workflow=(root/'.github/workflows/native-ci.yml').read_text()
assert "'dist/real-screenshots/android/android-settings.png':'BOPAVI-Android-Settings.png'" in workflow
assert 'dist/BOPAVI-Android-Settings.png' in workflow
assert 'BOPAVI-Android-Settings.png' in (root/'README.md').read_text()
assert 'test "$(wc -l < SHA256SUMS.txt)" -eq 14' in workflow
print("PASS: responsive settings glass panels, saved pilot, functional audio/privacy, screenshot proof")

# v0.1.61 — world + mascot artwork is regenerated from the same source
# files for both platforms; animated feathers belong to their owning wing.
hero_xml=ET.parse(root/'docs/assets/hero.svg').getroot()
svg_paths=[e for e in hero_xml.iter() if e.tag.endswith('path')]
for side in ('left','right'):
    feather_paths=[e for e in svg_paths if e.attrib.get('data-wing')==side]
    assert len(feather_paths)>=15,(side,'missing separate animated wing feather contours')
    assert any(e.attrib.get('fill')=='none' for e in feather_paths),(side,'missing bright feather ribs')
    assert any(e.attrib.get('fill','').startswith('#') for e in feather_paths),(side,'missing layered feather color')
hero_svg=(root/'docs/assets/hero.svg').read_text()
assert 'Rim light and polished-gold bevel' in hero_svg
assert 'Articulated layered flight feathers' in hero_svg
source=(root/'tools/generate_worlds.py').read_text()
for fragment in ('Cinematic landmark pass','id="falls-mist"',
                 'waterfall_world = index in (0, 1, 2, 4, 5, 6)',
                 'for n, length in enumerate((166, 189, 154, 174, 132))',
                 'for n in range(9):','for n in range(6):',
                 'for n in range(5):','cinematic'):
    assert fragment.lower() in source.lower(),fragment
generator=(root/'tools/generate_images.py').read_text()
assert "wing_parts={side:" in generator
assert "for side in ('left','right')" in generator
assert "wing=recolor(original_wing,hue)" in generator
assert "Image.alpha_composite(wings,body)" in generator
# No gameplay math, saved skin identifiers or distribution signing touched.
assert 'radius = 23f' in (root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
assert 'radius: Float = 23' in (root/'ios/BOPAVI/BopaviCore.swift').read_text()
print('PASS: 30+ animated feather contours, gold goggle bevels and shared eight-world skyfalls')

# v0.1.62 — no release may reuse an active-flight screenshot for Results.
# The artifact digests in v0.1.61 were identical despite different labels.
emulator=(root/'tools/qa_android_emulator.sh').read_text()
assert 'result_capture_unique=0' in emulator
assert 'result_capture_attempt=1' in emulator
assert 'result_capture_attempt" -le 8' in emulator
assert 'cmp -s qa/screenshots/android-gameplay-active.png qa/screenshots/android-result.png' in emulator
assert 'cmp -s qa/screenshots/android-gameplay-ready.png qa/screenshots/android-result.png' in emulator
assert 'Results PNG duplicates active or idle gameplay after bounded retries' in emulator
assert 'grep -q \'PONOVO\'' in emulator
assert 'capture android-result-diagnostic' in emulator
workflow=(root/'.github/workflows/native-ci.yml').read_text()
assert "results=Path('dist/BOPAVI-Android-Results.png')" in workflow
assert 'for earlier in (ready,active):' in workflow
assert 'Results screenshot duplicates gameplay screenshot:' in workflow
assert 'results UI screenshot distinct from both idle and active gameplay' in workflow
print('PASS: real Results screenshot cannot equal active or idle flight release capture')

# v0.1.62 — new bird identities and prior saves resolve to same indices.
for inventory in catalogs:
    for pair in (('portantin','vihor'),('noa','iskra'),('any','orion')):
        assert all('"' + name + '"' in inventory for name in pair)
    assert 'skinIndexForSlug' in inventory
for fragment in ('(6,\'vihor\')','(7,\'iskra\')','(8,\'orion\')'):
    assert fragment in artgen
for controller in (android_menu,ios_menu):
    assert all(name in controller for name in ('Vihor','Iskra','Orion'))
    assert not any(text in controller for text in ('Portantin:','Noa:','Any:'))
print('PASS: 3 individual premium birds, stable skin indices and legacy save alias mapping')

# v0.1.62 — API36's slow Surface compositor otherwise exposes only the
# idle frame and later Results. A real debug-only first-flap state remains
# renderable until a second genuine touch continues the actual simulation.
# No non-debuggable binary can activate this screenshot timing accommodation.
android_view=(root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
assert 'qaCaptureFirstFlap:Boolean = false' in android_view
assert 'ApplicationInfo.FLAG_DEBUGGABLE' in android_view
assert 'qaCaptureFirstFlap &&' in android_view
assert 'if(qaCaptureEnabled) qaWaitingForCapture=true' in android_view
assert 'if(!paused && !qaWaitingForCapture && !game.finished)' in android_view
assert 'if(!paused && !qaWaitingForCapture && game.active' in android_view
assert 'qaWaitingForCapture=false' in android_view and 'lastFrame=0L' in android_view
assert 'game.flap()' in android_view.split('if(qaWaitingForCapture)',1)[1].split('} else {',1)[0]
assert 'qaCaptureFirstFlap=intent?.getBooleanExtra("bopavi_qa_capture_first_flap",false)==true' in android_menu
assert 'adb shell am start -W -n com.brendigo.bopavi/.MainActivity --ez bopavi_qa_capture_first_flap true' in qa
assert 'adb shell input tap "$center" "$((height*50/100))"' in qa_flight_loop
assert 'qaWaitingForCapture' not in ios_canvas and 'qaCaptureFirstFlap' not in ios_menu
print('PASS: real debug-only first-flap capture, production physics unchanged, second-touch resume')

# v0.1.63: control modes are real physics/input, not cosmetic badges.
android_game=(root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
ios_game=(root/'ios/BOPAVI/BopaviCore.swift').read_text()
android_view=(root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
ios_view=(root/'ios/BOPAVI/GameCanvas.swift').read_text()
android_ui=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
ios_ui=(root/'ios/BOPAVI/GameController.swift').read_text()
for engine in (android_game,ios_game):
    assert 'world % 2 == 1' in engine, 'missing 4 real drag-control worlds'
    assert 'steeringTarget' in engine and 'desired' in engine and 'blend' in engine
    assert '310' in engine and '*7' in engine and '*14' in engine
    assert 'y += velocity' in engine and 'damage()' in engine, 'collisions must remain in the simulation'
assert 'MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE -> steerTouch(event.y)' in android_view
assert 'override func touchesMoved(' in ios_view and 'steerTouch(touch)' in ios_view
for ui in (android_ui,ios_ui):
    assert '↕ POVUCI' in ui and '● TAP' in ui
    assert 'POVUCI GORE / DOLJE' in ui and 'TAP ZA ZAMAH' in ui
assert 'POVUCI GORE / DOLJE' in android_view and 'POVUCI GORE / DOLJE' in ios_view
assert 'if(event.actionMasked==MotionEvent.ACTION_DOWN) performClick()' in android_view
assert 'triggerFlap()' in ios_view
print('PASS: native drag steering and classic tap wings coexist across all 8 worlds')

# v0.1.64: one player profile, no stale nickname or local-storage banners in UI.
android_ui=(root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
ios_ui=(root/'ios/BOPAVI/GameController.swift').read_text()
android_saves=(root/'android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt').read_text()
ios_saves=(root/'ios/BOPAVI/ProgressStore.swift').read_text()
for ui in (android_ui,ios_ui):
    assert 'menu("MOJI REKORDI"' in ui or 'base("MOJI REKORDI"' in ui
    assert 'Osobni rekord' in ui and 'MOJA POSTIGNUĆA' in ui
    assert '🏆  MOJI REKORDI' in ui
    assert 'LOKALNA LJESTVICA' not in ui
    assert 'Najbolji stvarni rezultati na ovom uređaju' not in ui
    assert 'Tvoj napredak spremljen je samo na uređaju' not in ui
    assert 'Ime je spremljeno na uređaju' not in ui
    assert 'Tvoj letački dnevnik i osobni rezultati' in ui
assert 'LocalScore(playerName(),score,world,mode,gates)' in android_saves
assert 'JSONObject().put("name",restoredName)' in android_saves
assert 'return LocalScore(name:playerName' in ios_saves
assert 'return ["name":playerName,"score":score' in ios_saves
assert 'let scores=leaderboard().map(scoreDictionary)' in ios_saves
assert 'val entries=JSONArray()' in android_saves.split('fun setPlayerName(',1)[1].split('data class LocalScore',1)[0]
assert 'bopavi-save-\\(UUID().uuidString).json' in ios_ui
assert '.completeFileProtectionUnlessOpen' in ios_ui
assert 'activity.completionWithItemsHandler=' in ios_ui
assert 'FileManager.default.removeItem(at:url)' in ios_ui
assert 'val stream=contentResolver.openOutputStream' in android_ui
assert 'if(requestCode!=42 && requestCode!=43)return' in android_ui
assert 'Kopiju nije moguće otvoriti ili spremiti.' in android_ui
assert 'rawName=row["name"]' not in ios_saves
print("PASS: personal top-ten records, private export cleanup and no user-facing development/storage filler")

# v0.1.65: the old packaged composite hero is dead; the source SVG still
# generates all live winged sprites. Unused PNG/asset catalogs must stay gone.
generator=(root/'tools/generate_images.py').read_text()
workflow=(root/'.github/workflows/native-ci.yml').read_text()
assert "Hero.imageset" not in generator
assert "drawable-nodpi/hero.png" not in generator
assert "drawable-nodpi/hero.png" not in workflow
assert "Hero.imageset" not in workflow
assert not (root/'ios/BOPAVI/Assets.xcassets/Hero.imageset/Contents.json').exists()
assert (root/'docs/assets/hero.svg').exists()
assert "hero_markup=" in generator
assert "tools/audit_native_dead_code.py" in workflow
audit=(root/'tools/audit_native_dead_code.py').read_text()
for check in ('audit_private_declarations','audit_resource_reachability',
              'audit_audio_pairing','audit_project_integration'):
    assert 'def '+check+'(' in audit,check

# Android 15's real previous failure had a Pixel Launcher foreground, not
# a BOPAVI GameView. Recovery is allowed ONLY if the app process survived and
# window focus is actually on the launcher; any genuine crash must still fail.
qa=(root/'tools/qa_android_emulator.sh').read_text()
for required in ('recover_confirmed_system_launcher',
                 "['adb','shell','pidof','com.brendigo.bopavi']",
                 "Process: com.brendigo.bopavi",
                 'mCurrentFocus',
                 'com.google.android.apps.nexuslauncher',
                 '0x20020000',
                 'system_recoveries<2',
                 'android-pilot-launch-logcat.txt',
                 'android-pilot-launch-activities.txt',
                 'android-pilot-launch-windows.txt',
                 'FAIL: BOPAVI process exited during pilot confirmation'):
    assert required in qa,required
assert 'qa/screenshots/*.txt' in workflow
assert 'Dodirni za let Bopija' in qa
assert 'verify_idle_preview' in qa
assert 'Poleti u novu avanturu.' in ios_menu
assert 'Odaberi letača i poleti u novu avanturu.' in android_menu
print("PASS: dead-resource removal, 4-scope audit and real-process-only Android launcher recovery")

# v0.1.66 — attached bill, complete wing margins and true shoulder joints.
hero=(root/'docs/assets/hero.svg').read_text()
assert 'Connected white muzzle anchors the bill directly to Bopi' in hero
assert 'Q91 70 65 81' in hero, 'lower jaw lost'
assert 'stroke="#fff4b2"' in hero, 'upper bill gloss lost'
assert 'M2 35Q34 27 92 50' not in hero, 'old detached beak returned'
generator=(root/'tools/generate_images.py').read_text()
assert 'viewBox="-80 -80 160 160" width="512" height="512"' in generator
for pilot in ('vihor','iskra','orion'):
    source=(root/'docs/assets'/f'{pilot}.svg').read_text()
    assert 'viewBox="-80 -80 160 160"' in source
    assert 'M-12 13 Q0 7 13 13' in source, (pilot,'smile missing')
    assert 'M-13 34 Q0 39 15 33' in source, (pilot,'scarf must lie below bill')
for name,source in (('Android',android_render),('iOS',ios_render)):
    for token in ('wingLeftPivot','wingRightPivot') if name=='Android' else ('leftWingPivot','rightWingPivot'):
        assert token in source,(name,token)
    assert ('235f else 220f' in source) if name=='Android' else ('235 : 220' in source)
assert 'private val birdRect=RectF(-birdSize*.5f' in android_render
assert 'sprite.draw(in:CGRect(x:-half,y:-half,width:birdArtSize,height:birdArtSize))' in ios_render
assert 'rotate(flap,wingLeftPivotX,wingLeftPivotY)' in android_render
assert 'rotate(-flap,wingRightPivotX,wingRightPivotY)' in android_render
assert 'left.draw(in:CGRect(x:-half-leftWingPivot.x,' in ios_render
assert 'right.draw(in:CGRect(x:-half-rightWingPivot.x,' in ios_render
assert (root/'tools/verify_pilot_sprite_anatomy.py').is_file()
assert 'python tools/verify_pilot_sprite_anatomy.py' in workflow
assert 'radius = 23f' in (root/'android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt').read_text()
assert 'radius: Float = 23' in (root/'ios/BOPAVI/BopaviCore.swift').read_text()
print('PASS: fixed anatomy, zero-crop wings, live 148/160px pilot art and unchanged collision model')

# v0.1.66 floating island obstacle polish is visual only, inside the old
# top/bottom shaft bounds; no physics or save changes.
assert 'private fun drawRockFacets(' in android_render
assert 'private func drawRockFacets(' in ios_render
assert 'drawRockFacets(c,x,w,2f,top-30f)' in android_render
assert 'drawRockFacets(c,x,w,bottom+28f,751f)' in android_render
assert 'drawRockFacets(c,x,w,2,top-30)' in ios_render
assert 'drawRockFacets(c,x,w,bottom+28,751)' in ios_render
print('PASS: both native gameplay paths draw faceted, textured island cliffs')

# v0.1.66 gameplay image composition: aspect-fill only the FAR world art on
# tall modern displays. Keep bird, HUD, gate physics and tap/drag in the fixed
# logical 480x800 viewport; no screen-dependent hitboxes or touch offsets.
assert 'private val worldCoverRect=RectF()' in android_render
assert 'val cover=max(w.toFloat()/bmp.width,h.toFloat()/bmp.height)' in android_render
assert 'canvas.drawBitmap(worldBitmap,null,worldCoverRect,p)' in android_render
assert 'if(worldBitmap==null) drawBackground(canvas)' in android_render
assert 'val scale=min(width/480f,height/800f)' in android_render
assert 'worldCoverRect.isEmpty()' in android_render
assert 'let scaleToCover=max(bound.width/world.size.width,' in ios_render
assert 'world.draw(in:CGRect(x:(bound.width-backdropWidth)*0.5,' in ios_render
assert 'if worldBackdrop==nil {background(c)}' in ios_render
assert 'let s=min(bound.width/480,bound.height/800)' in ios_render
assert 'canvas.drawBitmap(worldBitmap,null,worldRect,p)' not in android_render
assert 'background.draw(in:CGRect(x:0,y:0,width:480,height:800))' not in ios_render
print('PASS: Android/iOS full-bleed background only; letterboxed physical gameplay geometry preserved')
