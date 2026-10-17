"""Streamlined BOPAVI navigation must not regress to a multistep menu maze."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
A=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
I=(ROOT/"ios/BOPAVI/GameController.swift").read_text()
def segment(source,a,b):
    i=source.index(a);j=source.index(b,i+len(a))
    return source[i:j]
a_home=segment(A,"    private fun showHome(){","    private fun showWorlds(){")
i_home=segment(I,"    private func showHome(){","    private func showWorlds(){")
for home in (a_home,i_home):
    assert "IGRAJ" in home
    assert "bestPoints()" in home
    assert "startSceneryDrift" in home
    assert "showWorlds()" not in home
    assert "showSettings()" not in home
    assert "showPerks(" not in home
    assert "showProfile()" not in home
    assert "showPilotPicker(" not in home
assert "startGame(0,1L)" in a_home
assert "self.startGame(0,1)" in i_home
a_result=segment(A,"    private fun showResult(","    private fun showPerks()")
i_result=segment(I,"    private func showResult(","    private func showPerks()")
for result in (a_result,i_result):
    assert "progress.recordRun(g)" in result
    assert "PONOVO" in result and "POČETNA" in result
    assert "showHome()" in result
    assert "showWorlds()" not in result
# Stored settings and legacy scores are deliberately retained internally for
# migration, but these screens have no live Home/Result/Pause entry points.
print("PASS: one-tap endless BOPAVI with no commerce/level/profile detours")
