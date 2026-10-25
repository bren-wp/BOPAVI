#!/usr/bin/env python3
"""Real Android emulator audit of BOPAVI Options at 1.0/1.3/1.5 font scale.

Run after the full gameplay smoke while the same emulator is still available.
No mock views or source-only assertions: every target comes from UIAutomator.
"""
from __future__ import annotations

import re
import struct
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

SCREENSHOTS = Path("qa/screenshots")
PKG = "com.brendigo.bopavi"
# Keep controls in native dp rather than shrinking them to fit the dialog.
TARGETS = {
    "relaxed": ("RadioButton", "težina: opušteno", 52),
    "standard": ("RadioButton", "težina: standardno", 52),
    "challenging": ("RadioButton", "težina: izazovno", 52),
    "sound": ("Switch", "zvuk uključen", 64),
    "haptics": ("Switch", "vibracija pri igranju", 64),
    "motion": ("Switch", "smanjeni pokret i manje osvježavanja", 64),
    "music": ("SeekBar", "glazba", 48),
    "effects": ("SeekBar", "zvučni efekti", 48),
    "done": ("Button", "gotovo", 48),
}


def adb(*args: str, timeout: int = 35) -> str:
    result = subprocess.run(["adb", *args], check=True, capture_output=True,
                            text=True, timeout=timeout)
    return result.stdout.strip()


def bounds(node: ET.Element) -> tuple[int, int, int, int] | None:
    match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
    return tuple(map(int, match.groups())) if match else None


def measured_targets(root: ET.Element, width: int, height: int, density: int) -> set[str]:
    """Only count actual, fully visible, adequately sized native hit targets."""
    result: set[str] = set()
    for node in root.iter("node"):
        name = node.get("class", "").split(".")[-1]
        text = (node.get("content-desc") or node.get("text") or "").casefold()
        rect = bounds(node)
        # Native SeekBar handles drag and range adjustments while enabled.
        # UIAutomator often reports clickable=false and may report
        # focusable=false in touch mode. Neither indicates an inert slider:
        # check its concrete widget class, enabled state and measured bounds.
        interactive = (node.get("enabled", "true") == "true"
                       if name == "SeekBar"
                       else node.get("clickable") == "true")
        if rect is None or not interactive:
            continue
        left, top, right, bottom = rect
        if not (0 <= left < right <= width and 0 <= top < bottom <= height):
            continue
        for key, (widget, caption, minimum_dp) in TARGETS.items():
            if name != widget or caption not in text:
                continue
            # Allow only one pixel for dp-to-px rounding.
            if right - left >= int(minimum_dp * density / 160) - 1 and \
                    bottom - top >= int(minimum_dp * density / 160) - 1:
                result.add(key)
    return result


def dump() -> ET.Element:
    for attempt in range(5):
        try:
            adb("shell", "rm", "-f", "/sdcard/bopavi-accessible-ui.xml")
            adb("shell", "uiautomator", "dump", "/sdcard/bopavi-accessible-ui.xml",
                timeout=40)
            xml = adb("exec-out", "cat", "/sdcard/bopavi-accessible-ui.xml")
            root = ET.fromstring(xml)
            if root.tag == "hierarchy" and root.findall(".//node"):
                return root
        except (subprocess.SubprocessError, ET.ParseError, ValueError):
            pass
        time.sleep(2)
    raise AssertionError("FAIL: no valid Android UI hierarchy at large font scale")


def tap(node: ET.Element) -> None:
    rect = bounds(node)
    if rect is None:
        raise AssertionError("FAIL: native control has no tap bounds")
    l, t, r, b = rect
    adb("shell", "input", "tap", str((l+r)//2), str((t+b)//2))


def swipe(width: int, height: int) -> None:
    adb("shell", "input", "swipe", str(width//2), str(height*73//100),
        str(width//2), str(height*28//100), "430")
    time.sleep(0.6)


def screenshot(name: str, width: int, height: int) -> None:
    SCREENSHOTS.mkdir(parents=True, exist_ok=True)
    dest = SCREENSHOTS / (name + ".png")
    output = subprocess.check_output(["adb", "exec-out", "screencap", "-p"], timeout=30)
    assert output[:8] == bytes.fromhex("89504e470d0a1a0a")
    assert len(output) > 2000 and struct.unpack(">II", output[16:24]) == (width, height)
    dest.write_bytes(output)


def audit_case(label: str, width: int, height: int, density: int, scale: str) -> None:
    print(f"QA: Options {label}: {width}x{height} density={density} font={scale}", flush=True)
    adb("shell", "wm", "size", f"{width}x{height}")
    adb("shell", "wm", "density", str(density))
    adb("shell", "settings", "put", "system", "font_scale", scale)
    # Restart the Activity so its resources and TextViews use the new config.
    time.sleep(1)
    adb("shell", "am", "force-stop", PKG)
    adb("shell", "am", "start", "-W", "-n", PKG + "/.MainActivity")
    time.sleep(2)
    assert adb("shell", "pidof", PKG), "FAIL: app did not start after configuration change"
    actual_scale = adb("shell", "settings", "get", "system", "font_scale")
    assert abs(float(actual_scale) - float(scale)) < 0.01, (actual_scale, scale)

    opened = False
    for _ in range(5):
        root = dump()
        links = [n for n in root.iter("node")
                 if n.get("clickable") == "true" and
                 "otvori opcije igre" in n.get("content-desc", "").casefold()]
        if len(links) == 1:
            tap(links[0])
            opened = True
            break
        swipe(width, height)
    assert opened, f"FAIL: Options cannot be opened on {label}"
    time.sleep(1)

    found: set[str] = set()
    header_seen = False
    screenshot_saved = False
    for attempt in range(9):
        root = dump()
        strings = " ".join(((n.get("text") or "") + " " +
                            (n.get("content-desc") or "")).casefold()
                           for n in root.iter("node"))
        header_seen |= "opcije igre" in strings
        found |= measured_targets(root, width, height, density)
        if not screenshot_saved:
            screenshot(f"android-options-{label}", width, height)
            screenshot_saved = True
        if set(TARGETS) <= found:
            break
        swipe(width, height)
    # Preserve the final scrolled state and exact native hierarchy for
    # diagnosing genuine clipping or accessibility failures in CI artifacts.
    SCREENSHOTS.mkdir(parents=True, exist_ok=True)
    (SCREENSHOTS / f"android-options-{label}-final.xml").write_text(
        ET.tostring(root, encoding="unicode"), encoding="utf-8")
    screenshot(f"android-options-{label}-scroll", width, height)
    missing = sorted(set(TARGETS) - found)
    if missing:
        for n in root.iter("node"):
            if (n.get("class") or "").endswith("SeekBar"):
                print(f"QA SeekBar {label}: {n.attrib}", flush=True)
    assert header_seen, f"FAIL: Options dialog missing on {label}"
    assert not missing, f"FAIL: {label} missing, clipped or undersized controls: {missing}"
    # The action bar must remain reachable without hiding the settings content.
    done = [n for n in root.iter("node")
            if n.get("clickable") == "true" and
            (n.get("text") or "").casefold() == "gotovo"]
    assert len(done) == 1, f"FAIL: Options Done unavailable on {label}"
    tap(done[0])
    time.sleep(0.5)
    assert not any("opcije igre" in (n.get("text") or "").casefold()
                   for n in dump().iter("node")), f"FAIL: Options did not close on {label}"
    print(f"PASS: {label} - all nine native controls, full bounds, scrolling and Done", flush=True)


def main() -> None:
    original = adb("shell", "settings", "get", "system", "font_scale")
    cases = (
        ("compact-fs100", 720, 1280, 320, "1.0"),
        ("compact-fs130", 720, 1280, 320, "1.3"),
        ("compact-fs150", 720, 1280, 320, "1.5"),
        ("tall-fs150", 1080, 2400, 420, "1.5"),
    )
    try:
        for case in cases:
            audit_case(*case)
    finally:
        # Even a failed assertion must not poison subsequent emulator tests.
        if original in ("null", ""):
            adb("shell", "settings", "delete", "system", "font_scale")
        else:
            adb("shell", "settings", "put", "system", "font_scale", original)
        adb("shell", "wm", "size", "reset")
        adb("shell", "wm", "density", "reset")


if __name__ == "__main__":
    main()
