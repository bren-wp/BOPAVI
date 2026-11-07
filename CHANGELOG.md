# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.62 — Shop, karta razina i Gameplay: tri vlasničke reference (build 192)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.62)

- Izvorni offline Shop dobiva zaseban Native Canvas sloj: drvena trgovina, široka crveno-bijela tenda, pozlaćeni natpis, dvije svjetiljke, pult i police te ljubičasti odsjaji istaknute ponude.
- Šest kartica zadržava stvarne postojeće nadogradnje, cijene, inventar i mogućnost kupnje; promijenjen je samo prikaz naziva, ikonica, opisa i zelenih gumba.
- Gornje kapsule prikazuju stvarno spremljene novčiće i zarađene zvjezdice. Ljubičasta ikona **ne znači** da je uvedena lažna valuta dragulja, vanjska plaćanja ili lažne ponude.
- Istaknuta nadogradnja i dalje je stvarni proizvod povezan s postojećom transakcijom. Promotivna vizualna kartica ne glumi novi nenabavljivi skin.
- Ispravljena stvarna Canvas pogreška zbog koje je drugi prolaz staze resetirao Paint u FILL i crtao ogromni bež trokut preko mape. Oba sloja staze sada se crtaju isključivo kao STROKE.
- Slojeviti travnati rubovi i svijetli slapovi na tri ploveća otoka; razmještaj stvarnih čvorova, zaključavanja i spremljenih zvjezdica ostaje nepromijenjen.
- Gornji dio usklađen je s referencom: jedan glavni Back, dvije stvarne ikonične kapsule; dodatno listanje svjetova dostupno je rubnim kontrolama niže na zaslonu.
- Svijet Sky Meadows koristi osvijetljeni pješčenjak i tamnije kamene konture za stvarne stupove.
- Prije prvog dodira vidljivi su blagi bijelo-plavi tragovi vjetra iza originalnog Bopija, kao na referenci; oni nisu sudari, stavke ni dodatna fizika.
- Originalni položaj pilota, stvarni osamnaest-pikselni kolizijski krug, prikupljanje, prvi dodir, sigurni razmaci između prepreka i HUD ostaju funkcionalni.
- Izvorne referentne datoteke nisu zalijepljene preko interaktivnih elemenata. Svi ključni elementi ostaju izvorni Android Views/Canvas i rade bez interneta.
- Mjerljiva nulta razlika piksela nije potvrđena. Stvarni snimci iz emulatora provjeravaju se zasebno; ovo izdanje ne smije tvrditi 1:1 bez dokaza.

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
