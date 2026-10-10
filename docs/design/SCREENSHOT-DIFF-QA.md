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
