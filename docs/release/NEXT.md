# Next Android release

## v1.0.14 — Safe-area premium flight controls and efficient rock physics (build 144)

- **Premium HUD usability:** dynamically position the native pause button below the score, fruit and three-heart cards using the identical 480×800/480×640 viewport scale applied by the HUD. Respect physical display-cutout and status-bar safe insets on Android 28+ and recalculate margins after split-screen/viewport resizing.
- **Accessible controls:** the existing 56dp native pause button retains its touch target, visible labels and one-touch resume behavior. Its position no longer hides or blocks the lives card on compact phones and tablets.
- **Physics efficiency:** for the endless game, avoid computing dynamic gate openings and continuous swept-collision geometry when the obstacle's painted lip is still beyond the bird's 18px contact radius. The original 115px look-ahead remains for legacy finite levels with a larger magnet pickup range.
- **Gameplay fidelity:** retain unchanged flap impulse, gravity, swept cap collision, real berry pickup radius, three lives, scoring, inter-level cadence, 120Hz physics slices and battery-aware redraw behavior.
- **Maintainability:** put Android-independent overlay placement rules into `FlightOverlayLayout.kt` and add deterministic JVM tests for sizes 320–840dp and real cutout safe insets, plus rock/berry/magnet gameplay regression tests and a CI source contract.
- **Distribution:** stage Play-ready unsigned AAB (publisher signs offline), separately signable unsigned APK, QA debug APK, Croatian F-Droid submission archive with pinned commit, original screenshots and SHA256SUMS.txt. Published F-Droid inclusion requires external approval; never publish signing secrets.
- **Release gate:** pass privacy/dead-code audits, Android Lint and Kotlin/JVM tests, Android 15/16 emulator QA and Play listing screenshot checks before merging and letting the main branch create/tag the new GitHub Release. No device-measured frame-rate or battery gain is claimed.
