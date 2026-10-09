# BOPAVI v0.1.9 — audit mrtvog i nedostižnog koda

## Opseg

Pregledano je svih 12 ručno održavanih Kotlin i Swift datoteka u Android i iOS implementaciji: generiranje levela, simulacija, prikaz, navigacija, zvuk i trajna pohrana. Provjereni su pozivi funkcija unutar obje platforme; zasebno su obrađeni Kotlin/Swift parity testovi i automatizirani UI smoke.

## Konkretni nalazi i uklanjanja

- **Nedostižna mapa zaključanih svjetova**: `maxWorld()` sada uvijek vraća 7, pa su grane koje zatamnjuju kartice, prikazuju lokot ili zahtijevaju 30 prethodnih levela uklonjene na obje platforme.
- **Zastarjelo otključavanje novog svijeta**: uvjet `world == maxWorld() && world < 7` nije mogao postati istinit nakon otvaranja svih svjetova; uklonjen iz oba ProgressStorea.
- **Nekorištena metoda `rating()`**: bez poziva iz UI-ja, prikaza ili testova; uklonjena iz Kotlin i Swift simulacije.
- **Nekorišteno `completedLevelNumber`**: vrijednost se zapisivala pri prijelazu, ali nigdje nije čitana; uklonjena na obje platforme. Ostaje `completedOrdinal` koji je potreban za isplatu nagrade.
- **Postignuća**: ranije izoliran ekran povezan je s lokalnom ljestvicom umjesto njegovog uklanjanja. Sada koristi stvarni svijet s najvećim napretkom, a ne uvijek osmi svijet.
- **Izuzeci iz provjere jednom referenciranih funkcija**: `onTouchEvent` i `documentPicker` poziva platforma, dok je `signature` dio parity testova. Nisu uklonjeni.

## Sačuvana ponašanja

- Svih osam svjetova otvoreno je bez promjene spremljenih levela, kovanica ili oznaka napretka.
- Kontinuirani prijelaz levela ne resetira udaljenost niti zahtijeva dodatni klik.
- Štit se troši samo jednom tijekom kratkog perioda neranjivosti.
- Standardna težina i dalje koristi prethodnu gravitaciju i brzinu, a izvorna identifikacija levela ostaje ista.
- Zvuk i smanjene animacije ostaju dostupni u postavkama.

## Provjere prije izdanja

Source audit provjerava odsutnost uklonjenih nedostižnih grana i metoda, a Kotlin/Swift testovi provjeravaju tri težine. GitHub Actions mora zasebno potvrditi Android lint/JUnit, emulator screenshot QA, iOS simulator i unsigned device build te parity testove. Statički audit ne može dokazati 1:1 vizualni izgled ni odsutnost svih runtime grešaka; to zahtijeva i usporedbe stvarnih zaslona i testiranje na uređajima.
