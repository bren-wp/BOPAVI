#!/usr/bin/env python3
"""Unit-only parser probes; real Gradle inventory comes from CI, not fixtures."""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from audit_android_dependency_report import make_report, parse_runtime

runtime = """
releaseRuntimeClasspath - Runtime classpath of compilation 'release'
+--- org.jetbrains.kotlin:kotlin-stdlib:2.1.0 -> 2.2.0
|    +--- org.jetbrains:annotations:24.0.1
\\--- androidx.test:core:1.6.1
"""
gradle = 'plugins { id("com.android.application") version "9.5.0" apply false }'
settings = "repositories { google(); mavenCentral() }"
wrapper = "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.6.0-bin.zip"
parsed = parse_runtime(runtime)
assert len(parsed) == 3, parsed
assert any(p["name"] == "kotlin-stdlib" and p["version"] == "2.2.0" for p in parsed)
report = make_report(runtime, gradle, settings, wrapper)
assert report["agp_version"] == "9.5.0"
assert report["gradle_version"] == "9.6.0"
assert report["requires_per_file_license_review"]
assert report["requires_external_vulnerability_review"]
for bad in (runtime.replace("2.2.0", "latest.release"), "wrong configuration"):
    try:
        make_report(bad, gradle, settings, wrapper)
    except ValueError:
        pass
    else:
        raise AssertionError("Missing or unpinned runtime report must fail")
print("PASS: actual Gradle inventory parser, pinned AGP/Gradle, supply-chain caveats")
