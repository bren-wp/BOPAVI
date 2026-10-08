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
