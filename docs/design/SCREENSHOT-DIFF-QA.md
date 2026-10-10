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
