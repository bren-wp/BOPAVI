# Next Android release

## v1.0.60 — Splash, Home, Orao i Lisica (build 190)

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
- **Testiranje:** izrada grafike, automatski testovi potpunih obrisa B/O/P/A, Android 15/16 emulatori, potpune slike bez obrezivanja i provjera svih 22 datoteka službenog izdanja.
- **0%:** tražena točna podudarnost ostaje cilj, nije unaprijed potvrđena. Stvarne RGB razlike moraju se izmjeriti na Android snimkama.
- **Paketi:** debug APK, nepotpisani release APK i AAB, SHA-256 datoteka i stvarne snimke. Ključ za produkcijsko potpisivanje nije ugrađen.

**Vizualni opseg:** Splash, Home i novi izgled likova Orao/Lisica; gameplay fizika i spremanje napretka ostaju neizmijenjeni.

- **Rubni slučaj HUD-a:** novi izračun čuva barem 62% čitljive veličine kompletnog logotipa i po potrebi ga pomiče ispod HUD-a; Kotlin testovi pokrivaju referentni, notched i ekstremni raspored.
