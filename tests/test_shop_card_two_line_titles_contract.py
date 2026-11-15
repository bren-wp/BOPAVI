#!/usr/bin/env python3
"""Owner Shop: actual native two-line product titles, six unchanged wallet actions."""
from pathlib import Path
src=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
shop=(src/"ShopScreen.kt").read_text(encoding="utf-8")
rows=(src/"ShopCardRowWeights.kt").read_text(encoding="utf-8")
progress=(src/"ProgressStore.kt").read_text(encoding="utf-8")
assert "ShopCardRowWeights.HEADER" in shop
assert "ShopCardRowWeights.ILLUSTRATION" in shop
assert "ShopCardRowWeights.EFFECT" in shop
assert "ShopCardRowWeights.BUY" in shop
for token in ("const val HEADER=.90f","const val ILLUSTRATION=1.35f",
              "const val EFFECT=.79f","const val BUY=1.00f"):
    assert token in rows,token
for token in ('item.storefrontTitle','maxLines=2',
              'onBuy(item.index)','contentDescription="Earned coins: $coins"',
              'R.drawable.bopavi_owner_shop_kiosk',
              'priceButton(item.price,false'):
    assert token in shop,token
assert 'val perkPrices=listOf(80,65,150,120,230,195)' in progress
assert '@Synchronized fun buyPerk(index:Int):Boolean' in progress
assert '.commit()' in progress.split("fun buyPerk(",1)[1].split("fun previewPerks(",1)[0]
print("PASS: native Shop two-line heading room increased; six purchases unchanged")
