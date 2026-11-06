# Next Android release

## v1.0.53 — Gameplay floor 3D and Game Over Retry/Home (build 183)

**Candidate only — not yet verified, published or Play-signed.** Continues
BOPAVI v1.0.52, with the merged Splash/Home adjustments intact.

- **Gameplay (Sky Meadows):** add five original gold/brass beveled spikes and a
  rock/grass terrace along the **existing collision-solid ground plane**.
  The visual tips begin at logical `y=753` or lower; a bird body with its
  genuine 18-unit radius is already prevented by GameSimulation from entering
  the space. Keep the unchanged 480×800 simulation, pillar/mace colliders,
  first-flap lifecycle, moving rewards, scores and saved progress.
- **Gameplay HUD:** redraw active hearts with wider/taller red, navy-outlined,
  shadowed 3D lobes, preserving real life count, exact heart centers, portrait
  and accessible life status.
- **Game Over:** separate pressure-aware, curved **gold Retry** and **sky-blue
  Home** enamel, with layered rims/extrusion, upper reflections and larger
  embossed native button labels. Keep one accessible, working click target for
  each action; tapping Retry must still start real new gameplay and Home must
  navigate to the actual home screen.
- **Game Over Bopi:** tilt the original blue aviator's seated head gently
  and lift the inner eyebrows to express disappointment instead of anger;
  leave other selected skins and result data untouched.
- **Game Over Sky Meadows:** use the same enhanced, offline island backdrop
  as Home, with twilight tonal grading. Other worlds retain their world art.
  Never fake the numerical run statistics or replace Bopi with a flat screenshot.
- Add JVM collision-boundary unit tests and mandatory source/UI contracts.

## Screenshot baseline / remaining pixel differences

Actual **published v1.0.52**, 941×1672 snapshots from GitHub run
`42310082710`, original user references unchanged, RGB MAE (0–255):
Splash 78.53; Home 72.65; Gameplay 76.05; Game Over 74.91.
Largest ROIs are Gameplay lower ground spikes 119.93; Game Over blue
Home 87.61; Game Over gold Retry 81.84; Gameplay lower swinging mace
79.33; Gameplay portrait/hearts HUD 74.55.

The original reference shows some spike tips ABOVE the game's current
physical ground collision line. This release intentionally keeps every
hazard-looking spike inside the *real* existing floor collider so there
are no misleading non-collidable hazards or surprise physics changes.
Therefore **literal 1:1 has not been achieved**. The fantasy scenery,
pilot detail and animated frames remain subject to pixel/ROI comparisons.

**Release gate:** inspect a new genuine 941×1672 Android 15 Gameplay/Results
capture and API36 smoke, Kotlin and Python tests, Android APK/AAB build,
no dead-code/privacy/security failure, no lost tap target or save data,
and update the ROI report using actual captures. Do not suppress CI failures.
No release until mandatory checks pass and visual regressions are reviewed.

**Signing:** CI release AAB/APK are unsigned. Only an authorized publisher
can sign the upload artifact using the correct private key.
