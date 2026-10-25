#!/usr/bin/env python3
"""Fail-closed unit regressions for Android large-font hit-target measurements."""
import sys
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from qa_android_fontscale import TARGETS, measured_targets


def sample(width=720, height=1280, density=320):
    root = ET.Element("hierarchy")
    for key, (widget, caption, minimum_dp) in TARGETS.items():
        h = minimum_dp * density // 160
        ET.SubElement(root, "node", {
            "class": "android.widget." + widget,
            "content-desc": caption,
            "text": caption,
            "clickable": "true",
            "bounds": f"[20,100][{width-20},{100+h}]",
        })
    return root


class LargeFontGeometryTests(unittest.TestCase):
    def test_all_nine_native_controls_keep_measured_hit_targets(self):
        root = sample()
        self.assertEqual(set(TARGETS), measured_targets(root, 720, 1280, 320))

    def test_cropped_radio_button_or_switch_cannot_pass(self):
        root = sample()
        nodes = root.findall("node")
        nodes[0].set("bounds", "[20,100][700,150]")  # 25dp radio
        nodes[5].set("bounds", "[20,100][700,200]")  # 50dp switch
        seen = measured_targets(root, 720, 1280, 320)
        self.assertNotIn("relaxed", seen)
        self.assertNotIn("motion", seen)
        self.assertIn("sound", seen)

    def test_offscreen_or_noninteractive_controls_cannot_pass(self):
        root = sample()
        root.findall("node")[1].set("bounds", "[20,1200][700,1400]")
        root.findall("node")[2].set("clickable", "false")
        seen = measured_targets(root, 720, 1280, 320)
        self.assertNotIn("standard", seen)
        self.assertNotIn("challenging", seen)

    def test_real_emulator_matrix_is_wired_into_api35_smoke(self):
        qa = (ROOT / "tools/qa_android_fontscale.py").read_text()
        launcher = (ROOT / "tools/qa_android_emulator.sh").read_text()
        workflow = (ROOT / ".github/workflows/native-ci.yml").read_text()
        for required in ("compact-fs100", "compact-fs130", "compact-fs150",
                         "tall-fs150", "uiautomator", "screencap", '"wm", "size", "reset"'):
            self.assertIn(required, qa)
        self.assertIn("python3 tools/qa_android_fontscale.py", launcher)
        self.assertEqual(workflow.count("BOPAVI_VIEWPORT_QA=1 sh tools/qa_android_emulator.sh"), 2)
        self.assertIn("python tests/test_large_font_qa.py", workflow)


if __name__ == "__main__":
    unittest.main(verbosity=2)
