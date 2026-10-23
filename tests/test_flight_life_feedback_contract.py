#!/usr/bin/env python3
"""Ensure real, non-flashing life/guard changes are rendered and tested."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text(encoding="utf-8")
layout=(src/"FlightHudLayout.kt").read_text(encoding="utf-8")
feedback=(src/"FlightHudFeedback.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHudFeedbackTest.kt").read_text(encoding="utf-8")
hud=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHudLayoutTest.kt").read_text(encoding="utf-8")
for value in (
    "private val flightHudFeedback=FlightHudFeedback()",
    "flightHudFeedback.update(game.lives,game.pilotGuardReady,game.time)",
    "hudGlass(c,h.notice.left,h.notice.top,h.notice.right,h.notice.bottom)",
    "text(c,notice.label,h.notice.left+17f,h.notice.bottom-10f,13f,notice.tint)",
):
    assert value in view,value
for value in (
    "val notice = FlightHudBox(18f,149f,275f,184f)",
    "val ability = FlightHudBox(18f,91f,340f,140f)",
):
    assert value in layout,value
for value in (
    "private var previousLives:Int?=null",
    "private var previousGuard:Boolean?=null",
    'const val LOST_LIFE="−1 ŽIVOT"',
    'const val RESTORED_LIFE="+1 ŽIVOT"',
    'const val USED_SHIELD="ŠTIT POTROŠEN"',
    "lives<beforeLives",
    "lives>beforeLives",
    "beforeGuard==true && !guardReady",
    "if(time>=shown.startedAt && time-shown.startedAt<DURATION_SECONDS)",
):
    assert value in feedback,value
for name in (
    "initialObservationDoesNotInventLostOrRestoredLives",
    "lossAndRecoveryAreShownOncePerActualChange",
    "foxGuardDoesNotPretendToConsumeHeart",
    "lowFrameRatesDoNotRepeatEventsAndNoEventSurvivesRestart",
    "corruptTimeCannotLeavePermanentNotice",
):
    assert name in unit,name
assert "FlightHudLayout.notice" in hud or "hud.notice" in hud
assert "android.permission.INTERNET" not in (
    root/"android/app/src/main/AndroidManifest.xml").read_text()
print("PASS: short, truthful, non-blinking life and fox-guard HUD event integration")
