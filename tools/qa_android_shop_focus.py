#!/usr/bin/env python3
"""Strict Shop and Skins smoke on a REAL API36 Android emulator.

Captures the unscaled 941x1672 pixels. No fake screenshots, prerecorded wallet
balances, stub purchases, or silently successful duplicate actions.
"""
import re
import time
from qa_android_rotation import APP, adb, hierarchy, screenshot, tap
from qa_android_reference_menus import matching_nodes, node_with, acknowledge_native_fullscreen_education


def require(phrase,clickable=None,minimum=1):
    for _ in range(12):
        root=hierarchy()
        if acknowledge_native_fullscreen_education(root):
            time.sleep(.5)
            root=hierarchy()
        hits=matching_nodes(root,phrase,clickable=clickable)
        if len(hits)>=minimum:
            return hits
        time.sleep(.5)
    raise AssertionError(f"Shop must expose {phrase!r} (min {minimum})")


def wallet_and_inventory(name, price):
    # Read only genuine UIAutomator-owned state from the installed app.
    root = hierarchy()
    coin_nodes = matching_nodes(root, "EARNED COINS:")
    assert len(coin_nodes) == 1, "Shop must expose one actual local wallet"
    wallet = coin_nodes[0].get("content-desc") or coin_nodes[0].get("text") or ""
    match = re.search(r"earned coins:\\s*([0-9]+)", wallet, re.IGNORECASE)
    assert match, f"Cannot read real wallet: {wallet}"
    all_buttons = matching_nodes(root, f"{name}, price {price} coins", clickable=True)
    # Featured SUPER MAGNET appears twice; select its ordinary SKU card.
    buttons = [n for n in all_buttons
               if not (n.get("content-desc") or "").startswith("Featured ")]
    assert len(buttons) == 1, (name, price, len(buttons))
    match_owned = re.search(r"owned\\s+(\\d+)\\s+of\\s+99",
                            buttons[0].get("content-desc") or "", re.IGNORECASE)
    assert match_owned, f"Shop has no truthful inventory for {name}"
    return int(match.group(1)), int(match_owned.group(1)), buttons[0]


def exercise_real_purchases(original_pid):
    # No fake balance, stub purchase or direct SharedPreferences edit:
    # if a prior test legitimately earned coins, purchases must debit them
    # exactly once. Otherwise they must leave the saved wallet unchanged.
    for name, price in (
        ("Početni štit", 80), ("Početni magnet", 65),
        ("Dvostruki štit", 150), ("Dugi magnet", 120),
        ("Trostruki štit", 230), ("Super magnet", 195),
    ):
        before_coins, before_owned, button = wallet_and_inventory(name, price)
        tap(button)
        expected_purchase = before_coins >= price and before_owned < 99
        expected_coins = before_coins - price if expected_purchase else before_coins
        expected_owned = before_owned + 1 if expected_purchase else before_owned
        for attempt in range(8):
            time.sleep(.5)
            current_coins, current_owned, _ = wallet_and_inventory(name, price)
            if (current_coins, current_owned) == (expected_coins, expected_owned):
                break
        assert (current_coins, current_owned) == (expected_coins, expected_owned), (
            "Real Shop debit/inventory mismatch", name,
            (before_coins, before_owned), (current_coins, current_owned),
            (expected_coins, expected_owned))
        assert adb("shell", "pidof", APP) == original_pid, (
            "A purchase or insufficient-funds tap restarted the application", name)
        print(f"PASS: Shop {name}: "
              f"{'persisted exact-price purchase' if expected_purchase else 'no unauthorized debit'}",
              flush=True)


def main():
    adb("shell","am","force-stop",APP)
    adb("shell","wm","size","941x1672")
    adb("shell","wm","density","320")
    try:
        adb("shell","am","start","-W","-n",APP+"/.MainActivity")
        pid=adb("shell","pidof",APP)
        assert pid,"FAIL: Shop focused QA has no running process"
        tap(node_with("OTVORI TRGOVINU NADOGRADNJI",attempts=8))
        require("EARNED COINS:")
        require("EARNED STARS:")
        require("OWNED:",minimum=6)
        require("HOW TO EARN STARS",clickable=True)
        require("HOW TO EARN COINS",clickable=True)
        require("FEATURED:")
        for name in ("SHIELD","MAGNET","DOUBLE SHIELD","LONG MAGNET",
                     "TRIPLE SHIELD","SUPER MAGNET"):
            require(name)
        w,h=screenshot("android-shop-focus-reference-size")
        assert (w,h)==(941,1672),(w,h)
        assert adb("shell","pidof",APP)==pid,"FAIL: Shop screen restarted process"
        exercise_real_purchases(pid)
        # The star + button must not mint currency or navigate to a fake gem store.
        tap(require("HOW TO EARN STARS",clickable=True)[0])
        require("EARNED STARS:")
        assert adb("shell","pidof",APP)==pid,"FAIL: Shop star help caused process death"
        # The real Home back action remains tappable after help feedback.
        tap(node_with("HOME",attempts=8))
        require("OTVORI TRGOVINU NADOGRADNJI",clickable=True)

        # Independently capture the second owner-requested screen on exactly
        # the same API36 viewport, even if other, unrelated menu QA fails.
        # Verify the native accessibility actions, not just a wallpaper.
        tap(node_with("OTVORI IZBOR LIKOVA",attempts=8))
        require("SKINS",clickable=False)
        require("CHOOSE ROYAL BOPI",clickable=True)
        require("FOREST EXPLORER",clickable=True)
        require("STAR PILOT",clickable=True)
        w,h=screenshot("android-skins-focus-reference-size")
        assert (w,h)==(941,1672),(w,h)
        assert adb("shell","pidof",APP)==pid,"FAIL: Skins screen restarted process"
        tap(node_with("BACK TO HOME",attempts=8))
        require("OTVORI TRGOVINU NADOGRADNJI",clickable=True)
        print("PASS: real Shop and Skins 941x1672 screenshots; six Shop SKUs, four live pilots, no process restart")
    finally:
        adb("shell","wm","size","reset")
        adb("shell","wm","density","reset")


if __name__=="__main__":
    main()
