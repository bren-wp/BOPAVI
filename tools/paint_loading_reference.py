#!/usr/bin/env python3
"""Splash-only background architecture traced from the owner's 941x1672 scene.

Draws OFFLINE scenery behind live BOPAVI logo, animated pilot, collectible
illustrations and genuine native loading control; NO screenshot, UI, logo, bird,
loading bar or progress percentage is baked into this image.
"""
from math import sin
from random import Random
from PIL import Image, ImageDraw, ImageFilter


def paint_loading_reference(base: Image.Image) -> Image.Image:
    if base.size != (900, 1600):
        raise ValueError("BOPAVI loading background needs a 900x1600 source")
    rng = Random(9411672 + 54)
    scenery = base.convert("RGBA")
    layer = Image.new("RGBA", scenery.size)
    draw = ImageDraw.Draw(layer, "RGBA")

    def cliff(cx, top, width, depth):
        """Jagged ivy-covered waterfall cliff, kept outside the pilot's core."""
        left, right = cx - width / 2, cx + width / 2
        # Existing reference has tapered, fractured sandstone rather than
        # triangular flat columns. Left and right facets are differently lit.
        ridge = [(left, top + 13), (left + width*.13, top - 18),
                 (cx + width*.10, top - 14), (right, top + 5)]
        draw.polygon(ridge + [
            (right-width*.11, top+depth*.69),
            (cx+width*.12, top+depth),
            (cx-width*.30, top+depth*.80),
            (left+width*.03, top+depth*.42)],
            fill=(77, 73, 104, 248))
        draw.polygon([(left,top+13),(cx-width*.12,top+25),
                      (cx-width*.24,top+depth*.75),
                      (left+width*.03,top+depth*.42)],
                     fill=(183, 142, 115, 210))
        draw.polygon([(cx+width*.12,top+22),(right,top+5),
                      (right-width*.11,top+depth*.69),
                      (cx+width*.12,top+depth)],
                     fill=(49, 65, 98, 203))
        for i in range(16):
            x=left+12+(i*31)%(max(32,int(width)-25))
            y=top+35+(i*29)%max(40,int(depth*.65))
            draw.line((x,y,x+8,y+21+(i%3)*12),
                      fill=(236,195,147,49+i%4*16),width=2)
        # Voluminous tiers of grasses and small garden bushes.
        draw.ellipse((left-9,top-25,right+9,top+35),
                     fill=(22,111,55,255))
        draw.ellipse((left,top-29,right,top+17),
                     fill=(102,204,74,255))
        draw.arc((left+9,top-28,right-9,top+15),177,347,
                 fill=(195,252,133,215),width=6)
        for i in range(35):
            x=left+8+(i*41+11)%max(28,int(width)-20)
            y=top-19+(i*13)%16
            rad=3+(i%5)
            draw.ellipse((x-rad,y-rad,x+rad,y+rad),
                         fill=(18,91+(i*11)%70,43,226))
            if i%8==0:
                draw.ellipse((x+rad-2,y-rad-2,x+rad+3,y-rad+3),
                             fill=(255,246,204,231))
        # Detached, translucent water curtains show flow all the way down.
        fallx=cx+width*.20
        for i in range(16):
            x=fallx+(i-8)*2.8
            yy=top+23
            xe=x-9+sin(i)*3
            draw.line((x,yy,xe,top+depth*.83),
                      fill=(208,246,255,130+(i*43)%105),
                      width=2 if i%3 else 3)
        draw.ellipse((fallx-32,top+depth*.79,fallx+34,top+depth*.90),
                     fill=(219,253,255,109))

    def castle(cx, ground, scale):
        # Red terracotta roofs, flag poles and pale carved stone towers.
        for offset,height in ((-41,106),(-6,150),(37,114)):
            x=cx+offset*scale
            y=ground-height*scale
            w=17*scale
            draw.polygon([(x-w,y),(x+w,y),(x+w,ground),
                          (x-w,ground)],fill=(248,231,209,237))
            draw.line((x-w,y,x-w,ground),fill=(108,100,103,194),width=2)
            draw.line((x+w,y,x+w,ground),fill=(124,129,137,190),width=2)
            draw.polygon([(x-w*1.40,y),(x,y-38*scale),
                          (x+w*1.4,y)],fill=(209,79,63,255))
            draw.line((x,y-38*scale,x,y-57*scale),
                      fill=(80,85,95,218),width=2)
            draw.polygon([(x,y-55*scale),
                          (x+24*scale,y-48*scale),
                          (x,y-44*scale)],fill=(255,123,69,255))
            for yy in (y+29*scale,y+59*scale):
                draw.rounded_rectangle((x-5*scale,yy,x+5*scale,
                                        yy+14*scale),
                                       radius=max(1,scale),
                                       fill=(48,78,116,211))
        draw.polygon([(cx-66*scale,ground),(cx+70*scale,ground),
                      (cx+48*scale,ground+21*scale),
                      (cx-59*scale,ground+17*scale)],
                     fill=(244,226,198,244))

    # The owner's source has a prominent LEFT castle on cliff, and a smaller
    # secondary castle high on the right. Both are scenic, not Android Views.
    cliff(50,253,254,420)
    castle(72,257,1.14)
    cliff(855,378,205,270)
    castle(834,377,.65)
    cliff(41,948,171,280)
    cliff(865,1085,155,230)

    # Tiny far islands create depth against blue sky without reproducing UI.
    for cx,yy,scale in ((251,66,.65),(619,58,.43),(715,740,.35),
                        (390,1079,.29),(785,666,.28)):
        w=100*scale
        draw.polygon([(cx-w,yy),(cx+w,yy),(cx+w*.40,yy+139*scale),
                      (cx,yy+190*scale),(cx-w*.52,yy+113*scale)],
                     fill=(104,89,110,160))
        draw.ellipse((cx-w,yy-22*scale,cx+w,yy+19*scale),
                     fill=(62,168,76,216))
        for i in range(5):
            x=cx-w+i*w*.43
            draw.ellipse((x-7*scale,yy-19*scale,
                          x+8*scale,yy+3*scale),fill=(24,115,55,194))

    # Soft cloud banks behind the actual logo and Bopi, not on top of them.
    scenery=Image.alpha_composite(scenery,layer)
    clouds=Image.new("RGBA",scenery.size)
    pen=ImageDraw.Draw(clouds,"RGBA")
    for cx,cy,s in ((-45,440,.72),(960,535,.68),
                    (-35,1245,.87),(892,1300,.80),
                    (110,1480,.85),(752,1465,.80)):
        for j in range(16):
            dx=rng.randrange(-100,101)*s
            dy=rng.randrange(-49,49)*s
            rad=rng.randrange(30,82)*s
            pen.ellipse((cx+dx-rad,cy+dy-rad*.63,
                         cx+dx+rad,cy+dy+rad*.63),
                        fill=(251,254,255,35+(j*17)%76))
    scenery=Image.alpha_composite(scenery,clouds.filter(
        ImageFilter.GaussianBlur(11)))
    return scenery.convert("RGB")


if __name__=="__main__":
    sample=Image.new("RGB",(900,1600),(44,143,221))
    output=paint_loading_reference(sample)
    assert output.size==(900,1600) and output.tobytes()!=sample.tobytes()
    assert paint_loading_reference(sample).tobytes()==output.tobytes()
    try:
        paint_loading_reference(Image.new("RGB",(899,1600)))
    except ValueError:
        pass
    else:
        raise AssertionError("Invalid screenshot-scale background accepted")
    print("PASS: deterministic original offline Splash scenery without baked UI")
