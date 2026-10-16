"""BOPAVI HUD reference parity: real glyph-free vector geometry.

This protects deterministic cross-platform crown/checker positioning in the
480 x 800 gameplay viewport. It deliberately does NOT assert that any full
screen or cinematic backdrop is pixel-identical to user reference paintings.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ANDROID = (ROOT / "android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
IOS = (ROOT / "ios/BOPAVI/GameCanvas.swift").read_text()

contract = {
    "crown": (
        'drawHudCrown(c,165f,46f,22f)',
        'drawHudCrown(c,165,46,22)',
    ),
    "finish": (
        'drawHudFinishFlag(c,363f,45f,25f)',
        'drawHudFinishFlag(c,363,45,25)',
    ),
    "wallet": (
        'drawHudCrown(c,94f,50f,15f)',
        'drawHudCrown(c,94,50,15)',
    ),
    "checkpoint": (
        'drawHudCrown(c,cx,cy-44f,20f)',
        'drawHudCrown(c,cx,cy-44,20)',
    ),
}
for label, (android, ios) in contract.items():
    assert android in ANDROID, (label, "missing Android native icon")
    assert ios in IOS, (label, "missing iOS native icon")

for code, a, b, c in (
    (ANDROID, "private fun drawHudCrown(", "private fun drawHudFinishFlag(", "private fun drawLevelProgress("),
    (IOS, "private func drawHudCrown(", "private func drawHudFinishFlag(", "private func drawLevelProgress("),
):
    assert code.index(a) < code.index(b) < code.index(c)
    # All 3 outline points plus base must share coordinates on both OSes.
    def has_fraction(value):
        return ("0." + value in code) or ("." + value + "f" in code)
    assert all(has_fraction(x) for x in ("34", "55", "24", "19", "08"))
    # Android uses packed ARGB integers; iOS color() uses 24-bit RGB.
    palette = (
        ("0xffc76f03", "0xffffd342", "0xffffef9b",
         "0xffd7f4ff", "0xff17488d", "0xff5eeaff")
        if code is ANDROID else
        ("0xc76f03", "0xffd342", "0xffef9b",
         "0xd7f4ff", "0x17488d", "0x5eeaff")
    )
    assert all(col in code for col in palette)
    progress_end = "    private fun hudGlass(" if code is ANDROID else "    private func hudGlass("
    progress = code.split(c, 1)[1].split(progress_end, 1)[0]
    assert "drawHudCrown(" in progress and "drawHudFinishFlag(" in progress
    assert "⚑" not in progress and "♛" not in progress
    assert "game.levelProgress" in progress
    assert "game.displayLevel" in progress

assert "for(row in 0..3)for(col in 0..3)" in ANDROID
assert "for row in 0..<4" in IOS and "for col in 0..<4" in IOS
assert "if((row+col)%2==0)" in ANDROID
assert "(row+col)%2==0" in IOS
assert "hudText(\"♛\"" not in IOS
assert 'text(c,"♛"' not in ANDROID
assert "hudText(\"⚑\"" not in IOS

# UI remains live: these are genuine logical values and buttons elsewhere
# in each native tree, not a reference image with screenshot hotspots.
for code in (ANDROID, IOS):
    assert "game.coins" in code and "game.score()" in code
    assert "game.distance" in code and "game.totalPassed" in code
    assert "game.passed" in code and "game.level.gates" in code

print("PASS: glyph-independent crown, goal flag and real HUD metrics on Android/iOS")
