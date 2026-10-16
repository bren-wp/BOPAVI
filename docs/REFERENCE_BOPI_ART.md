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

## One-time installation

Download the source ZIP from the ChatGPT conversation and, from the
repository root, run:

```sh
python -m pip install pillow cairosvg
python tools/install_reference_pack.py /path/to/BOPAVI-reference-bopi-layers-v0.2.1.zip
python tools/generate_images.py
python tests/test_reference_bopi_import.py
python tools/verify_pilot_sprite_anatomy.py
git add art/reference-approved/bopi
```

Then commit the **source** directory on a feature branch and open a PR.
The native PNGs are generated during CI, so do not hand-edit any output file.
The source archive must not be fetched dynamically at runtime, downloaded
from expiring URLs in CI, or silently copied from a screenshot inside the app.

The import fails closed on a missing wing, unexpected ZIP entry, changed hash,
invalid image dimensions, or tampered manifest. Without the source directory,
the original nine animated SVG pilots remain available. No account, internet
connection or external art services are required.

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
