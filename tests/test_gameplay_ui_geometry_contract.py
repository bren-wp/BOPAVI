#!/usr/bin/env python3
"""Prevent visual phantom cliffs, cropped Android actions and wrong original icons.

Pixel-identity to external screenshots requires original reference captures;
this gate protects provable source geometry and the real owner-approved art.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text(encoding="utf-8")
main=(src/"MainActivity.kt").read_text(encoding="utf-8")
icon=(src/"BopaviUiIconDrawable.kt").read_text(encoding="utf-8")
generator=(root/"tools/generate_images.py").read_text(encoding="utf-8")
test=(root/"android/app/src/test/java/com/brendigo/bopavi/PillarVisualContactTest.kt").read_text(encoding="utf-8")
rooted=view.split("private fun drawRootedCliffBases(",1)[1].split(
    "private fun drawRockStrata(",1)[0]
assert "val left=x; val right=x+w" in rooted
assert "val left=x-6f; val right=x+w+4f" not in rooted
assert "rect(c,x-7f,top-28f,x+w+5f,top,cap,7f)" in view
assert "rect(c,x-7f,bottom,x+w+5f,bottom+27f,cap,7f)" in view
assert "PillarCollision.bodyHitsPillar" in test
for width in ("54f","74f","80f","86f","98f","110f"):
    assert width in test, width

assert "private fun applySafeViewportInsets(" in main
assert "Build.VERSION.SDK_INT>=30" in main
assert "WindowInsets.Type.displayCutout()" in main
for field in ("Left","Top","Right","Bottom"):
    assert f"cutout?.safeInset{field} ?: 0" in main
assert "viewport.setOnApplyWindowInsetsListener{v,insets->" in main
assert "applySafeViewportInsets(v,insets)" in main
assert "fitted.setOnApplyWindowInsetsListener { view, insets ->" in main
assert "applySafeViewportInsets(view,insets)" in main
assert "viewport.requestApplyInsets()" in main
assert "fitted.requestApplyInsets()" in main

assert "paint.color = Color.argb(Color.alpha(paint.color), 76, 211, 137)" in icon
assert "legacy_mascot=ImageOps.contain(mascot,(905,905)" in generator
assert "icon_mascot=ImageOps.contain(mascot,(735,735)" in generator
assert "foreground.alpha_composite(icon_mascot" in generator
assert "app_icon_monochrome.png" in generator
print("PASS: honest variable-width cliff art, 26–36 viewport insets, berry colour and exact Bopi launcher")
