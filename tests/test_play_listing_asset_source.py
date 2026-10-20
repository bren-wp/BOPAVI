#!/usr/bin/env python3
"""Regression guard: Play feature art must not require deleted runtime Hero PNGs."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
listing = (ROOT / "tools/create_play_listing_assets.py").read_text(encoding="utf-8")
audit = (ROOT / "tools/audit_native_dead_code.py").read_text(encoding="utf-8")
flow = (ROOT / ".github/workflows/native-ci.yml").read_text(encoding="utf-8")
source = ROOT / "docs/assets/hero.svg"

assert source.is_file(), "Original approved SVG must survive dead-asset cleanup"
assert 'hero_svg = Path("docs/assets/hero.svg")' in listing
assert "cairosvg.svg2png(url=str(hero_svg), output_width=900)" in listing
assert 'ImageOps.fit(hero, (590, 500)' in listing
assert 'Image.open(art / "drawable-nodpi/hero.png")' not in listing
assert not (ROOT / "android/app/src/main/res/drawable-nodpi/hero.png").exists()
assert "python -m pip install --quiet pillow cairosvg" in flow
assert "python tools/create_play_listing_assets.py" in flow
assert "python tests/test_play_listing_asset_source.py" in flow
assert "Orphan hero raster" in audit
print("PASS: Play art uses approved SVG, no deleted runtime image, renderer installed")
