#!/usr/bin/env python3
"""Four native Skins cards: 3 old pilots and genuine earned Orion.

The Skins gallery must never advertise an unowned hero as equipped, and
purchasing Star Pilot requires an explicit confirmation + existing coin ledger.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
screen=(src/"PilotSelectionScreen.kt").read_text()
model=(src/"PilotSelectionModel.kt").read_text()
layout=(src/"PilotSelectionLayout.kt").read_text()
menu=(src/"ReferenceMenus.kt").read_text()
main=(src/"MainActivity.kt").read_text()
store=(src/"ProgressStore.kt").read_text()
qa=(root/"tools/qa_android_reference_menus.py").read_text()
junit=(root/"android/app/src/test/java/com/brendigo/bopavi/PilotSelectionModelTest.kt").read_text()

assert "PilotSelectionScreen(host,progress).create(choice,onChoose,onUnlock,onShop,onBack)" in menu
assert "stage.useShopFullBleedReferenceCamera()" in screen
# Android AutoSizeTextTypeUniformWithConfiguration requires max > min.
# A 12sp status label must NOT pass an illegal 12..12 range.
assert 'if(size.toInt()>12)' in screen
assert 'setAutoSizeTextTypeUniformWithConfiguration(12,size.toInt(),1,' in screen
assert "12..12 interval that crashes Skins on API 35" in screen
assert 'selectedScreen="skins"\n        applyPremiumSystemBars()' in main
for name in ("CLASSIC AVIATOR","SKY OWL","SKY FOX","STAR PILOT"):
    assert name in model,name
assert "Pilot(3,\"STAR PILOT\",\"ORION\",PilotAbility.BOPI)" in model
assert "fun locked(candidate:Int,orionOwned:Boolean)" in model
assert "val orionOwned=progress.owned(8)" in screen
assert "PilotSelectionModel.locked(index,orionOwned)" in screen
assert "setOnClickListener{chooseOrUnlock(index)}" in screen
assert "onUnlock()" in screen
assert "stage.place(LevelMapLockGlyph(host)" in screen
assert "R.drawable.bopiportrait8" in screen and "R.drawable.bopi8" in screen
assert "R.drawable.bopileft8" in screen and "R.drawable.bopiright8" in screen
assert 'cardLefts=floatArrayOf(23f,252f,481f,710f)' in layout
assert "cardLefts.size==4" in layout
assert "const val cardWidth=207f" in layout
assert "const val cardHeight=307f" in layout
assert "const val heroSize=660f" in layout
assert "val heroId=PilotSelectionModel.spriteIdForChoice(selected)" in screen
assert "heroLeftWings[heroChoice],heroRightWings[heroChoice]," in screen
assert "allowMotion=!progress.lessMotion()" in screen
assert 'cycleArrow("‹","Previous hero")' in screen
assert 'cycleArrow("›","Next hero")' in screen
assert 'contentDescription="Equip "+item.title' in screen
assert "setOnClickListener { onChoose(selected) }" in screen
assert "stage.setOnApplyWindowInsetsListener" in screen
assert "minimumHeight=dp(48)" in screen
assert "contentDescription=PilotSelectionModel.accessibility(" in screen
assert "importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO" in screen
assert "SkinsReferenceDecor(host)" in screen
assert "SkinsCardEnamelDrawable(isSelected,isLocked)" in screen
decor=(src/"SkinsReferenceDecor.kt").read_text()
assert "class SkinsReferenceDecor(" in decor
assert "class SkinsCardEnamelDrawable(" in decor
assert "private val selected:Boolean,private val locked:Boolean=false" in decor
assert "c.scale(bounds.width()/207f,bounds.height()/307f)" in decor
assert "shape=GradientDrawable.OVAL" in screen
assert "val cost=progress.costs[8]" in main
assert 'setTitle("Unlock Star Pilot")' in main
assert 'setNegativeButton("Cancel",null)' in main
assert 'setPositiveButton("Unlock")' in main
assert "progress.selectOrBuy(8)" in main
assert "progress.chooseArcadePilot(3)" in main
assert "if(index !in 0..3 || (index==3 && !owned(8)))return false" in store
assert 'return if(value==3 && !owned(8))0 else value' in store
assert 'val costs = listOf(0, 60, 80, 110, 130, 160, 0, 220, 240)' in store
assert "selectOrBuy(" not in screen  # card tap does NOT silently spend coins
assert "Selected SKY OWL" in junit
assert "bopiportrait8" in (src/"HomeReferenceScreen.kt").read_text()
assert "portraits[pilotIndex.coerceIn(0,3)]" in (src/"HomeReferenceScreen.kt").read_text()
assert 'node_with("STAR PILOT",clickable=True)' in qa
assert "def open_native_skins():" in qa
assert "findViewById<android.view.ViewGroup>(android.R.id.content)" in main
assert "?.removeAllViews()" in main
assert "android-skins-process-death-logcat.txt" in qa
assert "def died_or_restarted(current_pid):" in qa
assert "died_or_restarted(running_pid())" in qa
assert "FATAL EXCEPTION" in qa and "OutOfMemoryError" in qa

assert "initial_pid=adb(\"shell\",\"pidof\",APP)" in qa
assert "four-card Skins gallery not reached after verified Home taps" in qa
assert "android-skins-navigation-failed.xml" in qa
assert "open_native_skins()" in qa
assert qa.count("    open_native_skins()")>=3
assert 'len(titles)==1 and len(owl)==1 and len(star)==1' in qa
assert "Locked STAR PILOT" in junit
assert 'tap(node_with("CHOOSE SKY OWL"))' in qa
assert 'capture_png("android-skins-reference-size")' in qa
assert '"941x1672"' in qa
assert "http://" not in screen and "https://" not in screen
print("PASS: four real native skins; saved Orion lock, confirmation and earned-coin purchase")
