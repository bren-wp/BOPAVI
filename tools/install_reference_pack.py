#!/usr/bin/env python3
"""Safely unpack an owner's approved Bopi-layer ZIP into this repository.

Run offline before opening a PR containing the *source* layers. This command
cannot fetch external assets, extract arbitrary paths or change native code.
"""
from __future__ import annotations

import argparse
import json
import shutil
import tempfile
import zipfile
from hashlib import sha256
from pathlib import Path

from PIL import Image

PREFIX="art/reference-approved/bopi/"
NAMES={"manifest.json", "README.txt",
       "bopi0_body.webp","bopi0_left.webp","bopi0_right.webp"}


def install_pack(archive:Path, root:Path)->None:
    root=root.resolve()
    archive=archive.resolve()
    if not archive.is_file() or not zipfile.is_zipfile(archive):
        raise ValueError("Provide a real BOPAVI reference layer ZIP")
    destination=root/PREFIX
    with zipfile.ZipFile(archive) as zipf:
        infos=zipf.infolist()
        entries={i.filename for i in infos}
        if entries != {PREFIX+name for name in NAMES} or len(infos)!=len(NAMES):
            raise ValueError("Reference art archive has extra, missing or unsafe paths")
        if any(i.is_dir() or i.file_size>2_000_000 for i in infos):
            raise ValueError("Reference art contains oversized or directory members")
        with tempfile.TemporaryDirectory() as tmp:
            staging=Path(tmp)
            for info in infos:
                # No zip.extract: do not follow arbitrary archive paths.
                name=info.filename[len(PREFIX):]
                target=staging/name
                with zipf.open(info) as source, target.open("wb") as output:
                    shutil.copyfileobj(source,output)
            manifest=json.loads((staging/"manifest.json").read_text())
            if manifest.get("canvas_size") != [224,192]:
                raise ValueError("Incorrect source canvas")
            if manifest.get("layers") != [
                "bopi0_body.webp","bopi0_left.webp","bopi0_right.webp"
            ]:
                raise ValueError("Unexpected layer definitions")
            for name in sorted(NAMES):
                if name.endswith(".webp"):
                    data=(staging/name).read_bytes()
                    if sha256(data).hexdigest()!=manifest.get(name+"_sha256"):
                        raise ValueError(f"Tampered layer in archive: {name}")
                    with Image.open(staging/name) as image:
                        if image.format!="WEBP" or image.size!=(224,192):
                            raise ValueError(f"Invalid Bopi image layer: {name}")
            destination.mkdir(parents=True,exist_ok=True)
            for name in sorted(NAMES):
                target=destination/name
                if target.exists() and target.read_bytes()!=(staging/name).read_bytes():
                    raise ValueError(f"Refusing to overwrite different source art: {name}")
            for name in sorted(NAMES):
                (destination/name).write_bytes((staging/name).read_bytes())
    print("PASS: owner-approved Bopi asset pack safely installed under",destination)


if __name__=="__main__":
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive",type=Path,help="BOPAVI-reference-bopi-layers-v0.2.1.zip")
    parser.add_argument("--root",type=Path,default=Path(__file__).resolve().parents[1])
    args=parser.parse_args()
    install_pack(args.archive,args.root)
