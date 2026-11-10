# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.75 — Shop / Skins i izravni ulaz u Home (build 205)

**Published:** 2026-11-10 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.75)

- **Pokretanje:** uklonjen je 3,5-sekundni prilagođeni startup/splash prikaz. Launcher Activity sada odmah učitava stvarni Home. Uklonjeni su stari launch theme i njegova slikovna podloga. Androidov sistemski splash na API 31+ može se kratko prikazati po pravilima sustava; aplikacija ne dodaje vlastito čekanje.
- **Shop:** cijena i ikona novčića sada se iscrtavaju na stvarnom Android Buttonu, umjesto kroz dekorativni sloj koji je nestajao na snimkama. Zadržani su stvarni troškovi i atomarne transakcije iz ProgressStore. Povećana je širina grafike proizvoda i približene dimenzije Shop naslova referenci.
- **Skins:** povezana plava naslovna traka, zasebne kapsule sa stvarnim novčićima i zvjezdicama, dva aktivna + gumba koja otvaraju stvarni Shop. Četiri stvarne kartice likova imaju čitljiviji naslov i veći prostor za info panel / EQUIP.
- **Distribucija:** galerija za novo izdanje sadrži 15 stvarno snimljenih Android ekrana, bez splash prikaza. Potrebna je provjera svih objavljenih snimaka, SHA-256 i novih APK/AAB paketa.
- **Vizualna istinitost:** izvorne reference 941×1672 ostaju strogi cilj, ali **nema potvrđene 1:1 podudarnosti** dok nove stvarne emulator snimke ne prođu usporedbu. Ne dodavati lažne dragulje, izmišljene kupnje ili statične snimke kao funkcionalne kontrole.
- **Potpisivanje:** standardni release APK i AAB su nepotpisani i zahtijevaju postojeći privatni ključ za produkcijsku distribuciju.

## v1.0.74 — Level Map / Skins: stjenovita dubina i precizniji QA (build 204)

**Published:** 2026-11-10 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.74)

- **Skins:** prethodni pravokutni podij zamijenjen je slojevitom kamenom platformom s nepravilnim 3D plohama, svijetlim travnatim rubom i zaobljenim vrhom. Sve četiri postojeće interaktivne kartice, stvarni spriteovi, odabir i valute ostaju netaknuti.
- **Level Map:** dodane su zasebne viseće loze i listovi uz reljef sva tri lebdeća otoka. Zaključane razine, zvjezdice, staze između čvorova unutar istog otoka i navigacija i dalje se oslanjaju na stvarni ProgressStore.
- **Gameplay:** zadržana je korisnikova izričita iznimka: nema donjih šiljaka prije prvog dodira ni tijekom leta. Stvarni viseći opasni objekti, prolazi, prstenovi i fizičke kolizije ostaju aktivni.
- **Vizualni QA:** devet referenci 941 × 1672 px provjerava se bez skaliranja. Alat sada može izvesti izvorne RGB difference PNG datoteke i odvojene toplinske karte za analizu. Toplinska karta ne mijenja izmjereni RGB-MAE.
- **Verifikacija:** potrebni su stvarni Android 15/16 screenshotovi i novi RGB rezultati. Nijedan ekran nije deklariran 1:1 odnosno 0% samo na temelju izvornog koda ili sintetskih testova.
- **Distribucija:** izdanja su Android-only. Debug APK namijenjen je testiranju, a release APK i AAB moraju se potpisati privatnim postojećim ključem prije Google Play distribucije.
