#!/usr/bin/env python3
"""Capture the *real native* reference Loading view on API35, not a mock image.

The debug-only activity extra holds the same view used for actual background
level generation so that CI can inspect it without timing a short real load.
"""
from pathlib import Path
import subprocess
import time
import xml.etree.ElementTree as ET

APP = "com.brendigo.bopavi"
OUT = Path("qa/screenshots/android-loading.png")
PNG_HEADER = bytes.fromhex("89504e470d0a1a0a")

def adb(*args):
    return subprocess.run(["adb", *args], check=True, capture_output=True,
                          timeout=45).stdout

def hierarchy():
    for attempt in range(6):
        try:
            adb("shell", "uiautomator", "dump", "/sdcard/bopavi-loading-ui.xml")
            root = ET.fromstring(adb("exec-out", "cat", "/sdcard/bopavi-loading-ui.xml"))
            if root.tag == "hierarchy" and root.findall(".//node"):
                return root
        except (ET.ParseError, subprocess.SubprocessError):
            pass
        time.sleep(1)
    raise AssertionError("FAIL: Loading has no native Android UI hierarchy")

def main():
    try:
        adb("shell", "am", "force-stop", APP)
        adb("shell", "am", "start", "-W", "-n", APP + "/.MainActivity",
            "--ez", "bopavi_qa_loading_preview", "true")
        assert adb("shell", "pidof", APP).strip(), "FAIL: Loading crashed Android process"
        nodes=list(hierarchy().iter("node"))
        view=[n for n in nodes if n.get("content-desc") == "Loading level"]
        assert len(view)==1, "FAIL: missing one native semantic Loading view"
        assert view[0].get("clickable")=="false", "FAIL: decorative Loading fake button"
        image=adb("exec-out", "screencap", "-p")
        assert image.startswith(PNG_HEADER) and len(image)>2000, "FAIL: Loading PNG invalid"
        OUT.parent.mkdir(parents=True,exist_ok=True)
        OUT.write_bytes(image)
        print("PASS: native Loading reference view is accessible and captured",flush=True)
    finally:
        # The preview is not a navigable game route and must never strand QA.
        adb("shell", "am", "force-stop", APP)
        adb("shell", "am", "start", "-W", "-n", APP + "/.MainActivity")
    home=hierarchy()
    assert any(n.get("text","")=="PLAY" and n.get("clickable")=="true"
               for n in home.iter("node")), "FAIL: normal Home lost after Loading QA"
    print("PASS: normal application launch remains Home, not a second startup splash",flush=True)

if __name__=="__main__":
    main()
