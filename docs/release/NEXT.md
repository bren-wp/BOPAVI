# BOPAVI — Next Android release

## v1.0.85 — New owner references, gameplay cleanup and code modularization (build 215)

- Reconcile the ten new BOPAVI owner mockups across five Android screen sizes (1080×1920, 1080×2280, 1080×2340, 1080×2400 and 1440×3040).
- Remove six small golden ring rewards per gate from both gameplay Canvas rendering and swept-collection physics, including source files and obsolete unit tests. Preserve existing legitimate gate coins, berries, crystals, and local balances.
- Split independent rendering/geometry classes out of large Kotlin files without changing public contracts, save-key migrations, animation resources or collision shapes.
- Audit abandoned screen references, developer-only code, dead symbols and binary asset dependencies; verify Android CI on API 35/36 and preserve three native Home actions (Shop, Skins, Settings).
- Use direct screenshot-to-reference comparisons only when authentic app captures and reference bytes are available at identical dimensions. No claim of 0% visual deviation until verified.
