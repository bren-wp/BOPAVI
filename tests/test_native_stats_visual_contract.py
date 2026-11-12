#!/usr/bin/env python3
"""Run result and pause remain accessible; no standalone Records/Leaderboard pages."""
from pathlib import Path
src=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
a=(src/"MainActivity.kt").read_text()
icons=(src/"BopaviUiIconDrawable.kt").read_text()
result=a.split("private fun showResult(g:GameSimulation){",1)[1].split("override fun onBackPressed()",1)[0]
pause=a.split("private fun showPremiumPause(game:GameView){",1)[1].split("private fun startGame(){",1)[0]
for s in ('progress.recordRun(g)','recordSummaryRow("NAJBOLJI REZULTAT"',
          'recordSummaryRow("NAJVIŠE HRANE"','backAction(panel,"POČETNA"'):
    assert s in result,s
for s in ('NASTAVI','PONOVO','IZLAZ','setCompoundDrawablesRelativeWithIntrinsicBounds('):
    assert s in pause,s
for s in ('private fun showLocalRecords(){','LeaderboardScreen(',
          'selectedScreen="records"'):
    assert s not in a,s
assert "Kind.PAUSE" in a
for kind in ("GATE","FLIGHT","WING"):
    assert f"Kind.{kind} ->" in icons
print("PASS: native run result still works without obsolete records destination")
