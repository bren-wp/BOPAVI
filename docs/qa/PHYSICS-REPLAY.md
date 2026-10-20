# BOPAVI — Android physics replay and frame pacing

**Updated:** 2026-10-20. This documents the current Kotlin implementation,
not earlier hypothetical 30Hz drift or the discontinued Swift parity build.

`GameSimulation.step(delta)` rejects nonfinite/nonpositive deltas, clamps long
pauses to at most 0.10 seconds, and integrates each accepted time interval in
at most **1/120-second** pieces. At each step, the integrator updates the
-255 tap impulse, 685 base gravity and 365 terminal fall speed, accounting for
difficulty. The bird's body collider remains a radius-18 circle.

`LevelEngineTest` replays 600ms at 30/60/90/120Hz, tests brief
display stalls and invalid time values, and checks position/velocity and
distance convergence against the shared 120Hz integration behavior.
`tests/run_native_core_tests.sh` also validates Kotlin replay
vectors. These tests do not measure input-to-photon latency on physical devices.

### Required QA evidence

- Replay identical input schedules and compare y, velocity, distance and score.
- Exercise frame spikes (100ms), background interruptions, immediate restart.
- Observe power and frame pacing on low/mid/high-end Android hardware.
- Do not change hitbox sizes or travel speeds to conceal an unverified visual bug.
- Require real reference footage and debug overlay proof for pixel-level claims.

Latest Android CI: https://github.com/bren-wp/BOPAVI/actions.
