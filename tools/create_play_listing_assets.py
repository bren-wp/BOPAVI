#!/usr/bin/env python3
"""Generate Play listing graphics from existing BOPAVI art and real emulator captures.

Screenshots MUST come from QA-Android-visual-flow; never simulate game UI or
substitute artwork for a screenshot. Play-safe 9:16 crops preserve actual pixels.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageOps
import argparse
import shutil
import zipfile
from release_metadata import release_metadata

parser = argparse.ArgumentParser()
parser.add_argument("--screenshots", type=Path, default=Path("qa/screenshots"))
parser.add_argument("--out", type=Path, default=Path("dist/google-play"))
args = parser.parse_args()
version, _ = release_metadata()
out = args.out
out.mkdir(parents=True, exist_ok=True)
art = Path("android/app/src/main/res")
icon = Image.open(art / "drawable/app_icon.png").convert("RGB")
if icon.size != (512, 512):
    raise SystemExit("Play icon source must be the current 512x512 game icon")
icon.save(out / "play-icon-512.png", format="PNG", optimize=True)

# 1024x500 24-bit hero: BOPAVI's own original illustration and logo, no mock UI.
feature = Image.new("RGB", (1024, 500))
draw = ImageDraw.Draw(feature)
for y in range(500):
    t = y / 499
    draw.line((0, y, 1024, y), fill=(int(9+24*t), int(49+74*t), int(112+92*t)))
hero = Image.open(art / "drawable-nodpi/hero.png").convert("RGB")
hero = ImageOps.fit(hero, (590, 500), method=Image.Resampling.LANCZOS, centering=(0.56,0.50))
feature.paste(hero, (434, 0))
# Smooth fade into the graphic on the left, without obstructing the actual art.
overlay = Image.new("RGBA", (1024, 500), (0, 0, 0, 0))
layer = ImageDraw.Draw(overlay)
for x in range(260, 620):
    a = int(235 * (620-x)/360)
    layer.line((x, 0, x, 500), fill=(9, 49, 112, a))
feature = Image.alpha_composite(feature.convert("RGBA"), overlay)
logo = Image.open(art / "drawable-nodpi/logo.png").convert("RGBA")
logo.thumbnail((470, 290), Image.Resampling.LANCZOS)
feature.alpha_composite(logo, ((500-logo.width)//2+18, (500-logo.height)//2))
feature.convert("RGB").save(out / "feature-graphic-1024x500.png", optimize=True)

# Source screenshot names are from the REAL Android device UI automation.
# Cropping to 9:16 is required because emulator's raw 1080x2400 is over 2:1,
# which Play disallows. Do not paint or fabricate any app contents.
sources = [
    ("android-home.png", "01-home.png"),
    ("android-pilot-picker.png", "02-pilot-selection.png"),
    ("android-gameplay-ready.png", "03-flight-preview.png"),
    ("android-result.png", "04-result.png"),
]
for source, dest in sources:
    path = args.screenshots / source
    if not path.is_file():
        raise SystemExit(f"Missing real emulator screenshot: {path}")
    with Image.open(path) as raw:
        im = raw.convert("RGB")
        if im.width < 720 or im.height < im.width * 1.6:
            raise SystemExit(f"Screenshot is not a real portrait emulator capture: {path}: {im.size}")
        target_width = im.width
        target_height = target_width * 16 // 9
        if im.height < target_height:
            raise SystemExit(f"Too short for lossless 9:16 cropping: {path}")
        # Bias toward lower controls while preserving the central play field.
        excess = im.height - target_height
        top = int(excess * 0.64)
        im = im.crop((0, top, target_width, top + target_height))
        if im.width < 320 or im.height > 3840 or im.height > 2 * im.width:
            raise SystemExit(f"Invalid Play screenshot dimensions: {path}: {im.size}")
        im.save(out / dest, optimize=True)

(out / "README.txt").write_text(
    f"BOPAVI Google Play listing kit (v{version})\n"
    "play-icon-512.png: original BOPAVI app icon, RGB 512x512.\n"
    "feature-graphic-1024x500.png: original BOPAVI art and logo, RGB 1024x500.\n"
    "01-04: REAL Android emulator screenshots, cropped to 9:16 without creating UI.\n"
    "03-flight-preview shows IDLE game before first flap, not live gameplay.\n"
    "For Google Play games recommendations, capture at least 3 real ACTIVE\n"
    "gameplay shots on Android phones in addition to this starter kit.\n"
    "Verify images, cropped controls, safe areas and representation before publishing.\n",
    encoding="utf-8",
)
bundle = out.parent / f"BOPAVI-Google-Play-listing-v{version}.zip"
with zipfile.ZipFile(bundle, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zf:
    for path in sorted(out.iterdir()):
        zf.write(path, path.name)
print(f"PASS: {bundle}, {len(sources)} genuine Android screenshots and 2 original store graphics")
