# BOPAVI — owner-reference Bopi layers (Android and iOS)

This step brings the *actual Bopi illustration* supplied in the portrait
gameplay screenshot into the game's native art system. It is not a new AI-drawn
concept, a baked gameplay screenshot or a replacement for real controls.

## Approved source pack

File prepared with the project owner's supplied screenshot:
`BOPAVI-reference-bopi-layers-v0.2.1.zip`

The archive contains only the following files in
`art/reference-approved/bopi/`:

- `bopi0_body.webp` — head, goggles, beak, torso, scarf and feet
- `bopi0_left.webp` — source left wing
- `bopi0_right.webp` — source right wing
- `manifest.json` — sizes and SHA-256 checksums
- `README.txt` — provenance and instructions

Images have 224 × 192 transparent RGBA canvases with aligned coordinate
systems. Source shoulders were manually inspected at (106,118) and (157,129).
The rig still uses real simulation input: wings animate via flap events, the
body tilts with velocity, and **no collision size or player x-coordinate changes**.

## Bundled and reproducible (v0.2.3 build 108)

The *actual owner-approved reference source* now lives in the GitHub repository
under `art/reference-approved/bopi/`: three independently movable WebP layers,
`manifest.json`, and `README.txt`. Each source WebP has been compared against
the original ZIP's immutable SHA-256 digest. No ZIP download, external CDN,
ChatGPT link or network request is needed in GitHub CI, Android or iOS.

GitHub Actions runs `python tools/generate_images.py`. The importer fails
closed on missing/tampered layers, incorrect 224×192 canvases or a bad manifest,
then produces **matching 512×512 PNG bytes** on Android and iOS. Its 2×2 marker
activates the photograph-derived shoulders and moving wings. The newly added
`tests/test_approved_bopi_source.py` makes the real art mandatory in graphics
CI; synthetic sprites are only used by the separate negative-case test.

For a completely fresh local checkout, reproduce and inspect the art with:

```sh
python -m pip install pillow cairosvg
python tools/generate_images.py
python tests/test_approved_bopi_source.py
python tools/verify_pilot_sprite_anatomy.py
```

Use `tools/install_reference_pack.py` only for a **new approved source pack**
and update the pinned SHA-256 manifest/test after a deliberate, reviewed art
change. Never generate synthetic production artwork to satisfy a checksum.

## Rendering and 1:1 acceptance

The screenshot is a composition, not a sprite sheet. To avoid duplicate
backgrounds, the original static bird silhouette was isolated and split into
transparent body and wing layers. The process does **not** include original
sky, islands, coins, HUD or static image buttons. Android and iOS emit
identical 512 × 512 PNG resources for the *same* master art, and a marker
selects the correct imported wing joints only when the validated files exist.

Visually compare actual device captures at multiple bird elevations and
flap phases with the supplied source. Specifically inspect goggles and
feather-tip masking, portrait size, halo, left-side crown/magnet/feather rail,
real counters, pause, world-specific scrolling obstacles and touch targets.
Crop-mask transparency is a first pass, not a certification of pixel identity.
Additional character skins need separate source art, and worlds require clean
illustrative background layers that do not contain false obstacles or coins.

**Do not label a full screen 1:1 until its emulator screenshot has been
compared side by side at the same viewport and state.** This import covers
only the Bopi character pipeline. Eight-world background source layers,
obstacle textures, menus and other pilots remain outstanding.

## Privacy and authorship

This pack is derived from user-provided artwork for BOPAVI. Confirm ownership
and permission before public distribution. No third-party screenshot scraper,
cloud analytics, user data or internet runtime connection is used.
