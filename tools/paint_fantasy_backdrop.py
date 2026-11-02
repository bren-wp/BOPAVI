#!/usr/bin/env python3
"""Deterministic offline scenic detail for the shared Splash/Home BACKGROUND.

No UI labels, mascots, progress bars, scores, or buttons are rasterized here.
All illustrations remain behind separately composed Android-native controls.
"""
from PIL import Image, ImageDraw
from random import Random


def enrich_background(source: Image.Image) -> Image.Image:
    if source.size != (900, 1600):
        raise ValueError("Expected untouched 900 x 1600 landscape grid")
    overlay = Image.new("RGBA", source.size, (0, 0, 0, 0))
    pen = ImageDraw.Draw(overlay, "RGBA")
    rng = Random(24187)

    # Luminous floating mountain terraces, far behind Bopi's main silhouette.
    for x, y, size in ((226,310,.43), (720,327,.35),
                       (340,497,.29), (595,557,.25), (813,744,.18)):
        w = 122*size
        pen.polygon(((x-w,y+6*size),(x+w,y+6*size),
                     (x+42*size,y+149*size),(x,y+191*size),
                     (x-52*size,y+134*size)),fill=(71,79,120,185))
        pen.polygon(((x-w,y+6*size),(x-12*size,y+25*size),
                     (x-43*size,y+134*size)),fill=(185,149,110,148))
        pen.ellipse((x-w,y-23*size,x+w,y+19*size),fill=(66,166,70,200))
        for k in range(6):
            sx=x-w+(k*37+13)*size
            pen.ellipse((sx-8*size,y-23*size,sx+11*size,y+2*size),
                        fill=(29,116,62,172))

    def castle(x, y, s):
        for dx, tall in ((-29,58),(11,85),(45,52)):
            tx=x+dx*s
            ty=y-tall*s
            pen.rectangle((tx-13*s,ty,tx+14*s,y+8*s),
                          fill=(237,229,214,209))
            pen.polygon(((tx-19*s,ty),(tx,ty-23*s),(tx+20*s,ty)),
                        fill=(205,88,70,224))
            pen.rectangle((tx-3*s,ty+18*s,tx+3*s,ty+34*s),
                          fill=(71,154,188,191))
            pen.line((tx,ty-24*s,tx,ty-36*s),fill=(86,64,63,180),
                     width=max(1,round(2*s)))
            pen.polygon(((tx,ty-36*s),(tx+18*s,ty-31*s),
                         (tx,ty-29*s)),fill=(252,122,72,209))
    castle(102,426,.66)
    castle(773,487,.58)
    castle(809,943,.27)

    # Waterfalls glow over rocky faces and retain readable silhouettes.
    for x,y,length,wide in ((116,436,194,15),(776,491,242,13),(817,949,119,8)):
        for i in range(9):
            dx=(i-4)*wide/9
            pen.line((x+dx,y,x+dx-13,y+length),
                     fill=(188,240,255,118 if i%2 else 166),
                     width=2 if i%2 else 3)
        pen.ellipse((x-27,y+length-8,x+29,y+length+10),
                    fill=(232,249,255,83))

    # Small hot-air balloons, suspended baskets, and distant airship.
    for x,y,r in ((161,130,30),(817,230,23),(736,798,15)):
        pen.ellipse((x-r,y-1.3*r,x+r,y+.85*r),fill=(249,189,124,205))
        pen.pieslice((x-r,y-1.3*r,x+r,y+.85*r),
                     220,291,fill=(186,92,126,173))
        pen.ellipse((x-.3*r,y-1.14*r,x+.24*r,y+.46*r),
                    fill=(255,238,169,144))
        pen.line((x-.28*r,y+.73*r,x-.18*r,y+1.34*r),
                 fill=(67,67,79,194),width=2)
        pen.line((x+.28*r,y+.73*r,x+.18*r,y+1.34*r),
                 fill=(67,67,79,194),width=2)
        pen.rectangle((x-.24*r,y+1.32*r,x+.24*r,y+1.48*r),
                      fill=(117,78,50,203))
    pen.ellipse((744,363,864,416),fill=(203,171,133,197),
                outline=(120,92,75,161),width=3)
    pen.arc((751,371,857,405),192,340,fill=(255,232,185,175),width=4)
    pen.line((807,406,807,430),fill=(90,71,69,174),width=3)
    pen.polygon(((787,427),(831,427),(822,438),(799,438)),
                fill=(133,88,55,199))
    pen.ellipse((854,377,875,399),fill=(249,204,128,190))

    # Soft edges only: keep the central pilot and letterform edges unobscured.
    for x,y,s in ((-80,585,.66),(967,670,.62),
                  (-31,1100,.78),(943,1194,.66)):
        for dx,dy,rx,ry in ((-50,8,105,40),(27,-30,80,66),(88,9,96,42)):
            pen.ellipse((x+(dx-rx)*s,y+(dy-ry)*s,
                         x+(dx+rx)*s,y+(dy+ry)*s),
                        fill=(249,255,255,95))
    for _ in range(29):
        x=rng.randrange(0,900)
        y=rng.randrange(75,1150)
        radius=rng.choice((1,1,2))
        pen.ellipse((x-radius,y-radius,x+radius,y+radius),
                    fill=(255,255,219,rng.randrange(45,105)))

    return Image.alpha_composite(source.convert("RGBA"),overlay).convert("RGB")


if __name__ == "__main__":
    source=Image.new("RGB",(900,1600),(32,158,230))
    modified=enrich_background(source)
    assert modified.size==source.size and modified.tobytes()!=source.tobytes()
    assert enrich_background(source).tobytes()==modified.tobytes()
    try:
        enrich_background(Image.new("RGB",(901,1600)))
    except ValueError:
        pass
    else:
        raise AssertionError("Source dimensions must be validated")
    print("PASS: offline decorated fantasy sky, deterministic and original-sized")
