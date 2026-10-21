# Next Android release

## v1.0.11 — Endless visual precision and smooth scenery (build 141)

- Fix long-distance scenery freeze/jitter: collision/world positioning already used precise Double distance, but dynamic parallax, sky camera oscillation, mist, foreground leaves and fallback landscape were still using a Float rounded after every physics step. Beyond large marathon distances the Float value stopped changing on small steps, creating visible discontinuity.
- Compute scenery phases using the existing Double monotonic distance, wrapping at each small visual tile period before conversion to Float. No new animation timers, allocations, resources, SDKs or permissions.
- Preserve the original multi-layer landscape speeds and subtle drift: 3-depth islands, background camera sway, clouds, particles, foreground vegetation, ridge and fallback biome silhouettes. Reduced-motion mode also freezes ridges in the rare fallback renderer.
- Dedicated JVM and Python regression tests cover movement at 2^25 world units plus a 0.25px step, invalid phases, all three parallax layers and zero new changes to the swept collision, r18 body or 120Hz physics.
- Existing three playable pilots, local storage, privacy/terms, hearts/berries, HUD, pause and Play imagery retained.
- **Release gate:** full source/privacy/dead-code audit, sound/art verification, Android Lint/JVM/APK/AAB, Android 15 gameplay/collision screenshots, Android 16 navigation and versioned GitHub Release. Hardware battery tests and Google Play approval remain external.
