# Next Android release

## v1.0.61 — Pending (build 191)

### Gameplay and three pilots
- First-world pillars now use carved warm sandstone, bevelled golden crown plaques, red crown banners, real anchored climbing leaves and daisies.
- Alternating metal cone caps end at the existing collision-solid edge: no visually dangerous, non-collidable protrusions and no change to the 18-unit physical Bopi/Sova/Lisac collider.
- Real finite coin pickups use hollow glowing golden 3D rings; no fake collection path, fabricated score or out-of-range hitbox.
- Active flight displays a live native SCORE plaque, actual offline saved coin balance, real collected crystals (not a purchasable gem currency) and a working on-screen Pause action. Both + controls request the real offline shop with an explicit leave-flight confirmation.
- Idle flight still hides obstacles until the actual first flap; the ready/tutorial state remains unchanged.
- Bopi, Sova and Lisac preserve independent animated wing layers, their original IDs and saved pilot selection; Sova and Lisac use a subtle forward flight perspective on their existing shaded 3D sprites.
- All active HUD controls are scaled through native safe insets across compact and tall Android layouts.
- Updated automated Kotlin/HUD geometry checks and Python source contracts; true 0% pixel difference must be assessed against a matching original reference plus actual device/emulator captures, not claimed from compilation.

### Distribution
- Publish fresh unsigned Android APK/AAB alongside a QA debug APK only after the full native release CI validates.
- Update the README gallery from real Android emulator screenshots; do not replace screenshots with fabricated renders.
