# BOPAVI v0.2.21 — focused code, performance and dead-code review

## Native gameplay
- Android `GameSimulation.kt` and Swift `BopaviCore.swift` now share an
  allocation-free segment-to-circle berry collision predicate. Both sample
  the old opening center, bird position and gate movement for the current
  bounded physics slice. This closes endpoint-only pickup gaps without a
  magnetic or automatic reward, score boost or altered body hitbox.
- Swept collision does not replace the existing physical pillar collision;
  only food pickup is swept. Separate Kotlin/Swift regression tests verify
  contact, near misses, tangency, zero movement and nonfinite values.
- Cached HUD text is rebuilt only when the actual score/food count changes;
  native accessibility labels receive the same conditional treatment.

## Dead code and allocations
- Eliminated per-result image ID arrays for eight worlds and nine portraits
  from Android's screen path; results now directly use the single Bopi and
  fixed first-world visuals mandated by the simplified UX.
- Historical world, pilot, inventory and progression save handlers and art
  generators remain for versioned backup compatibility and native tests.
  Their existence must not be mistaken for visible menu screens.
- All Kotlin and Swift source and asset linkage remain guarded by
  `tools/audit_native_dead_code.py` in CI, including Android resource lookup
  and iOS asset bundle parity.

## Save integrity and privacy
- Only the maximum real per-flight berry total is added as
  `endless_food_best` on each local platform. Legacy save JSON version 6
  receives an optional `bestFood` value; older backups safely default to zero
  without lowering an already earned berry record.
- Run counters saturate before incrementing to avoid integer overflow.
- No telemetry, INTERNET permission, registrations, IAP, ads or third-party
  tracking introduced. The game remains offline.

## Remaining limitations
- Automated emulator/simulator results are not proof against all vendor GPU
  faults, OS low-memory kills or device-specific crashes. User logs are
  required to identify a separate physical-device root cause.
- A future destructive removal of historical save format/API logic should
  only occur with an explicit migration policy and compatibility tests.
- No claim of pixel-perfect parity with unspecified external reference
  screenshots is made by this source audit.
