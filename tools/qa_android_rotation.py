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
    # Freeze only the first *real* flap for deterministic screenshot/score QA.
    # Do not use a debug-only flight-launch shortcut: the owner-visible PLAY
    # must work from landscape and keep the same process/rotation.
    adb("shell", "am", "start", "-W", "-n", APP + "/.MainActivity",
        "--ez", "bopavi_qa_capture_first_flap", "true")
    try:
        initial_pid = adb("shell", "pidof", APP)
        assert initial_pid, "FAIL: missing BOPAVI process"
        w, h = rotate(1)
        assert w > h, "FAIL: landscape screenshot not wider than tall"
        root = hierarchy()
        screenshot("android-home-landscape")
        for _ in range(10):
            root=hierarchy()
            buttons=[n for n in root.iter("node")
                     if n.get("package")==APP and n.get("clickable")=="true"
                     and n.get("enabled")=="true"]
            descriptions={n.get("content-desc") for n in buttons}
            expected={"Pokreni igru","Otvori izbor likova",
                      "Otvori trgovinu nadogradnji", "Otvori opcije igre"}
            if len(buttons)==4 and descriptions==expected:break
            adb("shell","input","swipe",w//2,h*80//100,w//2,h*25//100,380)
            time.sleep(.5)
        assert len(buttons)==4 and descriptions==expected, (
            "FAIL: rotated Home must have Play, Shop, Skins and Settings")
        # The failed 2026-11-14 Android15 run restarted the Activity after
        # locking landscape, which yielded a PORTRAIT 1080x2400 GameView
        # despite retaining earlier 2400x1080 landscape dimensions.
        # Tap the real native PLAY while the existing landscape window lives;
        # this directly tests the production navigation path as well.
        play=[n for n in buttons if n.get("content-desc")=="Pokreni igru"]
        assert len(play)==1, "FAIL: exactly one landscape PLAY action expected"
        tap(play[0])
        assert adb("shell","pidof",APP)==initial_pid, (
            "FAIL: PLAY launch restarted the landscape process")
        # Do not mistake the idle GameView preview for a real flight.
        idle = []
        for _ in range(10):
            root = hierarchy()
            idle = [n for n in root.iter("node")
                    if n.get("clickable") == "true"
                    and "ŽIVOTI:" in label(n)
                    and "DODIRNI ZA ZAMAH KRILIMA" in label(n)]
            if len(idle) == 1:
                break
            time.sleep(1)
        assert len(idle) == 1, "FAIL: landscape GameView did not appear"
        native_w,native_h=screenshot("android-flight-landscape")
        assert (native_w,native_h)==(w,h) and native_w>native_h, (
            "FAIL: gameplay switched orientation after landscape PLAY",
            {"expected":(w,h),"actual":(native_w,native_h)})
        assert adb("shell", "pidof", APP) == initial_pid, (
            "FAIL: landscape PLAY recreated the process")

        # A genuine touch begins the run. The debug-only capture gate freezes
        # simulation time after that first flap, preserving real HUD values;
        # the production APK ignores the extra and runs normally.
        tap(idle[0])

        def active_flight():
            for _ in range(10):
                current = hierarchy()
                flights = [n for n in current.iter("node")
                           if n.get("clickable") == "true"
                           and all(word in label(n).upper() for word in
                                   ("REZULTAT:","NOVČIĆI:","KRISTALI:","GORE DESNO PAUZA"))]
                pauses = find_action(current, "PAUZIRAJ IGRU")
                if len(flights) == 1 and not pauses:
                    return flights[0]
                time.sleep(1)
            raise AssertionError("FAIL: active SCORE HUD missing or Pause overlay returned")

        def assert_flight_visible(flight, width, height):
            match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                                 flight.get("bounds") or "")
            assert match, "FAIL: active flight has no native bounds"
            left, top, right, bottom = map(int, match.groups())
            assert 0 <= left < right <= width and 0 <= top < bottom <= height, (
                "FAIL: flight touch target is clipped after rotation",
                {"bounds":(left,top,right,bottom),"frame":(width,height),
                 "native_label":label(flight)[:140]})

        # UIAutomator must refer to the exact same landscape frame that
        # Screencap measured; never reuse dimensions from an earlier Home.
        measured_w,measured_h=screenshot("android-flight-landscape-active")
        assert (measured_w,measured_h)==(w,h), (
            "FAIL: active GameView unexpectedly rotated before capture",
            {"expected":(w,h),"actual":(measured_w,measured_h)})
        active = active_flight()
        flight_before = active.get("content-desc")
        assert flight_before and "Rezultat:" in flight_before
        assert_flight_visible(active, measured_w, measured_h)

        portrait_w, portrait_h = rotate(0)
        active = active_flight()
        assert active.get("content-desc") == flight_before, (
            "FAIL: rotation reset current score, active pilot or GameView state")
        assert_flight_visible(active, portrait_w, portrait_h)
        assert adb("shell", "pidof", APP) == initial_pid, "FAIL: rotation killed process"
        screenshot("android-flight-portrait-active")

        # The same flight remains tappable and can resume after rotation;
        # no restart, synthetic score or newly created GameView is accepted.
        tap(active)
        assert adb("shell", "pidof", APP) == initial_pid, (
            "FAIL: resuming the rotated flight killed the process")
        print("PASS: landscape/portrait retained genuine active flight, SCORE HUD and process", flush=True)
    finally:
        adb("shell", "cmd", "window", "user-rotation", "free")


if __name__ == "__main__":
    main()
