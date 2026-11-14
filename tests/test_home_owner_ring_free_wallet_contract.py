#!/usr/bin/env python3
"""Current Home design has no metallic ring trail or center coin pill."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
layout=(src/"HomeReferenceLayout.kt").read_text()
orbit=(src/"HomeOrbitDecoration.kt").read_text()
home=(src/"HomeReferenceScreen.kt").read_text()
assert "val coins=HomeBounds(708f,21f,220f,64f)" in layout
assert "val coins=HomeBounds(457f" not in layout
assert "private fun ring(" not in orbit
assert "ring(canvas," not in orbit
assert "sparkle(canvas,729f,603f,14f)" in orbit
assert "HomeOrbitDecoration(host)" in home
assert 'val actions=listOf(skins,shop,settings)' in layout
assert 'pill("SKINS"' in home and 'pill("SHOP"' in home and 'pill("SETTINGS"' in home
print("PASS: owner wallet at top-right and ring-free Home without deleting real navigations")
