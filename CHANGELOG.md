# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.4 — Three animal pilots, refined terrain, legal links and power usage (build 134)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.4)

- Add original Bopi, Sova (owl) and Lisac (winged fox) as immediately selectable Home characters with unique body, animated wing and portrait sprites; retain historical saves.
- Replace the prominent privacy button with two compact, independently accessible offline native footer links for Privacy Policy and Terms of Use.
- Improve the natural forest green obstacle material and HUD contrast without widening or changing any collision geometry.
- Stop the Home screen's endless decorative animation and pace automatic gameplay redraws at approximately 60fps-class or 30fps with reduced-motion preference; keep immediate touch response and fixed-substep physics.
- Keep procedural hazards flowing continuously across internal generation boundaries; no level menus, artificial completion overlays or restart between obstacle groups.
- Expand CI source, legal/character and emulator contract checks. On-device battery consumption, physical crash-free operation, public HTTPS hosting and Play publication require separate verification.

## v1.0.3 — Croatian UI, polished navigation and Play policy hardening (build 133)

**Published:** 2026-10-20 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.3)

- Use Croatian on every reachable native screen and accessibility label regardless of device locale, without erasing historical user settings or imported save keys.
- Replace the result screen's old plain Home action with a clearly styled, high-contrast, 58dp native arrow-back button and a consistent Croatian accessible description.
- Improve the Pause exit arrow; preserve game-state transitions, Back gestures, protected ongoing flight and real score persistence.
- Revalidate current Google Play API 36, truthful Croatian listing, privacy and real same-build artwork.
- Expand dead-code/resource audits and prevent orphan resources while retaining old public releases and backup migration behaviour.
- Remove unreachable 26-language UI catalog and dead helper logic from the shipped Kotlin code. Save migration and historic records stay intact.
- Extend decorative obstacle shafts into tall-screen camera overflow, eliminating disconnected/floating-looking rock ends without widening collision geometry. Refine Croatian scoreboard typography and skip safe-corridor rock CCD work.
- Add a discoverable Croatian in-app privacy notice (offline native dialog) and minimum 58dp legal tap area on Home; compact-screen art scales independently to retain readable primary buttons.
- Preserve the actively used ParallaxScenery helper and its Kotlin tests after runtime-reachability verification; it is not dead code.
- Refresh Google Play metadata, signing/privacy rules, Android 16 API 36 checks and current v1.0.2 publication evidence.
- Validate that the actual in-app Croatian privacy policy opens and closes on Android 15/16, and that its touch target remains at least 48dp on compact and tall emulator displays.
