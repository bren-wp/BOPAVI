# Next Android release

## v1.0.55 — Gameplay hazards activate on first real flap (build 185)

**Candidate only — not verified, merged or published.**

Continues the published BOPAVI v1.0.54 release (build 184) and changes
only when actual Gameplay obstacles are drawn. The underlying simulation,
physical collisions, genuine rewards, player saves, skins, sound and game
results remain unchanged.

### Fixes

- Before the first real flap, the 941×1672 Gameplay preview displays
  scenery, Bopi, the actual hearts HUD and the "Tap to fly" instruction,
  without drawing the lower floor spikes, procedural pillars, swinging
  metal maces or linked reward items.
- After the first actual player tap invokes `GameSimulation.flap()`,
  `active` becomes true and the genuine gameplay obstacles, hazards
  and pickups appear together with the active simulation.
- Pause and background/resume preserve active-flight obstacle rendering.
  Retry starts a fresh inactive game and therefore hides them again
  until the next genuine first flap.
- Keep the existing 480×800 simulation coordinates, 18px bird hitbox,
  lower collision boundary, stage progression, procedural world geometry,
  collected currency and saved progress exactly as before.
- Add `FlightHazardPresentationTest` and a mandatory Python source
  contract. Add a real Android emulator screenshot-state test comparing
  unmodified 941×1672 idle and first-flap active images, specifically
  checking that the lower-floor hazard strip visibly changes.
- Preserve all Splash, Home, Shop, Skins, Settings and Game Over native
  controls from the previous stable build, including the 3D Retry/Home
  buttons and signed-in account-free local privacy model.

### Required evidence before publication

- [ ] Privacy/security, dead-code, asset and source-contract audits pass.
- [ ] Kotlin/JVM and Android APK/AAB builds pass.
- [ ] Android 15 Play → first flap → pause → resume → Retry passes.
- [ ] Android 16 API36 smoke and navigation pass.
- [ ] Genuine emulator screenshots confirm that idle obstacles are hidden
      and active obstacles are visible, with no simulated fake game state.
- [ ] Recompare all four priority owner-reference screens and document
      remaining pixel differences; never claim exact 1:1 without proof.
- [ ] Validate accessibility labels, touch targets and stored progression.

**Distribution:** Debug APK is for QA only. Release APK and AAB in CI are
unsigned; an authorized publisher must use the correct private upload
signing key before submitting to Google Play. Keep all earlier releases.
