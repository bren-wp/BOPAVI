Scene-to-world alignment: world 1 Crystal Sky now maps to icy cyan crystal artwork, and world 6 Enchanted Forest uses the woodland scene plus dedicated luminous foliage overlay (formerly world 0 duplicate). This is a concrete correction, not 1:1 illustration fidelity.

## v0.1.66 — Tall-screen world scene coverage

Authentic Android CI screenshots at 1080×2400 showed broad plain-color sky bands above/below the fixed 480×800 simulation. Both native renderers now aspect-cover the far biomes across the entire physical screen but retain the original exact physics/HUD/touch transforms. Retest using emulator/device PNG comparisons. This removes one concrete composition gap, **not** the remaining substantial painterly 3D illustration mismatch.

## v0.1.65 — Accurate device screenshots and pilot-navigation regression

- Production main CI after v0.1.64 failed with real launcher foreground at pilot confirmation, rather than a confirmed in-game idle state. Diagnostic XML from CI artifact `11774864120` establishes the focused UI looked like **Pixel Launcher**. This must not be misreported as a proven app crash or ignored as a test success.
- CI now preserves crash/activity/window logs and only allows bounded recovery of a still-alive BOPAVI task hidden by focused launcher. The real idle GameView is still mandatory; a process crash, false button, wrong navigation destination or injected screenshot fails the build.
- Removed unreferenced packaged composite Hero art, **not** the source Bopi vector used to generate live sprites. Runtime sprite rendering remains unchanged.
- Visual comparison against the user-supplied 3D painting refs remains an open task on **all** screens. No new reference or generated image was placed into chat or presented as an authentic game screenshot.

## v0.1.64 — Personal records and reference screen fidelity

The user supplied additional reference images for all eight world biomes, main menu, character choice, levels, profile, settings, pause, results, loading and brand guide. These are **desired style/geometry targets**, not verified live app output. Source-backed changes in this iteration focus on **Profile/Settings and personal records**, maintaining genuinely interactive controls. The visual gap remains large in the illustration style and lighting, and cannot honestly be called pixel-perfect.

- **Implemented**: single-player top-ten ranking with highlighted personal best; historical nicknames normalized; no public/cloud scoreboard; removed redundant saved-on-device UI wording; safeguarded voluntary backup exports on both platforms.
- **Still pending for 1:1**: painterly 3D Bopi feathers/goggles, dense castles/clouds/waterfalls, foreground bloom and per-world 3D islands; redesigned crown HUD and pause/result cards; objective screenshots and pixel-overlay review at matching resolution.
- **Production privacy policy remains explicit** that gameplay data persists in platform app storage and users control voluntary backup exports. Hiding irrelevant consumer copy must not hide mandatory truthful data disclosures.
- **Real artifact rule**: only emulator/device screenshots may be published as `BOPAVI-Android-*.png` / `BOPAVI-iOS-*.png`. Never commit UI references as if they were an actual running app.

## v0.1.63 — World-specific gesture concepts versus actual gameplay

The supplied reference set shows a crystal-space and enchanted-fantasy world with portrait HUD, crown coins, enormous sculpted cliff faces, floating cities and exceptionally detailed 3D mascot feathers. The *new* reference world grid differentiates tap and vertical dragging. This iteration implements the **actual input/physics/UI difference** in the native apps rather than a static illustration.

| World ID | World | Real control |
| --- | --- | --- |
| 0 | Zeleni otoci | TAP (flap) |
| 1 | Kristalno nebo | POVUCI (vertical drag) |
| 2 | Lava vrhovi | TAP (flap) |
| 3 | Noćni dvorci | POVUCI (vertical drag) |
| 4 | Biserno more | TAP (flap) |
| 5 | Nebeske kule | POVUCI (vertical drag) |
| 6 | Čarobna šuma | TAP (flap) |
| 7 | Zvjezdani bezdani | POVUCI (vertical drag) |

**Remaining visual gaps:** background castles and rock textures, glass HUD bevels, Bopi's eye/goggle depth, dynamic particles, and scene-to-scene 3D lighting still diverge from the user concept art. Real Android/iOS emulator screen captures and objective geometry/visual comparisons remain required before describing any screen as 1:1.

## Iteration 34 — feather silhouette and floating-world depth (v0.1.61 candidate)

**Observed remaining concept gap:** Bopi's reference has separately shaped translucent feather vanes and reflective pilot goggles. Existing SVG wings have very few wide shapes. The reference flying scene combines large multidimensional castles, misty waterfalls, forest canopies and sculpted floating cliffs; the prior deterministic 2D world generator only had a few broad polygons and plain water streaks.

**Actual shared-source change:** the approved Bopi SVG now contains 15 individually marked left/right feather paths, including layered blue feather segments, white ribs and soft reflective highlights. The existing build splits these paths from the body, preserving actual wing-flap motion on both native platforms. Goggle bevel reflections and scarf edging are also refined. The eight deterministic generated world sources now add five curved waterfall strands, translucent drifting-spray **appearance** (static scenic pixels), faceted rock chips, clusters of miniature crowns and receding bridges; all live gates, HUD, characters, physics and saved values are unchanged.

**QA gate:** the GitHub graphics job must render all eight 960x1600 world textures and nine skin/wing/portrait collections and verify byte-for-byte Android/iOS equality; emulator gameplay captures and Swift/Android smoke tests remain mandatory. The new art is stylized vector rasterization, **not 1:1 3D concept art**; no static screenshot is substituted for running gameplay.

## Iteration 33 — full Settings sheet and selected-pilot hero (v0.1.60 candidate)

**Source-derived reference difference:** the reference POSTAVKE page has a wordmark + large flying Bopi above a single tall royal-blue, cyan-edged panel. v0.1.59 used centered wordmark, separate tile sections and no saved-pilot hero in Settings. The source had genuine persistent music/effects/haptic/difficulty controls but lacked this presentation hierarchy.

**Implemented in native controls (both systems):** one responsive premium glass sheet over the existing world, with independent native sliders, switches, difficulty tiles, profile name editor, offline backup/restore and navigational actions. The Settings hero now uses the genuinely selected existing Bopi portrait rather than generic or composite screenshot art. Android uses GradientDrawable; iOS uses a cached gradient CAGradientLayer resized only on layout. Both retain safe-area scrolling and VoiceOver/accessibility semantics. The explicit HOME navigation remains outside the sheet; Pause does not navigate to this screen while a run is active.

**Proof and limits:** Android QA already captures `android-settings.png`; release staging now checks that file's PNG header, dimensions and size, then publishes it unchanged along with a SHA-256 checksum. A reference illustration **is not** evidence of actual runtime pixels; real screenshot-diff review remains required on Android and iOS. The detailed 3D Bopi art, dimensional background, custom glossy fonts and concept-only cloud/diamond/logout controls are still absent. No claim of pixel-identical 1:1.

## Iteration 32 — cached pilot aura in live gameplay (v0.1.59 candidate)

**Reference disparity:** the 12 October BOPAVI flight concept surrounds Bopi's goggles, animated wings and scarf with soft cyan/gold light. Existing actual Android/iOS gameplay renders the intact nine-pilot 2D sprites and physical wing beats but lacks this specific depth cue behind the pilot.

**Change:** both platform renderers now paint the same 74-unit radial Bopi light (gold center, sky-blue middle, transparent edge), using one cached Android `RadialGradient` or one lazy iOS `CGGradient`. It gently expands by up to 13% from the real 240 ms `flapPulse`. Reduced-motion users get a steady, non-pulsing light. The real selected skin, separately animated wings, interactive controls, gameplay simulation, collision radius, scoring and local saves are unchanged. No new image, external library, purchase, network permission or tracking is added.

**Acceptance still required:** Android API 35/36 and iOS simulator screenshots, startup/gameplay smoke, screenshot-difference review and cross-platform compilation. The supplied concept's detailed 3D feather and world art are still not reproduced; this does **not** establish 1:1 visual fidelity. Do not use reference artwork as a runtime screenshot.

## Iteration 31 — live profile gold rail and reference card hierarchy (v0.1.58)

**Observed disparity:** previous v0.1.57 profile had only a text level strip under the portrait, tiny icon-only medals without unlock conditions, and world previews with no rank markers. The concept shows a gold progress rail below the large pilot portrait, descriptive achievement tiles and gold world-number badges.

**Implemented:** Android `ProgressBar` and iOS `UIProgressView` now show authentic saved-world stars `min(worldStars(chosenWorld),30) / 30` under the avatar, rather than inventing character XP or flight metres. Four medal cards show actual achievement thresholds with locked/unlocked states from original data. Top-world cards now overlay true ranking numbers on original game assets; taps still open actual level lists. Android/iOS native labels remain dynamically sourced from the save and readable with assistive technology.

**Reference constraint:** the profile illustration in the conversation contains fabricated numbers and high-end painted 3D assets. These are not copied into the local save, and pixel-for-pixel 1:1 is **not** verified. QA must compare the new runtime emulator and simulator screenshots on narrow/large displays; feather rendering, typography, decorative medal assets and lighting are still visually different.

## Iteration 30 — real offline profile, achievements and worlds (v0.1.57)

**Reference:** new BOPAVI PROFIL concept alongside the ten previously supplied game references. The concept includes blue/gold portrait, live metrics, four achievement seals, three favorite worlds, orange Edit Profile and a persistent four-tab footer.

**Before:** both platforms showed a full-width stacked portrait and name, followed by separate results rows. No profile achievement badge row, top-world gallery or bottom tabs.

**After:** portrait and stats share a compact horizontal navy/cyan hero card. Four achievement seals display *computed* earned/locked status from local completed-world progress, wins and collectibles. Three world illustration cards are ranked by saved stars and scores, with real navigation to levels. Gold editing CTA, existing settings and character gallery, and accessible four-tab footer are interactive on Android and iOS.

**Data boundary:** all scores/balances and selected pilots come from ProgressStore. No manufactured level, experience, diamond or inventory values; no fake cloud sync/sign-out. Existing character IDs, level continuity and save schema are unchanged.

**Unresolved:** the reference shows volumetric 3D feathers, high-resolution beveled insignia and a highly specific layout that cannot be certified pixel-identical without actual emulator/simulator screenshot overlay comparison at multiple aspect ratios. Current artwork is drawn from the game's own existing image resources. QA must mark remaining visual differences explicitly.

## Iteration 29 — same in-flight star shapes, less per-frame allocation (v0.1.56)

**Specific cross-platform mismatch:** Android rendered actual flying star pickups using the level's font-dependent, biome-specific collectible glyph, whereas iOS already rendered a real five-point gold star. The two games showed different shapes despite identical pickup rules. Android now draws the same 10-point star path, size, placement and gold colour as the iOS Core Graphics renderer; both use actual saved-progress/visible-pickup logic without fake diamonds or currencies.

**Confirmed code hot spot:** iOS's frequently called rounded-rectangle painter constructed a new `CGPath(roundedRect:)` for each HUD/gate fragment per frame. It now uses four bounded quarter arcs in the existing CGContext. This reduces explicit path-object creation while retaining the same circular-corner radius and saved-state-independent draw commands. Benchmark claims are intentionally withheld until measured on devices.

**Screenshot evidence correction:** A SHA-256 audit of the published v0.1.55 images revealed that `BOPAVI-Android-Flight-Active.png` exactly duplicated the idle `BOPAVI-Android-Flight-Preview.png`. Build success alone was therefore insufficient for an active-frame claim. v0.1.56 delays capture briefly after the real injected flap and requires different preflight/active file digests during CI and final release staging. The v0.1.55 asset remains historically inaccurate; it is not retroactively described as valid gameplay proof.

**Remaining visual gaps:** the same approved illustrated 2D mascot and islands are used. Volumetric 3D feather textures, castles, backgrounds, ornaments and 1:1 pixel-diff QA of all reference screens remain outstanding.

## Iteration 28 — crown coins and actual final-gate portal (v0.1.55)

**Reference divergence:** gameplay #7 shows bright bevelled circular gold crown coins and a glowing blue finish portal. Previous gameplay used world-coloured pickup circles with generic biome glyphs and had no visual cue for the final procedural gate. This mismatched the reference despite accurate gameplay logic.

**Implemented on both platforms:** golden concentric coin discs with real vector crown embossing, subtle glint and gentle optional pulse; shimmering sapphire/cyan finish portal with gold crown attached only to `level.gates.lastIndex` (or queued `upcomingGates.lastIndex`). The artwork paints from the existing simulation's gate center and visibility state — no fake buttons, inventory, off-network data, collision change or per-frame bitmap allocations. Reduced motion disables pulsation.

**Evidence:** shared source regressions + Android/iOS native build and QA screenshot artifacts. Release workflow now requires and publishes a real, emulator-captured active-flight screenshot, in addition to idle flight and home images. This provides a reproducible, visually inspectable path for comparison with concept #7.

**Unresolved:** large-scale textured 3D mascot/feathers, volumetric castle-and-waterfall scenery, detailed glass materials, frame-perfect 9:16 screenshots and a complete pixel-by-pixel benchmark against all ten static concept images.

## Iteration 27 — active flight HUD (v0.1.54)

**Reference:** the seventh approved screenshot places a small gold coin wallet in the upper left, a long narrow progress gauge between a crown and finish flag at the top centre, a dedicated pause button at upper right, and three overlapping glassy status circles down the left edge. The old production UI occupied almost the whole top with three equal rectangular counters and then a separate long progress row.

**Implementation:** Android and iOS now reserve 13–139 at top for the real coins, 146–406 for a crown-to-flag gauge, and three 66-unit sapphire status discs centred at x46, y175/249/323. The centre badge reports real level and gate progress; a second small line reports the cumulative gates completed. Circles report actual earned stars, shields and active magnet time, and darken when the power is inactive. No UI sprites are baked into the world image or made tappable as fake controls.

**Evidence:** source parity regression checks; Android live flight screenshot captured in smoke only on confirmed active-flight accessibility state; existing signed-off Android 15/16 emulator and iOS simulator CI requirements unchanged. The reference's `1,280/2,000 m` is not valid for the current simulation, which tracks gates and abstract world distance, so invented metre labels are intentionally excluded.

**Not yet matched:** volumetric flying Bopi, lush 3D scenery, fully illustrated crown/wing/coin buttons and particle trails, depth-of-field blur, glass shader and individual 1:1 screenshot-diff benchmarks. The full provided design is still a visual target.

## Iteration 26 — 2×4 worlds and genuinely distinct splash/home screens

The user references show eight wide, visually rich world cards with compact text on top of the painting. The previous native apps laid out two columns and four rows, but each tile was 244 logical points tall on iOS and used 170px of Android artwork plus separate caption rows; the grid took far more vertical space than the target.

**Implementation:** Android tile art now has a 132dp height and no external title/statistic rows; iOS tile is 144pt tall with a 132pt art viewport. The real world title, world number, authentic stars/30, best score and action arrow are overlaid at equivalent positions. The surrounding art is dimmed just enough for legibility. Every world remains interactive and supports existing save IDs.

**Second reference divergence:** A single image previously represented both startup loading and the home menu, so the requested arrangement (flying bird above winged wordmark in splash versus logo above bird on the menu) could not both be correct. The build generator now outputs a separate start and home scene for Android/iOS from existing game SVG artwork, without static buttons, fake loading percentages or new online dependencies.

**Evidence added:** Automated Android smoke captures `android-worlds.png` from the real interactive grid. Successful releases publish the image and the README gallery links to its real screenshot, with the existing PNG signature, portrait resolution and size checks.

**Still open:** Native art uses reusable SVG/raster 2D bird and islands rather than the reference's fully modeled 3D-feather mascot and high-resolution fantasy backgrounds; ten reference screens have not yet passed quantitative 1:1 diffs. Do not misrepresent staged render screenshots as game UI.

## Iteration 25 — sky-island cloud depth and live premium HUD

**Source mismatch found:** World illustration generation still ended each background with an opaque solid ground-colored rectangle at y=757, even after the native overpaint stripe was removed in v0.1.51. All eight rebuilt background rasters now use per-biome layered cloudbanks with irregular silhouettes instead, retaining their full artwork extent and original world index mapping.

**HUD mismatch addressed:** Rendered coin, star, ordinal and power-up counters had flat midnight rectangles and an abstract coin dot. Android/iOS now draw equivalent cyan-outlined translucent blue pills, a layered crown coin emblem and reflective trim. All numeric values remain sourced from real game state, independent of concept screenshot numbers. Existing accessibility and Pause button architecture are untouched.

**Regression scope:** World generators and the Android/iOS HUD helpers have shared source tests. iOS crown typeface is retained rather than repeatedly created in the frame loop. No gameplay rebalancing, changes to virtual coins, save migration or collision boundaries.

**Remaining visual deficit:** The supplied reference depicts high-detail 3D-like models, volumetric waterfalls, sculpted buttons and very detailed environment assets. The current shared SVG-to-raster art is still a 2D illustration. Screenshot evidence and pixel-diff results for all ten screens are needed before any honest 1:1 claim.

## Iteration 24 — full-height background, actual screenshots and README integrity

- Android GameView and iOS GameCanvas previously drew an opaque solid-color ground stripe from logical coordinate y=751 to the bottom of their 480×800 gameplay scene after restoring the canvas. That stripe erased foreground fantasy world details independently of the painted world raster. Both paths now draw the complete illustrative scene and rely on their already-existing full-viewport sky fill for letterboxes.
- Marketing README has been redesigned around the real shipped logo, icon, character SVGs, four footer illustrations, live game features and verified emulator/simulator screenshot gallery. Gallery URLs point to current GitHub Release assets, not concept images.
- Release stage now fails closed unless it can download real Android QA screenshots (home, pilot picker, idle flight, results) and an iOS simulator home capture. All images are checked for genuine PNG structure and portrait dimensions; no UI is generated or painted for this gallery.
- Kotlin/Swift declaration/import audit found and removed one unused Android property and three unused imports. The new conservative guard runs in CI; fallback sprite rendering, accessibility callbacks, physics and save migration remain.
- Visual gaps: premium 3D-like Bopi fur/feather shading, modeled castles/waterfalls, complete HUD composition and 10-screen native pixel comparison are **still open**. This iteration improves a reproducible occlusion problem and truthful visual documentation, not full 1:1 parity.

## Iteration 23 — Bopi facial features and animated feather detail

- Compared reference mascot eyes/beak/goggles with the production source SVG. The existing Bopi source had near-flat dark pupil ovals and a closed triangular beak. New shared layers create more dimensional cyan irises, distinct white catchlights, blue brows, a mouth and tongue, reflective crest and additional aviator strap highlights.
- The previous generator assumed the **first four** matching SVG paths were two wings. Newly added feather art would have silently been rendered into Bopi's body instead of animated wings. The extraction now uses explicit `data-wing="left|right"` labels on each flying feather stroke and only composites the body independently. This prevents a real animation mismatch on both platforms.
- Real v0.1.50 sprite-art inspection found the background illustration's soft ground shadow had been rasterized into Bopi's flying body. This now stays in the menu painting and is excluded from moving flight sprites, with an alpha-pixel CI guard. The open beak was also moved toward the face center so it is not hidden by the scarf.
- Both mobile apps use the same build-time sprite art and the same unchanged collision math. No new runtime allocation or effects were introduced and no saves/IDs/economy were migrated.
- **Unresolved reference gap:** independent 3D-quality feather sprites, lush volumetric islands/waterfalls and lighting, and all-ten-screen pixel-diff human review. This improves Bopi's silhouette and expressiveness but is **not** a 1:1 reproduction of the supplied concept render.

## Iteration 22 — premium footer art and measured home layering fix

- Real Android v0.1.48 screenshot showed the interactive four-part wallet obscuring the crown/letters of the painted BOPAVI logo. The shared launch-art logo Y coordinate is adjusted from 130 to 230 (900×1600 art coordinates), separating functional top header from brand artwork across Android/iOS.
- Shared Bopi SVG now uses a dark/sky-blue collar and gold crown badge instead of the previous pink scarf, while preserving detached animated wing geometry. Compare the actual post-build character on both platforms.
- Shared original source SVGs now render a person icon, clipboard, cyan feather and gold/blue treasure chest. Both native apps use the same icon bitmaps with separately accessible real buttons. This fixes the former monochrome chess/star/diamond/box mismatch with the four premium footer reference icons.
- CI checks four equal 128px icon dimensions and byte equality of both platforms; source regression checks ensure actual native navigation remains.
- **Not 1:1 yet:** the hero mascot, floating islands, elaborate lighting, 3D-appearing logo and some screen proportions need more art work and real screenshot review. Do not count the new flat/vector imagery as a full reproduction of the supplied references.

## Iteration 21 — first-flight hint and no idle frame churn

- Android and iOS now share an on-canvas DODIRNI ZA LET prompt at the same 480x800 logical location. It is overlaid on genuine game art and removed on the first flap, not a fake clickable screenshot.
- Android no longer requests recurring redraws while the preview is idle. iOS starts its display link only after first flap and stops it while paused. TalkBack/VoiceOver activation now triggers the same real flap handler as touch.
- No physics, save-data, inventory or procedural level semantics changed. Actual perspective, 3D mascot quality, and exact reference composition remain a measurable art gap.
- Require fresh Android API35/36 and iOS simulator QA evidence before claiming runtime verification; manual pixel comparison and physical-device battery measurements are outstanding.

## Iteration 20 — interactive BOPAVI home header

- Reference-inspired avatar/earned-currency/settings top bar implemented as true Android/iOS controls. Selected pilot image is from existing checked-in art. Profile, achievements, offline coin shop and settings are actionable; text counters derive from saves.
- Fixed redundant static home-chip UI without introducing an imaginary diamond balance or remotely purchased currency. Layout flexes on narrow devices.
- Exact 3D mascot, logo extrusion and pixel-diff parity still require validated production graphics.

## Iteration 19 — page status and locked preview correctness

- The 20-tile illustrated level cards retain reference composition and original saved lock/reward state. Added a compact actual page count and stars summary; a wholly locked page now has no misleading start-flight action, while its back and return-to-frontier controls remain available.
- Responsive typography and exact concept-image pixel comparison still require separate device snapshot approval.

## Iteration 18 — future level page preview

Pagination of 20-tile grids is independent of saved unlock progress on Android and iOS, as in the reference. Locked levels can be inspected but never entered. Fully locked pages expose a contextual return to the player's true progression frontier.

## Iteration 18 — contextual bottom navigation

The home bottom nav now returns to Home, rather than unexpectedly opening Settings. Nested store and profile screens carry explicit return origins across transactions and Android predictive Back mirrors the screen's visible navigation. Reference pixel-fidelity is not yet measured.

## Iteration 18 — lightweight flying-pilot preview

- The selected portrait in the pre-flight hero gently bobs independently of the UI and world art; gameplay retains its own physics-only flying animation.
- Android and iOS obey reduced-motion preferences and return to a stable static picture. Android cancels the animator when the portrait detaches, preventing ghost animations.
- Full visual 1:1 is not claimed without source asset and pixel-diff approval.

## Iteration 17 — full-bleed, measured against actual emulator output

Actual v0.1.40 screenshots show OS status chrome and simpler flat game art. The current iteration hides status chrome across menus with native cutout protections and improves real wallet/footer text scaling. Character depth and premium reference 1:1 fidelity remain distinct, unverified art goals.

## Iteration 16 — illustrated pilot cards

All nine selectable characters now occupy real interactive cards with scenic backgrounds, native overlaid titles, ownership lock icons and saved-coin pricing, using existing project illustrations. Premium art 1:1 remains an independent acceptance target.

## Iteration 15 — pause settings preserve the live simulation

- Reference pause shows four quick actions including POSTAVKE. Previous app implementation replaced the game canvas on that action; now POSTAVKE expands an authentic, responsive blue/gold in-place panel for music volume, effects and vibration on both platforms.
- The same original local settings persistence backs both full settings and pause options, with no internet permission, fake account, or mock cloud backup. The active run remains paused during adjustment.
- Full 3D illustration fidelity and ten-screen pixel-perfect parity remain unverified.

## Iteration 14 — world gallery premium composition

- Brought native Android/iOS two-column world cards closer to reference: full artwork header, dark live-title overlay, gold selection border, separate crown and round blue arrow, and compact two-row real metrics.
- Added Android tactile ripple and iOS selected accessible state; all displayed progress is bound to existing local saves, not copied from the illustrative references.
- Dense cards remain scrollable and use single-line scaling for long Croatian world names. No additional bitmap or third-party font dependencies were introduced.
- Full 1:1 reproduction of rendered depth, letter outlines and proprietary-style character art is not yet established.

## Iteration 13 — reference world titles and coherent theme artwork
- Eight source screenshot world titles now match Android/iOS gallery labels exactly in the same gallery index order.
- Existing world SVG compositions are remapped to match their new subject (lava, moon castles, pearl sea, temple spires); original forest edge silhouettes, glowing mushrooms and fireflies distinguish the seventh card from green floating islands.
- Scene source remapping changes decorative world background assets, **not** stable save IDs, deterministic level generation, collision boundaries, score/stars or play-flow state. Cards and world names stay live and accessible.
- Remaining gaps: reference 3D assets, logo rendering, card ornament detail, font licensing, dynamic-size device pixel differences and comprehensive ten-screen 1:1 screenshot comparisons.

## Iteration 12 — genuinely earned three-star level cards

- Source images request 0–3 gold stars per level and X/30 stars per world. This iteration stores rating from the **real star pickups within each completed level**: no automatic three-star completion award.
- Android and iOS present filled and outline stars on live, accessible level grid buttons, with saved world totals based on levels 1–10 (30 possible rating positions).
- Every value remains clickable/accessible and is maintained in v6 offline backups. Unlimited seamless flight still uses a separate running total and is never interrupted or restarted at a level boundary.
- Discrepancy: 3D Bopi textures, golden medal sprites, individual world card illustrative art and exact typography are still not pixel matched to the ten reference renders.

## Iteration 11 — live selected-pilot result illustration and verified record
- Android and iOS result screen header layers the *real selected pilot portrait* over the *played world illustration* using separate platform views; no pre-rendered screenshot or baked-in interactive text.
- A gold NOVI REKORD badge is only shown on a strictly improved pre-save personal best, and the success/defeat result sound now distinguishes level and hit effects.
- Android emulator QA checks result pilot/world accessible composition; source parity ensures both platforms use one-time persisted results and the same record predicate.
- Reference gap remains: 3D feathers, goggles, volumetric landscapes and exact typographic/stroke fidelity require original standalone art assets and visual diff QA; this iteration does not claim 1:1.

## Iteration 10 — accessible saved-progress world cards
- World selector remains two columns × four rows, now with illustrated world art, dynamic uppercase title, independently rendered actual `LEVEL`, actual saved collection count and actual record score on both platforms.
- Android real UI automation scrolls across every world and verifies accessible, genuinely saved metrics, returning to Home before gameplay navigation. No static mockup imagery with non-interactive counters.
- Reference concept's per-world 12/30 stars and world names do not correspond to the existing game/save schema, so these values are **not claimed** or fabricated. Full 3D scenery, typography and screenshot-pixel parity remain outstanding.

## Iteration 9 — matching settings difficulty cards
- Android and iOS now use matching interactive, horizontally arranged Lagano / Normalno / Zahtjevno cards with existing complete winged character portraits and a gold active outline.
- These are real touch controls bound to persisted offline physics settings, not static reference renders. The Android emulator test clicks and verifies two options by accessible state.
- Remaining visual gaps: the detailed 3D artwork, typography/rendering and decorative controls from the supplied reference are still not pixel-identical. The reference's language selector and cloud save cannot be claimed implemented without full functionality and data security.

## Iteration 8 — complete nine-pilot portraits
- Actual Android/iOS galleries, preflight hero previews and PAUZA now show generated **full-wing character compositions**, instead of only the separate torso sprite (a confirmed source-level visual omission).
- The true animated flight path still loads the unchanged separate `Bopi`, `BopiLeft` and `BopiRight` layers, maintaining frame-accurate flap mechanics and pixel parity.
- Generator builds one transparent 512×512 portrait per character with subtle ambient halo from approved existing wings and body; art appears from identical PNG bytes on both platforms.
- Remaining gap: these are enhanced illustrations from existing SVG shapes, **not** the 3D feather, goggles, mascot lighting and exact premium backgrounds in supplied screenshots. Full screenshot-diff approval remains open.

## Iteration 7 — eight illustrated world environments
- Shared deterministic generator now adds faceted rock geometry, additional atmospheric highlights, castles, waterfalls, coral, crystal and snow formations, volcanic fissures, celestial portals, and biome-specific foreground shapes to all eight world images.
- This is production game scenery rasterized once for both operating systems, not an in-chat concept sheet or a non-interactive screenshot standing in for controls. Gameplay collision shapes and flight lane are unaffected.
- Reference difference still open: original stylized characters and generated 2D environments do not yet reproduce the source's high-detail 3D feather textures and realistic volumetric lighting. Exact screenshot matching requires a future imported art pass and multi-resolution approval.

## Iteration 6 — authentic gameplay HUD
- Android and iOS present same 3 status chips (current-run coins, current-run stars, procedural level), cyan gate-progress meter, cumulative passed label and live shield/magnet badges.
- UIKit iOS counter duplicate and second painted PAUZA legend under the real native modal removed. Preflight remains deliberately free of gameplay HUD.
- Progress bar means **actual current level gates passed**, NOT the screenshot's fictitious `1,280 / 2,000 m` until real distance unit and target metrics exist. Cumulative progress never jumps to zero at a seamless transition.
- Outstanding visual differences: world/character 3D rendering, perspective and ornamental coin sprites/glowing textures require real standalone art and screenshot approval.

## Iteration 5 — pilot gallery
- Featured Portantin, Noa, Any cards occupy the first top row on both devices, matching the reference ordering without remapping canonical skin IDs or corrupting ownership/coin balances.
- Selected pilot hero uses real layered world illustration, dim overlay, portrait, selected badge. The **POLETI S** action follows the full gallery rather than preceding it.
- Outstanding art: the concept's flying portraits are detailed 3D renderings; the source images remain simplified vector birds. Exact photorealistic visual matching still needs genuine licensed/approved standalone artwork, screenshot comparisons and multi-device QA.

## Iteration 4 — actual result/rewards layout
- Result summary now uses three responsive real-score/coins/gates cards matching the screenshot's information hierarchy.
- Rewards row uses four actual offline values (run coins, run stars, claimable record bonus, saved best score). The artwork's diamond/chest/crown inventory is not invented or simulated.
- One-time score bonus claim updates numbers in place; Android text share and iOS activity share use genuine system UIs with no player identity.
- Real screenshot comparison at multiple device sizes and full 3D asset fidelity remain **open**, notwithstanding functional and source parity.

## Iteration 3 — premium pause overlay
- Replaced Android AlertDialog item list and iOS system action sheet with native custom scrollable pause cards over dimmed actual gameplay. The selected character, blue/gold header, orange Continue, blue Retry, gray Exit and four real shortcuts align the control hierarchy with the provided screenshot.
- Sound/music toggles are fully connected to saved audio state. Reactivating sound from the pause screen explicitly keeps soundtrack paused until Continue.
- The supplied illustrative 3D bird and world artwork still differ from the flat existing assets; UI fidelity to the reference remains partial until separately approved texture/sprite exports are integrated.

## Iteration 2 — implemented, pending pixel-art approval

- Worlds: both platforms now have numbered crown badges and decorative blue navigation arrows over the existing real world art; entire world tile remains the tap target.
- Levels: 20 real illustrated level tiles in 4 columns × 5 rows, with stored frontier lock/complete/selected states and actual procedural level type icons. Prev/next paging and jump-to-unlocked remain functional.
- Settings: dual live `musicVolume`/`effectsVolume` sliders, actual audio mixer gain changes, save migration-friendly persistence and restore parity.
- Remaining visual gap: original source image illustrations are flat 2D; actual user references are detailed 3D renders and must be imported as suitable separate assets rather than embedding fake buttons or counters. This milestone is **not** 100% visually identical.

# Reference vs real-build UI — visual QA log

Date: 2026-10-10. Target: ten supplied BOPAVI premium mockups. Verified real device output: [Android API35 screenshot artifact](https://github.com/bren-wp/BOPAVI/actions/runs/38080344200/artifacts/11680835601) from the PR branch. It includes home, settings, pilot picker, idle gameplay and result. Android API36 passed the same screen smoke.

## Differences actually observed

| Screen | Live result | Reference difference | Next task |
| --- | --- | --- | --- |
| Home | Golden IGRAJ, full-width blue SVJETOVI/POSTAVKE, score/coin chips, four working tabs; UI smoke PASS | Flat bird art, small glyphs and flat floating islands vs. high-fidelity illuminated 3D concept | Supply clean mascot/background/icon layer exports; verify 9:16 framing, adjust shine and shadows without embedding buttons |
| Settings | Shared winged logo, cyan outlines, navy panels, real animation/audio/haptics and difficulty; interaction smoke PASS | Reference has bespoke card icons, blue progress sliders and decorative gold selections; current sound is a real on/off switch and cannot pretend to have separate 80/70% sliders | Implement actual per-channel audio gains and saved volume before adding sliders; responsive card icon atlas |
| Pilot picker | Nine actual user-selectable characters, three-column real portraits and coins/ownership; scroll & launch smoke PASS | Current launch preview uses default saved Bopi, not reference-selected Portantin; portraits are currently stylized SVGs rather than 3D character renders | Create nine matching character sprite sheets with coherent expressions and exact naming; never discard existing character selections/saves |
| Worlds | Eight selectable worlds with local progress | Flat original world backdrops and different canonical level world names vs. concept card paintings | Art production and explicit names migration with saved-progress compatibility, not merely replacing labels |
| Levels | Twenty-page real unlocked/locked grid | Reference uses illustrated cards with 3-star ratings; current level core stores unlock states, but does not track independent visual three-star ratings | Specify and persist genuine star achievements before rendering them |
| Gameplay | Real pause behavior, collision and procedural seamless progression, native Canvas | Existing side-on flying simulation is not the perspective-based concept screenshot; fake 3D perspective obstacles would alter hitbox truth | Design gameplay backgrounds and effects consistent with current side-scroller hitboxes; do not overwrite simulation |
| Pause | Functional pause/retry/exit, only after first flap | Modal chrome and 3D decoration do not yet match concept | Polish accessible pause modal using shared action/panel tokens |
| Result | Dark cards, true score and total passes, real earned-coin values, working retry; smoke PASS | 3D confetti/reward chest/diamond presentation is not available and would be false without a backend/inventory | Real confetti animation and existing earned rewards only; no imaginary diamonds or video-reward controls |
| Splash | Single branded winged logo and mascot art generated for Android and iOS | Current character/background illustration style remains flat | Replace only the artwork layer from approved standalone exports; preserve actual loading/splash lifecycle |
| Brand board | Same primary gold, cyan, dark blue and white visual tokens on Android/iOS | Mockup-specific glossy font/3D extrusion unavailable in current open-source SVG render | Approve standalone wordmark, hero, portrait sprites, icon atlas and font license, then compare actual screenshots |

## Verification policy

- Do not claim **100% pixel identical** until the real screenshots at agreed phone resolutions match the supplied visual reference by human review plus layout screenshots. The current Android screenshot proves a **real visual gap** in illustration quality.
- All UI state must be dynamic: coins, scores, saved pilots, unlocked levels, actual haptic/audio settings and result stats.
- Do not stage mockup-only diamonds, cloud backup, logout, rewarded videos or purchases as inactive buttons.
- Keep real Android API35/36 and iOS simulator + device compile gates; avoid GitHub signing.
- Open separate implementation tasks for high-fidelity asset exports and safe-area/pixel-diff QA rather than replacing gameplay with flattened mockup screenshots.


## v0.1.81 — Home safe-area and reference comparison (pending run evidence)

The supplied 2026-10-14 reference gallery is a **design target**, not an application screenshot. This PR intentionally touches Home only; Splash, Settings, Store, Records, Profile, Worlds, Levels and active gameplay are not visually redesigned.

- Android: preserve full-bleed artwork while applying navigation-bar and cutout insets on API 26–36 to real controls.
- iOS: bind the Home stack to the safe-area viewport and retain genuine actions, local score/currency and the four navigational destinations.
- The emulator's **real** Home PNG is checked for clickable targets entirely inside physical screenshot bounds, 40dp minimum bounds and no overlapping hit rectangles; keep API35/API36 tests and iOS simulator capture.
- **Acceptance not yet claimed:** pixel-for-pixel Bopi/wordmark/environment fidelity; cross-platform side-by-side screenshot approval; additional device sizes. Separate clean artwork layers are needed rather than an entire screenshot with baked-in fictitious counters.
- CI and screenshot review must pass before merge. v0.1.81 must not be published manually from this documentation.


### v0.1.80 baseline evidence inspected for this PR
- Android workflow artifact 11858433751: actual 1080×2400 HOME shows Bopi, winged wordmark, the three CTAs and four active footer tabs. Illustration depth, lighting and mascot anatomy remain far from the new concept images.
- iOS workflow artifact 11858883855: actual 1206×2622 PNG **does not show any Home CTA or bottom tab**. It shows only oversized background artwork beneath the system status/dynamic-island region. The old screenshot smoke accepted this because it checked only that the PNG file was non-empty.
- v0.1.81 adds a bounded simulator retry plus an AppKit PNG verifier that requires both the gold IGRAJ band and navy footer navigation. This is a necessary evidence gate, not proof of pixel-level fidelity.


## v0.1.82 — Active gameplay reference gate (pending post-build screenshot approval)
- **Focus:** physically shorter/taller upper/lower obstacle shafts, seeded variable mouth height per gate and visible biome-specific cliff silhouettes. Existing game mode remains tap-to-flap, left bird / right incoming obstacles, portrait Android+iOS.
- Rendering and collision must call the same dynamic opening for Easy / Normal / Hard; real body radius remains 18 logical pixels. No fake collectible counters, enlarged mascot or screenshot-baked HUD.
- Compare actual Android API35, API36 and iOS simulator active-flight shots against the supplied Crystal, Forest, Ocean, Sky, Moon, Lava and Cosmic references. Confirm top-safe-area HUD stays physically top-aligned, Bopi remains small, all eight world backgrounds are different and obstacle cap lips line up with collisions.
- **Not 1:1 approved:** game sprites and backdrop layers remain code-generated 2D artwork; the supplied concept screenshot cannot safely be used directly as a gameplay texture. Finished state requires reference-specific licensed Bopi/biome exports and image-by-image review.

## v0.1.83 — grounded obstacles and physical top HUD
- Compare live eight-world Android/iOS screenshots against supplied references; top and bottom cliff roots must visibly reach the playfield edges with no isolated floating shafts. Keep decorative rock roots strictly within each existing shaft collider and away from the passable gate mouth.
- HUD gauge is one connected navy panel with real level/gates text, live score and distance. Compact power counters remain physically above Bopi, with no obstruction of the flight line and no invented metres. Observe iPhone notch and Android display-cutout placement on multiple aspect ratios.
- English is the initial locale. Settings > Language offers 26 explicit choices; changing locale saves locally without resetting progress. English/Croatian are the first functional UI coverage tier. Core commands in additional languages have translations, but other pages require manual linguistic QA before being described as fully localized.
- Screen-by-screen, 1:1 artwork approval is still pending authentic side-by-side screenshots with transparent character/environment assets. These visual differences cannot be hidden by screenshot-baked scenery.

## v0.1.84 — rooted gameplay silhouette + compact status + real HUD translations
- Inspect real Android API35/API36 active-flight PNG and iOS simulator from CI. On the 480×800 physical camera, the upper stone **shaft extends above y=0** and lower shaft **extends below y=800** for each obstacle. The physical collision cap must remain on the dynamic opening (no decorative false hitbox).
- Compact status gauge at x=146..381, y=18..108 (iOS width 235, height 90). Mini shield/star/magnet readouts use centers y=124 and total height 28 instead of y=141 and 38. Score and distance are true GameSimulation data; do not append fake metres.
- For level numbers of six/seven digits and long localized strings, both engines fit text to the 184-unit gauge caption width on change; Bopi sprite and tight body collider are unchanged.
- Gameplay HUD has eight core labels in all 26 picker locales, tested with native Kotlin/Swift parity and source audit. **Other UI screens can still show English fallback or legacy Croatian copy and require a full language-by-language review.**
- **1:1 is not approved.** Actual world art and Bopi illustration are simpler than supplied fantasy 3D references. Do not imply that generated 2D assets equal licensed layered 3D art. Screenshot-by-screenshot review is required.

## v0.1.85 — no floating pillars, no inter-level void, and gentler soundtrack
- Actual v0.1.85 QA revealed a blank first-flight view when the first gate spawned at x=560 beyond a 480-unit camera. New invariant: the first 76-unit gate begins at x=420; inspect the first active-flap PNG for a top/bottom attached obstacle at the right edge and unchanged Bopi size.
- Every gate is 76 simulation-space units wide across all worlds and all difficulty levels. Eight world-specific rock shaders remain attached off-screen vertically. Top and lower visible shoulders never extend into the collision-tested opening. Difficulty, gap position and rock height still vary procedurally.
- Adjacent gates are separated by the ordinary 228 − 2×zone step (218…228 units) even across a stream level boundary, with no special 190–208 transition and no extra pause. Check real flight screencaps and physical tap response at boundary.
- All main menus use safe-area-adaptive live views without vertical scrolling. Accessibility/tap targets must be verified at short/long aspect ratios: a visually smaller page is not sufficient if controls become unusable.
- Android/iOS soundtrack must actually contain the *regenerated* 48-second world_n.wav assets. GitHub Actions calls the original Python generator before source audit and both builds, then verifies WAV durations, quiet RMS, safe loop endpoints, per-platform SHA-equivalence and short effects. Existing repository WAV assets are legacy placeholders for source checkout; release APK and iOS bundle must carry the generated long loops.
- Review live screenshots against provided references. No claim of 1:1 art fidelity until visual differences have been resolved and documented.

## v0.1.86 — native nine-pilot gallery and non-scrolling overlays
- Capture genuine Android pilot picker screenshot with nine interactive cards, saved selected state, confirm-before-virtual-coin-unlock and real accessible touch rectangles. Ensure iOS uses the same nine bundled native images and independent sprites, not a composited reference image.
- Android must no longer wrap Pause/Results in ScrollView. iOS must set scrolling and vertical bounce off on both, and avoid setContentOffset when pause settings expand. Verify actual controls on physical/simulator screenshots before 1:1 approval.
- Selected pilot's portrait, wing and sprite must match between gallery, flight, pause and result; keep original sprite art size 124 and hit radius 18.
- Original 3D reference concepts have not been converted into complete layered game assets. Appearance, all 26 translations and short-device touch geometry remain open signoff items.

## v0.1.87 — native English-first result, worlds, and non-scrolling destination layout
- On fresh English installs confirm all eight real world gallery cards display English names and their actual progress; Croatian Settings > Language must preserve original naming.
- Force a gameplay Result and verify "FLIGHT OVER!"/"LEVEL COMPLETE!" and actual score, world, coins and gate totals. Both World Map and Coin Shop remain tappable at once with no vertical scroll. Inspect at Android API35/API36 and iOS simulator sizes.
- VoiceOver/TalkBack: result reward headings, world card and navigation labels should be expressed in the chosen language, with real dynamic figures.
- Keep the current no-claims boundary for premium 3D artwork parity and full-language coverage; no screenshot baked into game assets.

## v0.1.88 — verified native gameplay HUD caption boundaries
- Evidence from the actual 1080×2400 Android v0.1.87 gameplay frame: score and distance sat too close to the bottom of the central race HUD. Expected after correction: 235×90 gameplay HUD from x146..381, y18..108 in virtual 480×800 space; stage badge y58..81; Android footer baselines y98; iOS footer top y88; 8–10 logical unit bottom clearance. Score/distance each have 94-unit max glyph width and do not overlap each other.
- Verify the real screenshot after the Android API35/36 smoke tests; iOS retains the same physical coordinates but still needs its own actual gameplay simulator screenshot to approve full 1:1. Do not confuse Swift source parity with visual sign-off.
- Existing 76-unit rooted rock pillars, varied heights, continuous stage seam, 48-second calmer soundtrack, Bopi sprite size/hitbox and offline storage must not change.
