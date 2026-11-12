#!/usr/bin/env python3
"""New BOPAVI owner pack metadata and strict pixel comparison regression suite."""
import json
import sys
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/"tools"))
from install_current_references import read_index
from compare_current_references import pixel_metrics,compare_records

manifest,rows=read_index()
assert manifest["archive_sha256"]=="0df582d495744b6c169c653a277a426c78f686dc240932fb4f931695aca1bb8c"
assert len(rows)==50
assert {r["relative_path"].split("/",1)[0] for r in rows}==set(manifest["viewports"])
assert {r["relative_path"].split("/",1)[1] for r in rows}==set(manifest["screens"])
assert manifest["home_actions"]==["Shop","Skins","Settings"]
assert manifest["golden_ring_trails_enabled"] is False
assert manifest["source_type"].startswith("owner-provided static")
design=Image.new("RGB",(12,20),(12,20,30))
same=pixel_metrics(design,design.copy())
assert same["pixel_identical"] and same["different_pixels"]==0
assert same["different_pixels_percent"]==0.0 and same["mae_rgb_0_255"]==0
changed=design.copy()
changed.putpixel((4,11),(13,20,30))
diff=pixel_metrics(design,changed)
assert diff["different_pixels"]==1 and not diff["pixel_identical"]
assert round(diff["different_pixels_percent"],3)==round(100/240,3)
try:
    pixel_metrics(design,Image.new("RGB",(13,20)))
except ValueError as exc:
    assert "Size mismatch" in str(exc)
else:
    raise AssertionError("Different resolutions must never count as 1:1")
rows_subset=[rows[0]]
result=compare_records(rows_subset,{},Path("/a/nonexistent/location"),{},None)
assert len(result)==1 and result[0]["status"]=="unverified"
assert "No native Android capture mapped" in result[0]["reason"]
# Check no synthetic PNG bytes or screenshot resizing in the comparator.
code=(ROOT/"tools/compare_current_references.py").read_text()
assert ".resize(" not in code and ".crop(" not in code
assert "No native Android capture mapped" in code
assert "all_exact" in code
print("PASS: 50 pinned owner mockups, 5 viewports, zero-tolerance RGB diff and fail-closed captures")
