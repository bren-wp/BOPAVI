#!/usr/bin/env python3
"""Rank real 941x1672 BOPAVI reference differences, with button/HUD priority.

No screenshots are generated: actual captures must come from a real emulator
and original references must be user-provided unmodified PNGs.
"""
from __future__ import annotations
import argparse
import json
from pathlib import Path
from qa_three_owner_references import compare

SCREENS=("loading","home","gameplay","game-over")
PRIORITY_WORDS=("play","skins","shop","settings","button","retry",
                "track","hud","currency","medallions","hint")

def rank(results:list[dict])->list[dict]:
    all_regions=[]
    for item in results:
        for name,region in item["regions"].items():
            mae=region["mae_rgb"]
            priority=any(word in name.lower() for word in PRIORITY_WORDS)
            all_regions.append({
                "screen":item["screen"],
                "region":name,
                "mae_rgb":mae,
                "button_or_hud_priority":priority,
                "priority_score":round(mae*(2 if priority else 1),4),
                "exact_rgb_match":region["exact_rgb_match"],
            })
    return sorted(all_regions,key=lambda row:(
        row["priority_score"],row["mae_rgb"]),reverse=True)

def main()->int:
    p=argparse.ArgumentParser(description=__doc__)
    for screen in SCREENS:
        p.add_argument("--"+screen+"-reference",required=True,type=Path)
        p.add_argument("--"+screen+"-actual",required=True,type=Path)
    p.add_argument("--require-exact",action="store_true")
    args=p.parse_args()
    results=[]
    for screen in SCREENS:
        ref=getattr(args,screen.replace("-","_")+"_reference")
        actual=getattr(args,screen.replace("-","_")+"_actual")
        try:
            results.append(compare(screen,ref,actual))
        except (OSError,ValueError) as exc:
            p.error(str(exc))
    output={
        "screen_results":[{"screen":r["screen"],
                           "mae_rgb":r["whole_screen_mae_rgb"],
                           "exact":r["exact_1_to_1_rgb"]} for r in results],
        "worst_first":rank(results),
        "all_four_exact":all(r["exact_1_to_1_rgb"] for r in results),
        "note":"Priority score doubles button/HUD MAE; visual acceptance still requires inspection."
    }
    print(json.dumps(output,indent=2))
    return 1 if args.require_exact and not output["all_four_exact"] else 0

if __name__=="__main__":
    raise SystemExit(main())
