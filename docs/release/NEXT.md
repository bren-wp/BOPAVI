# BOPAVI Android v1.0.86 — unreleased

## v1.0.86 — Owner visual fidelity: Settings and Shop (build 216)

- Branch-only quality pass based on the owner's 10-screen, five-viewport archive.
- Baseline 20 real Android 16 screenshots from v1.0.85: measured approximate RGB MAE on every fourth pixel: Settings 75.1/255; Shop 73.0; Skins 67.7; Home 67.1. None are RGB-identical. Comparison is for prioritization, not a 0%-deviation certification.
- Settings portrait owner view now uses the nine-row reference camera when touch target expansion is within a measured 16% row pitch. Native buttons, volume sliders, switches, legal actions and save-backed controls remain intact. Scrollable native fallback remains for small, high-density and large-font configurations.
- Shop wooden winged header and actual native SHOP wordmark now share an 85-grid-unit vertical offset, correcting an approximately 97-pixel title misalignment in the 1080×1920 source capture without moving balances, featured product or purchase grid.
- Remaining work: review real emulator screenshots and address the highest Shop/Skins layout and 3D differences, verify exact RGB across 50 owner references, run all Android 15/16 and regression jobs. No historic published asset is removed.
- Release stays blocked until all mandatory CI jobs pass; the new version has not been signed or published.

## 2026-11-14 — follow-up visual and emulator stabilization (PR pending)

- Real 20-view owner benchmark using genuine v1.0.85/v1.0.86 Android 16 PNGs and the matching original 50-image ZIP: Shop 73.01→73.45 RGB MAE (regression on tall displays); Settings 75.13→68.52 (improvement); Skins and Home unchanged. Sample every fourth pixel on each axis; this is not a pixel-perfect claim.
- Separate upper Shop canopy/kiosk/wooden sign from its centered 3×2 purchasable card grid. Header art, actual SHOP TextView, wallet counters and Back move together using a consistent physical top anchor; card positions, genuine currencies and purchase interactions are preserved.
- Settings groups now use independent measured viewport anchors (brand, portrait, title, nine accessible rows and bottom restore). Small/large-font fallback remains unchanged.
- Android 15 QA still requires a genuinely active first flap, one native Pause after background and identical saved lives and process. Retry one *real* touchscreen tap only if Android reports the initial GameView is still idle, never synthesize game state or claim an emulator-only cause.
- New visual acceptance must come from real Android 15/16 screenshots, all screen resolutions and updated RGB reports; release blocked until mandatory CI is green.

- Skins portrait camera now uses an owner-measured top-weighted (.18) vertical anchor on long phones rather than the generic center (.50) anchor. On 1080×2400 that adjusts the complete native art/cards/selection panel by ~154 physical pixels together; on 16:9/compact viewports the old placement remains. Real screenshots must confirm actual improvement.

- Home: original owner reference shows the single earned-coin wallet against the top-right edge (x≈708 on 941px camera), not over the central brand. Removed remaining decorative gold ring trail from Home; retained sparkles, natural streaks, native 3D buttons, real saved coins and accessibility.
