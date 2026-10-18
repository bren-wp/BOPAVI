#!/usr/bin/env python3
"""BOPAVI: conservative whole-native-source, packaging and audio reachability audit.

Runs on a clean GitHub checkout without Android SDK or an iOS simulator.
This is an additional guard, not a compiler call-graph or a memory-leak proof.
Platform callbacks, dynamic names, persisted field IDs and accessibility
entry points are never automatically removed by token-count heuristics.
"""
from pathlib import Path
import hashlib
import re

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "android/app/src/main/java/com/brendigo/bopavi"
IOS = ROOT / "ios/BOPAVI"
ANDROID_SOURCES = [
    ANDROID / name for name in (
        "GameSimulation.kt", "GameView.kt", "LevelEngine.kt",
        "MainActivity.kt", "ProgressStore.kt", "Soundscape.kt")
]
IOS_SOURCES = [
    IOS / name for name in (
        "BopaviApp.swift", "BopaviCore.swift", "GameCanvas.swift",
        "GameController.swift", "ProgressStore.swift", "Soundscape.swift")
]
SOURCES = ANDROID_SOURCES + IOS_SOURCES
DECLARATIONS = {
    ".kt": re.compile(
        r"\bprivate\s+(?:(?:suspend|lateinit)\s+)?(?:val|var|fun)\s+([A-Za-z_]\w*)\b"
    ),
    ".swift": re.compile(
        r"\bprivate(?:\([^)]+\))?\s+(?:let|var|func)\s+([A-Za-z_]\w*)\b"
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
    print(f"PASS: {len(SOURCES)} platform source units, {declarations} private "
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
    hero_catalog = ROOT / "ios/BOPAVI/Assets.xcassets/Hero.imageset/Contents.json"
    # The old check missed bare orphan PNGs after Contents.json was removed.
    # Xcode still warns about an unassigned Hero.imageset/hero.png, and an
    # unreferenced Android resource can still inflate an unsigned bundle.
    orphan_android = ROOT / "android/app/src/main/res/drawable-nodpi/hero.png"
    orphan_ios = ROOT / "ios/BOPAVI/Assets.xcassets/Hero.imageset/hero.png"
    if (hero_catalog.exists() or orphan_android.exists() or orphan_ios.exists() or
            "Hero.imageset" in generator or "drawable-nodpi/hero.png" in generator or
            "drawable-nodpi/hero.png" in workflow or "Hero.imageset" in workflow):
        issues.append("Unreferenced runtime Hero PNG/catalog still packaged; retain docs/assets/hero.svg")
    # Logo, HomeArt and LaunchArt are real runtime or OS launch resources.
    ios_source = "\n".join(path.read_text(encoding="utf-8") for path in IOS_SOURCES)
    ios_plist = (IOS / "Info.plist").read_text(encoding="utf-8")
    for token in ('UIImage(named:"Logo")', 'UIImage(named:"HomeArt")'):
        if token not in ios_source:
            issues.append(f"Referenced iOS app art missing: {token}")
    # Minimal game intentionally uses only a native neutral launch background;
    # the large BOPAVI illustration is drawn once by the live Home view.
    if "<key>UILaunchScreen</key>" not in ios_plist or "<string>LaunchBackground</string>" not in ios_plist:
        issues.append("iOS system launch background missing")
    if not (ROOT / "docs/assets/hero.svg").is_file():
        issues.append("Source hero SVG must remain for Bopi sprite generator and README")
    fail(issues)
    print(f"PASS: {len(referenced['drawable'])} Android runtime drawable names, "
          "8 environments, 9 pilots, 18 wings, launch/logo assets and no orphan Hero catalog")


def audit_audio_pairing():
    android_raw = ROOT / "android/app/src/main/res/raw"
    ios_audio = IOS / "Audio"
    android_names = {p.stem for p in android_raw.glob("*.wav")}
    ios_names = {p.stem for p in ios_audio.glob("*.wav")}
    expected = {"click", "collect", "hit", "level", "tap"}
    expected.update(f"world_{i}" for i in range(8))
    sources = "\n".join(path.read_text(encoding="utf-8") for path in ANDROID_SOURCES)
    declared = {name for kind, name in RESOURCE.findall(sources) if kind == "raw"}
    issues = []
    if android_names != expected:
        issues.append(f"Android audio missing/orphan: {sorted(expected ^ android_names)}")
    if ios_names != expected:
        issues.append(f"iOS audio missing/orphan: {sorted(expected ^ ios_names)}")
    if declared != expected:
        issues.append(f"Audio lookup mismatch: {sorted(expected ^ declared)}")
    for name in sorted(expected & android_names & ios_names):
        a = hashlib.sha256((android_raw / f"{name}.wav").read_bytes()).digest()
        b = hashlib.sha256((ios_audio / f"{name}.wav").read_bytes()).digest()
        if a != b:
            issues.append(f"Cross-platform audio differs: {name}")
    fail(issues)
    print("PASS: 13 paired runtime audio resources; no orphan WAV and byte-identical platforms")


def audit_project_integration():
    project = (ROOT / "ios/BOPAVI.xcodeproj/project.pbxproj").read_text(encoding="utf-8")
    issues = []
    for path in IOS_SOURCES:
        if path.name not in project:
            issues.append(f"Swift source is unlinked from Xcode target: {path.name}")
    manifest = (ROOT / "android/app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    if "android.permission.INTERNET" in manifest:
        issues.append("Unexpected network permission in offline single-player game")
    if 'android:allowBackup="false"' not in manifest:
        issues.append("OS backup policy changed without explicit privacy review")
    fail(issues)
    print("PASS: six iOS source units linked to project, offline Android permission/backup contract")



def audit_gameplay_locale_parity():
    """Audit all eight runtime vocabulary keys for all 26 language choices."""
    def rows(path, start, end):
        content=path.read_text(encoding="utf-8")
        if start not in content or end not in content.split(start, 1)[1]:
            raise ValueError(f"Missing live-HUD locale table in {path.name}")
        section=content.split(start, 1)[1].split(end, 1)[0]
        matches=re.findall(r'(?m)^\s*"([^"\r\n]+)"\s*,?\s*$', section)
        return [value.split("|") for value in matches]
    issues=[]
    try:
        android=rows(ANDROID/"LevelEngine.kt","private val flightRows=listOf(",
                     '    ).map { it.split("|") }')
        ios=rows(IOS/"BopaviCore.swift","private static let flightRows:[String]=[",
                 "    ]")
    except ValueError as exc:
        fail([str(exc)])
    for platform,table in (("Android",android),("iOS",ios)):
        if len(table)!=26 or len(set(row[0] for row in table))!=26:
            issues.append(f"{platform}: missing or duplicate gameplay language code")
        for row in table:
            if len(row)!=9 or any(not cell.strip() for cell in row):
                issues.append(f"{platform}: incomplete HUD translation for {row[0]}")
    if android != ios:
        issues.append("Android/iOS HUD translations diverge or have dead entries")
    ios_source=(IOS/"GameCanvas.swift").read_text(encoding="utf-8")
    glass=ios_source.split("private func hudGlass(",1)[-1].split(
        "private func hudGoldCoin(",1)[0]
    if "CGPath(roundedRect:" in glass:
        issues.append("Per-frame heap allocation of the HUD rim CGPath")
    for path in (ANDROID/"GameView.kt",IOS/"GameCanvas.swift"):
        source=path.read_text(encoding="utf-8")
        if "GameLanguages.hudPack" not in source or "drawRootedCliffBases(" not in source:
            issues.append(f"{path.name}: inaccessible translations or missing cliff roots")
    fail(issues)
    print("PASS: 26 paired native gameplay languages, no dead labels or CGPath churn")



def audit():
    audit_private_declarations()
    audit_resource_reachability()
    audit_audio_pairing()
    audit_project_integration()
    audit_gameplay_locale_parity()
    print("PASS: comprehensive static native dead-code / dead-asset / package audit")


if __name__ == "__main__":
    audit()
