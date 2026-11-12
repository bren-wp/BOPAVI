# Next Android release

## v1.0.81 — Napredak i Karta razina: poravnanje nativnih elemenata (build 211)

- **Karta razina / HOME:** uklonjen vizualni duplikat povratne strelice koji se vidio na stvarnoj Android 15 snimci iz v1.0.80. Nativna kontrola sada u jednom Viewu crta 3D plavi okvir i vektorsku strelicu, koristeći identičnu transformaciju i jednu pristupačnu dodirnu zonu.
- **Napredak:** stvarne kartice osam svjetova koriste mjerenu visinu umjesto fiksne prazne površine 192dp, čime više sadržaja stane na zaslon; boje, star-milestone, spremljeni rezultati i navigacija ostaju stvarni.
- **Pristupačnost:** kartice Napretka dobivaju Android ripple pri dodiru. Dugi brojevi završenih razina i opisi zvjezdica ne smiju se odrezati zbog povećanog fonta.
- **Regresije:** dodana je zasebna provjera točno jedne 3D HOME kontrole na mapi te emulatorska provjera jedinstvene nativne HOME radnje; postojeći Android 15/16 testovi ostaju uključeni.
- **Paketi:** novo GitHub izdanje uključuje Android APK, nepotpisane release APK/AAB datoteke, stvarne snimke zaslona i SHA-256 kontrolne vrijednosti.
- **Ograničenje:** 0 % RGB odstupanja od vlasnikovih referenci još nije potvrđeno. Potrebna je usporedba neizmijenjenih izvornih PNG referenci s novim emulator snimkama pri jednakom stanju igre.
