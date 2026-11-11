# Next Android release

## v1.0.78 — Pouzdana provjera GitHub izdanja (build 208)

- **CI nakon spajanja:** ispravljena završna provjera objavljenih paketa; prethodna v1.0.77 uspješno je kompilirana i objavljena, ali je manifest pogrešno zahtijevao Splash snimku koja više nije u igri.
- **Povijesna kompatibilnost:** v1.0.59–v1.0.74 i dalje zahtijevaju Splash PNG; od v1.0.75 nadalje isporučuje se 15 autentičnih Android snimaka bez Splasha.
- **Regresijska zaštita:** novi testovi provjeravaju povijesni prijelaz i odbijaju izostanak bilo kojeg drugog obaveznog artefakta. Stroga SHA-256 provjera svih objavljenih paketa ostaje uključena.
- **Stabilnost:** bez brisanja prethodnih izdanja i bez promjena Android nativne logike; postojeće offline funkcije, zasloni i postavke zadržavaju se.
- **Vizualni QA:** nije potvrđeno 0 % RGB odstupanja; devet prioritetnih zaslona zahtijeva nova mjerljiva uspoređivanja sa stvarnim vlasničkim referencama.
