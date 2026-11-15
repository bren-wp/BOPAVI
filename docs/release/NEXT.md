# BOPAVI v1.0.89 — Home visual geometry candidate (build 219)

## v1.0.89 — Owner-reference Home logo camera correction (build 219)

**CANDIDATE / NOT PUBLISHED.** This is the first screen-by-screen fidelity iteration, not a claim of 1:1 completion. Production Kotlin/layout is changed, so an Android versionCode increase is mandatory before a future release.

- Shrink and raise the actual native winged-logo ImageView based on the owner's 1080×1920 painted wordmark bounds (from approximately 840px wide in v1.0.88 toward approximately 560px). Keep the real logo asset and SVG; do not embed the owner screenshot in the app.
- Validate the wallet and painted logo using true 2-D non-overlap rather than an unnecessary vertical ordering rule. Preserve the four real PLAY/SKINS/SHOP/SETTINGS callbacks, original button slots and saved data.
- Add direct Kotlin/JVM camera geometry assertions; publish separate original-versus-real-Android RGB evidence across the four available viewport references. Full RGB 1:1 is **not** claimed.
- Continue in order: Home geometry and art, Skins, Shop (six real purchases), Settings, Game Over variants, Gameplay/Loading, Pause, Results and legal dialogs. Do not merge until source + Android 15/16 emulator tests succeed and real pixel measurements are reviewed.

### Previous published v1.0.88 owner Home baseline

Run [43765935322](https://github.com/bren-wp/BOPAVI/actions/runs/43765935322), reference-status artifact `BOPAVI-owner-reference-rgb-report`, original owner WebPs (not screenshots). Each is the full-frame MAE in RGB channel units 0–255, no scaling:

| Original size | Native status | RGB MAE | Changed RGB pixels |
| --- | --- | ---: | ---: |
| 1080×1920 | different | 65.969882 | 99.998216% |
| 1080×2280 | different | 64.178212 | 99.999188% |
| 1080×2340 | different | 63.468075 | 99.999763% |
| 1080×2400 | different | 63.108658 | 99.999730% |

Owner 1080×1920 Home shows only three original lower action buttons, whereas product-owner-approved ADR-0003 adds PLAY as a fourth actual button. Therefore the Home **cannot** obtain 0 differing pixels against historical reference without an approved reference including PLAY. The new candidate uses native Android UI and keeps the deliberate change explicit. No resized, fabricated or reused screenshot qualifies as verification.

### Remaining blockers

- Obtain authentic candidate native 1080×1920 / 1080×2280 / 1080×2340 / 1080×2400 screenshots and compare against matching SHA-pinned owner originals.
- Verify visual appearance of the winged wordmark (font/palette) and sky art separately; camera-bound alignment alone cannot correct illustrated asset differences.
- Confirm accessibility, notches, font-scale, compact/landscape layouts, 48dp hitboxes, loading and transitions. Do not delete old saves or reference art.
- Physical devices, SAF reliability, per-asset rights, exhaustive security/dead-code and performance remain unverified.

