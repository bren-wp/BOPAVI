# BOPAVI v1.0.91 — Shop native card typography candidate (build 221)

## v1.0.91 — Two-line Shop items, unchanged real wallet/purchases (build 221)

**CANDIDATE / NOT PUBLISHED.** This is stacked on the independently pending v1.0.89 Home PR #239 and v1.0.90 Skins PR #240; **do not merge or release** out of order or without exact-head CI + native RGB screenshots.

- The 6 native 3×2 Shop cards previously allocated only 0.65/4.04 of their vertical weight to two-line headings. Genuine prior native 1080×1920 screenshots clip the second line of long names (DOUBLE SHIELD, SUPER MAGNET) despite the original owner WebP showing both.
- Make pure Kotlin `ShopCardRowWeights` (header .90, illustration 1.35, description .79, buy 1.00), preserving the original total 4.04 so frame geometry and purchase button portion are unchanged. This is a measured-source targeted heading correction, **not** a claim of improved whole-frame RGB.
- Keep six actual buy buttons and `ProgressStore.buyPerk` synchronized/committed coin and inventory transaction, price/ability item data, featured panel and original decorative kiosk asset. Do not invent IAP or hide owned state.
- Register Python source and Kotlin/JVM tests for weights, long names and unchanged buy behavior.
- Require real Android 15/16 UIAutomator, native unresized screenshots and comparisons against **all four** owner Shop WebPs, font-scale/rotation and TalkBack checks before visual acceptance.

### Published v1.0.88 Shop reference baseline (not this candidate)

Original SHA-pinned owner WebPs vs same-build authentic Android PNGs from successful main CI [43765935322](https://github.com/bren-wp/BOPAVI/actions/runs/43765935322):

| Physical viewport | Full-frame RGB MAE (0–255) | Changed RGB pixels |
| --- | ---: | ---: |
| 1080×1920 | 63.659683 | 99.994117% |
| 1080×2280 | 73.176275 | 99.992122% |
| 1080×2340 | 71.954981 | 99.991651% |
| 1080×2400 | 70.903066 | 99.992207% |

None is pixel-perfect. Original Shop kiosk detail/illustration, scene color and bottom foliage remain major differences. Do not replace real controls with screenshot crops or stretch screenshot PNGs to claim 1:1.

### Acceptance/continuation
After Home and Skins candidate native results are reviewed, compare Shop on the true v1.0.91 head, revise if it worsens/does not materially improve correct visual text. Next screens: Settings → Game Over A/B → Loading/Game → Pause → Results → Privacy/Terms, including functional dialogs.
