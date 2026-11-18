#!/usr/bin/env python3
"""Regression for the P2 Game Over logo occlusion review on PR #244.

The decorative crown overlaps the owner-aligned logo rectangle on purpose,
but must be drawn BEFORE the complete wordmark, never over the letters.
This tests native child ordering only, not exact screenshot RGB parity.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
source = (root / "android/app/src/main/java/com/brendigo/bopavi/GameOverScreen.kt").read_text()
crown = 'stage.place(GameOverCrown(host),431f,53f,79f,76f,'
wordmark = 'setImageResource(R.drawable.logo)'
assert source.count(crown) == 1, "Missing or duplicated Game Over 3D crown"
assert source.count(wordmark) == 1, "Missing or duplicated Game Over wordmark"
assert source.index(crown) < source.index(wordmark), (
    "3D crown would paint over BOPAVI wordmark on every viewport"
)
# Preserve the live controls; a reference image must never replace them.
assert 'stage.place(action("RETRY",true,onRetry)' in source
assert 'stage.place(action("HOME",false,onHome)' in source
assert 'statRow("Distance",stats.distance' in source
assert re.search(r'stage\.place\(ImageView\(host\)\.apply\s*\{\s*'
                 r'setImageResource\(R\.drawable\.logo\)', source)
print("PASS: Game Over 3D crown is behind the native BOPAVI logo and live actions remain")
