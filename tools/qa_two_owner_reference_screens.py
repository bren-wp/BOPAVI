#!/usr/bin/env python3
"""Measure REAL 941x1672 Home and Loading vs the owner's supplied PNGs.

Diagnostic tool only. No resizing, compositing, generated pictures, artificial
wallet balances, fake loading percentage or acceptance-score manipulation.
Unlike a source-only assertion, this reports actual RGB differences by
screen and independently ranked regions.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image, ImageChops, ImageStat

SIZE = (941, 1672)
REGIONS = {
    "Home": {
        "wallet_and_safe_top": (445, 8, 930, 105),
        "winged_logo_crown": (100, 98, 840, 390),
        "animated_bopi": (100, 385, 810, 886),
        "gold_play": (100, 881, 840, 1080),
        "purple_skins_green_shop": (119, 1080, 830, 1250),
        "blue_settings": (215, 1220, 722, 1400),
        "round_footer_medals": (180, 1420, 770, 1630),
        "left_fantasy_world": (0, 100, 120, 1250),
        "right_fantasy_world": (830, 100, 941, 1260),
    },
    "Splash": {
        "winged_logo_crown": (95, 90, 855, 398),
        "animated_bopi": (85, 390, 820, 885),
        "gold_ring_trail": (0, 825, 800, 1260),
        "3d_loading_track_caption": (105, 1300, 840, 1508),
        "left_fantasy_world": (0, 100, 120, 1290),
        "right_fantasy_world": (820, 100, 941, 1290),
    },
}


def load_png(path: str | Path) -> Image.Image:
    path = Path(path)
    with Image.open(path) as source:
        if source.format != "PNG" or source.size != SIZE:
            raise ValueError(f"{path}: expected original PNG exactly {SIZE}, "
                             f"found {source.format} {source.size}")
        return source.convert("RGB")


def mae(owner: Image.Image, actual: Image.Image,
        roi: tuple[int, int, int, int] | None = None) -> float:
    if owner.size != SIZE or actual.size != SIZE:
        raise ValueError("Pixel comparison requires two unmodified 941x1672 frames")
    a, b = ((owner, actual) if roi is None
            else (owner.crop(roi), actual.crop(roi)))
    return sum(ImageStat.Stat(ImageChops.difference(a, b)).mean) / 3.0


def compare(owner_path: str | Path, actual_path: str | Path,
            name: str) -> dict:
    owner, actual = load_png(owner_path), load_png(actual_path)
    regions = {key: round(mae(owner, actual, bounds), 3)
               for key, bounds in REGIONS[name].items()}
    return {
        "screen": name,
        "resolution": list(SIZE),
        "owner_sha256": hashlib.sha256(Path(owner_path).read_bytes()).hexdigest(),
        "actual_sha256": hashlib.sha256(Path(actual_path).read_bytes()).hexdigest(),
        "whole_screen_rgb_mae": round(mae(owner, actual), 3),
        "regions_ranked": [
            {"name": key, "rgb_mae": value,
             "box": list(REGIONS[name][key])}
            for key, value in sorted(regions.items(), key=lambda kv: -kv[1])
        ],
        "pixel_identical": ImageChops.difference(owner, actual).getbbox() is None,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--home-reference", required=True, type=Path)
    parser.add_argument("--home-actual", required=True, type=Path)
    parser.add_argument("--splash-reference", required=True, type=Path)
    parser.add_argument("--splash-actual", required=True, type=Path)
    parser.add_argument("--json-report", type=Path)
    args = parser.parse_args()

    report = {
        "measurement": "8-bit RGB channel mean absolute error, native 941x1672",
        "warnings": [
            "Animated wing phases and authentic counters need aligned runtime state.",
            "A build passing tests does not prove visual equivalence.",
            "This tool never edits, scales or replaces the original pictures.",
        ],
        "screens": [
            compare(args.home_reference, args.home_actual, "Home"),
            compare(args.splash_reference, args.splash_actual, "Splash"),
        ],
    }
    output = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.json_report:
        args.json_report.parent.mkdir(parents=True, exist_ok=True)
        args.json_report.write_text(output, encoding="utf-8")
    print(output, end="")


if __name__ == "__main__":
    main()
