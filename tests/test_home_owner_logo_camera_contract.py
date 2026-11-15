#!/usr/bin/env python3
"""Regression: Home owner camera correction retains 4 live controls and owner assets."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
layout=(src/"HomeReferenceLayout.kt").read_text(encoding="utf-8")
screen=(src/"HomeReferenceScreen.kt").read_text(encoding="utf-8")
assert "val logo=HomeBounds(194f,48f,552f,229f)" in layout
assert "if(coins.overlaps(logoArtwork))return false" in layout
assert "val hero=HomeBounds(300f,520f,400f,400f)" in layout
assert "val actions=listOf(skins,shop,settings)" in layout
assert 'val play=HomeBounds(304f,899f,333f,115f)' in layout
assert "setImageResource(R.drawable.logo)" in screen
assert "setImageResource(R.drawable.homeback)" in screen
assert "walletRail.onRowBottomChanged={rowBottom->" in screen
for name in ("PLAY","SKINS","SHOP","SETTINGS"):
    assert screen.count('pill("'+name+'"')==1,name
assert "setOnClickListener{onClick()}" in screen
print("PASS: Home logo alignment moves closer to owner camera; four live callbacks retained")
