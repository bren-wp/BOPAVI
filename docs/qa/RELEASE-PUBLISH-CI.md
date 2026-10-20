# BOPAVI — Android-only CI, release integrity and retention

GitHub Actions lives in .github/workflows/native-ci.yml. Active pipeline:

- Android source, privacy, asset and dead-code regression checks
- Original approved Bopi image/audio production, provenance and integrity tests
- Kotlin compilation, Android Lint and JVM gameplay/physics/collision tests
- Android debug APK and unsigned Android release AAB
- Android 15 gameplay/emulator UI/collision screenshot QA
- Android 16 emulator smoke QA and screenshot-based Play preview packaging
- Android-only SHA-256 manifest, release asset verification and publishing
- Published-release documentation sync and guarded pre-v1 legacy retention

PRs **never publish releases**; all required Android checks must pass before
merging. On successful main publication, the documentation updater reads
actual published GitHub release records and regenerates the two newest stable
versions; a subsequent docs-only commit does not trigger another release.
Publishing a product commit under an existing version tag is blocked.

Public GitHub v1.0.0 and later releases are immutable and never candidates
for cleanup. A separate v0.x legacy-retention script is fail-closed and
requires checksums and an existing successful newly published version.

Release binaries are unsigned by default. Play Console signing, approval,
physical-device testing and source-reference pixel parity are distinct gates
that the build workflow does not certify.
