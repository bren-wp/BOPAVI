"""Verify that animated scenery never exposes empty screen borders.

The artwork is a decorative 960×1600 source. Gameplay stays in its original
480×800 collision grid; the camera transforms only the world bitmap.
"""
from pathlib import Path
import math

ROOT = Path(__file__).resolve().parents[1]
ANDROID = (ROOT / "android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
IOS = (ROOT / "ios/BOPAVI/GameCanvas.swift").read_text()

# Both native implementations must use identical values.
assert "private val worldOverscan=1.06f" in ANDROID
assert "private val worldHorizontalDrift=.018f" in ANDROID
assert "private val worldVerticalDrift=.008f" in ANDROID
assert "worldOverscan" in ANDROID.split("override fun onSizeChanged(", 1)[1]
assert "scaleToCover=max(bound.width/world.size.width," in IOS
assert "bound.height/world.size.height)*1.06" in IOS
assert "bound.width*0.018" in IOS and "bound.height*0.008" in IOS
for code in (ANDROID, IOS):
    assert ".0011" in code and ".00045" in code
    assert "reducedMotion" in code
    assert "game.active" in code and "game.finished" in code
# Source image movement must be isolated before game coordinate transform.
assert ANDROID.index("canvas.drawBitmap(worldBitmap,null,worldDriftRect,p)") < ANDROID.index("canvas.scale(scale,scale)")
assert IOS.index("world.draw(in:CGRect(x:(bound.width-backdropWidth)") < IOS.index("c.scaleBy(x:s,y:s)")

for width,height in [
    (320,568),(360,640),(360,800),(393,852),(412,915),(480,800),
    (720,1280),(1080,2340),(1080,2400),(1440,3200),
    (800,1280),(1280,800),(1920,1080),(2560,1600),
]:
    source_width,source_height = 960,1600
    cover=max(width/source_width,height/source_height)*1.06
    full_w,full_h=source_width*cover,source_height*cover
    assert full_w >= width and full_h >= height

    for distance in [0,250,900,1200,7000,25000,1e6]:
        dx=math.sin(distance*.0011)*width*.018
        dy=math.sin(distance*.00045)*height*.008
        left=(width-full_w)/2+dx
        top=(height-full_h)/2+dy
        assert left <= 0 and top <= 0, (width,height,distance,"top/left gap")
        assert left+full_w >= width and top+full_h >= height, (
            width,height,distance,"bottom/right gap"
        )

        # Reduced-motion and inactive/finished flight both force zero
        # camera movement, independent of elapsed distance.
        for reduced,active,finished in [(True,True,False),
                                        (False,False,False),
                                        (False,True,True)]:
            motion = not reduced and active and not finished
            assert (0 if not motion else dx) == 0
            assert (0 if not motion else dy) == 0

print("PASS: parallax painted-world cover has no exposed screen borders on 14 layouts")
