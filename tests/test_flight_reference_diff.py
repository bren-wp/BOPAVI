#!/usr/bin/env python3
"""Self-contained numeric regression for the flight reference pixel-diff CLI."""
import importlib.util
from pathlib import Path
from PIL import Image

script = Path(__file__).resolve().parents[1] / "tools/compare_flight_reference.py"
spec = importlib.util.spec_from_file_location("flight_diff", script)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
reference = Image.new("RGB", (480, 800), (25, 50, 100))
copy = reference.copy()
same = module.compare(reference, copy)
assert same["overall"]["pixel_identical"]
assert same["overall"]["mae_rgb"] == 0
assert all(region["pixel_identical"] for region in same["regions"].values())
changed = reference.copy()
changed.putpixel((20, 25), (255, 255, 255))
diff = module.compare(reference, changed)
assert not diff["overall"]["pixel_identical"]
assert diff["overall"]["changed_pixels_above_threshold"] == 1
assert diff["regions"]["hud"]["changed_pixels_above_threshold"] == 1
assert diff["regions"]["hero"]["changed_pixels_above_threshold"] == 0
try:
    module.compare(reference, Image.new("RGB", (941, 1672)))
except ValueError as exc:
    assert "Size mismatch" in str(exc)
else:
    raise AssertionError("Mismatched image sizes cannot pass the diff tool")
print("PASS: pixel metric regions, true equality and nonmatching viewport rejection")
