#!/usr/bin/env python3
"""Owner Shop title and backing follow the same corrective camera transform."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
header=(src/"ShopHeaderLayout.kt").read_text()
shop=(src/"ShopScreen.kt").read_text()
decor=(src/"ShopStorefrontDecor.kt").read_text()
assert "const val titleOffset=85f" in header
assert "const val titleTop=123f+titleOffset" in header
assert "183f,ShopHeaderLayout.titleTop,575f,201f" in shop
assert "c.translate(0f,ShopHeaderLayout.titleOffset)" in decor
assert "woodenBoard(c)" in decor
assert "c.restoreToCount(titleSave)" in decor
assert "stage.place(priceButton(featured.price,true," in shop
assert "ShopReferenceLayout.rows[row]" in shop
assert "ShopReferenceLayout.columns[col]" in shop
print("PASS: owner header correction shares coordinates with 3D wood sign; purchase grid unchanged")
