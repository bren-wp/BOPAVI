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

**Important QA correction:** the legacy `tools/qa_owner_zero_pixel.py`
registry had mistakenly swapped the original Skins and Settings SHA-256 hashes.
That mistake invalidated the historical Skins measurement of 32.1376%.
Its hashes now agree with `tools/qa_owner_941_all_screens.py` and the
actual user-supplied PNG bytes. A regression test pins the correct roles:

- Shop original SHA-256: `9e7b6f8aebe0a129c1a048459d3f24f58c7fb9c82d797bb12d3fd418305d21d8`
- Skins original SHA-256: `1c0585aff3c9374b6afe3288358c7b1bec814947409ddb91019d104e1c27dd2a`
- Settings original SHA-256: `37c2ef39d3f5fd09b5bef33ef1ef6242f316c54dd96984a742d697988349b5ff`

Correctly paired historical screenshots, all from earlier builds and not the
latest PR head, yield normalized RGB mean absolute error:

- Shop, Android 16 screenshot: **23.3593%**
- Skins, Android 16 screenshot: **27.3322%**
- Settings, older Android 15 screenshot: **24.7671%**

Correct Skins regions (Android 16): wooden title 24.9284%, main hero
28.5639%, stone platform 26.0966%, four cards 31.8971%, selected info
31.8931%. These values are measured at the original 941×1672 pixel grid.

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
