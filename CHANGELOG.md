# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.17 — Accessible premium buttons and stable flight announcements (build 147)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.17)

- **Readable primary actions:** use deep-blue text, not white, on the existing gold Home/Results/Pause gradient buttons. Each endpoint of the unchanged gold gradients now exceeds the WCAG AA 4.5:1 normal-text contrast threshold. Keep native ripples, touch targets and illustration assets intact.
- **Clearer spoken flight UI:** include the actual selected pilot, flap gesture, score, collected food and lives in the native TalkBack label. Each tap reuses the most recent HUD accessibility label instead of overwriting it with a bare pilot name.
- **Efficient updates:** compose the full HUD description only when a score, berry count or life count changes. The fast render loop still reuses the cached label; the first active tap gets a short localized fallback hint.
- **Regression protection:** check real primary-button foreground and gradient-endpoint contrast mathematically in a new CI source contract; verify HUD announcements keep control instructions across taps.
- **Safety:** physics, collision geometry, level streaming, scoring, persistent data, audio policy, pilot graphics and offline privacy behavior remain unchanged. Real-world TalkBack usability and actual battery savings still require device verification.

## v1.0.16 — Results audio lifecycle and flight accessibility (build 146)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.16)

- **Audio and power:** opening the results screen stops world music and releases its decoder and Android audio focus, while preserving the independent win/hit sound effect. Avoid needless audio work while reviewing scores or records.
- **Lifecycle:** activity foreground restoration only resumes a valid idle game-preview soundtrack, never music from a results/home screen or an already finished run. Paused active flights remain paused.
- **Accessibility:** the spoken flight HUD now retains the actual gesture instruction alongside score, berry count and remaining lives, so updating the counters cannot silently remove the TalkBack input hint.
- **Regression:** extend the source-level lifecycle contract to verify results audio teardown, scene-gated resume and flight instructions; retain the existing Kotlin physics/audio tests, Android Lint, API 35/36 emulator QA and release-integrity gates.
- **Scope:** no changes to the fixed physics, collision geometry, scoring, pilot artwork, worlds, saved progress, signing credentials or F-Droid acceptance status. CPU, battery and TalkBack behavior still require physical-device verification.
