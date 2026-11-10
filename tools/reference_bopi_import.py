#!/usr/bin/env python3
"""Install owner-approved, layered Bopi art into Android resources.

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
    for part, name in (
        ("body", "bopi0"),
        ("left", "bopileft0"),
        ("right", "bopiright0"),
    ):
        content = _png(native[part])
        target = android / f"{name}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)

    portrait = Image.alpha_composite(
        Image.alpha_composite(native["left"], native["right"]), native["body"]
    )
    bytes_portrait = _png(portrait)
    (android / "bopiportrait0.png").write_bytes(bytes_portrait)

    # The owner Skins reference shows four variations of the SAME rounded
    # Bopi aviator. Earlier native gallery cards incorrectly used unrelated
    # owl/fox vector silhouettes. Preserve existing saved IDs 9/10/8 while
    # deriving their flight pieces from this checksum-verified owner mascot.
    # This is real transparent sprite artwork, not a pasted menu screenshot.
    from colorsys import rgb_to_hsv, hsv_to_rgb
    from PIL import ImageDraw, ImageFilter
    def tint_feathers(image: Image.Image, theme: str) -> Image.Image:
        transformed = image.copy()
        pixels = transformed.load()
        for yy in range(transformed.height):
            for xx in range(transformed.width):
                red,green,blue,alpha=pixels[xx,yy]
                if alpha<12:
                    continue
                hue,saturation,value=rgb_to_hsv(red/255,green/255,blue/255)
                if .48<=hue<=.73 and saturation>.31 and blue>red*1.16:
                    if theme=="royal":
                        nr,ng,nb=hsv_to_rgb(.115,saturation*.40,
                                           min(1.,value*1.21))
                    elif theme=="forest":
                        nr,ng,nb=hsv_to_rgb(.365,saturation*.91,value)
                    else:
                        nr,ng,nb=hsv_to_rgb(.785,saturation*.92,value)
                    pixels[xx,yy]=(round(nr*255),round(ng*255),
                                   round(nb*255),alpha)
        return transformed

    def aviator_accessory(theme: str) -> Image.Image:
        layer=Image.new("RGBA",NATIVE_SIZE,(0,0,0,0))
        drawing=ImageDraw.Draw(layer)
        if theme=="royal":
            # Gold crown with three raised peaks and a jewel, over the goggles.
            silhouette=[(208,143),(219,79),(244,103),(265,59),
                        (291,105),(324,74),(347,143)]
            drawing.polygon([(x+3,y+7) for x,y in silhouette],
                            fill=(58,20,24,125))
            drawing.polygon(silhouette,fill=(255,172,15,255),
                            outline=(109,43,12,255),width=7)
            drawing.polygon([(225,127),(226,93),(244,117),(264,77),
                             (287,121),(320,94),(329,129)],
                            fill=(255,221,71,255))
            drawing.rounded_rectangle((211,132,348,150),radius=9,
                                      fill=(204,84,17,255),
                                      outline=(102,44,18,255),width=4)
            drawing.ellipse((271,121,291,142),fill=(228,61,69,255),
                            outline=(255,244,179,255),width=3)
        elif theme=="forest":
            # Explorer's green brim and khaki crown, all local painted pixels.
            drawing.ellipse((171,135,398,178),fill=(40,54,26,145))
            drawing.rounded_rectangle((218,85,347,153),radius=36,
                                      fill=(153,133,61,255),
                                      outline=(55,74,37,255),width=6)
            drawing.polygon([(241,92),(326,93),(340,133),(214,133)],
                            fill=(184,167,91,255))
            drawing.rounded_rectangle((221,129,346,149),radius=8,
                                      fill=(81,93,43,255))
            drawing.ellipse((169,137,390,172),fill=(143,135,65,255),
                            outline=(57,68,28,255),width=6)
            drawing.polygon([(315,105),(337,75),(346,108)],
                            fill=(62,151,54,255))
        else:
            drawing.rounded_rectangle((200,88,361,150),radius=34,
                                      fill=(53,28,102,255),
                                      outline=(14,17,55,255),width=7)
            drawing.ellipse((186,115,224,171),
                            fill=(79,61,140,255),outline=(14,13,55,255),width=6)
            drawing.ellipse((339,115,377,171),
                            fill=(79,61,140,255),outline=(14,13,55,255),width=6)
            drawing.polygon([(277,78),(288,102),(315,105),(296,120),
                             (302,146),(277,130),(251,146),(257,120),
                             (238,105),(266,102)],fill=(255,196,34,255))
        return layer

    for variant,theme in ((9,"royal"),(10,"forest"),(8,"star")):
        parts={name:tint_feathers(layer,theme) for name,layer in native.items()}
        # The accessory is part of the actual body sprite, so gameplay and
        # Skins render the SAME selected hero with the same persisted ID.
        parts["body"]=Image.alpha_composite(parts["body"],
                                              aviator_accessory(theme))
        for name,res in (("body",f"bopi{variant}"),
                         ("left",f"bopileft{variant}"),
                         ("right",f"bopiright{variant}")):
            (android / f"{res}.png").write_bytes(_png(parts[name]))
        picture=Image.alpha_composite(
            Image.alpha_composite(parts["left"],parts["right"]),
            parts["body"])
        (android/f"bopiportrait{variant}.png").write_bytes(_png(picture))

    # Runtime marker is emitted only for an actual validated photo bundle.
    # The marker lets Android choose reference shoulder joints without
    # ever changing the collider, gameplay state or save-file schema.
    marker = _png(Image.new("RGBA", (2, 2), (255, 255, 255, 255)))
    (android / "bopireference0.png").write_bytes(marker)
    print("PASS: approved reference Bopi body, moving wings and portrait are verified Android assets")
    return True


if __name__ == "__main__":
    install_reference_bopi(Path(__file__).resolve().parents[1])
