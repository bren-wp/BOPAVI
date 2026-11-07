# Next Android release

## v1.0.60 — Cjeloviti BOPAVI logotip na Splashu i Homeu (build 190)

- **Puni natpis:** B, O, P i A više nisu jedan vektorski objekt s zajedničkim even-odd pravilom. Svako slovo ima zaseban zatvoreni obris, unutarnje otvore i samostalan 3D reljef. Time se sprječava gubitak obojenih dijelova na mjestima preklapanja.
- **Bez rezanja:** drvena podloga i listovi premješteni su unutar vidljivog SVG okvira. Na kompaktnom Homeu logotip se mjeri prema stvarnoj širini prozora, uključujući Android podijeljeni zaslon.
- **Gornji HUD:** na Homeu se stvarna donja granica coin/gem kapsula ponovno mjeri nakon Android safe-area i notch pomaka. Potpuni logotip ostaje ispod HUD-a uz proporcionalnu prilagodbu, bez rastezanja slova.
- **Animirani Splash:** početna animacija ostaje 3,5 sekundi (3500 ms), traka se osvježava približno 30 FPS, a plavi indeterminirani odsjaj prelazi veći dio trake. Nakon Splasha automatski se otvara Home; učitavanje razina nije vezano uz proizvoljan postotak.
- **Puna README galerija:** svih 16 originalnih PNG-ova s GitHub izdanja prikazuje se u 2 stupca, s većim pregledom širine 320 px i klikom na originalnu datoteku.
- **Sve ostale funkcije:** zadržane su klikabilne kontrole, stvarni novčići i dragulji, pilotska animacija i stvarno učitavanje. Nijedan Gameplay zaslon ili fizika nisu mijenjani.
- **README:** marketinška galerija zadržava svih 16 stvarnih Android snimki. Prikazuje po dvije slike u redu radi čitljivosti na mobitelima; puni PNG-ovi dolaze u izdanju.
- **Testiranje:** izrada grafike, automatski testovi potpunih obrisa B/O/P/A, Android 15/16 emulatori, potpune slike bez obrezivanja i provjera svih 22 datoteka službenog izdanja.
- **0%:** tražena točna podudarnost ostaje cilj, nije unaprijed potvrđena. Stvarne RGB razlike moraju se izmjeriti na Android snimkama.
- **Paketi:** debug APK, nepotpisani release APK i AAB, SHA-256 datoteka i stvarne snimke. Ključ za produkcijsko potpisivanje nije ugrađen.

**Vizualni opseg:** samo Splash i Home.
