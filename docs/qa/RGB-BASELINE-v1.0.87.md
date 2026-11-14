# Measured native RGB baseline — published BOPAVI v1.0.87

**Provenance:** GitHub Actions run [43639612786](https://github.com/bren-wp/BOPAVI/actions/runs/43639612786),
product commit `b0fd895cca0b14d995a951d498c63d085f4bff5f`;
authentic unmodified API36 Android captures from its
`QA-Android16-visual-flow` artifact. Source WebPs came byte-for-byte
from the pinned `art/reference-current/images/` repository objects via
`BOPAVI-owner-reference-binaries` on PR #236. **This is a historical
v1.0.87 comparison, not the latest v1.0.88 screenshot acceptance.**

## Method

Decode each original owner WebP and the corresponding actual Android PNG
to RGB. Compare the *same physical width and height*, with no resizing,
masking, compositing, synthetic captures or postprocessing. A pixel differs
if ANY RGB channel differs. MAE is the arithmetic average of absolute RGB
channel errors on the [0,255] scale. The same method is repeated on three
disjoint vertical bands as supplementary diagnostic evidence.

**50 expected; 36 paired and measured; 36 different; 0 identical;
14 unverified.** The 14 unverified are four Game Over B states without
a separate native capture and ten 1440×3040 original reference WebPs
that are still missing from GitHub.

| Screen | 1080×1920 | 1080×2280 | 1080×2340 | 1080×2400 | 1440×3040 | Weighted RGB MAE (36 measured subset) |
| --- | --- | --- | --- | --- | --- | ---: |
| Home | different | different | different | different | original missing | 63.352821 |
| Gameplay instructions | different | different | different | different | original missing | 50.926284 |
| Gameplay flight | different | different | different | different | original missing | 45.289013 |
| Gameplay Pause | different | different | different | different | original missing | 49.116097 |
| Game Over A | different | different | different | different | original missing | 63.545353 |
| Game Over B | native capture missing | native capture missing | native capture missing | native capture missing | original missing | — |
| Skins | different | different | different | different | original missing | 61.927770 |
| **Shop** | different | different | different | different | original missing | **71.501767** |
| Settings | different | different | different | different | original missing | 64.046212 |
| Privacy | different | different | different | different | original missing | 60.515746 |

Every one of the 36 compared full frames has over 99.9% different pixels.
This is exact RGB matching, not a perceptual similarity score; tiny antialias
differences count as differences.

### Shop — all four genuine measured pairs

| Original owner image | Different RGB pixels | Different % | Full-frame RGB MAE | Top MAE | Middle MAE | Bottom MAE |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 1080×1920 | 2,073,463 | 99.993393 | 69.709310 | 71.589206 | 67.569073 | 69.969651 |
| 1080×2280 | 2,462,206 | 99.992122 | 73.176275 | 70.109742 | 69.597266 | **79.821818** |
| 1080×2340 | 2,526,989 | 99.991651 | 71.954981 | 66.073865 | 70.261971 | **79.529107** |
| 1080×2400 | 2,591,798 | 99.992207 | 70.903066 | 64.289204 | 70.651796 | **77.768198** |

Shop across all four images: 9,654,456 different RGB pixels
(99.992294%), full RGB MAE **71.501767/255**, weighted top/middle/bottom
MAE 67.808548 / 69.618759 / **77.077993**. The largest *average*
difference is in its bottom region, especially the tall viewports.

### Shop geometry — bounded source correction candidate

The 1080×1920 owner source contains blurred side gutters; at image
y≈1267 the wood framing runs approximately **x=101..979**. The
published native v1.0.87 capture has the corresponding frame around
**x=21..1062**, without those gutters. The physical centred scaling ratio
of their widths is **(979−101)/(1062−21)≈0.8434**.
PR #236 proposes only at *1080×1920* horizontal framing
`ShopReferenceLayout.owner16x9HorizontalFactor=0.8435`.
This is implemented in `ReferenceStage.kt` with no vertical layout
change, no alteration of 1080×2280/2340/2400, 1440×3040, 941×1672 or
other screens, and with native touch sizing preserved. It is a
**measured geometry candidate**, NOT proof that pixel MAE improved.
The real post-change Android capture and new RGB comparison are
mandatory before accepting it.

## Release decision

**No 1:1 acceptance.** A source-level geometry improvement, source
tests, or an Android build success never substitutes for actual
post-change native screenshots. Do not merge/publish v1.0.88 until the
separate 50/50 exact RGB gate and functional/provider tests pass.
