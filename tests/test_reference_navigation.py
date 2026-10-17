#!/usr/bin/env python3
"""Four-screen BOPAVI reference navigation and source-level QA.

No old menu galleries or live currency/shop widgets are permitted. Visual
pixel-parity still requires authentic device screenshots.
"""
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
android=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
ios=(ROOT/"ios/BOPAVI/GameController.swift").read_text()

def section(src,start,end):
    return src.split(start,1)[1].split(end,1)[0]

home_a=section(android,"private fun showHome(){","private fun showPremiumPause(")
home_i=section(ios,"private func showHome(){","private func showPremiumPause(")
for home in (home_a,home_i):
    assert "IGRAJ" in home
    assert "showWorlds()" not in home
    assert "showProfile()" not in home
    assert "showSettings()" not in home
    assert "showPerks(" not in home
    assert "showPilotPicker(" not in home
assert "FitViewportColumn" in home_a
assert "setOnApplyWindowInsetsListener" in home_a
assert "R.drawable.logo" in home_a
assert 'UIImage(named:"Logo")' in home_i
assert 'UIImage(named:"BopiPortrait\\(progress.skinIndex())")' in home_i
assert "safeAreaLayoutGuide" in ios

a_pause=section(android,"private fun showPremiumPause(","private fun startGame(")
i_pause=section(ios,"private func showPremiumPause(","private func startGame(")
assert 'game.paused=false' in a_pause
assert 'canvas?.paused=false' in i_pause
for code in (a_pause,i_pause):
    assert "PONOVO" in code
    assert "IZLAZ" in code
    assert "showHome()" in code
    assert "showSettings()" not in code

a_result=section(android,"private fun showResult(","private fun navigateBack()")
i_result=ios.split("private func showResult(",1)[1]
for code in (a_result,i_result):
    assert "progress.recordRun(g)" in code
    assert "bestPoints()" in code
    assert "showHome()" in code
    assert "startGame(" in code
    assert "showWorlds()" not in code and "showPerks(" not in code
print("PASS: four-screen simple BOPAVI navigation and legacy record preservation")
