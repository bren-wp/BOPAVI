#!/usr/bin/env python3
"""Source-level guard for 941 x 1672 Home 3D geometry; not a pixel-match claim."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
home=(src/"HomeReferenceScreen.kt").read_text()
medal=(src/"HomeMedalDrawable.kt").read_text()
layout=(src/"HomeReferenceLayout.kt").read_text()
assert "setPadding(0,0,0,0)" in home
assert "ReferenceButtonCluster.measure(" in home
assert "marginStart=cluster.gap" in home
assert "dp(if(isPlay)93 else 59)" not in home
assert "HomeMedalDrawable()" in home
assert "canvas.scale(bounds.width()/100f,bounds.height()/100f)" in medal
for signature in ("RectF(3f,11f,97f,100f)",
                  "0xffffbf36.toInt()","0xff06214e.toInt()",
                  "0xff284e7b.toInt()",
                  "android.R.attr.state_pressed in states"):
    assert signature in medal,signature
assert "HomeReferenceLayout" in layout
print("PASS: Home vectors scale with actual reference pixel view bounds, not density")
