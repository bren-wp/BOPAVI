# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.87 — Owner Shop 3D footer and responsive frame (build 217)

**Published:** 2026-11-14 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.87)

- Baseline from 20 original v1.0.86 Android16 owner-viewport PNGs against the user's original 50 WebP designs (no resize; sampled 1 in 4 pixels each dimension): Shop RGB MAE 72.62/255; Skins 68.76; Settings 67.25; Home 67.14. These are substantial mismatches, not 1:1 fidelity.
- Screen-by-screen priority **Shop first**. Isolate wooden bottom 3D counter in a passive native Canvas layer anchored to the physical bottom on tall portrait viewports, while the 3×2 native inventory, actual offline coin balances and purchase actions remain centered and intact.
- Decorative plants/flowers follow the same full-height bottom anchor rather than leaving the wooden counter visually floating over the sky on 19:9 and 20:9 handsets.
- Preserve smaller-window native scrolling, accessibility minimum touch sizes and all existing gameplay/HUD/collision physics.
- This is not a screenshot-based UI replacement. All prices, product state and CTA actions are still real Android Views.
- Final release blocked pending full source/security, Android 15 and Android 16 emulator suites, and NEW post-change pixel comparisons of all owner viewports. 0% RGB difference is not yet confirmed. No Play-signed output.
- Skins: decoupled top hanging wooden title, hero, stone 3D podium, four real native cards and EQUIP panel into calibrated vertical cameras, rather than shifting all elements 154px upward together. Original owner tall art puts cards near y≈1350px on 1080×2400; the old shared .18 anchor put them around y≈1200px. On 16:9 the new anchors have no effect.
- Settings: owner tall art spreads nine preference rows progressively from ~30px at the top to ~150px at the last legal row and extends the decorative wood behind them. Preserved real switches, native SeekBars, accessible 48dp taps, compact/large-font scrolling and both legal links.
- Home: original animated 512px Bopi body+wing set uses a smaller 400×400 reference viewport centered near x=500, with a lower native vertical position. Projection puts painted sprite top near y≈676 on 1080×1920 and ≈840 on 1080×2400 while retaining the three real Shop/Skins/Settings buttons.
- Expanded real API36 owner-viewport capture suite from 20 to 25 authentic native screenshots by entering the **actual Privacy Policy** from Settings on each of five viewport sizes; verify its heading and real close button and preserve the ability to return to Settings/Home. A captured dialog is not presumed pixel-identical to the owner's privacy mockup.
- Added actual 02_Gameplay_upute, 03_Gameplay_let and 04_Gameplay_pauza captures on the same five reference device resolutions. This requires a real GameSimulation, first tap and PAUZA/Nastavi/Ponovo/Izlaz native dialog, with a strictly bounded retry only if the first input was genuinely lost. Brings real unscaled capture inventory to 40/50; both Game Over variants remain UNVERIFIED and are not falsely marked 1:1.
- Legal page: replaced the generic white Android AlertDialog with an accessible, responsive, wooden 3D native dialog, blue enamel heading, cream paper body and green close CTA. Preserved the **verbatim bundled legal HTML**, links, ScrollView and real close/background dismiss. No screenshot contains fake clickable legal content.

## v1.0.86 — Owner visual fidelity: Settings and Shop (build 216)

**Published:** 2026-11-14 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.86)

- Branch-only quality pass based on the owner's 10-screen, five-viewport archive.
- Baseline 20 real Android 16 screenshots from v1.0.85: measured approximate RGB MAE on every fourth pixel: Settings 75.1/255; Shop 73.0; Skins 67.7; Home 67.1. None are RGB-identical. Comparison is for prioritization, not a 0%-deviation certification.
- Settings portrait owner view now uses the nine-row reference camera when touch target expansion is within a measured 16% row pitch. Native buttons, volume sliders, switches, legal actions and save-backed controls remain intact. Scrollable native fallback remains for small, high-density and large-font configurations.
- Shop wooden winged header and actual native SHOP wordmark now share an 85-grid-unit vertical offset, correcting an approximately 97-pixel title misalignment in the 1080×1920 source capture without moving balances, featured product or purchase grid.
- Remaining work: review real emulator screenshots and address the highest Shop/Skins layout and 3D differences, verify exact RGB across 50 owner references, run all Android 15/16 and regression jobs. No historic published asset is removed.
- Release stays blocked until all mandatory CI jobs pass; the new version has not been signed or published.
- Real 20-view owner benchmark using genuine v1.0.85/v1.0.86 Android 16 PNGs and the matching original 50-image ZIP: Shop 73.01→73.45 RGB MAE (regression on tall displays); Settings 75.13→68.52 (improvement); Skins and Home unchanged. Sample every fourth pixel on each axis; this is not a pixel-perfect claim.
- Separate upper Shop canopy/kiosk/wooden sign from its centered 3×2 purchasable card grid. Header art, actual SHOP TextView, wallet counters and Back move together using a consistent physical top anchor; card positions, genuine currencies and purchase interactions are preserved.
- Settings groups now use independent measured viewport anchors (brand, portrait, title, nine accessible rows and bottom restore). Small/large-font fallback remains unchanged.
- Android 15 QA still requires a genuinely active first flap, one native Pause after background and identical saved lives and process. Retry one *real* touchscreen tap only if Android reports the initial GameView is still idle, never synthesize game state or claim an emulator-only cause.
- New visual acceptance must come from real Android 15/16 screenshots, all screen resolutions and updated RGB reports; release blocked until mandatory CI is green.
- Skins portrait camera now uses an owner-measured top-weighted (.18) vertical anchor on long phones rather than the generic center (.50) anchor. On 1080×2400 that adjusts the complete native art/cards/selection panel by ~154 physical pixels together; on 16:9/compact viewports the old placement remains. Real screenshots must confirm actual improvement.
