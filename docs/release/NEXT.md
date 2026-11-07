# Next Android release

## v1.0.56 — Home and Splash 941x1672 owner reference alignment (build 186)

**Draft candidate only — not yet tested in Android emulator, merged,
signed or published.** Continues the merged and published BOPAVI 1.0.55
(build 185). Only Home and Splash visual presentation plus pixel QA
are being changed, not gameplay physics, user progress or game rules.

### Home: native artwork and 3D controls

- Reanchor the original 512×512 three-layer animated Bopi to a square
  660px viewport at (100,282) on the owner's 941×1672 design grid.
  The sprite's ACTUAL painted alpha begins at y≈393, with feet near
  y≈830. All three live body/wing layers still share the same bounds,
  and selected non-default skins keep their identities.
- Reposition/enlarge the original local BOPAVI winged crown logo from
  (122,112,697×273) to (110,100,720×285).
- Bring the top-right real coin/star balances back toward owner y≈21
  in genuine immersive full-screen mode. A visible status bar or physical
  right/centre camera cutout still protects the relevant native tappable
  wallet; no sample currency from reference art is inserted.
- Increase measured raised Play, Skins, Shop, Settings labels while
  preserving compact icon/text spacing and native accessible click targets.
  Introduce a warm golden Play arrow and pearl/ice toned text bevels.
- Strengthen softly defocused foliage at bottom corners in the local
  shared offline scene, keeping user controls as independent Android Views.

### Splash: native Bopi, loading bar and real preparation state

- Move the shared square pilot/wing art from (90,365,704×704) to
  (110,280,670×670): the painted source starts near y≈386, immediately
  after the wordmark instead of hanging too low.
- Broaden the winged gold/navy/cyan loading housing to owner bounds
  (161,1318)–(780,1422). The blue highlight remains INDETERMINATE,
  not an invented 66% loading state. Genuine completion remains driven
  exclusively by the actual level-preparation callback.
- Preserve offline reference-only animated Loading screen artwork,
  local feathers/rings, correct caption, accessibility and lifecycle.

### Pixel QA evidence and mandatory gates

The last published v1.0.55 native screenshots at 941×1672 compared
with the owner's *unchanged* supplied PNGs had total RGB MAE of
Home **72.543**, Splash **74.555**. Full ROI rankings and original
emulator run provenance are in
`docs/qa/OWNER-HOME-SPLASH-BASELINE-v1.0.55.md`.

- [ ] All Python source/privacy/dead-code/release-contract checks green
- [ ] PIL art generation reproducibility and icon tests green
- [ ] Kotlin/JVM tests and unsigned Android APK/AAB build green
- [ ] Android 15 Home/Play, first flap, pause, Retry and lifecycle QA
- [ ] Android 16 API36 smoke QA
- [ ] Fresh true Home and Loading captures in original 941×1672
- [ ] `tools/qa_two_owner_reference_screens.py` run on both unmodified
      owner PNGs and both latest emulator PNGs; ROI improvements checked
- [ ] No clipped labels, overlapping native actions or inaccessible
      wallet/cutout areas on supported portrait and smaller displays

**Pixel-perfect 1:1 is a target, not currently verified.** Some actual
logo, Bopi and painted background artwork still differ from the owner's
original illustrations; changing layout values is not proof of identity.
Never flatten a screenshot into controls or counterfeit earned currency.

**Signing:** Debug APK is for QA; release APK and AAB built by CI are
unsigned. An authorized publisher must sign with the correct existing
private upload key before submitting to Google Play. Preserve all
previous public releases.
