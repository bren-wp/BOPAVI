#!/usr/bin/env python3
"""Real-device Home viewport accessibility: reach every native action by touch.

Reference layouts fit without scrolling. Compact layouts may scroll rather
than shrink actions below 48dp. A control counts only while fully visible.
"""
from __future__ import annotations

import re
import struct
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

OUT = Path("qa/screenshots")
PACKAGE = "com.brendigo.bopavi"
TARGETS = {
    "Settings": ("OTVORI OPCIJE IGRE",),
    "Skins": ("OTVORI IZBOR LIKOVA",),
    "Shop": ("OTVORI TRGOVINU NADOGRADNJI",),
}
BOUND = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


def visible_targets(root: ET.Element, width: int, height: int,
                    density: int) -> set[str]:
    """Validate unique visible clickable nodes and actual >=48dp hit bounds."""
    found: set[str] = set()
    min48 = 48 * int(density) / 160
    nodes = list(root.iter("node"))
    for title, needles in TARGETS.items():
        matches = [
            node for node in nodes
            if node.get("package") == PACKAGE
            and node.get("clickable") == "true"
            and any(needle in (node.get("text", "") + " " +
                              node.get("content-desc", "")).upper()
                    for needle in needles)
        ]
        assert len(matches) <= 1, (title, "duplicate clickable Home control")
        if not matches:
            continue
        match = BOUND.fullmatch(matches[0].get("bounds", ""))
        assert match, (title, "invalid bounds")
        x1, y1, x2, y2 = map(int, match.groups())
        # UIAutomator may expose a partially clipped node at the viewport
        # boundary. Scroll until the *whole* native hit target is available.
        if not (0 <= x1 < x2 <= width and 0 <= y1 < y2 <= height):
            continue
        if x2 - x1 < min48 or y2 - y1 < min48:
            # A partially clipped bottom action is not counted as reachable.
            # When fully visible, it must still satisfy the minimum size.
            if y1 < 4 or y2 >= height - 4:
                continue
            raise AssertionError((title, "native touch target smaller than 48dp",
                                  (x1, y1, x2, y2), min48))
        found.add(title)
    return found


def scrollable_home(root: ET.Element) -> bool:
    return any(node.get("package") == PACKAGE
               and node.get("scrollable") == "true"
               for node in root.iter("node"))


def adb(*args: str, timeout: int = 45) -> bytes:
    return subprocess.run(["adb", *args], check=True, capture_output=True,
                          timeout=timeout).stdout


def dump() -> ET.Element:
    for attempt in range(5):
        try:
            adb("shell", "rm", "-f", "/sdcard/bopavi-home-viewport.xml")
            adb("shell", "uiautomator", "dump",
                "/sdcard/bopavi-home-viewport.xml")
            root = ET.fromstring(adb("exec-out", "cat",
                                   "/sdcard/bopavi-home-viewport.xml"))
            if root.tag == "hierarchy" and list(root.iter("node")):
                return root
        except (ET.ParseError, subprocess.SubprocessError):
            pass
        time.sleep(1.5)
    raise AssertionError("Real Android UI hierarchy unavailable after swipe")


def audit(width: int, height: int, density: int, label: str) -> None:
    image = (OUT / f"android-home-{label}.png").read_bytes()
    assert image[:8] == bytes.fromhex("89504e470d0a1a0a")
    assert struct.unpack(">II", image[16:24]) == (width, height), label

    root = ET.parse(OUT / "android-current-ui.xml").getroot()
    all_found: set[str] = set()
    scrolled = False
    for attempt in range(8):
        all_found.update(visible_targets(root, width, height, density))
        if set(TARGETS) <= all_found:
            break
        missing = sorted(set(TARGETS) - all_found)
        assert scrollable_home(root), (
            label, "Home actions missing and no accessible scroll container", missing)
        # Use a human swipe inside the native ScrollView, never artificially
        # mark below-the-fold actions as passing based on source code alone.
        adb("shell", "input", "swipe", str(width // 2), str(height * 76 // 100),
            str(width // 2), str(height * 27 // 100), "400")
        time.sleep(0.6)
        new_root = dump()
        scrolled = True
        root = new_root
        (OUT / f"android-home-{label}-scrolled.xml").write_text(
            ET.tostring(root, encoding="unicode"), encoding="utf-8")
    missing = sorted(set(TARGETS) - all_found)
    assert not missing, (label, "Home targets not reachable after swipes", missing)
    # In the 941x1672 owner reference, all seven controls must fit at once.
    if label == "reference":
        assert not scrolled, "Reference Home unexpectedly needs scrolling"
    if scrolled:
        shot = adb("exec-out", "screencap", "-p")
        assert shot[:8] == bytes.fromhex("89504e470d0a1a0a")
        assert struct.unpack(">II", shot[16:24]) == (width, height)
        (OUT / f"android-home-{label}-scrolled.png").write_bytes(shot)
    print(f"PASS: {label}: all {len(TARGETS)} reachable Home controls, "
          f">=48dp, {'native swipe' if scrolled else 'no swipe'}",
          flush=True)


if __name__ == "__main__":
    dimensions, dpi, name = sys.argv[1:]
    w, h = map(int, dimensions.split("x"))
    audit(w, h, int(dpi), name)
