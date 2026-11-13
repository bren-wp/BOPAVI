# BOPAVI — Current Android-only feature matrix

This file describes **reachable production UI** in the published APK. Legacy game data is retained without restoring deleted navigation.

## Published release evidence

~~~json
{
  "schema": 1,
  "baseline_sha": "c6eb1c18e8548cfdb5bb480296b33f1239a0ea27",
  "release": "v1.0.85",
  "publication_status": "published",
  "last_published_github_release": "v1.0.85",
  "build": 215,
  "scope_decision": "pending",
  "screens": [
    "home",
    "skins",
    "shop",
    "settings"
  ],
  "features": [
    {
      "id": "home",
      "status": "visible"
    },
    {
      "id": "flight",
      "status": "internal-legacy"
    },
    {
      "id": "tap_to_flap",
      "status": "internal-legacy"
    },
    {
      "id": "procedural_obstacles",
      "status": "internal-legacy"
    },
    {
      "id": "pause",
      "status": "internal-legacy"
    },
    {
      "id": "results",
      "status": "internal-legacy"
    },
    {
      "id": "score",
      "status": "internal-legacy"
    },
    {
      "id": "berries",
      "status": "internal-legacy"
    },
    {
      "id": "local_best",
      "status": "internal-legacy"
    },
    {
      "id": "retry",
      "status": "internal-legacy"
    },
    {
      "id": "offline",
      "status": "visible"
    },
    {
      "id": "no_ads",
      "status": "visible"
    },
    {
      "id": "no_account",
      "status": "visible"
    },
    {
      "id": "inline_pilot_selection",
      "status": "removed"
    },
    {
      "id": "quick_options_overlay",
      "status": "removed"
    },
    {
      "id": "local_records_overlay",
      "status": "removed"
    },
    {
      "id": "legacy_save_models",
      "status": "internal-legacy"
    },
    {
      "id": "skins_assets_nine",
      "status": "internal-legacy"
    },
    {
      "id": "world_assets_eight",
      "status": "internal-legacy"
    },
    {
      "id": "json_save_serializer",
      "status": "internal-legacy"
    },
    {
      "id": "virtual_wallet_save",
      "status": "visible"
    },
    {
      "id": "multiworld_premium_product",
      "status": "planned"
    },
    {
      "id": "skin_selection_screen",
      "status": "visible"
    },
    {
      "id": "world_selection_screen",
      "status": "removed"
    },
    {
      "id": "level_selection_screen",
      "status": "removed"
    },
    {
      "id": "shop_screen",
      "status": "visible"
    },
    {
      "id": "profile_screen",
      "status": "removed"
    },
    {
      "id": "settings_screen",
      "status": "visible"
    },
    {
      "id": "achievements_screen",
      "status": "removed"
    },
    {
      "id": "records_screen",
      "status": "removed"
    },
    {
      "id": "import_export_ui",
      "status": "removed"
    },
    {
      "id": "powerup_shop",
      "status": "visible"
    },
    {
      "id": "three_home_actions",
      "status": "visible"
    },
    {
      "id": "progress_screen",
      "status": "removed"
    },
    {
      "id": "leaderboard_screen",
      "status": "removed"
    },
    {
      "id": "level_map_screen",
      "status": "removed"
    }
  ],
  "home_navigation_decision": "approved-three-actions"
}
~~~

## Verified production screen routes

| Screen | Entry point | Available operations |
|---|---|---|
| Home | MainActivity.showHome | Exactly Shop, Skins, Settings; coin balance is passive |
| Skins | MainActivity.showSkins | Select and unlock pilots using local saved coins |
| Shop | MainActivity.showShop | Local virtual upgrades, wallet and bonuses |
| Settings | MainActivity.showSettingsScreen | Volume, controls, backup, reminders, language and legal notices |

Progress, Leaderboard, Records, Achievements and Level Map screens and their entry points are removed. New gameplay cannot be started from Home. The existing GameSimulation and save keys remain for backward compatibility and debug-only regression tests; they are not advertised as playable UI.

Real Android emulator screenshots do not by themselves certify pixel-perfect identity or physical-device stability. No third-party accounts, ads or Internet permission.
