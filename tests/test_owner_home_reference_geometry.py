#!/usr/bin/env python3
"""Owner-supplied Home reference geometry (not a zero-pixel visual PASS).

Pins native button slots to the real 1080px WebP assets from the owner's ZIP.
No screenshots are synthesized, scaled or substituted.
"""
from pathlib import Path
import hashlib
import re
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"android/app/src/main/java/com/brendigo/bopavi"
layout=(SRC/"HomeReferenceLayout.kt").read_text()
home=(SRC/"HomeReferenceScreen.kt").read_text()
icons=(SRC/"HomeHeroIconDrawable.kt").read_text()
screen_names=["skins","shop","settings"]

def bounds(name):
    match=re.search(r"val "+name+r"=HomeBounds\((\d+)f,(\d+)f,(\d+)f,(\d+)f\)",layout)
    assert match,f"Missing literal new-owner Home bounds: {name}"
    return tuple(map(float,match.groups()))

base={n:bounds(n) for n in screen_names}
assert base["skins"][1]+base["skins"][3]<base["shop"][1]
assert base["shop"][1]+base["shop"][3]<base["settings"][1]
assert all(abs(x+w/2-470.5)<=3 for x,y,w,h in base.values())
assert 'actionBounds(' in home and 'verticalAnchor=.5f' in home
assert '0xff79ecff.toInt()' in home  # blue skins
assert '0xfffff095.toInt()' in home  # gold shop
assert '0xffee9fff.toInt()' in home  # violet settings
assert 'setImageDrawable(HomeHeroIconDrawable(kind,dp(52)))' in home
assert 'val star=Path()' in icons and 'val basket=polygon(' in icons
assert 'setImageResource(skinPortraitRes)' not in home

# Geometric dimensions measured on the ORIGINAL static owner WebPs.
# Comparing boxes does not certify RGB identity; that requires Android captures.
cases={
    "Android_16_9_1080x1920":(1080,1920,1.0,(304,1357,471,136),(303,1529,473,134)),
    "Android_FHD_1080x2280":(1080,2280,1.19,(260,1610,560,164),(259,1815,562,159)),
    "Android_FHD_Plus_1080x2340":(1080,2340,1.19,(260,1640,560,164),(259,1845,562,160)),
    "Android_20_9_1080x2400":(1080,2400,1.19,(260,1670,560,164),(259,1875,562,160)),
}
for folder,(width,height,growth,shop_ref,settings_ref) in cases.items():
    path=ROOT/"art/reference-current/images"/folder/"01_Pocetni_zaslon.webp"
    assert path.is_file(),f"Missing source owner WebP: {path}"
    with Image.open(path) as image:
        assert image.size==(width,height),path
    scale=min(width/941,height/1672)
    offset_x=(width-941*scale)/2
    offset_y=(height-1672*scale)/2
    def project(name):
        x,y,w,h=base[name]
        x=470.5+(x-470.5)*growth
        y=836+(y-836)*growth
        return (offset_x+x*scale,offset_y+y*scale,w*growth*scale,h*growth*scale)
    for name,expected in [("shop",shop_ref),("settings",settings_ref)]:
        actual=project(name)
        errors=[abs(a-e) for a,e in zip(actual,expected)]
        assert max(errors)<14,(folder,name,expected,actual,errors)
print("PASS: Home stacked buttons follow four genuine owner viewport geometries; RGB acceptance remains separate")
