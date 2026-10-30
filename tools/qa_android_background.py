#!/usr/bin/env python3
"""Real Android background/foreground QA for the lives-only flight.

An active run must survive Home -> launcher return, with a single native
Pause dialog. The GameView must retain its real lives and first-flap state.
This is a debug-only deterministic capture, never a synthetic gameplay score.
"""
import time

from qa_android_rotation import APP, adb, hierarchy, find_action, label, tap


def one_control(phrase, attempts=16):
    for _ in range(attempts):
        root = hierarchy()
        hits = find_action(root, phrase)
        if len(hits) == 1:
            return hits[0]
        time.sleep(1)
    raise AssertionError(f"FAIL: expected exactly one accessible {phrase}")


def active_flight():
    for _ in range(15):
        root = hierarchy()
        games = [n for n in root.iter("node")
                 if n.get("clickable") == "true" and "ŽIVOTI:" in label(n)
                 and "BODOVI:" not in label(n) and "MOĆ:" not in label(n)]
        if len(games) == 1:
            return games[0]
        time.sleep(1)
    raise AssertionError("FAIL: live flight with lives-only accessibility missing")


def main():
    adb("shell", "am", "force-stop", APP)
    adb("shell", "am", "start", "-W", "-a", "android.intent.action.MAIN",
        "-c", "android.intent.category.LAUNCHER",
        "-n", APP + "/.MainActivity",
        "--ez", "bopavi_qa_capture_first_flap", "true")
    pid = adb("shell", "pidof", APP)
    assert pid, "FAIL: app did not start"
    tap(one_control("PLAY"))
    tap(one_control("DODIRNI ZA LET"))
    before = active_flight().get("content-desc")
    assert before and "Životi:" in before, "FAIL: no true initial live lives"

    # System Home puts the active Activity in the background without force-stop.
    adb("shell", "input", "keyevent", "3")
    time.sleep(2)
    assert adb("shell", "pidof", APP) == pid, "FAIL: process died on Home"

    # Real launcher Intent must bring back the SAME existing task/Activity.
    adb("shell", "am", "start", "-W", "-a", "android.intent.action.MAIN",
        "-c", "android.intent.category.LAUNCHER",
        "-n", APP + "/.MainActivity")
    again = one_control("NASTAVI")
    root = hierarchy()
    assert any("PAUZA" in label(n) for n in root.iter("node")), (
        "FAIL: active backgrounded flight did not open native Pause dialog")
    assert len(find_action(root, "PONOVO")) == 1
    assert len(find_action(root, "IZLAZ")) == 1
    assert adb("shell", "pidof", APP) == pid, (
        "FAIL: launcher return recreated the process")
    tap(again)
    resumed = active_flight()
    assert resumed.get("content-desc") == before, (
        "FAIL: backgrounding reset the pilot or its current lives")
    assert len(find_action(hierarchy(), "PAUZIRAJ IGRU")) == 0, (
        "FAIL: in-flight Pause overlay returned")
    tap(resumed)
    assert adb("shell", "pidof", APP) == pid, (
        "FAIL: resuming actual gameplay after background crashed")
    print("PASS: one native Pause on background return, original lives and real run preserved",
          flush=True)


if __name__ == "__main__":
    main()
