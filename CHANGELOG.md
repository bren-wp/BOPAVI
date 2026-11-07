# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.61 — Pending (build 191)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.61)

- First-world pillars now use carved warm sandstone, bevelled golden crown plaques, red crown banners, real anchored climbing leaves and daisies.
- Alternating metal cone caps end at the existing collision-solid edge: no visually dangerous, non-collidable protrusions and no change to the 18-unit physical Bopi/Sova/Lisac collider.
- In the first world, six curved gold rings per eligible pillar are now individually collectible in both finite and endless flight. The renderer and swept contact use the same world/viewport coordinates, and ring flags prevent duplicate rewards.
- Real finite central coin pickups remain unchanged. Additional royal rings increment true collected coins and live score rather than displaying fake, uncollectible bonus items.
- The first royal pillar enters the screen after the first genuine flap; sky-world floor spikes are removed to match the supplied pillar-based gameplay references (the separate forest biome retains its original ground decoration).
- Sky Meadows draws from the locally generated castle/waterfall background without baked gameplay controls or pilot artwork.
- During the game, Android navigation bars hide for a genuinely full-bleed viewport and return outside gameplay. Live lives are still available to accessibility even when the visual HUD uses the reference SCORE plaque.
- Active flight displays a live native SCORE plaque, actual offline saved coin balance, real collected crystals (not a purchasable gem currency) and a working on-screen Pause action. Both + controls request the real offline shop with an explicit leave-flight confirmation.
- Idle flight still hides obstacles until the actual first flap; the ready/tutorial state remains unchanged.
- Bopi, Sova and Lisac preserve independent animated wing layers, their original IDs and saved pilot selection; Sova and Lisac use a subtle forward flight perspective on their existing shaded 3D sprites.
- All active HUD controls are scaled through native safe insets across compact and tall Android layouts.
- Updated automated Kotlin/HUD geometry checks and Python source contracts; true 0% pixel difference must be assessed against a matching original reference plus actual device/emulator captures, not claimed from compilation.

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
