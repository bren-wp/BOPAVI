# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.74 — Level Map / Skins: stjenovita dubina i precizniji QA (build 204)

**Published:** 2026-11-10 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.74)

- **Skins:** prethodni pravokutni podij zamijenjen je slojevitom kamenom platformom s nepravilnim 3D plohama, svijetlim travnatim rubom i zaobljenim vrhom. Sve četiri postojeće interaktivne kartice, stvarni spriteovi, odabir i valute ostaju netaknuti.
- **Level Map:** dodane su zasebne viseće loze i listovi uz reljef sva tri lebdeća otoka. Zaključane razine, zvjezdice, staze između čvorova unutar istog otoka i navigacija i dalje se oslanjaju na stvarni ProgressStore.
- **Gameplay:** zadržana je korisnikova izričita iznimka: nema donjih šiljaka prije prvog dodira ni tijekom leta. Stvarni viseći opasni objekti, prolazi, prstenovi i fizičke kolizije ostaju aktivni.
- **Vizualni QA:** devet referenci 941 × 1672 px provjerava se bez skaliranja. Alat sada može izvesti izvorne RGB difference PNG datoteke i odvojene toplinske karte za analizu. Toplinska karta ne mijenja izmjereni RGB-MAE.
- **Verifikacija:** potrebni su stvarni Android 15/16 screenshotovi i novi RGB rezultati. Nijedan ekran nije deklariran 1:1 odnosno 0% samo na temelju izvornog koda ili sintetskih testova.
- **Distribucija:** izdanja su Android-only. Debug APK namijenjen je testiranju, a release APK i AAB moraju se potpisati privatnim postojećim ključem prije Google Play distribucije.

## v1.0.73 — Odabir junaka / Karta razina (build 203)

**Published:** 2026-11-10 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.73)

- Skins: četiri jednako raspoređene, stvarne kartice umjesto prethodne tri, uz izvorni raspored vlasnikove reference 941×1672. Postojeći Bopi, Sova i Lisac ostaju sa starim ID-jevima, podacima o spremanju i pravim sposobnostima.
- Star Pilot: četvrti junak koristi **postojeći Orion** (cosmetic skin #8), čiji se pravi sprite generira u projektu. Prikazuje se zaključan dok taj skin nije kupljen prikupljenim novčićima. Otključavanje koristi postojeću cijenu (240), provjeru stanja, **zasebnu potvrdu korisnika** i trajno spremanje odabira. Nema in-app naplate, izmišljene gem valute ili prečaca preko spremljenog stanja.
- Skins: redizajnirane su kartice u četiri stupca, izbornik ima native touch kontrole te je uklonjen netočan dodatni natpis FREE • UNLOCKED ispod info panela.
- Level Map: vizualne korekcije natpisa i proceduralnog scenografskog sloja usklađuju oblik/platforme s referencom bez lažnog otključavanja ili promjene brojača zvjezdica.
- Testovi: provjeriti jednostruku kupnju, odbijanje nedovoljnih novčića, save ID kompatibilnost, oba smjera navigacije, četvrti sprite na Home/Flight/Result i rezolucije s urezima zaslona.
- Vizualno: cilj 1:1, ali ne izjavljuje se 0% prije stvarnih screenshotova i mjerenja. Baseline v1.0.72: Level Map 28,781% / Skins 27,885% RGB-MAE na originalnim 941×1672 kadrovima.
- Distribucija: objaviti tek nakon Android 15/16, testova i uspješne GitHub Actions verifikacije. APK za release i AAB su nepotpisani; Play ažuriranje treba postojeći upload ključ.
