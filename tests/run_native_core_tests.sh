#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p tests/.build
CORE=android/app/src/main/java/com/brendigo/bopavi
kotlinc "$CORE/LevelEngine.kt" "$CORE/GameSimulation.kt" tests/KotlinParity.kt -include-runtime -d tests/.build/kotlin-tests.jar
java -jar tests/.build/kotlin-tests.jar > tests/.build/kotlin-vectors.txt
grep '^TEST|KOTLIN|OK|' tests/.build/kotlin-vectors.txt
python3 tests/test_native_files.py
echo "PASS: deterministic Android Kotlin gameplay vectors"
