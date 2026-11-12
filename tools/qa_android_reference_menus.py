#!/usr/bin/env python3
"""Interactive Android 15/16 smoke for the three remaining Home destinations."""
import time
from qa_android_rotation import APP, adb, hierarchy, label, tap, screenshot

ACTIONS={
    "Skins":"OTVORI IZBOR LIKOVA",
    "Shop":"OTVORI TRGOVINU NADOGRADNJI",
    "Settings":"OTVORI OPCIJE IGRE",
}

def matches(phrase, clickable=None):
    found=[]
    for n in hierarchy().iter("node"):
        if n.get("package")!=APP or phrase.upper() not in label(n):
            continue
        if clickable is not None and (n.get("clickable")=="true")!=clickable:
            continue
        found.append(n)
    return found

def home():
    actual=[n for n in hierarchy().iter("node")
            if n.get("package")==APP and n.get("clickable")=="true"
            and n.get("enabled")=="true"]
    descriptions={n.get("content-desc") for n in actual}
    assert len(actual)==3 and descriptions=={
        "Otvori izbor likova","Otvori trgovinu nadogradnji","Otvori opcije igre"
    }, f"FAIL: Home controls are not exactly Shop, Skins and Settings: {descriptions}"
    return actual

def open_route(name):
    needle=ACTIONS[name]
    for _ in range(10):
        home()
        targets=matches(needle,clickable=True)
        if len(targets)==1:
            tap(targets[0])
            for _ in range(10):
                time.sleep(.5)
                root=hierarchy()
                labels=[label(n) for n in root.iter("node")
                        if n.get("package")==APP]
                if name=="Skins" and any("CHOOSE ROYAL BOPI" in x for x in labels):
                    return
                if name=="Shop" and any("EARNED COINS:" in x for x in labels):
                    return
                if name=="Settings" and any("MUSIC" in x for x in labels):
                    return
            raise AssertionError("FAIL: tapped Home but real "+name+" never appeared")
        time.sleep(.5)
    raise AssertionError("FAIL: Home "+name+" action missing")

def main():
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        adb("shell","wm","density","320")
        resized=True
        adb("shell","am","force-stop",APP)
        adb("shell","am","start","-W","-n",APP+"/.MainActivity")
        for _ in range(12):
            try:home();break
            except AssertionError:time.sleep(1)
        home()
        assert screenshot("android-home-reference-size")==(941,1672)
        for name,capture in (
            ("Skins","android-skins-reference-size"),
            ("Shop","android-shop-reference-size"),
            ("Settings","android-settings-reference-size"),
        ):
            open_route(name)
            assert screenshot(capture)==(941,1672), "FAIL: "+name+" capture distorted"
            adb("shell","input","keyevent","4")
            for _ in range(10):
                time.sleep(.5)
                try:home();break
                except AssertionError:continue
            home()
            print("PASS: native "+name+" returns to three-action Home",flush=True)
        print("PASS: exactly three Home buttons; real Shop, Skins and Settings captures")
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
        adb("shell","am","force-stop",APP)
        adb("shell","am","start","-W","-n",APP+"/.MainActivity")

if __name__=="__main__":
    main()
