#!/usr/bin/env python3
"""Tiny synthetic TEST FIXTURES for reporter logic; not BOPAVI owner screenshots."""
import hashlib
import sys
import tempfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from report_current_reference_status import audit_records, safe_capture_path, triage_markdown

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
    markdown_missing = triage_markdown(missing)
    assert "owner RGB acceptance: BLOCKED" in markdown_missing
    assert "Missing owner reference WebPs (1)" in markdown_missing
    assert "test/owner.webp" in markdown_missing

    unmapped = audit_records([row], refs, captures, {})
    assert unmapped["reference_verified"] == 1
    assert unmapped["results"][0]["reason"].startswith("No valid explicitly mapped")
    assert not unmapped["all_exact"]

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
    assert not identical["all_exact"], "Only all 50 can be certified"

    with Image.open(shot) as image:
        changed = image.copy()
    changed.putpixel((2, 3), (255, 0, 0))
    changed.save(shot)
    mismatch = audit_records([row], refs, captures, mapped)
    assert mismatch["different"] == 1 and mismatch["identical"] == 0
    assert mismatch["results"][0]["different_pixels"] == 1
    markdown_diff = triage_markdown(mismatch)
    assert "owner RGB acceptance: BLOCKED" in markdown_diff
    assert "Largest measured RGB errors" in markdown_diff
    assert "0.4167%" in markdown_diff
    assert "50 original" in markdown_diff
    assert sum(row["different_pixels"] for row in
               mismatch["results"][0]["regions"].values()) == 1
    assert mismatch["results"][0]["regions"]["top_third"]["different_pixels"] == 1

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
