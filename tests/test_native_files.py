"""BOPAVI simplified premium release: explicit Android/iOS source contracts.

The removed menu journey is intentionally not tested as a required navigation
feature. Core physics, wallet migration, collisions and sprite parity retain
their separate Kotlin/Swift native tests and GitHub jobs.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
ANDROID = (ROOT/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
IOS = (ROOT/"ios/BOPAVI/GameController.swift").read_text()
MANIFEST = (ROOT/"android/app/src/main/AndroidManifest.xml").read_text()
GRADLE = (ROOT/"android/app/build.gradle.kts").read_text()
PBX = (ROOT/"ios/BOPAVI.xcodeproj/project.pbxproj").read_text()
PLIST = (ROOT/"ios/BOPAVI/Info.plist").read_text()
WORKFLOW = (ROOT/".github/workflows/native-ci.yml").read_text()

def between(src,first,last):
    assert src.count(first)==1, first
    a=src.index(first)+len(first)
    b=src.index(last,a)
    return src[a:b]

assert 'versionCode = 122' in GRADLE and 'versionName = "0.2.17"' in GRADLE
assert PBX.count('CURRENT_PROJECT_VERSION = 122;')==2
assert PBX.count('MARKETING_VERSION = 0.2.17;')==2
assert 'android:allowBackup="false"' in MANIFEST
assert '<uses-permission android:name="android.permission.INTERNET"' not in MANIFEST
assert 'UIInterfaceOrientationPortrait' in PLIST
assert "UIApplicationExitsOnSuspend" not in PLIST

a_boot = between(ANDROID,"    override fun onCreate(state: Bundle?) {",
                 "    private fun showNativeView(root: View) {")
assert 'progress=ProgressStore(this)' in a_boot
assert 'sound=Soundscape(this)' in a_boot
assert 'showHome()' in a_boot
assert 'showSplash()' not in a_boot
assert 'private fun showSplash()' not in ANDROID
assert 'DispatchQueue.main.asyncAfter' not in IOS.split('private func showHome()',1)[0]
# Native iOS launches using the system-managed neutral background.
assert '<key>UIImageName</key><string>LaunchArt</string>' not in PLIST

a_home = between(ANDROID,"    private fun showHome(){","    private fun showWorlds(){")
i_home = between(IOS,"    private func showHome(){","    private func showWorlds(){")
for label,home in (("Android",a_home),("iOS",i_home)):
    for forbidden in ("showProfile()", "showWorlds()", "showPilotPicker(",
                      "showSettings()", "showPerks(", "showCollection()",
                      "showAchievements(", "showLeaderboard()"):
        assert forbidden not in home, (label,"legacy navigation exposed",forbidden)
    assert "bestPoints()" in home
    assert "showHome()" not in home
assert "startGame(0,1L)" in a_home
assert "self.startGame(0,1)" in i_home
assert "portraitDrawableIds[progress.skin()]" in a_home
assert 'UIImage(named:"BopiPortrait\\(progress.skinIndex())")' in i_home
assert "setImageResource(R.drawable.logo)" in a_home
assert 'UIImage(named:"Logo")' in i_home
assert "FitViewportColumn(this)" in a_home
assert "safeAreaLayoutGuide" in IOS
assert 'one accessible action' in a_home

a_pause = between(ANDROID,"    private fun showPremiumPause(", "    private fun startGame(")
i_pause = between(IOS,"    private func showPremiumPause(", "    private func startGame(")
for label,pause in (("Android",a_pause),("iOS",i_pause)):
    for action in ("NASTAVI","PONOVO","IZLAZ"):
        assert action in pause, (label,action)
    assert "showSettings()" not in pause
    assert "showPerks(" not in pause
    assert "showWorlds()" not in pause
    assert "pauseSettings" not in pause
assert "game.paused=false" in a_pause
assert "close(true)" in i_pause

a_result = between(ANDROID,"    private fun showResult(", "    private fun showPerks()")
i_result = between(IOS,"    private func showResult(", "    private func showPerks()")
for label,result in (("Android",a_result),("iOS",i_result)):
    assert "progress.recordRun(g)" in result
    assert "g.score()" in result and "g.coins" in result
    assert "g.totalPassed" in result and "bestPoints()" in result
    assert "showHome()" in result
    assert "showLeaderboard()" not in result
    assert "showWorlds()" not in result
    assert "showPerks(" not in result
    assert "claimBonusCoins()" not in result
assert "startGame(0,1L)" in a_result
assert "self.startGame(0,1)" in i_result
assert a_result.index("previousRecord") < a_result.index("progress.recordRun(g)")
assert i_result.index("previousRecord") < i_result.index("progress.recordRun(g)")

# Gameplay stays real, not a static composite drawn on the menu.
a_game = between(ANDROID,"    private fun startGame(", "    private fun showResult(")
i_game = between(IOS,"    private func startGame(", "    private func showToast(")
assert "GameSimulation(" in a_game and "onFinished=" in a_game
assert "GameSimulation(" in i_game and "onFinished" in i_game
assert "progress.consumePerks()" in a_game and "progress.consumePerks()" in i_game
assert "recordLevelStars" in a_game and "recordLevelStars" in i_game

assert "source-audit:" in WORKFLOW
assert "Android APK + AAB" in WORKFLOW
assert "iOS simulator + unsigned device build" in WORKFLOW
assert "python tools/audit_native_dead_code.py" in WORKFLOW
assert "python tests/test_release_contract.py" in WORKFLOW
print("PASS: simplified one-tap Home, live infinite flight, safe saved results, minimal Pause on Android and iOS")
