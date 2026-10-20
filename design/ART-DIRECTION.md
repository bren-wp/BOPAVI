# BOPAVI — Android premium visual direction

**Brand:** BOPAVI / Bopi / Brendigo — https://brendigo.com.
Source art must remain original, locally generated and unmodified without
reference approval. No ads, online leaderboards, real-money purchases,
removed menus or misleading app imagery.

## Four current visible screens

- **Home:** real logo and approved blue Bopi artwork, one prominent IGRAJ/PLAY
  control, full-bleed scenery; no fake shop, world or settings buttons.
- **Flight:** animated separate wings, readable score and berry HUD,
  actual animated rock geometry and safe pause control, no false collisions.
- **Pause:** high-contrast pause card with real CONTINUE / RETRY / EXIT actions.
- **Results:** real score and berry total, best local records, RETRY / HOME.

The Android Canvas game uses a fixed logical 480×800 simulation coordinate
space. UI inset placement and background coverage are screen-dependent,
but must never change collision geometry. No unnecessary scrolling,
letterbox bands, clipped text or undersized touch targets. Test at a
minimum 48dp interactive target on small, tall and wide supported screens.

## Acceptance evidence

Use the original approved reference files where available. Visually compare
real Android screenshot pairs at matched display dimensions, state and
animation phase, followed by manual device review. GitHub graphic generation
or matching resource checksums are not a guarantee of 1:1 screen fidelity.
Historical retired product designs are archived, not current navigation.
