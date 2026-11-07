# BOPAVI owner-screen baseline — published v1.0.53 (build 183)

**Screenshots:** real original-size 941×1672 captures from the passing
[Android 15 main workflow 42315307299](https://github.com/bren-wp/BOPAVI/actions/runs/42315307299),
`QA-Android-visual-flow` artifact ID **13551341084**.
**References:** nine unchanged 941×1672 owner PNGs supplied in the conversation;
the four selected screens are Splash, Home, idle Gameplay and Game Over.

RGB MAE = mean of |8-bit reference RGB - captured RGB| across all pixels
and channels; no screenshots were resized, regenerated or overpainted before
measurement. Different authentic counters and animated phases can affect error.

| Screen | Captured filename | Full-screen RGB MAE (0–255) |
| --- | --- | ---: |
| Splash / Loading | android-loading-reference-size.png | **78.201** |
| Gameplay / Idle | android-gameplay-ready-reference-size.png | **74.347** |
| Home | android-home-reference.png | **72.653** |
| Game Over | android-game-over-reference-size.png | **70.427** |

**The largest full-screen discrepancy is Splash. No screen is 1:1.**

## Highest-error key regions

| Splash reference region | RGB MAE |
| --- | ---: |
| Winged BOPAVI logo and crown | 88.28 |
| Winged loading track | 84.65 |
| Original flying Bopi | 76.17 |
| Loading caption | 74.01 |
| Decorative gold rings | 68.55 |

| Other screen/reference region | RGB MAE |
| --- | ---: |
| Gameplay lower ground spikes | 102.97 |
| Home BOPAVI logo | 84.46 |
| Home Shop | 81.46 |
| Home footer medals | 78.99 |
| Game Over seated Bopi | 78.00 |
| Game Over Home | 76.77 |

## Candidate v1.0.54 / build 184 adjustments (NOT yet pixel-verified)

- Rebuild the original 780px winged wordmark from its SVG at 780×297
  pixels with taller enamel letters and crown (rather than stretching the PNG).
  Place at owner grid (91,95)–(850,384) instead of (92,128)–(849,357).
- Shrink/reposition the approved selected pilot's 512×512 wing/body rig to
  (90,365)–(794,1069). The original alpha begins at y=81 and its visibly
  painted top stays beneath the logo. Native animation and selection remain.
- Add a **separate Splash-only offline background** with the left cliff castle,
  smaller distant right castle, layered rock terraces, waterfalls and clouds.
  Do not bake a full reference screenshot, text, logo, Bopi, score or progress
  indicator into the scenic bitmap; Home keeps its existing background.
- Leave true asynchronous loading state, blue indeterminate highlight, initial
  lifecycle and first-flap accessibility untouched.
- Original v1.0.53 Gameplay floor/colliders, Game Over 3D native Retry/Home,
  scores, saved progress and alternate pilot variants are preserved.

**Release gate:** new Android 15 and Android 16 tests; APK/AAB; source/security
audits; native Splash screenshot at 941×1672; compare new numeric ROI errors
and inspect actual image. A successful build alone does NOT establish visual
improvement or exact parity. Never replace real UI with a single flat image.
