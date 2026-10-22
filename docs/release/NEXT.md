# Next Android release

## v1.0.16 — Results audio lifecycle and flight accessibility (build 146)

- **Audio and power:** opening the results screen stops world music and releases its decoder and Android audio focus, while preserving the independent win/hit sound effect. Avoid needless audio work while reviewing scores or records.
- **Lifecycle:** activity foreground restoration only resumes a valid idle game-preview soundtrack, never music from a results/home screen or an already finished run. Paused active flights remain paused.
- **Accessibility:** the spoken flight HUD now retains the actual gesture instruction alongside score, berry count and remaining lives, so updating the counters cannot silently remove the TalkBack input hint.
- **Regression:** extend the source-level lifecycle contract to verify results audio teardown, scene-gated resume and flight instructions; retain the existing Kotlin physics/audio tests, Android Lint, API 35/36 emulator QA and release-integrity gates.
- **Scope:** no changes to the fixed physics, collision geometry, scoring, pilot artwork, worlds, saved progress, signing credentials or F-Droid acceptance status. CPU, battery and TalkBack behavior still require physical-device verification.
