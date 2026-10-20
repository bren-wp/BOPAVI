# BOPAVI — procjena sigurnosti podataka za Google Play (v1.0.3 kandidat)

**Status:** izvorni kod pregledan; završna Play Console potvrda ostaje odgovornost izdavača.

| Područje | Stanje nativne Android aplikacije | Konačna provjera |
|---|---|---|
| Internetske dozvole i SDK-ovi | Bez INTERNET dopuštenja, bez oglasnog ili analitičkog SDK-a | Pregledati manifest i ovisnosti konačnog AAB-a |
| Podaci | Bodovi, bobice, osobni rekordi i povijesne postavke lokalno su spremljeni na uređaju | Provjeriti sve verzije koje se još distribuiraju na Playu |
| Račun ili prodaja | Nema registracije, oglasa ni kupnje unutar aplikacije | Potvrditi stvarni instalirani paket |
| Ručni uvoz/izvoz | Samo stari parser/serializer zbog kompatibilnosti podataka; nema korisnički dostupne tipke | Ne oglašavati ručni backup |
| Privatnost | U aplikaciji postoji hrvatski lokalni dijalog o privatnosti; javni HTML/Markdown dokument | Provjeriti javni HTTPS URL u anonimnom pregledniku |
| Brisanje | Sustavne Android postavke → BOPAVI → Pohrana → Obriši podatke, odnosno deinstalacija | Ne tvrditi da se time brišu ranije izvezene vanjske kopije |
| Ciljana dob i sadržaj | Jednostavna offline arkadna igra | Ispuniti stvarni Play IARC i ciljanu dob |

Google Play pravila: https://support.google.com/googleplay/android-developer/answer/18258653 i https://support.google.com/googleplay/android-developer/answer/9898842.

**Ne tvrdimo da je Play Console obrazac predan, da je produkcijski AAB potpisan ili da je trgovina odobrila aplikaciju.**  
Kontrolirati i postojeće objavljene verzije prije izjave „ne prikuplja podatke”.
