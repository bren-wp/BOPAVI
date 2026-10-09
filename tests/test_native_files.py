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
    for banned in ['BESKONAČNI','SVJETOVI','DNEVNA NAGRADA','POSTAVKE','1.048.576','8.388.608']:
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
print('PASS: native source inventory, 28 audio assets, single-action home, manifest, icons, no web engine/network permission')

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
assert 'levelOrigin=distance+300f-next.gates.first().x' in andr
assert 'levelOrigin=distance+300-next.gates[0].x' in swift
assert 'distance = 0f; passed = 0' not in andr
assert 'distance=0;passed=0' not in swift

# Keep fixed body raster dimensions; only separated wings may rotate.
assert 'c.scale(1f,squash)' not in (root/'android/app/src/main/java/com/brendigo/bopavi/GameView.kt').read_text()
assert 'c.scaleBy(x:1,y:1+phase*0.035)' not in (root/'ios/BOPAVI/GameCanvas.swift').read_text()

# Premium art and illustrated results must ship on Android/iOS.
assert '#ff871b' in (root/'docs/assets/logo.svg').read_text()
assert 'id="wood"' in (root/'docs/assets/logo.svg').read_text()
assert 'id="lens"' in (root/'docs/assets/hero.svg').read_text()
assert 'def island(' in (root/'tools/generate_images.py').read_text()
assert 'LET ZAVRŠEN!' in (root/'android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt').read_text()
assert 'LET ZAVRŠEN!' in (root/'ios/BOPAVI/GameController.swift').read_text()
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
    assert 'Razvoj: Brendigo' in source

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
    assert "DODIRNI Ⅱ ZA NASTAVAK" in canvas
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
