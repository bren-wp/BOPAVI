# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.34 — Active-flight rotation regression QA (build 164)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.34)

- Strengthen the Android 15 rotation test from an idle-preview check to a genuine first-flap flight check, using the existing debug-only capture gate.
- Verify that score, lives, pilot ability and the current GameView accessibility state remain identical across landscape-to-portrait rotation, and that the native Pause control stays visible and tappable within display bounds.
- Verify the same process remains alive and that the ongoing flight accepts the next genuine flap after rotation. Preserve all existing Android 15/16 UI, gameplay, large-font, R8 and release-integrity gates.
- Keep original gameplay physics, 480 × 800 logical coordinates, art, sound, offline privacy model and saved preferences unchanged. This is a verification-only source change, not a claim of a newly discovered production crash fix.

## v1.0.33 — Edge-to-edge, adaptive orientation and optimized R8 resources (build 163)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.33)

- Replace deprecated Window.setStatusBarColor and Window.setNavigationBarColor calls and theme attributes with modern inset-driven edge-to-edge on Android API 30+, including transparent navigation area, safe display-cutout padding for Home and Results, and safe HUD/Pause positioning. Preserve API 26–29 compatibility.
- Remove forced portrait orientation. Retain the current Activity/GameView across rotation with explicit orientation/screen-size configuration handling; GameView recomputes viewport camera/HUD geometry without resetting the 480x800 physics, progress or scores.
- Include real Android 15 landscape Home -> Flight -> portrait continuity QA on top of existing Android 15/16 full-flow screenshots, four font-scale variants, privacy and performance smoke.
- Upgrade to Android Gradle Plugin 9.0.1, Gradle 9.1.0 and native Kotlin integration. Enable R8 code minification and optimized resource shrinking for release APK and unsigned AAB; do not add external tracking or network dependencies.
- Preserve original artwork, music, save keys, Gameplay, Croatian UI, package/application ID and prior release artifacts. v1.0.33 is not published until Android lint, JVM, privacy/graphics audit, Android 15/16 emulators and release integrity all pass.
