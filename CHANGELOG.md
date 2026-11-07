# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.63 — Shop: stvarne vidljive cijene, ispravno slojevitost i izvorni kadar (build 193)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.63)

- Ispravljeno je stvarno prekrivanje FEATURED teksta i gumba visokim slojem ljubičaste pozadine. Naziv proizvoda, opis, ikona i žuti gumb ponovno se iscrtavaju iznad panela.
- Cijene svih šest nadogradnji i istaknute ponude prikazuju se zasebnim pasivnim oznakama iznad stvarnih Android gumba, umjesto da nestanu uz ikonicu kovanice. Klikovi i transakcije ostaju izvorni, dostupni i vezani uz lokalni inventar.
- Ispravljena je skrivena bijela strelica povratka: sada se nalazi iznad podignutog plavog gumba i ne presreće dodir.
- Storefront, maskota Bopi, krilati SHOP natpis, ponuda, police i šest kartica na referentnoj portretnoj rezoluciji koriste puni izvorni kadar 941 × 1672 px. Interaktivne kontrole i dalje imaju sigurni položaj kada ih presijeca stvarni izrez zaslona.
- Na kompaktnim zaslonima i pri povećanom fontu ostaje postojeći pregledni, pomični izvorni Android prikaz; nije postavljen plosnati screenshot preko kupovnih kontrola.
- README sadrži poveznice na stvarne snimke svih 16 zaslona iz posljednjeg stabilnog izdanja. Novo izdanje objavljuje se samo nakon provjere emulatora i pripreme 16 različitih stvarnih PNG snimaka.
- Ovo izdanje ne tvrdi 0 % razlike od priloženoga dizajna bez stvarne usporedbe RGB piksela. Golden Avi, dragulji i drugi proizvodi koji nisu podržani postojećim offline sustavom kupnje ne predstavljaju se kao lažne funkcionalnosti.

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
