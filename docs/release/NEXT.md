# Next Android release

## v1.0.32 — Large-text Options accessibility and real emulator touch geometry (build 162)

- **Measured native settings controls:** Android 15 opens the actual Options dialog and checks all three difficulty RadioButtons, all three setting Switches, two audio sliders and the Done button. Every control must expose a fully visible, clickable, correctly sized UIAutomator rectangle.
- **Font and screen-size matrix:** test font scales 1.0, 1.3 and 1.5 at 720×1280 / 320 dpi, plus 1.5 at 1080×2400 / 420 dpi. Dynamically restart the Activity for each setting and inspect the real Android hierarchy after scrolling.
- **No clipped configuration options:** settings may scroll when enlarged, but all nine interactive controls must be independently reachable without shrinking touch targets. Validate the actual Done dismissal, screenshots, screen bounds and emulator process stability.
- **Failure isolation:** every QA execution restores the original Android font scale, display size and display density even on an assertion failure. New unit regressions reject under-height, clipped, offscreen and non-interactive controls.
- **Unchanged gameplay, art and privacy:** leave the Bopi/Sova/Lisac artwork, adaptive launcher, game physics, original screens, audio, save keys, offline design, nine release screenshots and original security posture unchanged.

**Release gate:** Android lint, Kotlin/JVM, original art checks, privacy/source tests, real Android 15 gameplay/large-text QA, Android 16 smoke, authentic screenshots and SHA-256 integrity must all pass before merging and publishing. Test coverage on emulators does not establish literal 1:1 identity with unavailable owner reference screenshots or physical-device certification. Debug APK is installable; release APK and AAB remain unsigned for private publishing.
