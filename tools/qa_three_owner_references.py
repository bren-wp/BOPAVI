#!/usr/bin/env python3
"""Exact source-pixel ROI comparisons for the owner's four priority screens.

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
    "loading": {
        "winged_logo": (119, 67, 829, 394),
        "flying_bopi": (125, 429, 776, 1054),
        "hollow_gold_rings": (0, 883, 797, 1244),
        "winged_loading_track": (111, 1299, 828, 1442),
        "loading_caption": (326, 1433, 632, 1511),
    },
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
    "shop": {
        "wallet_and_back": (16, 8, 925, 122),
        "wooden_shop_title": (177, 116, 762, 329),
        "storefront_and_mascot": (21, 296, 919, 583),
        "featured_offer": (35, 560, 906, 874),
        "upper_product_row": (47, 877, 899, 1200),
        "lower_product_row": (47, 1202, 899, 1532),
        "lower_wood_and_foliage": (0, 1532, 941, 1672),
    },
    "skins": {
        "navigation_and_wallets": (12, 10, 933, 133),
        "hanging_wood_title": (204, 138, 731, 333),
        "central_winged_hero": (150, 332, 798, 814),
        "carousel_arrows": (23, 609, 918, 731),
        "stone_platform": (146, 790, 796, 972),
        "four_pilot_cards": (18, 970, 916, 1284),
        "equipped_info_and_button": (26, 1290, 916, 1505),
        "foreground_foliage": (0, 1505, 941, 1672),
    },
    "settings": {
        "logo_mascot_and_back": (13, 10, 836, 490),
        "gold_settings_title": (175, 482, 768, 609),
        "music_and_effects_sliders": (139, 612, 785, 795),
        "toggles_and_selectors": (139, 790, 785, 1249),
        "legal_rows": (139, 1251, 785, 1422),
        "wooden_panel_and_frame": (91, 554, 848, 1580),
        "bottom_restore_region": (190, 1418, 751, 1560),
        "bottom_foliage_and_version": (0, 1550, 941, 1672),
    },
}


def changed_rgb_pixels(delta: Image.Image) -> int:
    # One RGB channel changed by just 1 counts as a changed pixel.
    red, green, blue = delta.split()
    peak = ImageChops.lighter(ImageChops.lighter(red, green), blue)
    return peak.width * peak.height - peak.histogram()[0]


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
        different = changed_rgb_pixels(delta)
        total = delta.width * delta.height
        result[name] = {
            "box": list(region),
            "different_pixels": different,
            "pixel_deviation_percent": round(100 * different / total, 4),
            "mae_rgb": round(sum(ImageStat.Stat(delta).mean) / 3, 4),
            "exact_rgb_match": different == 0,
        }
    whole = ImageChops.difference(ref, actual)
    changed = changed_rgb_pixels(whole)
    total = SIZE[0] * SIZE[1]
    return {
        "screen": screen,
        "size": list(SIZE),
        "total_pixels": total,
        "different_pixels": changed,
        "pixel_deviation_percent": round(100 * changed / total, 4),
        "whole_screen_mae_rgb": round(sum(ImageStat.Stat(whole).mean) / 3, 4),
        "exact_1_to_1_rgb": changed == 0,
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
