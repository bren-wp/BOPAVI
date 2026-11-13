# BOPAVI — current owner references (v1.0.85)

**Canonical design source:** the owner's ZIP `BOPAVI_Svi_Zasloni_i_Gameplay_Android(1).zip`.
Original archive SHA-256: `0df582d495744b6c169c653a277a426c78f686dc240932fb4f931695aca1bb8c` (33,958,345 bytes).

This version supersedes historical **full-screen pixel baselines**, not the original
Bopi sprite/body/wing source artwork, Android APK assets or immutable releases.
The ten designs are provided for five pixel sizes, for **50 exact WebP assets**.
The package also has a contact sheet, `PROCITAJ_ME.txt` and its upstream manifest.
They are STATIC DESIGN MOCKUPS, **not** native screenshots. Real HTML or Android
controls must not be replaced with baked-in bitmap buttons or legal text.

## Screens
Home, Gameplay start, active flight, Pause, Game Over variant A, Game Over
variant B, Skins, Shop, Settings, Privacy Policy.

## Trust / import
`reference_index.tsv` pins **all 50 source bytes** by name, size, dimensions
and SHA-256, not compressed substitutes. The ZIP itself is checked against its
SHA-256, and each individual WebP is checked before installation.

```bash
python tools/install_current_references.py --archive "/path/to/BOPAVI_Svi_Zasloni_i_Gameplay_Android(1).zip" --verify
python tools/install_current_references.py --archive "/path/to/BOPAVI_Svi_Zasloni_i_Gameplay_Android(1).zip" --install
git add art/reference-current/images/
```

Do **not** assert zero pixel difference until the extracted binary references
actually exist in the repository and genuine installed-app screenshots have
been captured at the SAME physical resolution. Never stretch, crop or use
the mockup as a screenshot. `tools/compare_current_references.py` refuses
mismatched dimensions and missing screen captures.

**Upload status:** metadata, SHA-256, import and comparator tooling are versioned
in this PR. The original binary WebP files are not present in the GitHub branch
until physically transferred; marking an unverified reference as imported would
be incorrect.
