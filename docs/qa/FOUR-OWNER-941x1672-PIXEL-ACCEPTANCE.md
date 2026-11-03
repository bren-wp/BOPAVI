# BOPAVI: original 4-screen owner reference acceptance

The current request strictly covers **Splash → Home → Gameplay → Game Over**.
Each reference image is a genuine 941×1672 RGBA PNG supplied by the owner in this chat. The images are reference art, **not** compiled screen layouts.

## Verified reference fingerprints

| Screen | SHA-256 (unaltered owner PNG) |
|---|---|
| Splash | `cce13679bafb2d18410d35a56beef2cbfb249a8e636c356eed824e90778eb3e8` |
| Home | `e83c8f214c7dcee6fa87db737bfd412cb80cc37a8e73b5535bacf8b146617803` |
| Gameplay | `b80164e67da0e1b56e5b05d2f80fb383721d1a812dca5407255793a4de1ded08` |
| Game Over | `0fd6b82f6d54564a65eb7f44f0b4ac3027639cba406832c3b49edc484ae0a3df` |

### Specific must-pass visual checks

**Splash:** crown and winged wordmark in the first third, wide smiling Bopi with flight goggles and moving scarf, hollow metallic hoops in recession, gold/cyan 3D track, blue/white raised Loading label, never a static fake progress percentage.

**Home:** live two-rail currency at the top safe area, no density-driven shift on taller displays; navy outlined winged logo, matching flying hero; gold Play with wings, purple Skins, green Shop, blue Settings; **icon/label spacing must be compact and centered as one cluster**, 3D gloss, heavy bottom sidewalls. Three separately tappable trophy/podium/speaker medallions.

**Gameplay:** actual upper-left Bopi portrait with three hearts above the active flight world; ascending/descending airborne coins, violet crystal, blue arched crystal and swinging maces; no mismatch between visuals and collision area; real clickable 'Tap to fly' interaction and saved state.

**Game Over:** winged GAME/OVER title, crown and sculpted wood, sad seated aviator on a grass/stone platform with blue feathers, live result panel, dimensional Retry/Home buttons. Never show sample 1,843, 56 or 3 from concept art as the actual user's results.

### Source modules changed by v1.0.48

- `FlightLoadingView.kt` — Loading label extrusion
- `HomeReferenceScreen.kt`, `HomeTopWalletRail.kt` — compact CTAs and physical-safe-top wallets
- `FlightHudLayout.kt`, `GameView.kt` — real HUD anchor
- `GameOverScreen.kt`, `GameOverFlourish.kt`, `ResultStatText.kt` — real results and 3D illustration details

### Verify on a real build

1. Use a 941×1672 Android emulator, matching display density, font scale, bars and game state.
2. Capture actual Home, idle Flight, Loading and Game Over frames from a running debug build, not a static image.
3. Compare the original uploads against captures:

```bash
python3 tools/compare_owner_ui.py --reference original-splash.png --actual android-loading.png --max-mae 0
python3 tools/qa_three_owner_references.py --screen home --reference original-home.png --actual android-home.png --require-exact
python3 tools/qa_three_owner_references.py --screen gameplay --reference original-gameplay.png --actual android-gameplay.png --require-exact
python3 tools/qa_three_owner_references.py --screen game-over --reference original-game-over.png --actual android-game-over.png --require-exact
```

A zero-MAE result is required to claim **literal 1:1 pixels**. Genuine animated gameplay and live values make exact frame matching particularly hard; do not spoof state merely to pass the comparison.

**Current limitations:** The original sad seated Bopi sprite and the full illustrated multi-layer fantasy background are not independently present as approved source assets. New locally drawn wood/feather effects improve appearance but do not constitute complete parity. This PR is a work-in-progress until real Android screenshots and all CI checks are verified.
