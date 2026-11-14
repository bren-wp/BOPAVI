#!/usr/bin/env python3
"""Responsive compact Home has four true click destinations, including PLAY."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
src=r/"android/app/src/main/java/com/brendigo/bopavi"
main=(src/"MainActivity.kt").read_text()
home=main.split("private fun showHome(){",1)[1].split("private fun confirmGameplayShop(",1)[0]
layout=(src/"HomeReferenceLayout.kt").read_text()
for marker in ("FitViewportColumn(this)",
               "val compactWingedBopi=when(PilotSelectionModel.spriteIdForChoice(",
               "content.addView(compactWingedBopi,",
               'text="▶  PLAY"',
               'contentDescription="Pokreni igru"',
               'feature("SKINS"','feature("SHOP"',
               'text="SETTINGS"',
               'contentDescription="Otvori opcije igre"',
               "viewport.requestApplyInsets()",
               "content.addView(View(this),LinearLayout.LayoutParams(-1,0,1f))"):
    assert marker in home,marker
for marker in ('action(content,"▶  PLAY"', 'shortcut("',
               "HomeMedalDrawable()", "resourceChip(", 'showLevels()'):
    assert marker not in home,marker
assert 'isClickable=false' in home
assert 'val actions=listOf(skins,shop,settings)' in layout
assert 'val play=HomeBounds(' in layout
assert 'HomeReferenceScreen(this,progress).create(' in home
assert 'HomeReferenceLayout.supports(resources.displayMetrics.widthPixels,' in home
print("PASS: reference/compact Home preserve logo and hero with real Play navigation")
