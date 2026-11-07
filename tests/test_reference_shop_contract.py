#!/usr/bin/env python3
"""Owner-reference Shop: six genuine locally purchased items in a 3x2 grid."""
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
assert '@Synchronized fun buyPerk(index:Int):Boolean' in store
assert '.putInt("coins",coins()-perkPrices[index])' in store
assert '.putInt("perk_$index",perkCount(index)+1)' in store
assert '.commit()' in store.split("fun buyPerk(",1)[1].split("fun previewPerks(",1)[0]
assert '@Synchronized fun claimBonusCoins():Int' in store
assert 'return if(persisted)awarded else 0' in store
assert 'if(progress.buyPerk(index))' in app
assert 'shopPurchaseTapGate.allow(index,' in app
assert 'android.os.SystemClock.elapsedRealtime()' in app
purchaseGate=(src/"ShopPurchaseTapGate.kt").read_text()
gateTest=(root/"android/app/src/test/java/com/brendigo/bopavi/ShopPurchaseTapGateTest.kt").read_text()
assert 'nowMs-lastTime<intervalMs' in purchaseGate
assert 'secondAccidentalTapCannotChargeTwiceImmediately' in gateTest
assert 'distinctProductsAndLaterDeliberateBuysRemainAvailable' in gateTest
assert 'if(progress.claimBonusCoins()>0)' in app
assert 'val rows=floatArrayOf(886f,1208f)' in geom
assert 'val columns=floatArrayOf(35f,331f,627f)' in geom
assert 'val row=item.index/3' in shop and 'val col=item.index%3' in shop
assert 'val featured=items.last()' in shop
heroIcons=(src/"HomeHeroIconDrawable.kt").read_text()
assert 'HomeHeroIconDrawable(' in shop
assert 'if(featured.isShield)BopaviUiIconDrawable.Kind.SHIELD' in shop
assert 'BopaviUiIconDrawable.Kind.SHIELD->shield(canvas)' in heroIcons
assert 'BopaviUiIconDrawable.Kind.MAGNET->magnet(canvas)' in heroIcons
assert '0xfffff8a4.toInt()' in heroIcons and '0xffff8298.toInt()' in heroIcons
assert 'ShopReferenceLayout.supports(' in shop
assert 'val scroll=ScrollView(host).apply' in shop
assert 'isFillViewport=true' in shop
assert 'column.addView(perkCard(item,onBuy),' in shop
assert 'val stock=ShopItemState.fromStore(progress)' in shop
assert 'earned>0' in shop
assert 'fontScale<1.25f' in geom
assert 'onBuy(featured.index)' in shop
assert 'featured.canBuy' in shop
assert 'R.drawable.bopiportrait0' in shop
assert 'contentDescription="HOME"' in shop
assert 'stage.place(ReferenceArrowOverlayView(host),24f,18f,119f,99f)' in shop
assert '},130f,235f,570f,420f)' in shop
assert 'contentDescription="Earned stars: $stars"' in shop
assert 'if(bonus>0)' in shop
assert 'assertTrue(ShopReferenceLayout.valid())' in test
assert 'assertFalse(poor.canBuy)' in test and 'assertFalse(full.canBuy)' in test
assert 'LinearLayout.LayoutParams(-1,0,.90f)' in shop
assert 'LinearLayout.LayoutParams(dp(52),dp(52))' in shop
assert 'contentDescription="OWNED: ${item.owned}/99. "+item.accessibility' in shop
assert 'message("OWNED: ${item.owned}/99  •  TIER' not in shop
assert 'message(item.storefrontTitle,15' in shop
assert 'message(item.perkEffect,13' in shop
assert 'item.iconTierLabel' in shop
assert 'val storefrontTitle:String get()' in model
assert 'val perkEffect:String get()' in model
assert 'val iconTierLabel:String get()' in model
assert 'storefrontCardsHaveSixDistinctRealProducts' in test
assert 'capture_png("android-shop-reference-size")' in qa
assert '"GEMS COLLECTED"' in qa
assert 'R.drawable.reference_screenshot' not in shop
assert 'http://' not in shop and 'https://' not in shop

# The owner's shop contains a full wooden storefront and twelve striped
# awning sections. These are decor only: native controls must stay live.
decor=(src/"ShopStorefrontDecor.kt").read_text()
for element in ("private fun woodenBoard(","private fun awning(",
                "private fun lantern(","private fun featuredAura(",
                "private fun frame(","for(i in 0..11)",
                "c.scale(width/941f,height/1672f)"):
    assert element in decor, element
assert 'stage.place(ShopStorefrontDecor(host),0f,0f,941f,1672f)' in shop
assert 'stage.place(ShopEmbossedTitle(host)' in shop
headline=(src/"ShopEmbossedTitle.kt").read_text()
assert 'internal class ShopEmbossedTitle(context:Context):TextView(context)' in headline
assert 'text="SHOP"' in headline
assert 'intArrayOf(0xfffffaaa.toInt(),0xffffd932.toInt(),' in headline
assert 'contentDescription="SHOP"' in headline
assert 'Bopavi3DTheme.Finish.GREEN' in shop
assert 'ShopItemState.fromStore(progress)' in shop
assert 'onBuy(item.index)' in shop
assert 'R.drawable.reference_screenshot' not in decor

print("PASS: reference 3x2 Shop preserves six prices, offline buys, native access and bonuses")
