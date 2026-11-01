#!/usr/bin/env python3
"""Strict offline screenshot comparison: owner and emulator PNGs, 941x1672."""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageChops, ImageStat

SIZE=(941,1672)

def compare(reference:Path, actual:Path):
    with Image.open(reference) as ri, Image.open(actual) as ai:
        if ri.size!=SIZE or ai.size!=SIZE:
            raise ValueError(f"Expected 941x1672; got {ri.size} and {ai.size}")
        if ri.format!="PNG" or ai.format!="PNG":
            raise ValueError("Both sources must be original PNG files")
        diff=ImageChops.difference(ri.convert("RGB"),ai.convert("RGB"))
    mean=sum(ImageStat.Stat(diff).mean)/3
    regions=[]
    x_edges=(0, SIZE[0]//3, 2*SIZE[0]//3, SIZE[0])
    y_edges=(0, SIZE[1]//3, 2*SIZE[1]//3, SIZE[1])
    for row in range(3):
        for col in range(3):
            box=(x_edges[col],y_edges[row],x_edges[col+1],y_edges[row+1])
            regions.append({"row":row,"column":col,
                "mae_rgb":round(sum(ImageStat.Stat(diff.crop(box)).mean)/3,4)})
    return {"size":list(SIZE),"mean_rgb_abs_difference":round(mean,4),
            "exact_rgb_match":diff.getbbox() is None,"regions":regions}

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--reference",required=True,type=Path)
    parser.add_argument("--actual",required=True,type=Path)
    parser.add_argument("--max-mae",type=float)
    args=parser.parse_args()
    if args.max_mae is not None and not 0<=args.max_mae<=255:
        parser.error("max-mae must be between 0 and 255")
    try:
        result=compare(args.reference,args.actual)
    except (ValueError,OSError) as error:
        parser.error(str(error))
    value=result["mean_rgb_abs_difference"]
    result["threshold_pass"]=(None if args.max_mae is None else
        (value<=args.max_mae and
         (args.max_mae!=0 or result["exact_rgb_match"])))
    print(json.dumps(result,indent=2,ensure_ascii=False))
    return 1 if result["threshold_pass"] is False else 0

if __name__=="__main__":
    raise SystemExit(main())
