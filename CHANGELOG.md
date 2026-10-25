# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.32 — Large-text Options accessibility and real emulator touch geometry (build 162)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.32)

- **Measured native settings controls:** Android 15 opens the actual Options dialog and checks all three difficulty RadioButtons, all three setting Switches, two audio sliders and the Done button. Every control must expose a fully visible, clickable, correctly sized UIAutomator rectangle.
- **Font and screen-size matrix:** test font scales 1.0, 1.3 and 1.5 at 720×1280 / 320 dpi, plus 1.5 at 1080×2400 / 420 dpi. Dynamically restart the Activity for each setting and inspect the real Android hierarchy after scrolling.
- **No clipped configuration options:** settings may scroll when enlarged, but all nine interactive controls must be independently reachable without shrinking touch targets. Validate the actual Done dismissal, screenshots, screen bounds and emulator process stability.
- **Failure isolation:** every QA execution restores the original Android font scale, display size and display density even on an assertion failure. New unit regressions reject under-height, clipped, offscreen and non-interactive controls.
- **Unchanged gameplay, art and privacy:** leave the Bopi/Sova/Lisac artwork, adaptive launcher, game physics, original screens, audio, save keys, offline design, nine release screenshots and original security posture unchanged.

## v1.0.31 — Full nine-screen capture, accessible spacing and original round launcher (build 161)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.31)

- **All reachable native screens:** Android 15 captures nine *actual emulator* states: Home, Options, Records, Privacy, Terms, preflight, active Flight, Pause and Results. Validate PNG headers/dimensions, distinct native dialogs, game state/action transitions and SHA-256 for every published image. No generated mock screens or reused captures.
- **Options and responsive typography:** replace fixed-height 48dp Switch rows and 45dp difficulty RadioButtons with naturally measured, min-height 64dp/52dp touch targets. Add wrap-friendly insets, maximum four-line captions and explicit TalkBack labels to prevent clipped Croatian settings on narrow/high-font-scale Android devices.
- **Home character selector:** add bounded 10–13sp automatic sizing, 32dp heading height and current pilot/ability accessibility label that updates immediately on pilot selection. Preserve three selectable original pilots, main CTA, 48dp legal links and scroll-only-when-needed layout.
- **Pause screen accessibility:** do not speak the decorative chess-queen prefix when announcing the native pause heading; preserve all Continue/Retry/Exit actions.
- **Launcher:** add an explicitly declared `roundIcon` that references the exact same owner-approved adaptive Bopi background/foreground/themed mask rather than a replacement mascot or differently sized art.
- **Visual audit and release:** release nine different original screen captures, app debug APK, unsigned release APK/AAB, F-Droid preparation bundle, five-screen Play assets and checksums for 14 distributed files (15 assets including SHA256SUMS). Older releases remain unchanged.
- **Compatibility:** original package name, offline/no-tracking privacy, original artwork, gameplay physics, collision semantics, score, saved-progress keys, animations and audio behavior remain unchanged. Android 15/16 emulator QA, source/art provenance, lint, JVM tests, privacy and dead-code audit remain mandatory.
