## Premium difficulty settings — click-by-click acceptance
- On Android 15/16 and iOS simulator the **Postavke → Igrač i težina** section has horizontal Lagano / Normalno / Zahtjevno cards with actual pilot illustrations and no clipping on narrow displays.
- Tap each card: only the chosen card gains the gold border and accessible selected status; choice is persisted with original offline values 0/1/2.
- Confirm the next real flight uses the selected difficulty and the score/progression/save logic is unchanged; while inside the settings page there is no unexpected restart.
- Emulator QA must scroll to the card row, tap Lagano, check its updated accessible selection, then tap Normalno and check restored selection before returning home.
- Validate volume sliders, sound/haptics, data export/import, reduced motion and back navigation still work. TalkBack and VoiceOver expose actionable selection labels.
- Full release gate: privacy/art audit, Android lint/JVM/APK/unsigned AAB, Android 15/16 real smoke, iOS simulator/unsigned device and Swift parity. No production signing on GitHub.

## Complete character portrait parity — acceptance gates
- Android 15/16 and iOS simulator: for all nine pilots, gallery cards, selected pilot hero and native PAUZA display **both left and right wings**, not the earlier wingless torso-only asset. Portantin retains his passenger, goggles, mask and gloves; Noa/Any retain unique silhouettes.
- The same indices and owned masks are used throughout: Portantin / Noa / Any are the first visual row but ownership, coin costs and save persistence remain untouched.
- Confirm all `bopiportrait0..8` have 512×512 RGBA pixels with nonempty transparent margins and visibly differ from corresponding torso `bopi0..8`; Android/iOS PNG bytes must match exactly.
- Test first flap, separate wing animation, reduced-motion option and game collisions without using precomposed menu sprites in the active flight renderer.
- Validate long labels and full wing silhouettes do not clip in 3×3 gallery cards, preview and PAUZA on small screens.
- Run source/privacy audit, art generation, Android lint/JUnit/debug APK/unsigned AAB, Android API35/36 emulator smoke, iOS simulator/unsigned device/Swift parity before merging. Never production-sign builds on GitHub.

## Eight-world premium art — native parity acceptance
- Generate all eight world PNGs with the existing Python/CairoSVG/Pillow workflow. Both Android `drawable-nodpi/world0..7.png` and iOS `Assets.xcassets/World0..7.imageset/world0..7.png` must be byte-identical for each world.
- Verify visual uniqueness: castles (green/night), cascades, crystal spires, snow, lava, sky bridges, pearl reef and ring portal. No visual counter/buttons are rasterized into game art.
- Pixel smoke on Android 15, Android 16 and iOS simulator: no stretched islands, backdrop seam, clipped landmarks, interactive-element overlaps or obstructed moving gate collision entrances.
- Confirm older devices' gameplay memory/performance unchanged (textures still 960×1600 RGB, loaded once). Check flight, infinite-level seam, coin collection, results, idle pause lifecycle and reduced-motion setting.
- Do not merge before generator/asset audit, APK + unsigned AAB, Kotlin tests, iOS simulator + unsigned device build and real emulator smoke are green. No production GitHub signing.
- The 3D character and environment reference art is still a **separate visual parity gap**, not a checked production claim.

## In-flight HUD parity — required QA
- On Android API35/API36 and iOS simulator check top row displays **actual run coins, actual collected stars, actual current level**; native pause button remains unobstructed and accessible.
- The blue progress bar uses passed/current level total gates (not fictional meters), while cumulative passed gates and screen-reader progress never reset during seamless level promotion.
- If shield/magnet is active, show the authentic current count/time. Hidden boosts must not display nonzero icons.
- Idle flight shows **no HUD and no pause button** before first real flap. Paused gameplay freezes physics and only the real custom modal renders PAUZA, not an additional overlapping painted panel.
- No duplicate iOS UIView counter; no extra per-frame UIImage decoding, scaling changes or collision geometry changes.
- Require visual screenshots, Android unit/lint/APK/unsigned AAB, API35 and API36 smoke, iOS simulator/unsigned device tests, Swift save/physics parity and privacy audit before merge.
- Visual parity with supplied high-fidelity 3D art remains **open**, even if these real UI controls pass.

## Premium character picker — regression gate
- On Android 15 and 16 and iOS simulator, verify pilot gallery first visual row is Portantin / Noa / Any and all nine cards remain accessible by real scrolling.
- Check the chosen character hero matches the actual persisted index and correct world (world art/portrait are separate layers).
- Purchase confirmation must require sufficient **earned offline** coins; on cancel, ownership and wallet are unchanged; on success, the correct old index is stored without migration.
- Primary CTA stays **below** all nine selectable cards and launches only after explicit tap; first-flap pause must not be shown on idle launch.
- Run actual end-to-end pilot picker swipe/navigation, Kotlin + Swift/save parity, Android AAB unsigned, iOS simulator and unsigned device build. No GitHub signing.

## Premium results — acceptance criteria
- Finish an actual run. All three summary cards must match the *same* GameSimulation score, coins collected and cumulative passed gates. Existing record/previous-best comparisons still run **before** persisting the completed run.
- Four reward cards show actual coins, stars, earned but unclaimed high-score bonus and persistent best score, without implying that pickups are already spendable wallet credits.
- Verify local record bonus claims only once: bonus label updates to +0, wallet balance increases by the claim exactly once, save does not re-record wins/losses on claim.
- Verify 3-/4-column layouts on small Android and iOS screens, VoiceOver/TalkBack labels, Safe Area scroll, primary Retry, bottom Leaderboard/Home and native Share with only the score & gate count, not personal names.
- Verify effects and gameplay cannot run behind the result screen, home/level state remains intact, and there are no invented paid rewards.
- Android CI: privacy, JVM tests, API35 + API36 emulator flows, APK and unsigned AAB. iOS: launch smoke, Swift parity/migration, unsigned device compilation.
- Photorealistic reference artwork remains a separate unresolved visual parity requirement; maintain an honest screenshot mismatch log.

## Premium pause — regression and interaction QA
- Confirm no PAUZA action before the first flap, on both systems and accessibility tree.
- With active flight paused: gameplay coordinates and score stop advancing; music loop remains paused even if global sound is switched back on.
- Tap Continue => closes overlay and resumes same run; Retry => new idle run of the same world/level; Exit => home; Settings => actual local settings.
- Master Sound and Music toggles affect the real audio mixer, not cosmetic labels. Controls opens accurate gesture instructions and returns to the frozen flight.
- Dialog fully scrolls and remains accessible on compact phones / Display Zoom; labels and margins must not clip behind status or navigation regions.
- Android and iOS screenshots still require pixel-diff approval against the supplied 3D rendered pause reference. No fabricated background screenshot is used.

## Premium UI iteration 2 — QA acceptance
- World card numbering 1–8 uses the actual world index; badges/arrows remain noninteractive children, while the complete card is the tap target.
- Illustrated 4-column / 5-row level grid reads persisted frontier: complete levels show ✓; next available shows the active badge; future levels remain visibly locked with a non-startable tap.
- Verify previous/next 20-level paging at boundaries 1, 21 and 360; confirm saved stream frontiers do not reset in marathon mode.
- Music/effects volume sliders are separate real playback gains, defaulting to 80/70, saved as 0–100 on device and exported in v5 backup as optional fields; imported older v5 and v1–v4 saves still work.
- Run Android API35/API36 click-path smoke, Android unit tests, iOS simulator/device build, Swift core/save parity, and privacy audit before merging. Signing must remain disabled on GitHub.
- Screenshot comparison against provided 3D reference art remains incomplete; do not claim pixel-identical artwork yet.

## Premium UI references — verification gate

- Review and track all ten supplied reference screens; run screenshot comparisons against actual Android API35/36 and iOS simulator outputs.
- Preserve existing UI hierarchy and game lifecycle smoke tests including no pause until the first flap, accessible 9-pilot gallery and playable offline progression.
- The first implementation stage is a production code redesign, **not yet verified 1:1** visually against the photorealistic 3D concept art.
- Test gesture-navigation safe areas on 360dp Android phones and compact iPhones. Follow up on real hardware before claiming production-perfect screenshots.
- Signed Google Play bundles must **never** be generated or uploaded by GitHub Actions.

## v0.1.31 Android 15 launcher ANR investigation (2026-10-10)
- Main-branch CI 38013725742 failed only the Android 15 pilot-gallery smoke; the APK/AAB build, Android 16 smoke and iOS simulator succeeded.
- The CI trace repeatedly reported the SYSTEM Pixel Launcher ANR before seeing Portantin, Noa, and Any. Preserved XML and real screenshot show the game picker rendered with a scrollable Android `ScrollView`.
- QA now terminates only the exact confirmed hung launcher process, checks BOPAVI remains running and still requires real scrolling to find all three characters. An unverified/system-other or BOPAVI ANR is never suppressed.
- GitHub signing tests/manual AAB signing are disabled. Debug APK and unsigned AAB continue to be created for download. Do not claim Play upload readiness without offline signing.

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


## v0.1.21 — kartice svjetova i autentični lokalni rekord
- Izvorna iOS kartica imala je naslov/natpis položen preko ilustracije bez sigurnog kontrasta. UI je preuređen u odvojeni gornji preview (137 pt) i dva natpisa na tamnoplavoj podlozi; Android već koristi tu podjelu.
- Obje platforme jasno ističu trenutačno odabrani svijet i zadržavaju zasebnu pristupačnu oznaku za cijelu karticu; svih 8 svjetova otključano je odmah.
- `ResultHeadline` na Kotlinu/Swiftu koristi `score`, `previousBest` i `won`: nula/izjednačenje nije novi rekord; pobjeda ima prednost; novi rekord mora biti strogo veći od ranijeg. UI dohvati prijašnji rekord prije `recordRun`; nakon spremanja prikazuje ažurirani osobni najbolji rezultat.
- JUnit i Swift/Kotlin parity testovi provjeravaju iste ulaze; source audit provjerava strukturu kartica, redoslijed snimanja i dostupnost osam svjetova.
- CI treba dokazati Android lint/JUnit/build, emulator gameplay QA, iOS simulator/Swift/save/device build prije mergea, zatim iste poslove na main i 4 release artefakta. Bez fizičkih uređaja nije potvrđena vizualna identičnost 1:1.


## v0.1.22 — oznake levela i pauza
- Izbornik koristi `LevelEngine.create(world,n).type` i `BopaviCore.create(world,n).type` za svaki vidljiv level, te usklađene oznake `LevelKind.name/icon` na Kotlinu i Swiftu. Simulacija igre, generator i pohrana ostaju nepromijenjeni.
- U svim svjetovima svi leveli imaju deterministički tip: 1 = Normalni, 15 = Izazovni, 30 = Bonus, 60 = Elitni, 75 = Izazovni, 90 = Bonus, 120/360 = Elitni. Zaključani leveli nisu automatski otvoreni; svih osam svjetova jest.
- Mreže prikazuju zonu i raspon stranice, ikonice i pristupačne nazive koji uključuju status; odabrani/napredni level se razlikuje vizualno bez pretvaranja stvarno zaključanih levela u otključane.
- Pauza u oba sučelja pokazuje stvarni aktivni level i tekst `ODABERI NASTAVI LET`; akcije zvuka, povratka i nastavka ostaju kao prije.
- Kotlin JUnit i Kotlin/Swift parity provjere ispituju granične nivoe i naziv/ikonu; Python audit potvrđuje integraciju. Nakon zelenog PR CI-ja treba provjeriti isto na main i 4 stvarna GitHub Release artefakta. Screenshotovi s emulatora/simulatora nisu dokaz 1:1 za svaki fizički telefon.


## v0.1.23 — regresija: pauza samo tijekom stvarnog leta
- Analizom `startGame` na obje platforme utvrđeno je da se pauza dosad stvarala vidljiva čim korisnik odabere `IGRAJ`, čak i kada simulacija čeka prvi `flap` (`active=false`).
- Sada je Android `pause.visibility=INVISIBLE`, a iOS `pause.isHidden=true`, sve do prvog `inactive→active` prijelaza unutar simulacije; iOS početni HUD je sakriven, Android/iOS rendereri odgađaju HUD do aktivnog leta. Na početnom izborniku nema kontrole pauze.
- Na odlasku na drugi zaslon prethodna gameplay hijerarhija se zamjenjuje ili uklanja. Android/iOS rezultatski callback se prihvaća samo za trenutno aktivan `GameView`/`GameCanvas`, što sprječava povrat zastarjelog rezultata.
- Android emulator QA više ne traži pauzu kao dokaz da je `IGRAJ` otvorio igru: traži stvarni `Dodirni za let Bopija` view i izričito odbija pauzu prije prvog zamaha. Nakon zamaha mora se pojaviti vidljiva pauza (ako nije već stigao završni rezultat); nakon završetka mora nestati. Početni izbornik i dalje mora izložiti točno tri pristupačna gumba bez pauze ili statusa leta.
- Kotlin JUnit + Kotlin/Swift parity testovi osiguravaju da `active` i fizika ostaju u mirovanju prije prvog dodira. Python source audit provjerava oba callbacka, ispravan početni visibility, aktivni HUD, zaštitu callbackova i rigorozni emulator skript.
- Merge i izdanje dopušteni su tek nakon zelenog PR CI-ja, zelenog novog CI-ja na main te stvarne potvrde četiri artefakta; ovo nije zamjena za usporedni screenshot QA svih zaslona s referentnim renderima.


## v0.1.24 — neprekinuti let, Portantin i zaštita sigurnosnih kopija
- **Seamless:** Simulacije unaprijed izračunavaju `nextLevel` i njegov `nextOrigin` iz fiksnog razmaka posljednje prepreke. Oba renderera crtaju dolazne prepreke prije završetka trenutnog levela, zadržavajući njihovu točnu poziciju nakon promjene broja levela. Stara metoda ubacivanja prve nove prepreke na x=300 tek nakon završetka uklonjena je.
- **Neprekidni HUD:** statični prijelazni overlay se ne iscrtava, a ukupan broj prolaza `totalPassed` monotono raste umjesto resetiranja po levelu. Test mora usporediti koordinatu prve dolazne prepreke prije i nakon granice.
- **Likovi:** šest ranijih Bopijevih boja + besplatan Portantin (`skin_index=6`), izbor prije `startGame` na Androidu i iOS-u, pregled spriteova i potvrda početka. Ostaje podržan postojeći v5 JSON save i kupljene boje, čak i kad novi kod učita stare kopije.
- **Grafika:** jedan uređivi SVG Portantina u `docs/assets/portantin.svg`, s dvije izdvojive grupe krila; isti generirani 512 px prozirni spriteovi na obje platforme. Generator ruši build ako grafički slojevi nedostaju.
- **Sigurnost:** nijedan import ne smije neograničeno učitavati datoteku od vanjskog pružatelja; granica 550000 na obje platforme provjerava se i prije i nakon čitanja. Nema novih mrežnih dozvola ni telemetrije.
- **Quality gate:** Android lint/JUnit/APK/AAB, emulator stvarni tijek IGRAJ→ODABERI LIKA→POLETI i sudar, Swift simulator/physics/save/device build te source/privacy audit. Nakon svih zelenih PR poslova slijedi merge, puni main CI i provjera četiri GitHub Release artefakta. Fizički 1:1 screenshot audit još nije automatiziran.


## v0.1.25 — zona bez skoka i ilustrirani odabir lika
- Kotlin i Swift `prepareNext` izračunavaju ulazni razmak na temelju `lastGate.x - previousGate.x`; samo sintetski level s jednom preprekom zadržava sigurnu zadanu udaljenost od 242. Položaj unaprijed nacrtanih prepreka ne mijenja se prilikom promocije sljedećeg levela. Globalna udaljenost, prolazi, scena i kamera ostaju kontinuirani.
- Kotlin JUnit i Kotlin/Swift parity provjeravaju sva osam svijeta te levele 2, 62, 122, 182, 242, 302 i 360, uključujući šest izazovnih zona i završetak ciklusa. Testovi uspoređuju razmak stvarnog prethodnog niza s prvim stupom unaprijed pripremljenog sljedećeg levela.
- Obje platforme koriste galeriju svih sedam stvarnih likova s dva stupca i čitljivim natpisima; Android koristi `R.drawable.bopi0..6`, iOS `Bopi0..6`. Portantin ostaje dostupan odmah. Preskakanje pre-flight odabira nije dodano.
- Potvrda virtualne kupnje je obvezna: novi zaključani lik ne smije smanjiti broj osvojenih kovanica na prvi dodir. Dijalog mora navesti točnu cijenu i omogućiti odustajanje. Već otključani likovi ne naplaćuju se ponovno; preostale kovanice i skin izbor spremaju se lokalno.
- Python audit provjerava prisutnost ilustracija, odvojenu pristupačnost kartica, uvjetovano trošenje i obje galerije. Prije spajanja i objave obvezni su svi Android/iOS CI koraci, Android emulator smoke QA, iOS simulator/Swift/backup testovi i potvrda sva četiri stvarna artefakta. Nije potvrđena apsolutna pixel-perfect jednakost 1:1 na fizičkim uređajima.

## v0.1.26 — Noa i Any
- Provjeriti da se Noa i Any nalaze u izboru lika prije svakog leta te među svih devet kartica u Postavkama Androida i iOS-a.
- Potvrditi svaki portret i odvojeno gibanje lijevog/desnog krila pri dodiru, uključujući smanjene animacije i mali ekran.
- Otključati Nou za 220 te Any za 240 kovanica, odbiti potvrdu bez trošenja i ponovno izabrati otključani lik bez druge naplate.
- Provjeriti da v3 i v5 JSON kopije ne gube postojeće likove, saldo ni spremljeni izbor te da v5 pohranjuje nove premium likove.
- CI mora potvrditi svih devet nepraznih, međusobno različitih RGBA tijela 512×512 te osamnaest zasebnih krila i ispravne Android/iOS pakete.

## v0.1.27 — stvarna grafika premium likova
- Za lik Noa (indeks 7) i Any (indeks 8) provjeriti da render tijekom igre pokazuje izvorno tijelo i OBA izvorna krila, a ne dijelove Portantina (indeks 6).
- CI mora provjeriti identične 512×512 RGBA resurse između Android drawable-nodpi i iOS imageset za devet tijela i osamnaest krila.
- Vizualno pregledati zamah oba krila na oba sustava, aktivno/smanjeno gibanje i povratak s pauze; napredak i otključavanja ostaju nepromijenjeni.

## v0.1.28 — regresija premium likova, kupljene opreme i povratka iz pozadine
- Android: odabrati Nou i Any i otvoriti zaslon **ODABERI LIKA**; provjeriti stvarne ilustracije bez pada i pravilno pokretanje leta.
- Android/iOS: kupiti štit i magnet, otvoriti mirujući let, zaključati uređaj, vratiti se i odustati bez zamaha; broj kupljenih predmeta mora ostati nepromijenjen. Pri prvom stvarnom zamahu svaka prisutna pogodnost troši se jednom.
- Započeti let, premjestiti aplikaciju u pozadinu i vratiti se: let mora ostati pauziran dok korisnik izričito ne odabere nastavak. Mirujući pregled mora primati dodire bez nevidljive pauze.
- Automatski CI obuhvaća Kotlin i Swift testove, Python audit, Android emulator, iOS simulator i oba builda. Testovi na fizičkim uređajima i tržišno potpisani paketi nisu potvrđeni.

## v0.1.29 — sigurnost kopija i kontrola aktivnog leta
- iOS: pripremiti v5 JSON sigurnosnu kopiju s neispravnim osmom stavkom streamFrontiers. Uvoz se mora odbiti, a cijeli prethodni spremljeni napredak, kovanice, postavke i marker već preuzetih nagrada moraju ostati nepromijenjeni.
- Android/iOS: dovršiti barem jedan level u neprekinutom letu, potom završiti pokušaj. Zaslon rezultata mora prikazati ukupan broj prolaza iz svih levela u tom pokušaju.
- Android: u aktivnom letu pritisnuti tipku ili gestu Natrag — mora se otvoriti izbornik pauze, a odabirom Nastavi let isti pokušaj mora nastaviti. U mirujućem letu Natrag vraća izbor lika i ne troši opremu; na početnom zaslonu izlazi iz aplikacije.
- CI mora potvrditi Android/JVM, Swift save i physics testove, izvorni audit, Android emulator i iOS simulator/build. Testiranje fizičkih uređaja i potpisane trgovinske instalacije i dalje su zasebni uvjeti.
- Android CI: Pixel Launcher ANR smije se oporaviti samo nakon provjere naslova, sistemskog ID-a gumba Wait i koordinata; zatim treba ponovno ispisati stvarni UI te pronaći sve premium kartice. Ako ScrollView ostane nedostupan ili BOPAVI padne, test mora pasti.

## v0.1.31 — marathon coordinate precision, Android/iOS parity
- Android and iOS obstacle screen positions are computed in Double before converting to Float, preserving collisions and pre-rendered next-level gates far beyond 2^25 world pixels.
- Run Kotlin/JVM unit tests, Kotlin/Swift generator parity, Android 35/36 emulator smoke and iOS simulator launch. Verify no reset of bird position, score, progress, scenery or camera distance at a world boundary.
- Verify `versionCode 34` is unused in Play Console before uploading any signed AAB; do not use disposable CI signing credentials.
- The AAB structure verifier fails when any unreviewed `.so` library is included. Reassess native library alignment before enabling a dependency that adds one.
- Inspect device GPU animation and background parallax in long sessions: display distance remains Float for visuals; this cycle specifically corrects collision coordinates.

## v0.1.30 — Google Play / Android 16
- Provjeriti manifest i bundle: com.brendigo.bopavi, minSdk26, targetSdk36, versionCode31. Android edge-to-edge, sigurni inseti, statusne/navigacijske trake, prediktivna gesta Natrag na Androidu 13-16; aktivni let mora se pauzirati, ne napustiti.
- Javno izdanje ima nepotpisani AAB. Samo zaštićen, ručno pokrenut Google Play signing workflow može proizvoditi potpisani AAB s privatnim upload ključem, bez objave tajni.
- Provjeriti stvarnu Play 512x512 ikonu, 1024x500 grafiku te originalne emulator screenshotove izrezane na 1080x1920 bez novih/nacrtanih sučelja. Screenshot mirujuće igre nije aktivni gameplay.
- Potvrditi Play Console Data safety, javni HTTPS privacy policy, ciljani uzrast i sadržaj, stvarni test na fizičkom telefonu i Play pre-launch report. Za primjenjivi novi osobni račun 12 testera tijekom 14 dana.
- Pratiti detaljni checklist: docs/play/RELEASE-CHECKLIST.md.
