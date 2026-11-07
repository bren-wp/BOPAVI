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
from qa_owner_zero_pixel import SIZE, SCREEN_SIZE, OWNER_SHA256, score


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
            self.assertGreater(failed["rgb_deviation_percent"], 0)
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

    def test_three_owner_941_screens_require_unscaled_real_pixels(self):
        for name in ("Gameplay","LevelMap","Shop"):
            self.assertEqual(SCREEN_SIZE[name],(941,1672))
            self.assertEqual(len(OWNER_SHA256[name]),64)
            with tempfile.TemporaryDirectory() as temp:
                root=Path(temp)
                reference=root/"owner.png"
                capture=root/"actual.png"
                Image.new("RGB",(941,1672),(48,144,223)).save(reference)
                digest=hashlib.sha256(reference.read_bytes()).hexdigest()
                Image.new("RGB",(941,1672),(48,144,223)).save(capture)
                self.assertTrue(score(name,reference,capture,digest)["zero_pixel_deviation"])
                changed=Image.open(capture).convert("RGB")
                changed.putpixel((470,800),(48,144,224))
                changed.save(capture)
                self.assertEqual(score(name,reference,capture,digest)["different_pixels"],1)
                with self.assertRaisesRegex(ValueError,"wrong owner reference"):
                    score(name,reference,capture,"0"*64)
                Image.new("RGB",(864,1536),(48,144,223)).save(capture)
                with self.assertRaisesRegex(ValueError,"unscaled"):
                    score(name,reference,capture,digest)

    def test_unsupported_screens_never_pass(self):
        with self.assertRaisesRegex(ValueError, "Only registered owner"):
            score("Unknown", Path("x"), Path("y"), "")


if __name__ == "__main__":
    unittest.main(verbosity=2)
