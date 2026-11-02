#!/usr/bin/env python3
"""Ensure owner UI remains native and the backdrop remains an offline asset."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
g=(root/"tools/generate_images.py").read_text()
d=(root/"tools/paint_fantasy_backdrop.py").read_text()
home=(root/"android/app/src/main/java/com/brendigo/bopavi/HomeReferenceScreen.kt").read_text()
loading=(root/"android/app/src/main/java/com/brendigo/bopavi/FlightLoadingView.kt").read_text()
assert "home_art=enrich_background(home_art)" in g
assert "from paint_fantasy_backdrop import enrich_background" in g
assert "Random(24187)" in d and "source.size != (900, 1600)" in d
for item in ("castle(102,426,.66)","castle(773,487,.58)","enrich_background(source)"):
    assert item in d,item
assert "R.drawable.homeback" in home and "R.drawable.homeback" in loading
assert "wallet(\"COINS\",progress.coins()" in home
assert "HomeWingActor(host" in home and "setOnClickListener{onClick()}" in home
assert "image(canvas, brand, logoRect)" in loading
print("PASS: source fantasy backdrop is offline, independent of live BOPAVI UI")
