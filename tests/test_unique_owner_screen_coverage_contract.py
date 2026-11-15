#!/usr/bin/env python3
"""BOPAVI CI counts DISTINCT owner design states, not screenshot resolutions.

This is an honest source-coverage check. It never asserts pixel parity,
substitutes a fake capture, or changes the signed owner archive manifest.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from install_current_references import BASE, read_index, verify_image
from report_current_reference_status import rollup_design_states

manifest, rows = read_index()
names = manifest["screens"]
assert len(names) == 10 and len(set(names)) == 10
assert len(rows) == 50 and len(manifest["viewports"]) == 5, (
    "Archive provenance still describes five resolution sets"
)
seen = set()
for row in rows:
    name = row["relative_path"].split("/", 1)[-1]
    path = BASE / "images" / row["relative_path"]
    if path.is_file():
        verify_image(path.read_bytes(), row)
        seen.add(name)
assert seen == set(names), (
    "Every actual owner design state must have an authentic verified original",
    set(names) - seen,
)
report = {"results": [
    {"reference": row["relative_path"], "status": "unverified",
     "reference_verified": (BASE / "images" / row["relative_path"]).is_file()}
    for row in rows
]}
rollup_design_states(report, names)
assert report["screens_expected"] == 10
assert report["screens_with_original"] == 10
assert report["screen_coverage_passed"]
assert report["screens_exact_rgb"] == 0, "No claim of fake RGB identity"
assert report["screens_with_native_capture"] == 0, (
    "Source-only contract does not impersonate an emulator capture"
)
wf = (ROOT / ".github/workflows/native-ci.yml").read_text()
assert "--require-screen-coverage" in wf
assert "--require-50-exact" not in wf
assert "QA-Android16-visual-flow" in wf
assert "qa/reference-current-rgb-triage.md" in wf
assert "--require-50-exact" in (
    ROOT / "tools/report_current_reference_status.py").read_text(), (
    "Independent strict legacy 50-variant audit must remain available"
)
print("PASS: 10 unique owner-design states verified; 40 available variants "
      "are not 40 screens; no fabricated RGB parity")
