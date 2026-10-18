# BOPAVI physics frame-rate replay — PROMPT 03 (P0 candidate)

Baseline main `96cb160` / v0.2.21 build 126. This is **simulation-only** evidence. No hardware input-to-photon or human flight-feel measurements have yet been supplied.

## Hypothesis to test before changing physics

Kotlin and Swift use the same tap impulse **−255**, gravity **685**, terminal speed **365** and body radius **18**, but `step(delta)` uses a semi-implicit Euler update per variable physics slice of up to **34ms**. At a fixed 600ms from one flap in an empty opening, independent Float32 calculations predict:

| Vsync | Frame delta | Baseline y (approx., pre-fix) |
|---|---|---:|
| 30Hz | 33.33ms | 343.15 |
| 60Hz | 16.67ms | 339.73 |
| 90Hz | 11.11ms | 338.58 |
| 120Hz | 8.33ms | 338.01 |

**Finding to confirm natively:** up to ~5.14 logical pixel drift after 600ms at the same input schedule (30 vs 120 Hz). This is integrator discretization, not proof of a user-reported crash or an impossible gate. The native JUnit/Swift tests deliberately demand ≤0.75px; expect them to fail before an integration-only fix.

## Replay contract

`tests/KotlinParity.kt` and `tests/SwiftParity.swift` emit `R|hz|time|y|velocity|distance|score|food|finished` only from native test executables. `tests/run_native_core_tests.sh` compares per-platform vectors and allows <0.12 logical-unit numeric formatting error; tests additionally compare 30/60/90/120 Hz states within each OS.

```bash
cd android && ./gradlew :app:testDebugUnitTest
# macOS: existing CI also runs SwiftParity; with both compilers:
bash tests/run_native_core_tests.sh
```

Test conditions: first flap, 600ms neutral trajectory (no close pillars), rapid double-tap, invalid NaN/Infinity frame time, long 120Hz simulation, monotonic score, death freeze, existing 100ms stall cap and deterministic seam/gate collision checks. Production builds do **not** add telemetry, touch recording, debug traces, or a user-visible control. Renderer, hitbox, gravity and tap impulse remain unchanged in the proposed physics fix.

## Boundaries of verification / acceptance

- Native unit vectors and Android 15/16 emulator / iOS simulator build must pass on the **candidate commit** before merging.
- Real 30/60/90/120 Hz footage with visible inputs, repeated Pause/Continue/Retry and actual phone telemetry is needed to evaluate **tap → visual flap → vertical velocity**, latency and subjective feel. These cannot be certified by numeric replay alone.
- The generator's existing first ten contours and seam cap (≤82 world units per gate across a stream seam) must remain unchanged; no assumption that 8 premium worlds are user-selectable.
- Explicit waiting/running/paused/dead state refactor is **deferred**: simulation owns active/finished, renderer/UI owns paused; existing tests and pause handlers have not exhibited an invalid combined state. Introduce a single state machine only upon a failing counterexample and migration/compatibility review.
