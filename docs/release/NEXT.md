# Next Android release

## v1.0.49 — Compact 3D control spacing and HUD correction (build 179)

**Candidate only, not yet published.** Based on v1.0.48 main; scoped to owner reference Loading, Home, Gameplay and Game Over at 941 × 1672.

- Home Play, Skins, Shop, Settings 3D buttons now center the visible icon and measured text together, avoiding empty half-button text columns.
- Game Over Retry and Home native controls use the same measured 3D icon/text geometry, without altering retry or navigation actions.
- Home top-right coin/crystal HUD no longer stacks a second reference offset on the physical safe inset.
- Added shared `ReferenceButtonCluster` calculation and source/JVM regression tests.
- No fake currency, game pickups, gem wallet or clickable screenshot overlays.

**Before release:** CI source audit, Android tests and APK/AAB build, and real 941 × 1672 screenshots of all four screens compared against original owner references. Existing distinct sad seated Bopi asset and some background art remain visual gaps; no pixel-parity claim is made.
