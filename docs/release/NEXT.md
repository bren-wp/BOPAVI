# Next Android release

## v1.0.59 — Splash i Home: bolji 3D detalji i galerija svih zaslona (build 189)

**Čeka uspješno Android testiranje prije javne objave.**

- Nastavljeno izravno na main: isključivo Splash i Home izgled; ostale zaslone i gameplay fiziku ne mijenjamo.
- Splash/Home: osvijetljeniji plavi gradijent neba te slojeviti oblačni vrtovi, zelenilo i sitno bijelo cvijeće u donjoj trećini pozadine; svi dekorativni slojevi ostaju ispod funkcionalnog Android sučelja.
- Splash/Home: logotip BOPA dobio je četiri odvojena, zaobljena vektorska slova s pravim otvorima B/O/P/A, svjetlosne poteze i slojeve 3D reljefa; nativni logotip i dalje se crta iz lokalnih vektorskih resursa.
- Splash: krilati BOPAVI logotip s odvojenim plavim slovima V/i, zasebno prilagođen leteći Bopi, zlatni prstenovi, izvorna traka učitavanja s istinitim indeterminiranim stanjem i bolji položaj na visokim Android zaslonima.
- Home: zaobljena bijela tipografija s tanjim 3D rubom i manjim tamnim pomakom natpisa na glavnim gumbima.
- Home: prilagođene veličine 3D natpisa i ikonica Skins, Shop i Settings; postojeći Play, ostali gumbi, stvarna spremljena stanja i pristupačnost nastavljaju raditi.
- Novi README donosi opis igre i **16 stvarnih Android snimki**: Home, Splash, Skins, Shop, Settings, Achievements, Leaderboard, Level Map, Options, Records, Privacy, Terms, Flight Preview, Flight Active, Pause, Results.
- Objavljujemo stvarne PNG datoteke iz Android 15 emulatora, a ne konceptne slike. Nema skrivenog rasta broja valuta, zamjene interaktivnih zaslona snimkom ili fingiranja uspješne provjere.
- Ugrađen je strogi test stvarnog odstupanja od referenci na 864×1536. **0% odstupanja ostaje cilj, nije dokazano.** Pozadinska ilustracija, Bopi i 3D slojevi ostaju glavni vizualni nedostaci.
- APK za debug namijenjen je provjeri; release APK/AAB su nepotpisani i treba ih potpisati postojećim odgovarajućim ključem prije objave ili proizvodne nadogradnje.

**Obavezno:** lint, Android unit testovi, Android 15/16 emulatori, 16 različitih stvarnih snimki i provjera svih datoteka izdanja.
