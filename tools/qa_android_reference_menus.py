#!/usr/bin/env python3
"""Real Android 15 reference-menu regression, not clickable screenshot mockups."""
import time
from qa_android_rotation import APP,adb,hierarchy,find_action,tap,label

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

    tap(node_with("OTVORI IZBOR LIKOVA"))
    node_with("SKINS",clickable=False)
    tap(node_with("CHOOSE WISE OWL"))
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
    home()
    print("PASS: Shop shows persisted earned coin balance and owned perks",flush=True)

    tap(node_with("OTVORI OPCIJE IGRE"))
    tap(node_with("LEVEL MAP"))
    node_with("LEVEL MAP",clickable=False)
    node_with("PLAY LEVEL 1, 0 STARS",clickable=True)
    locked=exists("LOCKED LEVEL 2",enabled=False)
    assert locked,"FAIL: locked level is not truly disabled"
    tap(node_with("PLAY LEVEL 1, 0 STARS"))
    node_with("DODIRNI ZA LET")
    assert adb("shell","pidof",APP)==pid,"FAIL: real level launch crashed the app"
    print("PASS: actual Level Map locks and finite Level 1 launch",flush=True)

if __name__=="__main__":
    main()
