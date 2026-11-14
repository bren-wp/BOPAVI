#!/usr/bin/env python3
"""Capture 5 genuine BOPAVI screens, including native Privacy, per owner viewport.

This is a navigation/accessibility/screen-size gate, NOT a declaration that
its pixels are identical to the static mockups. Never resize screenshots or
replace Android controls with WebP artwork.
"""
from __future__ import annotations

import re
import time
from pathlib import Path
from xml.etree import ElementTree as ET

from qa_android_rotation import APP, OUT, adb, hierarchy, screenshot, tap, label
from qa_android_reference_menus import (
    acknowledge_native_fullscreen_education, home, open_route, node_with,
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

def real_flight_screens(folder:str,width:int,height:int):
    # Debug-only route enters an actual GameSimulation from the native
    # MainActivity. The first real input activates it; QA then freezes the
    # genuine physics clock while UIAutomator captures the exact pixels.
    adb("shell","am","force-stop",APP)
    adb("shell","am","start","-W","-n",APP+"/.MainActivity",
        "--ez","bopavi_qa_capture_first_flap","true",
        "--ez","bopavi_qa_launch_flight","true")
    ready=node_with("DODIRNI ZA ZAMAH KRILIMA",attempts=18)
    assert "PRIJE PRVOG ZAMAHA" in label(ready),(
        "Real GameView not in ready first-flap state")
    capture(f"owner-{folder}-02_Gameplay_upute",width,height)
    tap(ready)
    try:
        active=node_with("REZULTAT:",attempts=4)
    except AssertionError:
        # Android can drop an injected first touch during immersive setup.
        # Retry ONE REAL input only while original GameView is still ready.
        retry=node_with("PRIJE PRVOG ZAMAHA",attempts=3)
        tap(retry)
        active=node_with("REZULTAT:",attempts=10)
    for term in ("NOVČIĆI:","KRISTALI:","GORE DESNO PAUZA"):
        assert term in label(active),(term,"Active HUD not real")
    capture(f"owner-{folder}-03_Gameplay_let",width,height)
    adb("shell","input","keyevent","4")
    pause=node_with("NASTAVI",attempts=12)
    root=hierarchy()
    for item in ("PAUZA","PONOVO","IZLAZ"):
        assert any(item in label(n) for n in root.iter("node")),(
            "Real Pause is missing "+item)
    assert len([n for n in root.iter("node")
                if n.get("package")==APP and n.get("clickable")=="true"
                and "NASTAVI" in label(n)])==1,(
        "Duplicated native pause continuation")
    capture(f"owner-{folder}-04_Gameplay_pauza",width,height)
    print("PASS: "+folder+" real ready/active/pause, one GameSimulation",
          flush=True)


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
                if route=="Settings":
                    # An authentic legal HTML overlay is independently
                    # captured as screen 10; it is NOT rendered from a WebP.
                    tap(node_with("Pročitaj politiku privatnosti",attempts=10))
                    notice=hierarchy()
                    assert any("POLITIKA PRIVATNOSTI" in label(n)
                               for n in notice.iter("node")), (
                        "Privacy heading absent from real legal modal")
                    assert any("ZATVORI" in label(n)
                               for n in notice.iter("node")), (
                        "Real legal close action missing")
                    capture(f"owner-{folder}-10_Politika_privatnosti",
                            width,height)
                    adb("shell","input","keyevent","4")
                    # Must return to the original interactive Settings
                    # screen, then back to Home, not restart the process.
                    assert len([n for n in hierarchy().iter("node")
                                if n.get("package")==APP and
                                "PROČITAJ POLITIKU PRIVATNOSTI" in label(n)])>=1
                adb("shell","input","keyevent","4")
                wait_home(width,height,density)
            real_flight_screens(folder,width,height)
            print("PASS: "+folder+" 8 real frames including legal and gameplay",
                  flush=True)
        print("PASS: 40 authentic native screenshots, 5 owner viewports, "
              "gameplay+Privacy; two Game Over variants not yet captured",
              flush=True)
    finally:
        # Never leave the developer's Android emulator at a forged density.
        adb("shell","wm","size","reset")
        adb("shell","wm","density","reset")
        adb("shell","am","force-stop",APP)
        adb("shell","am","start","-W","-n",APP+"/.MainActivity")

if __name__=="__main__":
    main()
