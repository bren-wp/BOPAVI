## v0.1.14 — pokretne prepreke i ujednačene boje
- Pokretni stupovi i ostale dinamičke prepreke dobivaju diskretne pulsirajuće oznake unutar gornjeg i donjeg ruba; statičke prepreke ne dobivaju lažne indikatore gibanja.
- U modu smanjenih animacija oznake ostaju vidljive, ali miruju. Dinamičke oznake ne mijenjaju širinu otvora, hitboxove ni brzinu gameplaya.
- Android i iOS sada koriste identične boje svih osam svjetova za prikupljanja i oznake prepreka.
- Source regresije provjeravaju pokretne prepreke, granice četiriju oznaka, palete i odsutnost stvaranja bitmapa u petlji crtanja.
- Android versionCode 15 / v0.1.14; iOS build 15 / v0.1.14. Izdanje se objavljuje samo nakon punog zelenog CI-ja na glavnoj grani.
- Vizualno 1:1 prema referencama još nije potvrđeno na fizičkim uređajima.

## v0.1.13 — sjaj kolekcionarskih predmeta i reakcija štita
- Android/iOS: prikupljanje kovanica, zvjezdica i pogodnosti dobiva kratku animaciju deset čestica u boji pojedinog svijeta.
- Potrošnja štita pri sudaru dobiva dvanaest blagih radijalnih iskri, usklađenih s postojećim krugom zaštite.
- Povratne informacije vezane su uz stvarne `collectPulse` i `impactPulse` vrijednosti iz simulacije. Ne otvaraju izbornike i ne zaustavljaju kontinuirani let.
- Pristupačnost: za korisnike sa smanjenim animacijama dodatne čestice se ne crtaju; postojeće mirne oznake prikupljanja i udara ostaju.
- Render petlje koriste postojeći Paint/CGContext i determinističku geometriju bez novih bitmapa i objekata čestica.
- Dodane source-audit regresije za Android/iOS broj čestica, palete, trajanje i ograničenja opaciteta.
- Android build 14 / 0.1.13 i iOS build 14 / 0.1.13; izdanje se objavljuje tek nakon punog zelenog CI-ja na `main`.
- Vizualna podudarnost 1:1 prema referencama nije još potvrđena pregledom svih zaslona na fizičkim uređajima.

## v0.1.12 — živi svjetovi i animirani trag leta
- Android i iOS: osam tematski obojenih skupova svjetlucavih čestica koji se pomiču s krajolikom, uz kontinuiranu blagiju animaciju svjetline.
- Prednji slojevi svakog svijeta imaju proceduralne travke/kristalne detalje uz dodatni parallax; između numeriranih levela ostaje neprekinuti let.
- Bopi sada iza sebe ostavlja pet tankih zakrivljenih tragova pri aktivnom letu; dinamički detalji crtaju se Canvas/Core Graphics primitivima bez novih bitmapa i dekodiranja.
- U postavci smanjenih animacija gibanju okoliša zaustavlja se pomak/sway, a tragovi leta ne crtaju se.
- Dodani izvorni regresijski testovi za Android/iOS podudarnost broja čestica, pomaka i podrške za smanjene animacije.
- Android build 13 / 0.1.12, iOS build 13 / 0.1.12; GitHub pre-release uvjetovan svim zelenim CI provjerama.
- Izgled nije potvrđen kao 1:1 s dostavljenim referencama na fizičkim uređajima; rad na izvornoj ilustraciji i osvjetljenju ostaje otvoren.

## v0.1.11 — premium HUD, vibracije i provjera spremanja
- Android/iOS: zaseban prekidač „Vibracije pri igranju”. Prikupljanje predmeta aktivira taktilni signal samo kada je korisnik to omogućio.
- Android/iOS: štit i magnet imaju usklađene tamnoplave statusne oznake; prikazuju se preostali štitovi i vrijeme magneta.
- Optimiziran iOS HUD: font za statusne oznake koristi se ponovno umjesto stvaranja novog fonta svakog kadra.
- Preferencija vibracija prenosi se putem postojećih v5 sigurnosnih kopija; nedostajuća vrijednost u starim kopijama zadržava zadanu opciju.
- Ispravljene zastarjele pretpostavke testova o otključavanju svjetova; SwiftSaves sada radi u CI-ju s provjerom migracija i izvoza/uvoza vibracija.
- Regresijski izvorni audit potvrđuje HUD, postavke i oba renderera.
- Android build 12/version 0.1.11, iOS build 12/marketing 0.1.11; objava tek uz sve zelene jobove na main.

## v0.1.10 — poliranje pauze, zvuka i rezultata
- U Android/iOS pauzi moguće je odmah uključiti ili isključiti sav zvuk bez napuštanja igre; postavka se trajno pamti.
- Prikaz pauze usklađen je na obje platforme i uključuje jasnu uputu kako nastaviti igru; Android osvježava sadržaj i pri zaustavljanju.
- Završni rezultat prikazuje ime igrača i odabranu težinu.
- Izračun bodova koristi sigurno zbrajanje i ograničenje do 100 milijuna kako dugi letovi ne bi uzrokovali overflow.
- Dodani Kotlin/Swift regresijski testovi i izvorni audit.
- Android emulator QA: oporavak samo od točno prepoznatog dijaloga sustavnog Pixel Launchera, uz stvarni screenshot početnog zaslona; ANR same igre i dalje je blokirajuća greška.
- Android/iOS: pristupačno stanje lika prelazi u „Bopi leti” tek nakon stvarnog dodira. Emulator potvrđuje početak leta prije nego provjerava sudar i rezultat; izgubljeni sistemski dodiri ne stvaraju lažni gameplay pad.
- Android build 11 / 0.1.10 i iOS build 11 / 0.1.10. Izdanje je uvjetovano zelenim CI-jem na main.

## v0.1.9 — težine, lokalni rezultati i čišćenje koda
- Tri težine (Opušteno, Standardno, Izazovno) na Androidu i iOS-u mijenjaju brzinu i gravitaciju bez prekida levela; standardni način zadržava staru fiziku.
- Lokalna top-10 ljestvica prikazuje samo stvarne rezultate s imenima igrača, svijetom, težinom i brojem prolaza. Nema izmišljenih igrača ni mrežne povezanosti.
- Težina i ljestvica uključene su u kompatibilne sigurnosne kopije podataka uz provjeru raspona pri uvozu.
- Uklonjene mrtve grane zaključavanja svjetova, neupotrebljavani rating helper i zastarjela logika otključavanja svijeta nakon 30 levela.
- Android/iOS gameplay parity testovi i provjere izvornog koda dopunjeni su za nove načine rada.
- Izdanje se priprema za objavu tek nakon zelenog GitHub Actions CI-ja; potpisani App Store IPA nije uključen.
## v0.1.8 — Integracija slike i igre, otključani svjetovi
- Svih osam svjetova dostupno odmah, uz očuvani individualni napredak.
- Lokalni profil igrača s izvozom i uvozom spremljenog imena, autorske informacije i regresijske provjere.
- Dorade CI-ja za stabilnije čekanje rezultatskog ekrana i Android/iOS kontrole fizičkih prepreka.
- Izdanje se objavljuje isključivo nakon zelenog CI-ja za main, s Android i iOS artefaktima.
- Podudarnost 1:1 s referencama nije potvrđena; potrebno je testiranje na fizičkim uređajima.

## Razvoj v0.1.8 — optimizacija fizike i regresijski testovi (neobjavljeno)
- Android: uklonjena privremena lista iz izračuna pulsirajućih prepreka u aktivnoj petlji simulacije, uz očuvanu geometriju i pravila kolizije.
- Dodani JUnit regresijski scenariji za 120 Hz pomične prepreke i sprečavanje višestrukog trošenja štita tijekom istog kontakta.
- iOS: uklonjen konflikt dvaju fiksnih Auto Layout ograničenja brojača kovanica na početnom ekranu; dodan regresijski audit početnog zaslona.
- Promjene zahtijevaju uspješan GitHub Actions CI prije spajanja; vizualna podudarnost referencama nije potvrđena.

## v0.1.7 — Vizualna dorada prema referencama
- Ispravljen BOPAVI logotip: zasićena narančasta slova, tamnoplavi 3D rubovi i drveni podnaslov; dodan QA test stvarnih narančastih piksela u izvezenom PNG-u.
- Bogatija ilustracija Bopija: odsjaji pilot-naočala, sjenčanje lica, svjetliji trbuh i obrazi, uz postojeća odvojeno animirana krila.
- Splash/početni ekran: lebdeći otoci, cvijeće, oblačni slojevi, slapovi i udaljeni dvorci; kompaktniji HUD kovanica gore desno i sjajniji zeleni gumb IGRAJ.
- Android i iOS: dvije kolone ilustriranih kartica za svih osam svjetova, s čitljivim stanjem otključavanja.
- Gameplay: dodatni detalji kamena, pukotine, mahovina i rubno osvjetljenje tematskih prepreka.
- Zaslon rezultata: tematska ilustrirana pozadina, velika kartica rezultata i jasne kontrole umjesto generičke tekstualne stranice.
- Očuvani su numerirani kontinuirani leveli, offline napredak, osam zasebnih zvučnih tema i pogodnosti isključivo za osvojene kovanice. Nema plaćanja stvarnim novcem.
- Android/iOS QA i produkcijski grafički audit obvezni su prije izdanja. Vizualna 1:1 podudarnost i testiranje na svim fizičkim uređajima nisu potvrđeni.

## v0.1.6 — Krila i neprekinuti let (2026-10-08)
- Iz originalnog BOPAVI SVG-a nastaje šest prozirnih trupova i dvanaest odvojenih krila za stvarnu animaciju mahanja na Androidu i iOS-u.
- Smanjene animacije zadržavaju nepomična krila; geometrija sudara je nepromijenjena.
- Prelazak između numeriranih levela više ne vraća globalnu udaljenost na nulu; nova prepreka odmah se pojavljuje u vidljivom području.
- Nenametljiv natpis novog levela ne prekida let i ne traži dodatni dodir.
- Nagrađivanje kovanicama, kupnja pogodnosti kovanicama, 8 zasebnih zvučnih tema i offline spremanje ostaju.
- Novi Android/JUnit i iOS/Swift regresijski testovi za prijelaze, uz postojeći QA grafike i uređajnih simulacija.
- Vizualna 1:1 jednakost referencama i stabilnost na svim fizičkim uređajima nisu potvrđene.

## v0.1.5 — Nebeski redizajn (2026-10-08)

- **Titranje na Androidu:** uklonjeno rano prekidanje `onDraw()`; svaki zatraženi kadar sada se iscrta do kraja, a pomak fizike koristi stvarno trajanje kadra (60/90/120 Hz), bez pragova koji su uzrokovali trzanje na 90 Hz.
- **Android 15 / sistemske trake:** stabilizirane margine izbornika i uklonjena nepotrebna preraspodjela prikaza igre pri promjenama insets.
- **Početni ekran:** portretna ilustracija s logotipom i zelenim IGRAJ; uklonjeni uvećani izrez Bopija, duplicirani slogan i tamni prazni pojas u dnu. Android sistemske trake imaju diskretnu tamnoplavu pozadinu.
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
