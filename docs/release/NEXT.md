# BOPAVI v1.0.90 — Skins visual layout candidate (build 220)

## v1.0.90 — Owner Skins hero sizing and native arrow clearance (build 220)

**CANDIDATE / NOT PUBLISHED.** Stacked on Home v1.0.89 draft PR #239. Merge the Home candidate only after exact-head CI and native RGB review. This Skins PR must be independently reviewed and merged AFTER the Home release candidate is accepted. No version or release is published automatically from a Draft PR.

- Reduce the complete real Skins animated hero square from 688 to 626 units on the existing 941×1672 reference stage and recenter at x=157. New top=260 keeps the aviator and feathers above selection cards while clearing both genuine 112-unit carousel arrow hit rectangles. No menu callback or user data changes.
- Preserve four card choices, purchased/owned state, actual star/coin balances, unlock confirmation, selected hero equipment, v1–v6 data compatibility, native 48dp targets and source animation layers.
- Extend source + JUnit geometric regression tests. Actual Android 15/16 UIAutomator and 4 matching owner viewport native screenshots remain mandatory. No screenshot resampling or copied reference art.

### Pre-change owner Skins RGB baseline

Evidence from main v1.0.88 [43765935322](https://github.com/bren-wp/BOPAVI/actions/runs/43765935322), original 40 owner WebPs and authentic matched native captures:

| Owner physical viewport | Full RGB MAE (0–255) | Changed pixels |
|---|---:|---:|
| 1080×1920 | 66.597206 | 99.979938% |
| 1080×2280 | 59.683949 | 99.973968% |
| 1080×2340 | 59.350665 | 99.971747% |
| 1080×2400 | 60.881106 | 99.959684% |

**All different, zero 1:1 matches.** Stacked Home and Skins changes must be evaluated together using exact native PNGs on the new Skins SHA, then corrected further until measurable deviation improves; a green functional build is not parity.

### Blockers before merge/release

- Genuine 4-viewport native Skins snapshots at this exact source SHA and fresh RGB MAE, including selected, locked/unlocked, equip and confirmation states.
- Retest both arrows, touch targets, TalkBack, insufficient coins, selected state persistence, rotation and large fonts.
- No proof of pixel-perfect owner equivalence or physical-device performance from source tests alone.

