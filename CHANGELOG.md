# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.76 — Shop, Skins i Settings: 1:1 vizualni QA (build 206)

**Published:** 2026-11-11 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.76)

- **Skins:** niskorezolucijski atlas 4 × 76 × 77 px zamijenjen je postojećim lokalnim 512 × 512 px ilustracijama svih četiriju junaka. Središnji leteći Bopi smanjen je prema referentnom položaju iznad kamene platforme; strelice, stvarni status odabira/zaključavanja i EQUIP/UNLOCK ostaju Android kontrole.
- **Shop:** uklonjen je rastegnuti 3 KB raster cijelog kioska; ostaju nativno crtana drvena prodavaonica i postojeća lokalna 512 px ilustracija junaka. Stvarne offline cijene, zarada, kupljeni predmeti i šest interaktivnih kartica nisu promijenjeni.
- **Settings:** broj verzije prikazuje stvarnu instaliranu verziju aplikacije u podnožju reference i responzivnom prikazu. Stvarne spremljene postavke, pravni dokumenti i njihove funkcionalne veze nisu promijenjeni.
- **Provjere:** dodatni Shop/Skins/Settings source-contract testovi, Android lint/JVM, APK/AAB, emulator i stroga usporedba originalnih vlasničkih referenci 941 × 1672 px sa stvarnim snimkama bez skaliranja.
- **Vizualna iskrenost:** 0 % RGB odstupanja **nije potvrđeno**. Stara osnovna mjerenja: Shop 24,41 %, Skins 33,04 %, Settings 32,29 % (RGB MAE/255, prethodni Android build). Nakon nove emulator snimke ponovno izmjeriti svaki ekran i regiju, bez izmišljanja rezultata.
- **Funkcionalnost:** nema izmišljene kupovine dragulja, cloud servisa, obavijesti ili IAP obnove. Nema screenshotova koji glume interaktivne elemente.
- **Distribucija:** debug APK za QA; produkcijski APK i AAB su nepotpisani te zahtijevaju postojeći privatni ključ. Ne objavljivati i ne spajati prije prolaska QA.

## v1.0.75 — Shop / Skins i izravni ulaz u Home (build 205)

**Published:** 2026-11-10 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.75)

- **Pokretanje:** uklonjen je 3,5-sekundni prilagođeni startup/splash prikaz. Launcher Activity sada odmah učitava stvarni Home. Uklonjeni su stari launch theme i njegova slikovna podloga. Androidov sistemski splash na API 31+ može se kratko prikazati po pravilima sustava; aplikacija ne dodaje vlastito čekanje.
- **Shop:** cijena i ikona novčića sada se iscrtavaju na stvarnom Android Buttonu, umjesto kroz dekorativni sloj koji je nestajao na snimkama. Zadržani su stvarni troškovi i atomarne transakcije iz ProgressStore. Povećana je širina grafike proizvoda i približene dimenzije Shop naslova referenci.
- **Skins:** povezana plava naslovna traka, zasebne kapsule sa stvarnim novčićima i zvjezdicama, dva aktivna + gumba koja otvaraju stvarni Shop. Četiri stvarne kartice likova imaju čitljiviji naslov i veći prostor za info panel / EQUIP.
- **Distribucija:** galerija za novo izdanje sadrži 15 stvarno snimljenih Android ekrana, bez splash prikaza. Potrebna je provjera svih objavljenih snimaka, SHA-256 i novih APK/AAB paketa.
- **Vizualna istinitost:** izvorne reference 941×1672 ostaju strogi cilj, ali **nema potvrđene 1:1 podudarnosti** dok nove stvarne emulator snimke ne prođu usporedbu. Ne dodavati lažne dragulje, izmišljene kupnje ili statične snimke kao funkcionalne kontrole.
- **Potpisivanje:** standardni release APK i AAB su nepotpisani i zahtijevaju postojeći privatni ključ za produkcijsku distribuciju.
