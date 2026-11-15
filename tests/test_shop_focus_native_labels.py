#!/usr/bin/env python3
"""Prevent the API36 Shop wallet regex crash observed in run 43647246363.

Pure parser test only: generated UI labels are fixtures, not claimed screenshots.
Actual click/debit and Play-to-GameView remain real emulator acceptance.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from qa_android_shop_focus import COIN_AMOUNT, OWNED_AMOUNT

for spoken, expected in [
    ("Earned coins: 0", 0),
    ("Earned coins: 1", 1),
    ("Earned coins: 12500", 12500),
    (" EARNED COINS:  73 ", 73),
]:
    match = COIN_AMOUNT.search(spoken)
    assert match and int(match.group(1)) == expected, spoken

for spoken, expected in [
    ("Početni štit, price 80 coins, owned 0 of 99, not enough coins", 0),
    ("Super magnet, price 195 coins, owned 2 of 99, available to buy", 2),
    ("POČETNI MAGNET, price 65 coins, owned 99 of 99, inventory full", 99),
]:
    match = OWNED_AMOUNT.search(spoken)
    assert match and int(match.group(1)) == expected, spoken

for text in ["Earned rewards", "Not enough coins"]:
    assert not COIN_AMOUNT.search(text)
assert not OWNED_AMOUNT.search("owned five of 99")
assert "\\\\s" not in COIN_AMOUNT.pattern, "Regex must not match a literal backslash"
assert "\\\\s" not in OWNED_AMOUNT.pattern
print("PASS: API36 real-wallet and six-SKU inventory accessibility label parsing")
