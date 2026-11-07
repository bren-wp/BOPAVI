#!/usr/bin/env python3
"""Seven *native* Android viewport screenshots vs the owner's two 941x1672 refs.

Reference geometry must be mapped to each device without stretching.
Only the OWNER reference is projected; actual adb screenshots are always
decoded at their original native size and NEVER modified. Real stored wallet
values, transient animation frame and physical cutouts remain real and may
increase measured RGB deviation. Such differences are not hidden or exempted.

Full-screen metric: reference center-COVER projection (a diagnostic ideal for
background + foreground combined; aspect-ratio differences are explicitly
reported). Every UI/art ROI: reference FIT-CENTER geometry, matching the
native LevelMapStage and FlightLoadingView placements. This is a transparent
proxy until device-specific reference illustrations are supplied. A green
source test does NOT establish <=10% on any real device.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageChops, ImageOps, ImageStat

from qa_two_owner_reference_screens import (
    REGIONS, SIZE, TARGET_MAX_RGB_MAE, deviation_percent, passes_target,
)

# Must match the real adb size/density matrix in qa_android_emulator.sh.
VIEWPORTS = {
    "small": (480, 800, 160),
    "short": (600, 960, 240),
    "compact": (720, 1280, 320),
    "reference": (941, 1672, 320),
    "standard": (1080, 1920, 420),
    "tall": (1080, 2400, 420),
    "large": (1440, 2960, 560),
}


def open_native(path: Path, expected_size: tuple[int, int]) -> Image.Image:
    with Image.open(path) as image:
        if image.format != "PNG" or image.size != expected_size:
            raise ValueError(f"{path}: expected unmodified PNG {expected_size}, "
                             f"got {image.format} {image.size}")
        return image.convert("RGB")


def mean_rgb_delta(first: Image.Image, second: Image.Image) -> float:
    if first.size != second.size:
        raise ValueError("Pixel comparison refuses differently sized crops")
    return sum(ImageStat.Stat(ImageChops.difference(first, second)).mean) / 3.0


def fit_center_box(
    box: tuple[int,int,int,int], width: int, height: int
) -> tuple[int,int,int,int]:
    scale = min(width / SIZE[0], height / SIZE[1])
    left = (width-SIZE[0]*scale)/2.0
    top = (height-SIZE[1]*scale)/2.0
    x1,y1,x2,y2 = box
    result = (round(left+x1*scale), round(top+y1*scale),
              round(left+x2*scale), round(top+y2*scale))
    a,b,c,d = result
    if not (0 <= a < c <= width and 0 <= b < d <= height):
        raise ValueError(f"Reference artwork ROI {box} clipped on {width}x{height}")
    return result


def score_region(
    owner: Image.Image, native: Image.Image,
    roi: tuple[int,int,int,int]
) -> dict:
    box = fit_center_box(roi, *native.size)
    actual = native.crop(box)
    reference = owner.crop(roi).resize(actual.size, Image.Resampling.LANCZOS)
    delta = mean_rgb_delta(reference, actual)
    return {
        "reference_box": list(roi),
        "native_box": list(box),
        "rgb_mae": round(delta, 3),
        "deviation_percent": round(deviation_percent(delta), 3),
        "meets_target": passes_target(delta),
    }


def score_viewport(
    owner_path: Path, native_path: Path, name: str,
    label: str, size: tuple[int,int]
) -> dict:
    owner = open_native(owner_path, SIZE)
    native = open_native(native_path, size)
    # Only the reference is projected into a different Android aspect ratio.
    expected_full = ImageOps.fit(owner, size, method=Image.Resampling.LANCZOS)
    whole = mean_rgb_delta(expected_full, native)
    regions = {key: score_region(owner,native,roi)
               for key,roi in REGIONS[name].items()}
    passed = passes_target(whole) and all(
        region["meets_target"] for region in regions.values())
    return {
        "screen": name, "device": label, "native_size": list(size),
        "original_reference_size": list(SIZE),
        "real_capture_sha256": hashlib.sha256(native_path.read_bytes()).hexdigest(),
        "reference_sha256": hashlib.sha256(owner_path.read_bytes()).hexdigest(),
        "projection": {
            "full_screen": "reference center-cover; actual PNG unchanged",
            "named_regions": "reference fit-center; actual PNG unchanged",
            "center_crop_or_letterbox_may_differ": size != SIZE,
        },
        "whole_screen_rgb_mae": round(whole, 3),
        "whole_screen_deviation_percent": round(deviation_percent(whole), 3),
        "whole_screen_target_met": passes_target(whole),
        "regions": regions, "meets_target": passed,
    }


def benchmark(home: Path, splash: Path, screenshots: Path) -> dict:
    devices = []
    for label,(w,h,dpi) in VIEWPORTS.items():
        screens = [
            score_viewport(home,screenshots/f"android-home-{label}.png",
                           "Home",label,(w,h)),
            score_viewport(splash,screenshots/f"android-loading-{label}.png",
                           "Splash",label,(w,h)),
        ]
        devices.append({
            "device": label, "native_size": [w,h], "density_dpi": dpi,
            "screens": screens,
            "meets_target": all(s["meets_target"] for s in screens),
        })
    return {
        "goal": "Home AND Splash, all seven Android sizes, every UI/art ROI <=10%",
        "maximum_deviation_percent": 10.0,
        "maximum_rgb_mae": TARGET_MAX_RGB_MAE,
        "screenshots_are_untouched": True,
        "original_art_size": list(SIZE),
        "different_aspect_ratio_note": (
            "These are reference PROJECTIONS, not separately approved designs. "
            "Do not infer exact 1:1 on a different aspect ratio from MAE alone."
        ),
        "devices": devices,
        "all_devices_meet_target": all(d["meets_target"] for d in devices),
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--home-reference", type=Path, required=True)
    parser.add_argument("--splash-reference", type=Path, required=True)
    parser.add_argument("--screenshots-dir", type=Path, required=True)
    parser.add_argument("--json-report", type=Path)
    parser.add_argument("--enforce-target", action="store_true")
    args = parser.parse_args()
    report = benchmark(args.home_reference,args.splash_reference,
                       args.screenshots_dir)
    result=json.dumps(report,ensure_ascii=False,indent=2)+"\n"
    if args.json_report:
        args.json_report.parent.mkdir(parents=True,exist_ok=True)
        args.json_report.write_text(result,encoding="utf-8")
    print(result,end="")
    if args.enforce_target and not report["all_devices_meet_target"]:
        raise SystemExit(
            "FAIL: at least one Home/Splash native screen or named ROI exceeds "
            "10% RGB MAE on a small, normal or large Android profile")


if __name__ == "__main__":
    main()
