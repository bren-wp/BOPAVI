## v1.0.40 — Gameplay reference pass (build 170)

Third screen in the owner's nine-screen visual reconstruction: the active flight scene.

- Replace the former centered textual HUD with a **top-left glass flight badge** combining a real selected pilot portrait and up to three larger red hearts, following the supplied gameplay board. Keep the true single-life rating for finite levels, three lives for endless, and TalkBack's live localized life count. No scoreboard, coin counter or pause HUD.
- Draw the **real** pre-existing finite-level reward pickups that were previously invisible: glossy gold crown coins at physical gate centre, sapphire crystals (the original stage-star reward) at centre +35/-25 and shield/magnet icons at +39/+39. Visibility checks use the simulation's existing collected arrays and show nothing after pickup; endless berries and restorative hearts retain their original rules.
- Add dark-metal/gold/sapphire cap hardware fully inside the original physical cliff caps, so the visual danger matches real body/lip collisions without fictitious swinging hazards.
- Reuse Android Canvas Paint/Path/bitmap assets; no network requests, per-frame bitmap creation, changed physics or new saved-data keys.
- Add JVM tests for finite/endless pickup visibility and update HUD geometry contracts for left positioning and viewport containment.
- Retain harder pillar widths/gaps, Gradle 9.6.0, AGP 9.5.0, API36, Android 15/16 QA, visual screenshots and signed-publication restrictions.

**Pixel audit limitation:** native shapes and layout now follow the reference closely, but the screenshot's complete 3D fantasy art, full-size reference pendulum artwork and exact image-layer assets are still incomplete. Do not describe these as a pixel-perfect match or claim hazards exist when the collider doesn't support them. Publish only when every release gate succeeds.

### Continuation — physically colliding Sky Meadows swing hazards

- Later Sky Meadows gates (indices 4, 7, 10, …) now alternate an upper/lower moving mace, while introductory gates remain clear. The native Canvas render and the 120 Hz swept contact share `SkyMaceHazard` positions/radius; both finite and endless modes use the same physical rules.
- A worst-case hard-mode center lane remains open. Added JVM tests for spawn eligibility, exact rendered-pose contact, mid-step crossing and safe centerline at several generated level seeds.
- The new graphic is an interim local rendering, **not** a pixel-perfect recreation of the larger reference chain/mace artwork. Android emulator screenshots, gameplay replay/fairness review and PR CI are required before production or a 1:1 claim.

### Flight HUD — reference geometry and camera-safe placement

- Compared the actual GitHub Actions Android flight screenshot (1080×2400; run 41126993167) against the supplied flight artwork (941×1672). The screenshot showed the avatar/three-heart card much wider and significantly lower than the reference.
- Reduced HUD layout from 232×68 to 163×51 logical units, adjusted avatar/heart spacing and badge enamel treatment, and removed the non-reference life underline. **No fake extra lives** were added: finite mode still shows one true life, endless three.
- Corrected centered punch-hole handling: the left-aligned HUD is displaced for a real cutout that overlaps its x-range, but not for a center cutout that is entirely outside the badge. Top inset is now cached at the WindowInsets lifecycle boundary, not polled every frame.
- Unit and contract tests cover the new geometry, left/notch intersection, and accessibility preservation. **Pixel-perfect status remains unverified** until new emulator screenshots are captured and compared; world art and animated sprites still differ materially from the illustration.

### Gameplay ready state — one reference-state correction

- Reference: 941×1672 gameplay illustration, including a left lives HUD and a compact tutorial bubble near x=283..656, y=1245..1358.
- Previous Android emulator READY screenshot omitted the life HUD and painted a 306×75 two-line prompt. The game now draws the actual current-life HUD **before first flap** as well as while flying; the finite/endless life count stays truthful.
- A 190×54 coordinate-space glass callout maps to the reference's ~373×113 image-space bounds (max theoretical anchor deviation under six source pixels). English Android locale displays the one-line "Tap to fly" plus a locally drawn hand icon; Croatian retains its existing instructions and secondary control hint.
- Corrected a stale source-level regression test which demanded the earlier red enamel color and 1.2px stroke, while keeping its explicit check for translucent empty hearts.
- Pixel identity still requires full real-device screenshots and source artwork comparison. No claim of 1:1 completion, no release, no new persistence keys or physics changes.

### Honest pixel-diff test tooling

- Added `tools/compare_flight_reference.py`, producing whole-image and five region metrics (HUD, Bopi, obstacles, tutorial, bottom): RGB MAE, RMSE, changed pixel fractions and exact equality. Optional JSON/diff files remain internal QA artifacts.
- Source screenshot resolution is 941×1672, while the last Android emulator capture was 1080×2400. The tool correctly **rejects** direct comparison across mismatched sizes instead of stretching the image and claiming pixel identity. A matched Android screenshot with the same animation state and insets must be obtained before collecting 1:1 metrics.
- Added `tests/test_flight_reference_diff.py` to the CI source audit; local synthetic-image checks passed, including exact-match, single-pixel mutation and size rejection. Production screenshots must still be validated visually.
- Next after the flight artwork and all associated checks genuinely pass: Results / Game Over, followed by Level Complete. The supplied references contain both, so neither is to be skipped.
