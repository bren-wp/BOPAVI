# Screenshot-to-reference mapping for BOPAVI v1.0.85

The reference archive has **10 screens × 5 Android resolutions**. Actual
screenshot names are NOT assumed. A reviewer supplies `--capture-map` with
**all 50** explicit entries and genuine PNGs captured from the app.

Example mapping fragment:

```json
{
  "Android_16_9_1080x1920": {
    "01_Pocetni_zaslon.webp": "android-home-standard.png",
    "02_Gameplay_upute.webp": "android-flight-ready-standard.png",
    "03_Gameplay_let.webp": "android-flight-active-standard.png",
    "04_Gameplay_pauza.webp": "android-pause-standard.png",
    "05_Kraj_igre.webp": "android-game-over-a-standard.png",
    "06_Kraj_igre_varijanta.webp": "android-game-over-b-standard.png",
    "07_Skins.webp": "android-skins-standard.png",
    "08_Shop.webp": "android-shop-standard.png",
    "09_Settings.webp": "android-settings-standard.png",
    "10_Politika_privatnosti.webp": "android-privacy-standard.png"
  }
}
```

These screenshot names are examples, **not proof they have been captured**.
Missing capture, filename, viewport, or mismatching RGB pixel is a failure.
Never compare the two `Game Over` design options against a duplicated
single native capture. The real legal text must remain selectable.
If Android UI has dynamic coins or scores, use deterministic emulator
fixtures and verify they match the exact reference state; do not alter native
screenshot pixels.

```bash
python tools/compare_current_references.py \
  --archive "/path/to/BOPAVI_Svi_Zasloni_i_Gameplay_Android(1).zip" \
  --screenshots qa/screenshots \
  --capture-map qa/new-owner-capture-map.json \
  --json-out qa/new-owner-rgb-report.json
```

Only `all_exact: true` with `identical: 50` constitutes verified 0%
deviation across all supplied reference images. No such claim is made yet.
