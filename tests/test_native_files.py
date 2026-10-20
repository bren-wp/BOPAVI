"""Android-only regression contracts for the four live game screens."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
code=ROOT/"android/app/src/main/java/com/brendigo/bopavi"
a=(code/"MainActivity.kt").read_text()
simulation=(code/"GameSimulation.kt").read_text()
view=(code/"GameView.kt").read_text()
store=(code/"ProgressStore.kt").read_text()
manifest=(ROOT/"android/app/src/main/AndroidManifest.xml").read_text()
flow=(ROOT/".github/workflows/native-ci.yml").read_text()
assert not (ROOT/"ios").exists()
assert 'android:allowBackup="false"' in manifest
assert '<uses-permission android:name="android.permission.INTERNET"' not in manifest
def section(source,start,end=None):
    assert source.count(start)==1,start
    fragment=source.split(start,1)[1]
    return fragment.split(end,1)[0] if end else fragment
boot=section(a,"    override fun onCreate(state: Bundle?) {","    private fun showNativeView(root: View) {")
assert "showHome()" in boot and "showSplash()" not in a
home=section(a,"    private fun showHome(){","    private fun showPremiumPause(")
assert "IGRAJ" in home and "FitViewportColumn(this)" in home
assert "POLITIKA PRIVATNOSTI" in home and "showPrivacyNotice()" in home
assert 'contentDescription="Pročitaj politiku privatnosti"' in home
assert "portraitDrawableIds[0]" in home
pause=section(a,"    private fun showPremiumPause(","    private fun startGame(")
assert all(label in pause for label in ("NASTAVI","PONOVO","IZLAZ"))
assert "game.paused=false" in pause
play=section(a,"    private fun startGame(","    private fun showResult(")
assert "GameSimulation(" in play and "onFinished" in play
result=section(a,"    private fun showResult(","    override fun onBackPressed()")
assert all(label in result for label in ("progress.recordRun(g)","bestPoints()","g.score()","g.foodCollected","PONOVO","POČETNA","backAction(panel"))
for removed in ("showWorlds(","showLevels(","showShop(","showProfile(","showPerks(","showCollection(","showAchievements(","showLeaderboard(","showSettings(","showPilotPicker("):
    assert removed not in a,removed
assert "BerryCollision.touched(" in simulation and "collectedFood[i]=true" in simulation
assert "if(endless && gate.coin && !collectedFood[i])" in simulation
assert "if(!endless && gate.coin" in simulation
assert "foodCollected" in simulation and "sweptBodyHitsPillar" in simulation
assert all(symbol in store for symbol in ("endless_food_best","bestFood()","recordRun"))
assert "progress.bestFood()" in a and "NAJVIŠE HRANE" in a
assert all(symbol in view for symbol in ("cachedScore","cachedFood","lastAccessibilityScore","lastAccessibilityFood"))
assert "source-audit:" in flow and "python tools/audit_native_dead_code.py" in flow
print("PASS: Android home/flight/pause/results, save compatibility, fruit and HUD contracts")
