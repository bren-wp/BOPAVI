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
assert "def cinematic_detail(flat: Image.Image)" in d
assert "Random(9411672)" in d and "ImageFilter.GaussianBlur" in d
assert "return cinematic_detail(Image.alpha_composite(" in d
for item in ("castle(102,426,.66)","castle(773,487,.58)","enrich_background(source)"):
    assert item in d,item
assert "R.drawable.homeback" in home
assert "R.drawable.loadingback" in loading
assert "from paint_loading_reference import paint_loading_reference" in g
assert "loading_art=paint_loading_reference(home_art)" in g
assert "loading_art.save(a/'drawable-nodpi/loadingback.png',optimize=True)" in g
paint=(root/"tools/paint_loading_reference.py").read_text()
for marker in ("def paint_loading_reference(base: Image.Image)",
               "castle(72,257,1.14)","castle(834,377,.65)",
               "cliff(50,253,254,420,True)","Random(9411672 + 54)"):
    assert marker in paint,marker
for unwanted in ("RETRY","HOME BUTTON","Loading...","BOPAVI"):
    # File mentions the brand in comments, but must not draw UI labels.
    assert f'draw.text("{unwanted}"' not in paint
assert "wallet(\"COINS\",progress.coins()" in home
assert "HomeWingActor(host" in home and "setOnClickListener{onClick()}" in home
assert "image(canvas, brand, logoRect)" in loading
print("PASS: source fantasy backdrop is offline, independent of live BOPAVI UI")
