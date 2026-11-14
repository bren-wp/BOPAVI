# BOPAVI v1.0.87 — pending review; NOT released

## v1.0.87 — Owner Shop 3D footer and responsive frame (build 217)

- Baseline from 20 original v1.0.86 Android16 owner-viewport PNGs against the user's original 50 WebP designs (no resize; sampled 1 in 4 pixels each dimension): Shop RGB MAE 72.62/255; Skins 68.76; Settings 67.25; Home 67.14. These are substantial mismatches, not 1:1 fidelity.
- Screen-by-screen priority **Shop first**. Isolate wooden bottom 3D counter in a passive native Canvas layer anchored to the physical bottom on tall portrait viewports, while the 3×2 native inventory, actual offline coin balances and purchase actions remain centered and intact.
- Decorative plants/flowers follow the same full-height bottom anchor rather than leaving the wooden counter visually floating over the sky on 19:9 and 20:9 handsets.
- Preserve smaller-window native scrolling, accessibility minimum touch sizes and all existing gameplay/HUD/collision physics.
- This is not a screenshot-based UI replacement. All prices, product state and CTA actions are still real Android Views.
- Final release blocked pending full source/security, Android 15 and Android 16 emulator suites, and NEW post-change pixel comparisons of all owner viewports. 0% RGB difference is not yet confirmed. No Play-signed output.

- Skins: decoupled top hanging wooden title, hero, stone 3D podium, four real native cards and EQUIP panel into calibrated vertical cameras, rather than shifting all elements 154px upward together. Original owner tall art puts cards near y≈1350px on 1080×2400; the old shared .18 anchor put them around y≈1200px. On 16:9 the new anchors have no effect.
- Settings: owner tall art spreads nine preference rows progressively from ~30px at the top to ~150px at the last legal row and extends the decorative wood behind them. Preserved real switches, native SeekBars, accessible 48dp taps, compact/large-font scrolling and both legal links.
- Home: original animated 512px Bopi body+wing set uses a smaller 400×400 reference viewport centered near x=500, with a lower native vertical position. Projection puts painted sprite top near y≈676 on 1080×1920 and ≈840 on 1080×2400 while retaining the three real Shop/Skins/Settings buttons.

- Expanded real API36 owner-viewport capture suite from 20 to 25 authentic native screenshots by entering the **actual Privacy Policy** from Settings on each of five viewport sizes; verify its heading and real close button and preserve the ability to return to Settings/Home. A captured dialog is not presumed pixel-identical to the owner's privacy mockup.

- Added actual 02_Gameplay_upute, 03_Gameplay_let and 04_Gameplay_pauza captures on the same five reference device resolutions. This requires a real GameSimulation, first tap and PAUZA/Nastavi/Ponovo/Izlaz native dialog, with a strictly bounded retry only if the first input was genuinely lost. Brings real unscaled capture inventory to 40/50; both Game Over variants remain UNVERIFIED and are not falsely marked 1:1.

- Legal page: replaced the generic white Android AlertDialog with an accessible, responsive, wooden 3D native dialog, blue enamel heading, cream paper body and green close CTA. Preserved the **verbatim bundled legal HTML**, links, ScrollView and real close/background dismiss. No screenshot contains fake clickable legal content.
