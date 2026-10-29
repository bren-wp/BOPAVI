# Next Android release

## v1.0.33 — Edge-to-edge, adaptive orientation and optimized R8 resources (build 163)

- Replace deprecated Window.setStatusBarColor and Window.setNavigationBarColor calls and theme attributes with modern inset-driven edge-to-edge on Android API 30+, including transparent navigation area, safe display-cutout padding for Home and Results, and safe HUD/Pause positioning. Preserve API 26–29 compatibility.
- Remove forced portrait orientation. Retain the current Activity/GameView across rotation with explicit orientation/screen-size configuration handling; GameView recomputes viewport camera/HUD geometry without resetting the 480x800 physics, progress or scores.
- Include real Android 15 landscape Home -> Flight -> portrait continuity QA on top of existing Android 15/16 full-flow screenshots, four font-scale variants, privacy and performance smoke.
- Upgrade to Android Gradle Plugin 9.0.1, Gradle 9.1.0 and native Kotlin integration. Enable R8 code minification and optimized resource shrinking for release APK and unsigned AAB; do not add external tracking or network dependencies.
- Preserve original artwork, music, save keys, Gameplay, Croatian UI, package/application ID and prior release artifacts. v1.0.33 is not published until Android lint, JVM, privacy/graphics audit, Android 15/16 emulators and release integrity all pass.

The repo publishes only debug APK, unsigned release APK and unsigned release AAB. Private upload-key signing and publishing on Google Play remain separate. Play Console advisory status may update after a new artifact is processed. Physical-device testing and literal 1:1 owner-reference image comparison are not claimed.
