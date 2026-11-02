# Next Android release

## v1.0.47 — Reference-aligned Home 3D sizing and medal art (build 177)

**Candidate only — not published.** This is a follow-up to published v1.0.46, not a modification of its immutable release.

- Make the Play, Skins, Shop and Settings icon/text layout proportional to their native 941×1672 reference bounds rather than relying on density-multiplied fixed `dp` widths and margins.
- Upgrade the three Home footer icon plates to locally drawn circular blue/gold 3D medallions with a stepped navy rim, extruded gold bezel, curved white highlight and pressed/disabled states.
- Keep the original local fantasy backdrop, selected character, real navigation, balances, offline save and accessibility.
- Add explicit source regression checks for visual geometry and maintain existing reference-layout and widget tests.
- Gameplay follow-up: add physical metallic highlights to genuine gold crown coin pickups and existing spiked-mace geometry without affecting collision radius, pickup state, frame allocations, difficulty or saved progress.

- Level Map follow-up: use three nested, real enamel layers for blue, golden frontier and disabled stone stage markers, preserving actual locks and clickable targets.

**Acceptance gate:** Android Gradle compilation, tests, and fresh reference-sized emulator captures must pass. Match against the nine owner-supplied 941×1672 PNGs with `tools/compare_owner_ui.py` before describing any screen as pixel-perfect. Do not add fake gems, IAP, online rankings or cloud save to imitate a static concept.
