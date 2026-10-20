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
    dynamic={*(f"world{i}" for i in range(8)),*(f"bopi{i}" for i in range(9)),
             *(f"bopileft{i}" for i in range(9)),*(f"bopiright{i}" for i in range(9)),
             "bopiportrait0","logo","homeback"}
    require(dynamic<=referenced["drawable"],f"Missing drawable references: {dynamic-referenced['drawable']}")
    require(not (ROOT/"android/app/src/main/res/drawable-nodpi/hero.png").exists(),"Orphan hero raster")
    require((ROOT/"docs/assets/hero.svg").is_file(),"Approved hero SVG missing")
    manifest=(ROOT/"android/app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    require('android:allowBackup="false"' in manifest,"Backup policy changed")
    require("android.permission.INTERNET" not in manifest,"Offline contract breached")
    code=(CODE/"LevelEngine.kt").read_text(encoding="utf-8")
    begin='private val flightRows=listOf(';end='    ).map { it.split("|") }'
    require(begin in code and end in code,"HUD language inventory missing")
    text=code.split(begin,1)[1].split(end,1)[0]
    rows=[s.split("|") for s in re.findall(r'(?m)^\s*"([^"\r\n]+)"\s*,?\s*$',text)]
    require(len(rows)==26 and len({r[0] for r in rows})==26,"HUD locales missing")
    require(all(len(r)==9 and all(v.strip() for v in r) for r in rows),"Incomplete HUD translation")
    require("GameLanguages.hudPack" in joined and "drawRootedCliffBases(" in joined,
            "HUD or visual dependency not reached")
    print(f"PASS: {len(SOURCES)} Kotlin sources, 13 offline sounds, 26 HUD locales, Android-only")
if __name__=="__main__":main()
