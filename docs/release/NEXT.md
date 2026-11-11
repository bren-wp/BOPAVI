# Next Android release

## v1.0.76 — Shop, Skins i Settings: 1:1 vizualni QA (build 206)

**Status:** U pripremi u PR-u #227. Nije objavljeno niti potvrđeno kao 1:1.

- **Skins:** niskorezolucijski atlas 4 × 76 × 77 px zamijenjen je postojećim lokalnim 512 × 512 px ilustracijama svih četiriju junaka. Središnji leteći Bopi smanjen je prema referentnom položaju iznad kamene platforme; strelice, stvarni status odabira/zaključavanja i EQUIP/UNLOCK ostaju Android kontrole.
- **Shop:** uklonjen je rastegnuti 3 KB raster cijelog kioska; ostaju nativno crtana drvena prodavaonica i postojeća lokalna 512 px ilustracija junaka. Stvarne offline cijene, zarada, kupljeni predmeti i šest interaktivnih kartica nisu promijenjeni.
- **Settings:** broj verzije prikazuje stvarnu instaliranu verziju aplikacije u podnožju reference i responzivnom prikazu. Stvarne spremljene postavke, pravni dokumenti i njihove funkcionalne veze nisu promijenjeni.
- **Provjere:** dodatni Shop/Skins/Settings source-contract testovi, Android lint/JVM, APK/AAB, emulator i stroga usporedba originalnih vlasničkih referenci 941 × 1672 px sa stvarnim snimkama bez skaliranja.
- **Vizualna iskrenost:** 0 % RGB odstupanja **nije potvrđeno**. Stara osnovna mjerenja: Shop 24,41 %, Skins 33,04 %, Settings 32,29 % (RGB MAE/255, prethodni Android build). Nakon nove emulator snimke ponovno izmjeriti svaki ekran i regiju, bez izmišljanja rezultata.
- **Funkcionalnost:** nema izmišljene kupovine dragulja, cloud servisa, obavijesti ili IAP obnove. Nema screenshotova koji glume interaktivne elemente.
- **Distribucija:** debug APK za QA; produkcijski APK i AAB su nepotpisani te zahtijevaju postojeći privatni ključ. Ne objavljivati i ne spajati prije prolaska QA.
