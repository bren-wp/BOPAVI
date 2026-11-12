#!/usr/bin/env python3
"""Strict same-resolution RGB comparison of real Android PNG captures and owner WebPs.

A missing capture or any image resizing is NEVER an exact-match PASS.
No screenshot is generated, cropped, blended or substituted with a mockup.
"""
import argparse
import io
import json
from pathlib import Path

from PIL import Image, ImageChops, ImageStat
from install_current_references import read_index, verify_archive, verify_installed

ROOT=Path(__file__).resolve().parents[1]
REFS=ROOT/"art/reference-current/images"

def pixel_metrics(reference, actual):
    if reference.size!=actual.size:
        raise ValueError(f"Size mismatch: ref={reference.size} capture={actual.size}")
    # RGB semantics are explicit: alpha cannot hide a mismatched pixel.
    ref=reference.convert("RGB")
    got=actual.convert("RGB")
    diff=ImageChops.difference(ref,got)
    channels=diff.split()
    changed=ImageChops.lighter(channels[0],ImageChops.lighter(channels[1],channels[2]))
    hist=changed.histogram()
    count=ref.width*ref.height
    nonmatching=sum(hist[1:])
    mae=sum(ImageStat.Stat(diff).mean)/3
    return {
        "size":[ref.width,ref.height],
        "pixel_identical":nonmatching==0,
        "different_pixels":nonmatching,
        "different_pixels_percent":round(100.0*nonmatching/count,6),
        "mae_rgb_0_255":round(mae,6),
        "max_channel_difference":max(i for i,amount in enumerate(hist) if amount)
    }

def compare_records(records, references, screenshots, capture_map, viewport=None):
    """Return all available exact results AND explicit unverified failures."""
    results=[]
    for record in records:
        rel=record["relative_path"]
        folder,filename=rel.split("/",1)
        if viewport and folder!=viewport:continue
        mapped=capture_map.get(folder,{}).get(filename)
        result={"reference":rel,"capture":mapped,"status":"unverified"}
        if not mapped:
            result["reason"]="No native Android capture mapped"
        else:
            target=screenshots/mapped
            if not target.is_file():
                result["reason"]=f"Missing Android capture: {target}"
            elif target.suffix.lower()!=".png":
                result["reason"]="Native capture must be PNG, never the owner WebP"
            else:
                try:
                    with Image.open(io.BytesIO(references[rel])) as design, Image.open(target) as live:
                        if live.format!="PNG":
                            raise ValueError("Capture was not PNG")
                        stats=pixel_metrics(design,live)
                    result.update(stats)
                    result["status"]="identical" if stats["pixel_identical"] else "different"
                except (OSError,ValueError) as exc:
                    result["reason"]=str(exc)
        results.append(result)
    return results

def main():
    p=argparse.ArgumentParser()
    source=p.add_mutually_exclusive_group(required=True)
    source.add_argument("--archive",type=Path,help="Original exact owner ZIP")
    source.add_argument("--reference-dir",type=Path,help="Verified installed 50 WebPs")
    p.add_argument("--screenshots",type=Path,required=True)
    p.add_argument("--capture-map",type=Path,required=True,
                   help="JSON viewport -> screen filename -> real Android PNG path")
    p.add_argument("--viewport",help="Restrict to one of the five declared viewports")
    p.add_argument("--json-out",type=Path)
    p.add_argument("--report-only",action="store_true",
                   help="Print failures without exiting nonzero; does not claim PASS")
    a=p.parse_args()
    manifest,records=read_index()
    if a.viewport and a.viewport not in manifest["viewports"]:
        p.error("Unknown viewport")
    if a.archive:
        references=verify_archive(a.archive)
    else:
        verify_installed(a.reference_dir)
        references={r["relative_path"]:(a.reference_dir/r["relative_path"]).read_bytes()
                    for r in records}
    capture_map=json.loads(a.capture_map.read_text("utf-8"))
    results=compare_records(records,references,a.screenshots,capture_map,a.viewport)
    expected=10 if a.viewport else 50
    assert len(results)==expected
    ident=sum(row["status"]=="identical" for row in results)
    missing=sum(row["status"]=="unverified" for row in results)
    different=sum(row["status"]=="different" for row in results)
    summary={"expected":expected,"identical":ident,"different":different,
             "unverified":missing,"all_exact":ident==expected,"results":results}
    print(json.dumps(summary,ensure_ascii=False,indent=2))
    if a.json_out:
        a.json_out.parent.mkdir(parents=True,exist_ok=True)
        a.json_out.write_text(json.dumps(summary,ensure_ascii=False,indent=2)+"\n",
                              encoding="utf-8")
    if not summary["all_exact"] and not a.report_only:
        raise SystemExit("FAIL: image QA is not 100% RGB-identical for every screen")

if __name__=="__main__":
    main()
