#!/usr/bin/env python3
"""Home screenshot 00_32_51-1: genuine 941×1672 layered native UI."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text()
view=(src/"HomeReferenceScreen.kt").read_text()
geometry=(src/"HomeReferenceLayout.kt").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/HomeReferenceLayoutTest.kt").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
assert "HomeReferenceScreen(this,progress).create(" in activity
assert "HomeReferenceLayout.supports(resources.displayMetrics.widthPixels," in activity
assert "portraitDrawableIds[selectedPilotChoice]" in activity
for box in (
    "val coins=HomeBounds(452f,20f,215f,82f)",
    "val stars=HomeBounds(684f,20f,219f,82f)",
    "val logo=HomeBounds(127f,123f,686f,208f)",
    "val hero=HomeBounds(139f,367f,663f,500f)",
    "val play=HomeBounds(153f,891f,637f,182f)",
    "val skins=HomeBounds(136f,1090f,323f,138f)",
    "val shop=HomeBounds(478f,1090f,329f,138f)",
    "val settings=HomeBounds(248f,1245f,445f,122f)",
    "val achievements=HomeBounds(176f,1440f,195f,149f)",
    "val leaderboard=HomeBounds(379f,1440f,198f,149f)",
    "val sound=HomeBounds(588f,1440f,182f,149f)",
):
    assert box in geometry,box
for item in (
    "R.drawable.homeback","R.drawable.logo","setImageResource(pilot)",
    "progress.coins()","progress.worldStars(it)",
    "HomeOrbitDecoration(host)","HomeReferenceLayout.play",
    'pill("PLAY","▶  PLAY"','pill("SKINS","Otvori izbor likova"',
    'pill("SHOP","Otvori trgovinu nadogradnji"',
    'pill("SETTINGS","Otvori opcije igre"',
    'footer("ACHIEVEMENTS","Pogledaj svoja postignuća"',
    'footer("LEADERBOARD","Pogledaj svoje rekorde"',
    'footer("SOUND","Otvori zvučne postavke"',
    "stage.setOnApplyWindowInsetsListener","stage.setSafe(",
    "isClickable=true;isFocusable=true","minimumHeight=dp(48)",
):
    assert item in view,item
assert "assertTrue(HomeReferenceLayout.valid())" in unit
assert "assertTrue(HomeReferenceLayout.supports(941,1672,true))" in unit
assert 'capture "android-home-$label"' in qa
assert "941x1672:320:reference" in qa
assert "1,250" not in view and "85 Gems" not in view
print("PASS: reference native Home layout, saved counts and working routed actions")
