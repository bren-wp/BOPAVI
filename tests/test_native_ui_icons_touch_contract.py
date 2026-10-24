#!/usr/bin/env python3
"""Source guarantees for icon fidelity and actual touch target accessibility.

This is only a code-level gate; Android API35/API36 emulator captures exercise
the native controls. An owner's full-screen reference set is still required
before claiming actual pixel-for-pixel identity.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = root / "android/app/src/main/java/com/brendigo/bopavi"
activity = (src / "MainActivity.kt").read_text(encoding="utf-8")
viewport = (src / "FitViewportColumn.kt").read_text(encoding="utf-8")
icons = (src / "BopaviUiIconDrawable.kt").read_text(encoding="utf-8")
image_generator = (root / "tools/generate_images.py").read_text(encoding="utf-8")
launcher = (root / "android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml").read_text(encoding="utf-8")

assert "class FitViewportColumn(context: Context) : ScrollView(context)" in viewport
assert "isFillViewport = true" in viewport
assert "isVerticalScrollBarEnabled = false" in viewport
assert "scaleX=" not in viewport and "scaleY=" not in viewport
assert "scaleX =" not in viewport and "scaleY =" not in viewport
assert "clipToPadding = false" in viewport

for kind in ("PLAY","PAUSE","RESTART","HOME","EXIT","SETTINGS","RECORDS","STAR","BERRY"):
    assert kind in icons, kind
assert "Paint(Paint.ANTI_ALIAS_FLAG)" in icons
assert "override fun getIntrinsicWidth()" in icons
assert "requestedAlpha = alpha.coerceIn(0, 255)" in icons
assert "canvas.restoreToCount(checkpoint)" in icons

for snippet in (
    "private fun uiIcon(",
    "private fun actionKind(",
    "uiIcon(actionKind(text),if(primary)primaryButtonInk else Color.WHITE,24)",
    "uiIcon(if(text.contains(\"OPCIJE\"))BopaviUiIconDrawable.Kind.SETTINGS",
    "if(icon==\"🍓\")BopaviUiIconDrawable.Kind.BERRY",
    "uiIcon(actionKind(text),if(style==0)primaryButtonInk else Color.WHITE,24)",
    "uiIcon(BopaviUiIconDrawable.Kind.HOME,Color.WHITE,23)",
    "compoundDrawablePadding=d(9)",
    "contentDescription=\"Povratak na početnu\"",
    "LinearLayout.LayoutParams(0,d(48),1f)",
):
    assert snippet in activity, snippet

# The OS-themed icon is the real owner's Bopi alpha, not a second fabricated
# bird, a surrogate vector or a separately maintained unreliable silhouette.
assert "themed.putalpha(foreground.getchannel('A'))" in image_generator
assert "a/'drawable/app_icon_monochrome.png'" in image_generator
assert '<monochrome android:drawable="@drawable/app_icon_monochrome"/>' in launcher

print("PASS: original Bopi themed icon, consistent native icons and unscaled touch targets")
