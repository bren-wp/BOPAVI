# BOPAVI — owner-reference RGB comparison and screen prioritization
Date: 2026-11-09

Source of **actual** Android screenshots: successful GitHub Actions run
[42436664252](https://github.com/bren-wp/BOPAVI/actions/runs/42436664252)
from 2026-11-08, artifact `QA-Android-visual-flow`. This is **before**
2026-11-09 code fixes, not a post-fix screenshot acceptance.
Source of target references: nine owner-supplied PNGs in the ChatGPT
conversation, each 941×1672 pixels. References are not committed to GitHub.

## Method

For each captured frame, convert the reference and screenshot to RGB
without resizing, overlaying or registration, then calculate:

```text
RGB-MAE-% = mean(abs(reference_RGB - actual_RGB)) / 255 * 100
```

This metric is brightness/color-sensitive and penalizes slight shifts;
it is **not** the percentage of failing pixels and cannot alone prove
functional correctness. All comparisons use exactly 941×1672. The owner
can reproduce with `tools/compare_owner_ui.py` using original images.

| Descending priority | Reference | Actual capture | RGB-MAE-% |
| --- | --- | --- | ---: |
| 1 | Level Map | android-level-map-reference-size.png | 29.03 |
| 2 | Gameplay ready | android-gameplay-ready-reference-size.png | 27.74 |
| 3 | Skins | android-skins-reference-size.png | 27.46 |
| 4 | Shop | android-shop-reference-size.png | 27.31 |
| 5 | Game Over | android-game-over-reference-size.png | 26.63 |
| 6 | Settings | android-settings-reference-size.png | 26.56 |
| 7 | Splash | android-loading-reference-size.png | 26.18 |
| 8 | Home | android-home-reference.png | 23.50 |
| — | Level Complete | **No equivalent QA capture found** | Not measured |

## Confirmed visual issues and corrective priorities

1. **Level Map:** actual islands/paths, elevation and typography are flat
   stylized vectors versus 3D fantasy islands in owner artwork. First
   priority is layered terrain, bridges, locked nodes, crown title bars.
2. **Gameplay:** owner wants bright blue Bopi, highly detailed floating
   terrain and pillar geometry. Actual screenshot shows different bird
   silhouette and much flatter scenery. The prior capture also contains
   five bottom-edge spikes. On 2026-11-09 those were removed deliberately
   across all flight states, **overriding that detail in the older reference**.
   New captures are needed to verify this change visually.
3. **Skins:** portrait, dynamic body appearance, suspended wooden sign,
   rotating avatar and character cards require deeper reference matching.
4. **Shop:** marketplace structure approximates the source, but product
   illustration, 3D card materials and hero artwork are not matched.
   The owner mockup includes a gem wallet that this offline app does not
   actually implement. Never display or charge a fictitious currency.
5. **Game Over:** mascot pose, crown emblem, gem/coin indicators and lower
   call-to-action sizes differ from the source. A mockup's gem tally is
   not real run telemetry and must not be faked.
6. **Settings:** actual capture contains shortened labels like
   `Soun...` and `Vibra...` and cramped mode selectors. Reduced label
   typography and concise difficulty captions, preserving full accessible
   names. The reference also shows unavailable Cloud Save, language and
   Restore Purchases; do not expose as functional until implemented.
7. **Splash/Home:** broad composition exists but foreground backgrounds,
   wing geometry, gloss/shading, mascot art and edge treatment diverge.

## Acceptance criteria

- Re-capture all nine screens **after** this change at 941×1672, plus
  compact/tall devices, and report actual RGB-MAE and per-region deltas.
- Check gameplay in idle, first flap and later flight: **no lower spikes**,
  no ghost collisions, real coins and hazards, no layout overlap.
- Inspect actual accessibility nodes and touch targets. Test physical
  phone lifecycle, pause/resume, locked states, purchases and progression.
- Treat zero-percent similarity claims as unverified until all real
  same-state captures compare exactly. No equivalence is claimed here.
