#!/usr/bin/env python3
"""Real Android 15 reference-menu regression, not clickable screenshot mockups."""
import time
from qa_android_rotation import APP,adb,hierarchy,find_action,tap,label,screenshot as capture_png

def matching_nodes(root, phrase, clickable=None, enabled=None):
    """Case-insensitive native accessibility match with optional real UI flags.

    qa_android_rotation.label() always uppercases node text/description. Keep
    query normalization here as well, including mixed-case Croatian labels.
    """
    needle = phrase.casefold()
    return [n for n in root.iter("node")
            if needle in label(n).casefold()
            and (clickable is None or (n.get("clickable")=="true")==clickable)
            and (enabled is None or (n.get("enabled")=="true")==enabled)]


def map_home_controls(root):
    """Match BOPAVI's native HOME, not Android's system Home navigation."""
    return [n for n in root.iter("node")
            if n.get("package")==APP and n.get("clickable")=="true"
            and n.get("enabled")=="true"
            and (n.get("content-desc") or "").strip().casefold()=="home"]


def unique_map_home():
    """Require one native HOME even after tests have scrolled compact maps.

    UIAutomator excludes offscreen children of native ScrollView. On compact
    1080x2400 maps the level-1 CTA is below the visible fold, and finding it
    scrolls HOME above the fold. Scroll back to the top before concluding
    that the game has lost its HOME action. Two visible APP HOME controls
    always fail immediately.
    """
    root=hierarchy()
    for scan in range(9):
        matches=map_home_controls(root)
        if len(matches)>1:
            break
        if len(matches)==1:
            widget_class=matches[0].get("class") or ""
            assert widget_class,"FAIL: native map HOME has no accessibility class"
            print("QA: unique native BOPAVI HOME:",widget_class,flush=True)
            return matches[0]
        native_scroll=[n for n in root.iter("node")
                       if n.get("package")==APP and
                       n.get("class")=="android.widget.ScrollView"]
        if not native_scroll or scan==8:
            break
        # Swipe finger DOWN to reveal the first row of the actual native
        # ScrollView. Do not simulate an app button or skip any assertions.
        adb("shell","input","swipe","440","320","440","1450","280")
        time.sleep(.25)
        root=hierarchy()
    import xml.etree.ElementTree as ET
    from pathlib import Path
    Path("qa/screenshots/android-map-home-identity-failed.xml").write_bytes(
        ET.tostring(root,encoding="utf-8"))
    candidates=[(n.get("package"),n.get("class"),n.get("text"),
                 n.get("content-desc"),n.get("bounds"))
                for n in root.iter("node") if n.get("clickable")=="true"
                and "home" in label(n).casefold()]
    print("FAIL: HOME candidates:",candidates,flush=True)
    raise AssertionError(
        f"FAIL: expected exactly one app-owned HOME, got {len(matches)}")


def verify_eight_progress_cards():
    """Scroll and inspect all eight live world cards at 941x1672."""
    import re
    worlds=("SKY MEADOWS","CRYSTAL RUINS","VOLCANO PEAKS",
            "NIGHT CASTLES","PEARL SEA","SKY TOWERS","MAGIC FOREST",
            "STAR ABYSS")
    seen=set()
    for scan in range(10):
        root=hierarchy()
        for node in root.iter("node"):
            desc=(node.get("content-desc") or "").casefold()
            if (node.get("package")!=APP or node.get("clickable")!="true"
                    or node.get("enabled")!="true"
                    or "otvori kartu razina" not in desc):
                continue
            bounds=re.fullmatch(
                r"\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]",
                node.get("bounds") or "")
            if not bounds:
                continue
            x1,y1,x2,y2=map(int,bounds.groups())
            if (x2-x1<96 or y2-y1<96 or x2<=0 or x1>=941
                    or y2<=0 or y1>=1672):
                continue
            names=[name for name in worlds if name in label(node)]
            assert len(names)==1,"FAIL: ambiguous Progress card accessible label"
            seen.add(names[0])
        if scan==2:
            w,h=capture_png("android-progress-middle-reference-size")
            assert (w,h)==(941,1672),"FAIL: Progress middle capture distorted"
        if len(seen)==len(worlds):
            break
        adb("shell","input","swipe","470","1340","470","510","320")
        time.sleep(.35)
    assert seen==set(worlds),(
        "FAIL: missing native Progress cards: "+repr(sorted(set(worlds)-seen)))
    w,h=capture_png("android-progress-bottom-reference-size")
    assert (w,h)==(941,1672),"FAIL: Progress bottom capture distorted"
    print("PASS: all eight Progress cards accessible, enabled and >=48dp",
          flush=True)
    for _ in range(9):
        first=[n for n in matching_nodes(hierarchy(),"SKY MEADOWS",clickable=True)
               if n.get("package")==APP and "otvori kartu razina" in
               (n.get("content-desc") or "").casefold()]
        if len(first)==1:
            b=re.fullmatch(r"\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]",
                first[0].get("bounds") or "")
            if b and int(b.group(2))>=0 and int(b.group(4))<=1672:
                return
        adb("shell","input","swipe","470","450","470","1410","250")
        time.sleep(.3)
    raise AssertionError("FAIL: unable to return to first Progress world")



def acknowledge_native_fullscreen_education(root):
    """Dismiss ONLY the exact OS-owned immersive prompt, never an app dialog.

    Shop and Level Map now match the reference's full-bleed screenshots. Some
    fresh Android 15/16 emulators display one-time system education on the
    first menu launch. We still require the real Shop/Map controls afterward.
    """
    system=("android","com.android.systemui")
    nodes=list(root.iter("node"))
    titles=[n for n in nodes if n.get("package") in system and
            n.get("text")=="Viewing full screen"]
    if not titles:
        return False
    assert len(titles)==1,"FAIL: ambiguous native fullscreen dialog"
    package=titles[0].get("package")
    assert len([n for n in nodes if n.get("package")==package and
                n.get("text")=="To exit, swipe down from the top of your screen"])==1, (
                "FAIL: unexpected system fullscreen overlay")
    buttons=[n for n in nodes if n.get("package")==package and
             n.get("text")=="Got it" and
             n.get("class")=="android.widget.Button" and
             n.get("clickable")=="true" and n.get("enabled")=="true"]
    assert len(buttons)==1,"FAIL: unverified immersive education acknowledgment"
    tap(buttons[0])
    print("QA: acknowledged verified Android fullscreen education",flush=True)
    return True


def node_with(phrase,clickable=True,attempts=15):
    for attempt in range(attempts):
        root=hierarchy()
        if acknowledge_native_fullscreen_education(root):
            time.sleep(.6)
            root=hierarchy()
        results=matching_nodes(root,phrase,clickable=True if clickable else None)
        if len(results)==1:
            return results[0]
        if attempt in (4,9):
            adb("shell","input","swipe","540","1450","540","700","380")
        time.sleep(1)
    raise AssertionError(f"FAIL: reference menu control absent or duplicated: {phrase}")


def exists(phrase,enabled=None):
    return matching_nodes(hierarchy(),phrase,enabled=enabled)

def home():
    adb("shell","input","keyevent","4")
    node_with("OTVORI IZBOR LIKOVA")


def open_native_skins():
    """Open the real Skins page after a cold start, never accept a Home label.

    Hosted Android 15 occasionally drops the first tap following repeated
    screen-size/density tests. Retry a native tap ONLY while the verified Home
    control still exists; require the genuine hero gallery and preserve real
    failure diagnostics instead of treating the Home SKINS caption as a pass.
    """
    import xml.etree.ElementTree as ET
    from pathlib import Path
    initial_pid=adb("shell","pidof",APP)
    assert initial_pid,"FAIL: BOPAVI not running before opening Skins"
    # Gather fresh app-process death evidence; no failure is retried away.
    import subprocess
    subprocess.run(["adb","logcat","-c"],capture_output=True,timeout=20)

    def died_or_restarted(current_pid):
        if current_pid==initial_pid:
            return False
        crash=subprocess.run(
            ["adb","logcat","-d","-v","threadtime","-t","2200"],
            text=True,capture_output=True,timeout=25)
        Path("qa/screenshots/android-skins-process-death-logcat.txt").write_text(
            crash.stdout,encoding="utf-8")
        suspects=("FATAL EXCEPTION","AndroidRuntime","OutOfMemoryError",
                  "Fatal signal","lowmemorykiller","com.brendigo.bopavi")
        matching=[line for line in crash.stdout.splitlines()
                  if any(word in line for word in suspects)]
        print("FAIL: BOPAVI process exited/restarted while opening real Skins",
              "initial PID:",initial_pid,"current PID:",repr(current_pid),flush=True)
        for line in matching[-65:]:
            print("ANDROID CRASH:",line,flush=True)
        raise AssertionError("FAIL: Skins app process exited; inspect "
              "android-skins-process-death-logcat.txt")

    def running_pid():
        current=subprocess.run(["adb","shell","pidof",APP],
            text=True,capture_output=True,timeout=20)
        if current.returncode:
            return ""
        return current.stdout.strip()

    def verified_gallery(root):
        titles=[n for n in root.iter("node")
                if n.get("package")==APP
                and n.get("class")=="android.widget.TextView"
                and n.get("text")=="SKINS"]
        royal=[n for n in root.iter("node") if n.get("clickable")=="true"
             and ("CHOOSE ROYAL BOPI" in label(n) or "SELECTED ROYAL BOPI" in label(n))]
        star=[n for n in root.iter("node") if n.get("clickable")=="true"
              and "STAR PILOT" in label(n)]
        return len(titles)==1 and len(royal)==1 and len(star)==1

    taps=0
    # Fresh start may still show the actual animated Loading state.
    # Wait for a verified native Home action; never tap an unknown window.
    for poll in range(16):
        root=hierarchy()
        if acknowledge_native_fullscreen_education(root):
            time.sleep(1)
            root=hierarchy()
        if verified_gallery(root):
            print("PASS: real Skins header, Royal Bopi and Star Pilot cards visible",flush=True)
            return
        died_or_restarted(running_pid())
        buttons=matching_nodes(root,"OTVORI IZBOR LIKOVA",clickable=True)
        if len(buttons)>1:
            raise AssertionError("FAIL: duplicate real Home Skins actions")
        if len(buttons)==1 and taps<4:
            tap(buttons[0])
            taps+=1
            print(f"QA: tapped verified Home Skins action ({taps}/4)",flush=True)
            time.sleep(2)
        else:
            # A real home-to-skins transition, or a still-animating Skins
            # window: re-read the actual device instead of fabricating state.
            time.sleep(1)

    root=hierarchy()
    if not verified_gallery(root):
        Path("qa/screenshots/android-skins-navigation-failed.xml").write_bytes(
            ET.tostring(root,encoding="utf-8"))
        capture_png("android-skins-navigation-failed")
        raise AssertionError("FAIL: four-card Skins gallery not reached after verified Home taps")
    print("PASS: real Skins gallery opened on final attempt",flush=True)

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

        # The separate Progress page is reachable through existing Options,
        # without adding non-reference buttons to the illustrated Home.
        tap(node_with("OTVORI ZVUČNE POSTAVKE"))
        tap(node_with("NAPREDAK"))
        node_with("NAJBOLJI REZULTAT",clickable=False)
        assert exists("SKY MEADOWS"),"FAIL: actual world progress card missing"
        assert exists("Zvjezdice s prvih 20 razina"),(
            "FAIL: the first-20 stars milestone was not shown")
        width,height=capture_png("android-progress-reference-size")
        assert (width,height)==(941,1672),"FAIL: Progress capture is not 941x1672"
        # Verify real on-device layout reflow while the Progress page is open;
        # a stale dp-sized view tree must never survive a display resize.
        adb("shell","wm","size","720x1280")
        time.sleep(1)
        node_with("NAJBOLJI REZULTAT",clickable=False)
        assert exists("SKY MEADOWS"),"FAIL: Progress missing after resize"
        adb("shell","wm","size","941x1672")
        time.sleep(1)
        node_with("NAJBOLJI REZULTAT",clickable=False)
        # Open the real saved world from its native Progress card, rather
        # than taking a screenshot of a decorative unclickable panel.
        verify_eight_progress_cards()
        tap(node_with("SKY MEADOWS",clickable=True))
        node_with("SELECT WORLD 1, SKY MEADOWS",clickable=True)
        node_with("PLAY LEVEL 1, 0 STARS",clickable=True)
        tap(unique_map_home())
        print("PASS: real saved Progress page, world-card navigation, live viewport reflow and 941px capture",
              flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    # Restart only the UI process to restore the emulator's standard grid.
    adb("shell","am","force-stop",APP)
    adb("shell","am","start","-W","-n",APP+"/.MainActivity")

    open_native_skins()
    # Real 941x1672 Android screenshot BEFORE and AFTER changing selected pilot.
    # Native capture helper preserves PNG bytes; no bitmap resizing is allowed.
    resized=False
    try:
        adb("shell","wm","size","941x1672")
        resized=True
        adb("shell","wm","density","320")
        node_with("CHOOSE ROYAL BOPI")
        # The reference gallery has FOUR real card targets, including the
        # saved-coin Orion lock rather than a screenshot-only fourth tile.
        node_with("STAR PILOT",clickable=True)
        width,height=capture_png("android-skins-reference-size")
        assert (width,height)==(941,1672),"FAIL: skins is not 941x1672"
        print("PASS: real native Skins screenshot 941x1672",flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    tap(node_with("CHOOSE ROYAL BOPI"))
    node_with("SELECTED ROYAL BOPI")
    home()
    # A fresh process must restore the REAL saved arcade_pilot, not just a
    # transient selected card inside the current Activity.
    adb("shell","am","force-stop",APP)
    adb("shell","am","start","-W","-n",APP+"/.MainActivity")
    open_native_skins()
    node_with("SELECTED ROYAL BOPI")
    home()
    open_native_skins()
    tap(node_with("CHOOSE CLASSIC AVIATOR"))
    home()
    print("PASS: Skins opens, persists pilot selection and returns Home",flush=True)

    # The hosted emulator may drop one Home tap immediately after restoring
    # the 941px skin-selection viewport. Check the ACTUAL native Shop hierarchy
    # after each tap, and retry only while Home is still visibly present.
    shop_ready=False
    for shop_attempt in range(3):
        tap(node_with("OTVORI TRGOVINU NADOGRADNJI",attempts=6))
        for _ in range(6):
            root=hierarchy()
            if acknowledge_native_fullscreen_education(root):
                time.sleep(.6)
                root=hierarchy()
            has_wallet=bool(matching_nodes(root,"EARNED COINS:"))
            has_inventory=bool(matching_nodes(root,"OWNED:"))
            if has_wallet and has_inventory:
                shop_ready=True
                break
            time.sleep(1)
        if shop_ready:
            break
        root=hierarchy()
        if not matching_nodes(root,"OTVORI TRGOVINU NADOGRADNJI",clickable=True):
            raise AssertionError("FAIL: Shop did not open and Home input is not recoverable")
        print("QA: real Home still visible after Shop tap; retrying native input",flush=True)
    if not shop_ready:
        # Save the real native hierarchy; do not pretend the Shop existed.
        from pathlib import Path
        import xml.etree.ElementTree as ET
        Path("qa/screenshots/android-shop-failed-ui.xml").write_bytes(
            ET.tostring(hierarchy(),encoding="utf-8"))
        capture_png("android-shop-failed")
        raise AssertionError("FAIL: real Shop wallet or product inventory did not appear")
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
        # This resize changes the required Settings COMPOSITION: the
        # original 420dpi scroll view must become the 941px native
        # nine-row reference. A stale scrolled page is not acceptable.
        # Verify the real owner Settings header without scrolling it
        # into or out of view, and preserve evidence when reflow fails.
        import xml.etree.ElementTree as ET
        from pathlib import Path
        for resize_attempt in range(12):
            root=hierarchy()
            if acknowledge_native_fullscreen_education(root):
                time.sleep(.6)
                continue
            actual_title=[n for n in root.iter("node")
                if n.get("package")==APP
                and n.get("class")=="android.widget.TextView"
                and n.get("text")=="SETTINGS"]
            if len(actual_title)==1:
                break
            time.sleep(.5)
        else:
            Path("qa/screenshots/android-settings-resize-failed-ui.xml").write_bytes(
                ET.tostring(root,encoding="utf-8"))
            capture_png("android-settings-resize-failed")
            raise AssertionError(
                "FAIL: Settings was not recomposed after live density/size change")
        print("PASS: Settings retained live native title after 941px/320dpi reflow",
              flush=True)
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
    # The reference exposes only one native HOME action; the rendered
    # back-arrow art is part of that same View, not an offset overlay.
    map_home=unique_map_home()
    assert map_home.get("enabled")=="true","FAIL: map HOME disabled"
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
        # Capture each genuine 941x1672 world page, preserving live controls.
        tap(node_with("MORE WORLDS"))
        node_with("SELECT WORLD 4, NIGHT CASTLES",clickable=True)
        node_with("SELECT WORLD 5, PEARL SEA",clickable=True)
        node_with("SELECT WORLD 6, SKY TOWERS",clickable=True)
        unique_map_home()
        w,h=capture_png("android-level-map-page-two-reference-size")
        assert (w,h)==(941,1672),"FAIL: map page 2 distorted"
        tap(node_with("MORE WORLDS"))
        node_with("SELECT WORLD 7, MAGIC FOREST",clickable=True)
        node_with("SELECT WORLD 8, STAR ABYSS",clickable=True)
        unique_map_home()
        w,h=capture_png("android-level-map-page-three-reference-size")
        assert (w,h)==(941,1672),"FAIL: map page 3 distorted"
        tap(node_with("PREVIOUS WORLDS"))
        tap(node_with("PREVIOUS WORLDS"))
        node_with("SELECT WORLD 1, SKY MEADOWS",clickable=True)
        unique_map_home()
        print("PASS: real 941x1672 Level Map with enabled/locked native controls",flush=True)
    finally:
        if resized:
            adb("shell","wm","size","reset")
            adb("shell","wm","density","reset")
    locked=exists("LOCKED LEVEL 2",enabled=False)
    assert locked,"FAIL: locked level is not truly disabled"
    # Earlier steps deliberately force-stop/relaunch the Activity to verify
    # persisted pilot choice, so the initial launch PID is no longer valid.
    # Check that starting Level 1 does not kill/restart its current process.
    pid_before_level=adb("shell","pidof",APP)
    assert pid_before_level,"FAIL: Level Map process missing before launch"
    tap(node_with("PLAY LEVEL 1, 0 STARS"))
    node_with("DODIRNI ZA ZAMAH KRILIMA")
    assert adb("shell","pidof",APP)==pid_before_level, (
        "FAIL: real level launch killed or restarted the app process")
    print("PASS: actual Level Map locks and finite Level 1 launch",flush=True)

if __name__=="__main__":
    main()
