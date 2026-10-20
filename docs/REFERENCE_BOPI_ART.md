# BOPAVI — Approved original Bopi artwork for Android

The preserved original character source is in
`art/reference-approved/bopi/`: `bopi0_body.webp`,
`bopi0_left.webp`, `bopi0_right.webp`, their pinned
`manifest.json` checksums and `README.txt`.
The 224×192 source layers share one coordinate system and are not
a fabricated gameplay screenshot.

The Android-only `tools/reference_bopi_import.py` validates the exact
sources, canvas dimensions, manifest and SHA-256 before generating
independent 512×512 body/wing/portrait PNG assets plus a 2×2 reference
anchor marker. Missing or mismatched artwork must fail the integrity check.

```sh
python -m pip install pillow cairosvg
python tools/generate_images.py
python tests/test_approved_bopi_source.py
python tools/verify_pilot_sprite_anatomy.py
```

The display animates the two wings and Bopi's rotation using gameplay
state; character sprite transparency is not a physical hitbox.
The physical collider remains the radius-18 body circle.
Use `tools/install_reference_pack.py` only for a newly approved
replacement source, and review manifest digests before merging.

Artwork provenance does **not** prove that Home, Flight, Pause and
Results are 1:1 with user references. Compare *real Android emulator/device
screenshots* to the original approved reference at the same viewport and
game state; assess wing outlines, scenery, obstacle edges and HUD.
Never distribute test-rendered mock UI as an in-game capture.
