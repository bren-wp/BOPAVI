#!/usr/bin/env python3
"""Traceable Android runtime dependency inventory from an actual Gradle build.

This is a lightweight *partial* SBOM, NOT a license/vulnerability clearance.
The release build's resolved dependencies and buildEnvironment logs are kept
as CI artifacts. Only actual declared and resolved versions are reported.
"""
import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
COORDINATE = re.compile(
    r"^[\s|+\\-]*---\s+([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):([^\s()]+)",
    re.MULTILINE,
)
BAD_VERSION = re.compile(r"(^|[-:.])(latest|snapshot|\+)(?=$|[-:.])", re.IGNORECASE)


def parse_runtime(text):
    if "releaseRuntimeClasspath" not in text:
        raise ValueError("Report does not contain releaseRuntimeClasspath")
    packages = {}
    for group, name, requested in COORDINATE.findall(text):
        # Gradle shows requested -> resolved; keep the actual resulting version.
        result = requested.split("->")[-1]
        line = next((x for x in text.splitlines()
                     if f"{group}:{name}:{requested}" in x), "")
        if " -> " in line:
            result = line.rsplit(" -> ", 1)[1].split()[0]
        key = (group, name)
        if key in packages and packages[key] != result:
            raise ValueError(f"Conflicting runtime versions for {group}:{name}")
        packages[key] = result
    listed = [
        {"group": group, "name": name, "version": version}
        for (group, name), version in sorted(packages.items())
    ]
    return listed


def make_report(runtime_text, gradle_build, settings_build, wrapper):
    packages = parse_runtime(runtime_text)
    fixed_agp = re.search(
        r'id\("com\.android\.application"\)\s+version\s+"([^"]+)"',
        gradle_build,
    )
    wrapper_version = re.search(r"gradle-([\d.]+)-bin\.zip", wrapper)
    if not fixed_agp or not wrapper_version:
        raise ValueError("Android Gradle Plugin or Gradle wrapper is not pinned")
    if "mavenCentral()" not in settings_build or "google()" not in settings_build:
        raise ValueError("Expected explicit Gradle dependency repositories")
    unpinned = [f"{p['group']}:{p['name']}:{p['version']}"
                for p in packages if not p["version"] or
                BAD_VERSION.search(p["version"])]
    if unpinned:
        raise ValueError("Unpinned resolved dependency versions: " + ", ".join(unpinned))
    return {
        "scope": "actual Gradle releaseRuntimeClasspath; not a complete CycloneDX/SPDX SBOM",
        "agp_version": fixed_agp.group(1),
        "gradle_version": wrapper_version.group(1),
        "runtime_dependency_count": len(packages),
        "runtime_dependencies": packages,
        "requires_external_vulnerability_review": True,
        "requires_per_file_license_review": True,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--runtime", required=True, type=Path)
    parser.add_argument("--json-out", required=True, type=Path)
    args = parser.parse_args()
    result = make_report(
        args.runtime.read_text(encoding="utf-8"),
        (ROOT / "android/build.gradle.kts").read_text(encoding="utf-8"),
        (ROOT / "android/settings.gradle.kts").read_text(encoding="utf-8"),
        (ROOT / "android/gradle/wrapper/gradle-wrapper.properties").read_text(encoding="utf-8"),
    )
    args.json_out.parent.mkdir(parents=True, exist_ok=True)
    args.json_out.write_text(json.dumps(result, indent=2, ensure_ascii=False) + "\n",
                             encoding="utf-8")
    print(f'PASS: {result["runtime_dependency_count"]} resolved runtime modules, '
          'pinned Gradle/AGP; license/CVE eligibility NOT certified')


if __name__ == "__main__":
    main()
