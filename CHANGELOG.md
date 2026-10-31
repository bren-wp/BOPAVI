# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

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

## v1.0.39 — Native reference Loading screen (build 169)

**Published:** 2026-10-30 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.39)

- Add native Canvas-based `FlightLoadingView` for the genuine flight preparation step, using the original locally bundled BOPAVI logo, selected bird and fantasy-island sky.
- Reconstruct the large center hero, decorative collectible coins, golden winged frame, cyan progress track, and accessible native loading label. Fit uniformly on tall/compact devices with a center-cropped backdrop; no external service or fake wallet.
- Only mark the track complete after deterministic level generation succeeds; do not fabricate intermediate percentages, add an arbitrary timer, or display a second OS startup splash.
- Generate levels asynchronously before entering the existing GameView, with generation-token cancellation if Back returns Home, process is destroyed or the Activity loses focus. Consume purchased run perks only after successful loading; existing save keys and flight physics are untouched.
- Add an explicit emulator-only loading preview intent for reliable screenshot and accessibility QA; the real loading view uses the same code.
- Keep Gradle 9.6.0, Android Gradle Plugin 9.5.0, target API 36; verify Android 15/16 device QA, native Lint, tests, dead-code audit, and release checks.
