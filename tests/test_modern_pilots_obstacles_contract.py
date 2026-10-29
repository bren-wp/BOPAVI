#!/usr/bin/env python3
"""Release contract: real variable pillars, shared facing and approved icon.

Gameplay gates use their new widths for both painted shafts and swept body
collision. All pilot sprites share one restrained frontal Canvas pose. The
launcher foreground is exactly the owner-approved Bopi portrait.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
level=(src/"LevelEngine.kt").read_text(encoding="utf-8")
difficulty=(src/"FlightChallengePolicy.kt").read_text(encoding="utf-8")
view=(src/"GameView.kt").read_text(encoding="utf-8")
pose=(src/"PilotFlightPose.kt").read_text(encoding="utf-8")
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/PilotFlightPoseTest.kt").read_text(encoding="utf-8")
generator=(root/"tools/generate_images.py").read_text(encoding="utf-8")
icon=(root/"tests/test_launcher_premium.py").read_text(encoding="utf-8")
fairness=(root/"android/app/src/test/java/com/brendigo/bopavi/ObstacleFairnessTest.kt").read_text(encoding="utf-8")

assert 'width = FlightChallengePolicy.gateWidth(' in level
assert 'gap = FlightChallengePolicy.gateGap(' in level
for v in ("0 -> 54f", "1 -> 74f", "2 -> 86f", "3 -> 98f", "4 -> 110f", "else -> 80f"):
    assert v in difficulty,v
assert "return max(164f" in difficulty
assert "LevelEngine.opening(gate" in (src/"GameSimulation.kt").read_text()
assert "PillarCollision.sweptBodyHitsPillar(" in (src/"GameSimulation.kt").read_text()
assert "g.width" in view
assert "seededGateRhythmsHaveBodyClearanceAndRecoverableAltitudeChanges" in fairness
assert "c.rotate(PilotFlightPose.pitchDegrees(game.velocity,reducedMotion))" in view
assert "PilotFlightPose.wingDegrees(game.time,game.flapPulse,reducedMotion)" in view
assert "coerceIn(-7f,9f)" in pose
assert "allCharactersUseSameActualRenderingPose" in unit
assert "0xe8092949.toInt()" in view
assert "p.strokeWidth=1.3f" in view
assert "mascot=source.crop(bounds)" in generator
assert "foreground.alpha_composite(icon_mascot" in generator
assert "legacy_layer.alpha_composite(legacy_mascot" in generator
assert "assert adaptive.tobytes()==expected.tobytes()" in icon
assert "ImageDraw.Draw(halo" not in generator
assert "drawPolishedGateLip(c,x,w,top-28f)" in view
assert "M-17 29 Q-34 55" in (root/"docs/assets/lisac.svg").read_text()
print("PASS: collision-safe pillars, frontal Sova/Lisac rig and original mascot icon")
