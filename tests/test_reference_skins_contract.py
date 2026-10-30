#!/usr/bin/env python3
"""Screen 8: native Skins gallery matches three playable pilots, not fake wallet."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
screen=(src/"PilotSelectionScreen.kt").read_text()
model=(src/"PilotSelectionModel.kt").read_text()
layout=(src/"PilotSelectionLayout.kt").read_text()
menu=(src/"ReferenceMenus.kt").read_text()
main=(src/"MainActivity.kt").read_text()
qa=(root/"tools/qa_android_reference_menus.py").read_text()
junit=(root/"android/app/src/test/java/com/brendigo/bopavi/PilotSelectionModelTest.kt").read_text()
assert "PilotSelectionScreen(host,progress).create(choice,onChoose,onBack)" in menu
assert "progress.chooseArcadePilot(index)" in main
for word in ("CLASSIC AVIATOR","WISE OWL","SKY FOX","FREE • UNLOCKED",
             "Choose ","Selected "):
    assert word in model,word
for expected in (
    "R.drawable.bopiportrait0","R.drawable.bopiportrait9",
    "R.drawable.bopiportrait10","stage.setOnApplyWindowInsetsListener",
    "PilotSelectionModel.accessibility(selected,index)",
    "setOnClickListener{if(!isSelected)onChoose(index)}",
    "importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO",
    "progress.coins()","minimumHeight=dp(48)",
    "item.ability.shortTitle","equipped.ability.description",
):
    assert expected in screen,expected
assert "cardLefts=floatArrayOf(52f,344f,636f)" in layout
assert "const val referenceWidth=941f" in layout
assert "val skins=intArrayOf(" in screen
assert "Selected WISE OWL" in junit
assert 'tap(node_with("CHOOSE WISE OWL"))' in qa
assert 'capture_png("android-skins-reference-size")' in qa
assert '"941x1672"' in qa
assert "selectOrBuy(" not in screen  # No fake purchase of playable pilots
assert "http://" not in screen and "https://" not in screen
print("PASS: native three playable pilots, real saved selection, responsive native cards")
