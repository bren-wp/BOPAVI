# Next Android release

## v1.0.77 — Shop, Skins i Settings: funkcionalne kontrole i sigurnost (build 207)

**Status:** U provjeri u PR-u #228; još nije objavljeno i nije potvrđeno 1:1.

- **Settings:** nativni redovi za Music, Sound Effects, Notifications, Language, Controls, Vibration, Cloud Save, Privacy Policy, Terms of Service i zasebna Restore Purchases tipka. Postojeće postavke zvuka, težine i animacije ostaju dostupne kroz prilagodljiv prikaz/Controls.
- **Obavijesti:** dobrovoljni lokalni podsjetnik, prema zadanim postavkama isključen; Android 13+ traži odgovarajuće dopuštenje. Nema push servisa ni skrivenog prikupljanja podataka.
- **Spremanje:** korisnik preko Android Storage Access Frameworka izričito odabire lokalni ili vanjski cloud dokument za JSON sigurnosnu kopiju; dopuštenja su ograničena odabranim dokumentom i opozivaju se pri isključivanju. Validacija veličine i formata prije uvoza.
- **Restore Purchases:** vraća samo prethodno spremljena lokalna otključavanja i napredak iz kompatibilnog JSON dokumenta, **ne** Google Play kupnje. Igra trenutačno nema Play Billing artikle.
- **Jezik:** pohrana odabira hrvatskog ili engleskog u postavkama; to ne znači da je sva igra lokalizirana.
- **Privatnost:** offline pravni tekstovi i javne kopije sinkronizirani s opcionalnim SAF spremanjem i lokalnim obavijestima.
- **Napredak:** zaseban nativni ekran s lokalno spremljenim statistikama svih osam svjetova; dovršene razine izvode se iz spremljenog frontiera, a do 60 zvjezdica označava isključivo prvih 20 razina svakog svijeta. Pristup preko postojećeg izbornika OPCIJE IGRE → NAPREDAK, bez dodavanja gumba na ilustriranu Home referencu.
- **Postignuća, Rekordi i Karta razina:** novi DPI-aware sigurnosni uvjeti; na zaslonima gdje bi 941px referentne kartice rezale tekst ili čvorovi bili manji od 48dp, koriste se pravi nativni skrolajući rasporedi. Dodani JUnit testovi za 320dpi i veće gustoće.
- **Level Complete / Game Over:** sačuvani stvarni rezultati, vizualna referentna kompozicija i nativne NEXT/RETRY/HOME/SHARE tipke; potrebna nova provjera stvarnih screenshotova.
- **QA:** stvarni 941×1672 emulator screenshot za novi Napredak, uz postojeće Shop, Skins, Settings, Achievements, Leaderboard, Level Map i rezultate. Za Progress i druge ekrane bez izvorne referentne slike ne izmišljati postotak identičnosti.
- **Usklađivanje referenci:** 3D dekor, nativne kontrole, pristupačni 48dp dodirni ciljevi i skrolajući prikaz na manjim/gušćim zaslonima. Grafika Shop/Skins/Settings i dalje se mora uskladiti s izvornim referencama 941×1672.
- **Provjere:** statičke regresije, Kotlin/JVM, Android lint, APK/AAB, Android 15/16 emulatori te stvarne usporedbe po regijama. **0 % RGB odstupanja nije potvrđeno.** Starija izmjerena baza (prethodni Android 16): Shop 23,3593 % i Skins 32,1376 % normaliziranog RGB-MAE; za ovu verziju potrebne su nove snimke.
- **Distribucija:** QA debug APK; release APK i Google Play AAB su nepotpisani. Ne objavljivati niti spajati u main prije prolaska svih provjera.
