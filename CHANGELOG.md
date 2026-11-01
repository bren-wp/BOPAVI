# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

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

## v1.0.40 — Gameplay reference pass (build 170)

**Published:** 2026-10-31 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.40)

- Replace the former centered textual HUD with a **top-left glass flight badge** combining a real selected pilot portrait and up to three larger red hearts, following the supplied gameplay board. Keep the true single-life rating for finite levels, three lives for endless, and TalkBack's live localized life count. No scoreboard, coin counter or pause HUD.
- Draw the **real** pre-existing finite-level reward pickups that were previously invisible: glossy gold crown coins at physical gate centre, sapphire crystals (the original stage-star reward) at centre +35/-25 and shield/magnet icons at +39/+39. Visibility checks use the simulation's existing collected arrays and show nothing after pickup; endless berries and restorative hearts retain their original rules.
- Add dark-metal/gold/sapphire cap hardware fully inside the original physical cliff caps, so the visual danger matches real body/lip collisions without fictitious swinging hazards.
- Reuse Android Canvas Paint/Path/bitmap assets; no network requests, per-frame bitmap creation, changed physics or new saved-data keys.
- Add JVM tests for finite/endless pickup visibility and update HUD geometry contracts for left positioning and viewport containment.
- Retain harder pillar widths/gaps, Gradle 9.6.0, AGP 9.5.0, API36, Android 15/16 QA, visual screenshots and signed-publication restrictions.
- Later Sky Meadows gates (indices 4, 7, 10, …) now alternate an upper/lower moving mace, while introductory gates remain clear. The native Canvas render and the 120 Hz swept contact share `SkyMaceHazard` positions/radius; both finite and endless modes use the same physical rules.
- A worst-case hard-mode center lane remains open. Added JVM tests for spawn eligibility, exact rendered-pose contact, mid-step crossing and safe centerline at several generated level seeds.
- The new graphic is an interim local rendering, **not** a pixel-perfect recreation of the larger reference chain/mace artwork. Android emulator screenshots, gameplay replay/fairness review and PR CI are required before production or a 1:1 claim.
- Compared the actual GitHub Actions Android flight screenshot (1080×2400; run 41126993167) against the supplied flight artwork (941×1672). The screenshot showed the avatar/three-heart card much wider and significantly lower than the reference.
- Reduced HUD layout from 232×68 to 163×51 logical units, adjusted avatar/heart spacing and badge enamel treatment, and removed the non-reference life underline. **No fake extra lives** were added: finite mode still shows one true life, endless three.
- Corrected centered punch-hole handling: the left-aligned HUD is displaced for a real cutout that overlaps its x-range, but not for a center cutout that is entirely outside the badge. Top inset is now cached at the WindowInsets lifecycle boundary, not polled every frame.
