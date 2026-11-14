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
                    result.update(metrics)
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


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--reference-dir", type=Path, default=BASE / "images")
    parser.add_argument("--screenshots", type=Path, default=ROOT / "qa/screenshots")
    parser.add_argument("--capture-map", type=Path, default=ROOT / "qa/new-owner-capture-map.json")
    parser.add_argument("--json-out", type=Path, required=True)
    parser.add_argument("--require-50-exact", action="store_true")
    args = parser.parse_args()
    _, rows = read_index()
    # An absent mapping is an explicit unverified result, NEVER auto-pair
    # design mockups with a convenient but unrelated release screenshot.
    capture_map = json.loads(args.capture_map.read_text("utf-8")) if args.capture_map.is_file() else {}
    if not isinstance(capture_map, dict):
        raise SystemExit("Capture map must be a JSON object")
    report = audit_records(rows, args.reference_dir, args.screenshots, capture_map)
    args.json_out.parent.mkdir(parents=True, exist_ok=True)
    args.json_out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: v for k, v in report.items() if k != "results"}, indent=2))
    if args.require_50_exact and not report["all_exact"]:
        raise SystemExit("VISUAL ACCEPTANCE FAIL: no claim of 50/50 exact native RGB parity")


if __name__ == "__main__":
    main()
