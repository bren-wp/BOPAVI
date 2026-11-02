# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.47 — Reference-aligned Home 3D sizing and medal art (build 177)

**Published:** 2026-11-02 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.47)

- Make the Play, Skins, Shop and Settings icon/text layout proportional to their native 941×1672 reference bounds rather than relying on density-multiplied fixed `dp` widths and margins.
- Upgrade the three Home footer icon plates to locally drawn circular blue/gold 3D medallions with a stepped navy rim, extruded gold bezel, curved white highlight and pressed/disabled states.
- Keep the original local fantasy backdrop, selected character, real navigation, balances, offline save and accessibility.
- Add explicit source regression checks for visual geometry and maintain existing reference-layout and widget tests.
- Gameplay follow-up: add physical metallic highlights to genuine gold crown coin pickups and existing spiked-mace geometry without affecting collision radius, pickup state, frame allocations, difficulty or saved progress.
- Level Map follow-up: use three nested, real enamel layers for blue, golden frontier and disabled stone stage markers, preserving actual locks and clickable targets.
- Skins follow-up: replace the reference-sized face-only hero with the exact same registered transparent whole-body and animated wing layers used in Home/Flight; keep small portraits only in genuine character selection cards.
- Shop follow-up: replace flat shield/magnet item pictograms with original local, raised sapphire/gold and enamel-red vector items. Featured icon follows the real item type rather than always displaying a magnet.
- Game Over follow-up: replace flat result pictograms with vector raised location pin and berry designs plus existing 3D coin/star medals, retaining real distance and collectible values.
- Home, Gameplay and Game Over are the only screens in this dedicated comparison pass. Game Over Retry/Home now have physical 3D icons and embossed labels, result-row column widths follow design-space proportions, and Gameplay star pickups use the purple faceted crystal palette visible in the reference while preserving real saved stars.
- Current focused four-screen pass: cold-start Splash lasts 2450 ms (not an artificial loading delay), a stable blue loading track, identical illustrated "Tap to fly" cue in all locales with Croatian accessibility, a dollar-faced Home gold coin, a crown-faced Game Over collectible, and a contact shadow under the result character.
- Home's purple crystal icon and Game Over's third-row crystal now match the provided reference while still displaying only genuinely earned star values; no new gem balance or payment system was introduced.

## v1.0.46 — Splash/Home 3D UI and pilot consistency (build 176)

**Published:** 2026-11-02 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.46)

- Keep Splash, Home and gameplay character selection consistent through one canonical pilot/sprite mapping (Bopi 0, Sova 9, Lisac 10).
- Improve the reference Home's raised 3D button depth, rounded enamel shape, clipped specular shine and thick borders.
- Render a dedicated 3D leaderboard podium instead of a generic fallback glyph.
- Prevent circular Home footer buttons from stretching to oval shapes; fit embossed labels and wallet controls to reference bounds.
- Honor reduced-motion preference for Home animated wings and press scaling.
- Update UI/character regression contracts and document acceptance for all nine supplied 941×1672 references.
- Full Android test, Android Lint, APK/AAB build and emulator screenshot comparison with the original references.
- Visually inspect every page and actual touch function. **No 1:1 screenshot acceptance has been established.**
- This build does not add fictional gems, in-app purchase restoration, cloud backup, or payment-related features simply because they appear in artwork.
