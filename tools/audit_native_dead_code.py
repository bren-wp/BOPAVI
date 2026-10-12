#!/usr/bin/env python3
"""Conservative native dead-code audit. Never delete save or platform callbacks.

Private declarations are safe to flag when their identifier is never referred to
again in their own compilation file; Kotlin imports are checked separately.
This is a regression guard, not a claim to prove whole-program reachability.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "android/app/src/main/java/com/brendigo/bopavi"
IOS = ROOT / "ios/BOPAVI"
SOURCES = [
    *(ANDROID / filename for filename in (
        "GameSimulation.kt", "GameView.kt", "LevelEngine.kt",
        "MainActivity.kt", "ProgressStore.kt", "Soundscape.kt")),
    *(IOS / filename for filename in (
        "BopaviApp.swift", "BopaviCore.swift", "GameCanvas.swift",
        "GameController.swift", "ProgressStore.swift", "Soundscape.swift")),
]
DECLARATIONS = {
    ".kt": re.compile(r"\bprivate\s+(?:(?:suspend|lateinit)\s+)?(?:val|var|fun)\s+([A-Za-z_]\w*)\b"),
    ".swift": re.compile(r"\bprivate(?:\([^)]+\))?\s+(?:let|var|func)\s+([A-Za-z_]\w*)\b"),
}
KOTLIN_IMPORT = re.compile(r"(?m)^import\s+([\w.]+)(?:\s+as\s+(\w+))?\s*$")


def audit():
    candidates = []
    declarations = imports = 0
    for path in SOURCES:
        src = path.read_text(encoding="utf-8")
        for match in DECLARATIONS[path.suffix].finditer(src):
            symbol = match.group(1)
            declarations += 1
            if len(re.findall(rf"\b{re.escape(symbol)}\b", src)) == 1:
                candidates.append((path, symbol, "private declaration"))
        if path.suffix == ".kt":
            for match in KOTLIN_IMPORT.finditer(src):
                symbol = match.group(2) or match.group(1).split(".")[-1]
                if symbol == "*":
                    continue
                imports += 1
                if len(re.findall(rf"\b{re.escape(symbol)}\b", src)) == 1:
                    candidates.append((path, symbol, "unused import"))
    if candidates:
        for path, symbol, reason in candidates:
            print(f"FAIL: {path.relative_to(ROOT)}: {symbol} ({reason})")
        raise SystemExit(1)
    print(
        f"PASS: native dead-code regression — {len(SOURCES)} Kotlin/Swift files, "
        f"{declarations} private declarations and {imports} Kotlin imports checked"
    )


if __name__ == "__main__":
    audit()
