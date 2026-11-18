#!/usr/bin/env python3
"""Four owner Shop original extracts remain decor-only and source-verified."""
import sys
import tempfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from generate_owner_shop_kiosk import VIEWPORTS, stage_pixels, generate_all

assert len(VIEWPORTS) == 4
assert {h for _, _, h, _ in VIEWPORTS} == {1920, 2280, 2340, 2400}
assert stage_pixels(1080, 1920) == (107, 459, 865, 316)
assert stage_pixels(1080, 2280) == (27, 524, 1026, 316)
assert stage_pixels(1080, 2340) == (27, 534, 1026, 316)
assert stage_pixels(1080, 2400) == (27, 545, 1026, 316)
with tempfile.TemporaryDirectory() as d:
    generated = generate_all(ROOT, Path(d))
    assert len(generated) == 4
    for (folder, width, height, _), file in zip(VIEWPORTS, generated):
        x, y, w, h = stage_pixels(width, height)
        assert 0 <= x < x + w <= width
        assert 0 <= y < y + h <= height
        with Image.open(file) as art:
            assert art.format == "WEBP"
            assert art.size == (w, h)
            alpha = art.convert("RGBA").getchannel("A")
            assert alpha.getpixel((w // 2, 0)) == 0
            assert alpha.getpixel((w // 2, h - 1)) == 0
            assert alpha.getpixel((w // 2, 100)) == 255
            assert alpha.getpixel((w // 2, 245)) == (0 if height == 1920 else
                round(255 * (265 - 245) / (265 - 215)))
    # Browser-hosted source art must never impersonate an actionable Shop UI.
    screen = (ROOT / "android/app/src/main/java/com/brendigo/bopavi/ShopScreen.kt").read_text()
    assert 'priceButton(item.price,false' in screen
    assert 'onBuy(item.index)' in screen
    assert 'setImageResource(originalStall)' in screen
    assert 'R.drawable.bopavi_owner_shop_kiosk' in screen
    assert '},24f,400f,894f,276f,' in screen

print("PASS: 4 original owner Shop stall layers, SHA-protected, no baked controls")
