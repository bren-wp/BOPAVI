# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.42 — Home wing motion and 3D control states (build 172)

**Published:** 2026-11-01 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.42)

- The large 941×1672 native Home now reuses its actual transparent left-wing, right-wing and body sprite resources in a lifecycle-aware `HomeWingActor`. The subtle native wing flaps stop when the window becomes invisible or is detached and respect disabled system animator motion. The central body stays fixed; gameplay physics and saved state are unchanged.
- The shared local enamel drawable used on Home, other menus and Pause now reacts to real Android pressed, selected and disabled states. Pressed buttons have reduced extrusion; selected surfaces gain a restrained reflective overlay; disabled level-map actions are visibly dimmed without becoming clickable.
- Retain native `HomeEmbossedLabel` TextView accessibility and actual wallet values. Add source regression checks for wing layer ordering, animator cancellation, Android drawable state propagation and 3D menu reuse.
- **Visual QA remaining:** actual owner reference rasters are not stored in the repository. Pixel-by-pixel matching against the original 941×1672 concept images requires the supplied originals and real emulator screenshots; code contracts cannot certify an exact visual match.
- **Release guard:** No merge, signature or publication requested. CI must pass Android 15/16 emulator navigation, Gradle unit/lint/build, asset audit and source contracts before review.
- Measured the owner's newly supplied 941×1672 Home composition against real API35 screenshot artifacts from the last green main workflow. The old three-card row plus two-card footer was inconsistent with the reference. Native layout now uses two horizontal SKINS/SHOP buttons, one wide SETTINGS button, and independent circular ACHIEVEMENTS/LEADERBOARD/SOUND targets on the bottom row. The saved coin and star wallet chips move to the top-right placement from the reference; no fake gem inventory, prices or content added. Updated Kotlin geometry and Python source regression tests. Exact artwork identity remains unverified.
- Replaced the prior three tall Skins columns with the owner-reference hierarchy: selected pilot's large genuine sprite, raised platform, dynamically named plaque, three accessible native character selection cards, and a saved-pilot ability panel. The reference depicts four ornamental character skins, but only three are real playable arcade pilots in this build; no fourth fake locked purchase or currency was created. Character choice remains persisted through the existing onChoose navigation callback.

## v1.0.41 — Splash and Home 3D visual polish (build 171)

**Published:** 2026-11-01 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.41)

- Compose an illustrated native Splash at cold launch and maintain the existing truthful, cancellable offline level-preparation screen. The short introductory display is not a network progress claim and must never consume player inventory.
- Align the 941×1672 Home artwork to the owner's supplied layout: left coin balance, right star total, winged logo, large flying pilot and Play CTA, with Skins/Shop/Settings above Achievements/Sound.
- Render glossy native Canvas 3D buttons with light rims, specular reflections, recessed extrusions, drop shadows and tactile press feedback. All elements remain accessible Android views with real navigation actions and no clickable flat screenshot.
- Keep the Leaderboard accessible via a small labeled records badge within the Achievements panel. Keep actual locally persisted balances and selected pilot skin, not the example reference numbers.
- Recompose all eleven selected BOPAVI pilots from their original transparent body and wing sprites. Add the native feather-jersey Skins emblem, feather coin and standalone gold star artwork.
- Replace the previous oversized golden loading track with a slim blue/cyan glass bar and move the Loading caption above it. The animated fill remains indeterminate until real preparation completes.
- Extend the native dimensional enamel palette to Skins, Shop, Level Map, Settings, Achievements, Leaderboard, Game Over and Level Complete menus, including their real action buttons and earned-result cards.
- Apply the same dimensional palette to pause/navigation controls, with subtle highlights on the genuine in-flight lives HUD. Physics, collision boxes, saved progress and offline-only payment logic remain unchanged.
- Update Android Kotlin geometry tests and Python source contracts. Continue API35 emulator navigation/viewport QA, API36 smoke tests, privacy/dead-code audit and release packaging gates.
- **Visual acceptance:** The implementation has not yet been shown to be pixel-for-pixel identical to the supplied raster images; compare genuine emulator screenshots against original 941×1672 files before any 1:1 statement.
