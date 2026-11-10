#!/usr/bin/env python3
"""Owner's active scope: direct Home startup and real Shop/Skins controls.

This is a source-level regression test, NOT a claim of zero RGB deviation.
The separately captured Android 15/16 screenshots remain the visual authority.
"""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text(encoding="utf-8")
shop=(src/"ShopScreen.kt").read_text(encoding="utf-8")
skins=(src/"PilotSelectionScreen.kt").read_text(encoding="utf-8")
menus=(src/"ReferenceMenus.kt").read_text(encoding="utf-8")
manifest=(root/"android/app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
styles=(root/"android/app/src/main/res/values/styles.xml").read_text(encoding="utf-8")
workflow=(root/".github/workflows/native-ci.yml").read_text(encoding="utf-8")
readme=(root/"README.md").read_text(encoding="utf-8")

startup=activity.split("override fun onCreate(state: Bundle?) {",1)[1].split(
    "override fun onConfigurationChanged(",1)[0]
assert "showBrandedStartup()" not in activity
assert 'selectedScreen="splash"' not in activity
assert 'else showHome()' in startup
assert 'if(qaIntentEnabled("bopavi_qa_loading_preview"))' in startup
assert 'android:theme="@style/BopaviTheme"' in manifest
assert "BopaviLaunchTheme" not in manifest and "BopaviLaunchTheme" not in styles
assert not (root/"android/app/src/main/res/drawable/bopavi_startup.xml").exists()
assert not (root/"android/app/src/main/res/values-v31/styles.xml").exists()
assert "BOPAVI-Android-Splash.png" not in workflow
assert "BOPAVI-Android-Splash.png" not in readme
assert 'test "$(wc -l < SHA256SUMS.txt)" -eq 20' in workflow

# Every price must be visible ON the actual native buy button, not on a
# decorative sibling that may disappear on the GPU/Android Z stack.
assert 'return button(NumberFormat.getIntegerInstance().format(price),' in shop
assert 'setCompoundDrawablesRelative(coin,null,null,null)' in shop
assert 'contentDescription=accessibility' in shop
assert 'else -> onBuy(item.index)' in shop
assert 'onBuy(featured.index)' in shop

# Both wallet plus targets genuinely open Shop. The four cards retain save
# ownership constraints and may not silently spend currency on selection.
assert 'ShopPlusControl(host,"Open Shop for coins"){onShop()}' in skins
assert 'ShopPlusControl(host,"Open Shop for rewards"){onShop()}' in skins
assert 'onShop={showShop()}' in activity
assert 'create(choice,onChoose,onUnlock,onShop,onBack)' in menus
assert 'PilotSelectionModel.locked(index,orionOwned)' in skins
assert 'setOnClickListener{chooseOrUnlock(index)}' in skins
assert 'selectOrBuy(' not in skins

print("PASS: direct Home without custom Splash; native Shop prices, honest Skins wallets and purchases")
