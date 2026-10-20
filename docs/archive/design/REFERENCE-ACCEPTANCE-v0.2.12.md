# BOPAVI — non-gameplay 1:1 acceptance ledger (v0.2.12)

## Ground truth

The owner-supplied 17 October 2026 reference renders (including the eight biomes,
Home, Splash, Worlds, Levels, Pilot selection, Profile, Settings, Pause, Results,
and the branding board) are desired **design outputs**. They are not evidence of
native application pixels. Never publish a reference render as an Android/iOS capture.

The completed GitHub v0.2.11 Android QA artifact contains real 1080×2400 screenshots.
Seven matching owner-provided renders are 864×1536 or 941×1672. Each of the seven
actual/reference pairs was RGB-composited via a center-cover crop at 180×400,
then measured by average channel absolute difference (0–255). This metric is a
*triage baseline*, not a perceptual-fidelity percentage or evidence of 1:1 parity.

| Real Android screen | Reference found | Approx. MAE / 255 | Required correction |
| --- | --- | ---: | --- |
| Home | Yes | 69.8 | Standalone proportionate 3D Bopi, wordmark, deep background and button hierarchy |
| Worlds | Yes | 69.6 | Luminous painterly world portraits and large 2×4 image cards |
| Levels | Yes | 65.8 | Full scenic cell art, icon/star quality, crown title, selection glow |
| Pilot picker | Yes | 69.3 | Large Bopi hero, eight companion character looks, colorful card frames |
| Profile | Yes | 71.2 | Rich portrait frame, oversized stats, metallic achievement medals, world art |
| Settings | Yes | 67.1 | Rounded blue/gold typography, illustrated header, inset glass panels, cyan sliders |
| Results | Yes | 81.2 | Hero/wordmark and gold rewards, highly detailed illustrated depth |
| Pause | Yes | Not captured | Capture the real paused overlay before any visual sign-off |
| Splash/loading | Yes | Not captured | Compare actual launch timing artwork and native loading label |
| Store | Only a component sample on brand board | Not comparable | Complete owner-approved full-screen reference needed for strict pixel match |
| Collection | No complete standalone render | Not comparable | Establish an explicit full-screen visual target while preserving real save counts |
| Achievements/tasks | No complete standalone render | Not comparable | Establish an explicit full-screen visual target; badges reflect saved conditions |
| Records | No complete standalone render | Not comparable | Establish an explicit full-screen visual target; data remain real |

### Why no 100% claim

- The eight world and mascot reference images are richly painted dimensional
  illustrations; the current app's generated assets are simplified vector/raster
  landscapes. Native spacing tweaks cannot resolve this fundamental image mismatch.
- The reference files and actual device captures use **different aspect ratios**
  (about 0.562 versus 0.450). Center-cover cropping discards side detail; there
  is no meaningful promise of identical literal pixels on every device.
- Several concept screens depict diamonds, paid-unlock markers, cloud/logout and
  fixed counters. The working offline game does not have these systems. They must
  not become fake data or screenshot hotspots to chase an image-diff score.
- A single composite frame is not an editable component library. Accurate
  runtime art needs separate approved logo, winged-pilot sprites with alpha,
  each world/background, CTA states, badges, fonts/licences and control assets.

### Acceptance gates for the complete non-gameplay UI

1. Each existing screen (including Pause, Collection, tasks, Records and Store)
   must have an authentic **device** screenshot, not generated promotional art.
2. Compare each with an owner-approved target at the same logical aspect ratio,
   plus small/large Android and iPhone safe-area variants. Report placement, text
   overflow, clipped controls, fonts, art fidelity and color mismatches.
3. Do not substitute any reference image with baked buttons, balances, stars,
   character ownership or fake cloud/security controls.
4. Verify every real button, slider, toggle, pager and back action with emulator
   or simulator navigation tests; preserve saves, local coin economy and physics.
5. Hold any **1:1** sign-off until the complete list is closed with actual images.
   A green compile or emulator pass confirms function, *not* pixel fidelity.

Run `python tools/compare_reference_ui.py --help` to see how to compare local
owner-provided references against a genuine extracted CI capture directory,
without committing those private images to GitHub.

### v0.2.12 implementation scope

The Home logo and selected pilot now render as separate responsive native art
layers over a background without baked-in Bopi or BOPAVI text. The pilot picker
gains a larger genuine selected character illustration on both platforms.
This addresses layout/identity, **not** the painterly rendering deficit above.
