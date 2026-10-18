"""V5 simplified game contracts: four screens, stable offline saves and native actions."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
A=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
I=(ROOT/"ios/BOPAVI/GameController.swift").read_text()
AC=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/GameSimulation.kt").read_text()
IC=(ROOT/"ios/BOPAVI/BopaviCore.swift").read_text()
MANIFEST=(ROOT/"android/app/src/main/AndroidManifest.xml").read_text()
PLIST=(ROOT/"ios/BOPAVI/Info.plist").read_text()
WORKFLOW=(ROOT/".github/workflows/native-ci.yml").read_text()

def section(source,start,end=None):
    assert source.count(start)==1, start
    body=source.split(start,1)[1]
    return body.split(end,1)[0] if end else body

assert 'android:allowBackup="false"' in MANIFEST
assert '<uses-permission android:name="android.permission.INTERNET"' not in MANIFEST
assert 'UIInterfaceOrientationPortrait' in PLIST
a_boot=section(A,"    override fun onCreate(state: Bundle?) {","    private fun showNativeView(root: View) {")
i_boot=section(I,"    override func viewDidLoad(){","    deinit ")
assert "showHome()" in a_boot and "showHome()" in i_boot
assert "showSplash()" not in A and "showSplash()" not in I

a_home=section(A,"    private fun showHome(){","    private fun showPremiumPause(")
i_home=section(I,"    private func showHome(){","    private func showPremiumPause(")
for label,home in (("android",a_home),("iOS",i_home)):
    assert "IGRAJ" in home, label
    assert ("BopiPortrait0" in home or "portraitDrawableIds[0]" in home), label
    assert "bestPoints()" not in home, label
    assert "startSceneryDrift" in home, label
    for forbidden in ("showWorlds(", "showSettings(", "showProfile(", "showPerks(", "showCollection(", "showLeaderboard("):
        assert forbidden not in home,(label,forbidden)
assert a_home.count("action(content,")==1
assert 'UIImage(named:"BopiPortrait0")' in i_home
assert "FitViewportColumn(this)" in a_home

a_pause=section(A,"    private fun showPremiumPause(","    private fun startGame(")
i_pause=section(I,"    private func showPremiumPause(","    private func startGame(")
for pause in (a_pause,i_pause):
    assert all(label in pause for label in ("NASTAVI","PONOVO","IZLAZ"))
    assert not any(s in pause for s in ("showSettings(", "showPerks(", "showWorlds("))
assert "game.paused=false" in a_pause
assert "close(true)" in i_pause

a_play=section(A,"    private fun startGame(","    private fun showResult(")
i_play=section(I,"    private func startGame(","    private func showResult(")
for play in (a_play,i_play):
    assert "GameSimulation(" in play
    assert "onFinished" in play
    assert "consumePerks()" not in play
    assert "completeLevel(" not in play
    assert "recordLevelStars(" not in play
    assert "skinIndex:0" in play or "progress.lessMotion(),0" in play

a_result=section(A,"    private fun showResult(","    override fun onBackPressed()")
i_result=section(I,"    private func showResult(")
for result in (a_result,i_result):
    assert "progress.recordRun(g)" in result
    assert "bestPoints()" in result
    assert "g.score()" in result
    assert "g.coins" not in result
    assert "PONOVO" in result and "POČETNA" in result and "showHome()" in result
for source in (A,I):
    for removed in ("showWorlds(", "showLevels(", "showShop(", "showProfile(", "showPerks(", "showCollection(", "showAchievements(", "showLeaderboard(", "showSettings(", "showPilotPicker("):
        assert removed not in source, removed

assert "if(!endless && gate.coin" in AC and "if !endless && gate.coin" in IC
assert "foodCollected" in AC and "foodCollected" in IC
assert "collectedFood" in AC and "collectedFood" in IC
assert "g.foodCollected" in a_result and "g.foodCollected" in i_result
assert "if(!endless && gate.power" in AC and "if !endless && gate.power" in IC
assert "source-audit:" in WORKFLOW and "python tools/audit_native_dead_code.py" in WORKFLOW
print("PASS: native four-view gameplay, original Bopi, non-reward endless score and safe persistence")

# V6 improvements: food records and screen readers must use genuine saved data.
for progress in ("android/app/src/main/java/com/brendigo/bopavi/ProgressStore.kt",
                 "ios/BOPAVI/ProgressStore.swift"):
    code=(ROOT/progress).read_text()
    assert "endless_food_best" in code and "bestFood()" in code
for source in (A,I):
    assert "progress.bestFood()" in source and "NAJVIŠE HRANE" in source
for path in ("android/app/src/main/java/com/brendigo/bopavi/GameView.kt",
             "ios/BOPAVI/GameCanvas.swift"):
    gameview=(ROOT/path).read_text()
    assert "cachedScore" in gameview and "cachedFood" in gameview
    assert "lastAccessibilityScore" in gameview and "lastAccessibilityFood" in gameview
print("PASS: berry personal best, two-line result record and cached in-flight HUD")
