#!/usr/bin/env python3
"""v0.2.17: strict minimal native UI contract (other physics/save suites remain).

Only Home, endless gameplay, Pause and Result may be reachable. Real data must
survive migration; never replace game state with reference-art screenshots.
"""
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
android=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
ios=(ROOT/"ios/BOPAVI/GameController.swift").read_text()
view=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
canvas=(ROOT/"ios/BOPAVI/GameCanvas.swift").read_text()
save_android=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt").read_text()
save_ios=(ROOT/"ios/BOPAVI/ProgressStore.swift").read_text()
manifest=(ROOT/"android/app/src/main/AndroidManifest.xml").read_text()
gradle=(ROOT/"android/app/build.gradle.kts").read_text()
project=(ROOT/"ios/BOPAVI.xcodeproj/project.pbxproj").read_text()

for code,language in ((android,"Android"),(ios,"iOS")):
    assert "showHome(" in code and "showPremiumPause(" in code
    assert "startGame(" in code and "showResult(" in code,language
    # Deleting only menu buttons is insufficient: the abandoned screen
    # controllers must disappear from both production binaries.
    for unused in ("showWorlds(","showLevels(","showPilotPicker(","showSettings(",
                   "showSettingsDetails(","showProfile(","showPerks(","showCollection(",
                   "showLeaderboard(","showAchievements(","showLanguagePicker("):
        assert unused not in code,(language,"dead screen",unused)
    assert "endless" in code,language
    assert "progress.recordRun(" in code,language
    assert "PREMIUM" not in code or "WORLDS" not in code,language

a_home=android.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
i_home=ios.split("private func showHome(){",1)[1].split("private func showPremiumPause(",1)[0]
for code in (a_home,i_home):
    assert code.count("▶  IGRAJ")==1
    for dead in ("SVJETOVI","POSTAVKE","TRGOVINA","LEVELI"):
        assert dead not in code,dead
assert "startGame(progress.chosenWorld(),1L)" in a_home
assert "self.startGame(self.progress.chosenWorld(),1)" in i_home
assert "R.drawable.logo" in a_home and "R.drawable.homeback" in a_home
assert 'UIImage(named:"Logo")' in i_home and 'UIImage(named:"HomeArt")' in i_home
assert "FitViewportColumn(this)" in a_home
assert "safeAreaLayoutGuide" in ios and "displayCutout()" in a_home

a_start=android.split("private fun startGame(",1)[1].split("private fun showResult(",1)[0]
i_start=ios.split("private func startGame(",1)[1].split("private func showResult(",1)[0]
assert "LevelEngine.createStream(" in a_start and ",true,0,0f," in a_start
assert "BopaviCore.createStream(" in i_start and "endless:true" in i_start
for code in (a_start,i_start):
    assert "showPremiumPause(" in code
    assert "consumePerks(" not in code and "completeLevel(" not in code
    assert "onFinished" in code

a_pause=android.split("private fun showPremiumPause(",1)[1].split("private fun startGame(",1)[0]
i_pause=ios.split("private func showPremiumPause(",1)[1].split("private func startGame(",1)[0]
for code in (a_pause,i_pause):
    for button in ("NASTAVI","PONOVO","IZLAZ"):
        assert button in code,("Pause",button)
    assert "showSettings()" not in code
    assert "Level " not in code and "LEVEL " not in code
    assert "sound.pause()" not in code or "game.paused" in code or "canvas?.paused" in code

a_result=android.split("private fun showResult(",1)[1].split("private fun navigateBack()",1)[0]
i_result=ios.split("private func showResult(",1)[1]
for code in (a_result,i_result):
    for stat in ("REZULTAT","PROLAZI","NAJBOLJI REZULTAT"):
        assert stat in code,stat
    assert "progress.recordRun(" in code
    assert "PONOVO" in code and "POČETNA" in code
    for dead in ("showPerks(","showWorlds(","showLeaderboard("):
        assert dead not in code,dead

# OS launch artwork is the only splash. No runtime scene or fake loading bar.
assert "showSplash(" not in android+ios
assert "UČITAVANJE..." not in android+ios
assert 'android:theme="@style/BopaviLaunchTheme"' in manifest
assert "<string>LaunchArt</string>" in (ROOT/"ios/BOPAVI/Info.plist").read_text()
assert "android.permission.INTERNET" not in manifest
assert 'android:allowBackup="false"' in manifest

# Existing persisted records and settings remain readable; no destructive
# migration, wiped defaults, changed key names, or automatic shop spending.
for save in (save_android,save_ios):
    for token in ("endless_best","coins","wins","deaths","skin_index",
                  "owned_mask","chosen_world","recordRun("):
        assert token in save,("legacy data",token)
    assert "clear()" not in save or "prefs.edit().clear()" not in save
assert "drawRootedCliffBases(" in view and "drawRootedCliffBases(" in canvas
assert 'versionCode = 122' in gradle and 'versionName = "0.2.17"' in gradle
assert project.count("CURRENT_PROJECT_VERSION = 122;")==2
assert project.count("MARKETING_VERSION = 0.2.17;")==2
print("PASS: 4 reachable native screens, no runtime splash, real endless flight, retained scores and safe-area art")
