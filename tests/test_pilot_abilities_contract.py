#!/usr/bin/env python3
"""Fail-closed wiring contract for three playable skills and real gate physics."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
activity=(src/"MainActivity.kt").read_text(encoding="utf-8")
view=(src/"GameView.kt").read_text(encoding="utf-8")
sim=(src/"GameSimulation.kt").read_text(encoding="utf-8")
level=(src/"LevelEngine.kt").read_text(encoding="utf-8")
abilities=(src/"PilotAbility.kt").read_text(encoding="utf-8")
challenge=(src/"FlightChallengePolicy.kt").read_text(encoding="utf-8")
tests=(root/"android/app/src/test/java/com/brendigo/bopavi/PilotAbilityTest.kt").read_text(encoding="utf-8")
other=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightChallengePolicyTest.kt").read_text(encoding="utf-8")

for needle in (
    'pilotNames=arrayOf("Bopi","Sova","Lisac")',
    'PilotAbility.fromChoice(chosenPilot)',
    'progress.chooseArcadePilot(index)',
    'portraitDrawableIds[selectedPilotChoice]',
    'game.game.pilotAbility.shortTitle',
    'game.game.pilotAbility.description',
    'g.pilotAbility.description',
    'g.pilotAbility.shortTitle',
):
    assert needle in activity,needle

# Pilot abilities remain real mechanics but no power badge is drawn in flight.
assert "game.pilotAbility" not in view  # Real pilot skills stay in GameSimulation, not the HUD
assert "hudGlass(c,h.ability" not in view
assert "drawHudPowerIcon(" not in view
assert '"ŽIVOTI"' in view

for needle in (
    'val pilotAbility:PilotAbility = PilotAbility.CLASSIC',
    'pilotAbility.speedMultiplier',
    'pilotAbility.gravityMultiplier',
    'pilotAbility.terminalFallSpeed',
    'var pilotGuardReady = endless && pilotAbility == PilotAbility.LISAC; private set',
    'if (pilotGuardReady) {',
    'pilotGuardReady=false',
    'invulnerable=1.25f',
    'impactPulse=.65f',
    'val radius = 18f',
    'PillarCollision.sweptBodyHitsPillar(',
):
    assert needle in sim,needle
assert 'terminalFallSpeed:Float=365f' in abilities
assert 'terminalFallSpeed=315f' in abilities
assert "fun hudTint(" not in abilities and "fun hudStatus(" not in abilities
assert 'FlightChallengePolicy.levelSpeed(zone,world,epoch,type)' in level
assert 'FlightChallengePolicy.gateGap(' in level
assert 'FlightChallengePolicy.gateMovement(' in level
assert 'max(164f' in challenge and 'min(22f' in challenge
for name in ('threeSelectablePilotsAreDistinctAndStable',
             'legacyFlightRemainsCompatibleButRealBopiTravelsFaster',
             'owlDescendsMoreSlowlyWithoutModifyingColliderOrInput',
             'foxBlocksExactlyOneRealCollisionThenLosesAHeart',
             'guardCannotBeCarriedIntoLegacyFiniteLevels',
    'owlHasARealLowerTerminalFallSpeedWithoutChangingCollision',
    'everyPilotKeepsFlightPhysicsConsistentAt30_60_120Hz',
    'skillsKeepTheirSavedIdentifiersAfterRemovingPowerHud'):
    assert name in tests,name
for name in ('generatedWorldsAreFasterButBoundedAndDeterministic',
             'gatesAreTougherAfterWarmupWithoutImpossibleMouths',
             'movingPillarsStayBoundedAndZeroRemainsStill'):
    assert name in other,name
qa=(root/"tools/qa_android_emulator.sh").read_text(encoding="utf-8")
assert 'and "MOĆ:" not in label(n)' in qa
manifest=(root/"android/app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
assert 'android.permission.INTERNET' not in manifest
print("PASS: three unique native abilities, active abilities without a cluttered status rail, bounded true pillar physics")
