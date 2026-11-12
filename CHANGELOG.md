# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.80 — Napredak i Karta razina: nativna navigacija i referentni raster (build 210)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.80)

- **Napredak:** svih osam kartica svjetova postaju prave pristupačne Android kontrole. Odabirom kartice otvara se točan svijet na Karti razina, s postojećim spremljenim novčićima, zvjezdicama, razinama i statistikama.
- **Čitljivost:** naslov, vrijednosti rezultata, broj novčića i zamaha te broj zvjezdica skaliraju se unutar postojećeg prostora bez dodavanja izmišljenih vrijednosti ili novih valuta.
- **Karta razina:** na izvornom 941×1672 prikazu ilustracija sada koristi puni referentni raster; interaktivni elementi i dalje poštuju sigurne sistemske rubove i najmanje 48dp dodirne površine.
- **Svi svjetovi:** sačuvana je izvorna paleta prve stranice Sky Meadows/Crystal Ruins/Volcano Peaks. Kasnije stranice dobivaju zasebne lokalno crtane palete za preostale svjetove, bez promjene stvarnih zaključavanja ili položaja čvorova.
- **Rotacija/DPI:** otvoreni zasloni Napredak i Karta razina ponovno se sastavljaju nakon promjene orijentacije, veličine i gustoće; trenutačna igra ne prekida se.
- **QA:** Android/JVM i stvarni emulator provjeravaju funkcionalne prijelaze Progress → World Map, uključujući promjenu veličine i stvarne nove snimke zaslona. Prošli rezultat ne smije se predstavljati kao 0 % RGB podudarnost.
- **Distribucija:** novo GitHub izdanje s APK/AAB paketima (release paketi nepotpisani), 17 stvarnih Android snimki i SHA-256 kontrolom; bez novog pull requesta.

## v1.0.79 — Stvarna galerija i marketinški prikaz BOPAVI igre (build 209)

**Published:** 2026-11-12 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.79)

- **Marketinški README:** nova prezentacija igre sa svjetovima, junacima, lokalnim postignućima, nadogradnjama, privatnošću i jasno označenim preuzimanjem.
- **Autentični screenshotovi:** galerija sada prikazuje 17 stvarnih Android PNG snimki, uključujući Game Over na 941 × 1672 i zaseban Progress zaslon. Nema generiranih mockupova koji glume nativnu aplikaciju.
- **Provjera paketa:** GitHub workflow zahtijeva 17 izvornih emulator snimki, provjerava njihove dimenzije i različitost, svih 22 SHA-256 zapisa i 23 datoteke u objavljenom izdanju.
- **Regresija izdanja:** provjera povijesnih paketa zadržava točne skupove za sve starije objave, a dvije nove snimke zahtijeva samo od verzije 1.0.79. Stara izdanja nisu prepisana.
- **Kvaliteta:** sačuvani su postojeći Android 15/16 testovi, offline logika igre i sigurnosne kontrole. APK/AAB release paketi ostaju nepotpisani do privatnog potpisivanja.
- **Pixel-perfect status:** potpuna 0 % RGB podudarnost nije potvrđena. Za svih devet prioritetnih zaslona potrebne su nove usporedbe izvornog PNG-a i stvarnog emulator prikaza bez skaliranja.
