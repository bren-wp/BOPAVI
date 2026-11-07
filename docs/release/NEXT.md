# Next Android release

## v1.0.62 — Shop, karta razina i Gameplay: tri vlasničke reference (build 192)

### Shop — 941 × 1672
- Izvorni offline Shop dobiva zaseban Native Canvas sloj: drvena trgovina, široka crveno-bijela tenda, pozlaćeni natpis, dvije svjetiljke, pult i police te ljubičasti odsjaji istaknute ponude.
- Šest kartica zadržava stvarne postojeće nadogradnje, cijene, inventar i mogućnost kupnje; promijenjen je samo prikaz naziva, ikonica, opisa i zelenih gumba.
- Gornje kapsule prikazuju stvarno spremljene novčiće i zarađene zvjezdice. Ljubičasta ikona **ne znači** da je uvedena lažna valuta dragulja, vanjska plaćanja ili lažne ponude.
- Istaknuta nadogradnja i dalje je stvarni proizvod povezan s postojećom transakcijom. Promotivna vizualna kartica ne glumi novi nenabavljivi skin.

### Level Map — 941 × 1672
- Ispravljena stvarna Canvas pogreška zbog koje je drugi prolaz staze resetirao Paint u FILL i crtao ogromni bež trokut preko mape. Oba sloja staze sada se crtaju isključivo kao STROKE.
- Slojeviti travnati rubovi i svijetli slapovi na tri ploveća otoka; razmještaj stvarnih čvorova, zaključavanja i spremljenih zvjezdica ostaje nepromijenjen.
- Gornji dio usklađen je s referencom: jedan glavni Back, dvije stvarne ikonične kapsule; dodatno listanje svjetova dostupno je rubnim kontrolama niže na zaslonu.

### Gameplay — 941 × 1672
- Svijet Sky Meadows koristi osvijetljeni pješčenjak i tamnije kamene konture za stvarne stupove.
- Prije prvog dodira vidljivi su blagi bijelo-plavi tragovi vjetra iza originalnog Bopija, kao na referenci; oni nisu sudari, stavke ni dodatna fizika.
- Originalni položaj pilota, stvarni osamnaest-pikselni kolizijski krug, prikupljanje, prvi dodir, sigurni razmaci između prepreka i HUD ostaju funkcionalni.

### QA i isporuka
- Izvorne referentne datoteke nisu zalijepljene preko interaktivnih elemenata. Svi ključni elementi ostaju izvorni Android Views/Canvas i rade bez interneta.
- Mjerljiva nulta razlika piksela nije potvrđena. Stvarni snimci iz emulatora provjeravaju se zasebno; ovo izdanje ne smije tvrditi 1:1 bez dokaza.
- Debug APK je za provjeru; Release APK i AAB su nepotpisani i zahtijevaju privatno potpisivanje odgovarajućim ključem prije objave na Google Playu.
