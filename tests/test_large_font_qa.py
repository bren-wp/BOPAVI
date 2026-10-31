#!/usr/bin/env python3
"""Fail-closed unit regressions for Android large-font hit-target measurements."""
import sys
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from qa_android_fontscale import TARGETS, measured_targets, native_settings_home


def sample(width=720, height=1280, density=320):
    root = ET.Element("hierarchy")
    for key, (widget, caption, minimum_dp) in TARGETS.items():
        h = minimum_dp * density // 160
        ET.SubElement(root, "node", {
            "class": "android.widget." + widget,
            "content-desc": caption,
            "text": caption,
            "clickable": "false" if widget == "SeekBar" else "true",
            "focusable": "false" if widget == "SeekBar" else "true",
            "enabled": "true",
            "bounds": f"[20,100][{width-20},{100+h}]",
        })
    return root


class LargeFontGeometryTests(unittest.TestCase):
    def test_all_eleven_native_controls_keep_measured_hit_targets(self):
        root = sample()
        self.assertEqual(set(TARGETS), measured_targets(root, 720, 1280, 320))

    def test_cropped_difficulty_button_or_switch_cannot_pass(self):
        root = sample()
        nodes = root.findall("node")
        nodes[0].set("bounds", "[20,100][700,150]")  # 25dp choice button
        nodes[5].set("bounds", "[20,100][700,200]")  # 50dp switch
        seen = measured_targets(root, 720, 1280, 320)
        self.assertNotIn("relaxed", seen)
        self.assertNotIn("motion", seen)
        self.assertIn("sound", seen)

    def test_seekbar_accessibility_does_not_require_clickable_button_semantics(self):
        root = sample()
        # Android exposes enabled/focusable native range controls that can
        # receive a drag or accessibility adjust action without clickable=true.
        self.assertEqual(root.findall("node")[6].get("clickable"), "false")
        self.assertIn("music", measured_targets(root, 720, 1280, 320))
        root.findall("node")[6].set("enabled", "false")
        self.assertNotIn("music", measured_targets(root, 720, 1280, 320))

    def test_offscreen_or_noninteractive_controls_cannot_pass(self):
        root = sample()
        root.findall("node")[1].set("bounds", "[20,1200][700,1400]")
        root.findall("node")[2].set("clickable", "false")
        seen = measured_targets(root, 720, 1280, 320)
        self.assertNotIn("standard", seen)
        self.assertNotIn("challenging", seen)

    def test_full_screen_settings_replaces_obsolete_dialog_targets(self):
        qa = (ROOT / "tools/qa_android_fontscale.py").read_text()
        self.assertEqual(11, len(TARGETS))
        self.assertEqual(("Button", "home", 48), TARGETS["home"])
        self.assertEqual(("Button", "pročitaj politiku privatnosti", 48),
                         TARGETS["privacy"])
        self.assertEqual(("Button", "pročitaj uvjete korištenja", 48),
                         TARGETS["terms"])
        self.assertTrue(all(TARGETS[key][0] == "Button" for key in
                            ("relaxed", "standard", "challenging")))
        self.assertNotIn('"RadioButton",', qa)
        self.assertNotIn('"gotovo"', qa)
        self.assertIn('header_seen |= "settings" in strings', qa)
        self.assertIn('Settings HOME did not return Home', qa)

    def test_disabled_home_and_clipped_legal_buttons_are_rejected(self):
        root = sample()
        nodes = root.findall("node")
        nodes[-1].set("clickable", "false")  # Home must be genuinely tappable
        nodes[-2].set("bounds", "[20,1240][700,1400]")
        actual = measured_targets(root, 720, 1280, 320)
        self.assertNotIn("home", actual)
        self.assertNotIn("terms", actual)
        self.assertIn("privacy", actual)

    def test_bottom_home_requires_actual_visible_native_action(self):
        root = ET.Element("hierarchy")
        breadcrumb = ET.SubElement(root, "node", {
            "class": "android.widget.Button", "content-desc": "‹  HOME",
            "clickable": "true", "enabled": "true",
            "bounds": "[20,30][300,135]",
        })
        self.assertEqual([], native_settings_home(root, 720, 1280, 320))
        bottom = ET.SubElement(root, "node", {
            "class": "android.widget.Button", "content-desc": "HOME",
            "clickable": "true", "enabled": "true",
            "bounds": "[20,1060][700,1190]",
        })
        self.assertEqual([bottom], native_settings_home(root, 720, 1280, 320))
        bottom.set("bounds", "[20,1240][700,1380]")
        self.assertEqual([], native_settings_home(root, 720, 1280, 320))
        bottom.set("bounds", "[20,1060][700,1190]")
        bottom.set("clickable", "false")
        self.assertEqual([], native_settings_home(root, 720, 1280, 320))
        self.assertIsNotNone(breadcrumb)

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
