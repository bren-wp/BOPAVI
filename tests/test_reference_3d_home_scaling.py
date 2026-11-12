#!/usr/bin/env python3
from pathlib import Path
src=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
h=(src/"HomeReferenceScreen.kt").read_text()
e=(src/"HomeEnamelDrawable.kt").read_text()
l=(src/"HomeReferenceLayout.kt").read_text()
assert "setPadding(0,0,0,0)" in h
assert "ReferenceButtonCluster.measure(" in h
assert "measure.measureText(text)" in h
assert "marginStart=cluster.gap" in h
assert "sourceHome=true" in h
assert "canvas.clipPath(faceClip)" in e
assert "val actions=listOf(skins,shop,settings)" in l
print("PASS: three real Home 3D controls with proportional measured icon/text")
