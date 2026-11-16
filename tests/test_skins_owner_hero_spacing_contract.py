#!/usr/bin/env python3
"""Skins reference hero geometry must clear both native carousel arrow targets."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
l=(src/"PilotSelectionLayout.kt").read_text(encoding="utf-8")
s=(src/"PilotSelectionScreen.kt").read_text(encoding="utf-8")
model=(src/"PilotSelectionModel.kt").read_text(encoding="utf-8")
assert "const val heroLeft=157f" in l
assert "const val heroTop=260f" in l
assert "const val heroSize=626f" in l
assert "heroLeft>=138f && heroLeft+heroSize<=803f" in l
assert "PilotSelectionLayout.heroLeft,PilotSelectionLayout.heroTop" in s
assert "PilotSelectionLayout.heroSize,PilotSelectionLayout.heroSize" in s
assert 'cycleArrow("‹","Previous hero")' in s
assert 'cycleArrow("›","Next hero")' in s
assert "setOnClickListener{chooseOrUnlock(index)}" in s
assert "PilotSelectionModel.locked(index,orionOwned)" in s
assert "R.drawable.bopiportrait8" in s
assert "fun safeChoice(" in model
print("PASS: genuine Skins hero smaller, arrow clearance + stored selection preserved")
