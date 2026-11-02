#!/usr/bin/env python3
"""Exact source-pixel ROI comparisons for the owner's ONLY three priority screens.

A passing compile or low global MAE does not establish 1:1 appearance. This
tool requires both original owner PNG and real 941x1672 emulator PNG; it
refuses resizing, recompression and fabricated expected numeric balances.
"""
from __future__ import annotations
import argparse
import json
from pathlib import Path
from PIL import Image, ImageChops, ImageStat

SIZE = (941, 1672)
REGIONS = {
    "home": {
        "currency_counters": (447, 12, 931, 101),
        "winged_logo": (127, 92, 824, 386),
        "flying_bopi": (118, 386, 731, 868),
        "gold_play_3d": (129, 875, 810, 1091),
        "purple_skins_3d": (113, 1085, 472, 1253),
        "green_shop_3d": (467, 1085, 811, 1253),
        "blue_settings_3d": (242, 1254, 699, 1388),
        "footer_medallions": (186, 1439, 770, 1612),
    },
    "gameplay": {
        "avatar_and_hearts_hud": (18, 13, 368, 129),
        "upper_spiked_mace": (637, 2, 941, 389),
        "flying_bopi": (114, 552, 472, 933),
        "arched_jewel": (641, 727, 921, 1023),
        "gold_coins_and_purple_jewel": (443, 967, 941, 1208),
        "tap_to_fly_hint": (277, 1230, 676, 1371),
        "lower_spiked_mace": (704, 1225, 941, 1466),
        "lower_ground_spikes": (189, 1440, 827, 1672),
    },
    "game-over": {
        "winged_crown_wordmark": (147, 30, 794, 464),
        "sad_bopi": (211, 436, 724, 819),
        "floating_stone_platform": (161, 718, 754, 951),
        "three_results_rows": (125, 928, 819, 1239),
        "gold_retry_button": (173, 1224, 773, 1394),
        "blue_home_button": (237, 1397, 712, 1544),
    },
}


def compare(screen: str, reference: Path, captured: Path) -> dict:
    images = []
    for file in (reference, captured):
        with Image.open(file) as im:
            if im.format != "PNG" or im.size != SIZE:
                raise ValueError(f"{file}: expected original {SIZE} PNG; got {im.format} {im.size}")
            images.append(im.convert("RGB"))
    ref, actual = images
    result = {}
    for name, region in REGIONS[screen].items():
        delta = ImageChops.difference(ref.crop(region), actual.crop(region))
        result[name] = {
            "box": list(region),
            "mae_rgb": round(sum(ImageStat.Stat(delta).mean) / 3, 4),
            "exact_rgb_match": delta.getbbox() is None,
        }
    whole = ImageChops.difference(ref, actual)
    return {
        "screen": screen,
        "size": list(SIZE),
        "whole_screen_mae_rgb": round(sum(ImageStat.Stat(whole).mean) / 3, 4),
        "exact_1_to_1_rgb": whole.getbbox() is None,
        "regions": result,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--screen", choices=sorted(REGIONS), required=True)
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--require-exact", action="store_true",
        help="exit with failure unless ALL 941x1672 RGB pixels are identical")
    args = parser.parse_args()
    try:
        result = compare(args.screen, args.reference, args.actual)
    except (OSError, ValueError) as exc:
        parser.error(str(exc))
    print(json.dumps(result, indent=2))
    return 1 if args.require_exact and not result["exact_1_to_1_rgb"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
