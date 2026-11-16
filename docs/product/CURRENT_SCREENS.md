# BOPAVI — Current Android screens

**Published release baseline:** v1.0.89 (build 219). This corrects the post-publication documentation generator; it does not rewrite the original GitHub release.

The production Home has **four functional accessible actions**: **PLAY, SKINS, SHOP and SETTINGS**. PLAY starts the actual game through MainActivity.startGame(), Loading and GameView. The passive coin balance is not a fifth action.

| Accessible screen or state | Genuine release screenshot / evidence | Operation |
|---|---|---|
| Home | BOPAVI-Android-Home.png | PLAY, SKINS, SHOP, SETTINGS |
| Loading / Ready | BOPAVI-Android-Flight-Preview.png (ready state) | Transition from PLAY to playable flight |
| Flight | BOPAVI-Android-Flight-Active.png | Tap-to-flap, obstacles, score and coins |
| Pause | BOPAVI-Android-Pause.png | Resume and navigation |
| Game Over | BOPAVI-Android-Game-Over.png | End-state feedback |
| Results | BOPAVI-Android-Results.png | Saved results, Retry and Home |
| Skins | BOPAVI-Android-Skins.png | Pilot selection and local unlocks |
| Shop | BOPAVI-Android-Shop.png | Six offline upgrades and virtual coins |
| Settings | BOPAVI-Android-Settings.png | Audio, accessibility, privacy, terms and SAF |
| Privacy (Settings dialog) | BOPAVI-Android-Privacy.png | Bundled privacy notice |
| Terms (Settings dialog) | BOPAVI-Android-Terms.png | Bundled terms of use |

Removed standalone pages: Progress, Leaderboard, Records, Achievements and Level Map. Their removal does not disable gameplay or delete compatible historical player data.

**Reference design:** 10 distinct states; 40 genuine owner originals over four available resolutions. Screenshots are real release assets, but source checks and the existence of screenshots alone do **not** establish 1:1 RGB parity or real-device stability.

Published screenshots: https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.89. Unreleased changes are documented separately in `docs/release/NEXT.md`.
