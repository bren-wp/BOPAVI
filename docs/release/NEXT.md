# Next Android release

## v1.0.55 — Gameplay hazards activate on the first real flap (build 185)

**Candidate, not yet signed or published.** Continues the full merged
v1.0.54 Splash/Home work from PR #218 on the current main branch.

### Gameplay state-to-rendering fix

- Before the FIRST actual player tap, show the legitimate local fantasy
  scenery, Bopi, real lives HUD and accessible "Tap to fly" instruction.
  **Do not show dynamic floor spikes, top/bottom generated pillar gates,
  hanging metal mace hazards or gate-linked collectible rewards while idle.**
- First real flap calls `GameSimulation.flap()`, changing `active` from false
  to true, and the same state authorizes every dynamic hazard's rendering.
- Pausing, backgrounding and resuming the SAME active run preserve visible
  hazards. Retry creates a NEW inactive GameSimulation, so the idle screen
  is once again clear. Completed runs transition to Game Over.
- Actual procedural gates, moving maces, reward availability, physics,
  480×800 game world, y=753 floor, 18px body collider, score, wallet,
  persisted progress, controls and skin selection remain unchanged.
- Keep local Game Over 3D Retry/Home, seated Bopi, existing Home/Shop/Skins
  controls, native safe areas and privacy/security protections unchanged.
- Add new JVM tests and mandatory source checks to guarantee that future
  visual changes cannot reintroduce obstacles before the first actual flap.
- Require separate genuine Android idle and active gameplay screenshots
  at 941×1672; verify them before merge or publication.

### Validation gates

- [ ] Source audit and all existing offline/security/asset tests
- [ ] Kotlin/JVM state regression tests and lint
- [ ] Build Android APK and unsigned AAB
- [ ] Android 15 first-flap, lifecycle, collision, pause and Retry QA
- [ ] Android 16 API36 smoke tests
- [ ] Inspect real idle/active Gameplay captures and reference geometry
- [ ] Re-run full-screen/ROI comparison for Splash, Home, Gameplay, Game Over
- [ ] No regression in accessibility or real game/saved results

**Pixel parity is not claimed.** The owner-provided illustrations are
reference targets, not functional Android screenshots or replacement assets.
Only genuine new emulator screenshots can confirm measured improvements.
Publisher private upload signing remains necessary; CI APK/AAB artifacts are
unsigned.
