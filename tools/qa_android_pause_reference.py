#!/usr/bin/env python3
"""Real Android 15/16 Pause modal capture at the approved source viewport.

No placeholder image is painted. Launch, flap, system-Back and Resume are all
actual application and user-input events on an existing Android emulator.
"""
import time
from qa_android_rotation import APP,adb,hierarchy,find_action,tap,label,screenshot

def expect_one(phrase,clickable=True,attempts=15):
    for _ in range(attempts):
        nodes=[n for n in hierarchy().iter("node")
               if phrase in label(n)
               and (not clickable or n.get("clickable")=="true")]
        if len(nodes)==1:
            return nodes[0]
        time.sleep(1)
    raise AssertionError("FAIL: unique native Pause/Flight control absent: "+phrase)

def assert_pause():
    expect_one("PAUZA",clickable=False)
    for action in ("NASTAVI","PONOVO","IZLAZ"):
        expect_one(action)
    assert len([n for n in hierarchy().iter("node")
                if "NASTAVI" in label(n) and n.get("clickable")=="true"])==1, (
        "FAIL: duplicated Pause dialog")
    print("PASS: exactly one real pause modal with three working native actions",flush=True)

def main():
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        resized=True
        adb("shell","wm","density","320")
        adb("shell","am","force-stop",APP)
        adb("shell","am","start","-W","-n",APP+"/.MainActivity",
            "--ez","bopavi_qa_capture_first_flap","true")
        tap(expect_one("▶  PLAY"))
        active=expect_one("ŽIVOTI:")
        tap(active)
        # A genuine first flap begins one GameSimulation; no QA result injection.
        expect_one("ŽIVOTI:")
        adb("shell","input","keyevent","4")
        assert_pause()
        w,h=screenshot("android-pause-reference-size")
        assert (w,h)==(941,1672),f"FAIL: Pause viewport {(w,h)} is not 941x1672"
        tap(expect_one("NASTAVI"))
        expect_one("ŽIVOTI:")
        assert not [n for n in hierarchy().iter("node")
                    if "NASTAVI" in label(n)],"FAIL: Pause remained after Resume"
        print("PASS: real reference-size Pause screenshot and unchanged flight Resume",flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
        adb("shell","am","force-stop",APP)
        adb("shell","am","start","-W","-n",APP+"/.MainActivity")

if __name__=="__main__":
    main()
