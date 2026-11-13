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
assert 'val rows=floatArrayOf(883f,1208f)' in geom
assert 'val columns=floatArrayOf(52f,336f,619f)' in geom
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
assert 'R.drawable.bopavi_owner_shop_kiosk' in shop
assert 'R.drawable.bopavi_owner_shop_kiosk' in shop  # actual owner's reference kiosk artwork
assert '},24f,302f,894f,276f)' in shop
assert 'R.drawable.bopiportrait1' in shop  # golden mascot behind the real featured perk
assert 'setPadding(dp(4),0,dp(4),0)' in shop  # unclipped native purchase price
assert 'contentDescription="HOME"' in shop
assert 'stage.place(ShopBackControl(host,onBack),' in shop
assert 'contentDescription="HOME"' in (src/"ReferenceBackArrow.kt").read_text()
assert 'glyph.draw(canvas)' in (src/"ReferenceBackArrow.kt").read_text()
assert 'stage.useShopFullBleedReferenceCamera()' in shop
stage=(src/"ReferenceStage.kt").read_text()
assert 'fun useShopFullBleedReferenceCamera()' in stage
assert 'if(!slot.referenceAligned && !shopFullBleed)return safe' in stage
assert '},24f,302f,894f,276f)' in shop
assert 'contentDescription="Earned stars: $stars"' in shop
assert 'BopaviUiIconDrawable.Kind.STAR,0xffffcf3d.toInt(),dp(34)' in shop
assert 'BopaviUiIconDrawable.Kind.GEM,0xfff554ef.toInt(),dp(34)' not in shop
assert 'if(bonus>0)' in shop
assert 'assertTrue(ShopReferenceLayout.valid())' in test
assert 'assertFalse(poor.canBuy)' in test and 'assertFalse(full.canBuy)' in test
assert 'LinearLayout.LayoutParams(-1,0,1.00f)' in shop
assert 'val hit=Rect()' in shop
assert 'buy.getHitRect(hit)' in shop
assert 'card.touchDelegate=TouchDelegate(hit,buy)' in shop


assert 'background=ShopCreamCardDrawable()' in shop
assert 'illustration.addView(ShopPerkArtwork(host,item),' in shop
assert '},24f,302f,894f,276f)' in shop
assert 'background=ShopFeaturedPanelDrawable()' in shop
# Dynamic featured SKU remains real, with embossed lettering/ribbon as decor.
assert 'ShopFeaturedName(host,featured.storefrontTitle)' in shop
assert 'background=ShopFeaturedRibbonDrawable()' in shop
feature_art=(src/"ShopFeaturedLettering.kt").read_text()
assert 'internal class ShopFeaturedName(context:Context,label:String):TextView(context)' in feature_art
assert 'text=label' in feature_art and 'contentDescription=label' in feature_art
assert 'internal class ShopFeaturedRibbonDrawable:Drawable()' in feature_art
assert 'canvas.drawText' not in feature_art  # Draws text via native Canvas c
feature=shop.split('background=ShopFeaturedPanelDrawable()',1)[1].split(
    '},ShopReferenceLayout.featureLeft',1)[0]
assert 'elevation=dp(8).toFloat()' not in feature, "FEATURED pane hid native labels"
# Price content and actual buy callback are on the same Android Button.
assert 'private fun priceButton(price:Int,gold:Boolean,' in shop
assert 'return button(NumberFormat.getIntegerInstance().format(price),' in shop
assert 'setCompoundDrawablesRelative(coin,null,null,null)' in shop
assert 'Bopavi3DTheme.Finish.GREEN' in shop
assert '183f,ShopHeaderLayout.titleTop,575f,201f' in shop
assert 'card.addView(illustration,LinearLayout.LayoutParams(-1,0,1.53f))' in shop
assert '},LinearLayout.LayoutParams(-1,0,.86f))' in shop
assert 'setAutoSizeTextTypeUniformWithConfiguration(10,14,1,' in shop
assert 'includeFontPadding=false' in shop


assert 'importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO' in shop



assert 'stage.place(priceButton(featured.price,true,' in shop
assert 'val buy=priceButton(item.price,false,item.accessibility+' in shop
assert 'background=Bopavi3DTheme.panel(Bopavi3DTheme.Finish.CREAM)' not in shop
assert 'contentDescription="OWNED: ${item.owned}/99. "+item.accessibility' in shop
assert 'message("OWNED: ${item.owned}/99  •  TIER' not in shop
assert 'stage.place(message("TIER ${featured.tier}' not in shop
assert 'message(item.storefrontTitle,17' in shop
assert 'message(item.perkEffect,14' in shop
assert 'item.iconTierLabel' in shop
assert 'val storefrontTitle:String get()' in model
assert 'val perkEffect:String get()' in model
assert 'val iconTierLabel:String get()' in model
assert 'storefrontCardsHaveSixDistinctRealProducts' in test
assert '("Shop","android-shop-reference-size")' in qa
assert 'screenshot(capture)==(941,1672)' in qa
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
assert 'stage.place(ShopCounterFront(host),23f,546f,895f,36f)' not in shop
assert 'ShopEmbossedTitle(host)' in shop
assert 'contentDescription="SHOP"' in (src/"ShopEmbossedTitle.kt").read_text()
assert '183f,ShopHeaderLayout.titleTop,575f,201f' in shop
headline=(src/"ShopEmbossedTitle.kt").read_text()
assert 'internal class ShopEmbossedTitle(context:Context):TextView(context)' in headline
assert 'text="SHOP"' in headline
assert 'intArrayOf(0xfffffaaa.toInt(),0xffffd932.toInt(),' in headline
assert 'contentDescription="SHOP"' in headline
assert 'Bopavi3DTheme.Finish.GREEN' in shop
assert 'ShopItemState.fromStore(progress)' in shop
assert 'onBuy(item.index)' in shop
assert 'R.drawable.reference_screenshot' not in decor
assert "chest(c,666f,474f,101f,88f)" in decor
assert "chest(c,138f,518f,78f,45f)" in decor
assert "kioskSparkles(c)" in decor
assert "class ShopForegroundFoliage(" in decor
assert "internal class ShopPerkArtwork(" in decor
assert "internal class ShopCounterFront(" in decor
assert "if(item.tier>=2)drawSymbol" in decor
assert "if(item.tier>=3)drawSymbol" in decor
assert "isClickable=false" in decor and "isFocusable=false" in decor
assert 'stage.place(ShopForegroundFoliage(host),0f,0f,941f,1672f)' in shop
assert "if(featured.canBuy)onBuy(featured.index)" in shop
assert "item.soldOut -> android.widget.Toast.makeText" in shop
assert "!item.affordable -> android.widget.Toast.makeText" in shop
enamels=(src/"ShopReferenceEnamels.kt").read_text()
for cls in ("ShopCreamCardDrawable","ShopFeaturedPanelDrawable"):
    assert "class "+cls in enamels
assert "LinearGradient" in enamels and "RadialGradient" in enamels
assert "class ShopPlusControl(" in enamels
assert "isClickable=true" in enamels and "setOnClickListener { onTap() }" in enamels
assert 'stage.place(ShopPlusControl(host,"How to earn stars")' in shop
assert 'stage.place(ShopPlusControl(host,' in shop
assert 'if(progress.bonusCoinsAvailable()>0)onClaim()' in shop
assert '"Earn coins by completing levels."' in shop
assert '"Earn stars by completing levels."' in shop
assert 'R.drawable.reference_screenshot' not in enamels

print("PASS: reference 3x2 Shop preserves six prices, offline buys, native access and bonuses")
