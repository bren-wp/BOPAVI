# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.3 — Croatian UI, polished navigation and Play policy hardening (build 133)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.3)

- Use Croatian on every reachable native screen and accessibility label regardless of device locale, without erasing historical user settings or imported save keys.
- Replace the result screen's old plain Home action with a clearly styled, high-contrast, 58dp native arrow-back button and a consistent Croatian accessible description.
- Improve the Pause exit arrow; preserve game-state transitions, Back gestures, protected ongoing flight and real score persistence.
- Revalidate current Google Play API 36, truthful Croatian listing, privacy and real same-build artwork.
- Expand dead-code/resource audits and prevent orphan resources while retaining old public releases and backup migration behaviour.
- Remove unreachable 26-language UI catalog and dead helper logic from the shipped Kotlin code. Save migration and historic records stay intact.
- Extend decorative obstacle shafts into tall-screen camera overflow, eliminating disconnected/floating-looking rock ends without widening collision geometry. Refine Croatian scoreboard typography and skip safe-corridor rock CCD work.
- Add a discoverable Croatian in-app privacy notice (offline native dialog) and minimum 58dp legal tap area on Home; compact-screen art scales independently to retain readable primary buttons.
- Preserve the actively used ParallaxScenery helper and its Kotlin tests after runtime-reachability verification; it is not dead code.
- Refresh Google Play metadata, signing/privacy rules, Android 16 API 36 checks and current v1.0.2 publication evidence.
- Validate that the actual in-app Croatian privacy policy opens and closes on Android 15/16, and that its touch target remains at least 48dp on compact and tall emulator displays.

## v1.0.2 — Android audio focus, stability contracts and documentation cleanup (build 132)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.2)

- Safely handle Android soundtrack focus loss, interruptions, resume and resource teardown with no external audio SDK and no additional app permissions.
- Expand source and regression audits for Android audio lifecycle while retaining original gameplay physics, collision geometry, artwork and offline local save keys.
- Clean up active Android crash, QA, release, artwork and accessibility guidance; archive historically useful retired iOS/old UI evidence outside active documentation.
- Preserve all published stable GitHub v1+ releases and stage v1.0.2 as an unpublished Android-only candidate until the full CI and review gates pass.
- Extend deterministic Android generator regression coverage across world families, extreme level ordinals, animated mouth clearance and conservative hard-mode ascent budgets.
- Reject mislabeled Android release previews: a real GameView idle state, first active flight with visible pause control, and retry-to-idle are now verified before captures; image hash differences alone are insufficient.
- Centre the Results content vertically on tall screens and cap the Pause modal height to avoid oversized empty panels, retaining inset-safe scaling and native accessible controls.
- Fix active-flight screenshot QA false negative using real Android UIAutomator evidence: GameView replaces its initial 'Bopi is flying' label with the accessible live SCORE/FOOD HUD during rendering. Require actual HUD plus Pause, not the transient initial label.
