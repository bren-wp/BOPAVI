#!/usr/bin/env python3
"""Install owner-approved, layered Bopi art into both native asset catalogs.

Optional and offline: never download screenshots, scrape external art, or bake
HUD/buttons/coins into the sprite. Without an approved bundle existing art is
left untouched. A partially supplied or modified bundle fails the build.
"""
from __future__ import annotations

from hashlib import sha256
from io import BytesIO
from pathlib import Path
import json

from PIL import Image

ART_SIZE = (224, 192)
NATIVE_SIZE = (512, 512)
PARTS = ("body", "left", "right")


def _png(image: Image.Image) -> bytes:
    stream = BytesIO()
    image.save(stream, format="PNG", optimize=True)
    return stream.getvalue()


def install_reference_bopi(root: Path) -> bool:
    source = root / "art/reference-approved/bopi"
    if not source.exists():
        return False

    manifest_path = source / "manifest.json"
    if not manifest_path.is_file():
        raise ValueError("Reference Bopi folder is present without manifest.json")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if tuple(manifest.get("canvas_size", ())) != ART_SIZE:
        raise ValueError("Reference Bopi must use the approved 224x192 canvas")
    if manifest.get("layers") != [f"bopi0_{part}.webp" for part in PARTS]:
        raise ValueError("Reference Bopi manifest has unexpected layer names")
    source_images: dict[str, Image.Image] = {}
    for part in PARTS:
        filename = f"bopi0_{part}.webp"
        path = source / filename
        if not path.is_file():
            raise ValueError(f"Missing required reference Bopi layer: {filename}")
        if sha256(path.read_bytes()).hexdigest() != manifest.get(filename + "_sha256"):
            raise ValueError(f"Checksum mismatch in approved Bopi layer: {filename}")
        with Image.open(path) as decoded:
            if decoded.format != "WEBP" or decoded.size != ART_SIZE:
                raise ValueError(f"Reference Bopi layer has invalid format or size: {filename}")
            image = decoded.convert("RGBA")
        if not image.getchannel("A").getbbox():
            raise ValueError(f"Reference Bopi layer has no visible pixels: {filename}")
        source_images[part] = image

    # The artwork keeps its approved aspect ratio and shoulder coordinates;
    # the non-square master is letterboxed transparently, never stretched.
    native: dict[str, Image.Image] = {}
    for part in PARTS:
        image = source_images[part].resize((512, 439), Image.Resampling.LANCZOS)
        frame = Image.new("RGBA", NATIVE_SIZE, (0, 0, 0, 0))
        frame.alpha_composite(image, (0, 36))
        native[part] = frame

    android = root / "android/app/src/main/res/drawable-nodpi"
    ios = root / "ios/BOPAVI/Assets.xcassets"
    for part, name, catalog in (
        ("body", "bopi0", "Bopi0"),
        ("left", "bopileft0", "BopiLeft0"),
        ("right", "bopiright0", "BopiRight0"),
    ):
        content = _png(native[part])
        target = android / f"{name}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
        folder = ios / f"{catalog}.imageset"
        folder.mkdir(parents=True, exist_ok=True)
        (folder / f"{name}.png").write_bytes(content)

    portrait = Image.alpha_composite(
        Image.alpha_composite(native["left"], native["right"]), native["body"]
    )
    bytes_portrait = _png(portrait)
    (android / "bopiportrait0.png").write_bytes(bytes_portrait)
    portrait_folder = ios / "BopiPortrait0.imageset"
    portrait_folder.mkdir(parents=True, exist_ok=True)
    (portrait_folder / "bopiportrait0.png").write_bytes(bytes_portrait)

    # Runtime marker is emitted only for an actual validated photo bundle.
    # The marker lets Android / iOS choose reference shoulder joints without
    # ever changing the collider, gameplay state or save-file schema.
    marker = _png(Image.new("RGBA", (2, 2), (255, 255, 255, 255)))
    (android / "bopireference0.png").write_bytes(marker)
    marker_folder = ios / "BopiReference0.imageset"
    marker_folder.mkdir(parents=True, exist_ok=True)
    (marker_folder / "bopireference0.png").write_bytes(marker)
    (marker_folder / "Contents.json").write_text(json.dumps({
        "images": [{"filename": "bopireference0.png", "idiom": "universal"}],
        "info": {"author": "xcode", "version": 1},
    }))
    print("PASS: approved reference Bopi body, moving wings and portrait share native pixels")
    return True


if __name__ == "__main__":
    install_reference_bopi(Path(__file__).resolve().parents[1])
