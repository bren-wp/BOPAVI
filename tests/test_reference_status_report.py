#!/usr/bin/env python3
"""Tiny synthetic TEST FIXTURES for reporter logic; not BOPAVI owner screenshots."""
import hashlib
import sys
import tempfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from report_current_reference_status import audit_records, safe_capture_path, triage_markdown, rollup_design_states

with tempfile.TemporaryDirectory() as folder:
    base = Path(folder)
    refs = base / "references"
    captures = base / "captures"
    (refs / "test").mkdir(parents=True)
    captures.mkdir()
    design = Image.new("RGB", (12, 20), (10, 20, 30))
    webp = refs / "test" / "owner.webp"
    design.save(webp, format="WEBP", lossless=True)
    payload = webp.read_bytes()
    row = {
        "relative_path": "test/owner.webp",
        "width": "12",
        "height": "20",
        "bytes": str(len(payload)),
        "sha256": hashlib.sha256(payload).hexdigest(),
    }
    missing = audit_records([row], refs / "missing", captures, {})
    assert missing["reference_verified"] == 0 and missing["unverified"] == 1
    assert not missing["all_exact"]
    rollup_design_states(missing, ["owner.webp"])
    assert not missing["screen_coverage_passed"]
    assert missing["screens_expected"] == 1
    markdown_missing = triage_markdown(missing)
    assert "unique design-state coverage: INCOMPLETE" in markdown_missing
    assert "Missing optional reference variants: 1." in markdown_missing
    assert "test/owner.webp" in markdown_missing

    unmapped = audit_records([row], refs, captures, {})
    assert unmapped["reference_verified"] == 1
    assert unmapped["results"][0]["reason"].startswith("No valid explicitly mapped")
    assert not unmapped["all_exact"]
    rollup_design_states(unmapped, ["owner.webp"])
    assert unmapped["screen_coverage_passed"]
    assert unmapped["screens_with_original"] == 1
    assert unmapped["screens_with_native_capture"] == 0
    assert unmapped["screens_exact_rgb"] == 0

    shot = captures / "native.png"
    with Image.open(webp) as expected:
        expected.save(shot, format="PNG")
    mapped = {"test": {"owner.webp": "native.png"}}
    identical = audit_records([row], refs, captures, mapped)
    assert identical["identical"] == 1 and identical["different"] == 0
    assert identical["results"][0]["different_pixels"] == 0
    assert identical["results"][0]["mae_rgb_0_255"] == 0
    assert set(identical["results"][0]["regions"]) == {
        "top_third", "middle_third", "bottom_third"
    }
    assert all(row["different_pixels"] == 0
               for row in identical["results"][0]["regions"].values())
    exact_cells = identical["results"][0]["grid_regions"]
    assert set(exact_cells) == {
        "top_left", "top_center", "top_right",
        "middle_left", "middle_center", "middle_right",
        "bottom_left", "bottom_center", "bottom_right",
    }
    assert all(cell["different_pixels"] == 0 for cell in exact_cells.values())
    assert not identical["all_exact"], "Only all 50 archived variants can be certified"
    rollup_design_states(identical, ["owner.webp"])
    assert identical["screen_coverage_passed"]
    assert identical["screens_exact_rgb"] == 1
    assert identical["screens_with_native_capture"] == 1

    with Image.open(shot) as image:
        changed = image.copy()
    changed.putpixel((2, 3), (255, 0, 0))
    changed.save(shot)
    mismatch = audit_records([row], refs, captures, mapped)
    assert mismatch["different"] == 1 and mismatch["identical"] == 0
    assert mismatch["results"][0]["different_pixels"] == 1
    rollup_design_states(mismatch, ["owner.webp"])
    assert mismatch["screen_coverage_passed"]
    assert mismatch["screens_different_rgb"] == 1
    assert mismatch["screens_exact_rgb"] == 0
    markdown_diff = triage_markdown(mismatch)
    assert "unique design-state coverage: PASS" in markdown_diff
    assert "Largest measured RGB errors" in markdown_diff
    assert "0.4167%" in markdown_diff
    assert "Coverage PASS is NOT pixel-perfect" in markdown_diff
    assert sum(row["different_pixels"] for row in
               mismatch["results"][0]["regions"].values()) == 1
    assert mismatch["results"][0]["regions"]["top_third"]["different_pixels"] == 1
    # Original synthetic 12x20 frame, changed pixel (2,3) is top-left only.
    cells = mismatch["results"][0]["grid_regions"]
    assert sum(cell["different_pixels"] for cell in cells.values()) == 1
    assert cells["top_left"]["different_pixels"] == 1
    assert "top_left" in markdown_diff

    assert safe_capture_path(captures, "../../secret.png") is None
    assert safe_capture_path(captures, "/tmp/image.png") is None
    assert safe_capture_path(captures, "fake.webp") is None
    assert safe_capture_path(captures, None) is None

    other = dict(row, relative_path="test/second.webp")
    (refs / "test" / "second.webp").write_bytes(payload)
    duplicates = audit_records([row, other], refs, captures, {
        "test": {"owner.webp": "native.png", "second.webp": "native.png"}
    })
    assert duplicates["identical"] == 0 and duplicates["unverified"] == 2
    assert all("multiple distinct" in r["reason"] for r in duplicates["results"])

print("PASS: fail-closed 50-reference RGB reporter, actionable Markdown and strict native captures")
