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

### Android 15 QA compatibility with the live ready-flight semantics

- Diagnosed run 41133128581 using its uploaded `android-current-ui.xml`: PLAY did open the real 1080×2400 `android.view.View`, clickable and with `Bopi leti. Dodirni za zamah krilima. Životi: 3/3.` accessibility state. The test still only searched for an older `DODIRNI ZA LET` or English `TAP TO FLY WITH` substring, causing a **false negative** before the gameplay screenshot.
- Corrected the gate to require the actual clickable native GameView, a real `ŽIVOTI:` count, the real flap hint, and absence of the Home PLAY button. A fake splash, Home or non-interactive decorative image still fails. Added a code regression assertion. No gameplay physics or accessibility text was weakened.

- Calibrated width ratios against the actual 941×1672 owner image: top resource rail 51% of viewport, primary PLAY 67%, secondary SKINS/SHOP row 72%, Settings 54%, and bottom shortcut row 64%. Reference art requires ample negative space around the live controls; ratios stay constant while normal Android dp font/touch minima remain enforced.

### Sequential screen pass 3 — Levels, native world map

- Replaced the old vertical list of stages with a real `LevelMapScreen` and `LevelMapLandscape`: a 941×1672 reference coordinate layout with three floating island regions, painted rope paths, crystals/lava, local sky backdrop, and genuine accessible Android level nodes. First map page shows Sky Meadows, Crystal Ruins, Volcano Peaks; arrow navigation still exposes all eight existing biomes in grouped pages, without changing `chosen_world` save keys.
- Nodes select the current visible six-level window around the genuine saved frontier; locked nodes are physically disabled and cannot call `onPlay`. Frontier is capped for the final stage so 1,048,577 cannot be passed into level generation. The golden PLAY LEVEL button launches the selected real playable stage. The existing Android QA text/role requirements remain: LEVEL MAP, PLAY LEVEL 1/0 STARS and disabled LOCKED LEVEL 2.
- Changed `worldStars` display aggregation to the first 20 recorded stages instead of 10: max 20 × 3 = 60 reference stars, based on real persisted ratings only. No rewards or saved data are added or fabricated.
- Native touch targets remain at least 48dp; drawn landscape and actual click targets share the same reference transform, while top/bottom display insets are respected. Background is local, paints reuse their objects; no image generation, remote network, or layout-sized raster art.
- The reference still has substantially richer 3D scenery than current local game assets, so this is a functional world-map reconstruction, **not a verified pixel-identical result**. Capture and compare a real Android 941×1672 screenshot before signing off.
- Next after visual verification: **Gameplay** (first flight), followed by Pause, Game Over and Level Complete.

- Added an Android 15 emulator snapshot of the actual native Level Map at exactly **941×1672** (`android-level-map-reference-size.png`), with explicit PNG IHDR validation. The emulator first verifies all three world plaques and real locked/unlocked touch controls; it restores its display overrides before continuing the existing finite Level 1 launch test. This is genuine evidence for a future image comparison, not an automatic pixel-identical certification.

### Sequential screen pass 4 — Gameplay: truthful first-flap visual verification

- Replaced torso-only flight HUD avatar with a cropped close-up from the **pre-rendered full original Bopi portrait** for each of the 11 saved skins, while keeping the original body-only layer and both animated wings unchanged in actual flight. One immutable 512x512 source crop, decoded on attach and released on detach; the old torso remains a crash-safe fallback.
- Corrected the reference-sized emulator ready-state detector to require the real clickable `GameView`, actual `Životi:` count and current Croatian flap hint. The obsolete `DODIRNI ZA LET` wording could never pass on the live game.
- Native QA now captures **both** untouched Ready and real first-flap Active frames at 941×1672. A first genuine touch changes the same debug-only simulated flight; the QA hold freezes that real frame. A byte-identical screenshot fails; both PNG IHDR dimensions and process survival are asserted. No mock screenshot, fabricated reward or changed 18px collider.
- This is one measurable gameplay/UI pass, **not** visual 1:1 certification: the source's high-detail islands, exact Bopi painterly silhouette, waterfall/hazard artwork and animation-state matching remain materially different.

- Diagnosed two QA defects from the surrounding sequential screens. `qa_android_reference_menus.py` passed PNG output through a text-decoding `adb()` helper, so even an otherwise correct world-map screenshot would fail or corrupt. It now uses the existing binary `screenshot()` utility with exact dimensions and restores display overrides. Two navigation checks use the actual current TalkBack flap prompt and live-life description, not the removed legacy text.

### Sequential pass 5 — Pause (consistent art treatment, no standalone reference)

- Native Pause now retains a single modal instance, with a cyan-framed navy panel and a correctly framed full-wing Bopi portrait; touch targets stay at least 55dp high, gold Continue and blue Retry/Exit actions follow the game UI family. The centered dialog uses 80% of the physical viewport (capped at 420dp) and the existing scroll-safe container for compact devices.
- Fixed finite-stage Retry: it now selects the exact saved active `(world, level)` rather than silently jumping to the endless mode. Pure `PauseReplayTarget` and JUnit tests cover finite versus endless; no persistence schema changes.
- Prevented repeated Back/focus transitions from stacking Pause windows; a dismissed dialog releases its own reference. Outside touch cannot accidentally resume, while Back/Cancel can safely resume the current live run.
- Added a real Android screenshot route `android-pause-reference-size.png` at 941×1672, requiring Home → actual first flap → system Back → exactly one Pause dialog → Resume. PNG dimensions are checked in the binary screenshot helper, not invented from text.
- There is no directly supplied Pause reference, so **a 1:1 visual match cannot be proven**. Next in sequence: Game Over/Results (the owner's first supplied screenshot).

### Sequential screen pass 6 — Game Over / Results (owner reference 941×1672)

- Added an independent loss-only, fully native `GameOverScreen` using the shared 941×1672 reference camera. The original win/Level Complete UI remains untouched for its later dedicated pass. Logo lettering now has separate white GAME and coral OVER layers, real Bopi portrait, painted green floating rock podium, three cream full-width stats rows, a gold RETRY and blue HOME with 48dp minimum touch targets.
- All displayed data come from the finished `GameSimulation`, via pure JVM-tested `GameOverStats`: Distance in engine units (no false meter conversion), Food Collected and Score in endless mode; Distance, Coins Collected and Stars Collected in finite mode. There is **no** real gem currency in this build, so we do not invent a Gems Collected count from the mock reference.
- Loss values are recorded through the existing `progress.recordRun(g)` exactly once before routing; Retry starts the same finite world/level or a new endless run, Home returns to the actual native menu. Results remain accessible and work offline, avoiding bitmap mockups.
- Current painterly Bopi art does not contain the exact sad sitting pose from the screenshot, and backgrounds are simpler. **Do not assert pixel-perfect 1:1** without a directly comparable Android screenshot.

- Emulator UI QA now captures a real Game Over screenshot at 941×1672 after the actual flight collision, checks loss-specific Distance/Food/Score and refuses fake gem/coin counters, then performs the true RETRY transition. The old source test for the generic Results layout is retained for the future Level Complete branch.
