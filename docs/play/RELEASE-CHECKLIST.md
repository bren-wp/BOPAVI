# BOPAVI — kontrolni popis objave za Google Play i App Store

NIJEDNA stavka nije unaprijed označena obavljenom. Svaki korak traži dokaz.

- [ ] PR za v0.1.80: obavezni Android/iOS buildovi, privatnost, Android emulator 15/16 i iOS simulator/device su uspješni i spojeni.
- [ ] GitHub Release v0.1.80 je stvarno objavljen na ispravnom SHA commitu, sa svih 14 javnih artefakata, uključujući SHA-256 manifest.
- [ ] Application ID com.brendigo.bopavi, versionCode 83, targetSdk 36.
- [ ] Verificiran Play Console račun i prava izdavača.
- [ ] Kreirana Play Console aplikacija BOPAVI i potvrđen besplatni/plaćeni status.
- [ ] Aktiviran Play App Signing i izrađen trajni lokalni upload-key.
- [ ] Siguran backup JKS-a i lozinki izvan GitHub repozitorija.
- [ ] Privatno, izvan GitHuba potpisan AAB vlastitim, već verificiranim upload ključem.
- [ ] Lokalno provjeren potpis i SHA-256 zbroj AAB-a prije Play Console uploada.
- [ ] Signed AAB prenesen u Play Console; nikad javni unsigned AAB.
- [ ] Store listing HR, originalna 512x512 ikona i 1024x500 feature grafika.
- [ ] Najmanje 2 stvarna screenshotova; za preporuke 3 screenshotova aktivne igre.
- [ ] Privacy policy javno objavljen i otvoren anonimnim preglednikom na stvarno verificiranom HTTPS URL-u; poveznica unesena u Play Console i App Store Connect.
- [ ] Potvrđena dostupnost Politike privatnosti i Uvjeta korištenja unutar Android i iOS Postavki.
- [ ] Pregledani uvjeti korištenja i politika privatnosti od odgovorne osobe / pravnog savjetnika prije distribucije.
- [ ] Data safety, Ads, App access, IARC Content rating, Target audience popunjeni.
- [ ] Provjerena sva pravila usmjerena djeci ako aplikacija stvarno cilja djecu.
- [ ] Fizički QA Android 8/12/13/15/16, Bopija, 8 svjetova, zvuk, pauza, Back.
- [ ] Spremljeni podaci, JSON import/export, virtualne kovanice i oprema provjereni.
- [ ] Interna Play distribucija i Pre-launch report bez blokirajućih problema.
- [ ] Ako je primjenjivo: najmanje 12 testera 14 dana, produkcijski pristup odobren.
- [ ] Play release predan na odobrenje i odobren; stranica radi i instalacija potvrđena.
- [ ] Nakon objave prati crash/ANR i povratne informacije u Play Console.

## Apple App Store — zasebna provjera

- [ ] Apple Developer članstvo, legal entity i prava izdavača potvrđeni.
- [ ] iOS Bundle ID `com.brendigo.bopavi` odgovara trajnom identitetu u App Store Connectu.
- [ ] Apple signing certificates/profiles i Apple distribucijski potpis pripremljeni privatno, bez učitavanja privatnih ključeva na GitHub.
- [ ] Arhivirani Release build testiran na stvarnom iPhoneu, a po potrebi i iPadu (projekt uključuje iPad target).
- [ ] Dovršeni App Privacy podaci, dobna ocjena i ciljana publika, podaci o podršci i potpuno dostupan Privacy Policy URL.
- [ ] Dostavljene stvarne snimke zaslona odgovarajućih iPhone/iPad uređaja, metapodaci, opis igre i licence.
- [ ] TestFlight i App Review provedeni, bez blokirajućih problema; objava potvrđena u App Store Connectu.

**Status:** nijedna vanjska radnja potpisivanja ili objave nije unaprijed označena uspješnom. Automatizirani GitHub Release ne podrazumijeva odobrenje trgovina.
