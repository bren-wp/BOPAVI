# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.14 — Safe-area premium flight controls and efficient rock physics (build 144)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.14)

- **Premium HUD usability:** dynamically position the native pause button below the score, fruit and three-heart cards using the identical 480×800/480×640 viewport scale applied by the HUD. Respect physical display-cutout and status-bar safe insets on Android 28+ and recalculate margins after split-screen/viewport resizing.
- **Accessible controls:** the existing 56dp native pause button retains its touch target, visible labels and one-touch resume behavior. Its position no longer hides or blocks the lives card on compact phones and tablets.
- **Physics efficiency:** for the endless game, avoid computing dynamic gate openings and continuous swept-collision geometry when the obstacle's painted lip is still beyond the bird's 18px contact radius. The original 115px look-ahead remains for legacy finite levels with a larger magnet pickup range.
- **Gameplay fidelity:** retain unchanged flap impulse, gravity, swept cap collision, real berry pickup radius, three lives, scoring, inter-level cadence, 120Hz physics slices and battery-aware redraw behavior.
- **Maintainability:** put Android-independent overlay placement rules into `FlightOverlayLayout.kt` and add deterministic JVM tests for sizes 320–840dp and real cutout safe insets, plus rock/berry/magnet gameplay regression tests and a CI source contract.
- **Distribution:** stage Play-ready unsigned AAB (publisher signs offline), separately signable unsigned APK, QA debug APK, Croatian F-Droid submission archive with pinned commit, original screenshots and SHA256SUMS.txt. Published F-Droid inclusion requires external approval; never publish signing secrets.
- **Release gate:** pass privacy/dead-code audits, Android Lint and Kotlin/JVM tests, Android 15/16 emulator QA and Play listing screenshot checks before merging and letting the main branch create/tag the new GitHub Release. No device-measured frame-rate or battery gain is claimed.

## v1.0.13 — Premium HUD, smarter battery saving and physics parity (build 143)

**Published:** 2026-10-21 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.13)

- **Battery and frame pacing:** native Android OS power-saving mode now requests approximately 30 redraws per second, as does the existing Reduced motion preference, while normal motion remains synchronized to display 60/90/120Hz refresh. Battery saving is inspected during View attachment and visibility/lifecycle transitions, not through binder calls in the render loop.
- **Smooth physics:** retain 120Hz substep integration, the same -255f tap impulse, gravity, 18px body hitbox, swept obstacle collision, score rules and continuous cross-level parallax. Added deterministic tests comparing 30/60/120Hz one-second trajectories across all three difficulties.
- **Premium in-flight HUD:** lives now share a compact blue glass card aligned to the score and berries with the Croatian label **ŽIVOTI**. No wallets, development indicators or fake controls were added.
- **Truthful results:** the results hero and backdrop use the actual completed flight world's illustration and Croatian biome name instead of always showing the first world.
- **Maintainability:** the battery-aware native frame policy has been isolated in `GameFramePacing.kt` with Android-independent JVM unit tests and source audit contracts. No disruptive full rewrite or removal of compatibility code.
- **Distribution:** continue publishing the verified unsigned Google Play AAB, separately signable release APK, debug QA APK, original screenshot evidence, SHA-256 manifest and F-Droid source submission archive. Official F-Droid listing still requires fdroiddata acceptance.
- **Release gate:** Android Lint, Kotlin/JVM and Python regression tests, privacy/dead-code checks, Android 15 and 16 emulator QA, Play screenshots, verified unsigned AAB/APK, checksums and publication must pass before v1.0.13 is considered released. No measured battery or device FPS improvement is claimed.
