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

    return cinematic_detail(Image.alpha_composite(source.convert("RGBA"),overlay).convert("RGB"))


def cinematic_detail(flat: Image.Image) -> Image.Image:
    """Add deterministic soft cloud banks, distant garden cliffs and foliage.

    Scene only: no Bopi/logo, UI text, score, fake currency or touch targets.
    All detail stays in the OFFLINE landscape behind native Android Views.
    """
    from PIL import ImageFilter
    from math import sin
    w,h=flat.size
    rng=Random(9411672)
    result=flat.convert("RGBA")

    # Owner Splash/Home references have a luminous, multilayered white cloud
    # sea behind the flying hero. The old cyan blank centre produced huge
    # whole-screen errors even when logo/button geometry was source-aligned.
    # These are scenic *background* pigment layers, never image UI elements.
    # Keep the castles/rock terraces and native pilot above the cloud banks.
    bank=Image.new("RGBA",(w,h),(0,0,0,0))
    cloud_pigment=ImageDraw.Draw(bank,"RGBA")
    for index,(cx,cy,rx,ry) in enumerate((
        (425,227,190,84),(596,278,155,87),(-16,601,155,105),
        (932,678,144,115),(447,1068,188,76),(280,1293,142,93))):
        cloud_rng=Random(837100+index)
        for j in range(28):
            dx=cloud_rng.uniform(-1.0,1.0)*rx*.74
            dy=cloud_rng.uniform(-1.0,1.0)*ry*.52
            ar=cloud_rng.uniform(.24,.63)*rx
            br=cloud_rng.uniform(.34,.70)*ry
            cloud_pigment.ellipse((cx+dx-ar,cy+dy-br,
                                  cx+dx+ar,cy+dy+br),
                fill=(248,253,255,cloud_rng.randint(48,107)))
        # The lower pearl-white edge is denser than the translucent skyward
        # diffusion, matching the owner's airy sculpted cloud banks.
        cloud_pigment.ellipse((cx-rx*.67,cy-ry*.13,
                              cx+rx*.77,cy+ry*.45),
                             fill=(252,255,255,89))
    result=Image.alpha_composite(
        result,bank.filter(ImageFilter.GaussianBlur(20)))
    result=Image.alpha_composite(
        result,bank.filter(ImageFilter.GaussianBlur(3)))

    # Puffy, semi-transparent cloud banks around the outer thirds.
    mist=Image.new("L",(w,h),0)
    pen=ImageDraw.Draw(mist)
    for cx,cy,s in ((-24,127,1.05),(927,168,1.04),
                    (-36,600,1.20),(925,727,1.10),
                    (4,1030,1.08),(901,1180,1.13),
                    (182,1425,.87),(775,1463,.86)):
        for _ in range(17):
            dx=rng.randint(-145,145)*s
            dy=rng.randint(-72,68)*s
            radius=rng.randint(33,87)*s
            x,y=cx+dx,cy+dy
            pen.ellipse((int(x-radius),int(y-radius*.64),
                         int(x+radius),int(y+radius*.64)),
                        fill=rng.randint(78,139))
    cloud=Image.new("RGBA",(w,h),(249,253,255,0))
    cloud.putalpha(mist.filter(ImageFilter.GaussianBlur(18)))
    result=Image.alpha_composite(result,cloud)

    # Layered rock cliffs and waterfalls reinforce the owner's left/right
    # waterfall islands without covering the central animated pilot.
    layer=Image.new("RGBA",(w,h),(0,0,0,0))
    d=ImageDraw.Draw(layer,"RGBA")
    for index,(cx,top,rad,depth) in enumerate(
        ((35,315,155,174),(856,435,137,172),
         (24,877,116,156),(877,1048,113,149))):
        left,right=cx-rad,cx+rad
        d.ellipse((left-8,top+depth-18,right+8,top+depth+22),
                  fill=(29,69,113,32))
        d.polygon(((left,top+11),(right,top+13),
                   (cx+rad*.66,top+depth*.62),
                   (cx+rad*.1,top+depth),(cx-rad*.56,top+depth*.69)),
                  fill=(78,80,107,228))
        d.polygon(((left+9,top+15),(cx-rad*.06,top+29),
                   (cx-rad*.38,top+depth*.66),
                   (cx-rad*.56,top+depth*.69)),
                  fill=(183,148,124,194))
        d.polygon(((cx+rad*.08,top+29),(right-6,top+14),
                   (cx+rad*.66,top+depth*.62),
                   (cx+rad*.14,top+depth*.85)),
                  fill=(66,75,106,180))
        for k in range(8):
            x=left+24+k*(2*rad-48)/8
            y=top+25+(k%3)*8
            d.line((int(x),int(y),int(x+8),int(y+34+(k%3)*10)),
                   fill=(242,207,156,83),width=2)
        d.ellipse((left-5,top-24,right+5,top+32),
                  fill=(27,116,65,242))
        d.ellipse((left+1,top-30,right-1,top+14),
                  fill=(84,198,77,247))
        d.arc((left+8,top-27,right-9,top+17),185,350,
              fill=(195,255,146,204),width=6)
        xfall=cx+rad*.48 if index%2 else cx-rad*.24
        for k in range(12):
            x=xfall+(k-6)*2.9
            bottom=top+depth*.85+sin(k*1.9)*7
            d.line((int(x),top+18,int(x-13),int(bottom)),
                   fill=(169,231,255,127+((k*47)%90)),width=3)
        d.ellipse((xfall-35,top+depth*.8,xfall+42,top+depth*.91),
                  fill=(226,252,255,76))
        for k in range(20):
            x=left+12+(k*29+index*17)%max(20,2*rad-26)
            y=top-22+(k%5)*5
            rr=5+(k%3)*3
            d.ellipse((x-rr,y-rr,x+rr,y+rr),
                      fill=(24,98,53,235) if k%4
                           else (151,240,67,229))
            if k%6==0:
                d.ellipse((x+rr-2,y-rr-3,x+rr+3,y-rr+2),
                          fill=(255,251,231,225))
    # In the owner illustration, dark soft-focus foliage frames the lower
    # corners. It is BACKGROUND, never a synthetic clickable image.
    fringe=Image.new("RGBA",(w,h),(0,0,0,0))
    foliage=ImageDraw.Draw(fringe,"RGBA")
    for side in (-1,1):
        anchor=0 if side<0 else w
        # Both supplied Home/Splash references have dense, defocused
        # foreground leaves that frame the bottom, NOT an empty blue sea.
        # Keep the centre of the three native Home footer controls free.
        for k in range(35):
            x=anchor+side*rng.randrange(-105,205)
            y=rng.randrange(1455,1730)
            rx,ry=rng.randrange(35,113),rng.randrange(21,77)
            foliage.ellipse((x-rx,y-ry,x+rx,y+ry),
                            fill=(9,45+((k*7)%38),27,138+(k%5)*17))
            if k%4==0:
                foliage.arc((x-rx+8,y-ry+6,x+rx-9,y+ry-5),
                            210,330,fill=(122,178,74,98),width=3)
        # A separate closer silhouette keeps the bottom corners dark
        # without painting over gameplay controls or the central Bopi.
        for k in range(13):
            x=anchor+side*rng.randrange(-78,126)
            y=rng.randrange(1582,1740)
            rx,ry=rng.randrange(55,126),rng.randrange(31,85)
            foliage.ellipse((x-rx,y-ry,x+rx,y+ry),
                            fill=(5,31+(k%4)*9,22,202))
    result=Image.alpha_composite(result,layer)
    result=Image.alpha_composite(result,
        fringe.filter(ImageFilter.GaussianBlur(9)))
    return result.convert("RGB")


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
