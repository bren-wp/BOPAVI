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
    return matching_nodes(hierarchy(),phrase,clickable=clickable)

# Native UIAutomation helpers are shared by the focused Shop/Skins
# emulator tests. Match real app-owned Android nodes, never screenshots.
def matching_nodes(root, phrase, clickable=None):
    """Find enabled BOPAVI nodes by spoken or visible label in one hierarchy."""
    needle=phrase.casefold()
    return [
        n for n in root.iter("node")
        if n.get("package")==APP
        and n.get("enabled","true")=="true"
        and needle in ((n.get("text") or "")+" "+(n.get("content-desc") or "")).casefold()
        and (clickable is None or (n.get("clickable")=="true")==clickable)
    ]


def acknowledge_native_fullscreen_education(root):
    """Dismiss ONLY a verified Android-owned first-use immersive tutorial."""
    nodes=list(root.iter("node"))
    titles=[n for n in nodes
            if n.get("package") in ("android","com.android.systemui")
            and n.get("text")=="Viewing full screen"]
    if len(titles)!=1:
        return False
    system_owner=titles[0].get("package")
    if not any(n.get("package")==system_owner and
               "To exit, swipe down from the top of your screen"
               in (n.get("text") or "") for n in nodes):
        return False
    buttons=[n for n in nodes if n.get("package")==system_owner
             and n.get("text")=="Got it"
             and n.get("clickable")=="true"
             and n.get("enabled","true")=="true"]
    if len(buttons)!=1:
        return False
    tap(buttons[0])
    return True


def node_with(phrase, clickable=True, attempts=10):
    """Require one unique real, enabled native control; never fabricate a tap."""
    for attempt in range(attempts):
        root=hierarchy()
        if acknowledge_native_fullscreen_education(root):
            time.sleep(.5)
            continue
        hits=matching_nodes(root,phrase,clickable=clickable)
        if len(hits)>1:
            raise AssertionError(
                f"Duplicate native action {phrase!r}: {len(hits)} matches")
        if len(hits)==1:
            return hits[0]
        time.sleep(.5)
    raise AssertionError(f"Missing native BOPAVI action {phrase!r}")


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
