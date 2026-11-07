#!/usr/bin/env python3
"""The owner ready Gameplay reference includes true gate/pickup previews.

This source contract checks that the same physical gate coordinates render
on idle and after the real first flap, without starting physics prematurely.
It does NOT claim RGB parity.
"""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
source=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(source/"GameView.kt").read_text(encoding="utf-8")
simulation=(source/"GameSimulation.kt").read_text(encoding="utf-8")
model=(source/"FlightHazardPresentation.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightHazardPresentationTest.kt").read_text(encoding="utf-8")
qa=(root/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")

# Physics starts from a real click, not from level preparation or rendering.
assert "var active = false; private set" in simulation
first_flap=simulation.split("fun flap() {",1)[1].split("fun step(",1)[0]
assert "active = true" in first_flap
assert "flaps++" in first_flap
assert "if (!active || finished) return" in simulation

# The owner frame shows real hazards even before the first actual tap.
# Preview is visual only: the simulation's state and physics stay idle.
assert "fun obstaclesVisible(firstFlapReceived:Boolean,finished:Boolean)" in model
assert "        !finished" in model
assert "obstaclesVisible(firstFlapReceived:Boolean,finished:Boolean)" in model
assert "animation" not in model.lower()

on_draw=view.split("override fun onDraw(canvas:Canvas) {",1)[1].split(
    "private fun drawWorldLighting(",1)[0]
assert "val showGameplayObstacles=FlightHazardPresentation.obstaclesVisible(" in on_draw
assert "game.active,game.finished)" in on_draw
hazards=on_draw.split("if(showGameplayObstacles) {",1)[1].split(
    "        drawBird(canvas)",1)[0]
for item in ("drawEnchantedForestGroundSpikes(canvas)",
             "game.upcomingGates()", "drawGate(canvas,g,x,i,true)",
             "drawGate(canvas,g,x,i)"):
    assert item in hazards,item
# All physical gate drawing must happen inside the preview visibility guard.
before=on_draw.split("if(showGameplayObstacles) {",1)[0]
assert "drawEnchantedForestGroundSpikes(canvas)" not in before
assert "if(game.level.world==0 || game.level.world==6)" in on_draw
assert "drawEnchantedForestGroundSpikes(canvas)" in on_draw
assert "drawGate(canvas," not in before
assert "if(collisionOverlayEnabled && showGameplayObstacles)" in on_draw

# Bopi, lives and instruction remain on idle; visual preview has no physics work.
assert "drawBird(canvas)" in on_draw
assert "if(!game.active && !game.finished) {" in on_draw
assert "drawFlightReadyTutorial(canvas)" in on_draw
assert "drawHud(canvas)" in on_draw
assert "game.step(it)" in on_draw
assert "if(game.active && !paused && !qaWaitingForCapture && !game.finished)" in on_draw

click=view.split("override fun performClick():Boolean {",1)[1].split(
    "override fun onAttachedToWindow()",1)[0]
assert "val firstFlap=!game.active" in click
assert "game.flap()" in click
assert "if(firstFlap && game.active)" in click
for case in ("ownerReferenceShowsRealGatePreviewBeforeFirstFlap",
             "showRealHazardsAfterFirstFlap",
             "finishedRunCannotDrawGameplayHazardsOverResult",
             "newRunStartsWithHonestRealGatePreviewWithoutOldStateLeak"):
    assert case in unit,case

# Existing native Android QA captures both true idle and real first-flap frames.
for capture in ("android-gameplay-ready-reference-size",
                "android-gameplay-active-reference-size"):
    assert capture in qa,capture
assert "python3 tools/qa_first_flap_hazards.py" in qa
probe=(root/"tools/qa_first_flap_hazards.py").read_text(encoding="utf-8")
for marker in ("WIDTH, HEIGHT = 941, 1672", "ROI = (265, 1200, 700, 1390)",
               "read_rgb_crop(ready_path)", "read_rgb_crop(active_path)",
               "if fraction<.06:", "import zlib"):
    assert marker in probe,marker
assert "import PIL" not in probe and "cv2" not in probe
print("PASS: ready Gameplay previews real gates and safe ground-edge cones; collisions still start on first tap")
