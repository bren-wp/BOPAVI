"""BOPAVI v0.1.96 — verify all eight original, functional fantasy worlds.

This validates native packaged scenery only; it does not claim screenshot
pixel fidelity or modify gameplay collision geometry.
"""
from pathlib import Path
from hashlib import sha256
import sys
import xml.etree.ElementTree as ET
from PIL import Image, ImageStat

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from generate_worlds import (shapes, storybook_landmarks,
                             WORLD_SCENE_SOURCE, REFERENCE_WORLD_NAMES)

assert len(WORLD_SCENE_SOURCE) == len(REFERENCE_WORLD_NAMES) == 8
assert tuple(WORLD_SCENE_SOURCE) == (0, 2, 3, 5, 6, 4, 1, 7)
assert len(set(WORLD_SCENE_SOURCE)) == 8

descriptors = (
    "story-falls", "#de9bff", "#ff8a17", "#ffd274",
    "#c7fff8", "story-gold", "#fff9da", "#ddafff"
)
checksums = set()
for world in range(8):
    source = WORLD_SCENE_SOURCE[world]
    ornament = storybook_landmarks(source)
    assert descriptors[world] in ornament, (world, "missing key landmark")
    assert "glyph" not in ornament and "onclick" not in ornament
    # Full source SVG must be well-formed without network calls.
    xml = shapes(source)
    assert "storybook_landmarks" not in xml  # expanded into real shapes
    root = ET.fromstring(xml)
    assert root.attrib["viewBox"] == "0 0 480 800"
    assert len(root.findall(".//{http://www.w3.org/2000/svg}path")) > 35

    name = f"world{world}.png"
    android = ROOT / "android/app/src/main/res/drawable-nodpi" / name
    assert android.is_file(), (world, "missing background")
    data = android.read_bytes()
    checksums.add(sha256(data).hexdigest())
    with Image.open(android) as artwork:
        artwork = artwork.convert("RGB")
        assert artwork.size == (960, 1600), (world, artwork.size)
        stat = ImageStat.Stat(artwork.resize((80, 135)))
        # Every illustrated biome must have real palette variation rather
        # than a blank flat-color screenshot or placeholder asset.
        assert max(stat.stddev) > 25, (world, stat.stddev)

assert len(checksums) == 8, "Duplicated world backdrop"
print("PASS: eight distinct illustrated 960×1600 fantasy biomes, packaged for Android")
