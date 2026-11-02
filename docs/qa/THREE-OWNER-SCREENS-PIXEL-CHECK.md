# BOPAVI — four original owner screens (Gameplay / Loading / Home / Game Over)

The user supplied four original 941×1672 PNGs for the current pass, including the branded Loading/Splash screen. This
specification deliberately **does not** claim that the current Android output is
pixel-identical; a source-level contract is never a substitute for an actual
emulator frame.

## Exact owner files (SHA-256; uploaded in chat, not bundled in the APK)

| Screen | Owner PNG SHA-256 |
|---|---|
| Loading | `cce13679bafb2d18410d35a56beef2cbfb249a8e636c356eed824e90778eb3e8` |
| Home | `e83c8f214c7dcee6fa87db737bfd412cb80cc37a8e73b5535bacf8b146617803` |
| Gameplay | `b80164e67da0e1b56e5b05d2f80fb383721d1a812dca5407255793a4de1ded08` |
| Game Over | `0fd6b82f6d54564a65eb7f44f0b4ac3027639cba406832c3b49edc484ae0a3df` |

All four originals are **941 x 1672 RGBA**. Do not resize them or bake them
into in-game UI controls. User-created screenshot content is reference art only.

## What must be checked

**Loading/Splash:** exact winged logo and crown, flying Bopi,
four metallic hollow hoops, white feather ornaments, blue/gold raised progress track and
'Loading...' caption. A true cold launch now holds the illustration for about
2.45 seconds; actual level preparation remains independent and cancellable.

**Home:** upper-right currencies, winged logo and crown, flying Bopi
body/wings/goggles/scarf, gold Play button and wing ornament, purple Skins,
green Shop, blue Settings, the 3D trophy/podium/speaker plates and labels.
Buttons must keep real click targets and show press states.

**Gameplay:** actual three-heart HUD and selected avatar, flying Bopi and wind,
hanging spiked-mace facets, real gold crown coins, purple crystal pickup,
cyan glass 'Tap to fly' prompt, stone arch, floating-island parallax,
lower spike floor. Hitbox shapes must continue to agree with physics; only
existing genuine pickups appear.

**Game Over:** raised two-line GAME/OVER lettering, crown/wings, correctly
posed Bopi and stone island, cream three-row results panel, red pin icon,
actual coins/stars or endless food/score, white raised retry arrow on gold
and white house on blue. The actual Game Over Bopi art is still a significant
visual parity risk: the current runtime uses a portrait drawable rather than
the unique sad/seated 3D pose shown in the owner reference. Do not claim this
asset matches while that condition persists.

## Reproducible pixel QA

Capture each running app state on an Android 15 or 16 emulator or device at
exactly 941x1672 pixels, including the same selected character, in-game state,
font scaling, system-bar policy and gesture state as the reference.

```bash
python3 tools/qa_three_owner_references.py --screen loading \
  --reference /path/to/original-loading.png \
  --actual /path/to/emulator-loading.png --require-exact

python3 tools/qa_three_owner_references.py --screen home \
  --reference /path/to/original-home.png \
  --actual /path/to/emulator-home.png --require-exact

python3 tools/qa_three_owner_references.py --screen gameplay \
  --reference /path/to/original-gameplay.png \
  --actual /path/to/emulator-gameplay.png --require-exact

python3 tools/qa_three_owner_references.py --screen game-over \
  --reference /path/to/original-game-over.png \
  --actual /path/to/emulator-game-over.png --require-exact
```

The script prints an overall RGB MAE, exact full-screen pixel equality, and
separate region scores for critical controls/art. Any non-identical pixel
causes `--require-exact` to fail, deliberately. Dynamic balances and gameplay
positions will differ until the application has real state matching the owner
reference; do not copy sample digits into fake counters.

A visual acceptance additionally requires Android touch/keyboard/TalkBack
tests, safe display insets on small/large phones, an unsquashed 3D icon
pipeline, and correct game-state persistence. **No 1:1 acceptance recorded.**
