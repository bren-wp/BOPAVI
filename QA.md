# BOPAVI — QA izdanja v0.1.0

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
