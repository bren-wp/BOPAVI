# Next Android release

## v1.0.49 — 3D button spacing and reference HUD (build 179)

**Unpublished candidate** based on main v1.0.48. Scope: four owner PNG references at 941×1672 — Loading, Home, Gameplay and Game Over. Changes are concentrated on the controls with the highest source-verified spacing mismatch.

- **Home:** Play, Skins, Shop and Settings now measure actual bold caption width and center the 3D icon and text together. The label no longer sits inside a half-width blank column. The native button retains its full clickable surface, 3D extrusion, movement preferences and navigation.
- **Home wallet:** stop adding a second 21px top margin on top of the device safe-area inset. Keep genuinely visible cutouts/status bars safe; show earned in-game values, not fake samples.
- **Game Over:** Gold Retry and blue Home actions use the same compact measured icon/letter layout. Result values, original location and retry handlers are unchanged.
- **Gameplay/Loading:** Existing real HUD, hitboxes, collectibles, loading-bar rendering and full-screen backdrop are protected by source tests; do not invent 3D or pixel-perfect acceptance without emulator evidence.
- New pure Kotlin `ReferenceButtonCluster` geometry, JVM tests and source contracts check spacing and safe HUD placement.

**Release gates:** full GitHub Actions Android build/lint/tests and 941×1672 emulator captures checked against the four original reference PNGs. No exact 1:1 claim without actual captures. The distinct sad seated Bopi illustration and some environmental details still lack standalone source artwork; do not substitute a baked screenshot for working UI.
