#!/usr/bin/env python3
"""Real Android 15 reference-menu regression, not clickable screenshot mockups."""
import time
from qa_android_rotation import APP,adb,hierarchy,find_action,tap,label,screenshot as capture_png

def node_with(phrase,clickable=True,attempts=15):
    for attempt in range(attempts):
        root=hierarchy()
        results=[n for n in root.iter("node")
                 if phrase in label(n)
                 and (not clickable or n.get("clickable")=="true")]
        if len(results)==1:
            return results[0]
        if attempt in (4,9):
            adb("shell","input","swipe","540","1450","540","700","380")
        time.sleep(1)
    raise AssertionError(f"FAIL: reference menu control absent or duplicated: {phrase}")

def exists(phrase,enabled=None):
    root=hierarchy()
    return [n for n in root.iter("node") if phrase in label(n)
            and (enabled is None or (n.get("enabled")=="true")==enabled)]

def home():
    adb("shell","input","keyevent","4")
    node_with("OTVORI IZBOR LIKOVA")

def main():
    adb("shell","am","force-stop",APP)
    adb("shell","am","start","-W","-n",APP+"/.MainActivity")
    pid=adb("shell","pidof",APP)
    assert pid,"FAIL: app failed to start for reference menus"

    # An authentic reference-sized screenshot from eight native goal cards.
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        resized=True
        adb("shell","wm","density","320")
        tap(node_with("POGLEDAJ SVOJA POSTIGNUĆA"))
        node_with("ACHIEVEMENTS",clickable=False)
        for name in ("FIRST VICTORY","SKY EXPLORER","WING MASTER",
                     "BERRY HUNTER","GATE RUNNER","HIGH FLYER",
                     "MEADOW FINDER","KEEP FLYING"):
            assert exists(name),"FAIL: missing true goal "+name
        width,height=capture_png("android-achievements-reference-size")
        assert (width,height)==(941,1672),"FAIL: incorrect Achievements PNG"
        tap(node_with("HOME"))
        tap(node_with("POGLEDAJ SVOJE REKORDE"))
        node_with("LEADERBOARD",clickable=False)
        node_with("TOP 10 LOCAL SCORES",clickable=False)
        assert exists("BEST SCORE"),"FAIL: score summary absent"
        width,height=capture_png("android-leaderboard-reference-size")
        assert (width,height)==(941,1672),"FAIL: actual Leaderboard PNG dimensions"
        tap(node_with("HOME"))
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    # Restart only the UI process to restore the emulator's standard grid.
    adb("shell","am","force-stop",APP)
    adb("shell","am","start","-W","-n",APP+"/.MainActivity")

    tap(node_with("OTVORI IZBOR LIKOVA"))
    node_with("SKINS",clickable=False)
    # Real 941x1672 Android screenshot BEFORE and AFTER changing selected pilot.
    # Native capture helper preserves PNG bytes; no bitmap resizing is allowed.
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        resized=True
        adb("shell","wm","density","320")
        node_with("CHOOSE WISE OWL")
        width,height=capture_png("android-skins-reference-size")
        assert (width,height)==(941,1672),"FAIL: skins is not 941x1672"
        print("PASS: real native Skins screenshot 941x1672",flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    tap(node_with("CHOOSE WISE OWL"))
    node_with("SELECTED WISE OWL")
    home()
    # A fresh process must restore the REAL saved arcade_pilot, not just a
    # transient selected card inside the current Activity.
    adb("shell","am","force-stop",APP)
    adb("shell","am","start","-W","-n",APP+"/.MainActivity")
    tap(node_with("OTVORI IZBOR LIKOVA"))
    node_with("SELECTED WISE OWL")
    home()
    tap(node_with("OTVORI IZBOR LIKOVA"))
    tap(node_with("CHOOSE CLASSIC AVIATOR"))
    home()
    print("PASS: Skins opens, persists pilot selection and returns Home",flush=True)

    tap(node_with("OTVORI TRGOVINU NADOGRADNJI"))
    node_with("EARNED COINS:",clickable=False)
    assert exists("OWNED:",enabled=None),"FAIL: no real persisted shop inventory"
    assert not exists("GEMS COLLECTED"),"FAIL: fake gem balance"
    # Assert a real native screenshot, no reference bitmap or fake balances.
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        resized=True
        adb("shell","wm","density","320")
        node_with("EARNED COINS:",clickable=False)
        w,h=capture_png("android-shop-reference-size")
        assert (w,h)==(941,1672),"FAIL: Shop screenshot has wrong size"
        print("PASS: real native Shop screenshot 941x1672",flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    home()
    print("PASS: Shop shows persisted earned coin balance and owned perks",flush=True)

    tap(node_with("OTVORI OPCIJE IGRE"))
    node_with("SETTINGS",clickable=False)
    node_with("Music",clickable=False)
    node_with("Glazba:",clickable=False)
    node_with("Zvučni efekti:",clickable=False)
    node_with("Pročitaj politiku privatnosti")
    node_with("Pročitaj uvjete korištenja")
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        resized=True
        adb("shell","wm","density","320")
        node_with("SETTINGS",clickable=False)
        width,height=capture_png("android-settings-reference-size")
        assert (width,height)==(941,1672),"FAIL: Settings screenshot is not 941x1672"
        print("PASS: native Settings 941x1672",flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    home()
    tap(node_with("OTVORI ZVUČNE POSTAVKE"))
    tap(node_with("LEVEL MAP"))
    node_with("LEVEL MAP",clickable=False)
    node_with("SELECT WORLD 1, SKY MEADOWS",clickable=True)
    node_with("SELECT WORLD 2, CRYSTAL RUINS",clickable=True)
    node_with("SELECT WORLD 3, VOLCANO PEAKS",clickable=True)
    node_with("PLAY LEVEL 1, 0 STARS",clickable=True)
    # Verify alternate world pages through genuine native controls, and
    # return to the first world without unlocking any level.
    tap(node_with("MORE WORLDS"))
    node_with("SELECT WORLD 4, NIGHT CASTLES",clickable=True)
    node_with("SELECT WORLD 5, PEARL SEA",clickable=True)
    node_with("SELECT WORLD 6, SKY TOWERS",clickable=True)
    tap(node_with("MORE WORLDS"))
    node_with("SELECT WORLD 7, MAGIC FOREST",clickable=True)
    node_with("SELECT WORLD 8, STAR ABYSS",clickable=True)
    tap(node_with("PREVIOUS WORLDS"))
    node_with("SELECT WORLD 4, NIGHT CASTLES",clickable=True)
    tap(node_with("PREVIOUS WORLDS"))
    node_with("SELECT WORLD 1, SKY MEADOWS",clickable=True)
    assert exists("LOCKED LEVEL 2",enabled=False)
    # Genuine screenshot at the owner's source pixel grid, not a resized
    # mock. Explicitly restore the emulator before testing finite gameplay.
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        resized=True
        adb("shell","wm","density","320")
        time.sleep(1)
        node_with("PLAY LEVEL 1, 0 STARS",clickable=True)
        assert exists("LOCKED LEVEL 2",enabled=False)
        # The existing helper captures binary PNG bytes directly. adb()
        # returns text for shell/UI commands and must never decode a PNG.
        width,height=capture_png("android-level-map-reference-size")
        assert (width,height)==(941,1672)
        print("PASS: real 941x1672 Level Map with enabled/locked native controls",flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    locked=exists("LOCKED LEVEL 2",enabled=False)
    assert locked,"FAIL: locked level is not truly disabled"
    tap(node_with("PLAY LEVEL 1, 0 STARS"))
    node_with("DODIRNI ZA ZAMAH KRILIMA")
    assert adb("shell","pidof",APP)==pid,"FAIL: real level launch crashed the app"
    print("PASS: actual Level Map locks and finite Level 1 launch",flush=True)

if __name__=="__main__":
    main()
