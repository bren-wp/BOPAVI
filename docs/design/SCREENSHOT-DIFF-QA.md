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
