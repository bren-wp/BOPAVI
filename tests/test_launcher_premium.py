#!/usr/bin/env python3
"""Verify installer icons from the actual approved Bopi art, not concept screens."""
from pathlib import Path
from PIL import Image, ImageOps

ROOT=Path(__file__).resolve().parents[1]
ANDROID=ROOT/"android/app/src/main/res"
with Image.open(ANDROID/"drawable/app_icon.png") as im:
    legacy=im.convert("RGB")
with Image.open(ANDROID/"drawable/app_icon_foreground.png") as im:
    adaptive=im.convert("RGBA")
with Image.open(ANDROID/"drawable-nodpi/bopiportrait0.png") as im:
    mascot=im.convert("RGBA")
assert legacy.size==(512,512)
assert adaptive.size==(512,512) and mascot.size==(512,512)
assert adaptive.getpixel((0,0))[3] == 0, "Adaptive foreground has an opaque square"
assert adaptive.getpixel((511,511))[3] == 0
assert adaptive.getchannel("A").getextrema()[1] == 255, (
    "The real character is absent from the adaptive layer"
)
assert mascot.getchannel("A").getbbox()
# The exact adaptive mascot must be the approved Bopi portrait — no generic
# vector face, fake goggles, concentric ring or decorative repaint.
trimmed=mascot.crop(mascot.getchannel("A").getbbox())
expected_bird=ImageOps.contain(trimmed,(735,735),method=Image.Resampling.LANCZOS)
expected=Image.new("RGBA",(1024,1024),(0,0,0,0))
expected.alpha_composite(expected_bird,((1024-expected_bird.width)//2,
                                      (1024-expected_bird.height)//2))
expected=expected.resize((512,512),Image.Resampling.LANCZOS)
assert adaptive.tobytes()==expected.tobytes(), (
    "Launcher foreground must use unchanged approved Bopi portrait pixels")
# All four extreme pixels must remain solid saturated world colours, not the
# historical white rounded-square padding. Devices apply their own outer mask.
for corner in ((0,0),(511,0),(0,511),(511,511)):
    r,g,b=legacy.getpixel(corner)
    assert b>r and b>g and (r+g+b)<620, ("white launcher padding",corner)
# Explicitly reject a one-colour placeholder or a nearly empty square.
assert len(legacy.resize((48,48)).getcolors(maxcolors=2400) or [])>380
xml=(ANDROID/"mipmap-anydpi-v26/ic_launcher.xml").read_text()
assert '@drawable/app_icon_foreground' in xml
assert '@drawable/app_icon_background' in xml
with Image.open(ANDROID/"drawable/app_icon_background.png") as im:
    assert im.size == (512,512) and im.convert('RGB').getpixel((0,0)) != (255,255,255)
print("PASS: actual reference mascot, full-bleed icon, no white launcher padding, transparent adaptive layer for Android")
