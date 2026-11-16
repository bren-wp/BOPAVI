#!/usr/bin/env python3
"""GAME OVER owner's missing live-vector brand header; preserve real retry/stats."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
s=(p/"GameOverScreen.kt").read_text()
assert 'setImageResource(R.drawable.logo)' in s
assert '},260f,6f,421f,116f,referenceAligned=true)' in s
assert 'GameOverCrown(host)' in s
assert 'ResultEmblemLabel(host,"GAME"' in s
assert 'ResultEmblemLabel(host,"OVER"' in s
assert 'GameOverSeatedBopi(host)' in s
assert 'statRow("Distance",stats.distance' in s
assert 'stage.place(action("RETRY",true,onRetry)' in s
assert 'stage.place(action("HOME",false,onHome)' in s
assert 'setOnClickListener{onClick()}' in s
print("PASS: real Game Over branded title + original native stats/Retry/Home")
