# BOPAVI P05 — Fruit/food score and saved record audit

Source: main `d6e25b3`, GitHub release v0.2.22/build127; current 4-state endless UX. No shop or profile is accessible.

| Contract | Android | iOS |
|---|---|---|
| Berry location | GameView.drawGate: midpoint of opening, rendered with r13 | GameCanvas.gate: same |
| Physical pickup | GameSimulation.advance → relative swept BerryCollision.touched, contact r18+r13 | BopaviCore/GameSimulation equivalent |
| One-shot | collectedFood[i] true once until new run; zero/NaN delta never updates | same |
| Normalized score | Endless score comes from passed gates, never berry count; coins remain 0 | same |
| HUD | Cache score/food text and accessibility string only upon changed values | cached count/attributed string, accessibility |
| Results | g.foodCollected in Results; progress.bestFood after recordRun | equivalent |
| Persistent best | SharedPreferences endless_food_best = max(existing,earned) | UserDefaults endless_food_best = max(existing,earned) |
| Backups | v1–v6 import preserves existing highest berry record | equivalent, tested actual export/import/rejected rollback |

**Added tests:** 7 identical Kotlin/Swift native `B|index|touched` vectors (fast crossing, zero velocity, tangency, +.02 miss, nonfinite); per-run/retry once-only assertions, zero-delta checks and unchanged score/coins; Swift save test for genuine contact → recordRun → export → importing older zero `bestFood` → malformed-v6 rollback. A source contract test also checks the Android persistence path. Android SharedPreferences instrumented rollback on real device is *not* established by a Python grep and remains a separate gate.

**Paused gameplay:** model has no paused member; platform controller/view prevents `step` while paused. Long frame gaps limited to 100ms, in 1/120s slices, with moving opening sampled at both endpoints. Bobice never become game currency. A future profile requires explicit scope approval.

**Visual DoD pending:** same-version Android and iOS active/result screenshots and real video showing near miss, pickup, pause, retry and saved best after force-stop/restart. Current release captures alone do not verify test-only changes. No claim of release/store approval or physical-device verification.
