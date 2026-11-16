# Sljedeće Android izdanje — kandidat v1.0.92 / build 222

Jedini aktivni razvojni PR: #244, izravno prema `main`. Prethodno javno izdanje: **v1.0.89**. PR-ovi #240 (Skins), #242 (Shop) i #243 (ostali ekrani) zatvoreni su bez spajanja; sve njihove odobrene izmjene prenesene su u #244. Nisu potrebni dodatni PR-ovi za ovu iteraciju.

## v1.0.92 — Objedinjene nativne korekcije zaslona i QA (build 222)

**CANDIDATE / NOT PUBLISHED.** Svi sadržaji navedeni u nastavku implementirani su u izvornom kodu, ali konačno izdanje i vizualna jednakost 1:1 zahtijevaju provjeru izvornog commita i stvarnih Android snimki prije spajanja.

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

