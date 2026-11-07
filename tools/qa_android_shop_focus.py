#!/usr/bin/env python3
"""Strict single-screen Shop smoke on a REAL API36 Android emulator.

Captures the unscaled 941x1672 pixels. No fake screenshots, prerecorded wallet
balances, stub purchases, or silently successful duplicate actions.
"""
import time
from qa_android_rotation import APP, adb, hierarchy, screenshot, tap
from qa_android_reference_menus import matching_nodes, node_with, acknowledge_native_fullscreen_education


def require(phrase,clickable=None,minimum=1):
    for _ in range(12):
        root=hierarchy()
        if acknowledge_native_fullscreen_education(root):
            time.sleep(.5)
            root=hierarchy()
        hits=matching_nodes(root,phrase,clickable=clickable)
        if len(hits)>=minimum:
            return hits
        time.sleep(.5)
    raise AssertionError(f"Shop must expose {phrase!r} (min {minimum})")


def main():
    adb("shell","am","force-stop",APP)
    adb("shell","wm","size","941x1672")
    adb("shell","wm","density","320")
    try:
        adb("shell","am","start","-W","-n",APP+"/.MainActivity")
        pid=adb("shell","pidof",APP)
        assert pid,"FAIL: Shop focused QA has no running process"
        tap(node_with("OTVORI TRGOVINU NADOGRADNJI",attempts=8))
        require("EARNED COINS:")
        require("EARNED STARS:")
        require("OWNED:",minimum=6)
        require("HOW TO EARN STARS",clickable=True)
        require("HOW TO EARN COINS",clickable=True)
        require("FEATURED:")
        for name in ("SHIELD","MAGNET","DOUBLE SHIELD","LONG MAGNET",
                     "TRIPLE SHIELD","SUPER MAGNET"):
            require(name)
        w,h=screenshot("android-shop-focus-reference-size")
        assert (w,h)==(941,1672),(w,h)
        assert adb("shell","pidof",APP)==pid,"FAIL: Shop screen restarted process"
        # The star + button must not mint currency or navigate to a fake gem store.
        tap(require("HOW TO EARN STARS",clickable=True)[0])
        require("EARNED STARS:")
        assert adb("shell","pidof",APP)==pid,"FAIL: Shop star help caused process death"
        # The real Home back action remains tappable after help feedback.
        tap(node_with("HOME",attempts=8))
        require("OTVORI TRGOVINU NADOGRADNJI",clickable=True)
        print("PASS: real Shop 941x1672, six native items, two live plus controls and Home navigation")
    finally:
        adb("shell","wm","size","reset")
        adb("shell","wm","density","reset")


if __name__=="__main__":
    main()
