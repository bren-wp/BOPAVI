#!/usr/bin/env python3
"""Three user-facing animals keep old pilot resources and safe, private selection."""
from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
K=ROOT/"android/app/src/main/java/com/brendigo/bopavi"
main=(K/"MainActivity.kt").read_text()
view=(K/"GameView.kt").read_text()
store=(K/"ProgressStore.kt").read_text()
generator=(ROOT/"tools/generate_images.py").read_text()
assert 'pilotNames=arrayOf("Bopi","Sova","Lisac")' in main
assert "pilotSpriteIds=intArrayOf(0,9,10)" in main
assert "progress.chooseArcadePilot(index)" in main
assert "fun refreshPilotCards()" in main
home=main.split("private fun showHome(){",1)[1].split("private fun showPremiumPause(",1)[0]
assert "refreshPilotCards()" in home
assert "showHome()" not in home
assert "heroPortrait.setImageResource(portraitDrawableIds[selectedPilotChoice])" in home
assert "GameView(this,simulation,progress.lessMotion(),pilotSpriteIds[selectedPilotChoice]" in main
assert "arcade_pilot" in store and 'prefs.getInt("skin_index", 0)' in store
assert "bopiportrait9" in main and "bopiportrait10" in main
assert "selectedSkin=skinIndex.coerceIn(0,10)" in view
for n in ("bopi9","bopi10","bopileft9","bopileft10","bopiright9","bopiright10"):
    assert "R.drawable."+n in view,n
for animal in ("sova","lisac"):
    source=(ROOT/"docs/assets"/(animal+".svg")).read_text()
    assert '<g id="wing-left">' in source and '<g id="wing-right">' in source
    assert re.search(r'<defs>.*?</defs>',source,re.S)
    assert f"'{animal}'" in generator
assert "GameFramePacing.redrawDelayMillis(reducedMotion,powerSaveMode)" in view
assert "startSceneryDrift(" not in main
assert "val radius = 18f" in (K/"GameSimulation.kt").read_text()
print("PASS: 3 offline characters, 2 original animal sources, old save compatibility, adaptive vsync pacing")
