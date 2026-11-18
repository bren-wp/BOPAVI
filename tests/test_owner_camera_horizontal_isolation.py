#!/usr/bin/env python3
"""Regression: the Shop-only 16:9 crop may never compress Settings or Skins.

The original 1080x1920 native captures showed shortened Settings labels and
narrow Skins cards while the approved owner references filled the portrait.
This is a static geometry guard, not proof of exact RGB equivalence.
"""
from pathlib import Path

src = Path(__file__).resolve().parents[1] / "android/app/src/main/java/com/brendigo/bopavi"
stage = (src / "ReferenceStage.kt").read_text()
shop = (src / "ShopScreen.kt").read_text()
settings = (src / "SettingsScreen.kt").read_text()
skins = (src / "PilotSelectionScreen.kt").read_text()
layout = (src / "ShopReferenceLayout.kt").read_text()
assert "const val owner16x9HorizontalFactor=0.8435f" in layout
assert "private var shopWidthCorrection=false" in stage
assert "fun useShopFullBleedReferenceCamera()" in stage
assert "shopWidthCorrection=true" in stage
assert "fun useOwnerFullBleedReferenceCamera()" in stage
assert "shopWidthCorrection=false" in stage
assert "if(shopFullBleed && shopWidthCorrection)" in stage
assert "ShopReferenceLayout.horizontalFactor(width,height) else 1f" in stage
assert "stage.useShopFullBleedReferenceCamera()" in shop
assert "stage.useOwnerFullBleedReferenceCamera()" in settings
assert "stage.useOwnerFullBleedReferenceCamera()" in skins
assert "stage.useShopFullBleedReferenceCamera()" not in settings + skins
assert "stage.useOwnerFullBleedReferenceCamera()" not in shop
assert "if(!slot.referenceAligned && !shopFullBleed)return safe" in stage
# All real shopping transactions and Settings native touch targets stay alive.
assert "ShopItemState.fromStore(progress)" in shop
assert "SettingsReferenceLayout.rowHeight,minTouch=true," in settings
assert "setOnClickListener{chooseOrUnlock(index)}" in skins
print("PASS: Shop 16:9 x-correction is isolated; Settings/Skins preserve full native width")
