# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.65 — Shop i Game Over: prepoznatljivi 3D elementi (build 195)

**Published:** 2026-11-08 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.65)

- Shop: sve postojeće stvarne nadogradnje štita i magneta sada imaju različite slojevite 3D ilustracije za osnovnu, drugu i treću razinu proizvoda, umjesto ponavljanja identične ikonice.
- Shop: dodan zaseban dekorativni drveni rub pulta ispred Bopija, bez presretanja dodira ili utjecaja na kupnje.
- Game Over: novi drveno-kremasti okvir za tri retka rezultata; stvarne vrijednosti ostaju pristupačni Android tekstualni elementi.
- Game Over: osvojenim zvjezdicama pridružena je ispravna zlatna ikona zvjezdice, a ne ikona dragulja.
- Zadržane su postojeće cijene, inventar, šest stvarnih offline kupnji, RETRY/HOME navigacija i spremanje napretka bez migracije.
- Dodane su statičke provjere novih umjetničkih slojeva i razlike između stvarnih zvjezdica i neimplementiranog novčanika dragulja.
- Referentnih 941 × 1672 piksela cilj je usporedbe, ne potvrda postignutih 0 % odstupanja. Nedostaju proizvodi Extra Hearts, Coin Booster, Gem Pack i Random Skin, a Golden Avi nije zasebni kupivi skin. Njih se ne smije predstavljati kao stvarno dostupne kupnje dok ne postoje igraća logika i pohrana.
- Produkcijska objava dopuštena je samo nakon uspješnih JVM i Android CI testova te snimanja stvarnih APK ekrana. Izdani release APK i AAB nepotpisani su, a debug APK namijenjen je testiranju.

## v1.0.63 — Shop: stvarne vidljive cijene, ispravno slojevitost i izvorni kadar (build 193)

**Published:** 2026-11-07 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.63)

- Ispravljeno je stvarno prekrivanje FEATURED teksta i gumba visokim slojem ljubičaste pozadine. Naziv proizvoda, opis, ikona i žuti gumb ponovno se iscrtavaju iznad panela.
- Cijene svih šest nadogradnji i istaknute ponude prikazuju se zasebnim pasivnim oznakama iznad stvarnih Android gumba, umjesto da nestanu uz ikonicu kovanice. Klikovi i transakcije ostaju izvorni, dostupni i vezani uz lokalni inventar.
- Ispravljena je skrivena bijela strelica povratka: sada se nalazi iznad podignutog plavog gumba i ne presreće dodir.
- Storefront, maskota Bopi, krilati SHOP natpis, ponuda, police i šest kartica na referentnoj portretnoj rezoluciji koriste puni izvorni kadar 941 × 1672 px. Interaktivne kontrole i dalje imaju sigurni položaj kada ih presijeca stvarni izrez zaslona.
- Na kompaktnim zaslonima i pri povećanom fontu ostaje postojeći pregledni, pomični izvorni Android prikaz; nije postavljen plosnati screenshot preko kupovnih kontrola.
- README sadrži poveznice na stvarne snimke svih 16 zaslona iz posljednjeg stabilnog izdanja. Novo izdanje objavljuje se samo nakon provjere emulatora i pripreme 16 različitih stvarnih PNG snimaka.
- Ovo izdanje ne tvrdi 0 % razlike od priloženoga dizajna bez stvarne usporedbe RGB piksela. Golden Avi, dragulji i drugi proizvodi koji nisu podržani postojećim offline sustavom kupnje ne predstavljaju se kao lažne funkcionalnosti.
