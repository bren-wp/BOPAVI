#!/usr/bin/env python3
"""Deterministic checks for the native two-screen RGB comparator."""
from pathlib import Path
import sys
import tempfile
import unittest

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from qa_two_owner_reference_screens import (
    SIZE, REGIONS, TARGET_MAX_DEVIATION_PERCENT, TARGET_MAX_RGB_MAE,
    compare, load_png, mae, deviation_percent, passes_target,
)


class TwoScreenPixelComparisonTests(unittest.TestCase):
    def test_10_percent_threshold_uses_unrounded_rgb_error(self):
        self.assertEqual(TARGET_MAX_DEVIATION_PERCENT, 10.0)
        self.assertEqual(TARGET_MAX_RGB_MAE, 25.5)
        self.assertTrue(passes_target(25.5))
        self.assertFalse(passes_target(25.50001))
        self.assertEqual(deviation_percent(255.0), 100.0)

    def test_every_named_roi_must_also_be_within_10_percent(self):
        with tempfile.TemporaryDirectory() as tmp:
            reference=Path(tmp)/"reference.png"
            capture=Path(tmp)/"capture.png"
            Image.new("RGB",SIZE,(90,90,90)).save(reference)
            Image.new("RGB",SIZE,(115,115,115)).save(capture)
            passing=compare(reference,capture,"Home")
            self.assertTrue(passing["whole_screen_target_met"])
            self.assertTrue(passing["all_regions_target_met"])
            self.assertTrue(passing["meets_target"])
            self.assertTrue(all(r["meets_target"] for r in passing["regions_ranked"]))
            Image.new("RGB",SIZE,(116,116,116)).save(capture)
            failing=compare(reference,capture,"Splash")
            self.assertFalse(failing["whole_screen_target_met"])
            self.assertFalse(failing["all_regions_target_met"])
            self.assertFalse(failing["meets_target"])
            self.assertTrue(all(not r["meets_target"] for r in failing["regions_ranked"]))

    def test_identical_pixels_are_zero_and_explicit(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "owner.png"
            Image.new("RGB", SIZE, (25, 88, 189)).save(path)
            self.assertEqual(0, mae(load_png(path), load_png(path)))
            for screen in ("Home", "Splash"):
                report = compare(path, path, screen)
                self.assertTrue(report["pixel_identical"])
                self.assertEqual(0, report["whole_screen_rgb_mae"])
                self.assertTrue(all(
                    r["rgb_mae"] == 0 for r in report["regions_ranked"]))
                self.assertEqual(report["resolution"], list(SIZE))

    def test_silent_resizing_and_fake_full_match_are_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            wrong = Path(tmp) / "wrong.png"
            Image.new("RGB", (940, 1672), (13, 85, 234)).save(wrong)
            with self.assertRaisesRegex(ValueError, "exactly"):
                load_png(wrong)
            ref = Image.new("RGB", SIZE, (0, 0, 0))
            changed = Image.new("RGB", SIZE, (255, 255, 255))
            self.assertEqual(255.0, mae(ref, changed))

    def test_roi_ranking_is_independent_of_background_outside_box(self):
        with tempfile.TemporaryDirectory() as tmp:
            reference = Path(tmp) / "reference.png"
            capture = Path(tmp) / "capture.png"
            Image.new("RGB", SIZE, (0, 0, 0)).save(reference)
            image = Image.new("RGB", SIZE, (0, 0, 0))
            # Top-right wallet samples differ, but the rest is exactly black.
            from PIL import ImageDraw
            ImageDraw.Draw(image).rectangle((450, 15, 918, 84),
                                            fill=(255, 255, 255))
            image.save(capture)
            result = compare(reference, capture, "Home")
            self.assertFalse(result["pixel_identical"])
            self.assertGreater(result["whole_screen_rgb_mae"], 0)
            self.assertEqual("wallet_and_safe_top",
                             result["regions_ranked"][0]["name"])
            # PIL's rectangle includes x=918 and y=84: exactly 469×70
            # changed pixels inside the 485×97 wallet ROI. Compute the
            # exact expected mean instead of asserting an impossible >200.
            expected=(469 * 70 * 255) / (485 * 97)
            self.assertAlmostEqual(result["regions_ranked"][0]["rgb_mae"],
                                   expected, delta=.001)
            self.assertFalse(result["regions_ranked"][0]["meets_target"])

    def test_every_roi_stays_inside_original_reference_grid(self):
        for regions in REGIONS.values():
            for name, box in regions.items():
                l,t,r,b=box
                self.assertTrue(0 <= l < r <= SIZE[0], name)
                self.assertTrue(0 <= t < b <= SIZE[1], name)


if __name__ == "__main__":
    unittest.main(verbosity=2)
