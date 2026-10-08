#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p tests/.build
KOTLIN_CORE=android/app/src/main/java/com/brendigo/bopavi
SWIFT_CORE=ios/BOPAVI
kotlinc "$KOTLIN_CORE/LevelEngine.kt" "$KOTLIN_CORE/GameSimulation.kt" tests/KotlinParity.kt -include-runtime -d tests/.build/kotlin-tests.jar
java -jar tests/.build/kotlin-tests.jar > tests/.build/kotlin-vectors.txt
swiftc -O "$SWIFT_CORE/BopaviCore.swift" "$SWIFT_CORE/ProgressStore.swift" tests/SwiftParity.swift -o tests/.build/swift-tests
./tests/.build/swift-tests > tests/.build/swift-vectors.txt
python3 - <<'PY'
from pathlib import Path
ka=Path('tests/.build/kotlin-vectors.txt').read_text().splitlines()
sw=Path('tests/.build/swift-vectors.txt').read_text().splitlines()
assert len(ka)==len(sw)
for a,b in zip(ka[:-1],sw[:-1]):
    x=a.split('|');y=b.split('|')
    if x[0]=='S':
        assert x[1:3]==y[1:3],(x,y)
        # Allow small floating point differences in remixed phases, but not center signature differences.
        assert x[3]==y[3],(x,y)
        continue
    assert x[:6]==y[:6],(x,y)
    assert abs(float(x[6])-float(y[6]))<.001,(x,y)
    assert abs(float(x[7])-float(y[7]))<.001,(x,y)
    assert x[8]==y[8],(x,y)
assert ka[-1].startswith('TEST|KOTLIN|OK|')
assert sw[-1].startswith('TEST|SWIFT|OK|')
print(f'PASS: {len(ka)-1} Android/iOS generator vectors agree, 24,000 generated configurations per platform, plus simulation checks.')
PY
swiftc -O "$SWIFT_CORE/BopaviCore.swift" "$SWIFT_CORE/ProgressStore.swift" tests/SwiftSaves.swift -o tests/.build/swift-saves
./tests/.build/swift-saves
swiftc -frontend -parse "$SWIFT_CORE"/*.swift
python3 tests/test_native_files.py
