# BOPAVI — Android QA standard

Current application: native Kotlin, `com.brendigo.bopavi`. The only
reachable game screens are Home, Flight, Pause and Results. The detailed
Android test and release contracts live in
[docs/qa](docs/qa/), [Android architecture](docs/android/ARCHITECTURE.md)
and [Android release checklist](docs/play/RELEASE-CHECKLIST.md).

## Required before merging or releasing

- Build Kotlin application and test suites with Gradle; run Android Lint.
- Execute deterministic 30/60/90/120Hz flight and continuous collision tests.
- Exercise near-miss/cap contact, one-time berry collection, scoring and retry.
- Verify Home → Flight → Pause/Resume → Results → Replay/Home on Android emulators.
- Inspect real screenshots for clipped text, spacing, display cutouts and touch targets.
- Confirm background/foreground playback, audio-focus interruptions, rapid retries.
- Check SDK 35/36 emulators, then representative physical phones, tablets only if supported.
- Measure real hardware memory use, frame pacing, battery and crashes before certification.
- Review offline permissions, save compatibility, resource integrity and signing.
- Check GitHub Actions for the exact commit, no skipped required jobs, accurate APK/AAB artifacts.
- Do not publish until the only working PR is complete, code-reviewed and merged.

Historical test instructions for removed UI and discontinued platforms are
retained in [docs/archive](docs/archive/) as evidence, **not instructions**.
A green emulator build does not certify pixel-perfect reference parity or
Google Play approval.
