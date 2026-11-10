# Next Android release

## v1.0.75 — Shop / Skins i izravni ulaz u Home (build 205)

- **Pokretanje:** uklonjen je 3,5-sekundni prilagođeni startup/splash prikaz. Launcher Activity sada odmah učitava stvarni Home. Uklonjeni su stari launch theme i njegova slikovna podloga. Androidov sistemski splash na API 31+ može se kratko prikazati po pravilima sustava; aplikacija ne dodaje vlastito čekanje.
- **Shop:** cijena i ikona novčića sada se iscrtavaju na stvarnom Android Buttonu, umjesto kroz dekorativni sloj koji je nestajao na snimkama. Zadržani su stvarni troškovi i atomarne transakcije iz ProgressStore. Povećana je širina grafike proizvoda i približene dimenzije Shop naslova referenci.
- **Skins:** povezana plava naslovna traka, zasebne kapsule sa stvarnim novčićima i zvjezdicama, dva aktivna + gumba koja otvaraju stvarni Shop. Četiri stvarne kartice likova imaju čitljiviji naslov i veći prostor za info panel / EQUIP.
- **Distribucija:** galerija za novo izdanje sadrži 15 stvarno snimljenih Android ekrana, bez splash prikaza. Potrebna je provjera svih objavljenih snimaka, SHA-256 i novih APK/AAB paketa.
- **Vizualna istinitost:** izvorne reference 941×1672 ostaju strogi cilj, ali **nema potvrđene 1:1 podudarnosti** dok nove stvarne emulator snimke ne prođu usporedbu. Ne dodavati lažne dragulje, izmišljene kupnje ili statične snimke kao funkcionalne kontrole.
- **Potpisivanje:** standardni release APK i AAB su nepotpisani i zahtijevaju postojeći privatni ključ za produkcijsku distribuciju.
