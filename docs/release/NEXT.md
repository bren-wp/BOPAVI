# Next Android release

## v1.0.5 — Uninterrupted speed and consistent character artwork (build 135)

- Smooth horizontal flight speed across invisible procedural challenge-batch boundaries instead of instantly snapping to the next group's speed. Gate and berry collision sweeps now use the exact scroll displacement.
- Keep original 120 Hz semi-implicit physics substeps, fixed body collision circle, obstacle cadence, food collection, saved high scores and continuous incoming obstacles unchanged.
- Show the selected player (Bopi, Sova or Lisac) in Pause, matching Home, Flight and Results. No new menu, login, network permission, tracking or paid feature.
- Add focused JVM and source-level regression checks for boundary speed continuity, pause art selection and save compatibility.
- Carry forward complete, offline legal texts sourced verbatim from the owner's index.html (11 privacy / 12 terms sections with publisher contact); existing MIT licensing history remains preserved.
- CI and emulator tests must pass before publication. Physical battery profiling and Google Play store approval are not implied.
