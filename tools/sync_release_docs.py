#!/usr/bin/env python3
"""Update only latest two actually published stable BOPAVI releases."""
import argparse
import json
import re
from pathlib import Path
from release_metadata import release_metadata
STABLE=re.compile(r"v([1-9]\d*)\.(\d+)\.(\d+)")
HEADER=re.compile(r"(?m)^## v\d+\.\d+\.\d+\s+[—-]\s+[^\n]+$")
ACTIVE=("docs/product/CURRENT_SCREENS.md","docs/play/STORE-LISTING-hr-HR.md","docs/play/RELEASE-CHECKLIST.md")
def decode_pages(raw):
    result=[];decoder=json.JSONDecoder();pos=0
    while pos<len(raw):
        while pos<len(raw) and raw[pos].isspace():pos+=1
        if pos==len(raw):break
        page,pos=decoder.raw_decode(raw,pos)
        if not isinstance(page,list):raise ValueError("Release API must return arrays")
        result.extend(page)
    return result
def releases(items):
    kept=[];tags=set()
    for r in items:
        tag=str(r.get("tag_name",""))
        if not STABLE.fullmatch(tag) or r.get("draft") or r.get("prerelease") or not r.get("published_at"):continue
        if tag in tags:raise ValueError("Duplicate stable release: "+tag)
        tags.add(tag);kept.append(r)
    return sorted(kept,key=lambda r:tuple(map(int,STABLE.fullmatch(r["tag_name"]).groups())),reverse=True)
def section(r):
    tag=r["tag_name"];body=str(r.get("body") or "")
    header=HEADER.search(body)
    if header is not None and header.group().startswith("## "+tag+" ") and "(build " in header.group():
        # Current releases require their explicit versioned/build-labelled title.
        title=header.group().rstrip("\r")
    else:
        # v1.0.28 shipped a historical short heading ('## v1.0.28 ')
        # but retained the immutable, authoritative '**Build 158 distribution:**'
        # footer. Do not modify a published GitHub release merely to fix docs.
        # Allow this strictly scoped format ONLY if both its exact tag and
        # numeric published build footer are present. A malformed descriptive
        # header or missing build information must still fail closed.
        legacy=re.search(r"(?m)^## (v\d+\.\d+\.\d+)[ \t]*\r?$",body)
        footer=re.search(r"(?m)^\*\*Build ([1-9]\d*) distribution:\*\*",body)
        if legacy is None or legacy.group(1)!=tag or footer is None:
            raise ValueError("Invalid published release notes for "+tag)
        header=legacy
        title="## "+tag+" — Android (build "+footer.group(1)+")"
    notes=body[header.end():].split("**Build ",1)[0].splitlines()
    bullets=[line.strip() for line in notes if line.strip().startswith("- ")]
    if not bullets:raise ValueError("Release missing documented changes: "+tag)
    date=r["published_at"][:10]
    if not re.fullmatch(r"\d{4}-\d{2}-\d{2}",date):raise ValueError("Bad publication date")
    return title+"\n\n**Published:** "+date+" · [GitHub release](https://github.com/bren-wp/BOPAVI/releases/tag/"+tag+")\n\n"+"\n".join(bullets[:12])+"\n"
def render(items):
    if not items:raise ValueError("No stable release")
    intro="# BOPAVI — Published stable releases\n\nOnly two latest published stable versions are shown. Unreleased notes: [NEXT.md](docs/release/NEXT.md).\nAll previous versions remain at https://github.com/bren-wp/BOPAVI/releases.\n\n"
    return intro+"\n".join(section(r) for r in items[:2])

def three_action_version(tag):
    """Apply only to versions explicitly built with the three-action Home."""
    match=STABLE.fullmatch(tag)
    return match is not None and tuple(map(int,match.groups()))>=(1,0,84)

def three_action_catalog(catalog):
    """Describe reachable production UI, not debug-only gameplay or old saves."""
    catalog["screens"]=["home","skins","shop","settings"]
    catalog["home_navigation_decision"]="approved-three-actions"
    states={
        "home":"visible",
        "flight":"internal-legacy",
        "tap_to_flap":"internal-legacy",
        "procedural_obstacles":"internal-legacy",
        "pause":"internal-legacy",
        "results":"internal-legacy",
        "score":"internal-legacy",
        "berries":"internal-legacy",
        "local_best":"internal-legacy",
        "retry":"internal-legacy",
        "offline":"visible",
        "no_ads":"visible",
        "no_account":"visible",
        "inline_pilot_selection":"removed",
        "quick_options_overlay":"removed",
        "local_records_overlay":"removed",
        "legacy_save_models":"internal-legacy",
        "virtual_wallet_save":"visible",
        "skin_selection_screen":"visible",
        "shop_screen":"visible",
        "settings_screen":"visible",
        "powerup_shop":"visible",
        "achievements_screen":"removed",
        "records_screen":"removed",
        "level_selection_screen":"removed",
        "world_selection_screen":"removed",
    }
    for feature in catalog["features"]:
        if feature["id"] in states:
            feature["status"]=states[feature["id"]]
    existing={f["id"] for f in catalog["features"]}
    for name,state in (("three_home_actions","visible"),
                       ("progress_screen","removed"),
                       ("leaderboard_screen","removed"),
                       ("level_map_screen","removed")):
        if name not in existing:
            catalog["features"].append({"id":name,"status":state})
    return catalog

def three_action_documents(tag,build,catalog):
    """Generate truthful copy only AFTER a successful public stable release."""
    matrix_json=json.dumps(catalog,ensure_ascii=False,indent=2)
    matrix=(
        "# BOPAVI — Current Android-only feature matrix\n\n"
        "This file describes **reachable production UI** in the published APK. "
        "Legacy game data is retained without restoring deleted navigation.\n\n"
        "## Published release evidence\n\n~~~json\n"+matrix_json+"\n~~~\n\n"
        "## Verified production screen routes\n\n"
        "| Screen | Entry point | Available operations |\n"
        "|---|---|---|\n"
        "| Home | MainActivity.showHome | Exactly Shop, Skins, Settings; coin balance is passive |\n"
        "| Skins | MainActivity.showSkins | Select and unlock pilots using local saved coins |\n"
        "| Shop | MainActivity.showShop | Local virtual upgrades, wallet and bonuses |\n"
        "| Settings | MainActivity.showSettingsScreen | Volume, controls, backup, reminders, language and legal notices |\n\n"
        "Progress, Leaderboard, Records, Achievements and Level Map screens "
        "and their entry points are removed. New gameplay cannot be started from Home. "
        "The existing GameSimulation and save keys remain for backward compatibility "
        "and debug-only regression tests; they are not advertised as playable UI.\n\n"
        "Real Android emulator screenshots do not by themselves certify pixel-perfect "
        "identity or physical-device stability. No third-party accounts, ads or Internet permission.\n"
    )
    screens=(
        "# BOPAVI — Current Android screens\n\n"
        "**Published release baseline:** "+tag+" (build "+str(build)+").\n\n"
        "The production Home exposes **exactly three accessible actions**: "
        "**Shop, Skins and Settings**. Coins are displayed as a passive balance. "
        "No Play, Sound shortcut, Achievements, Progress, Leaderboard, "
        "Records or Level Map action remains.\n\n"
        "| Accessible screen | Genuine screenshot | Operation |\n"
        "|---|---|---|\n"
        "| Home | BOPAVI-Android-Home.png | Shop, Skins, Settings |\n"
        "| Skins | BOPAVI-Android-Skins.png | Pilot selection and local unlocks |\n"
        "| Shop | BOPAVI-Android-Shop.png | Offline perk inventory and virtual coins |\n"
        "| Settings | BOPAVI-Android-Settings.png | Real settings, privacy, terms and SAF backup |\n"
        "| Privacy (Settings dialog) | BOPAVI-Android-Privacy.png | Bundled offline privacy notice |\n"
        "| Terms (Settings dialog) | BOPAVI-Android-Terms.png | Bundled offline service terms |\n\n"
        "**Compatibility only:** flight simulation, pause and results still exist "
        "internally and can be exercised by debuggable-only Android QA. "
        "There is intentionally no production Home button for starting a flight. "
        "Stored progress, virtual coins and purchased skins are preserved on update.\n\n"
        "Original same-build emulator captures: https://github.com/bren-wp/BOPAVI/releases/tag/"
        +tag+". Physical-device QA and exact 0% RGB parity are not claimed.\n"
    )
    listing=(
        "# Google Play — BOPAVI (hr-HR) · objavljeno "+tag+
        " (build "+str(build)+")\n\n"
        "<!-- verified-feature-ids: home,three_home_actions,skin_selection_screen,shop_screen,settings_screen,virtual_wallet_save,offline,no_ads,no_account -->\n\n"
        "**Naziv (do 30 znakova):** BOPAVI\n\n"
        "**Kratki opis (do 80 znakova):** Bopiju prilagodi likove, nadogradnje i postavke, bez oglasa i prijave.\n\n"
        "**Puni opis:**\n\n"
        "BOPAVI prikazuje tri glavna izbornika: Shop, Skins i Settings. "
        "U Skins odaberi dostupnog lika, u Shopu pogledaj poboljšanja "
        "koja se otključavaju već spremljenim virtualnim novčićima, "
        "a u Settingsu prilagodi zvuk, kontrole, jezik i sigurnosne kopije.\n\n"
        "Napredak i virtualna sredstva spremljeni su lokalno i ostaju očuvani "
        "nakon ažuriranja. Nisu potrebni registracija, oglasi ni internetska veza. "
        "U postavkama su dostupni uvjeti korištenja i politika privatnosti.\n\n"
        "Zasloni napretka, poretka, rekorda, postignuća i karte razina uklonjeni su. "
        "U ovoj verziji nema dostupnog gumba za pokretanje novog leta s početnog zaslona. "
        "Prijašnji rezultat i spremljene bobice nisu izbrisani.\n\n"
        "**Kategorija:** Igra → Arkadne (potvrditi u Play Consoleu).\n\n"
        "**Oglasi:** Ne. **Kupnje u aplikaciji:** Ne. "
        "Shop koristi isključivo lokalne virtualne novčiće.\n\n"
        "## Autentične slike — ugovor\n\n"
        "Obvezno snimiti pravi Home, Shop, Skins i Settings iz istoga buil-da. "
        "Dodatne gameplay/rezultat snimke služe tehničkoj transparentnosti, "
        "ali nisu dokaz produkcijske navigacije. Izostaviti stare "
        "Achievements, Progress, Leaderboard, Level-Map, Options i Records snimke.\n\n"
        "GitHub izdanje: https://github.com/bren-wp/BOPAVI/releases/tag/"+tag+".\n"
        "Provjera na fizičkom uređaju i Play Store odobrenje nisu potvrđeni.\n"
    )
    checklist=(
        "# BOPAVI — Android release acceptance checklist\n\n"
        "**Last published release evidence:** "+tag+" (build "+str(build)+").\n"
        "**Application:** com.brendigo.bopavi · BRENDIGO · https://brendigo.com.\n\n"
        "## Production UI\n\n"
        "- [ ] Home has exactly three accessible controls: Shop, Skins, Settings.\n"
        "- [ ] No links, buttons, gestures or standalone screens for Progress, "
        "Leaderboard, Records, Achievements, Level Map or Play.\n"
        "- [ ] Shop virtual purchases and Skins ownership stay transactional and offline.\n"
        "- [ ] Settings privacy, legal controls, SAF backup, audio and accessibility work.\n"
        "- [ ] Run Android 15/16 emulator UI and image checks at multiple densities.\n"
        "- [ ] Check physical Android devices and TalkBack before claiming production parity.\n\n"
        "## Data, security and distribution\n\n"
        "- [ ] Preserve historical v1–v6 local save keys and virtual balances.\n"
        "- [ ] No INTERNET permission, ad SDKs or new account requirement.\n"
        "- [ ] Confirm 11 original emulator PNG captures, no removed-screen assets.\n"
        "- [ ] Check Gradle Android Lint, JVM tests, R8 APK/AAB, SHA-256 manifests.\n"
        "- [ ] Sign production distribution privately with publisher-approved key.\n"
        "- [ ] Check Play Console policy, accessibility, data safety and physical-device stability.\n"
        "- [ ] Never rewrite or delete older stable GitHub releases.\n"
    )
    return {
        "docs/product/FEATURE-MATRIX.md":matrix,
        "docs/product/CURRENT_SCREENS.md":screens,
        "docs/play/STORE-LISTING-hr-HR.md":listing,
        "docs/play/RELEASE-CHECKLIST.md":checklist,
    }

def sync(root,items,tag):
    public=releases(items)
    if not public or public[0]["tag_name"]!=tag:raise ValueError("Expected successful public stable release "+tag)
    version,build=release_metadata(root)
    if "v"+version!=tag or "(build "+str(build)+")" not in section(public[0]):
        raise ValueError("Published versionCode/versionName mismatch")
    changelog=render(public)
    path=root/"docs/product/FEATURE-MATRIX.md";original=path.read_text(encoding="utf-8")
    match=re.search(r"~~~json\s*(\{.*?\})\s*~~~",original,re.S)
    if not match:raise ValueError("Feature matrix missing")
    catalog=json.loads(match.group(1));previous=str(catalog["release"]);old_build=int(catalog["build"])
    catalog.update(release=tag,build=build,publication_status="published",last_published_github_release=tag)
    new_matrix=original[:match.start(1)]+json.dumps(catalog,ensure_ascii=False,indent=2)+original[match.end(1):]
    updates={"CHANGELOG.md":changelog,"docs/product/FEATURE-MATRIX.md":new_matrix}
    for name in ACTIVE:
        old=(root/name).read_text(encoding="utf-8")
        updates[name]=old.replace(previous,tag).replace("build "+str(old_build),"build "+str(build))
    if three_action_version(tag):
        # The previous published baseline has different routes. Replacing
        # version strings alone would falsely advertise deleted screens.
        catalog=three_action_catalog(catalog)
        updates.update(three_action_documents(tag,build,catalog))
    # The publication has finished: it is no longer an unreleased candidate.
    pending=(root/"docs/release/NEXT.md").read_text(encoding="utf-8")
    if re.search(rf"(?m)^## {re.escape(tag)}\s+[—-]",pending):
        updates["docs/release/NEXT.md"]=(
            "# Next Android release\n\nNo unreleased changes are staged.\n"
            "Increase the Android versionName/versionCode and review a new NEXT.md "
            "entry before publishing another release.\n"
        )
    modified=[]
    for name,value in updates.items():
        dest=root/name
        if dest.read_text(encoding="utf-8")!=value:dest.write_text(value,encoding="utf-8");modified.append(name)
    return modified
if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--releases",type=Path,required=True);p.add_argument("--expected-tag",required=True)
    args=p.parse_args()
    changed=sync(Path(__file__).resolve().parents[1],decode_pages(args.releases.read_text()),args.expected_tag)
    print("PASS: idempotent published-release documentation:",changed or "unchanged")
