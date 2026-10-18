#!/usr/bin/env python3
"""Fail-closed static contract for debug-only physics collision outlines."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
android=(r/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
activity=(r/"android/app/src/main/java/com/brendigo/bopavi/MainActivity.kt").read_text()
ios=(r/"ios/BOPAVI/GameCanvas.swift").read_text()
ci=(r/".github/workflows/native-ci.yml").read_text()
assert 'FLAG_DEBUGGABLE' in android and 'collisionOverlayEnabled=qaCollisionOverlay &&' in android
assert 'bopavi_qa_collision_overlay' in activity
assert 'drawCollisionDebugOverlay(canvas)' in android
assert ios.count('#if DEBUG')>=2 and ios.count('#endif')>=2
assert 'ProcessInfo.processInfo.arguments.contains("-BOPAVICollisionDebug")' in ios
assert 'private func drawCollisionDebugOverlay(_ c:CGContext)' in ios
assert 'Compile DEBUG-only collision QA overlay' in ci and '-configuration Debug' in ci
for f in (android,ios):
    for marker in ("drawCircle(game.birdX,game.y,game.radius,p)", "c.strokeEllipse(in:CGRect(x:CGFloat(game.birdX)-r"):
        if marker in f: break
    else: raise AssertionError("No body-circle overlay")
# This used to compile even on Debug without an active DEBUG condition:
# compiling a Debug simulator alone does not prove that #if DEBUG runs.
project=(r/"ios/BOPAVI.xcodeproj/project.pbxproj").read_text()
debug_targets=[line for line in project.splitlines() if "/* target_debug */" in line]
release_targets=[line for line in project.splitlines() if "/* target_release */" in line]
assert len(debug_targets)==1 and len(release_targets)==1
assert "SWIFT_ACTIVE_COMPILATION_CONDITIONS = DEBUG;" in debug_targets[0], (
    "Target Debug must explicitly define DEBUG for the collision overlay")
assert "SWIFT_ACTIVE_COMPILATION_CONDITIONS" not in release_targets[0], (
    "Release must not inherit the diagnostic overlay compilation condition")
print("PASS: only debuggable/test-opt-in collision debug artwork, with iOS DEBUG compile")
