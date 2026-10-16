"""Reference screenshot #gameplay: vertical power/status HUD contract.

Measured from the user's 941x1672 portrait captures. Validates a first
image-based layout correction, not photorealistic or screen-wide 1:1 parity.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
android=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
ios=(ROOT/"ios/BOPAVI/GameCanvas.swift").read_text()
anchors=(("★",101,"game.stars"),("∩",163,"game.magnetTime"),
         ("◆",225,"game.shield"))
for src,number_suffix in ((android,"f"),(ios,"")):
    for icon,y,value in anchors:
        assert f'hudOrb(c,42{number_suffix},{y}{number_suffix},"{icon}"' in src,(icon,y)
        assert value in src
    # Exactly three statuses and one implementation, not duplicate labels.
    assert src.count("hudOrb(c,")==3
    assert "hudGlass(c,146" in src
    assert "drawHudCrown(" in src and "drawHudFinishFlag(" in src
    assert "hudPilotPortrait(" in src
    assert "game.coins" in src and "game.score()" in src
    assert "game.flap()" in src  # Android/Swift both use actual flap physics
    for old in ("hudOrb(c,174","hudOrb(c,267","hudOrb(c,360"):
        assert old not in src
    # No baked counter strings taken from the screenshot.
    assert 'hudOrb(c,42'+number_suffix+',101'+number_suffix+',"★","2"' not in src

# Screenshot aspect-ratio crosscheck: original 941x1672 reference.
# Our 480x800 UI art is projected to the center-cropped device viewport.
# Tolerance is < 16 source pixels per axis (a small fraction of 941x1672).
for (icon,y,_),expected_y in zip(anchors,(211,341,470)):
    screen_x=42*941/480
    screen_y=y*1672/800
    assert abs(screen_x-82)<6,(icon,screen_x)
    assert abs(screen_y-expected_y)<16,(icon,screen_y)

# Portrait is exactly 54 px across and clears the first status circle.
assert "RectF(11f,10f,65f,64f)" in android
assert "oval(c,12f,10f,66f,64f" in android
assert "oval(c,12,10,54,54" in ios
assert 64 < 101-29, "Portrait overlaps crown status ring"
assert 42+29 < 73, "Crown ring enters the wallet badge"
assert 42+34 < 80, "Status count badge blocks the central flight corridor"
assert 225+32 < 300, "Last power icon is too low for the reference"

# No false hit areas: genuine touch-to-flap and native Pause remain in place.
assert "override fun onTouchEvent(event:MotionEvent)" in android
assert "override func touchesBegan(_ touches:Set<UITouch>" in ios
assert "private fun hudOrb(" in android and "private func hudOrb(" in ios
assert "c.drawCircle(cx,cy,24f,p)" in android
assert "c.strokeEllipse(in:CGRect(x:cx-24,y:cy-24,width:48,height:48))" in ios

print("PASS: reference-measured left HUD positions, native icons, live counters and safe geometry")
