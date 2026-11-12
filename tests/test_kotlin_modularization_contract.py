#!/usr/bin/env python3
"""Safe Kotlin structural split: top-level declarations move, API stays stable."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]/"android/app/src/main/java/com/brendigo/bopavi"
sources={p.name:p.read_text(encoding="utf-8") for p in r.glob("*.kt")}
required={
    "GameSimulation":"GameSimulation.kt",
    "WorldCoordinates":"WorldCoordinates.kt",
    "PillarCollision":"PillarCollision.kt",
    "BerryCollision":"BerryCollision.kt",
    "GameOverScreen":"GameOverScreen.kt",
    "GameOverCrown":"GameOverCrown.kt",
    "GameOverPodium":"GameOverPodium.kt",
    "HomeReferenceScreen":"HomeReferenceScreen.kt",
    "HomeOrbitDecoration":"HomeOrbitDecoration.kt",
}
import re
for symbol,filename in required.items():
    declaration=re.compile(rf"(?m)^(?:internal )?(?:class|object) {symbol}\\b")
    hits=[name for name,src in sources.items() if declaration.search(src)]
    assert hits==[filename],f"{symbol}: expected {filename}, found {hits}"
sim=sources["GameSimulation.kt"]
for entry in ("PillarCollision.sweptBodyHitsPillar(",
              "BerryCollision.touched(","WorldCoordinates."):
    assert entry in sim,entry
assert "fun sweptBodyHitsPillar(" in sources["PillarCollision.kt"]
assert "fun touched(" in sources["BerryCollision.kt"]
assert "internal class GameOverCrown" in sources["GameOverCrown.kt"]
assert "internal class GameOverPodium" in sources["GameOverPodium.kt"]
assert "private fun drawBird(" in sources["GameView.kt"]
assert "RoyalRingTrail" not in "\n".join(sources.values())
print("PASS: 9 top-level Android API contracts spread across independent Kotlin files")
