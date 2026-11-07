# Owner Home and Splash comparison — published BOPAVI v1.0.55

**Exactly two screens in scope:** owner-provided `Home` and
`Splash/Loading` 941×1672 PNGs, compared with unmodified native Android
captures from the successful v1.0.55 PR #219 QA run
[42323116344](https://github.com/bren-wp/BOPAVI/actions/runs/42323116344),
artifact `QA-Android-visual-flow` (ID `13554691803`).

Both owner images and both emulator captures were measured in native
**941×1672** resolution, without resizing, recolouring, inpainting, masking,
screenshot substitution, artificial wallet values or synthesized game state.
RGB MAE is the average absolute RGB channel difference on 8-bit pixels.

| Owner screen | Actual QA capture | Whole screen RGB MAE (0–255) |
| --- | --- | ---: |
| Home | `android-home-reference.png` | **72.543** |
| Splash / Loading | `android-loading-reference-size.png` | **74.555** |

### Home, region ranking

| Original 941×1672 rectangle | ROI | RGB MAE |
| --- | --- | ---: |
| Logo, crown, wings | (100,98)–(840,390) | 83.59 |
| Achievements/Leaderboard/Sound | (180,1420)–(770,1630) | 79.70 |
| Skins and Shop native buttons | (119,1080)–(830,1250) | 77.44 |
| Animated Bopi | (100,385)–(810,886) | 70.00 |
| Left castle/waterfall landscape | (0,100)–(120,1250) | 68.21 |
| Right landscape | (830,100)–(941,1260) | 65.49 |
| Settings | (215,1220)–(722,1400) | 63.56 |
| Gold Play | (100,881)–(840,1080) | 61.67 |
| Actual live wallet HUD | (445,8)–(930,105) | 60.44 |

### Splash, region ranking

| Original 941×1672 rectangle | ROI | RGB MAE |
| --- | --- | ---: |
| Winged BOPAVI logo | (95,90)–(855,398) | 89.44 |
| Winged Loading housing and caption | (105,1300)–(840,1508) | 78.20 |
| Left scenery | (0,100)–(120,1290) | 73.21 |
| Right scenery | (820,100)–(941,1290) | 73.07 |
| Gold ring trail | (0,825)–(800,1260) | 64.42 |
| Flying Bopi and wing layers | (85,390)–(820,885) | 65.35 |

### Candidate 1.0.56 / build 186, NOT YET PIXEL-VERIFIED

- Home: move original square 512×512 Bopi rig from (152,342,645²)
  to (100,282,660²), corresponding to visible alpha top y≈393, feet
  y≈830. Original screenshot's pilot is notably higher/left.
- Home: move/enlarge winged BOPAVI logo; make accessible native Play,
  Skins, Shop, Settings and footer text closer to reference font size,
  with compact 3D icon+text clusters, a golden Play triangle and
  pearl-tinted relief.
- Home: full-screen wallet values should anchor to y≈21 in immersive
  mode, but still avoid actual physical cutouts and visible status bars.
  Display the genuine earned wallet/level currency, not the reference's
  illustrative 1,250/85 amounts.
- Splash: relocate the approved pilot body/wing square to (110,280,670²)
  so painted source alpha starts near y≈386 rather than y≈476.
  Slightly broaden the gold/blue winged Loading housing while preserving
  **real indeterminate progress** until the preparation-finished event.
- Both: strengthen blurred dark foreground foliage in the local
  offline sky landscape. Do not bake a whole screenshot into clickable UI.

Use `tools/qa_two_owner_reference_screens.py` with the ORIGINAL owner PNGs
and the next successful native captures to obtain independently ranked ROIs.

**Not 1:1:** these adjustments target the geometry and polish of existing
assets; the current independently drawn logo, Bopi and detailed painterly
castle/waterfall background still differ substantially from the supplied
reference illustrations. Original approved separated art layers would be
needed for exact source-pixel parity; never claim it from source tests alone.
