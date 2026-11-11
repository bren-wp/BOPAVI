# BOPAVI — 9-screen UI fidelity / functional acceptance matrix

Owner objective: **1:1, zero RGB pixel deviation**. A screenshot is evidence,
not an Android control; every button, slider, purchase and navigation event
must be implemented by a real native component. Do not generate imagery as
a replacement for test results.

| Screen | Implementation | Verified data/actions | 1:1 acceptance blocker |
| --- | --- | --- | --- |
| Shop / Store | `ShopScreen.kt` | Real earned coins, local SKU purchases, real prices | Original 941×1672 screenshot diff and original product art need further work |
| Skins | `PilotSelectionScreen.kt` | Saved selected/equipped/locked pilots and unlock actions | Original hero, platform and card artwork differ |
| Settings | `SettingsScreen.kt` | Saved audio, notifications, bilingual setting, backup/restore, legal links | Actual 941×1672 row/title/footer diff and Android 15 stable navigation not yet accepted |
| Level Complete | `LevelCompleteScreen.kt` | True awarded stars/coins and NEXT/HOME/SHARE actions | Fresh source-reference screenshot and pixel diff required |
| Game Over | `GameOverScreen.kt` | Real score/distance/collectibles, RETRY and HOME | Fresh source-reference screenshot and pixel diff required; alternate pilots need approved poses |
| Postignuća / Achievements | `AchievementsScreen.kt` | Eight goals calculated from saved run counters | No accepted 941px owner bitmap in repository; native high-DPI fallback now tested |
| Napredak / Progress | `ProgressOverviewScreen.kt` | Eight worlds, saved frontier, first-20 level stars, wins, runs, flaps, coins | New genuine screen capture and an approved design reference needed for numeric 1:1 |
| Karta razina / Level Map | `LevelMapScreen.kt` | Eight worlds, truly disabled locked nodes, real saved ratings | World artwork/stone paths require pixel review; high-DPI node hit targets now guarded |
| Rekordi / Leaderboard | `LeaderboardScreen.kt` | Actual local top-ten entries and summary stats | Accepted owner bitmap needed; high-DPI two-line row clipping now guarded |

## Current measurable claim

Last historical genuine Android 16 measurements from an earlier revision:
- Shop: normalized RGB mean absolute error 23.3593% vs original.
- Skins: normalized RGB mean absolute error 32.1376% vs original.

These **do not apply to the latest PR head** and are not evidence of
improvement in this round. Other screens currently lack fresh head-commit
reference comparisons; do **not** fabricate percentages or label them as 0%.

## Acceptance procedure for each screen

1. Fix the original reference source (941×1672 PNG + SHA-256).
2. Build the exact PR commit, run a native emulator/device screenshot on the
   same physical image grid, save the full unmasked RGB capture.
3. Compute `different_pixels`, `rgb_mae` and region-level reports without
   resizing, masking, filtering or overlays before numerical comparison.
4. Confirm native interaction (enabled/disabled, selection, back, purchase,
   Share/Retry, privacy, sliders/toggles), UI automation and accessibility.
5. Verify portrait/landscape, 200–400% font/zoom and DPI-triggered scrolling
   layouts without lost or overlapping touch actions.
6. Merge only after the green CI and owner review. Numeric zero can be claimed
   only when **every compared RGB pixel is exactly equal**; no current result
   meets that criterion.

This PR does not modify the original reference images and does not produce
mock button screenshots.
