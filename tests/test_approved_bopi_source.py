#!/usr/bin/env python3
"""Verify the shipped owner-reference Bopi source and real generated native pixels.

This test intentionally uses committed real art, never synthetic test thumbnails.
A passing check demonstrates asset provenance/integrity, not full-screen 1:1 QA.
"""
from __future__ import annotations

from hashlib import sha256
from pathlib import Path
import json

from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "art/reference-approved/bopi"
ANDROID = ROOT / "android/app/src/main/res/drawable-nodpi"
EXPECTED = {
    "bopi0_body.webp": "b9872a4750392b8ec99851c5fc656751f05e1714ed5edeb26ab45d055443df74",
    "bopi0_left.webp": "f61be45fa1c08c54199b17b14e148cd79a944cda255a9527ac74b98c30f8f8ae",
    "bopi0_right.webp": "0101670d2ab6f6164ba6f133e68dca654b0abc48a618c659aceefce1cd27f071",
}

assert SOURCE.is_dir(), "Approved source art must be committed, not fetched in CI"
assert {path.name for path in SOURCE.iterdir()} == set(EXPECTED) | {"manifest.json", "README.txt"}
manifest = json.loads((SOURCE / "manifest.json").read_text(encoding="utf-8"))
assert manifest["canvas_size"] == [224, 192]
assert manifest["source_size"] == [941, 1672]
assert manifest["layers"] == list(EXPECTED)
assert manifest["requires_visual_review"] is True

for filename, digest in EXPECTED.items():
    payload = (SOURCE / filename).read_bytes()
    assert sha256(payload).hexdigest() == digest, filename
    assert manifest[filename + "_sha256"] == digest, filename
    with Image.open(SOURCE / filename) as decoded:
        assert decoded.format == "WEBP" and decoded.size == (224, 192), filename
        rgba = decoded.convert("RGBA")
    alpha = rgba.getchannel("A")
    assert alpha.getbbox(), filename
    assert alpha.getextrema()[0] == 0 and alpha.getextrema()[1] == 255, filename
    assert len(rgba.getcolors(maxcolors=1_000_000) or []) > 150, (
        filename, "Source is an unexpectedly flat/generated placeholder"
    )

pairs = (
    ("bopi0", "Bopi0", (512, 512)),
    ("bopileft0", "BopiLeft0", (512, 512)),
    ("bopiright0", "BopiRight0", (512, 512)),
    ("bopiportrait0", "BopiPortrait0", (512, 512)),
    ("bopireference0", "BopiReference0", (2, 2)),
)
images = {}
for name, catalog, size in pairs:
    android = ANDROID / (name + ".png")
    assert android.is_file(), name
    with Image.open(android) as native:
        assert native.format == "PNG" and native.size == size, name
        images[name] = native.convert("RGBA")
    assert images[name].getchannel("A").getbbox(), name

assert images["bopireference0"].getpixel((0, 0)) == (255, 255, 255, 255), (
    "Runtime must identify the validated reference-art shoulder anchors"
)
for wing in ("bopileft0", "bopiright0"):
    assert ImageChops.difference(images["bopi0"], images[wing]).getbbox(), (
        wing, "Body and wing are not independent"
    )
assert ImageChops.difference(images["bopiportrait0"], images["bopi0"]).getbbox(), (
    "Profile portrait must include both wings"
)
print("PASS: three real approved photo layers, frozen SHA-256, two native animated wing assets, "
      "true winged portrait and verified Android PNG integrity")
