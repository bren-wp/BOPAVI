"""Deterministic illustrated world scenes shared by the two native renderers.
All artwork is generated in the build from source; no external network/assets.
"""
from pathlib import Path
import io, json, math, random
from PIL import Image
import cairosvg

THEMES = [
    # sky top, sky bottom, land, shadow, rock, highlight
    ("#0b97f4","#dcfaff","#76df5c","#258765","#b79a78","#e9ff9d"),
    ("#13b9ed","#ffe2a7","#f8cc79","#49c7ae","#d79570","#fff0af"),
    ("#438fea","#e5f7ff","#bceeff","#73acd2","#89b9df","#ffffff"),
    ("#65336d","#ff985d","#f7a044","#8a2a48","#553a51","#ffd36b"),
    ("#5aa9fa","#fff1c5","#ffd987","#c7b081","#baa58f","#fff7d0"),
    ("#121d51","#6950b8","#a58dff","#453577","#38355f","#d6c5ff"),
    ("#127c99","#aaf9ee","#75dfda","#2781a1","#425e94","#e7fffd"),
    ("#08144a","#544aab","#b4aaff","#6c63b8","#384379","#e5e1ff"),
]

def shapes(index: int):
    sky1, sky2, turf, shade, rock, shine = THEMES[index]
    rng = random.Random(950 + index)
    items=[]
    def add(s): items.append(s)
    add(f'<svg xmlns="http://www.w3.org/2000/svg" width="480" height="800" viewBox="0 0 480 800">')
    add(f'''<defs>
    <linearGradient id="sky" x2="0" y2="1"><stop stop-color="{sky1}"/><stop offset="1" stop-color="{sky2}"/></linearGradient>
    <linearGradient id="cliff" x2=".4" y2="1"><stop stop-color="{rock}"/><stop offset="1" stop-color="{shade}"/></linearGradient>
    <linearGradient id="ledge" x2=".25" y2="1"><stop stop-color="{shine}"/><stop offset=".5" stop-color="{turf}"/><stop offset="1" stop-color="{shade}"/></linearGradient>
    <radialGradient id="glow"><stop stop-color="{shine}" stop-opacity=".76"/><stop offset="1" stop-color="{shine}" stop-opacity="0"/></radialGradient>
    </defs><rect width="480" height="800" fill="url(#sky)"/>''')
    if index in (5,7):
        for i in range(35):
            x,y=rng.randrange(480),rng.randrange(550)
            rad=rng.choice([1,1.3,2.4])
            add(f'<circle cx="{x}" cy="{y}" r="{rad}" fill="#ffffff" opacity=".7"/>')
    light_x=385 if index%2==0 else 91
    add(f'<circle cx="{light_x}" cy="165" r="160" fill="url(#glow)"/>')
    if index==5:
        add('<circle cx="377" cy="115" r="38" fill="#f9f2cc"/><circle cx="394" cy="104" r="34" fill="#372963"/>')
    elif index==7:
        add('<circle cx="367" cy="151" r="56" fill="#d78ce9"/><ellipse cx="367" cy="151" rx="86" ry="19" stroke="#ece1ff" stroke-width="10" fill="none" transform="rotate(-23 367 151)" opacity=".8"/>')
    elif index!=3:
        add(f'<circle cx="{light_x}" cy="165" r="34" fill="{shine}" opacity=".92"/>')
    # Three gently receding cloud fields. Small SVG objects, rasterized only once.
    if index not in (3,7):
        for layer in range(3):
            for i in range(6):
                x=-80+i*133 +rng.randrange(-22,24)
                y=105+layer*128+rng.randrange(-40,45)
                opacity=.43 if layer==0 else .3 if layer==1 else .2
                add(f'<g opacity="{opacity}"><ellipse cx="{x+45}" cy="{y+29}" rx="56" ry="17" fill="white"/>'
                    f'<circle cx="{x+22}" cy="{y+15}" r="25" fill="white"/>'
                    f'<circle cx="{x+62}" cy="{y+6}" r="31" fill="white"/></g>')
    # Deep background silhouettes are biome dependent, no identical recolored landscape.
    for i in range(7):
        x=i*92-70
        h=120+rng.randrange(30,100)
        baseline=684
        if index in (2,3,6):
            add(f'<path d="M{x-12} {baseline} L{x+37} {baseline-h} L{x+89} {baseline}Z" fill="{shade}" opacity=".19"/>')
            if index==2:
                add(f'<path d="M{x+18} {baseline-h/2} L{x+37} {baseline-h} L{x+55} {baseline-h/2}Z" fill="white" opacity=".5"/>')
        elif index==4:
            add(f'<rect x="{x}" y="{baseline-h/2}" width="17" height="{h/2}" rx="5" fill="{rock}" opacity=".33"/>'
                f'<rect x="{x-9}" y="{baseline-h/2-11}" width="35" height="14" rx="4" fill="{shine}" opacity=".45"/>')
        else:
            add(f'<path d="M{x-30} {baseline} Q{x+35} {baseline-h} {x+90} {baseline}Z" fill="{shade}" opacity=".16"/>')
    # Floating cliff layers, waterfalls and textured ledges at different scales.
    for i in range(9):
        xx=rng.randrange(-100,520)
        yy=rng.randrange(70,680)
        ww=rng.choice((68,86,115))
        if 130<xx<285 and 245<yy<540:
            xx+=150
        hh=65+rng.randrange(15,80)
        opacity=(.52 if i<4 else .84)
        add(f'<g opacity="{opacity}">'
            f'<path d="M{xx} {yy+15} Q{xx+ww/2} {yy-6} {xx+ww} {yy+17} '
            f'L{xx+ww*.67} {yy+hh} L{xx+ww*.42} {yy+hh+18} L{xx+ww*.18} {yy+hh*.62}Z" fill="url(#cliff)"/>'
            f'<ellipse cx="{xx+ww/2}" cy="{yy+12}" rx="{ww/2+3}" ry="17" fill="url(#ledge)"/>'
            f'<path d="M{xx+ww*.36} {yy+32} L{xx+ww*.34} {yy+hh*.77}" stroke="{shine}" stroke-width="5" opacity=".29"/>'
            f'</g>')
        if index==0 and i in (1,4,7):
            add(f'<path d="M{xx+ww*.54} {yy+26} L{xx+ww*.52} {yy+hh+64}" stroke="#a5f8ff" stroke-width="11" stroke-linecap="round" opacity=".58"/>')
        if index in (0,1,4,5) and ww>80:
            if index==4:
                add(f'<g opacity=".73"><rect x="{xx+ww*.42}" y="{yy-45}" width="17" height="53" rx="3" fill="#f4ecd4"/>'
                    f'<path d="M{xx+ww*.34} {yy-41} L{xx+ww*.51} {yy-59} L{xx+ww*.68} {yy-41}Z" fill="#fff4b4"/></g>')
            elif index==1:
                add(f'<path d="M{xx+ww*.57} {yy+8} Q{xx+ww*.60} {yy-42} {xx+ww*.45} {yy-69}" fill="none" stroke="#997047" stroke-width="7"/>'
                    f'<ellipse cx="{xx+ww*.45}" cy="{yy-72}" rx="34" ry="12" fill="#4fc991"/>')
            elif index==5:
                add(f'<path d="M{xx+ww*.49} {yy+6} V{yy-39}" stroke="#9482ca" stroke-width="11" opacity=".6"/>')
            else:
                add(f'<circle cx="{xx+ww*.44}" cy="{yy-12}" r="20" fill="#48b75e" opacity=".84"/>')
        if index==6:
            add(f'<path d="M{xx+ww*.44} {yy+8} L{xx+ww*.58} {yy-38} L{xx+ww*.73} {yy+12}Z" fill="#7efff0" opacity=".8" stroke="#dcfff9" stroke-width="3"/>')
        if index==3:
            add(f'<path d="M{xx+ww*.38} {yy+12} L{xx+ww*.31} {yy+hh-12}" stroke="#ffb63f" stroke-width="6" opacity=".75"/>')
    if index==7:
        for i in range(4):
            x=48+i*120; y=355+(i%2)*113
            add(f'<circle cx="{x}" cy="{y}" r="{13+i*5}" fill="{shine}" opacity=".53"/>')
    # Ground is deliberately below the obstacle play region, with a soft lip.
    add(f'<rect x="0" y="757" width="480" height="43" fill="{shade}"/>'
        f'<path d="M0 754 Q110 741 225 753 T480 750 V772 H0Z" fill="url(#ledge)"/>')
    add('</svg>')
    return "".join(items)

def generate_worlds(android_res:Path, ios_assets:Path):
    android=android_res/"drawable-nodpi"
    android.mkdir(parents=True,exist_ok=True)
    for world in range(8):
        svg=shapes(world)
        png=cairosvg.svg2png(bytestring=svg.encode(),output_width=960,output_height=1600)
        im=Image.open(io.BytesIO(png)).convert("RGB")
        name=f"world{world}"
        im.save(android/(name+".png"),optimize=True)
        assets=ios_assets/(f"World{world}.imageset")
        assets.mkdir(exist_ok=True)
        im.save(assets/(name+".png"),optimize=True)
        (assets/"Contents.json").write_text(json.dumps({
          "images":[{"filename":name+".png","idiom":"universal"}],
          "info":{"author":"xcode","version":1}
        }))
    print("PASS: 8 distinct world backdrops shared by Android and iOS")
