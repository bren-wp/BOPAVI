# Next Android release — pending CI and visual acceptance

## v1.0.41 — Splash and Home 3D visual polish (build 171)

**Pending; not published.** This entry documents proposed changes only. Retain published v1.0.40 and all earlier releases.

- Compose an illustrated native Splash at cold launch and maintain the existing truthful, cancellable offline level-preparation screen. The short introductory display is not a network progress claim and must never consume player inventory.
- Align the 941×1672 Home artwork to the owner's supplied layout: left coin balance, right star total, winged logo, large flying pilot and Play CTA, with Skins/Shop/Settings above Achievements/Sound.
- Render glossy native Canvas 3D buttons with light rims, specular reflections, recessed extrusions, drop shadows and tactile press feedback. All elements remain accessible Android views with real navigation actions and no clickable flat screenshot.
- Keep the Leaderboard accessible via a small labeled records badge within the Achievements panel. Keep actual locally persisted balances and selected pilot skin, not the example reference numbers.
- Recompose all eleven selected BOPAVI pilots from their original transparent body and wing sprites. Add the native feather-jersey Skins emblem, feather coin and standalone gold star artwork.
- Replace the previous oversized golden loading track with a slim blue/cyan glass bar and move the Loading caption above it. The animated fill remains indeterminate until real preparation completes.
- Update Android Kotlin geometry tests and Python source contracts. Continue API35 emulator navigation/viewport QA, API36 smoke tests, privacy/dead-code audit and release packaging gates.
- **Visual acceptance:** The implementation has not yet been shown to be pixel-for-pixel identical to the supplied raster images; compare genuine emulator screenshots against original 941×1672 files before any 1:1 statement.

**Distribution:** A debug APK is for testing. Release APK and Google Play AAB remain unsigned until signed privately using an authorized existing key. Do not automatically merge or publish before the complete CI and visual review.
