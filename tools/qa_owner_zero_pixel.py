#!/usr/bin/env python3
"""Strict 0% owner-art acceptance for REAL native Android screen captures.

Compare decoded RGB values at each owner's original pixel grid.
No image resizing, color normalization, masking, cropping, screenshot
substitution, invented wallet balances, or reference-image UI shortcuts.
This reports measurable equality; device provenance must be verified by QA.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageChops, ImageStat

SIZE = (864, 1536)
SCREEN_SIZE = {
    "Splash": SIZE,
    "Home": SIZE,
    "Gameplay": (941, 1672),
    "LevelMap": (941, 1672),
    "Shop": (941, 1672),
    "Skins": (941, 1672),
    "Settings": (941, 1672),
}
OWNER_SHA256 = {
    "Splash": "18dd38645ad3b4255e08723ba1ef7a5b8eaf4702dbda90cae238e77e17524d2a",
    "Home": "358f7369690d28d63d9b14fc178e819dd6535a982bd9c110c15064bf3c5214aa",
    # Original owner-uploaded 941x1672 references; these are hashes only,
    # NOT flattened production screenshots or fake clickable background art.
    "Gameplay": "b80164e67da0e1b56e5b05d2f80fb383721d1a812dca5407255793a4de1ded08",
    "LevelMap": "f648d884b89d8c74049dd2f91841896dc7e931ed381773b5a8dc3e2566448c2a",
    "Shop": "9e7b6f8aebe0a129c1a048459d3f24f58c7fb9c82d797bb12d3fd418305d21d8",
    "Skins": "37c2ef39d3f5fd09b5bef33ef1ef6242f316c54dd96984a742d697988349b5ff",
    "Settings": "1c0585aff3c9374b6afe3288358c7b1bec814947409ddb91019d104e1c27dd2a",
}


def score(name: str, owner_file: Path, native_file: Path, expected_sha: str) -> dict:
    if name not in OWNER_SHA256:
        raise ValueError("Only registered owner reference screens are supported")
    size = SCREEN_SIZE[name]
    sha = hashlib.sha256(owner_file.read_bytes()).hexdigest()
    if sha != expected_sha:
        raise ValueError(f"{name}: wrong owner reference SHA-256: {sha}")
    with Image.open(owner_file) as art:
        if art.size != size or art.format not in ("JPEG", "PNG"):
            raise ValueError(f"{name}: owner reference must be exactly {size[0]}x{size[1]}")
        reference = art.convert("RGB")
    with Image.open(native_file) as image:
        if image.size != size or image.format != "PNG":
            raise ValueError(f"{name}: native capture must be an unscaled {size[0]}x{size[1]} PNG")
        actual = image.convert("RGB")
    difference = ImageChops.difference(reference, actual)
    # Count every changed RGB pixel exactly, even when only the blue
    # channel differs by one unit. The maximum of all three channel
    # deltas is zero ONLY for a truly identical pixel.
    red, green, blue = difference.split()
    changed = size[0] * size[1] - ImageChops.lighter(
        ImageChops.lighter(red, green), blue).histogram()[0]
    channel_mae = sum(ImageStat.Stat(difference).mean) / 3.0
    return {
        "screen": name,
        "owner_sha256": sha,
        "native_sha256": hashlib.sha256(native_file.read_bytes()).hexdigest(),
        "width": size[0],
        "height": size[1],
        "total_pixels": size[0] * size[1],
        "different_pixels": changed,
        "rgb_mae": channel_mae,
        "rgb_deviation_percent": channel_mae / 255 * 100,
        "zero_pixel_deviation": changed == 0,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--owner-splash", type=Path)
    parser.add_argument("--owner-home", type=Path)
    parser.add_argument("--native-splash", type=Path)
    parser.add_argument("--native-home", type=Path)
    parser.add_argument("--report", type=Path)
    parser.add_argument("--owner-gameplay", type=Path)
    parser.add_argument("--native-gameplay", type=Path)
    parser.add_argument("--owner-level-map", type=Path)
    parser.add_argument("--native-level-map", type=Path)
    parser.add_argument("--owner-shop", type=Path)
    parser.add_argument("--native-shop", type=Path)
    parser.add_argument("--owner-skins", type=Path)
    parser.add_argument("--native-skins", type=Path)
    parser.add_argument("--owner-settings", type=Path)
    parser.add_argument("--native-settings", type=Path)
    args = parser.parse_args()
    result = {
        "goal": "0% unscaled RGB difference on every selected owner screen",
        "warning": "Passing a pixel comparison alone does not establish that the "
                   "capture came from a running, interactive Android app. "
                   "Retain adb screen-state and native UI hierarchy evidence.",
        "screens": [],
    }
    pairs = (
        ("Splash", args.owner_splash, args.native_splash),
        ("Home", args.owner_home, args.native_home),
        ("Gameplay", args.owner_gameplay, args.native_gameplay),
        ("LevelMap", args.owner_level_map, args.native_level_map),
        ("Shop", args.owner_shop, args.native_shop),
        ("Skins", args.owner_skins, args.native_skins),
        ("Settings", args.owner_settings, args.native_settings),
    )
    for name, owner, native in pairs:
        if (owner is None) != (native is None):
            parser.error(f"{name}: supply both --owner and --native file")
        if owner is not None:
            result["screens"].append(score(name, owner, native, OWNER_SHA256[name]))
    if not result["screens"]:
        parser.error("Provide at least one paired original and actual Android screenshot")
    result["both_zero"] = all(s["zero_pixel_deviation"] for s in result["screens"])
    result["all_selected_zero"] = result["both_zero"]
    output = json.dumps(result, indent=2, ensure_ascii=False) + "\n"
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(output, encoding="utf-8")
    print(output, end="")
    if not result["both_zero"]:
        raise SystemExit("FAIL: selected owner screens have nonzero changed RGB pixels")


if __name__ == "__main__":
    main()
