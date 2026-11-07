# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.60 — Splash, Home, Orao i Lisica (build 190)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.60)

- **Orao:** lokalni SVG za stari sprite 9 precrtan je u smeđe-bijelog pilota s jantarno-smeđim očima, zasjenjenim perjem, kožnim remenom, dva tirkizno-plava zlatno obrubljena stakla i plavim šalom.
- **Lisica:** originalni lokalni SVG za sprite 10 sada prikazuje narančasto-bijelo krzno, pune visoke uši, velik bijeli vrh repa, nasmiješeno lice, crveni šal i zlatnu medalju; krila ostaju zasebni animirani slojevi.
- **Odabir likova:** korisnički naziv drugog pilota je **ORAO / SKY EAGLE**, a interni stari naziv SOVA, broj spritea 9 i sposobnost PilotAbility.SOVA ostaju zbog kompatibilnosti spremljenog napretka.
- **QA likova:** automatski audit transparentnih slojeva tijela, oba krila i punih portreta sada obuhvaća svih 11 skinova, uključujući likove 9 i 10.
- **README:** dodatna marketinška galerija dva pilota prikazuje lokalne SVG resurse; svih 16 stvarnih Android snimki ostaje klikabilno.
- **Puni natpis:** B, O, P i A više nisu jedan vektorski objekt s zajedničkim even-odd pravilom. Svako slovo ima zaseban zatvoreni obris, unutarnje otvore i samostalan 3D reljef. Time se sprječava gubitak obojenih dijelova na mjestima preklapanja.
- **Bez rezanja:** drvena podloga i listovi premješteni su unutar vidljivog SVG okvira. Na kompaktnom Homeu logotip se mjeri prema stvarnoj širini prozora, uključujući Android podijeljeni zaslon.
- **Gornji HUD:** na Homeu se stvarna donja granica coin/gem kapsula ponovno mjeri nakon Android safe-area i notch pomaka. Potpuni logotip ostaje ispod HUD-a uz proporcionalnu prilagodbu, bez rastezanja slova.
- **Animirani Splash:** početna animacija ostaje 3,5 sekundi (3500 ms), traka se osvježava približno 30 FPS, a plavi indeterminirani odsjaj prelazi veći dio trake. Nakon Splasha automatski se otvara Home; učitavanje razina nije vezano uz proizvoljan postotak.
- **Puna README galerija:** svih 16 originalnih PNG-ova s GitHub izdanja prikazuje se u jednom stupcu, s pregledima širine 270 px i klikom na originalnu datoteku.
- **Sve ostale funkcije:** zadržane su klikabilne kontrole, stvarni novčići i dragulji, pilotska animacija i stvarno učitavanje. Nijedan Gameplay zaslon ili fizika nisu mijenjani.
- **README:** marketinška galerija zadržava svih 16 stvarnih Android snimki. Prikazuje po jednu sliku u redu radi čitljivosti na mobitelima; puni PNG-ovi dolaze u izdanju.

## v1.0.59 — Splash i Home: bolji 3D detalji i galerija svih zaslona (build 189)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.59)

- Nastavljeno izravno na main: isključivo Splash i Home izgled; ostale zaslone i gameplay fiziku ne mijenjamo.
- Splash/Home: osvijetljeniji plavi gradijent neba te slojeviti oblačni vrtovi, zelenilo i sitno bijelo cvijeće u donjoj trećini pozadine; svi dekorativni slojevi ostaju ispod funkcionalnog Android sučelja.
- Splash/Home: B, O, P i A ponovno su četiri odvojene 3D konture s vlastitim tamnoplavim rubom i autentičnim izrezima; prethodna spojena silueta bila je nečitljiva.
- Splash/Home: novi vektorski obris BOPA izravno je rekonstruiran iz dostavljene 864px vlasničke reference, s mekim kubnim krivuljama i očuvanim otvorima slova; slojevi reljefa ostaju odvojeni i lokalni.
- Home: COINS i GEM sada su jedna zajednički poravnata gornja HUD traka i fizički izrez zaslona ne može spustiti samo jedan novčanik preko logotipa.
- Stabilnost: za Android 16 emulator prednost ima provjerena AOSP sistemska slika, čime se uklanja nepotrebno prvo pokretanje Google usluga koje je pratilo stvarni ANR u CI-u; testni prolaz i dalje je obavezan.
- Splash: krilati BOPAVI logotip s odvojenim plavim slovima V/i, zasebno prilagođen leteći Bopi, zlatni prstenovi, izvorna traka učitavanja s istinitim indeterminiranim stanjem i bolji položaj na visokim Android zaslonima.
- Home: zaobljena bijela tipografija s tanjim 3D rubom i manjim tamnim pomakom natpisa na glavnim gumbima.
- Home: prilagođene veličine 3D natpisa i ikonica Skins, Shop i Settings; postojeći Play, ostali gumbi, stvarna spremljena stanja i pristupačnost nastavljaju raditi.
- Novi README donosi opis igre i **16 stvarnih Android snimki**: Home, Splash, Skins, Shop, Settings, Achievements, Leaderboard, Level Map, Options, Records, Privacy, Terms, Flight Preview, Flight Active, Pause, Results.
- Objavljujemo stvarne PNG datoteke iz Android 15 emulatora, a ne konceptne slike. Nema skrivenog rasta broja valuta, zamjene interaktivnih zaslona snimkom ili fingiranja uspješne provjere.
- Ugrađen je strogi test stvarnog odstupanja od referenci na 864×1536. **0% odstupanja ostaje cilj, nije dokazano.** Pozadinska ilustracija, Bopi i 3D slojevi ostaju glavni vizualni nedostaci.
