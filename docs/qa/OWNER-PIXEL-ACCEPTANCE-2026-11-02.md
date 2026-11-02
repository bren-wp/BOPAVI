# BOPAVI owner-reference visual acceptance — 2026-11-02

These nine user-supplied, portrait PNG reference screens are **941 × 1672** pixels. Their design is the specification; the native Android application is the implementation. Do **not** treat any generated emulator image, vector approximation, or a source-contract test as proof of a 1:1 pixel match.

## Mandatory evaluation for each screen

- **Geometry:** compare logo, avatar, HUD, title, panels, buttons, labels, margins, relative alignment, content safe areas, character scale, and scroll boundaries.
- **3D appearance:** compare each button's contour, navy/brown outline, thick lower extrusion, colored sidewall, highlight direction, specular shape, shadow offset, state transitions, and press depth. Text and symbols must also have independent readable embossed rims.
- **Icons:** compare silhouette, aspect ratio, position, fill palette, highlight and border; do not substitute an emoji, glyph, or generic default circle. The selected character must be the same during loading, Home, and Flight.
- **Interaction:** every visible control needs a real click/drag action, correct navigation, offline state persistence, disabled/locked treatment, and accessibility description. Do not display a fictional gem wallet, payment, cloud-sync, or purchase action.
- **Responsive:** portrait 941×1672 is the pixel baseline, with smaller/taller Android screens, landscape, display cutouts, gesture navigation, and 1.0/1.3/1.5/2.0 font scales requiring no clipping or overlay collisions. Minimum 48dp interaction targets wherever physically feasible must not distort the visible graphic.
- **Verification:** capture the same state on a real emulator/device at 941×1672 and run `python3 tools/compare_owner_ui.py --reference <owner.png> --actual <capture.png>`. Review the nine 3×3 regional MAE values, then inspect overlays manually for controls. A precise visual PASS requires actual captures, not just successful compilation.

| Order | Reference | Critical non-negotiable visual checks | Current source |
|---:|---|---|---|
| 1 | Splash / Loading | Winged BoPAVi logo; Bopi and his goggles/scarf; correctly scaled hollow gold flight rings; gold/blue enamel loading track; caption underneath; perspective fantasy world | `FlightLoadingView.kt`, `FlightLoadingLayout.kt` |
| 2 | Home | Coin/star live rail, crown and winged brand, flying character, giant golden Play with white feather sides, purple Skins, green Shop, blue Settings, circular trophy/podium/sound shortcuts | `HomeReferenceScreen.kt`, `HomeReferenceLayout.kt`, `HomeEnamelDrawable.kt` |
| 3 | Gameplay | Correct blue bird, heart HUD, moving spiked hazards, airborne tokens, floating cliffs, readable translucent Tap-to-fly prompt, reliable hitboxes | `GameView.kt`, `GameSimulation.kt` |
| 4 | Level map | Sky Meadows, Crystal Ruins, Volcano Peaks, pathways and nodes, locked versus unlocked tiers, stars, gold Play Level CTA | `LevelMapScreen.kt`, `LevelMapGeometry.kt` |
| 5 | Skins | Suspended carved wooden heading, flying aviator centered, left/right circular arrows, card border/shadows, highlight and selected check, functional equip controls | `PilotSelectionScreen.kt` |
| 6 | Shop | Hanging wooden shop header, colorful featured ribbon, gold/green CTA materials, 3D perk icons, readable cost and stock state, truthful offline wallet | `ShopScreen.kt` |
| 7 | Settings | White/cream carved board, evenly sized settings rows, colored rounded-square icons, real thumb/switch states, functioning dropdowns and links | `SettingsScreen.kt` |
| 8 | Level Complete | 3D lettermark, ecstatic Bopi, three gold stars, real collected values, glowing reward chest, embossed Next Level/Home/Share controls | `LevelCompleteScreen.kt` |
| 9 | Game Over | Crown/winged 3D heading, sad Bopi sitting on the rock, framed distance/coins/gems stats, orange Retry and blue Home controls | `GameOverScreen.kt` |

**Implementation and authenticity notes:**

1. Current project resources are local and native, but the screenshots include details that do not correspond to implemented features (notably gems, cloud save, IAP, and some displayed skin options). Never render them as working features until a truthful game model exists.
2. User-supplied reference pictures are not stored in the GitHub repository by this change; the owner must supply/reference the originals locally when running `compare_owner_ui.py`.
3. Splash/Home sprite IDs are now centrally resolved: choice 0 → Bopi sprite 0; 1 → owl sprite 9; 2 → fox sprite 10. The small icon renderer does not stand in for this artwork.
4. **Status: initial source corrections only, visual acceptance pending.** No APK/device screenshot verification or 1:1 approval has been performed by this document.
