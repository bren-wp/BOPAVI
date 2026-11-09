# BOPAVI — Published stable releases

Only two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).
All previous versions remain at https://github.com/bren-wp/BOPAVI/releases.

## v1.0.71 — Level Complete: vjerodostojna nagradna ploča (build 201)

**Published:** 2026-11-09 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.71)

- **Level Complete:** referentna nagradna ploča sada ima dvije velike ikone i dinamičke brojke koje su stvarno zabilježene tijekom završene razine: novčiće i ocjenu zvjezdicama. Povećana škrinja smještena je uz njih unutar zajedničke krem ploče umjesto ispod tri samostalne kartice.
- **Stvarno stanje:** nema izmišljene gem valute, naplate niti lažnog otključavanja. Score ostaje u opisu pristupačnosti bez dodatne vizualne kartice koja nije na vlasnikovoj referenci. Riječi LEVEL COMPLETE! i broj razine imaju pristupačan puni opis.
- **UI:** uklonjen je dodatni vidljivi natpis LEVEL n koji nema ekvivalent u vlasnikovoj ilustraciji. Akcije NEXT LEVEL / LEVEL MAP, HOME i SHARE ostaju pravi Android gumbi s postojećim funkcijama.
- **Priprema:** ovu promjenu treba usporediti s izvornim PNG-om Level Complete na stvarnoj Android snimci 941 × 1672. Takav QA par još nije dostupan; tvrdnja o 0% odstupanja ili o dovršetku svih devet zaslona nije opravdana.
- **Distribucija:** build 201 se ne smije javno izdati bez uspješne potpune Android 15/16 CI provjere. GitHub release AAB i release APK su nepotpisani; Play predaja zahtijeva postojeći izdavateljev upload ključ. Prijašnja verzija v1.0.70 ne smije se preskočiti u dokumentaciji ako još čeka objavu.

## v1.0.69 — Splash i Home: bolji izvorni kadar i 3D grafika (build 199)

**Published:** 2026-11-08 · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/v1.0.69)

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
