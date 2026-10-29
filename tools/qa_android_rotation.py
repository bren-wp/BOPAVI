#!/usr/bin/env python3
"""Android 15/16 real-window orientation QA without restarting the game.

Keep the original 480x800 flight simulation unchanged. The display window can
rotate, while Activity/GameView remain alive and Home actions stay accessible.
Run on the already booted emulator after the baseline screenshot/large-font QA.
"""
import re
import struct
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

APP = "com.brendigo.bopavi"
OUT = Path("qa/screenshots")
OUT.mkdir(parents=True, exist_ok=True)


def adb(*args, timeout=45):
    return subprocess.run(["adb", *map(str, args)], text=True, capture_output=True,
                          check=True, timeout=timeout).stdout.strip()


def hierarchy():
    for _ in range(5):
        try:
            adb("shell", "uiautomator", "dump", "/sdcard/bopavi-rotate.xml")
            data = adb("exec-out", "cat", "/sdcard/bopavi-rotate.xml")
            root = ET.fromstring(data)
            if root.tag == "hierarchy" and list(root.iter("node")):
                return root
        except (subprocess.CalledProcessError, ET.ParseError):
            pass
        time.sleep(2)
    raise AssertionError("FAIL: unavailable Android hierarchy after rotation")


def label(node):
    return ((node.get("text") or "") + " " + (node.get("content-desc") or "")).upper()


def tap(node):
    match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                         node.get("bounds") or "")
    assert match, "FAIL: no native tap bounds"
    l, t, r, b = map(int, match.groups())
    adb("shell", "input", "tap", (l+r)//2, (t+b)//2)


def screenshot(name):
    data = subprocess.check_output(["adb", "exec-out", "screencap", "-p"], timeout=30)
    assert len(data) > 2000 and data[:8] == bytes.fromhex("89504e470d0a1a0a")
    w, h = struct.unpack(">II", data[16:24])
    (OUT / (name + ".png")).write_bytes(data)
    return w, h


def rotate(value):
    # WindowManager command is supported by Android 15/16 emulator builds.
    adb("shell", "cmd", "window", "user-rotation", "lock", value)
    for _ in range(8):
        time.sleep(1)
        w, h = screenshot("android-rotation-current")
        if (w > h) == (value == 1):
            return w, h
    raise AssertionError(f"FAIL: display did not rotate to {value}")


def find_action(root, phrase):
    return [n for n in root.iter("node")
            if n.get("clickable") == "true" and phrase in label(n)]


def main():
    adb("shell", "am", "force-stop", APP)
    adb("shell", "cmd", "window", "user-rotation", "lock", "0")
    # Debug-only QA holds a genuine first-flap GameSimulation state steady.
    # This makes a score/lives continuity comparison deterministic across rotation.
    adb("shell", "am", "start", "-W", "-n", APP + "/.MainActivity",
        "--ez", "bopavi_qa_capture_first_flap", "true")
    try:
        initial_pid = adb("shell", "pidof", APP)
        assert initial_pid, "FAIL: missing BOPAVI process"
        w, h = rotate(1)
        assert w > h, "FAIL: landscape screenshot not wider than tall"
        root = hierarchy()
        for _ in range(6):
            plays = find_action(root, "IGRAJ")
            if plays:
                break
            # Scroll the native Home if the landscape viewport is too short.
            adb("shell", "input", "swipe", w//2, h*80//100, w//2, h*25//100, 380)
            time.sleep(1)
            root = hierarchy()
        assert len(plays) == 1, "FAIL: landscape Home has no accessible Play"
        screenshot("android-home-landscape")
        tap(plays[0])

        # Do not mistake the idle GameView preview for a real flight.
        idle = []
        for _ in range(10):
            root = hierarchy()
            idle = [n for n in root.iter("node")
                    if n.get("clickable") == "true" and "DODIRNI ZA LET" in label(n)]
            if len(idle) == 1:
                break
            time.sleep(1)
        assert len(idle) == 1, "FAIL: landscape GameView did not appear"
        screenshot("android-flight-landscape")
        assert adb("shell", "pidof", APP) == initial_pid, "FAIL: rotation recreated process"

        # A genuine touch begins the run. The debug-only capture gate freezes
        # simulation time after that first flap, preserving real HUD values;
        # the production APK ignores the extra and runs normally.
        tap(idle[0])

        def active_flight():
            for _ in range(10):
                current = hierarchy()
                flights = [n for n in current.iter("node")
                           if n.get("clickable") == "true"
                           and "BODOVI:" in label(n) and "ŽIVOTI:" in label(n)]
                pauses = find_action(current, "PAUZIRAJ IGRU")
                if len(flights) == 1 and len(pauses) == 1:
                    return flights[0], pauses[0]
                time.sleep(1)
            raise AssertionError("FAIL: active flight HUD or accessible Pause is missing")

        def assert_pause_visible(pause, width, height):
            match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                                 pause.get("bounds") or "")
            assert match, "FAIL: Pause has no native bounds"
            left, top, right, bottom = map(int, match.groups())
            assert 0 <= left < right <= width and 0 <= top < bottom <= height, (
                "FAIL: Pause is outside the visible display after rotation")
            assert right-left >= 32 and bottom-top >= 32, (
                "FAIL: Pause touch target is clipped after rotation")

        active, pause = active_flight()
        flight_before = active.get("content-desc")
        assert flight_before and "Bodovi:" in flight_before and "Životi:" in flight_before
        assert_pause_visible(pause, w, h)
        screenshot("android-flight-landscape-active")

        portrait_w, portrait_h = rotate(0)
        active, pause = active_flight()
        assert active.get("content-desc") == flight_before, (
            "FAIL: rotation reset the active score, lives, pilot power or flight state")
        assert_pause_visible(pause, portrait_w, portrait_h)
        assert adb("shell", "pidof", APP) == initial_pid, "FAIL: rotation killed process"
        screenshot("android-flight-portrait-active")

        # The same flight remains tappable and can resume after rotation;
        # no restart, synthetic score or newly created GameView is accepted.
        tap(active)
        assert adb("shell", "pidof", APP) == initial_pid, (
            "FAIL: resuming the rotated flight killed the process")
        print("PASS: landscape/portrait retained genuine active flight, HUD, Pause and process", flush=True)
    finally:
        adb("shell", "cmd", "window", "user-rotation", "free")


if __name__ == "__main__":
    main()
