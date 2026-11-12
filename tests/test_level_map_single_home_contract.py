#!/usr/bin/env python3
"""One native visual HOME control on the owner's 941×1672 Level Map."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
screen=(src/"LevelMapScreen.kt").read_text(encoding="utf-8")
chrome=(src/"LevelMapReferenceChrome.kt").read_text(encoding="utf-8")
qa=(root/"tools/qa_android_reference_menus.py").read_text(encoding="utf-8")
progress=(src/"ProgressOverviewScreen.kt").read_text(encoding="utf-8")
for old in (
    'stage.place(nativeButton("←","Home",blue)',
    'stage.place(ReferenceArrowOverlayView(host)',
    'text="←"',
):
    assert old not in screen,old
assert screen.count("stage.place(LevelMapBackControl(host,onBack),")==1
assert "20f,16f,122f,95f,minTouch=true" in screen
for token in (
    "internal class LevelMapBackControl(",
    "private val arrow=ReferenceBackArrow(58)",
    'contentDescription="HOME"',
    "setOnClickListener { onBack() }",
    "arrow.setBounds(x,y,x+size,y+size)",
    "background=Bopavi3DTheme.button(Bopavi3DTheme.Finish.SKY)",
):
    assert token in chrome,token
# Require exactly one app-owned, enabled and accessible HOME control.
# A real second BOPAVI HOME remains a hard failure; Android's system
# navigation bar must never be counted as a duplicate game button.
assert "def map_home_controls(root):" in qa
assert 'n.get("package")==APP' in qa
assert '(n.get("content-desc") or "").strip().casefold()=="home"' in qa
assert "def unique_map_home():" in qa
assert "map_home=unique_map_home()" in qa
assert "qa/screenshots/android-map-home-identity-failed.xml" in qa
assert 'map_home=matching_nodes(hierarchy(),"HOME",clickable=True)' not in qa
assert "RippleDrawable(" in progress
assert "LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT)" in progress
print("PASS: single HOME action, aligned vector and measured Progress cards")
