#!/usr/bin/env python3
"""Conservative Android-only source, asset and privacy regression scan."""
from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
CODE=ROOT/"android/app/src/main/java/com/brendigo/bopavi"
SOURCES=sorted(CODE.glob("*.kt"))
RESOURCE=re.compile(r"\bR\.(drawable|raw)\.([A-Za-z_]\w*)\b")
IMPORT=re.compile(r"(?m)^import\s+([\w.]+)(?:\s+as\s+(\w+))?\s*$")
PRIVATE=re.compile(r"\bprivate\s+(?:(?:suspend|lateinit)\s+)?(?:val|var|fun)\s+([A-Za-z_]\w*)\b")
def require(ok,msg):
    if not ok:raise AssertionError(msg)
def main():
    require(SOURCES,"No Android source")
    require(not (ROOT/"ios").exists(),"Removed iOS directory restored")
    require(not list(ROOT.rglob("*.swift")),"Swift source remains")
    joined="\n".join(p.read_text(encoding="utf-8") for p in SOURCES)
    for path in SOURCES:
        source=path.read_text(encoding="utf-8")
        for pattern,kind in ((IMPORT,"unused import"),(PRIVATE,"unused private symbol")):
            for hit in pattern.finditer(source):
                name=(hit.group(2) or hit.group(1).split(".")[-1]) if kind=="unused import" else hit.group(1)
                if name!="*" and len(re.findall(rf"\b{re.escape(name)}\b",source))==1:
                    raise AssertionError(f"{path.name}: {kind}: {name}")
    referenced={"drawable":set(),"raw":set()}
    for kind,name in RESOURCE.findall(joined):referenced[kind].add(name)
    names={"click","collect","hit","level","tap"}|{f"world_{i}" for i in range(8)}
    actual={p.stem for p in (ROOT/"android/app/src/main/res/raw").glob("*.wav")}
    require(actual==names,f"Audio inventory mismatch: {actual^names}")
    require(referenced["raw"]==names,"Audio resource references missing")
    dynamic={*(f"world{i}" for i in range(8)),*(f"bopi{i}" for i in range(11)),
             *(f"bopileft{i}" for i in range(11)),*(f"bopiright{i}" for i in range(11)),
             "bopiportrait0","bopiportrait9","bopiportrait10","logo","homeback"}
    require(dynamic<=referenced["drawable"],f"Missing drawable references: {dynamic-referenced['drawable']}")
    require(not (ROOT/"android/app/src/main/res/drawable-nodpi/hero.png").exists(),"Orphan hero raster")
    require((ROOT/"docs/assets/hero.svg").is_file(),"Approved hero SVG missing")
    manifest=(ROOT/"android/app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    require('android:allowBackup="false"' in manifest,"Backup policy changed")
    require("android.permission.INTERNET" not in manifest,"Offline contract breached")
    # Retired language picker and full 26-locale runtime catalog have no
    # reachable UI. Do not delete legacy save, score or import/export contracts.
    require("GameLanguages" not in joined,"Dead language picker catalog still compiled")
    game=(CODE/"GameView.kt").read_text(encoding="utf-8")
    activity=(CODE/"MainActivity.kt").read_text(encoding="utf-8")
    store=(CODE/"ProgressStore.kt").read_text(encoding="utf-8")
    require('"BODOVI"' in game and '"HRANA"' in game,
            "Croatian gameplay HUD missing")
    require('contentDescription="Povratak na početnu"' in activity,
            "Accessible native Back button missing")
    require('fun importJson(contents: String)' in store and
            'fun exportJson()' in store and
            'endless_food_best' in store, "Historic save/record migration damaged")
    require('drawRootedCliffBases(' in joined,"Terrain renderer disconnected")
    print(f"PASS: {len(SOURCES)} Kotlin sources, 13 offline sounds, Croatian-only UI, Android-only")
if __name__=="__main__":main()
