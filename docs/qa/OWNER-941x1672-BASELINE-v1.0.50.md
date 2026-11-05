# BOPAVI owner-screen reference baseline — v1.0.50, build 180

**Data:** Original 941×1672 owner PNGs supplied on 6 November 2026 and real unmodified PNGs from the green GitHub Actions run [41582421004](https://github.com/bren-wp/BOPAVI/actions/runs/41582421004), artifact `QA-Android-visual-flow` (ID 13207215834). This measures the LAST PUBLISHED implementation, **not** the unverified v1.0.51 candidate.

| Priority screen | Actual screenshot from artifact | Whole-screen RGB MAE, 0–255 |
| --- | --- | ---: |
| Splash / Loading | `android-loading-reference-size.png` | 78.45 |
| Home | `android-home-reference.png` | 72.54 |
| Gameplay (ready state) | `android-gameplay-ready-reference-size.png` | 75.77 |
| Game Over | `android-game-over-reference-size.png` | 77.09 |

All images have native dimensions 941×1672; none was resized for comparison. Whole-screen MAE is the average of absolute per-channel differences of 8-bit RGB pixels. Different legitimate wallet values, animations and game scores also contribute to error and should **not** be counterfeited to lower MAE.

## Highest error regions, ordered by button/HUD-weighted priority

| Screen and ROI | Raw ROI MAE | Priority score |
| --- | ---: | ---: |
| Game Over — blue Home | 87.74 | 175.48 |
| Game Over — gold Retry | 87.43 | 174.86 |
| Home — green Shop | 87.23 | 174.47 |
| Home — footer medallions | 81.26 | 162.51 |
| Home — purple Skins | 80.52 | 161.05 |
| Loading — winged loading track | 71.94 | 143.89 |
| Gameplay — avatar and hearts HUD | 71.58 | 143.16 |
| Home — blue Settings | 66.08 | 132.17 |
| Home — currency counters | 63.01 | 126.02 |
| Gameplay — Tap to Fly hint | 61.18 | 122.36 |
| Home — gold Play | 60.85 | 121.71 |
| Gameplay — lower ground spikes | 119.93 | 119.93 |

Weighted priority = 2 × ROI MAE for controls/HUD, otherwise 1 × ROI MAE. Use the repository's `tools/qa_three_owner_references.py` and `tools/qa_rank_four_reference_screens.py` for the authoritative fully reproducible ranking. This report does not relax exact-RGB acceptance.

## Verified art and functional limitations

- The published Game Over displays an airborne portrait instead of a distinct sad seated original Bopi.
- Its fantasy backdrop and the Home/Loading world are simplified local illustrations, visibly different from the owner's detailed painted islands. Changing button spacing alone cannot make the full screens exact.
- The preflight loading track appears partly filled before a completion signal, despite no measured percentage. Build 181 replaces it with a moving indeterminate scan.
- Physical safe insets, genuinely collected items and real gameplay states must be retained when positioning the HUD and generating captures.
- This baseline is **not a pass** for literal 1:1 pixel identity; all four original/actual comparisons must be repeated after a successful real emulator build.

## Next capture sequence

1. Run Android 15 and Android 16 gameplay lifecycle and native UI jobs without skipping failures.
2. Download new genuine `QA-Android-visual-flow` artifacts from the candidate head commit.
3. Compare Splash → Home → Gameplay → Game Over in that order at native 941×1672 using the unmodified owner PNGs.
4. Rank ROIs and inspect any worsened controls or lost click targets before a merge/release.
