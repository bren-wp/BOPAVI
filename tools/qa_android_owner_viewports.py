#!/usr/bin/env python3
"""Capture genuine BOPAVI Home, Skins, Shop and Settings at all owner viewports.

This is a navigation/accessibility/screen-size gate, NOT a declaration that
its pixels are identical to the static mockups. Never resize screenshots or
replace Android controls with WebP artwork.
"""
from __future__ import annotations

import re
import time
from pathlib import Path
from xml.etree import ElementTree as ET

from qa_android_rotation import APP, OUT, adb, hierarchy, screenshot
from qa_android_reference_menus import (
    acknowledge_native_fullscreen_education, home, open_route,
)

# Exact physical sizes in BOPAVI_Svi_Zasloni_i_Gameplay_Android(2).zip.
# Pixel densities are representative emulator fixtures, not inferred DPIs
# from the static source images.
VIEWPORTS = (
    ("Android_16_9_1080x1920", 1080, 1920, 420),
    ("Android_FHD_1080x2280", 1080, 2280, 420),
    ("Android_FHD_Plus_1080x2340", 1080, 2340, 420),
    ("Android_20_9_1080x2400", 1080, 2400, 420),
    ("Android_QHD_1440x3040", 1440, 3040, 560),
)
DESTINATIONS = (
    ("Skins", "07_Skins"),
    ("Shop", "08_Shop"),
    ("Settings", "09_Settings"),
)
RECT = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")

def wait_home(width: int, height: int, density: int):
    for attempt in range(18):
        root = hierarchy()
        if acknowledge_native_fullscreen_education(root):
            time.sleep(.6)
            continue
        try:
            native = home()
        except AssertionError:
            time.sleep(.6)
            continue
        # Every native CTA must be fully visible, physically >=48dp,
        # clickable and not behind a system bar or cutout.
        minimum = 48*density/160
        for node in native:
            rect = RECT.fullmatch(node.get("bounds", ""))
            assert rect, "Native Home button lacks real touch bounds"
            left,top,right,bottom=map(int,rect.groups())
            assert (0 <= left < right <= width and
                    0 <= top < bottom <= height), (
                "Home button clipped by viewport",node.attrib)
            assert min(right-left,bottom-top)>=minimum-1, (
                "Home control smaller than 48dp",node.attrib,minimum)
        return native
    raise AssertionError("Home with three accessible native actions unavailable")

def capture(name: str, width: int, height: int):
    actual=screenshot(name)
    assert actual==(width,height),(name,actual,(width,height))
    assert (OUT/(name+".png")).stat().st_size>2000
    root=hierarchy()
    (OUT/(name+".xml")).write_bytes(ET.tostring(root,encoding="utf-8"))
    print("PASS: real Android "+name+f" {width}x{height}",flush=True)

def main():
    try:
        for folder,width,height,density in VIEWPORTS:
            adb("shell","wm","size",f"{width}x{height}")
            adb("shell","wm","density",str(density))
            adb("shell","am","force-stop",APP)
            adb("shell","am","start","-W","-n",APP+"/.MainActivity")
            wait_home(width,height,density)
            capture(f"owner-{folder}-01_Pocetni_zaslon",width,height)
            for route,stem in DESTINATIONS:
                open_route(route)
                capture(f"owner-{folder}-{stem}",width,height)
                adb("shell","input","keyevent","4")
                wait_home(width,height,density)
            print("PASS: "+folder+" all 3 production routes and 4 native frames",flush=True)
        print("PASS: 20 authentic native screenshots, 5 owner viewports, "
              "three Home destinations; pixel equivalence not asserted",flush=True)
    finally:
        # Never leave the developer's Android emulator at a forged density.
        adb("shell","wm","size","reset")
        adb("shell","wm","density","reset")
        adb("shell","am","force-stop",APP)
        adb("shell","am","start","-W","-n",APP+"/.MainActivity")

if __name__=="__main__":
    main()
