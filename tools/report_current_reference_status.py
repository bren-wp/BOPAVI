#!/usr/bin/env python3
"""Fail-closed owner-reference inventory and native capture RGB acceptance report.

This does not synthesize, resize or alter any owner's reference or Android capture.
An unprovided reference, unproven capture, duplicated capture, bad hash or a
single different RGB pixel is NEVER an exact-match acceptance.
"""
import argparse
import hashlib
import io
import json
import os
from pathlib import Path, PurePosixPath

from PIL import Image
from compare_current_references import pixel_metrics
from install_current_references import BASE, read_index, verify_image

ROOT = Path(__file__).resolve().parents[1]


def safe_capture_path(root, name):
    if not isinstance(name, str) or not name or "\\" in name:
        return None
    rel = PurePosixPath(name)
    if rel.is_absolute() or any(part in ("..", ".") for part in rel.parts):
        return None
    if rel.suffix.lower() != ".png":
        return None
    return root.joinpath(*rel.parts)


def region_metrics(reference, actual):
    """Non-masked supplementary RGB metrics on actual-size horizontal bands.

    Full-frame comparison is always performed first. Bands are never
    resized/filtered, and never replace or reduce full-frame acceptance.
    """
    width, height = reference.size
    bounds = (
        ("top_third", 0, height // 3),
        ("middle_third", height // 3, 2 * height // 3),
        ("bottom_third", 2 * height // 3, height),
    )
    return {
        name: pixel_metrics(reference.crop((0, top, width, bottom)),
                            actual.crop((0, top, width, bottom)))
        for name, top, bottom in bounds
    }


def audit_records(rows, reference_dir, captures_dir, capture_map):
    mapped = {}
    for row in rows:
        rel = row["relative_path"]
        folder, filename = rel.split("/", 1)
        candidate = capture_map.get(folder, {})
        name = candidate.get(filename) if isinstance(candidate, dict) else None
        if name is not None:
            mapped.setdefault(name, []).append(rel)
    output = []
    for row in rows:
        rel = row["relative_path"]
        folder, filename = rel.split("/", 1)
        candidate = capture_map.get(folder, {})
        mapping = candidate.get(filename) if isinstance(candidate, dict) else None
        result = {"reference": rel, "capture": mapping, "status": "unverified"}
        ref_file = reference_dir / rel
        if not ref_file.is_file():
            result["reason"] = "Original owner WebP missing from repository"
        else:
            try:
                data = ref_file.read_bytes()
                verify_image(data, row)
                result["reference_verified"] = True
                capture = safe_capture_path(captures_dir, mapping)
                if capture is None:
                    result["reason"] = "No valid explicitly mapped native PNG capture"
                elif len(mapped.get(mapping, [])) > 1:
                    result["reason"] = "Same PNG mapped to multiple distinct reference states"
                elif not capture.is_file():
                    result["reason"] = "Mapped native PNG is missing"
                else:
                    with Image.open(io.BytesIO(data)) as original, Image.open(capture) as actual:
                        if actual.format != "PNG":
                            raise ValueError("Capture is not a genuine PNG file")
                        metrics = pixel_metrics(original, actual)
                        regions = region_metrics(original, actual)
                    result.update(metrics)
                    result["regions"] = regions
                    result["capture_sha256"] = hashlib.sha256(capture.read_bytes()).hexdigest()
                    result["status"] = "identical" if metrics["pixel_identical"] else "different"
            except (AssertionError, OSError, ValueError) as error:
                result["reason"] = f"Invalid reference or capture: {error}"
        output.append(result)
    expected = len(rows)
    summary = {
        "expected": expected,
        "reference_verified": sum(bool(r.get("reference_verified")) for r in output),
        "identical": sum(r["status"] == "identical" for r in output),
        "different": sum(r["status"] == "different" for r in output),
        "unverified": sum(r["status"] == "unverified" for r in output),
    }
    summary["all_exact"] = summary["expected"] == 50 and summary["identical"] == 50
    summary["results"] = output
    return summary



# Only distinct design states require one owner source. The immutable
# 50-record reference archive retains its original per-resolution checks.
PREFERRED_VIEWPORTS = (
    "Android_FHD_1080x2280",
    "Android_20_9_1080x2400",
    "Android_FHD_Plus_1080x2340",
    "Android_16_9_1080x1920",
    "Android_QHD_1440x3040",
)


def rollup_design_states(report, screen_names):
    """One hash-verified reference per unique state; retain full RGB metrics."""
    if not screen_names or len(set(screen_names)) != len(screen_names):
        raise ValueError("Design-state names must be nonempty and unique")
    items = []
    for screen in screen_names:
        matching = [
            x for x in report["results"]
            if x["reference"].split("/", 1)[-1] == screen
        ]
        verified = [x for x in matching if x.get("reference_verified")]
        compared = [x for x in verified
                    if x["status"] in ("identical", "different")]
        def rank(x):
            viewport = x["reference"].split("/", 1)[0]
            return (PREFERRED_VIEWPORTS.index(viewport)
                    if viewport in PREFERRED_VIEWPORTS
                    else len(PREFERRED_VIEWPORTS), x["reference"])
        candidates = sorted(compared or verified, key=rank)
        chosen = candidates[0] if candidates else None
        items.append({
            "screen": screen,
            "reference_available": bool(verified),
            "reference_variants": len(verified),
            "native_capture_available": bool(compared),
            "representative_reference": chosen["reference"] if chosen else None,
            "representative_capture": chosen.get("capture") if chosen else None,
            "representative_status": chosen["status"] if chosen else "unverified",
            "representative_mae_rgb_0_255":
                chosen.get("mae_rgb_0_255") if chosen else None,
            "reason": chosen.get("reason") if chosen else "No original owner reference",
        })
    report["screen_states"] = items
    report["screens_expected"] = len(items)
    report["screens_with_original"] = sum(x["reference_available"] for x in items)
    report["screens_with_native_capture"] = sum(
        x["native_capture_available"] for x in items)
    report["screens_exact_rgb"] = sum(
        x["representative_status"] == "identical" for x in items)
    report["screens_different_rgb"] = sum(
        x["representative_status"] == "different" for x in items)
    report["screen_coverage_passed"] = (
        report["screens_with_original"] == report["screens_expected"])
    return report


def triage_markdown(report):
    """Human-readable CI evidence; never changes all_exact or passes a gate."""
    results = report["results"]
    different = sorted(
        (item for item in results if item["status"] == "different"),
        key=lambda item: item.get("mae_rgb_0_255", 0), reverse=True,
    )
    unverified = [item for item in results if item["status"] == "unverified"]
    absent = [item for item in unverified
              if item.get("reason") == "Original owner WebP missing from repository"]
    missing_captures = [item for item in unverified if
                        item.get("reason") == "No valid explicitly mapped native PNG capture"]
    verdict = "PASS" if report["all_exact"] else "BLOCKED"
    out = [
        f"## BOPAVI owner RGB acceptance: {verdict}",
        "",
        "Native Android QA and source/build verification are independent of this "
        "strict visual acceptance. A missing reference, capture, or a single changed "
        "RGB pixel blocks the visual gate.",
        "",
        "| Original states | Verified references | Exact | Different | Unverified |",
        "|---:|---:|---:|---:|---:|",
        f'| {report["expected"]} | {report["reference_verified"]} | '
        f'{report["identical"]} | {report["different"]} | {report["unverified"]} |',
        "",
        f"### Missing owner reference WebPs ({len(absent)})",
        "",
    ]
    if absent:
        out.extend(f"- `{item['reference']}`" for item in absent)
    else:
        out.append("None.")
    out += ["", f"### Missing native state mappings ({len(missing_captures)})", ""]
    if missing_captures:
        out.extend(f"- `{item['reference']}`" for item in missing_captures)
    else:
        out.append("None.")
    additional = [
        item for item in unverified
        if item not in absent and item not in missing_captures
    ]
    if additional:
        out += ["", "### Other unverified evidence", ""]
        out.extend(f"- `{item['reference']}`: {item.get('reason', 'Unknown')}"
                   for item in additional)
    out += [
        "", "### Largest measured RGB errors (fix these visuals first)", "",
        "| Owner reference | Changed RGB pixels | RGB MAE (0–255) | Worst third by MAE |",
        "|---|---:|---:|---|",
    ]
    for item in different[:10]:
        regions = item.get("regions", {})
        worst = max(regions, key=lambda key: regions[key].get("mae_rgb_0_255", 0)) \
            if regions else "unknown"
        out.append(
            f"| `{item['reference']}` | "
            f"{item['different_pixels_percent']:.4f}% | "
            f"{item['mae_rgb_0_255']:.3f} | {worst} |"
        )
    if not different:
        out.append("| No comparable mismatches | — | — | — |")
    out += [
        "",
        "**Acceptance is not achieved until all 50 original, hash-verified owner "
        "references are compared with distinct, unscaled, genuine Android captures "
        "and all RGB pixel differences are zero.**",
        "",
        "Historical Home references predate the user-required visible PLAY button: "
        "updated, owner-approved Home targets are also required before the new "
        "functional Home can be called 1:1. Never replace owner references "
        "with screenshots generated by this application.",
        "",
    ]
    return "\n".join(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--reference-dir", type=Path, default=BASE / "images")
    parser.add_argument("--screenshots", type=Path, default=ROOT / "qa/screenshots")
    parser.add_argument("--capture-map", type=Path, default=ROOT / "qa/new-owner-capture-map.json")
    parser.add_argument("--json-out", type=Path, required=True)
    parser.add_argument("--require-50-exact", action="store_true")
    parser.add_argument("--require-screen-coverage", action="store_true")
    parser.add_argument("--markdown-out", type=Path)
    args = parser.parse_args()
    manifest, rows = read_index()
    # An absent mapping is an explicit unverified result, NEVER auto-pair
    # design mockups with a convenient but unrelated release screenshot.
    capture_map = json.loads(args.capture_map.read_text("utf-8")) if args.capture_map.is_file() else {}
    if not isinstance(capture_map, dict):
        raise SystemExit("Capture map must be a JSON object")
    report = audit_records(rows, args.reference_dir, args.screenshots, capture_map)
    rollup_design_states(report, manifest["screens"])
    args.json_out.parent.mkdir(parents=True, exist_ok=True)
    args.json_out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if args.markdown_out:
        args.markdown_out.parent.mkdir(parents=True, exist_ok=True)
        args.markdown_out.write_text(triage_markdown(report), encoding="utf-8")
    step_summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary:
        with open(step_summary, "a", encoding="utf-8") as summary_file:
            summary_file.write(triage_markdown(report))

    print(json.dumps({k: v for k, v in report.items() if k != "results"}, indent=2))
    for item in report["results"]:
        if item["status"] == "different" or item["status"] == "identical":
            print(f'{item["reference"]}: {item["different_pixels"]} RGB pixels '
                  f'({item["different_pixels_percent"]}%), '
                  f'MAE={item["mae_rgb_0_255"]}/255, '
                  f'regions=' + ', '.join(
                      f'{name}:{value["different_pixels_percent"]}%'
                      for name, value in item["regions"].items()))
        else:
            print(f'{item["reference"]}: UNVERIFIED — {item.get("reason", "unknown")}')
    if args.require_screen_coverage and not report["screen_coverage_passed"]:
        raise SystemExit("DESIGN-STATE COVERAGE FAIL: no valid original for every distinct screen")
    if args.require_50_exact and not report["all_exact"]:
        raise SystemExit("VISUAL ACCEPTANCE FAIL: no claim of 50/50 exact native RGB parity")


if __name__ == "__main__":
    main()
