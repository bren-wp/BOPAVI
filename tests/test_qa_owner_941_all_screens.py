#!/usr/bin/env python3
"""Standalone nine-screen pixel comparator tests, no copied UI screenshots."""
import importlib.util
from pathlib import Path
import unittest
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
SPEC=importlib.util.spec_from_file_location('owner941',
    ROOT/'tools/qa_owner_941_all_screens.py')
assert SPEC and SPEC.loader
checker=importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(checker)

class Owner941PixelTests(unittest.TestCase):
    def test_entire_original_owner_collection_pinned(self):
        self.assertEqual(set(checker.OWNER_SHA256),{
            'Splash','Home','Gameplay','LevelMap','Shop','Skins',
            'Settings','LevelComplete','GameOver'})
        self.assertTrue(all(len(v)==64 for v in checker.OWNER_SHA256.values()))
        self.assertEqual(checker.SIZE,(941,1672))

    def test_one_channel_one_pixel_never_rounds_to_zero(self):
        a=Image.new('RGB',checker.SIZE,(10,55,130))
        b=a.copy()
        same=checker.pixel_report(a,b,'Home')
        self.assertTrue(same['exact_rgb_match'])
        self.assertEqual(same['different_pixels'],0)
        b.putpixel((220,1500),(10,55,131))
        diff=checker.pixel_report(a,b,'Home')
        self.assertFalse(diff['exact_rgb_match'])
        self.assertEqual(diff['different_pixels'],1)
        self.assertGreater(diff['rgb_mae'],0)
        self.assertEqual(diff['regions_by_deviation'][0]['name'],'footer')

    def test_unsupported_and_resized_references_fail(self):
        im=Image.new('RGB',checker.SIZE,(1,2,3))
        with self.assertRaisesRegex(ValueError,'Unsupported'):
            checker.compare('unknown',Path('missing'),Path('missing'))
        with self.assertRaisesRegex(ValueError,'non-native'):
            checker.pixel_report(im,Image.new('RGB',(864,1536)),'Splash')

if __name__=='__main__':unittest.main()
