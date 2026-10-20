# BOPAVI — Native Android architecture

Application: `com.brendigo.bopavi` · Android SDK · Kotlin, no runtime
backend. Entry point: MainActivity → Home → GameView/GameSimulation → Results.

| Module | Responsibility |
|---|---|
| GameSimulation.kt | Flight impulse, bounded 120Hz physics substeps, score, fruit, game states |
| LevelEngine.kt | Deterministic gates, animated openings and difficulty-independent world grid |
| GameView.kt | Android Canvas art, HUD, input, safe-area and camera presentation |
| MainActivity.kt | Screen routes, pause/resume, navigation and lifecycle |
| ProgressStore.kt | Compatible local scores, settings and older save-key semantics |
| Soundscape.kt | Offline original loops and effect playback |
| FitViewportColumn.kt | Non-scrolling small-screen home layout |
| LevelEngineTest.kt | Deterministic physics, collisions, food and progress regressions |

The reference world remains 480×800; screen scaling/cutout handling must not
change gate geometry or body radius. The responsive menu measures actual
interactive content before fitting within the viewport.

Build and test with Android JDK 17 tooling:
`cd android && ./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease`

Source and generated graphics/audio are validated separately in GitHub Actions.
Google Play signing requires a separate, authorized upload-key workflow.
