#!/usr/bin/env python3
"""Verify real generated pilot anatomy, uncut wings in Android resources.

The tests read produced transparent PNG resources. They intentionally do not
compare with reference mockups and do not claim photographic/pixel-perfect 1:1.
"""
from pathlib import Path
from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "android/app/src/main/res/drawable-nodpi"


def open_sprite(name: str, catalog: str):
    android = ANDROID / f"{name}.png"
    if not android.is_file():
        raise AssertionError(f"Missing Android pilot asset: {name}")
    picture = Image.open(android).convert("RGBA")
    assert picture.size == (512, 512), (name, "sprite resolution")
    return picture


def anatomy_pixels(picture: Image.Image, skin: int):
    # In both source coordinate systems, the beak is below the irises and
    # above the scarf. Restrict testing to this region so gold goggles do
    # not hide a missing or disconnected beak.
    box = (230, 270, 385, 360) if skin < 6 else (204, 280, 340, 378)
    warm = dark_mouth = opaque = 0
    for r, g, b, a in picture.crop(box).getdata():
        if a < 175:
            continue
        opaque += 1
        if r >= 165 and g >= 65 and b <= 155 and r >= g + 42:
            warm += 1
        if 45 < r < 170 and 10 < g < 105 and 10 < b < 135:
            dark_mouth += 1
    assert opaque >= 750, (skin, "facial artwork is transparent/missing")
    assert warm >= 90, (skin, "warm upper bill and lower jaw are missing")
    assert dark_mouth >= 25, (skin, "open smile shading is missing")


def main():
    for skin in range(9):
        body = open_sprite(f"bopi{skin}", f"Bopi{skin}")
        left = open_sprite(f"bopileft{skin}", f"BopiLeft{skin}")
        right = open_sprite(f"bopiright{skin}", f"BopiRight{skin}")
        portrait = open_sprite(f"bopiportrait{skin}", f"BopiPortrait{skin}")
        anatomy_pixels(body, skin)
        for side, wing in (("left", left), ("right", right)):
            bounds = wing.getchannel("A").getbbox()
            assert bounds, (skin, side, "empty wing")
            x0, y0, x1, y1 = bounds
            assert min(x0, y0, 512 - x1, 512 - y1) >= 6, (
                skin, side, "wing touches raster edge; clipping risk", bounds
            )
        # Menus are full portraits while flight sprites retain articulated wings.
        composite = Image.alpha_composite(Image.alpha_composite(left, right), body)
        assert ImageChops.difference(composite, body).getbbox(), (
            skin, "portrait would have no real wings"
        )
        assert portrait.getchannel("A").getbbox(), (skin, "empty menu portrait")
        # A wing must actually extend beyond the torso silhouette; otherwise
        # the pilot appears as a floating head with detached/hidden wings.
        wing_alpha = Image.alpha_composite(left, right).getchannel("A")
        body_alpha = body.getchannel("A")
        visible_feathers = sum(
            1 for wing, torso in zip(wing_alpha.getdata(), body_alpha.getdata())
            if wing >= 160 and torso < 32
        )
        assert visible_feathers >= 150, (skin, "wings hidden behind torso")
    print("PASS: nine Android pilots have shaded attached bills, "
          "nine uncut left/right wing pairs and full portraits")


if __name__ == "__main__":
    main()
