#!/usr/bin/env python3
"""Shop/Skins/Settings: preserve genuine owner-pixel evidence and strict scoring."""
from pathlib import Path
import importlib.util
import sys
import tempfile
from PIL import Image

root = Path(__file__).resolve().parents[1]
path = root / "tools" / "qa_three_owner_references.py"
spec = importlib.util.spec_from_file_location("owner_three_screen_comparator", path)
assert spec is not None and spec.loader is not None
tool = importlib.util.module_from_spec(spec)
spec.loader.exec_module(tool)

strict = (root / "tools" / "qa_owner_zero_pixel.py").read_text()
expected = {
    "shop": "9e7b6f8aebe0a129c1a048459d3f24f58c7fb9c82d797bb12d3fd418305d21d8",
    "skins": "37c2ef39d3f5fd09b5bef33ef1ef6242f316c54dd96984a742d697988349b5ff",
    "settings": "1c0585aff3c9374b6afe3288358c7b1bec814947409ddb91019d104e1c27dd2a",
}
for screen, sha in expected.items():
    assert screen in tool.REGIONS
    assert f'"{screen.title()}"' in strict
    assert sha in strict
    for region, box in tool.REGIONS[screen].items():
        left, top, right, bottom = box
        assert region
        assert 0 <= left < right <= 941, (screen, region, box)
        assert 0 <= top < bottom <= 1672, (screen, region, box)

with tempfile.TemporaryDirectory() as tmp:
    directory = Path(tmp)
    owner = directory / "owner.png"
    native = directory / "native.png"
    Image.new("RGB", (941, 1672), (10, 20, 30)).save(owner)
    Image.new("RGB", (941, 1672), (10, 20, 30)).save(native)
    for screen in expected:
        equal = tool.compare(screen, owner, native)
        assert equal["exact_1_to_1_rgb"]
        assert equal["different_pixels"] == 0
        assert equal["pixel_deviation_percent"] == 0
        assert all(item["exact_rgb_match"] for item in equal["regions"].values())
    with Image.open(native) as source:
        modified = source.copy()
    modified.putpixel((500, 680), (11, 20, 30)) # Only one channel, 1/255.
    modified.save(native)
    for screen in expected:
        difference = tool.compare(screen, owner, native)
        assert not difference["exact_1_to_1_rgb"]
        assert difference["different_pixels"] == 1
        assert difference["pixel_deviation_percent"] > 0
        assert any(item["different_pixels"] == 1 for item in difference["regions"].values())

print("PASS: original owner hashes and strict RGB pixel scoring for Shop, Skins, Settings")
