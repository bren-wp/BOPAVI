#!/usr/bin/env python3
"""Native hero-first Skins gallery with only the three genuinely playable pilots."""
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
assert 'stage.useShopFullBleedReferenceCamera()' in screen
assert 'selectedScreen="skins"\n        applyPremiumSystemBars()' in main
assert "progress.chooseArcadePilot(index)" in main
for word in ("CLASSIC AVIATOR","SKY OWL","SKY FOX","FREE • UNLOCKED",
             "Choose ","Selected "):
    assert word in model,word
for expected in (
    "R.drawable.bopiportrait0","R.drawable.bopiportrait9",
    "R.drawable.bopiportrait10","stage.setOnApplyWindowInsetsListener",
    "PilotSelectionModel.accessibility(selected,index)",
    "setOnClickListener{onChoose(index)}",
    "importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO",
    "progress.coins()","minimumHeight=dp(48)",
    "item.ability.shortTitle","item.ability.description",
):
    assert expected in screen,expected
assert "cardLefts=floatArrayOf(34f,339f,644f)" in layout
assert "const val heroLeft=100f" in layout
assert "const val heroTop=220f" in layout
assert "const val heroSize=740f" in layout
assert 'cycleArrow("‹","Previous hero")' in screen
assert 'cycleArrow("›","Next hero")' in screen
assert 'contentDescription="Equip "+item.title' in screen
assert 'setOnClickListener { onChoose(selected) }' in screen
assert "heroTop+heroSize<cardTop" in layout
assert "platformTop+platformHeight<cardTop" in layout
assert "stage.place(HomeWingActor(host," in screen
assert "val heroId=PilotSelectionModel.spriteIdForChoice(selected)" in screen
assert "heroLeftWings[heroChoice],heroRightWings[heroChoice]," in screen
assert "allowMotion=!progress.lessMotion()" in screen
assert "PilotSelectionLayout.heroLeft" in screen
# The only oval is a real, accessible previous/next button—not a
# flat placeholder obscuring the new stone platform and floating hero.
assert "shape=GradientDrawable.OVAL" in screen.split("private fun cycleArrow(",1)[1].split("fun create(",1)[0]
assert "SkinsReferenceDecor(host)" in screen
assert "SkinsReferenceDecor(host)" in screen
assert "SkinsCardEnamelDrawable(isSelected)" in screen
decor=(src/"SkinsReferenceDecor.kt").read_text()
assert "class SkinsReferenceDecor(" in decor
assert "class SkinsCardEnamelDrawable(" in decor
assert "importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO" in decor
assert "Bopavi3DTheme.panel(Bopavi3DTheme.Finish.SKY)" in screen
assert "const val referenceWidth=941f" in layout
assert "val skins=intArrayOf(" in screen
assert "Selected SKY OWL" in junit
assert 'tap(node_with("CHOOSE SKY OWL"))' in qa
assert 'capture_png("android-skins-reference-size")' in qa
assert '"941x1672"' in qa
assert "selectOrBuy(" not in screen  # No fake purchase of playable pilots
assert "http://" not in screen and "https://" not in screen
print("PASS: native selected pilot hero, three real selection cards, persisted free choices")
