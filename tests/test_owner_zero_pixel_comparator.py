#!/usr/bin/env python3
"""No resampling, hiding differing pixels or asserting 0% from a rounded MAE."""
from pathlib import Path
import hashlib
import sys
import tempfile
import unittest

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from qa_owner_zero_pixel import SIZE, score


class ZeroPixelAcceptanceTests(unittest.TestCase):
    def test_real_decoded_reference_pixels_have_exact_0_only_when_identical(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            owner = root / "owner.jpeg"
            native = root / "native.png"
            Image.new("RGB", SIZE, (64, 132, 215)).save(owner, "JPEG")
            expected = hashlib.sha256(owner.read_bytes()).hexdigest()
            # The actual JPEG decoder output is the pixel authority: an
            # identical PNG has 0 changed pixels independent of file bytes.
            with Image.open(owner) as image:
                pixels = image.convert("RGB")
            pixels.save(native)
            passed = score("Splash", owner, native, expected)
            self.assertEqual(passed["different_pixels"], 0)
            self.assertEqual(passed["rgb_mae"], 0)
            self.assertTrue(passed["zero_pixel_deviation"])
            pixels.putpixel((211, 977), (65, 132, 215))
            pixels.save(native)
            failed = score("Splash", owner, native, expected)
            self.assertEqual(failed["different_pixels"], 1)
            self.assertGreater(failed["rgb_mae"], 0)
            self.assertFalse(failed["zero_pixel_deviation"])

    def test_wrong_reference_sha_or_capture_dimensions_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            owner = root / "owner.jpeg"
            native = root / "native.png"
            Image.new("RGB", SIZE, (11, 22, 33)).save(owner, "JPEG")
            h = hashlib.sha256(owner.read_bytes()).hexdigest()
            Image.new("RGB", (941, 1672), (11, 22, 33)).save(native)
            with self.assertRaisesRegex(ValueError, "unscaled"):
                score("Home", owner, native, h)
            with self.assertRaisesRegex(ValueError, "wrong owner reference"):
                score("Home", owner, native, "0" * 64)

    def test_unsupported_screens_never_pass(self):
        with self.assertRaisesRegex(ValueError, "Only Splash and Home"):
            score("Gameplay", Path("x"), Path("y"), "")


if __name__ == "__main__":
    unittest.main(verbosity=2)
