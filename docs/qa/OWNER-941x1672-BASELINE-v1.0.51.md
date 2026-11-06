# Four owner references vs real Android v1.0.51 screenshots

## Source of measurements

- Original owner's **unchanged** 941×1672 PNG references supplied in conversation.
- Real 941×1672 native Android captures from successful main Actions run
  [42134092837](https://github.com/bren-wp/BOPAVI/actions/runs/42134092837),
  `QA-Android-visual-flow` artifact ID `13465032387`.
- RGB MAE is mean(abs(reference_channel - capture_channel)) over 8-bit RGB
  values. No resizing, synthetic screenshot or falsified UI state.
- Reproducibility: `tools/qa_three_owner_references.py` and
  `tools/qa_rank_four_reference_screens.py` on the original captures.

| Screen | Native screenshot | Full screen RGB MAE (0–255) |
| --- | --- | ---: |
| Splash / Loading | android-loading-reference-size.png | 79.961 |
| Home | android-home-reference.png | 71.916 |
| Gameplay | android-gameplay-ready-reference-size.png | 76.053 |
| Game Over | android-game-over-reference-size.png | 74.910 |

**None is a proven 1:1 pixel match.** Temporal animation phases and genuine
coins, hearts, saved scores and screen state may vary between captures. Never
change actual data to copy the illustration's sample values.

### Biggest Splash/Home regions in v1.0.51

| Region of supplied 941×1672 reference | Mean absolute RGB error |
| --- | ---: |
| Splash — winged BOPAVI logo | 92.03 |
| Splash — loading track and wings | 85.10 |
| Splash — flying aviator | 75.97 |
| Splash — Loading caption | 75.49 |
| Home — winged BOPAVI logo | 85.11 |
| Home — three bottom medallions | 81.77 |
| Home — green Shop | 80.16 |
| Home — purple Skins | 74.66 |
| Home — flying Bopi | 68.29 |
| Home — wallet counters | 63.01 |
| Home — blue Settings | 61.47 |
| Home — gold Play | 60.99 |

Other high-error areas: Gameplay lower ground spikes 119.93; Game Over
blue Home 87.61, golden Retry 81.84. These are queued after first-pass
Splash/Home improvements, not hidden by passing source tests.

## v1.0.52 candidate changes / still unverified

1. **Splash:** 757×229 wordmark at reference (92,128), centered with correct
   780:236 source ratio; previous 885×268 logo was too wide.
2. **Home:** 645×645 square Bopi composition at (152,342). The previous
   745×483 view had a square 512×512 image centered into 483px height,
   substantially reducing apparent body and wings; the rendered source alpha
   union is x18..493, y86..424 in the classic sprite.
3. **Scenery:** additional local soft-focus clouds, waterfall islands and
   foliage, without rasterizing controls, text, save data or simulated score.
4. Shared real native buttons, safe touch targets, voice-over labels and
   saved wallet values are preserved.

New **build 182** screenshots must be obtained from a successful emulator
run and measured separately. The v1.0.51 numbers above are a baseline;
do NOT represent them as verified improvements in v1.0.52.
