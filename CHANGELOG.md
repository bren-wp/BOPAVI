# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

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

## v1.0.89 — Owner-reference Home logo camera correction (build 219)

**Published:** 2026-11-16 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.89)

- Shrink and raise the actual native winged-logo ImageView based on the owner's 1080×1920 painted wordmark bounds (from approximately 840px wide in v1.0.88 toward approximately 560px). Keep the real logo vector and native ImageView; do not embed the owner screenshot in the app.
- Recolor the original six independent vector glyphs to the reference palette (B/O/P cyan, A/V/I warm gold) while preserving their contours, crown, wings and source-generated Android asset. Asset artwork changes require the full candidate build and genuine screenshot validation.
- Validate the wallet and painted logo using true 2-D non-overlap rather than an unnecessary vertical ordering rule. Preserve the four real PLAY/SKINS/SHOP/SETTINGS callbacks, original button slots and saved data.
- Add direct Kotlin/JVM camera geometry assertions; publish separate original-versus-real-Android RGB evidence across the four available viewport references. Full RGB 1:1 is **not** claimed.
- Continue in order: Home geometry and art, Skins, Shop (six real purchases), Settings, Game Over variants, Gameplay/Loading, Pause, Results and legal dialogs. Do not merge until source + Android 15/16 emulator tests succeed and real pixel measurements are reviewed.
- Obtain authentic candidate native 1080×1920 / 1080×2280 / 1080×2340 / 1080×2400 screenshots and compare against matching SHA-pinned owner originals.
- Verify visual appearance of the winged wordmark (font/palette) and sky art separately; camera-bound alignment alone cannot correct illustrated asset differences.
- Confirm accessibility, notches, font-scale, compact/landscape layouts, 48dp hitboxes, loading and transitions. Do not delete old saves or reference art.
- Physical devices, SAF reliability, per-asset rights, exhaustive security/dead-code and performance remain unverified.
