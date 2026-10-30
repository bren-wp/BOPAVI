#!/usr/bin/env python3
"""Screen 9: six real offline power-ups and safe purchase eligibility."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
shop=(src/"ShopScreen.kt").read_text()
model=(src/"ShopItemState.kt").read_text()
geom=(src/"ShopReferenceLayout.kt").read_text()
menus=(src/"ReferenceMenus.kt").read_text()
store=(src/"ProgressStore.kt").read_text()
app=(src/"MainActivity.kt").read_text()
qa=(root/"tools/qa_android_reference_menus.py").read_text()
test=(root/"android/app/src/test/java/com/brendigo/bopavi/ShopItemStateTest.kt").read_text()
assert 'ShopScreen(host,progress).create(onBuy,onClaim,onBack)' in menus
for term in (
    'progress.coins()','progress.bonusCoinsAvailable()',
    'ShopItemState.fromStore(progress)','item.canBuy',
    'onBuy(item.index)','onClaim','contentDescription="Earned coins: $coins"',
    'BopaviUiIconDrawable.Kind.SHIELD','BopaviUiIconDrawable.Kind.MAGNET',
    'isEnabled=false','if(isAttachedToWindow)isEnabled=true',
    'stage.setOnApplyWindowInsetsListener','minimumHeight=dp(48)',
    'ShopReferenceLayout.rows[row]','ShopReferenceLayout.columns[col]',
):
    assert term in shop,term
assert 'val perkPrices=listOf(80,65,150,120,230,195)' in store
assert 'prefs.edit().putInt("coins",coins()-perkPrices[index]).putInt("perk_$index",perkCount(index)+1).apply()' in store
assert 'if(progress.buyPerk(index))' in app
assert 'if(progress.claimBonusCoins()>0)' in app
assert 'val rows=floatArrayOf(454f,791f,1128f)' in geom
assert 'assertTrue(ShopReferenceLayout.valid())' in test
assert 'assertFalse(poor.canBuy)' in test and 'assertFalse(full.canBuy)' in test
assert 'LinearLayout.LayoutParams(-1,0,1.10f)' in shop
assert 'dp(43),dp(43)' in shop
assert 'capture_png("android-shop-reference-size")' in qa
assert '"GEMS COLLECTED"' in qa
assert 'R.drawable.reference_screenshot' not in shop
assert 'http://' not in shop and 'https://' not in shop
print("PASS: authentic shop, six prices, real purchase/inventory, offline and accessible controls")
