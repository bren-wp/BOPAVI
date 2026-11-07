#!/usr/bin/env python3
"""Unit checks: Android 7-size target evaluator never fabricates acceptance."""
from pathlib import Path
import sys
import tempfile
import unittest

from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/"tools"))
from qa_responsive_home_splash import (
    VIEWPORTS,fit_center_box,score_viewport,open_native,mean_rgb_delta,
)
from qa_two_owner_reference_screens import SIZE


class AndroidResponsiveReferenceTests(unittest.TestCase):
    def test_required_real_device_matrix(self):
        self.assertEqual(set(VIEWPORTS), {
            "small","short","compact","reference",
            "standard","tall","large"})
        self.assertEqual(VIEWPORTS["small"],(480,800,160))
        self.assertEqual(VIEWPORTS["large"],(1440,2960,560))

    def test_unchanged_real_resolution_and_aspect_ratio(self):
        for name,(width,height,_) in VIEWPORTS.items():
            box=fit_center_box((100,400,800,1100),width,height)
            x1,y1,x2,y2=box
            self.assertTrue(0<=x1<x2<=width,name)
            self.assertTrue(0<=y1<y2<=height,name)
        self.assertEqual(fit_center_box((0,0,*SIZE),*SIZE),(0,0,*SIZE))

    def test_no_silent_resize_of_emulator_capture(self):
        with tempfile.TemporaryDirectory() as folder:
            source=Path(folder)/"device.png"
            Image.new("RGB",(479,800),(0,0,0)).save(source)
            with self.assertRaisesRegex(ValueError,"expected unmodified"):
                open_native(source,(480,800))

    def test_ten_percent_gate_on_small_real_pixels(self):
        with tempfile.TemporaryDirectory() as folder:
            root=Path(folder)
            original=root/"owner.png"
            actual=root/"device.png"
            Image.new("RGB",SIZE,(100,100,100)).save(original)
            Image.new("RGB",(480,800),(125,125,125)).save(actual)
            passed=score_viewport(original,actual,"Home","small",(480,800))
            self.assertEqual(passed["native_size"],[480,800])
            self.assertTrue(passed["meets_target"])
            self.assertTrue(all(v["meets_target"]
                                for v in passed["regions"].values()))
            Image.new("RGB",(480,800),(126,126,126)).save(actual)
            failed=score_viewport(original,actual,"Splash","small",(480,800))
            self.assertFalse(failed["meets_target"])
            self.assertFalse(failed["whole_screen_target_met"])

    def test_full_native_941_reference_is_never_reprojected(self):
        with tempfile.TemporaryDirectory() as folder:
            root=Path(folder)
            ref=root/"ref.png"
            shot=root/"capture.png"
            Image.new("RGB",SIZE,(41,102,215)).save(ref)
            Image.new("RGB",SIZE,(41,102,215)).save(shot)
            outcome=score_viewport(ref,shot,"Splash","reference",SIZE)
            self.assertTrue(outcome["meets_target"])
            self.assertFalse(outcome["projection"]["center_crop_or_letterbox_may_differ"])
            self.assertEqual(outcome["whole_screen_rgb_mae"],0)
            self.assertEqual(mean_rgb_delta(open_native(ref,SIZE),
                                            open_native(shot,SIZE)),0)


if __name__=="__main__":
    unittest.main(verbosity=2)
