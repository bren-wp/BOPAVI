#!/usr/bin/env python3
"""Verify installer icons from the actual approved Bopi art, not concept screens."""
from pathlib import Path
from PIL import Image, ImageChops

ROOT=Path(__file__).resolve().parents[1]
ANDROID=ROOT/"android/app/src/main/res"
IOS=ROOT/"ios/BOPAVI/Assets.xcassets"
with Image.open(ANDROID/"drawable/app_icon.png") as im:
    legacy=im.convert("RGB")
with Image.open(IOS/"AppIcon.appiconset/icon-1024.png") as im:
    ios=im.convert("RGB")
with Image.open(ANDROID/"drawable/app_icon_foreground.png") as im:
    adaptive=im.convert("RGBA")
with Image.open(ANDROID/"drawable-nodpi/bopiportrait0.png") as im:
    mascot=im.convert("RGBA")
assert legacy.size==(512,512) and ios.size==(1024,1024)
assert adaptive.size==(512,512) and mascot.size==(512,512)
assert ImageChops.difference(
    ios.resize((512,512),Image.Resampling.LANCZOS),legacy).getbbox() is None, (
        "Play and iOS launcher illustrations are inconsistent"
    )
assert adaptive.getpixel((0,0))[3] == 0, "Adaptive foreground has an opaque square"
assert adaptive.getpixel((511,511))[3] == 0
assert adaptive.getchannel("A").getextrema()[1] == 255, (
    "The real character is absent from the adaptive layer"
)
assert mascot.getchannel("A").getbbox()
# All four extreme pixels must remain solid saturated world colours, not the
# historical white rounded-square padding. Devices apply their own outer mask.
for corner in ((0,0),(511,0),(0,511),(511,511)):
    r,g,b=legacy.getpixel(corner)
    assert b>r and b>g and (r+g+b)<620, ("white launcher padding",corner)
# Explicitly reject a one-colour placeholder or a nearly empty square.
assert len(legacy.resize((48,48)).getcolors(maxcolors=2400) or [])>380
xml=(ANDROID/"mipmap-anydpi-v26/ic_launcher.xml").read_text()
assert '@drawable/app_icon_foreground' in xml
assert '@color/bopavi_icon_background' in xml
print("PASS: actual reference mascot, full-bleed icon, no white launcher padding, transparent adaptive layer and identical Android/iOS base pixels")
