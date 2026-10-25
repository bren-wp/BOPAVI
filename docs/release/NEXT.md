# Next Android release

## v1.0.31 — Full nine-screen capture, accessible spacing and original round launcher (build 161)

- **All reachable native screens:** Android 15 captures nine *actual emulator* states: Home, Options, Records, Privacy, Terms, preflight, active Flight, Pause and Results. Validate PNG headers/dimensions, distinct native dialogs, game state/action transitions and SHA-256 for every published image. No generated mock screens or reused captures.
- **Options and responsive typography:** replace fixed-height 48dp Switch rows and 45dp difficulty RadioButtons with naturally measured, min-height 64dp/52dp touch targets. Add wrap-friendly insets, maximum four-line captions and explicit TalkBack labels to prevent clipped Croatian settings on narrow/high-font-scale Android devices.
- **Home character selector:** add bounded 10–13sp automatic sizing, 32dp heading height and current pilot/ability accessibility label that updates immediately on pilot selection. Preserve three selectable original pilots, main CTA, 48dp legal links and scroll-only-when-needed layout.
- **Pause screen accessibility:** do not speak the decorative chess-queen prefix when announcing the native pause heading; preserve all Continue/Retry/Exit actions.
- **Launcher:** add an explicitly declared `roundIcon` that references the exact same owner-approved adaptive Bopi background/foreground/themed mask rather than a replacement mascot or differently sized art.
- **Visual audit and release:** release nine different original screen captures, app debug APK, unsigned release APK/AAB, F-Droid preparation bundle, five-screen Play assets and checksums for 14 distributed files (15 assets including SHA256SUMS). Older releases remain unchanged.
- **Compatibility:** original package name, offline/no-tracking privacy, original artwork, gameplay physics, collision semantics, score, saved-progress keys, animations and audio behavior remain unchanged. Android 15/16 emulator QA, source/art provenance, lint, JVM tests, privacy and dead-code audit remain mandatory.

**Visual-fidelity limitation:** original owner full-screen reference PNGs at matching viewport dimensions are not present in the repository. Native screen snapshots and geometry audits verify actual UI behavior, but cannot certify a literal 1:1 pixel-by-pixel match to unavailable reference images. `tools/compare_reference_ui.py --require-exact` provides full-resolution comparison when owner originals are supplied. Android release APK/AAB remain unsigned; production signing stays private.
