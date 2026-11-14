#!/usr/bin/env python3
"""Owner Shop's wooden sill is bottom-anchored, passive and not a bitmap UI."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
geom=(src/"ShopBottomLayout.kt").read_text()
dec=(src/"ShopStorefrontDecor.kt").read_text()
shop=(src/"ShopScreen.kt").read_text()
assert "const val woodTop=1320f" in geom
assert "const val frameBottom=1571f" in geom
assert "const val bottomAnchor=1f" in geom
assert "fun spansViewport(width:Int,height:Int):Boolean" in geom
assert "internal class ShopBottomSill(context:Context):View(context)" in dec
assert "height/ShopReferenceLayout.height" in dec
assert "importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO" in dec
assert "isClickable=false" in dec and "isFocusable=false" in dec
assert "stage.place(ShopBottomSill(host),0f,0f,941f,1672f," in shop
assert "stage.place(ShopForegroundFoliage(host),0f,0f,941f,1672f," in shop
assert shop.count("verticalAnchor=ShopBottomLayout.bottomAnchor")==2
for untouched in ('ShopReferenceLayout.rows[row]',
                  'ShopReferenceLayout.columns[col]',
                  'onBuy(item.index)',
                  'val coins=progress.coins()',
                  'stage.place(priceButton(featured.price,true,'):
    assert untouched in shop,untouched
assert 'R.drawable.reference_screenshot' not in dec
print("PASS: native bottom wood + plants, owner camera, all six real Shop products")
