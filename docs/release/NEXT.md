# Next Android release

## v1.0.20 — Better owl glide, clearer abilities and frame-rate QA (build 150)

- **Sova / real gameplay:** retains its 8% lower gravity and now caps downward velocity at 315 logical units/s instead of 365. This gives more reaction time under moving pillars during sustained falls, while preserving the same -255 tap impulse and 18px body collider as every other pilot. The change is bounded to the Sova pilot; all other physics remains unchanged.
- **More readable character powers:** the native HUD ability chip now uses an accessible distinct high-contrast tint for each pilot. Lisac's one-use shield is clearly green while ready and red/pink after consumption, backed by its existing changing Croatian TalkBack status; a slim visual accent stays inside the existing HUD rectangle, left of native pause control.
- **Pause and results:** a concise ability explanation appears in the native Pause dialog, and Results' accessibility description explains the selected pilot's advantage rather than only giving a generic ability name. The Home pilot cards likewise describe numerical characteristics (Bopi +6% speed, Sova less gravity/fall, Lisac one absorbed hit).
- **Performance and compatibility:** no bitmap generation, network call, additional fragment, or new animation in the drawing loop. The HUD tint and status are inexpensive enum lookups. Existing saved scores, player choice, store/legal links and all three difficulty settings are unchanged; historic CLASSIC game instances keep 365-unit descent.
- **Regression QA:** deterministic Kotlin/JVM checks verify actual sustained owl descent, equal collider and flap impulse, contrasting Fox shield states, and matching pilot movement after one second at 30, 60 and 120 Hz. The source contract guards native HUD and all screens; Android Lint, JVM tests, Android 15 gameplay/screenshots, Android 16 smoke, package integrity, privacy and dead-code audits remain mandatory.
- **Limitations:** physical-device TalkBack comfort and subjective play balance should still be checked manually; passing GitHub CI does not constitute Google Play/F-Droid approval or official distribution.

