# Next Android release — visual redesign, phase 1

## v1.0.37 — Reference-inspired native navigation (build 167)

Nine user-provided visual references have been mapped to Home, Loading, Gameplay,
World Map, Shop, Skins, Settings, Level Complete and Game Over.

### Implemented in this phase
- New **SKINS**, **SHOP**, and **LEVELS** entry points on Home, preserving existing
  options, records, Croatian accessibility labels, and the three usable pilots.
- Native, responsive Skins selector using real Bopi/Sova/Lisac portraits and
  previously saved `arcade_pilot` preference; no image-backed fake buttons.
- Offline Shop using the six existing earned-coin perk items, real costs and
  inventories. Genuine purchases and record-bonus claims, never simulated
  balances, non-existent gems or external payment systems.
- Real level map for all eight existing generated worlds; locked/unlocked status
  comes from saved per-world stream frontiers, while earned stars come from saved
  level ratings. Locked levels are disabled, not only painted as locked.
- Finite level start, genuine victory/failure result, actual next-level unlock and
  stars from physical pickups. Level completion avoids double counting wins
  after `recordRun`. Replay and Next Level retain the selected world.
- The existing 18-unit collider, challenging pillars, Android 15/16 lifecycle
  fixes, untouched on-device save schema, offline assets, Gradle 9.6, AGP 9.5,
  compile/target 36 and min SDK26 stay unchanged.

### Not yet pixel-identical
This is a real first-stage implementation, NOT a pixel-by-pixel claim.
Reference art is supplied in this conversation and isn't automatically
available to GitHub Actions as app drawable resources. Reference-perfect
illustrations, typography, 3D button art and exact 941 × 1672 raster
comparisons still require a proper reusable art asset pipeline, plus
physical-device validation. No Play or cloud-payment features are fabricated.

Publish only after source contracts, Kotlin tests, API35 genuine gameplay QA,
API36 smoke, screenshots, release package verification and checksums are green.
