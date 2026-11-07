#!/usr/bin/env python3
"""Verify a *real* Splash/Loading View on each Android viewport.

Runs against an installed app and unmodified adb PNG files; never renders
a substitute mockup, claims visual 1:1 or fakes completion percentage.
"""
from __future__ import annotations

from pathlib import Path
import struct
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

from PIL import Image, ImageStat

PACKAGE = "com.brendigo.bopavi"
OUTPUT = Path("qa/screenshots")
PNG_MAGIC = bytes.fromhex("89504e470d0a1a0a")


def audit(dimensions: str, dpi: int, label: str) -> None:
    width, height = map(int, dimensions.split("x"))
    assert 0 < width < height and 120 <= dpi <= 700
    path = OUTPUT / f"android-loading-{label}.png"
    raw = path.read_bytes()
    assert raw.startswith(PNG_MAGIC), f"{label}: no native Splash PNG"
    assert struct.unpack(">II", raw[16:24]) == (width, height), (
        label, "wrong Android screenshot resolution")
    with Image.open(path) as image:
        assert image.format == "PNG" and image.size == (width, height)
        # No constant-colour placeholders accepted in place of actual artwork.
        variances = ImageStat.Stat(image.convert("RGB")).var
        assert min(variances) > 120.0, (
            label, "Splash is blank or rendering failed", variances)

    for attempt in range(5):
        try:
            subprocess.run(["adb", "shell", "uiautomator", "dump",
                            "/sdcard/bopavi-splash-viewport.xml"],
                           capture_output=True, check=True, timeout=35)
            data = subprocess.check_output(
                ["adb", "exec-out", "cat",
                 "/sdcard/bopavi-splash-viewport.xml"], timeout=35)
            root = ET.fromstring(data)
            nodes = [node for node in root.iter("node")
                     if node.get("package") == PACKAGE]
            loading = [node for node in nodes
                       if "Loading level" in node.get("content-desc", "")]
            assert len(loading) == 1, (
                label, "one accessible real Loading view required",
                len(loading))
            assert loading[0].get("clickable") != "true", (
                label, "Loading artwork must not be a fake button")
            (OUTPUT / f"android-loading-{label}.xml").write_bytes(data)
            print(f"PASS: {label} ({width}x{height}, {dpi} dpi): "
                  "native detailed Splash and accessible Loading state",
                  flush=True)
            return
        except (ET.ParseError, AssertionError,
                subprocess.SubprocessError) as exc:
            if attempt == 4:
                raise AssertionError(
                    f"{label}: actual native Loading hierarchy unavailable") from exc
            time.sleep(1)


if __name__ == "__main__":
    if len(sys.argv) != 4:
        raise SystemExit("usage: qa_splash_viewports.py WxH density label")
    audit(sys.argv[1], int(sys.argv[2]), sys.argv[3])
