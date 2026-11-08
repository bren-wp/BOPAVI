# Next Android release

## v1.0.69 — Splash i Home: bolji izvorni kadar i 3D grafika (build 199)

- Splash: izvorni krilati BOPAVI logotip sada se prikazuje u okviru 792 × 330, na koordinatama (75,76) originalne reference 941 × 1672. Točno odgovara prirodnom omjeru izvora 780 × 325, umjesto vertikalnog izobličenja.
- Home: logotip povećan i podignut u okvir 798 × 351 na (72,72). Kontrola preklapanja uzima u obzir stvarnu alfa-siluetu, a ne prozirne rubove PNG-a.
- Home: uz standardni gornji HUD logotip se više ne smanjuje bez potrebe. Ponašanje na telefonima s izrezom kamere ostaje adaptivno.
- Home: broj stvarno osvojenih zvjezdica prikazuje zlatnu ikonu zvjezdice; ne prikazuje nepostojeću valutu dragulja ni lažne brojke s reference.
- Splash i Home: usklađeniji crveni šal (bez trokutastog klina preko prsa), približena druga naočala i zadržana animirana prava krila glavnog lika. Gameplay i fizički hitboxovi nisu mijenjani.
- Splash: referentni QA Loading sada koristi immersive prikaz bez donje Android navigacijske crte. Dostupnost sistemskih kontrola gestom je sačuvana.
- Oba zaslona: dopunjena su lokalno generirana pozadinska obilježja visokog otočnog dvorca i metalnog zračnog broda. Nema novog preuzimanja iz mreže ni lažnih kontrola.
- Izmjene su ograničene na Splash/Home, offline izradu zajedničke pozadine i njihove regresijske testove. Sve akcije ostaju stvarni, dostupni Android gumbi; Loading ne prikazuje izmišljeni postotak.
- Referenca: 941 × 1672; posljednja prethodno verificirana v1.0.68 odstupanja bila su Home 24,02% i Splash 24,66% RGB MAE. To je početna mjera, a NE novo mjerenje niti 0% podudarnosti.
- Produkcijska objava dopuštena je tek nakon uspješnog Android CI-a, stvarnih snimki i testiranja na Androidu 15/16. Play upload AAB zahtijeva potpis izdavatelja.
