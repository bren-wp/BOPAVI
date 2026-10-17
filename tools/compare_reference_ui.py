#!/usr/bin/env python3
"""Compare genuine device captures with user-owned reference screenshots.

Usage:
    python tools/compare_reference_ui.py --actual qa/screenshots --reference refs \
      --map home=home.jpg --map worlds=worlds.jpg --map settings=settings.jpg

Reference images stay local/private; this never produces fake runtime screenshots.
It does not normalize layouts across different aspect ratios or certify UI behavior.
"""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageOps, ImageChops, ImageStat


def measure(reference: Path, actual: Path) -> dict:
    with Image.open(reference) as ref_src, Image.open(actual) as actual_src:
        ref = ImageOps.exif_transpose(ref_src).convert("RGB")
        screen = ImageOps.exif_transpose(actual_src).convert("RGB")
        original_reference_size = list(ref.size)
        device_size = list(screen.size)
        # Exact comparison always uses ORIGINAL decoded full-resolution pixels,
        # never the downsampled triage images (which can hide fine differences).
        full_resolution_exact = (original_reference_size == device_size and
            ImageChops.difference(ref, screen).getbbox() is None)
        if original_reference_size != device_size:
            # Center crop approximates cover-mode artwork; record this explicitly
            # because unequal aspect ratios cannot possibly be pixel-identical.
            ref = ImageOps.fit(ref, screen.size, Image.Resampling.LANCZOS)
        # Small common resolution avoids huge memory use and is suited to triage,
        # NOT a substitute for a full-resolution visual sign-off.
        size = (min(screen.width, 360), min(screen.height, 800))
        ref = ref.resize(size, Image.Resampling.LANCZOS)
        screen = screen.resize(size, Image.Resampling.LANCZOS)
        diff = ImageChops.difference(ref, screen)
        stats = ImageStat.Stat(diff)
        mae = sum(stats.mean) / 3.0
        bins = diff.convert("L").histogram()
        exact_fraction = bins[0] / float(sum(bins))
        near_fraction = sum(bins[:13]) / float(sum(bins))
        return {
            "reference_dimensions": original_reference_size,
            "actual_dimensions": device_size,
            "aspect_ratio_mismatch": original_reference_size[0] * device_size[1]
                != original_reference_size[1] * device_size[0],
            "rgb_mean_absolute_error_0_255": round(mae, 2),
            "exact_luma_fraction": round(exact_fraction, 6),
            "near_luma_fraction_tolerance_12": round(near_fraction, 6),
            "pixel_identical": full_resolution_exact,
        }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--map", action="append", required=True,
                        metavar="SCREEN=REFERENCE_FILE")
    parser.add_argument("--out", type=Path)
    parser.add_argument("--require-exact", action="store_true")
    args = parser.parse_args()
    report = {}
    for mapping in args.map:
        if "=" not in mapping:
            parser.error("--map must be SCREEN=REFERENCE_FILE")
        screen, name = mapping.split("=", 1)
        if not screen or not name or "/" in screen or "\\" in screen:
            parser.error("Invalid SCREEN in mapping")
        reference = args.reference / name
        capture = args.actual / ("android-" + screen + ".png")
        if not reference.is_file() or not capture.is_file():
            parser.error("Missing matching reference/capture: " + str(reference) +
                         " or " + str(capture))
        report[screen] = measure(reference, capture)
    output = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.out:
        args.out.parent.mkdir(parents=True, exist_ok=True)
        args.out.write_text(output, encoding="utf-8")
    else:
        print(output, end="")
    return int(args.require_exact and not all(
        data["pixel_identical"] for data in report.values()))


if __name__ == "__main__":
    raise SystemExit(main())
