# Next Android release

## v1.0.74 — Level Map / Skins: stjenovita dubina i precizniji QA (build 204)

- **Skins:** prethodni pravokutni podij zamijenjen je slojevitom kamenom platformom s nepravilnim 3D plohama, svijetlim travnatim rubom i zaobljenim vrhom. Sve četiri postojeće interaktivne kartice, stvarni spriteovi, odabir i valute ostaju netaknuti.
- **Level Map:** dodane su zasebne viseće loze i listovi uz reljef sva tri lebdeća otoka. Zaključane razine, zvjezdice, staze između čvorova unutar istog otoka i navigacija i dalje se oslanjaju na stvarni ProgressStore.
- **Gameplay:** zadržana je korisnikova izričita iznimka: nema donjih šiljaka prije prvog dodira ni tijekom leta. Stvarni viseći opasni objekti, prolazi, prstenovi i fizičke kolizije ostaju aktivni.
- **Vizualni QA:** devet referenci 941 × 1672 px provjerava se bez skaliranja. Alat sada može izvesti izvorne RGB difference PNG datoteke i odvojene toplinske karte za analizu. Toplinska karta ne mijenja izmjereni RGB-MAE.
- **Verifikacija:** potrebni su stvarni Android 15/16 screenshotovi i novi RGB rezultati. Nijedan ekran nije deklariran 1:1 odnosno 0% samo na temelju izvornog koda ili sintetskih testova.
- **Distribucija:** izdanja su Android-only. Debug APK namijenjen je testiranju, a release APK i AAB moraju se potpisati privatnim postojećim ključem prije Google Play distribucije.
