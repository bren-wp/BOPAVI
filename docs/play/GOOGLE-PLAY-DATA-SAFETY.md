# BOPAVI — radna provjera Data safety

**Status:** analiza izvornog koda za v0.1.30. Play Console obrazac i konačnu izjavu mora potvrditi odgovorni izdavač iz stvarnog potpisanog AAB-a.

## Utvrđeno u projektu

- Android manifest **nema INTERNET dozvolu**. Nisu ugrađeni oglašavanje, Google Analytics/Firebase Analytics, udaljena autentikacija niti kupnje pravim novcem.
- Ime/nadimak igrača, bodovi, osvojene virtualne kovanice, otključani likovi, oprema, napredak i postavke ostaju u lokalnom SharedPreferences spremištu.
- Igrač može ručno izvesti JSON datoteku putem Android sustavnog odabira dokumenta (Storage Access Framework). Sam bira lokalnu/cloud lokaciju. Ako odabere trećeg pružatelja pohrane, na kopiju se primjenjuju njegova pravila. BOPAVI ne šalje podatke na svoje poslužitelje.
- Android dopušta ručni izvoz JSON-a, ali ima **allowBackup=false** za automatsku OS sigurnosnu kopiju podataka aplikacije.
- Brisanje lokalnih podataka: Android Settings → Apps → BOPAVI → Storage → Clear storage ili deinstalacija. Ručno spremljene kopije na drugim mjestima korisnik uklanja samostalno.

## Radni odgovori za izdavača

| Pitanje | Analizirano stanje |
|---|---|
| Podaci koji se prikupljaju na developerov server | Nema prema trenutnom kodu. |
| Dijeljenje podataka s trećima | Nema prema trenutnom kodu; objasniti dobrovoljni izvoz. |
| Oglasi | Nema. |
| Kupnje stvarnim novcem | Nema. |
| Korisnički računi | Nema. |
| Lokalni podaci | Da: nadimak, rezultati, virtualna valuta, postavke i napredak. |
| Brisanje | Lokalno brisanjem storagea ili deinstalacijom. |
| Javni Privacy Policy URL | Obavezan, čak i bez prikupljanja podataka. |

**Obavezna provjera:** svi SDK-ovi, manifest, dopuštenja, podatkovni tokovi i distribucijski uvjeti u konačnoj Play verziji. Ako se dodaju mrežne funkcije, oglasi, praćenje ili crash analytics, revidirati Data safety i politiku prije objave.

Službeni izvor: https://support.google.com/googleplay/android-developer/answer/10787469
