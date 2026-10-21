# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

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

## v1.0.14 — Safe-area premium flight controls and efficient rock physics (build 144)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.14)

- **Premium HUD usability:** dynamically position the native pause button below the score, fruit and three-heart cards using the identical 480×800/480×640 viewport scale applied by the HUD. Respect physical display-cutout and status-bar safe insets on Android 28+ and recalculate margins after split-screen/viewport resizing.
- **Accessible controls:** the existing 56dp native pause button retains its touch target, visible labels and one-touch resume behavior. Its position no longer hides or blocks the lives card on compact phones and tablets.
- **Physics efficiency:** for the endless game, avoid computing dynamic gate openings and continuous swept-collision geometry when the obstacle's painted lip is still beyond the bird's 18px contact radius. The original 115px look-ahead remains for legacy finite levels with a larger magnet pickup range.
- **Gameplay fidelity:** retain unchanged flap impulse, gravity, swept cap collision, real berry pickup radius, three lives, scoring, inter-level cadence, 120Hz physics slices and battery-aware redraw behavior.
- **Maintainability:** put Android-independent overlay placement rules into `FlightOverlayLayout.kt` and add deterministic JVM tests for sizes 320–840dp and real cutout safe insets, plus rock/berry/magnet gameplay regression tests and a CI source contract.
- **Distribution:** stage Play-ready unsigned AAB (publisher signs offline), separately signable unsigned APK, QA debug APK, Croatian F-Droid submission archive with pinned commit, original screenshots and SHA256SUMS.txt. Published F-Droid inclusion requires external approval; never publish signing secrets.
- **Release gate:** pass privacy/dead-code audits, Android Lint and Kotlin/JVM tests, Android 15/16 emulator QA and Play listing screenshot checks before merging and letting the main branch create/tag the new GitHub Release. No device-measured frame-rate or battery gain is claimed.
