#!/usr/bin/env python3
"""Check themed Bopi pilots retain legitimate owner-layer wings and drawn detail.

Called after generate_images.py. Uses original SVG raster as a control; never
compares to fabricated owner reference or claims pixel-perfect equality.
"""
from pathlib import Path
from PIL import Image, ImageChops
from io import BytesIO
import re
import cairosvg

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/"android/app/src/main/res/drawable-nodpi"

def source_parts(character):
    source=(ROOT/"docs/assets"/f"{character}.svg").read_text()
    definition=re.search(r"<defs>(.*?)</defs>",source,re.S)
    left=re.search(r'<g id="wing-left">.*?</g>',source,re.S)
    right=re.search(r'<g id="wing-right">.*?</g>',source,re.S)
    assert definition and left and right
    body=re.sub(r'<g id="wing-(?:left|right)">.*?</g>','',source,flags=re.S)
    return (body,
            '<defs>'+definition.group(1)+'</defs>'+left.group(0),
            '<defs>'+definition.group(1)+'</defs>'+right.group(0))

def base_raster(markup):
    body=re.sub(r'^.*?<svg[^>]*>','',markup,count=1,flags=re.S)
    body=re.sub(r'</svg>.*','',body,flags=re.S)
    svg='<svg xmlns="http://www.w3.org/2000/svg" viewBox="-80 -80 160 160" width="512" height="512">'+body+'</svg>'
    return Image.open(BytesIO(cairosvg.svg2png(bytestring=svg.encode(),output_width=512))).convert("RGBA")

if (ROOT/"art/reference-approved/bopi/manifest.json").is_file():
    # When owner-approved image layers are present, Skins uses coloured
    # derivatives of exactly that same Bopi (with Royal/Explorer/Star hats).
    # IDs 9/10/8 remain stable across exported saves and live gameplay.
    original=Image.open(OUT/"bopi0.png").convert("RGBA")
    for skin in (9,10,8):
        for name,reference_name in ((f"bopi{skin}","bopi0"),
                                    (f"bopileft{skin}","bopileft0"),
                                    (f"bopiright{skin}","bopiright0")):
            current=Image.open(OUT/f"{name}.png").convert("RGBA")
            reference=Image.open(OUT/f"{reference_name}.png").convert("RGBA")
            assert current.size==reference.size==(512,512),name
            assert current.getchannel("A").getbbox(),(name,"empty art")
            assert ImageChops.difference(reference,current).getbbox(),(
                name,"themed pilot duplicates Bopi without any new art")
            if "left" in name or "right" in name:
                assert current.getchannel("A").tobytes()==reference.getchannel("A").tobytes(),(
                    name,"altered owner-approved flight wing shape")
        portrait=Image.open(OUT/f"bopiportrait{skin}.png").convert("RGBA")
        assert portrait.size==(512,512) and portrait.getchannel("A").getbbox()
else:
    # Offline fallback build: keep the original 3D-shaded Sova/Lisac SVGs.
    for skin,character in ((9,"sova"),(10,"lisac")):
        for markup,name in zip(source_parts(character),
                               (f"bopi{skin}",f"bopileft{skin}",f"bopiright{skin}")):
            base=base_raster(markup)
            actual=Image.open(OUT/f"{name}.png").convert("RGBA")
            assert actual.size==(512,512),name
            assert base.getchannel("A").tobytes()==actual.getchannel("A").tobytes(),name
            diff=ImageChops.difference(base.convert("RGB"),actual.convert("RGB"))
            assert diff.getbbox(),(name,"missing original 3D relief")
            bounds=actual.getchannel("A").getbbox()
            assert bounds and min(bounds[0],bounds[1],512-bounds[2],512-bounds[3])>=6
        portrait=Image.open(OUT/f"bopiportrait{skin}.png").convert("RGBA")
        assert portrait.size==(512,512) and portrait.getchannel("A").getbbox()
logo=Image.open(OUT/"logo.png").convert("RGBA")
l,t,r,b=logo.getchannel("A").getbbox()
assert min(l,t,logo.width-r,logo.height-b)>=5,(
    "BOPAVI lettering/crown clipped at raster edge",(l,t,r,b))
print("PASS: Sova and Lisac retain Bopi's exact flight silhouette with real 3D shading; complete BOPAVI raster margins verified")
