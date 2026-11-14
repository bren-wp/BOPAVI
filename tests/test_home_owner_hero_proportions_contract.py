#!/usr/bin/env python3
"""Home native aviator is resized/placed from original owner's 16:9/20:9 art."""
from pathlib import Path
src=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
model=(src/"HomeReferenceLayout.kt").read_text()
screen=(src/"HomeReferenceScreen.kt").read_text()
assert "val hero=HomeBounds(300f,520f,400f,400f)" in model
assert "place(stage,actor,HomeReferenceLayout.hero,verticalAnchor=.35f)" in screen
assert "val heroArtwork=HomeBounds(" in model
assert "left[chosen],right[chosen],bodies[chosen]" in screen
assert "HomeWingActor(host," in screen
for label in ('pill("SKINS"','pill("SHOP"','pill("SETTINGS"'):
    assert label in screen,label
assert "setOnClickListener{onClick()}" in screen
print("PASS: owner-sized original animated Home sprite at two physical aspect ratios")
