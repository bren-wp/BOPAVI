# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.79 — Stvarna galerija i marketinški prikaz BOPAVI igre (build 209)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.79)

- **Marketinški README:** nova prezentacija igre sa svjetovima, junacima, lokalnim postignućima, nadogradnjama, privatnošću i jasno označenim preuzimanjem.
- **Autentični screenshotovi:** galerija sada prikazuje 17 stvarnih Android PNG snimki, uključujući Game Over na 941 × 1672 i zaseban Progress zaslon. Nema generiranih mockupova koji glume nativnu aplikaciju.
- **Provjera paketa:** GitHub workflow zahtijeva 17 izvornih emulator snimki, provjerava njihove dimenzije i različitost, svih 22 SHA-256 zapisa i 23 datoteke u objavljenom izdanju.
- **Regresija izdanja:** provjera povijesnih paketa zadržava točne skupove za sve starije objave, a dvije nove snimke zahtijeva samo od verzije 1.0.79. Stara izdanja nisu prepisana.
- **Kvaliteta:** sačuvani su postojeći Android 15/16 testovi, offline logika igre i sigurnosne kontrole. APK/AAB release paketi ostaju nepotpisani do privatnog potpisivanja.
- **Pixel-perfect status:** potpuna 0 % RGB podudarnost nije potvrđena. Za svih devet prioritetnih zaslona potrebne su nove usporedbe izvornog PNG-a i stvarnog emulator prikaza bez skaliranja.

## v1.0.78 — Pouzdana provjera GitHub izdanja (build 208)

**Published:** 2026-11-11 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.78)

- **CI nakon spajanja:** ispravljena završna provjera objavljenih paketa; prethodna v1.0.77 uspješno je kompilirana i objavljena, ali je manifest pogrešno zahtijevao Splash snimku koja više nije u igri.
- **Povijesna kompatibilnost:** v1.0.59–v1.0.74 i dalje zahtijevaju Splash PNG; od v1.0.75 nadalje isporučuje se 15 autentičnih Android snimaka bez Splasha.
- **Regresijska zaštita:** novi testovi provjeravaju povijesni prijelaz i odbijaju izostanak bilo kojeg drugog obaveznog artefakta. Stroga SHA-256 provjera svih objavljenih paketa ostaje uključena.
- **Stabilnost:** bez brisanja prethodnih izdanja i bez promjena Android nativne logike; postojeće offline funkcije, zasloni i postavke zadržavaju se.
- **Vizualni QA:** nije potvrđeno 0 % RGB odstupanja; devet prioritetnih zaslona zahtijeva nova mjerljiva uspoređivanja sa stvarnim vlasničkim referencama.
