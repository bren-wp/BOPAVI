#!/usr/bin/env python3
"""Source safety gates, not substitute for emulator pixel-by-pixel comparison.

A decorative spike may not protrude above the *existing physical* floor;
Retry/Home must stay real accessible native controls, not screenshot regions.
"""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
view=(src/"GameView.kt").read_text()
physics=(src/"GameSimulation.kt").read_text()
spikes=(src/"FlightGroundSpikeGeometry.kt").read_text()
result=(src/"GameOverScreen.kt").read_text()
button=(src/"ResultActionEnamelDrawable.kt").read_text()
qa=(root/"tools/qa_android_emulator.sh").read_text()
unit=(root/"android/app/src/test/java/com/brendigo/bopavi/FlightGroundSpikeGeometryTest.kt").read_text()

# Physical floor, sprite radius, and both actual pillar/mace colliders survive.
assert "if (y < radius + 5f || y > 753f - radius)" in physics
assert "val radius = 18f" in physics
assert "PillarCollision.sweptBodyHitsPillar" in physics
assert "SkyMaceHazard.sweptHit" in physics
assert "const val solidFloorTop = 753f" in spikes
assert "const val cameraBottom = 800f" in spikes
assert "return solidFloorTop+if(index%2==0)0f else 2f" in spikes
assert "tipY(i)<solidFloorTop" in spikes
assert "stoneTop>=solidFloorTop" in spikes
assert "spikeTipsRemainInExistingSolidGround" in unit
assert "visualSpikesNeverReduceTheBirdsAllowedMovement" in unit
assert "val showGameplayObstacles=FlightHazardPresentation.obstaclesVisible(" in view
assert "if(showGameplayObstacles) {" in view
# Both illustrated Sky Meadows and Enchanted Forest keep spike tips wholly
# within the pre-existing collision-solid floor. This is not a new hazard.
assert "if(game.level.world==0 || game.level.world==6)" in view
assert "drawEnchantedForestGroundSpikes(canvas)" in view
assert "val tip=g.tipY(i)" in view
assert "val base=g.spineBottom" in view
assert "drawEnchantedForestGroundSpikes" in view.split("private fun drawWorldAtmosphere",1)[0]
assert "private fun drawEnchantedForestGroundSpikes(c:Canvas)" in view

# Two full native click actions, real run stats and selected alternate skins.
assert 'stage.place(action("RETRY",true,onRetry)' in result
assert 'stage.place(action("HOME",false,onHome)' in result
assert "setOnClickListener{onClick()}" in result
assert "ResultActionEnamelDrawable(primary)" in result
assert "setOnTouchListener{v,event->" in result and "\n                false\n" in result
assert "HomeEmbossedLabel(host,label,60f,primary)" in result
assert "contentDescription=label" in result
assert "background=ResultRowEnamelDrawable()" in result
enamel=(src/"ResultSharedEnamelDrawable.kt").read_text()
assert "class ResultRowEnamelDrawable" in enamel
assert 'contentDescription="$label: $value"' in result
assert "GameOverSeatedBopi(host)" in result
seated=(src/"GameOverSeatedBopi.kt").read_text()
assert "val headPose=c.save()" in seated
assert "c.rotate(6f,256f,241f)" in seated
assert "c.restoreToCount(headPose)" in seated
assert "line(c,176f,224f,234f,207f" in seated
assert "line(c,288f,209f,344f,227f" in seated
assert "setImageResource(pilotDrawable)" in result
assert "val lossBackdrop=if(worldDrawable==R.drawable.world0)" in result
assert "R.drawable.homeback else worldDrawable" in result

# Drawn enamel has actual curved 3D rim, two different palettes and reactive press.
for needle in ("isStateful():Boolean=true",
               "onStateChange(states:IntArray):Boolean",
               "val drop=min(24f,h*.155f)",
               "if(pressed).49f else 1f",
               "val golden:Boolean", "LinearGradient(", "paint.shader=",
               "canvas.drawRoundRect(", "canvas.drawPath(gleam,paint)"):
    assert needle in button,needle
assert "onTouchEvent(" not in button
assert "BitmapFactory" not in button
assert 'if len(retry)!=1 or len(home)!=1' in qa
assert 'RETRY did not reach idle Flight' in qa

# Enlarged HUD art, while lives and accessibility remain genuine state.
assert "path.cubicTo(x-14.5f,y+1f,x-12f,y-13f,x,y-6f)" in view
assert "i<lives" in view and "Životi: $lives/" in view
print("PASS: collision-fair 3D floor and genuinely clickable, extruded Retry/Home")
