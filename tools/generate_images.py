#!/usr/bin/env python3
from pathlib import Path
from PIL import Image
import io, json, cairosvg
r=Path(__file__).resolve().parents[1]
a=r/'android/app/src/main/res';b=r/'ios/BOPAVI/Assets.xcassets'
for p in [a/'drawable',a/'drawable-nodpi',b/'AppIcon.appiconset',b/'Hero.imageset',b/'Logo.imageset',b/'LaunchArt.imageset',b/'HomeArt.imageset']:p.mkdir(parents=True,exist_ok=True)
icon=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/icon.svg'),output_width=1024,output_height=1024))).convert('RGB')
icon.save(b/'AppIcon.appiconset/icon-1024.png',optimize=True)
icon.resize((512,512),Image.Resampling.LANCZOS).save(a/'drawable/app_icon.png',optimize=True)
brand=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/logo.svg'),output_width=780))).convert('RGBA')
brand.save(a/'drawable-nodpi/logo.png',optimize=True)
brand.save(b/'Logo.imageset/logo.png',optimize=True)
hero=Image.open(io.BytesIO(cairosvg.svg2png(url=str(r/'docs/assets/hero.svg'),output_width=900))).convert('RGB')
hero.save(a/'drawable-nodpi/hero.png',optimize=True)
hero.save(b/'Hero.imageset/hero.png',optimize=True)
# Continuous bright sky below the floating islands instead of a flat navy band.
launch=Image.new('RGB',(900,1600),(18,110,201))
from PIL import ImageDraw
draw=ImageDraw.Draw(launch)
stops=[(0,(16,100,190)),(520,(45,168,240)),(1200,(105,208,247)),(1600,(51,152,219))]
for y in range(1600):
    for i in range(len(stops)-1):
        y0,c0=stops[i];y1,c1=stops[i+1]
        if y<=y1:
            t=max(0.0,min(1.0,(y-y0)/(y1-y0)))
            draw.line((0,y,900,y),fill=tuple(int(c0[j]+(c1[j]-c0[j])*t) for j in range(3)))
            break
clouds=Image.new('RGBA',launch.size,(0,0,0,0))
cloud_draw=ImageDraw.Draw(clouds)
for cx,cy,scale in [(-60,1270,1.0),(750,1330,.65),(220,1550,.85)]:
    cloud_draw.ellipse((cx-120*scale,cy-24*scale,cx+175*scale,cy+56*scale),fill=(240,253,255,50))
    cloud_draw.ellipse((cx-55*scale,cy-86*scale,cx+80*scale,cy+25*scale),fill=(245,252,255,60))
launch=Image.alpha_composite(launch.convert('RGBA'),clouds).convert('RGB')
# Feather the landscape edges into the portrait sky: no rectangular image seam.
mask=Image.new('L',hero.size,0)
mask_pixels=mask.load()
for y in range(hero.height):
    edge_alpha=min(1.0,y/95.0,(hero.height-1-y)/125.0)
    value=int(255*max(0.0,edge_alpha))
    for x in range(hero.width): mask_pixels[x,y]=value
launch.paste(hero,(0,340),mask)
# Atmospheric perspective: layered mountains and distant sky islands.
# All effects are rendered at build time into the same portrait raster for both platforms.
from math import sin, pi
atmosphere=ImageDraw.Draw(launch, 'RGBA')
for band, baseline, amplitude, tint in [
    (0, 1170, 52, (163,224,244,52)),
    (1, 1240, 66, (86,178,214,64)),
    (2, 1380, 82, (28,119,176,55)),
]:
    ridge=[(x, baseline + int(sin((x + 63*band)/107)*amplitude + sin(x/39 + band)*amplitude*.17)) for x in range(-20, 921, 15)]
    atmosphere.polygon([(-20,1600), *ridge, (920,1600)], fill=tint)
# Soft sunbeams remain below the interactive logo and do not obscure typography.
for k in range(9):
    x=75+k*108
    atmosphere.polygon([(445,445),(x-44,1180),(x+12,1180)], fill=(255,245,181,8))

# Fill the formerly empty lower third with detailed foreground scenery.
# This is painted only once during the build, never inside game frame loops.
scene=ImageDraw.Draw(launch,"RGBA")
def island(cx,top,width,depth,seed):
    from math import sin
    left=cx-width//2
    # Back rim and stacked shaded rock facets.
    scene.ellipse((left-14,top-5,left+width+15,top+40),fill=(23,95,112,48))
    scene.polygon([(left+5,top+13),(left+width-3,top+12),(cx+width*.23,top+depth*.72),
        (cx-6,top+depth),(cx-width*.31,top+depth*.72)],fill=(102,103,116,240))
    scene.polygon([(left+8,top+18),(cx-9,top+depth*.9),(cx-width*.3,top+depth*.65)],
        fill=(181,144,114,234))
    scene.polygon([(cx,top+25),(left+width-6,top+18),(cx+width*.23,top+depth*.67)],
        fill=(91,91,111,215))
    for k in range(8):
        x=left+24+(k*71+seed*17)%(max(25,width-42))
        ya=top+31+(k*37)%max(30,depth//2)
        scene.line((x,ya,x-14,ya+12,x-5,ya+27),fill=(68,70,92,100),width=4)
    # Bright lush turf with a reflective lime highlight.
    scene.ellipse((left,top-18,left+width,top+32),fill=(28,128,77,255))
    scene.ellipse((left+7,top-21,left+width-7,top+22),fill=(93,214,88,255))
    scene.arc((left+13,top-20,left+width-14,top+17),180,350,fill=(191,254,135,235),width=7)
    # Flowers and clumps of leaves make the island readable at 5-inch size.
    for k in range(7):
        x=left+19+(k*49+seed*11)%(max(20,width-34))
        y=top-13+(k%3)*4
        scene.ellipse((x-9,y-6,x+11,y+10),fill=(23,142,65,240))
        scene.ellipse((x-7,y-10,x+7,y+5),fill=(98,231,104,255))
        if k%3==0:
            scene.ellipse((x+8,y-7,x+14,y-1),fill=(255,220,74,255))
    if seed%2==0:
        wx=left+width*.58
        for off in range(0,20,4):
            scene.line((wx+off,top+25,wx+off-12,top+depth*.73),
                fill=(218,252,255,190-off*6),width=5)
        scene.ellipse((wx-26,top+depth*.69,wx+50,top+depth*.78),fill=(197,255,255,63))
island(115,1210,280,178,2)
island(758,1285,275,210,3)
island(439,1497,330,161,5)
# Fluffy foreground clouds create depth and a seamless safe area for controls.
for cx,cy,sc in [(-35,1570,1.1),(900,1530,1.1),(360,1620,1.4)]:
    for dx,dy,rx,ry in [(-86,5,138,58),(38,11,156,48),(-18,-40,92,80)]:
        x=cx+dx*sc; y=cy+dy*sc
        scene.ellipse((x-rx*sc,y-ry*sc,x+rx*sc,y+ry*sc),fill=(250,255,255,113))
logoCrop=brand.copy()
logoCrop.thumbnail((740,250),Image.Resampling.LANCZOS)
# These are two different reference screens. The home backdrop keeps its
# wordmark ABOVE Bopi and leaves all navigation/real counters as live views.
home_art=launch.copy()
home_art.paste(logoCrop,((900-logoCrop.width)//2,230),logoCrop)
home_art.save(a/'drawable-nodpi/homeback.png',optimize=True)
home_art.save(b/'HomeArt.imageset/home.png',optimize=True)
# The OS launch screen puts Bopi first and the same winged wordmark BELOW.
# No fabricated progress percentage: platform splash is only visible while
# the real app starts. Never bake interactive menu controls into this raster.
launch_art=launch.copy()
launch_art.paste(logoCrop,((900-logoCrop.width)//2,865),logoCrop)
launch_art.save(a/'drawable-nodpi/splash.png',optimize=True)
launch_art.save(b/'LaunchArt.imageset/launch.png',optimize=True)
(b/'Contents.json').write_text(json.dumps({'info':{'author':'xcode','version':1}}))
(b/'AppIcon.appiconset/Contents.json').write_text(json.dumps({'images':[{'filename':'icon-1024.png','idiom':'universal','platform':'ios','size':'1024x1024'}],'info':{'author':'xcode','version':1}}))
(b/'Logo.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'logo.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
(b/'LaunchArt.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'launch.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
(b/'HomeArt.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'home.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
(b/'Hero.imageset/Contents.json').write_text(json.dumps({'images':[{'filename':'hero.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))

# Extract Bopi from the approved hero artwork, without external assets or sprite licenses.
# Keep the same body, scarf and goggles on both native platforms.
import re, colorsys
hero_markup=(r/'docs/assets/hero.svg').read_text()
bird=re.search(r'<g transform="translate\(454 336\) rotate\(-12\)">([\s\S]*?)</g>',hero_markup)
if bird is None:
    raise RuntimeError('Bird group missing from hero art')
defs=re.search(r'<defs>(.*?)</defs>',hero_markup,re.S)
if defs is None:
    raise RuntimeError('Hero gradient definitions missing')
# Wing art uses explicitly marked paths, not fragile "first four paths".
# Feather highlights travel with the existing animated wings rather than
# remaining frozen on Bopi's torso. Saved skin indices never change.
wing_shapes=[m.group(0) for m in re.finditer(
    r'<path\b[^>]*\bdata-wing="(?:left|right)"[^>]*/>',bird.group(1))]
wing_parts={side:[shape for shape in wing_shapes if f'data-wing="{side}"' in shape]
            for side in ('left','right')}
if any(len(wing_parts[side])<4 for side in ('left','right')):
    raise RuntimeError('Cannot extract Bopi animated wing base and feather details')
body_markup=bird.group(1)
for shape in wing_shapes:
    body_markup=body_markup.replace(shape,'',1)
# The source painting contains a soft ground shadow under the flying bird.
# Keep it in the menu scenery, but never attach it to a moving flight sprite:
# an apparent hovering oval otherwise follows the player throughout gameplay.
shadow_oval='<ellipse cx="10" cy="164" rx="146" ry="28" fill="#1a7ea7" opacity=".16" filter="url(#shadow)"/>'
if shadow_oval not in body_markup:
    raise RuntimeError('Expected Bopi background shadow for sprite separation')
body_markup=body_markup.replace(shadow_oval,'',1)
def raster(part):
    xml=('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" '
         'viewBox="-260 -260 520 520"><defs>'+defs.group(1)+'</defs><g>'
         +part+'</g></svg>')
    return Image.open(io.BytesIO(cairosvg.svg2png(bytestring=xml.encode(),output_width=512,output_height=512))).convert('RGBA')
original=raster(body_markup)
original_wings=[raster(''.join(wing_parts[side])) for side in ('left','right')]
def recolor(original_image,hue):
    if hue is None:
        return original_image.copy()
    variant=original_image.copy()
    pixels=variant.load()
    for yy in range(variant.height):
        for xx in range(variant.width):
            red,green,blue,alpha=pixels[xx,yy]
            if alpha<12: continue
            h,s,v=colorsys.rgb_to_hsv(red/255,green/255,blue/255)
            if 0.49<=h<=0.72 and s>=0.34 and blue>red*1.16:
                nr,ng,nb=colorsys.hsv_to_rgb(hue,s,v)
                pixels[xx,yy]=(int(nr*255),int(ng*255),int(nb*255),alpha)
    return variant
# Recolor only the saturated blue feathers and body; retain goggles, beak and scarf.
palette=[None,.145,.975,.75,.46,.60]
for skin,hue in enumerate(palette):
    variant=recolor(original,hue)
    name=f'bopi{skin}'
    variant.save(a/'drawable-nodpi'/f'{name}.png',optimize=True)
    skinset=b/(f'Bopi{skin}.imageset')
    skinset.mkdir(exist_ok=True)
    variant.save(skinset/(name+'.png'),optimize=True)
    (skinset/'Contents.json').write_text(json.dumps({'images':[{'filename':name+'.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
    for label,original_wing in zip(('Left','Right'),original_wings):
        wing_name=f'bopi{label.lower()}{skin}'
        wing=recolor(original_wing,hue)
        wing.save(a/'drawable-nodpi'/(wing_name+'.png'),optimize=True)
        catalog=b/(f'Bopi{label}{skin}.imageset')
        catalog.mkdir(exist_ok=True)
        wing.save(catalog/(wing_name+'.png'),optimize=True)
        (catalog/'Contents.json').write_text(json.dumps({'images':[{'filename':wing_name+'.png','idiom':'universal'}],'info':{'author':'xcode','version':1}}))
# Three original bird pilots replace the former human pilot illustrations.
# IMPORTANT: keep saved skin indices 6/7/8 and generated Bopi6..8 sprite
# names stable, so owned masks, picked pilot and offline v1..v6 backups survive.
# The legacy backup slugs ("portantin", "noa", "any") remain internal aliases.
def render_bird_pilot(part):
    part=re.sub(r'^.*?<svg[^>]*>','',part,count=1,flags=re.S)
    part=re.sub(r'</svg>.*','',part,flags=re.S)
    xml='<svg xmlns="http://www.w3.org/2000/svg" viewBox="-65 -65 130 130" width="512" height="512">'+part+'</svg>'
    return Image.open(io.BytesIO(cairosvg.svg2png(bytestring=xml.encode(),output_width=512,output_height=512))).convert('RGBA')
for skin, character in ((6,'vihor'),(7,'iskra'),(8,'orion')):
    source=(r/'docs/assets'/f'{character}.svg').read_text()
    defs=re.search(r'<defs>(.*?)</defs>',source,re.S)
    left=re.search(r'<g id="wing-left">.*?</g>',source,re.S)
    right=re.search(r'<g id="wing-right">.*?</g>',source,re.S)
    if defs is None or left is None or right is None:
        raise RuntimeError(f'{character}: missing original wings or gradients')
    body=re.sub(r'<g id="wing-(?:left|right)">.*?</g>','',source,flags=re.S)
    parts=((body, f'bopi{skin}', f'Bopi{skin}'),
           ('<defs>'+defs.group(1)+'</defs>'+left.group(0),f'bopileft{skin}',f'BopiLeft{skin}'),
           ('<defs>'+defs.group(1)+'</defs>'+right.group(0),f'bopiright{skin}',f'BopiRight{skin}'))
    for markup, name, catalog in parts:
        sprite=render_bird_pilot(markup)
        if sprite.getbbox() is None:
            raise RuntimeError(f'{character}: empty native flight sprite {name}')
        sprite.save(a/'drawable-nodpi'/(name+'.png'),optimize=True)
        assetset=b/(catalog+'.imageset')
        assetset.mkdir(exist_ok=True)
        sprite.save(assetset/(name+'.png'),optimize=True)
        (assetset/'Contents.json').write_text(json.dumps({
            'images':[{'filename':name+'.png','idiom':'universal'}],
            'info':{'author':'xcode','version':1}
        }))
# Selection, settings, pause and results need a COMPLETE character portrait.
# In-flight layers remain independent for real flap animation. Earlier menus
# used only 'bopiN' (the torso), accidentally hiding BOTH wings for every skin.
# Composite the already-approved body/left/right layers once during the build.
# This adds no scene decoding or per-frame allocations to the game loop.
from PIL import ImageFilter
for skin in range(9):
    src=a/'drawable-nodpi'
    body=Image.open(src/f'bopi{skin}.png').convert('RGBA')
    left=Image.open(src/f'bopileft{skin}.png').convert('RGBA')
    right=Image.open(src/f'bopiright{skin}.png').convert('RGBA')
    if body.size!=(512,512) or left.size!=body.size or right.size!=body.size:
        raise RuntimeError(f'skin {skin}: incompatible character art size')
    wings=Image.alpha_composite(left,right)
    combined=Image.alpha_composite(wings,body)
    if combined.getbbox() is None or combined.getchannel('A').getextrema()[1]!=255:
        raise RuntimeError(f'skin {skin}: full-wing portrait is empty')
    # Subtle azure/gold depth beneath the actual pixels, NEVER a replacement
    # for the genuine sprite silhouette or a baked-in interactive control.
    hue=(85,200,255) if skin not in (1,2,7,8) else (
        (255,202,90) if skin==1 else (255,137,185) if skin in (2,8) else (104,225,255))
    diffusion=combined.getchannel('A').filter(ImageFilter.GaussianBlur(11))
    halo=Image.new('RGBA',body.size,(*hue,0))
    halo.putalpha(diffusion.point(lambda alpha: min(100,int(alpha*0.33))))
    portrait=Image.alpha_composite(halo,combined)
    name=f'bopiportrait{skin}.png'
    portrait.save(src/name,optimize=True)
    setdir=b/f'BopiPortrait{skin}.imageset'
    setdir.mkdir(parents=True,exist_ok=True)
    portrait.save(setdir/name,optimize=True)
    (setdir/'Contents.json').write_text(json.dumps({
        'images':[{'filename':name,'idiom':'universal'}],
        'info':{'author':'xcode','version':1}
    }))
# Four shared illustrated footer symbols, rendered once for both apps.
for key in ('profile','tasks','collection','shop'):
    name=f'nav_{key}.png'
    icon=Image.open(io.BytesIO(cairosvg.svg2png(
        url=str(r/'docs/assets'/f'nav-{key}.svg'),output_width=128,output_height=128))).convert('RGBA')
    icon.save(a/'drawable-nodpi'/name,optimize=True)
    catalog=b/f'Nav{key.capitalize()}.imageset'
    catalog.mkdir(parents=True,exist_ok=True)
    icon.save(catalog/name,optimize=True)
    (catalog/'Contents.json').write_text(json.dumps({
        'images':[{'filename':name,'idiom':'universal'}],
        'info':{'author':'xcode','version':1}
    }))
from generate_worlds import generate_worlds
generate_worlds(a,b)
print('Generated native art: 9 complete glowing portraits, 9 bodies, 18 flapping wings and 8 biomes')
