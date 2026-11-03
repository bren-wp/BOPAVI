# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.48 — Splash, Home, Gameplay and Game Over reference corrections (build 178)

**Published:** 2026-11-03 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.48)

- **Splash:** raised blue-and-white Loading lettering with separate navy shadow; preserve live preparations, flying Bopi, winged BOPAVI mark, rings and enamel progress bar.
- **Home:** reduce the excessive spaces between the icon and heading on Play, Skins, Shop and Settings by measuring one proportional centered cluster per button. Pin real coin/star balances to the physical safe top on tall portrait screens instead of vertically centering the wallets with the rest of the reference-stage UI. No fictional gem balance or payments.
- **Gameplay:** raise avatar and the actual three-heart HUD on reference-sized displays without altering hitboxes, lives, enemy positions, physics, collection or save behavior.
- **Game Over:** display current earned values inside locally drawn 3D outlined result text (white/navy captions; gold/bronze values), retaining real distance, coins/food and stars/score. Functional Retry/Home and the existing 3D podium remain.

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
