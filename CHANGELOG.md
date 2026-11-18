# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.93 — Candidate: Game Over logo occlusion and visual QA diagnostics (build 223)

**Published:** 2026-11-18 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.93)

- Fix the confirmed Game Over P2 review issue: keep the original measured 3D crown coordinates but render the crown behind the native BOPAVI logo so it cannot cover brand letters.
- Add a native-view source regression test to prevent the crown from returning above the wordmark, while preserving the real Retry/Home controls and gameplay-derived statistics.
- Isolate Shop's 0.8435 horizontal camera correction: Settings and Skins now use full-width portrait reference camera so their native row titles and selectable cards cannot inherit Shop's 16:9 compression.
- Add a regression source contract for Shop versus Settings/Skins camera selection and preserve all native touch controls.
- Replace the 260×80 Shop merchant decoration with four SHA-verified, lossless, viewport-matched owner WebP scenery crops; the SHOP logo, FEATURED label, product prices and all buy buttons remain live native views. Adjust the stall's vertical stage position from y=302 to y=400 to follow real owner art.
- Repair the stale reference-insets CI assertion that still demanded the Shop-only camera on Settings and Skins; assert both use the correct owner-wide camera.
- Enhance the strict original-WebP-versus-native-PNG RGB reporter with a non-masked 3×3 per-region diagnostic grid and the worst ninth per screen to prioritize the next UI correction.
- Extend synthetic test fixtures to check exact and one-pixel-different captures without fabricated emulator evidence.
- Preserve existing coins, saves, skin ownership, offline privacy, playable physics and all approved source references.

## v1.0.92 — Objedinjene nativne korekcije zaslona i QA (build 222)

**Published:** 2026-11-16 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.92)

- **Skins:** ispravljen položaj i veličina animiranog lika uz dvije funkcionalne strelice i četiri kartice za odabir.
- **Shop:** čitljivi nazivi proizvoda u dva retka, bez promjene šest transakcijskih offline kupnji, cijena, pohranjenih novčića ili inventara.
- **Settings:** veća slova i ikone nativnih postavki, očuvani svi stvarni prekidači, glasnoća, SAF backup/restore i pravne poveznice.
- **Game Over:** originalni lokalni BOPAVI logotip, stvarni rezultati, sačuvani lik i funkcionalne akcije RETRY/HOME.
- **Gameplay Ready:** lokalni vektorski logotip prikazuje se samo prije prvog zamaha krila, ne tijekom aktivne igre; fizika i kolizije ne mijenjaju se.
- **Pause:** čitljiviji naslov i tri stvarna gumba NASTAVI/PONOVO/IZLAZ, bez dupliciranja dijaloga ili mijenjanja aktivnog leta.
- **Results:** prikaz odabranog Oriona umjesto zadanog lika i poštovanje postavke smanjenog pokreta; nagrade ostaju stvarne.
- **Privacy i Terms:** uklonjena dodatna duplicirana oznaka broja dokumenta, a cjeloviti offline HTML, poveznice, pomicanje i ZATVORI ostaju funkcionalni.
- **Provjere:** izvorni Python/Kotlin testovi, Android lint, R8, APK/AAB, emulatorski Android 15/16 funkcionalni testovi i izvorni RGB audit. Native snimke i originalne WebP reference nikada se ne zamjenjuju simulacijom ili rastezanjem.
- **Poznata ograničenja:** vizualna identičnost 1:1 još nije potvrđena; Game Over B, Results i Terms nemaju sve zasebne odgovarajuće referentne ili nativne snimke za potpunu ocjenu.
