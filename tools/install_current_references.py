#!/usr/bin/env python3
"""Verify and install exact owner-supplied BOPAVI reference images, without re-encoding."""
import argparse
import csv
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
from zipfile import ZipFile, is_zipfile

from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"art/reference-current"

def read_index():
    manifest=json.loads((BASE/"manifest.json").read_text("utf-8"))
    with (BASE/"reference_index.tsv").open(newline="",encoding="utf-8") as fh:
        rows=list(csv.DictReader(fh,delimiter="\t"))
    assert len(rows)==manifest["webp_files"]==50
    expected=set()
    for record in rows:
        path=PurePosixPath(record["relative_path"])
        assert ".." not in path.parts and len(path.parts)==2 and str(path)==record["relative_path"]
        assert path.parts[0] in manifest["viewports"]
        assert path.parts[1] in manifest["screens"]
        assert (int(record["width"]),int(record["height"]))==tuple(
            manifest["viewports"][path.parts[0]])
        assert int(record["bytes"])>100000
        assert len(record["sha256"])==64
        assert str(path) not in expected
        expected.add(str(path))
    assert len(expected)==50
    return manifest,rows

def sha256(data):
    return hashlib.sha256(data).hexdigest()

def verify_image(data,record):
    assert len(data)==int(record["bytes"]),record["relative_path"]+" bytes changed"
    assert sha256(data)==record["sha256"],record["relative_path"]+" sha256 mismatch"
    with Image.open(io.BytesIO(data)) as im:
        assert im.format=="WEBP",record["relative_path"]+" is not WebP"
        assert im.size==(int(record["width"]),int(record["height"])),(
            record["relative_path"],im.size)
        im.verify()

def verify_archive(archive):
    manifest,rows=read_index()
    archive=Path(archive)
    assert is_zipfile(archive),f"Not a ZIP: {archive}"
    assert archive.stat().st_size==manifest["archive_size_bytes"]
    assert sha256(archive.read_bytes())==manifest["archive_sha256"],"Unknown/modified owner pack"
    prefix=manifest["root_directory"]+"/"
    with ZipFile(archive) as z:
        expected={prefix+r["relative_path"] for r in rows}
        expected.update(prefix+name for name in
                        ("PROCITAJ_ME.txt","manifest.json",
                         "BOPAVI_Pregled_Svih_Zaslona.png"))
        actual={e.filename for e in z.infolist() if not e.is_dir()}
        assert actual==expected,sorted(actual^expected)
        for e in z.infolist():
            p=PurePosixPath(e.filename)
            assert not e.filename.startswith("/") and ".." not in p.parts
            assert e.file_size<=4_000_000,"Oversized untrusted member"
            assert (e.external_attr>>16)&0o170000!=0o120000,"Symbolic link in ZIP"
        assert z.testzip() is None,"CRC error in reference archive"
        result={}
        for row in rows:
            raw=z.read(prefix+row["relative_path"])
            verify_image(raw,row)
            result[row["relative_path"]]=raw
    return result

def verify_installed(directory):
    _,rows=read_index()
    directory=Path(directory)
    for row in rows:
        p=directory/row["relative_path"]
        assert p.is_file(),f"Missing reference image: {p}"
        verify_image(p.read_bytes(),row)
    return len(rows)

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--archive",type=Path)
    mode=parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--verify",action="store_true")
    mode.add_argument("--install",action="store_true")
    mode.add_argument("--check-installed",action="store_true")
    parser.add_argument("--destination",type=Path,default=BASE/"images")
    args=parser.parse_args()
    if args.check_installed:
        count=verify_installed(args.destination)
        print(f"PASS: {count} installed exact owner references")
        return
    parser.error("--archive is required with --verify or --install") if args.archive is None else None
    files=verify_archive(args.archive)
    if args.install:
        for relative,raw in files.items():
            dest=args.destination/relative
            dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_bytes(raw)
        assert verify_installed(args.destination)==50
    print(f"PASS: {len(files)} exact owner WebPs; "+(
        "installed unchanged" if args.install else "zip verified, no changes"))

if __name__=="__main__":
    main()
