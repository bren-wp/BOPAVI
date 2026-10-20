#!/usr/bin/env python3
"""BOPAVI: conservative whole-native-source, packaging and audio reachability audit.

Runs on a clean GitHub checkout without Android SDK .
This is an additional guard, not a compiler call-graph or a memory-leak proof.
Platform callbacks, dynamic names, persisted field IDs and accessibility
entry points are never automatically removed by token-count heuristics.
"""
from pathlib import Path
import hashlib
import re

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "android/app/src/main/java/com/brendigo/bopavi"
ANDROID_SOURCES = [
    ANDROID / name for name in (
        "GameSimulation.kt", "GameView.kt", "LevelEngine.kt",
        "MainActivity.kt", "ProgressStore.kt", "Soundscape.kt")
]
SOURCES = ANDROID_SOURCES
DECLARATIONS = {
    ".kt": re.compile(
        r"\bprivate\s+(?:(?:suspend|lateinit)\s+)?(?:val|var|fun)\s+([A-Za-z_]\w*)\b"
    ),
}
KOTLIN_IMPORT = re.compile(r"(?m)^import\s+([\w.]+)(?:\s+as\s+(\w+))?\s*$")
RESOURCE = re.compile(r"\bR\.(drawable|raw)\.([A-Za-z_]\w*)\b")


def fail(issues):
    if issues:
        for issue in issues:
            print(f"FAIL: {issue}")
        raise SystemExit(1)


def audit_private_declarations():
    issues = []
    declarations = imports = 0
    for path in SOURCES:
        source = path.read_text(encoding="utf-8")
        for match in DECLARATIONS[path.suffix].finditer(source):
            symbol = match.group(1)
            declarations += 1
            # Symbol is mentioned only at declaration site: flag for review.
            # Keeping string/comments in this conservative pass avoids
            # deleting reflection or callback registrations accidentally.
            if len(re.findall(rf"\b{re.escape(symbol)}\b", source)) == 1:
                issues.append(f"{path.relative_to(ROOT)}: {symbol} (private declaration unused)")
        if path.suffix == ".kt":
            for match in KOTLIN_IMPORT.finditer(source):
                symbol = match.group(2) or match.group(1).split(".")[-1]
                if symbol == "*":
                    continue
                imports += 1
                if len(re.findall(rf"\b{re.escape(symbol)}\b", source)) == 1:
                    issues.append(f"{path.relative_to(ROOT)}: {symbol} (unused Kotlin import)")
    fail(issues)
    print(f"PASS: {len(SOURCES)} Android source units, {declarations} private "
          f"members and {imports} explicit Kotlin imports scanned")


def audit_resource_reachability():
    android_text = "\n".join(path.read_text(encoding="utf-8") for path in ANDROID_SOURCES)
    referenced = {"drawable": set(), "raw": set()}
    for kind, name in RESOURCE.findall(android_text):
        referenced[kind].add(name)
    names = set()
    for i in range(9):
        names.update({f"bopi{i}", f"bopileft{i}", f"bopiright{i}", f"bopiportrait{i}"})
    names.update({f"world{i}" for i in range(8)})
    names.update({"logo", "homeback", "splash", "app_icon",
                  "nav_profile", "nav_tasks", "nav_collection", "nav_shop",
                  "bopavi_startup"})
    issues = [f"Unrecognized drawable reference: {name}"
              for name in sorted(referenced["drawable"] - names)]
    # Keep dynamically indexed playable sprites and world art. The simplified
    # Home uses only the original Bopi portrait; other portrait assets remain
    # generated for archival compatibility but have no runtime references.
    expected_dynamic = {
        *(f"bopi{i}" for i in range(9)),
        *(f"bopileft{i}" for i in range(9)),
        *(f"bopiright{i}" for i in range(9)),
        "bopiportrait0",
        *(f"world{i}" for i in range(8)),
        "logo", "homeback",
    }
    issues.extend(f"Runtime drawable missing from source references: {name}"
                  for name in sorted(expected_dynamic - referenced["drawable"]))
    generator = (ROOT / "tools/generate_images.py").read_text(encoding="utf-8")
    workflow = (ROOT / ".github/workflows/native-ci.yml").read_text(encoding="utf-8")
    # The old check missed bare orphan PNGs after Contents.json was removed.
    # Xcode still warns about an unassigned Hero.imageset/hero.png, and an
    # unreferenced Android resource can still inflate an unsigned bundle.
    orphan_android = ROOT / "android/app/src/main/res/drawable-nodpi/hero.png"
    if (orphan_android.exists() or
            "Hero.imageset" in generator or "drawable-nodpi/hero.png" in generator or
            "drawable-nodpi/hero.png" in workflow ):
        issues.append("Unreferenced runtime Hero PNG/catalog still packaged; retain docs/assets/hero.svg")
    # Logo, HomeArt and LaunchArt are real runtime or OS launch resources.
    if not (ROOT / "docs/assets/hero.svg").is_file():
        issues.append("Source hero SVG must remain for Bopi sprite generator and README")
    fail(issues)
    print(f"PASS: {len(referenced['drawable'])} Android runtime drawable names, "
          "8 environments, 9 pilots, 18 wings, launch/logo assets and no orphan Hero catalog")


def audit_audio_pairing():
    android_raw = ROOT / "android/app/src/main/res/raw"
    android_names = {p.stem for p in android_raw.glob("*.wav")}
    expected = {"click", "collect", "hit", "level", "tap"}
    expected.update(f"world_{i}" for i in range(8))
    sources = "\n".join(path.read_text(encoding="utf-8") for path in ANDROID_SOURCES)
    declared = {name for kind, name in RESOURCE.findall(sources) if kind == "raw"}
    issues = []
    if android_names != expected:
        issues.append(f"Android audio missing/orphan: {sorted(expected ^ android_names)}")
    if declared != expected:
        issues.append(f"Audio lookup mismatch: {sorted(expected ^ declared)}")
    fail(issues)
    print("PASS: 13 Android runtime WAV resources; no orphan audio")


def audit_project_integration():
    manifest = (ROOT / "android/app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    issues = []
    if (ROOT / "ios").exists():
        issues.append("iOS project remains in Android-only working tree")
    if "android.permission.INTERNET" in manifest:
        issues.append("Unexpected network permission in offline single-player game")
    if 'android:allowBackup="false"' not in manifest:
        issues.append("Android backup/privacy policy changed")
    fail(issues)
    print("PASS: Android-only filesystem, offline permissions and backup contract")


def audit_gameplay_locale_parity():
    """Verify each Android HUD language contains all eight gameplay labels."""
    source = (ANDROID / "LevelEngine.kt").read_text(encoding="utf-8")
    start = 'private val flightRows=listOf('
    end = '    ).map { it.split("|") }'
    if start not in source or end not in source:
        fail(["Android gameplay language table missing"])
    section = source.split(start, 1)[1].split(end, 1)[0]
    rows = [entry.split("|") for entry in re.findall(
        r'(?m)^\\s*"([^"\\r\\n]+)"\\s*,?\\s*
    audit_private_declarations()
    audit_resource_reachability()
    audit_audio_pairing()
    audit_project_integration()
    audit_gameplay_locale_parity()
    print("PASS: conservative static Android dead-code / dead-asset / package audit")


if __name__ == "__main__":
    audit()
, section
    )]
    issues = []
    if len(rows) != 26 or len({row[0] for row in rows}) != 26:
        issues.append("Missing or duplicated Android HUD language")
    if any(len(row) != 9 or any(not word.strip() for word in row) for row in rows):
        issues.append("Incomplete Android HUD translation")
    view = (ANDROID / "GameView.kt").read_text(encoding="utf-8")
    if "GameLanguages.hudPack" not in view or "drawRootedCliffBases(" not in view:
        issues.append("Gameplay translations or anchored cliffs not reachable")
    fail(issues)
    print("PASS: 26 Android gameplay languages with eight HUD labels")


def audit():
    audit_private_declarations()
    audit_resource_reachability()
    audit_audio_pairing()
    audit_project_integration()
    audit_gameplay_locale_parity()
    print("PASS: comprehensive static native dead-code / dead-asset / package audit")


if __name__ == "__main__":
    audit()
