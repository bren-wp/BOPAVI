# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.31 — Full nine-screen capture, accessible spacing and original round launcher (build 161)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.31)

- **All reachable native screens:** Android 15 captures nine *actual emulator* states: Home, Options, Records, Privacy, Terms, preflight, active Flight, Pause and Results. Validate PNG headers/dimensions, distinct native dialogs, game state/action transitions and SHA-256 for every published image. No generated mock screens or reused captures.
- **Options and responsive typography:** replace fixed-height 48dp Switch rows and 45dp difficulty RadioButtons with naturally measured, min-height 64dp/52dp touch targets. Add wrap-friendly insets, maximum four-line captions and explicit TalkBack labels to prevent clipped Croatian settings on narrow/high-font-scale Android devices.
- **Home character selector:** add bounded 10–13sp automatic sizing, 32dp heading height and current pilot/ability accessibility label that updates immediately on pilot selection. Preserve three selectable original pilots, main CTA, 48dp legal links and scroll-only-when-needed layout.
- **Pause screen accessibility:** do not speak the decorative chess-queen prefix when announcing the native pause heading; preserve all Continue/Retry/Exit actions.
- **Launcher:** add an explicitly declared `roundIcon` that references the exact same owner-approved adaptive Bopi background/foreground/themed mask rather than a replacement mascot or differently sized art.
- **Visual audit and release:** release nine different original screen captures, app debug APK, unsigned release APK/AAB, F-Droid preparation bundle, five-screen Play assets and checksums for 14 distributed files (15 assets including SHA256SUMS). Older releases remain unchanged.
- **Compatibility:** original package name, offline/no-tracking privacy, original artwork, gameplay physics, collision semantics, score, saved-progress keys, animations and audio behavior remain unchanged. Android 15/16 emulator QA, source/art provenance, lint, JVM tests, privacy and dead-code audit remain mandatory.

## v1.0.30 — Uniform native icons, Records/Results polish and touch accessibility (build 160)

**Published:** 2026-10-25 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.30)

- **Results screen:** replace platform-dependent star and strawberry emoji in personal-best summary with reusable antialiased BOPAVI STAR and BERRY pictograms, preserving genuine persisted best points and food.
- **Records dialog:** show a consistent native icon beside each of five real offline statistics (best points, food, obstacles, flights and flaps). Add new native GATE, FLIGHT and WING drawings; each row has one complete, spoken TalkBack label and value rather than a decorative emoji glyph.
- **Options and Records headers:** use consistent native SETTINGS/RECORDS pictograms; avoid emoji glyphs that change with device and font.
- **Pause:** use the actual PAUSE pictogram (not a trophy icon); improve accessible resume action label to "Nastavi let".
- **Buttons and legal links:** add bounded native ripple feedback and keep explicit 48dp clickable areas, real native input targets and Croatian accessibility descriptions.
- **Native icon correctness:** correctly compose red berry opacity and green foliage alpha with Drawable alpha; preserve colour filter and original approved Bopi launcher artwork, adaptive icon mask and three pilots.
- **QA:** update icon, layout and options regression contracts, add a cross-screen native stats accessibility contract; run full Android lint/unit/gameplay/visual QA on Android 15/16, generate five authentic app screenshots and preserve signed-key policy.
- **Compatibility:** retain original saved progress keys, score logic, collision physics, package name and all existing offline privacy/security behavior.
