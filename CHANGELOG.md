# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.33 — Edge-to-edge, adaptive orientation and optimized R8 resources (build 163)

**Published:** 2026-10-29 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.33)

- Replace deprecated Window.setStatusBarColor and Window.setNavigationBarColor calls and theme attributes with modern inset-driven edge-to-edge on Android API 30+, including transparent navigation area, safe display-cutout padding for Home and Results, and safe HUD/Pause positioning. Preserve API 26–29 compatibility.
- Remove forced portrait orientation. Retain the current Activity/GameView across rotation with explicit orientation/screen-size configuration handling; GameView recomputes viewport camera/HUD geometry without resetting the 480x800 physics, progress or scores.
- Include real Android 15 landscape Home -> Flight -> portrait continuity QA on top of existing Android 15/16 full-flow screenshots, four font-scale variants, privacy and performance smoke.
- Upgrade to Android Gradle Plugin 9.0.1, Gradle 9.1.0 and native Kotlin integration. Enable R8 code minification and optimized resource shrinking for release APK and unsigned AAB; do not add external tracking or network dependencies.
- Preserve original artwork, music, save keys, Gameplay, Croatian UI, package/application ID and prior release artifacts. v1.0.33 is not published until Android lint, JVM, privacy/graphics audit, Android 15/16 emulators and release integrity all pass.

## v1.0.32 — Large-text Options accessibility and real emulator touch geometry (build 162)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.32)

- **Measured native settings controls:** Android 15 opens the actual Options dialog and checks all three difficulty RadioButtons, all three setting Switches, two audio sliders and the Done button. Every control must expose a fully visible, clickable, correctly sized UIAutomator rectangle.
- **Font and screen-size matrix:** test font scales 1.0, 1.3 and 1.5 at 720×1280 / 320 dpi, plus 1.5 at 1080×2400 / 420 dpi. Dynamically restart the Activity for each setting and inspect the real Android hierarchy after scrolling.
- **No clipped configuration options:** settings may scroll when enlarged, but all nine interactive controls must be independently reachable without shrinking touch targets. Validate the actual Done dismissal, screenshots, screen bounds and emulator process stability.
- **Failure isolation:** every QA execution restores the original Android font scale, display size and display density even on an assertion failure. New unit regressions reject under-height, clipped, offscreen and non-interactive controls.
- **Unchanged gameplay, art and privacy:** leave the Bopi/Sova/Lisac artwork, adaptive launcher, game physics, original screens, audio, save keys, offline design, nine release screenshots and original security posture unchanged.
