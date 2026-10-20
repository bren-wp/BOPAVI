# BOPAVI — Android berry and saved-score integrity

**Current native implementation:** `GameSimulation.kt`,
`GameView.kt`, `ProgressStore.kt`. Berries are physical pickups,
not coins or in-app purchases.

| Contract | Android behaviour |
|---|---|
| Fruit position | Centre of each gate's animated opening, drawn with radius 13 |
| Collection | `BerryCollision.touched` sweeps berry-body motion relative to Bopi, radius 18+13 |
| One shot | `collectedFood[i]` prevents duplicate pickup during a run |
| Scoring | Endless score reflects passed obstacles; fruit has a distinct count |
| Results | Shows real `foodCollected`, then records best score and food |
| Persistence | `SharedPreferences` key `endless_food_best` remains compatible |
| Legacy saves | Import validation preserves highest known berry record |

Kotlin JVM tests cover edge contact, moving pickup, tangency, invalid
numbers, repeated touches, retry and persisted-value source contracts.
Android emulator gameplay checks run in GitHub Actions. Hardware
process-death, battery, long-play telemetry and manual screenshot/video
comparisons remain separate release acceptance criteria.
