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

### Gameplay flight — body/obstacle horizontal reference alignment

- The supplied active-flight picture places Bopi around the 36–38% horizontal band. The older emulator screenshot placed the character around 26% (previous logical center x=126/480).
- Moved the real bird body/collider to x=175/480 and projected **all** current/incoming gates, maces, coins, crystals and powerups by the same 49-unit offset; world scroll, first-obstacle reaction distance, collision separation, tap impulse, gate spacing and finite progress denominator remain invariant.
- All bird-attached visual effects (pickup/hit bursts, auras, ribbons, wind trail and shield/magnet) now use the live simulation x coordinate; none remains frozen at 126.
- New JUnit and Python tests validate the reference anchor, eight biome families, same initial relative gate distance and a physical moving mace's translated hitbox.
- The sprite/background artistry is not yet pixel-identical. Screenshots from the new Android build are still required before accepting the screen.

### Native gameplay screenshot at source-reference dimensions

- Android 15 emulator QA now additionally opens an actual fresh flight in a 941×1672 viewport after normal UI, pause, results, resume, rotation, and loading checks finish. It captures `android-gameplay-ready-reference-size.png` through Android's own screencap API and verifies its PNG IHDR dimensions.
- The override is restored on normal completion and through the existing test-exit trap on failure; it cannot substitute a mocked UI or misleading resized screenshot.
- The image is an unedited native Android capture. Matching resolution is necessary but not sufficient for a pixel-perfect result: compare the same gameplay state, animation pose, viewport insets and original art using `tools/compare_flight_reference.py`.

### Sequential screen-by-screen pass 1: Splash / Loading

- The owner requested a new sequential review beginning with Splash, followed by Home, Levels, Gameplay, Pause, Game Over, Level Complete, Skins, Shop, and Settings.
- Inspected the actual offline Android art and native Loading View. The old Loading code stretched the 780×236 winged logo to a 721×282 box and used a square **portrait** crop instead of the winged full pilot. This is visibly unlike the approved 941×1672 Splash/Loading reference.
- Replaced the portrait with the actual full Bopi body and both wing layers from the existing verified local sprite family, keeping the same 512×512 shared coordinates and decoding them once. The winged logo now retains the original intrinsic aspect ratio, with geometry derived from the provided reference.
- The illustrated sky and local platform assets are still visibly simpler than the reference's dense fantasy environment. This step is NOT a pixel-identical pass; a true screenshot comparison must follow emulator capture at an equivalent viewport and loading state.
- Kept the system-managed Android launch splash separate from native level preparation: cold start continues directly to Home, avoiding double splash/fictional wait, while actual level preparation renders Loading and completes as soon as generated data is ready.
- Added JVM tests for reference bounds and source aspect fidelity, plus a CI source contract for real offline sprite layers, correct cleanup and no artificial sleep. **Next: Home**, after Splash visual/CI checks.

- Strengthened Android Loading QA: after the normal 1080×2400 preflight preview, the real native `FlightLoadingView` is also captured at **941×1672** (reference source size) with an asserted PNG header/IHDR and Android accessibility state. A `finally` block restores system viewport overrides and returns to Home.
- The capture is **evidence to evaluate**, not proof of visual identity. The poster-art landscape remains a major divergence from the uploaded richly rendered reference.

### Sequential screen-by-screen pass 2 — Home (reference 941×1672)

- Inspected the owner's first image against the real prior Android 1080×2400 / 720×1280 Home captures. The prior screen had huge full-width currency banners, a narrow bird, a large empty blue band before PLAY and all three bottom actions near the OS nav bar.
- The Home resource capsules are now compact and right-aligned, displaying actual earned coins and world stars, never the screenshot's fictional 1,250/85. The green plus area belongs to the genuinely clickable capsule: coins open Shop and stars open Levels (no new fake currency or IAP).
- Increased the Bopi hero envelope on tall phones, widened the brand image proportionately, aligned PLAY (~73% viewport width), SKINS/SHOP (~79%) and Settings (~63%) to the reference's compact centered buttons, and moved spare vertical flex **after** the footer instead of inserting an empty gap above PLAY. Compact devices keep their original hero cap and full-size native touch targets.
- Updated native icon vocabulary: coin, shop awning, three-tier leaderboard, speaker. Achievements now uses a real trophy instead of a generic star. Removed the unapproved brand tagline from SVG raster source. No external image generation or packaged screenshot overlays.
- Expanded actual Android viewport QA to capture Home at native 941×1672 as well as compact 720×1280 and tall 1080×2400; image content remains artwork-limited, so **do not claim pixel-exact identity yet**. Next after a verified Home pass: **Levels (world map)**, the owner's fourth reference.
