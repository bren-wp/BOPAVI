# Next Android release

## v1.0.79 — Stvarna galerija i marketinški prikaz BOPAVI igre (build 209)

- **Marketinški README:** nova prezentacija igre sa svjetovima, junacima, lokalnim postignućima, nadogradnjama, privatnošću i jasno označenim preuzimanjem.
- **Autentični screenshotovi:** galerija sada prikazuje 17 stvarnih Android PNG snimki, uključujući Game Over na 941 × 1672 i zaseban Progress zaslon. Nema generiranih mockupova koji glume nativnu aplikaciju.
- **Provjera paketa:** GitHub workflow zahtijeva 17 izvornih emulator snimki, provjerava njihove dimenzije i različitost, svih 22 SHA-256 zapisa i 23 datoteke u objavljenom izdanju.
- **Regresija izdanja:** provjera povijesnih paketa zadržava točne skupove za sve starije objave, a dvije nove snimke zahtijeva samo od verzije 1.0.79. Stara izdanja nisu prepisana.
- **Kvaliteta:** sačuvani su postojeći Android 15/16 testovi, offline logika igre i sigurnosne kontrole. APK/AAB release paketi ostaju nepotpisani do privatnog potpisivanja.
- **Pixel-perfect status:** potpuna 0 % RGB podudarnost nije potvrđena. Za svih devet prioritetnih zaslona potrebne su nove usporedbe izvornog PNG-a i stvarnog emulator prikaza bez skaliranja.
