# BOPAVI — produkcijski QA

## Automatski testovi

Pokrenuti `bash tests/run_native_core_tests.sh` i automatskog pilota iz `tests/Pilot.kt`. Prvi provjerava Kotlin/Swift geometriju, duge proceduralne cikluse, migraciju spremanja, nagrade i resurse. Drugi provjerava prohodnost uz pomoć automatiziranog pilota (ne provjerava subjektivnu težinu).

## Obavezni Android/iOS fizički testovi — nisu izvedeni u ovom okruženju

1. **Buildovi:** čist Debug i Release build, instalacija i prvo pokretanje na svim podržanim verzijama OS-a; provjera da su originalne audio datoteke upakirane.
2. **Grafika:** mali i veliki telefoni, izrezi i zaobljeni kutovi, rotacija/portret, prikazi svih svjetova, low-motion način rada.
3. **Zvuk:** start i prekid glazbe po svijetu, mute, zvuk na dodir, slušalice, primanje poziva, Bluetooth, povratak iz pozadine.
4. **Ekonomija:** nagradni level prvi put daje kovanice; ponavljanje ne daje; kupnja smanjuje stanje; pogodnost se troši točno jednom; ponovni ulazak zadržava podatke.
5. **Dugi leveli:** nastavi broj iznad prve proceduralne serije bez resetiranja naziva levela; migracija ranije verzije; otključavanje novog svijeta ne prisiljava izlazak iz trenutačnog.
6. **Stabilnost i performanse:** hladni start, više uzastopnih pokušaja, 30/60 FPS, memorija, baterija, state restore i ANR/crash logovi.
7. **Pristupačnost:** VoiceOver/TalkBack, kontrole najmanje 44 pt/48 dp, dinamički tekst, kontrast i smanjeno kretanje.

**Release gate:** ne označavati izdanje produkcijski spremnim dok Android i iOS buildovi, QA na stvarnim telefonima i crash/performance provjere ne budu stvarno prošli.


## v0.1.1 — vizualno i performansno poliranje
- Novi vizualni odziv skupljanja i aktivacije štita na obje platforme.
- Osam tematskih simbola i detalja prepreka; grafika ostaje unutar kolizijskih granica.
- iOS: Core Graphics boje i zaobljeni oblici bez stvaranja UIColor/UIBezierPath po svakom prolazu crtanja.
- Build gate: Android APK + AAB, iOS simulator + nepotpisani device build, audit resursa i privatnosti.
- Stvarni iPhone/Android uređaj, dugotrajan FPS/baterijski profil i potpisani IPA nisu potvrđeni.


## v0.1.2 — premium mobilni UX
- Android/iOS: provjeriti kartice svjetova, povratne gumbe, zaslon rezultata, zalihu pogodnosti i početni ekran.
- Grid levela renderira najviše 20 dostupnih odabira i ne prikazuje neaktivne stranice.
- Android provodi lintDebug i testDebugUnitTest prije APK/AAB.
- iOS treba proći Simulator build i nepotpisani device build prije GitHub izdanja.
- Dodatni profil FPS/memorije i uređajni smoke test nisu zamjena za uspješan CI.

## v0.1.3 — QA prema korisničkom videu
- Provjeriti frontalni dodir glavne cijevi, preklop široke završne kapice, dodir s donje i gornje strane.
- Bez kupljenog štita poraz mora nastupiti odmah; početni štit troši se jednokratno.
- Grafički i kolizijski promjer Bopija povećan je istovremeno.
- Android i iOS imaju isti kriterij sudara, uključujući bočno proširenje kapice.
- Novi home treba prikazati zaseban BOPAVI logo i veliku ilustraciju bez lažnog gumba unutar slike.
- Android splash i iOS LaunchArt prikaz bez bijele pozadine; sistemske trake u igri skrivene na Androidu.
- Stvarni uređajni smoke test i potpisani iOS IPA još se ne mogu potvrditi samo pomoću CI-ja.


## v0.1.4 — ilustrirani likovi i testovi
- Provjeriti 6 jedinstvenih prozirnih spriteova 512 × 512, bez dekodiranja unutar petlje crtanja.
- Provjeriti da štit, skupljanje i dodir kapice rade i s novom grafikom.
- Zadržati Android lint/JVM/APK/AAB i iOS Simulator/device/Swift fizikalne regresijske testove.
- Profiliranje na fizičkim telefonima i potpisani iOS IPA još nisu potvrđeni.

- Provjeriti omjere 19.5:9, 20:9, 16:9 te iPhone s Dynamic Islandom: cijela površina neba i tla, bez bijelih ili plavih pruga.


## v0.1.5 — premium vizual, titranje, stvarni launch QA
- Android: `onDraw` uvijek završava crtanje; provjera vremena ograničava samo korak fizike.
- Provjeriti 60/90/120 Hz zaslone, animaciju prve sekunde, dodir/pauzu/povratak iz pozadine, smanjene animacije 30 FPS.
- Za Android i iOS generirati 8 zasebnih 960×1600 svjetskih ilustracija. Učitati jednu pozadinu po svijetu, bez dekodiranja u petlji crtanja.
- Pokrenuti Android emulator i iOS Simulator; potvrditi pokretanje bez pada i spremiti screenshot početnog zaslona za ručni vizualni pregled.
- Izričito provjeriti home nasuprot BOPAVI referenci, kontrast zelenog gumba, pozicioniranje logotipa, pune visine zaslona.
- Testirati odabir levela, zaključavanje i spremanje napretka, sve svjetove, trenutni poraz pri sudaru bez štita.
- Buildovi i screenshotovi simulatora nisu dokaz stvarne stabilnosti, GPU profiliranja i potpune 1:1 usklađenosti na fizičkim telefonima.

- QA snimka iOS simulatora otkrila je preveliko uvećanje Bopija (landscape slika u portrait frameu) i dvostruki slogan. Prebačeno na istu portretnu kompoziciju koju koristi splash, bez drugog natpisa; provjeriti screenshotove obiju platformi nakon commita.

- Android emulator screenshot v0.1.5: potvrđen launch i početni UI; pronađen i uklonjen pretjeran tamnoplavi prazan pojas uz donje kontrole te usklađene Android sistemske trake. Ponoviti screenshot nakon zadnjeg commita.

- Android 90/120 Hz: usporediti položaj Bopija i brzinu prolaska kroz prepreke za jednako proteklo vrijeme; JUnit regresija simulira 60, 90 i 120 frejmova u sekundi.

## v0.1.7 — referentna vizualna kontrola
- Provjeriti razliku između dostavljenih konceptnih fotografija i stvarnih screenshotova: položaj logotipa, omjer Bopijeva tijela, detalji naočala, volumetrija otoka, slojevite prepreke i gumbi.
- Svaki grafički build mora potvrditi da logo PNG sadrži stvarne zasićene narančaste piksele; prethodni SVG gradijent proizvodio je presvijetlu plohu iako je izvorna paleta bila ispravna.
- Android i iOS karta svjetova mora imati četiri reda po dvije ilustrirane kartice, s preglednim nazivima i zaključanim stanjima.
- Pregledati završetak leta na svakoj platformi, prilagodbu rezultatske kartice malim ekranima, sve akcije (ponovno, oprema, mapa, početak) i čitljivost tijekom promjena orijentacije sustavnih traka.
- Obvezno: generiranje svih resursa, native audit, Android lint/JUnit/APK/AAB, Android emulator/screenshot, iOS Simulator build/launch/screenshot, Swift fizika/paritet i unsigned iPhone device build.
- Za stvarnu piksel-po-piksel jednakost potrebni su zasebni završni slojevi ilustracija i usporedni prikazi na fizičkim uređajima. Kolaži sami nisu zasebni spritesheetovi.


## v0.1.18 — ciljani audit upravljanja kadrovima i stanja
- Pregledani Kotlin/Swift simulacijski koraci, Android Canvas vsync i iOS CADisplayLink. Ranije se za svaki kadar preko 34 ms trajanje skraćivalo i gubilo se simulacijsko vrijeme.
- Novi model u oba corea dijeli kratke zastoje na korake do 34 ms, uz granicu 100 ms nakon duljeg zastoja. CI regresijski testovi uspoređuju ekvivalentne intervale.
- iOS callbacku uklonjena je redundantna provjera pauze nakon ranog izlaska. Na kompatibilnim uređajima može raditi do 120 Hz.
- U ovom ciklusu nisu mijenjani save/migration format, lokalna ljestvica ni grafički resursi. Ovo nije cjelovit dead-code audit svakog ekrana niti test na fizičkim uređajima; oboje ostaje za dodatnu provjeru.


## v0.1.18 — Android emulator: sistemski Pixel Launcher ANR
- Produkcijski CI na merge commitu `bbcd90b2` imao je neuspješnu provjeru otvaranja Postavki. Android APK/AAB, iOS, source audit i grafika bili su zeleni; GitHub Release je propisno preskočen.
- Pročitan je log posla `113868070085` i pregledani su screenshot `android-settings-diagnostic.png` i `android-current-ui.xml` iz artefakta `QA-Android-visual-flow` workflowa `37944417518`.
- Hijerarhija prikazuje sistemski dijalog `Pixel Launcher isn't responding` s opcijama `Close app` i `Wait`, iznad aktivnog BOPAVI početnog zaslona. BOPAVI proces ostao je aktivan; automatizirani dodir u Postavke preuzela je sistemska ANR komponenta.
- QA skripta sada može prepoznati i oporaviti samo **točan** Pixel Launcher ANR pritiskom na sistemsko `Wait`; zahtijeva da BOPAVI proces i dalje radi, ograničava pokušaje i nakon svakog koraka ponovno provjerava stvarni UI.
- Ako BOPAVI padne, pojavi se drukčiji dijalog ili se ekran Postavki i dalje ne otvori, test i dalje pada i pohranjuje screenshot, hijerarhiju i logcat. Nema preskakanja provjere ni lažno pozitivnih prolazaka.

## v0.1.18 — drugi produkcijski pad i AOSP emulator
- Workflow `37946675563`, Android smoke job `113875733201`: nakon uspješnog otvaranja Postavki ponovna provjera IGRAJ nije prošla. Spremljena XML hijerarhija iz QA artefakta `11623647855` potvrđuje da je opet prikazan isključivo sistemski dijalog `Pixel Launcher isn't responding`. Postavke su se prethodno zaista otvorile, a BOPAVI proces nije prijavljen kao ugašen.
- Android emulator QA prebačen je na službenu AOSP (`target: default`) sliku Androida 35, jer igra nema Google servise niti mrežne dozvole i ne treba Googleov Pixel Launcher koji redovito podiže ANR.
- Povratak iz Postavki sada se provjerava pet puta uz provjeru BOPAVI procesa. Oporavlja se samo točno prepoznat sistemski Pixel Launcher ANR; ako dijalog potroši Android Back, povratak se ponavlja tek nakon provjere da su Postavke i dalje otvorene.
- Test mora na kraju pronaći stvarna tri dostupna početna gumba ili jasno pasti, uz screenshot/XML/logcat. Nije dopušteno prihvatiti screenshot Postavki kao dokaz ispravnog povratka na početni zaslon.
- U audit dodana provjera sintakse shell QA skripte i prisutnosti AOSP emulator konfiguracije.

### Dopuna nakon PR #24: AOSP SDK arhiva na GitHub runneru
- PR workflow `37947986426` potvrdio je uspješne Android buildove i privacy/art audit, ali emulator se nije mogao ni instalirati: `system-images;android-35;default;x86_64` vraća `Error on ZipFile unknown archive` prije pokretanja aplikacije. Nije riječ o gameplay testu niti padu BOPAVI procesa.
- Vraćen je provjereno instalabilan emulator `target: google_apis`. Važan popravak ostaje: Android Back nakon Postavki ponovno se provjerava uz ograničenu obnovu samo potvrđenog Pixel Launcher ANR, ponovno slanje Back tipke samo ako je i dalje vidljiv zaslon Postavki te strogu provjeru sva tri početna gumba.
- Nema slabljenja smoke testa i release ostaje blokiran do zelenog stvarnog Android emulator gameplay QA na posljednjem commitu.


## v0.1.19 — vizualni parallax, lighting parity i regresije
- Uspoređeni Android `GameView.kt` i iOS `GameCanvas.swift`: nova tri sloja malih plutajućih otoka iscrtavaju se preko ilustrirane pozadine, a ispod prepreka i Bopija. Nema novog dekodiranja bitmapa unutar render petlje.
- Konkretan bug: Android `glowShaders` imao je vanjski prsten neprozirne boje; iOS vanjski alpha = 0. Androidov vanjski alpha kanal sada je 0.
- Parallax dijeli raspon 696 na pločice širine 174; tri stope pomaka 0.07/0.15/0.24 jednake su na obje platforme. Smanjene animacije zadržavaju okoliš, ali ga čine statičnim. Neispravna udaljenost ne pokreće pomak.
- Kotlin JUnit + Kotlin/Swift parity testovi provjeravaju pomake, granice i reduced-motion; Python audit potvrđuje oba renderera i da su svi svjetovi dostupni. Prije mergea CI mora potvrditi Android emulator i iOS simulator.
- Kolizije, bodovanje, migracije, novčanik, lokalna ljestvica, izvoz i uvoz nisu mijenjani. Osam svjetova odmah je dostupno; pojedinačni leveli napreduju zasebno.
- Tehnički testovi ne dokazuju vizualnu identičnost 1:1 s dostavljenim kompozitnim referencama. Potrebno je testirati zasebne ekrane i fizičke uređaje.


## v0.1.20 — Bopijev input-driven zamah
- Tap gestom Kotlin i Swift postavljaju `flapPulse=0.24` s postupnim opadanjem samo tijekom valjanih simulacijskih koraka. Ponovljeni dodir ga vraća na 0,24 s; neispravan delta ne mijenja impuls.
- Na obje platforme pomak krila dobiva dodatni 17° upstroke proporcionalan `flapPulse`; nakon otprilike 240 ms preostaje osnovna animacija. Proceduralni svjetski obojeni val i sedam sitnih čestica ostaju iza Bopija i ne mijenjaju kolizijsku geometriju.
- `reducedMotion` uklanja oba nova animirana elementa, a glasnoća, vibracije, igranje bez interneta i pohrana napretka ostaju netaknuti.
- Kotlin JUnit i Swift/Kotlin parity testovi pokrivaju impuls, ponovno pokretanje, vrijeme i trajanje. Source audit provjerava renderere i reduced-motion.
- Potrebno potvrditi PR CI na Android emulatoru i iOS simulatoru, zatim main CI i sva četiri release artefakta. Bez fizičkih uređaja nije moguće tvrditi punu 1:1 podudarnost prema slikama.
