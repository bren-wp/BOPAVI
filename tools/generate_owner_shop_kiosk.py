#!/usr/bin/env python3
"""Extract only non-interactive Shop scenery from SHA-verified owner WebPs.

The 260×80 legacy decorative crop was inflated on four 1080px cameras.
These high-resolution, lossless owner regions contain the awning, original
Bopi and stall, but fade before FEATURED text or real purchase controls.
The complete source reference is never used as a live screen.
"""
from pathlib import Path
from PIL import Image

VIEWPORTS = (
    ("Android_16_9_1080x1920", 1080, 1920, "bopavi_owner_shop_kiosk_1920"),
    ("Android_FHD_1080x2280", 1080, 2280, "bopavi_owner_shop_kiosk_2280"),
    ("Android_FHD_Plus_1080x2340", 1080, 2340, "bopavi_owner_shop_kiosk_2340"),
    ("Android_20_9_1080x2400", 1080, 2400, "bopavi_owner_shop_kiosk_2400"),
)

STAGE_X = 24
STAGE_Y = 400
STAGE_WIDTH = 894
STAGE_HEIGHT = 276
VERTICAL_ANCHOR = .18
SHOP_16_9_WIDTH_FACTOR = .8435


def stage_pixels(width: int, height: int) -> tuple[int, int, int, int]:
    """Match LevelMapStage integer coordinates for the four owner viewports."""
    if width <= 0 or height <= 0:
        raise ValueError("Invalid Shop owner viewport")
    scale = min(width / 941, height / 1672)
    excess = max(0, height - 1672 * scale)
    factor = SHOP_16_9_WIDTH_FACTOR if (width, height) == (1080, 1920) else 1.0
    x = int(width * .5 + (STAGE_X * scale - width * .5) * factor)
    y = int(STAGE_Y * scale + excess * VERTICAL_ANCHOR)
    w = int(STAGE_WIDTH * scale * factor)
    h = int(STAGE_HEIGHT * scale)
    if x < 0 or y < 0 or w < 1 or h < 1 or x + w > width or y + h > height:
        raise ValueError("Decorative stall exceeds the approved owner viewport")
    return x, y, w, h


def make_kiosk(source: Path, output: Path, width: int, height: int) -> None:
    with Image.open(source) as owner:
        if owner.format != "WEBP" or owner.size != (width, height):
            raise ValueError("Shop image is not a verified-size original WebP")
        x, y, w, h = stage_pixels(width, height)
        art = owner.convert("RGBA").crop((x, y, x + w, y + h))
    # Feather the original plaque boundary and NEVER copy a featured label.
    upper = 62 if height == 1920 else 46
    lower_start = 160 if height == 1920 else 215
    lower_end = 210 if height == 1920 else 265
    alpha = Image.new("L", (w, h), 0)
    for row in range(h):
        if row < upper:
            opacity = round(255 * row / upper)
        elif row <= lower_start:
            opacity = 255
        elif row < lower_end:
            opacity = round(255 * (lower_end - row) / (lower_end - lower_start))
        else:
            opacity = 0
        alpha.paste(max(0, min(255, opacity)), (0, row, w, row + 1))
    art.putalpha(alpha)
    output.parent.mkdir(parents=True, exist_ok=True)
    art.save(output, "WEBP", lossless=True, method=6)


def generate_all(root: Path, output_dir: Path) -> list[Path]:
    """Reject missing, modified, fake or wrong-resolution originals."""
    from install_current_references import read_index, verify_image
    _, records = read_index()
    indexed = {item["relative_path"]: item for item in records}
    outputs = []
    for folder, width, height, name in VIEWPORTS:
        key = folder + "/08_Shop.webp"
        original = root / "art/reference-current/images" / key
        verify_image(original.read_bytes(), indexed[key])
        target = output_dir / (name + ".webp")
        make_kiosk(original, target, width, height)
        outputs.append(target)
    return outputs


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[1]
    print("Generated four original Shop scenery assets:",
          [p.name for p in generate_all(root,
           root / "android/app/src/main/res/drawable-nodpi")])
