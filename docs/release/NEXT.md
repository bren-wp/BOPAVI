# Next Android release

## v1.0.51 — Honest splash progress and seated Game Over Bopi (build 181)

**Candidate only — NOT a published, verified or signed release.**

This change continues the existing merged v1.0.50 (build 180) Android game; it does not restart the project.

- Splash/Loading: during asynchronous level preparation, replace the 61–68% apparent prefilled progress bar with an indeterminate cyan scan; fill the blue enamel track only on the real preparation completion event. Keep the native offline artwork and loading accessibility state.
- Game Over: use a newly drawn local, noninteractive **sad seated classic Bopi** illustration instead of the flying portrait for the original blue aviator. Preserve the currently selected alternate-pilot portraits until dedicated approved seated images exist.
- Keep real Retry/Home click listeners and accessibility targets, true run stats, save data, gameplay physics, collision geometry, music and existing world progression unchanged.
- Add deterministic JVM tests for loading scan bounds, movement and completion, and source assertions for the seated Bopi route.

**Pixel QA:** genuine v1.0.50 941×1672 emulator screenshots, compared with the owner's original PNGs, show whole-screen RGB MAE of Loading 78.45, Home 72.54, Gameplay 75.77 and Game Over 77.09. This candidate is **NOT** visually approved as 1:1; rerun native screenshot comparisons before considering it complete.

**Verification required before publication:** every mandatory PR check, Android 15 Gameplay QA with first flap/background/rotation, Android 16 API36 smoke, Android APK+AAB builds, original 941×1672 screenshots and source/security audits must succeed. Inspect screenshots before merging. Never suppress tests to publish.

**Build 181 distribution:** Debug APK is for QA only. Release APK and AAB produced in GitHub CI remain unsigned; Google Play upload signing must be performed privately with the publisher's authorized upload key. Never label the unsigned AAB as Play-ready.
