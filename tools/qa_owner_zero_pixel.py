#!/usr/bin/env python3
"""Strict 0% owner-art acceptance for REAL native Android Splash/Home captures.

Compare decoded RGB values on precisely the owner's 864x1536 pixel grid.
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
OWNER_SHA256 = {
    "Splash": "18dd38645ad3b4255e08723ba1ef7a5b8eaf4702dbda90cae238e77e17524d2a",
    "Home": "358f7369690d28d63d9b14fc178e819dd6535a982bd9c110c15064bf3c5214aa",
}


def score(name: str, owner_file: Path, native_file: Path, expected_sha: str) -> dict:
    if name not in OWNER_SHA256:
        raise ValueError("Only Splash and Home are in scope")
    sha = hashlib.sha256(owner_file.read_bytes()).hexdigest()
    if sha != expected_sha:
        raise ValueError(f"{name}: wrong owner reference SHA-256: {sha}")
    with Image.open(owner_file) as art:
        if art.size != SIZE or art.format not in ("JPEG", "PNG"):
            raise ValueError(f"{name}: owner reference must be exactly 864x1536")
        reference = art.convert("RGB")
    with Image.open(native_file) as image:
        if image.size != SIZE or image.format != "PNG":
            raise ValueError(f"{name}: native capture must be an unscaled 864x1536 PNG")
        actual = image.convert("RGB")
    difference = ImageChops.difference(reference, actual)
    changed = sum(1 for rgb in difference.getdata() if any(rgb))
    channel_mae = sum(ImageStat.Stat(difference).mean) / 3.0
    return {
        "screen": name,
        "owner_sha256": sha,
        "native_sha256": hashlib.sha256(native_file.read_bytes()).hexdigest(),
        "width": SIZE[0],
        "height": SIZE[1],
        "total_pixels": SIZE[0] * SIZE[1],
        "different_pixels": changed,
        "rgb_mae": round(channel_mae, 6),
        "rgb_deviation_percent": round(channel_mae / 255 * 100, 6),
        "zero_pixel_deviation": changed == 0,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--owner-splash", type=Path, required=True)
    parser.add_argument("--owner-home", type=Path, required=True)
    parser.add_argument("--native-splash", type=Path, required=True)
    parser.add_argument("--native-home", type=Path, required=True)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    result = {
        "goal": "0% RGB difference in both original 864x1536 images",
        "warning": "Passing a pixel comparison alone does not establish that the "
                   "capture came from a running, interactive Android app. "
                   "Retain adb screen-state and native UI hierarchy evidence.",
        "screens": [
            score("Splash", args.owner_splash, args.native_splash,
                  OWNER_SHA256["Splash"]),
            score("Home", args.owner_home, args.native_home,
                  OWNER_SHA256["Home"]),
        ],
    }
    result["both_zero"] = all(s["zero_pixel_deviation"] for s in result["screens"])
    output = json.dumps(result, indent=2, ensure_ascii=False) + "\n"
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(output, encoding="utf-8")
    print(output, end="")
    if not result["both_zero"]:
        raise SystemExit("FAIL: Splash/Home 0% reference target not achieved")


if __name__ == "__main__":
    main()
