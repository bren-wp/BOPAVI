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
assert {"hud_wallets", "hud_score", "hud_pause", "upper_pillar",
        "lower_pillar", "pickups"}.issubset(diff["regions"])
# Different HUD areas cannot pass because the backdrop happens to match.
pause_capture = reference.copy()
pause_capture.putpixel((450, 20), (255, 255, 255))
pause_difference = module.compare(reference, pause_capture)
assert pause_difference["regions"]["hud_pause"]["changed_pixels_above_threshold"] == 1
assert pause_difference["regions"]["hero"]["changed_pixels_above_threshold"] == 0
strict = module.compare(reference, pause_capture, max_changed_percent=0.0)
assert not strict["tolerance"]["overall_within_limit"]
assert not strict["tolerance"]["region_within_limit"]["hud_pause"]
assert not strict["tolerance"]["visual_equivalence_proven"]
same_with_limit = module.compare(reference, reference.copy(), max_changed_percent=10.0)
assert same_with_limit["tolerance"]["overall_within_limit"]
assert all(same_with_limit["tolerance"]["region_within_limit"].values())
try:
    module.compare(reference, reference.copy(), max_changed_percent=101.0)
except ValueError:
    pass
else:
    raise AssertionError("Over-100% visual tolerance must be rejected")
try:
    module.compare(reference, Image.new("RGB", (941, 1672)))
except ValueError as exc:
    assert "Size mismatch" in str(exc)
else:
    raise AssertionError("Mismatched image sizes cannot pass the diff tool")
print("PASS: pixel metric regions, true equality and nonmatching viewport rejection")
