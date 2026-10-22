# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.16 — Results audio lifecycle and flight accessibility (build 146)

**Published:** 2026-10-22 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.16)

- **Audio and power:** opening the results screen stops world music and releases its decoder and Android audio focus, while preserving the independent win/hit sound effect. Avoid needless audio work while reviewing scores or records.
- **Lifecycle:** activity foreground restoration only resumes a valid idle game-preview soundtrack, never music from a results/home screen or an already finished run. Paused active flights remain paused.
- **Accessibility:** the spoken flight HUD now retains the actual gesture instruction alongside score, berry count and remaining lives, so updating the counters cannot silently remove the TalkBack input hint.
- **Regression:** extend the source-level lifecycle contract to verify results audio teardown, scene-gated resume and flight instructions; retain the existing Kotlin physics/audio tests, Android Lint, API 35/36 emulator QA and release-integrity gates.
- **Scope:** no changes to the fixed physics, collision geometry, scoring, pilot artwork, worlds, saved progress, signing credentials or F-Droid acceptance status. CPU, battery and TalkBack behavior still require physical-device verification.

## v1.0.15 — True music mute, audio-focus recovery and lower resource use (build 145)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.15)

- **Real 0% background-music mute:** muting the music slider pauses the soundtrack and promptly releases any held or pending Android audio-focus request. BOPAVI never plays a silent looping soundtrack or retains music focus just because the game is open.
- **Lazy audio decoding:** starting a world while music is muted no longer allocates a MediaPlayer. The local soundtrack decoder is initialized only when that active world actually needs audible music; independently enabled game sound effects continue to work.
- **Reliable unmute:** increasing volume from 0% in the same active game reuses the selected world and requests audio focus again before resuming. If focus is delayed, playback waits for a genuine gain event. Paused/backgrounded games do not restart sound merely because the slider changes.
- **Interruption safety:** delayed focus callbacks cannot revive music after the user muted it or the Activity released its focus. The existing permanent/transient loss handling and teardown remain intact.
- **Defensive mixer state:** unsupported NaN/Infinity input is normalized to silence; playable music and effects volumes remain clamped to 0–100% without changing saved user settings, the nine skins or game physics.
- **Maintainability:** extract Android-independent `MusicPlaybackPolicy.kt`, add focused Kotlin/JVM tests and a CI contract on the actual AudioManager/MediaPlayer integration. Keep the original eight offline soundtracks and sound-effect resources.
- **Distribution:** publish the existing ten-file Android release bundle: QA debug APK, privately signable unsigned release APK, unsigned Google Play AAB, four genuine screenshots, commit-pinned F-Droid submission ZIP, Play listing ZIP and SHA256SUMS.txt. No signing secrets are introduced.
- **Release gate:** require source/security/dead-code audit, Android Lint/JVM, Android 15 gameplay and Android 16 navigation emulator checks, authentic screenshots, APK/AAB signature checks, F-Droid source package and published SHA-256 integrity. Actual battery improvement and official F-Droid publication remain unverified until independently measured/approved.
