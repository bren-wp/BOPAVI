# Next Android release

## v1.0.44 — native Gameplay Ready reference polish (build 174)

- Refine the original 941×1672 Gameplay Ready callout in the unchanged 480×800 gameplay camera: layered translucent cyan enamel, a dark lower bevel, white highlight and outlined glass border. The original hand gesture now shares validated reference constants rather than scattered coordinates.
- Add a second subtle glossy highlight to the real lives-only 3D HUD, without adding currency, score, fictitious lives, or clickable Canvas UI.
- Preserve the real first tap/wing flap, original pilot bitmaps and animation, 18px collision radius, local earned pickups, game state, accessibility description, rotation and reduced-motion handling.
- Add JVM reference geometry and Python rendering source regression checks. Original emulator screenshots must still be compared against owner-supplied reference PNG at exact source dimensions; visual 1:1 has not been certified.
- Release remains unsigned/unpublished and PR must stay unmerged until CI and Android 15/16 emulator QA pass.

- Gameplay HUD follow-up: deepen the three existing actual life hearts with an inactive-aware red enamel undercut and small glossy lower reflection, reusing the original Canvas/Path without resource allocations. No additional HUD counters, UI targets, hitbox changes or fake hearts.

- Pause visual/functionality pass: preserve the original locally bundled selected Bopi portrait inside a cyan 3D enamel hero with a purely decorative gold halo. Use a **native accessible gold TextView title**, three real Resume/Retry/Exit Android buttons, and existing finite/endless replay semantics. Keep the live GameView paused, preserve focus, prevent duplicate dialogs, and retain scroll-safe compact/large-font layout with 48dp minimum touch controls. Native geometry is covered by JVM and Python contracts. There is no separate original Pause raster supplied, so pixel-accurate 1:1 matching is not yet verifiable.
