# Next Android release

## v1.0.73 — Odabir junaka / Karta razina (build 203)

- Skins: četiri jednako raspoređene, stvarne kartice umjesto prethodne tri, uz izvorni raspored vlasnikove reference 941×1672. Postojeći Bopi, Sova i Lisac ostaju sa starim ID-jevima, podacima o spremanju i pravim sposobnostima.
- Star Pilot: četvrti junak koristi **postojeći Orion** (cosmetic skin #8), čiji se pravi sprite generira u projektu. Prikazuje se zaključan dok taj skin nije kupljen prikupljenim novčićima. Otključavanje koristi postojeću cijenu (240), provjeru stanja, **zasebnu potvrdu korisnika** i trajno spremanje odabira. Nema in-app naplate, izmišljene gem valute ili prečaca preko spremljenog stanja.
- Skins: redizajnirane su kartice u četiri stupca, izbornik ima native touch kontrole te je uklonjen netočan dodatni natpis FREE • UNLOCKED ispod info panela.
- Level Map: vizualne korekcije natpisa i proceduralnog scenografskog sloja usklađuju oblik/platforme s referencom bez lažnog otključavanja ili promjene brojača zvjezdica.
- Testovi: provjeriti jednostruku kupnju, odbijanje nedovoljnih novčića, save ID kompatibilnost, oba smjera navigacije, četvrti sprite na Home/Flight/Result i rezolucije s urezima zaslona.
- Vizualno: cilj 1:1, ali ne izjavljuje se 0% prije stvarnih screenshotova i mjerenja. Baseline v1.0.72: Level Map 28,781% / Skins 27,885% RGB-MAE na originalnim 941×1672 kadrovima.
- Distribucija: objaviti tek nakon Android 15/16, testova i uspješne GitHub Actions verifikacije. APK za release i AAB su nepotpisani; Play ažuriranje treba postojeći upload ključ.
