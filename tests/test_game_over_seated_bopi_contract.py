#!/usr/bin/env python3
"""Game Over has a distinct sad seated default Bopi, no flattened screen."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
src=root/"android/app/src/main/java/com/brendigo/bopavi"
screen=(src/"GameOverScreen.kt").read_text()
pose=(src/"GameOverSeatedBopi.kt").read_text()
assert "GameOverSeatedBopi(host)" in screen
assert "pilotDrawable==R.drawable.bopiportrait0" in screen
assert "setImageResource(pilotDrawable)" in screen  # other selected pilots preserved
assert "stage.place(resultPilot,224f,431f,494f,445f," in screen
assert "stage.place(GameOverPodium(host),168f,728f,605f,216f," in screen
assert "onRetry" in screen and "onHome" in screen
assert "setOnClickListener{onClick()}" in screen
assert "internal class GameOverSeatedBopi(context:Context):View(context)" in pose
assert "importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO" in pose
for feature in ("SITTING pose", "Sad lowered brows",
                "Closed drooping orange beak", "Large sitting toes",
                "tucked downward"):
    assert feature in pose,feature
assert "android.graphics.BitmapFactory" not in pose
assert "onTouchEvent" not in pose
print("PASS: native Game Over has a separate seated aviator illustration and real Retry/Home")
