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
        title=header.group()
    else:
        # v1.0.28 shipped a historical short heading ('## v1.0.28 ')
        # but retained the immutable, authoritative '**Build 158 distribution:**'
        # footer. Do not modify a published GitHub release merely to fix docs.
        # Allow this strictly scoped format ONLY if both its exact tag and
        # numeric published build footer are present. A malformed descriptive
        # header or missing build information must still fail closed.
        legacy=re.search(r"(?m)^## (v\d+\.\d+\.\d+)[ \t]*$",body)
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
