## v0.1.5 — Nebeski redizajn (2026-10-08)

- **Titranje na Androidu:** uklonjeno rano prekidanje `onDraw()`; svaki zatraženi kadar sada se iscrta do kraja, dok se interval animacije primjenjuje na simulaciju.
- **Android 15 / sistemske trake:** stabilizirane margine izbornika i uklonjena nepotrebna preraspodjela prikaza igre pri promjenama insets.
- **Početni ekran:** portretna ilustracija preko cijelog zaslona s uklopljenim logotipom i velikim zelenim IGRAJ; nakon QA snimke uklonjeni su uvećani izrez Bopija i duplicirani slogan.
- **Svjetovi:** osam različitih pozadina s tematskim krajolikom, lebdećim otocima i posebnim elementima; renderiraju se iz zajedničkog izvora bez mrežnog pristupa.
- **Karte svjetova:** pregled s ilustriranim prikazima okruženja; svijet ima vlastite boje i nagrade.
- **Leveli:** puni pregled 4×5 uključuje završene, dostupne i zaključane stavke umjesto gotovo praznog popisa.
- **Uređajni QA:** automatizirano pokretanje Android/iOS simulatora i snimke početnih zaslona kao release gate.
- Igra ostaje offline, bez oglasa i kupnji stvarnim novcem. Napredak i kovanice ostaju spremljeni.
- Produkcijska 1:1 podudarnost i rad na svim fizičkim telefonima nisu još dokazani; referentni kolaži nisu izvorne pojedinačne 2D grafike.

## v0.1.4 — Bopi oživljava (2026-10-08)
- Šest prozirnih ilustriranih likova, izvedenih iz postojećeg BOPAVI brendinga.
- Android i iOS koriste jednaku ilustraciju Bopija, uz boje izgleda otključanih kovanicama.
- Spriteovi se generiraju prije mobilnih buildova i učitavaju jednom po prikazu.
- Animirani nagib, trag leta i efekti nagrada ostaju; postojeći geometrijski prikaz je fallback.
- Nebo i tlo sada vizualno pokrivaju cijeli zaslon visokih i širokih mobitela, bez razvlačenja svijeta ili hitboxa.
- Dodatne provjere zaštite od lažnog sudara u otvorenom prolazu.
- Nema kupnji za novac, oglasa ni prikaza ukupnog broja levela.
- Završna potvrda izgleda i rada na svim fizičkim uređajima još je potrebna.

## v0.1.3 — Pravi dodir (2026-10-08)
- Popravljen dodir Bopija sa stvarno nacrtanim završnim kapicama stupova (Android i iOS).
- Sudar bez štita završava pokušaj u istom koraku simulacije; štit štiti točno jedan udarac.
- Automatizirani regresijski testovi kolizije dodani za Kotlin/JUnit i Swift.
- Novi ilustrirani početni ekran, rasterizirani logotip i splash prikaz.
- Android gameplay skriva sistemske trake, izbornici zadržavaju kontrastne tamne trake.
- Redizajnirani lebdeći otoci, čitljiviji gameplay bez nepotrebnog natpisa na podlozi.
- Grafika se generira u CI-ju i uključuje u Android/iOS build artefakte.
- Ne tvrdimo da je završena provjera stabilnosti na svim fizičkim telefonima.

## v0.1.2 — Premium let (2026-10-08)
- Android i iOS: premium stilsko ujednačavanje izbornika, gradijenata, tipografije, gumba i kartica svjetova.
- Početni ekran ostaje usredotočen na Bopija, kovanice i jedan gumb IGRAJ.
- Odabir otključanih levela sada koristi kompaktnu mrežu 4 × 5 s navigacijom bez beskorisnih gumba.
- Bolja čitljivost HUD-a na manjim iPhoneima te prilagođeni tekstovi u postavkama.
- Android JUnit testovi generatora i fizike prolaza, uz lint i build gate.
- Bez oglasa i kupnji stvarnim novcem; po svjetu različiti zvukovi i blago.
- Potpisivanje i testiranje na fizičkim telefonima još su potrebni.

## v0.1.1 — Uglancan let (2026-10-08)
- Android/iOS: dodatne animacije Bopija pri skupljanju blaga i udarcu u štit.
- Nove dekoracije prepreka za livade, vulkan, noć i svemir; kolizije koriste originalne granice prolaza.
- iOS: različiti simboli blaga po svijetu, manje stvaranja UIKit objekata pri crtanju.
- Bez kupnji za stvarni novac; napredak, zvukovi i jednostavan početni ekran sačuvani.
- Potvrda produkcijske stabilnosti zahtijeva testiranje na stvarnim uređajima.

# BOPAVI — povijest izdanja

## v0.1.0 — Prvi GitHub projekt
- Dvije zasebne mobilne aplikacije: Android (Kotlin) i iOS (Swift).
- Osam svjetova s vlastitim grafikama, zvukom i predmetima za skupljanje.
- Proceduralni leveli s trajnim nastavkom napredovanja.
- Animacije Bopija, pomične prepreke, više načina promjene izazova.
- Pogodnosti za osvojene kovanice, bez kupnji za novac.
- Zajednički generator i testiranje jednakosti fizike.
- Ispravljen tip brojila zamaha u Android spremanju te uključena iOS audio implementacija u build.
- Novi marketing README s logotipom, ilustracijom i opisima svjetova.
