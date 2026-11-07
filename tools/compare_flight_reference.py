#!/usr/bin/env python3
"""Compare a deterministic real Android capture with the approved flight reference.

Both images MUST have identical dimensions, game state, animation phase,
locale, system insets and device viewport. This tool cannot establish those
preconditions by itself; it refuses to stretch screenshots into fake matches.
"""
import argparse
import json
import math
from pathlib import Path
from PIL import Image, ImageChops, ImageStat

# Coarse normalized masks for the user's portrait gameplay compositions.
# Separate top corners, full-width HUD and both pillar halves: a low
# environment error must never conceal a badly sized Bopi/fox/owl or cap.
# Overlap is deliberate; each region is a separate diagnostic, not a sum.
REGIONS = {
    "hud": (0.00, 0.00, 1.00, 0.15),
    "hud_wallets": (0.00, 0.00, 0.49, 0.07),
    "hud_score": (0.27, 0.02, 0.73, 0.15),
    "hud_pause": (0.84, 0.00, 1.00, 0.10),
    "hero": (0.07, 0.31, 0.59, 0.59),
    "obstacles": (0.30, 0.08, 0.96, 0.94),
    "upper_pillar": (0.29, 0.08, 0.92, 0.47),
    "lower_pillar": (0.31, 0.50, 0.92, 0.94),
    "pickups": (0.10, 0.24, 1.00, 0.67),
    "tutorial": (0.27, 0.71, 0.72, 0.84),
    "bottom": (0.00, 0.84, 1.00, 1.00),
}


def metrics(ref: Image.Image, actual: Image.Image, threshold: int) -> dict:
    difference = ImageChops.difference(ref, actual)
    channels = difference.split()
    stats = ImageStat.Stat(difference)
    n_pixels = ref.width * ref.height
    maximum = ImageChops.lighter(ImageChops.lighter(channels[0], channels[1]), channels[2])
    mismatched = maximum.point(lambda value: 255 if value > threshold else 0)
    bad_pixels = mismatched.histogram()[255]
    return {
        "mae_rgb": round(sum(stats.mean) / 3.0, 4),
        "rmse_rgb": round(math.sqrt(sum(x * x for x in stats.rms) / 3.0), 4),
        "changed_pixels_above_threshold": bad_pixels,
        "changed_fraction": round(bad_pixels / n_pixels, 6),
        "pixel_identical": max(maximum.getextrema()) == 0,
    }


def compare(reference: Image.Image, capture: Image.Image, threshold: int = 8,
            max_changed_percent: float | None = None) -> dict:
    if reference.size != capture.size:
        raise ValueError(
            f"Size mismatch reference={reference.size}, capture={capture.size}. "
            "Capture a matching viewport; never stretch images for a 1:1 claim."
        )
    if threshold not in range(256):
        raise ValueError("threshold must be an integer from 0 through 255")
    if max_changed_percent is not None and not 0 <= max_changed_percent <= 100:
        raise ValueError("max_changed_percent must be between 0 and 100")
    reference = reference.convert("RGB")
    capture = capture.convert("RGB")
    width, height = reference.size
    regions = {}
    for name, bounds in REGIONS.items():
        x0, y0, x1, y1 = bounds
        box = (int(x0 * width), int(y0 * height),
               min(width, int(x1 * width)), min(height, int(y1 * height)))
        regions[name] = {"box": box,
                         **metrics(reference.crop(box), capture.crop(box), threshold)}
    overall = metrics(reference, capture, threshold)
    result = {
        "dimensions": {"width": width, "height": height},
        "threshold": threshold,
        "same_state_and_animation_phase": "must be confirmed externally",
        "overall": overall,
        "regions": regions,
    }
    if max_changed_percent is not None:
        limit = max_changed_percent / 100.0
        result["tolerance"] = {
            "max_changed_percent": max_changed_percent,
            "overall_within_limit": overall["changed_fraction"] <= limit,
            "region_within_limit": {
                name: value["changed_fraction"] <= limit
                for name, value in regions.items()
            },
            "visual_equivalence_proven": False,
            "note": "Numeric tolerance only; verify matching game state and capture.",
        }
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--reference", required=True, type=Path)
    parser.add_argument("--capture", required=True, type=Path)
    parser.add_argument("--report", type=Path)
    parser.add_argument("--diff", type=Path, help="Optional absolute diff image (file only)")
    parser.add_argument("--threshold", type=int, default=8)
    parser.add_argument("--max-changed-percent", type=float,
                        help="Optional per-region pixel-difference acceptance threshold")
    args = parser.parse_args()
    with Image.open(args.reference) as r, Image.open(args.capture) as c:
        result = compare(r, c, args.threshold, args.max_changed_percent)
        if args.diff:
            args.diff.parent.mkdir(parents=True, exist_ok=True)
            ImageChops.difference(r.convert("RGB"), c.convert("RGB")).save(args.diff)
    rendered = json.dumps(result, ensure_ascii=False, indent=2)
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(rendered + "\n", encoding="utf-8")
    print(rendered)
    if "tolerance" in result:
        gates = result["tolerance"]
        if not gates["overall_within_limit"] or not all(
                gates["region_within_limit"].values()):
            raise SystemExit(1)


if __name__ == "__main__":
    main()
