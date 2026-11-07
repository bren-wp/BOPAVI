#!/usr/bin/env python3
"""Measure the owner's 864x1536 Splash/Home JPEGs against *real* 941x1672
Android captures. Only the owner source is resized; actual screenshots
remain byte-for-byte unchanged. No compositing, inpainting or fake counters.
"""
from __future__ import annotations
import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageChops, ImageStat

REFERENCE_SIZE = (864, 1536)
CAPTURE_SIZE = (941, 1672)
MAX_RGB_MAE = 25.5  # <=10% of one 8-bit channel
REGIONS = {
    "Splash": {
        "winged_logo": (95, 90, 855, 398),
        "flying_bopi": (85, 390, 820, 885),
        "foreground_gold_rings": (0, 825, 800, 1260),
        "winged_loading_track": (105, 1300, 840, 1508),
        "fantasy_left": (0, 100, 120, 1290),
        "fantasy_right": (820, 100, 941, 1290),
    },
    "Home": {
        "wallets": (445, 8, 930, 105),
        "winged_logo": (100, 98, 840, 390),
        "flying_bopi": (100, 385, 810, 886),
        "gold_play": (100, 881, 840, 1080),
        "skins_and_shop": (119, 1080, 830, 1250),
        "settings": (215, 1220, 722, 1400),
        "footer_medallions": (180, 1420, 770, 1630),
        "fantasy_left": (0, 100, 120, 1250),
        "fantasy_right": (830, 100, 941, 1260),
    },
}


def average_rgb_abs(first: Image.Image, second: Image.Image) -> float:
    if first.size != second.size:
        raise ValueError("Cannot compare images of differing sizes")
    return sum(ImageStat.Stat(ImageChops.difference(first, second)).mean) / 3.0


def report_screen(name: str, owner_path: Path, capture_path: Path) -> dict:
    if name not in REGIONS:
        raise ValueError("Only owner Splash and Home can be scored")
    with Image.open(owner_path) as reference:
        if reference.size != REFERENCE_SIZE or reference.format not in ("JPEG", "PNG"):
            raise ValueError(f"Owner {name} must be unaltered 864x1536 JPEG/PNG")
        owner = reference.convert("RGB").resize(
            CAPTURE_SIZE, Image.Resampling.LANCZOS)
    with Image.open(capture_path) as frame:
        if frame.format != "PNG" or frame.size != CAPTURE_SIZE:
            raise ValueError(f"Actual {name} must be native 941x1672 PNG")
        real = frame.convert("RGB")
    overall = average_rgb_abs(owner, real)
    regions = {label: average_rgb_abs(owner.crop(roi), real.crop(roi))
               for label, roi in REGIONS[name].items()}
    success = overall <= MAX_RGB_MAE and all(
        value <= MAX_RGB_MAE for value in regions.values())
    return {
        "screen": name,
        "original_sha256": hashlib.sha256(owner_path.read_bytes()).hexdigest(),
        "capture_sha256": hashlib.sha256(capture_path.read_bytes()).hexdigest(),
        "original_resolution": REFERENCE_SIZE,
        "capture_resolution": CAPTURE_SIZE,
        "reference_only_resampled": True,
        "whole_screen_rgb_mae": round(overall, 3),
        "whole_screen_deviation_percent": round(overall * 100 / 255, 3),
        "regions": [
            {"name": n, "rgb_mae": round(value, 3),
             "deviation_percent": round(value * 100 / 255, 3),
             "within_10_percent": value <= MAX_RGB_MAE}
            for n, value in sorted(regions.items(), key=lambda v: -v[1])
        ],
        "meets_all_targets": success,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--owner-splash", required=True, type=Path)
    parser.add_argument("--owner-home", required=True, type=Path)
    parser.add_argument("--real-splash", required=True, type=Path)
    parser.add_argument("--real-home", required=True, type=Path)
    parser.add_argument("--json-report", type=Path)
    parser.add_argument("--enforce-target", action="store_true")
    args = parser.parse_args()
    result = {
        "threshold": {"normalized_deviation_percent": 10,
                      "rgb_mae": MAX_RGB_MAE},
        "source_warning": "JPEG reference compression and 864-to-941 resampling "
                          "prevent a literal zero-difference comparison; "
                          "this is a diagnostic, not an assertion of 1:1.",
        "screens": [
            report_screen("Splash", args.owner_splash, args.real_splash),
            report_screen("Home", args.owner_home, args.real_home),
        ],
    }
    result["both_screens_meet_target"] = all(
        x["meets_all_targets"] for x in result["screens"])
    output = json.dumps(result, indent=2, ensure_ascii=False) + "\n"
    if args.json_report:
        args.json_report.parent.mkdir(parents=True, exist_ok=True)
        args.json_report.write_text(output, encoding="utf-8")
    print(output, end="")
    if args.enforce_target and not result["both_screens_meet_target"]:
        raise SystemExit("FAIL: Splash and Home require <=10% in every region")


if __name__ == "__main__":
    main()
