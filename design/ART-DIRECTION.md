# BOPAVI — referentna vizualna specifikacija

Priložene kompozitne reference služi kao vizualni cilj, ali nisu gotovi pojedinačni spriteovi ni dokaz da su postojeći zasloni identični. Ne kopirati tuđe oznake SkyHop ili Flybo, njihove trgovine stvarnim novcem, oglase ili online ljestvice. Originalni proizvod zadržava naziv BOPAVI i offline napredak.

## Obvezna kompozicija
- Splash: portret s plavim nebom do svih rubova, veliko narančasto-zlatno BOPAVI slovo s plavom 3D sjenom, Bopi s pilotskim naočalama i crvenim šalom, lebdeći travnati otoci, vodopadi i slojevi oblaka. Jedan logotip, bez ponavljanja.
- Početni ekran: ilustrirani Bopi, čitljiv logo, mali brojač osvojenih kovanica i jedan dominantni zeleni **IGRAJ**. Bez izdvojenih gumba za svjetove, trgovinu, likove, nagrade, postavke ili poseban način leta.
- Svjetovi: osam tematski različitih kartica i scena, **svi odmah dostupni** bez lokota na svijetu; pojedinačni leveli otključavaju se napredovanjem. Jasna navigacija bez prikaza ukupnog broja svih levela.
- Gameplay: Bopi s odvojenim krilima, jasne prepreke, predmeti i HUD, pouzdana kolizija. Kadriranje i sigurne zone moraju se usporediti na Android/iOS screenshotovima.
- Pauza, rezultat, završetak levela i poraz: tamnoplave kartice, čitljiv bijeli tekst, zeleni primarni i plavi sekundarni gumbi, dostupne zone dodira i bez zasjenjenja informacija.

## Kriterij vizualne provjere
Usporediti zasebne snimke stvarne aplikacije, po ekranima, na jednakom omjeru zaslona. Ručno ocijeniti anatomiju lika, logo, osvjetljenje, dubinu, raspored kontrola i odsutnost praznih rubova. CI provjera dimenzija grafike i uspješan build **nisu** potvrda 1:1 izgleda.

# BOPAVI — produkcijski vizualni smjer v0.5.0

- Lik Bopi: pilotske naočale, crveni šal, animirani zamasi krila, treptanje, nagib tijela i čestice leta.
- Osam svjetova: livade/plutajući otoci; plaža/valovi i palme; led/snježne pahulje; vulkan/lava; hram/stupovi; noć/mjesec i krijesnice; kristalna šuma/kristalne igle; svemir/planeti i orbite.
- Različiti predmeti za skupljanje: zvjezdice, školjke, pahulje, iskre, perje, mjesečev prah, kristali, zvjezdani prah.
- Sučelja: početni ekran samo BOPAVI, prikaz Bopija, stanje kovanica i IGRAJ; ostale radnje u pauzi i nakon pokušaja.
- Osjetljiva područja: sustavne sigurne zone, veličina dodirnih ciljeva >= 44 pt / 48 dp, portrait, vrlo uski zasloni putem pomičnih izbornika.
- Renderiranje: niskotroškovne putanje/gradijenti umjesto učestalog alociranja bitmapa; 30/60 FPS; bez interneta.

Vizualni prikazi u aplikaciji su implementirani putem Android Canvas i iOS Core Graphics. Reference nisu zamjena za testiranje stvarnih buildova niti za konačni profesionalni spritesheet i animacijske atlase.


## v0.1.19 — usklađivanje slike i stvarne igre
- Lebdeći otoci nisu samo dio statične pozadine: tri dubinska sloja pomiču se uz prijeđenu udaljenost leta, a pozadinski sjaj ima isti pad prozirnosti na Androidu i iOS-u.
- Natpisi tuđih brendova na referencama (Flybo, SkyBop, SkyHop) nisu identitet igre. Koristiti isključivo BOPAVI i Bopija.
- Svih osam svjetova odmah je otvoreno. Nikakve globalne izmišljene ljestvice ili kupnje za pravi novac iz referenci.

## v0.1.26 — novi originalni premium likovi
- Noa: safirno pilotsko odijelo, vizir, električno plava krila, zlatni znak zvijezde; izvor `docs/assets/noa.svg`.
- Any: ljubičasta kosa, zvjezdana tijara, ružičasto-ljubičasta krila i haljina; izvor `docs/assets/any.svg`.
- Krila su samostalne SVG grupe `wing-left` i `wing-right`; generator daje iste izvore tijela/krila na Androidu i iOS-u. Kolizijski radijus i fizika se ne mijenjaju. Premium oznaka znači otključavanje lokalnim kovanicama, bez kupnje stvarnim novcem ili prednosti u igri.
