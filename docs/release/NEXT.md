# BOPAVI Android v1.0.86 — unreleased

## Owner reference priority: Settings, Shop, Skins, Home

- Branch-only quality pass based on the owner's 10-screen, five-viewport archive.
- Baseline 20 real Android 16 screenshots from v1.0.85: measured approximate RGB MAE on every fourth pixel: Settings 75.1/255; Shop 73.0; Skins 67.7; Home 67.1. None are RGB-identical. Comparison is for prioritization, not a 0%-deviation certification.
- Settings portrait owner view now uses the nine-row reference camera when touch target expansion is within a measured 16% row pitch. Native buttons, volume sliders, switches, legal actions and save-backed controls remain intact. Scrollable native fallback remains for small, high-density and large-font configurations.
- Remaining work: review real emulator screenshots and address the highest Shop/Skins layout and 3D differences, verify exact RGB across 50 owner references, run all Android 15/16 and regression jobs. No historic published asset is removed.
- Release stays blocked until all mandatory CI jobs pass; the new version has not been signed or published.
