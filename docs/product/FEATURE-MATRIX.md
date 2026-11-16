# BOPAVI — Current Android-only feature matrix

This file describes the reachable production UI of the **published** Android release v1.0.89 (build 219), not an unreleased candidate. Historical three-action navigation ended with ADR-0003; retained save formats and intentionally removed standalone pages are distinguished below.

## Published release evidence

~~~json
{
  "schema": 1,
  "baseline_sha": "c6eb1c18e8548cfdb5bb480296b33f1239a0ea27",
  "release": "v1.0.89",
  "publication_status": "published",
  "last_published_github_release": "v1.0.89",
  "build": 219,
  "scope_decision": "pending",
  "screens": [
    "home",
    "loading",
    "flight",
    "pause",
    "results",
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
      "status": "visible"
    },
    {
      "id": "tap_to_flap",
      "status": "visible"
    },
    {
      "id": "procedural_obstacles",
      "status": "visible"
    },
    {
      "id": "pause",
      "status": "visible"
    },
    {
      "id": "results",
      "status": "visible"
    },
    {
      "id": "score",
      "status": "visible"
    },
    {
      "id": "berries",
      "status": "visible"
    },
    {
      "id": "local_best",
      "status": "visible"
    },
    {
      "id": "retry",
      "status": "visible"
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
      "status": "removed"
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
    },
    {
      "id": "four_home_actions",
      "status": "visible"
    },
    {
      "id": "gameplay_start_from_home",
      "status": "visible"
    },
    {
      "id": "flight_loading",
      "status": "visible"
    },
    {
      "id": "game_over_screen",
      "status": "visible"
    }
  ],
  "home_navigation_decision": "approved-four-actions-play"
}
~~~

## Verified source-level production routes (device acceptance remains separate)

| Screen/state | Entry point | Available operations |
|---|---|---|
| Home | MainActivity.showHome | Four accessible actions: PLAY, SKINS, SHOP, SETTINGS; passive coin balance |
| Loading / Ready | MainActivity.startGame | Production PLAY calls the existing flight preparation pipeline |
| Flight | GameView / GameSimulation | Tap-to-flap, obstacles, coins and gameplay score |
| Pause / Game Over / Results | GameView and result handlers | Resume, Retry, Home, locally stored results |
| Skins | MainActivity.showSkins | Select and unlock pilots using saved coins |
| Shop | MainActivity.showShop | Six virtual upgrades, wallet and inventory |
| Settings | MainActivity.showSettingsScreen | Audio, controls, backups, reminders, language and legal notices |

Progress, Leaderboard, Records, Achievements and Level Map remain removed as standalone pages. Gameplay is **reachable from Home using PLAY**, not debug-only. Older local save data and historical JSON formats remain supported; their reliability needs independent testing.

**Visual evidence is not pixel-perfect certification.** Ten distinct owner design states have 40 original WebP reference files across four available resolutions; the additional indexed resolution remains optional. Native emulator PNGs, complete RGB comparisons, physical-device SAF recovery, accessibility, FPS and battery results are separate acceptance gates.
