# BOPAVI — Production readiness, v1.0.82 candidate
Prepared on 2026-11-12; status: **NOT VERIFIED / NOT YET PUBLISHED**.

Baseline v1.0.81 was published. Proposed commit hardens ProgressStore durability and adds missing static QA coverage. Publication is conditional on a passing source audit, Gradle compilation/Lint/JVM tests, Android 15 and Android 16 emulator QA, screenshot source verification, SHA-256 checks and successful release job for the exact commit.

OPEN: actual fresh nine-screen 941x1672 RGB zero-pixel comparison, provider-level SAF backup recovery, device-level race/fault tests, physical-device FPS/battery/heap tests, complete dead-code and security audit, signing using the private release key, Play/F-Droid acceptance.

Do not label v1.0.82 stable until the GitHub release and every asset are actually verified. Do not replace or delete older releases.
