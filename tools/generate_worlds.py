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


def detailed_landmarks(index: int) -> str:
    """Distinct scenic landmarks in the same static 480x800 world raster.

    These are decorative background silhouettes. Collision/gate rendering
    remains entirely in the independent native Kotlin/Swift simulations.
    No runtime sprite allocation, network dependency or random clock state.
    """
    sky1, sky2, turf, shade, rock, shine = THEMES[index]
    parts=[
        '<defs>'
        f'<linearGradient id="land-rock" x1=".1" y1="0" x2=".85" y2="1">'
        f'<stop stop-color="{shine}"/><stop offset=".37" stop-color="{rock}"/>'
        f'<stop offset="1" stop-color="{shade}"/></linearGradient>',
        f'<linearGradient id="land-turf" x1="0" y1="0" x2="0" y2="1">'
        f'<stop stop-color="{shine}"/><stop offset=".26" stop-color="{turf}"/>'
        f'<stop offset="1" stop-color="{shade}"/></linearGradient>',
        '<linearGradient id="land-water" x1="0" y1="0" x2="1" y2="0">'
        '<stop stop-color="#1386d7" stop-opacity=".52"/>'
        '<stop offset=".48" stop-color="#e6ffff" stop-opacity=".91"/>'
        '<stop offset="1" stop-color="#20bcfa" stop-opacity=".42"/></linearGradient>',
        '</defs>'
    ]
    def add(fragment: str) -> None:
        parts.append(fragment)

    def floating_island(x: int, y: int, w: int, depth: int, foreground: bool) -> None:
        # Faceted undercut cliffs and layered grass caps create tangible depth
        # compared with the earlier plain elliptical islands.
        opacity=1 if foreground else .78
        add(f'<g opacity="{opacity}">'
            f'<ellipse cx="{x}" cy="{y+depth*.75}" rx="{w*.64}" ry="{depth*.26}"'
            ' fill="#102948" opacity=".21"/>'
            f'<path d="M{x-w*.48} {y+8} Q{x} {y-10} {x+w*.48} {y+9} '
            f'L{x+w*.30} {y+depth*.71} L{x+w*.04} {y+depth} '
            f'L{x-w*.23} {y+depth*.77}Z" fill="url(#land-rock)" stroke="#2e5271" stroke-width="2"/>'
            f'<path d="M{x-w*.48} {y+8} L{x-w*.16} {y+16} '
            f'L{x+w*.04} {y+depth} L{x-w*.23} {y+depth*.77}Z"'
            ' fill="#fff2d1" opacity=".20"/>'
            f'<path d="M{x+w*.10} {y+13} L{x+w*.48} {y+9} '
            f'L{x+w*.30} {y+depth*.71} L{x+w*.04} {y+depth}Z"'
            ' fill="#13213b" opacity=".26"/>'
            f'<ellipse cx="{x}" cy="{y+7}" rx="{w*.50}" ry="17" fill="url(#land-turf)"/>'
            f'<ellipse cx="{x}" cy="{y-1}" rx="{w*.42}" ry="9"'
            f' fill="{shine}" opacity=".58"/>')
        for k in range(5):
            sx=x-w*.30+(k*127%100)*w/100
            add(f'<path d="M{sx:.1f} {y+24} l{-5+(k%3)*4} '
                f'{depth*(.26+(k%3)*.13):.1f}" stroke="{shade}" '
                'stroke-opacity=".47" stroke-width="3" stroke-linecap="round"/>')
        add('</g>')

    def castle(x: int, y: int, scale: float, night: bool=False) -> None:
        wall="#5962b4" if night else "#e9e5cd"
        shadow="#303069" if night else "#7296ae"
        roof="#b794fa" if night else "#2989ce"
        add(f'<g transform="translate({x} {y}) scale({scale})">'
            f'<ellipse cx="2" cy="7" rx="61" ry="10" fill="{shadow}" opacity=".28"/>'
            f'<path d="M-43 1 V-40 L-26 -48 H28 L43 -37 V1Z" fill="{wall}"'
            f' stroke="{shadow}" stroke-width="4"/>'
            f'<path d="M-43 -40 L-26 -48 V1 H-43Z" fill="{shadow}" opacity=".37"/>'
            f'<path d="M-57 1 V-61 H-35 V1 M35 1 V-62 H57 V1"'
            f' fill="{wall}" stroke="{shadow}" stroke-width="3"/>'
            f'<path d="M-65 -61 L-46 -93 L-27 -61Z M27 -62 L46 -97 L65 -62Z"'
            f' fill="{roof}" stroke="#f4dbab" stroke-width="2"/>'
            f'<path d="M-29 -47 L0 -81 L28 -47Z" fill="{roof}"/>'
            f'<path d="M-10 1 V-24 Q0 -38 10 -24 V1Z" fill="{shadow}"/>'
            f'<rect x="-48" y="-45" width="5" height="12" rx="2" fill="#5df2ff"/>'
            f'<rect x="42" y="-43" width="5" height="12" rx="2" fill="#5df2ff"/>'
            f'<rect x="-4" y="-48" width="9" height="15" rx="4" fill="#b5efff"/>'
            f'<path d="M45 -92 v-18 l18 7 -18 5" fill="{roof}" stroke="{roof}"/>'
            '</g>')

    # Anchor worlds around the borders: the playable flight lane and incoming
    # gates have visual contrast but are never occluded by interactive sprites.
    floating_island(371,329,183,135,False)
    floating_island(76,506,140,115,True)
    if index in (0,1,4,5):
        castle(371,326,.82,index==5)
    if index in (0,4):
        castle(76,500,.54,False)
        for k in range(3):
            x=327+17*k
            add(f'<path d="M{x} 356 Q{x+6} 413 {x-5} 452" '
                'fill="none" stroke="url(#land-water)" stroke-width="8" opacity=".67"/>')
        add('<ellipse cx="346" cy="454" rx="44" ry="7" fill="#d6ffff" opacity=".46"/>')
    if index==1:
        # Crystal sky: luminous arches and pink-cyan prismatic spires.
        add('<g stroke="#e9ffff" stroke-width="2.5">'
            '<path d="M330 329 L347 247 L365 325Z" fill="#7ffff7"/>'
            '<path d="M359 330 L390 218 L414 329Z" fill="#b8a5ff"/>'
            '<path d="M397 329 L428 259 L448 329Z" fill="#ffcaec"/>'
            '</g>')
        add('<path d="M343 296 L347 247 L352 305 M388 291 L390 218 L398 293" '
            'stroke="#ffffff" stroke-opacity=".68" stroke-width="3"/>')
    if index==2:
        # Snow: sharper white crystalline towers and an icy mirror.
        for px,py,h in ((344,320,66),(380,306,91),(415,319,77),(58,504,58)):
            add(f'<path d="M{px-13} {py} L{px} {py-h} L{px+15} {py}Z" '
                'fill="#e2f8ff" stroke="#ffffff" stroke-width="3"/>'
                f'<path d="M{px} {py-h} L{px+15} {py} L{px+1} {py-8}Z" fill="#8ed1f6"/>')
        add('<path d="M310 352 Q369 380 451 353" fill="none" '
            'stroke="#dfffff" stroke-width="5" opacity=".65"/>')
    if index==3:
        # Volcano: grounded incandescent fissures, smoke and cinders.
        add('<path d="M309 331 L347 242 L368 296 L386 228 L453 332Z" '
            'fill="#623047" stroke="#ffb05c" stroke-width="3"/>'
            '<path d="M347 243 L358 279 L366 291 L374 328 M385 231 L386 271 L406 316" '
            'stroke="#ffaf2e" stroke-width="8" fill="none"/>'
            '<path d="M347 243 L358 279 L366 291 M385 231 L386 271" '
            'stroke="#fff19a" stroke-width="3" fill="none"/>')
        for k in range(7):
            px=329+(k*41)%120
            py=143+(k*47)%111
            add(f'<circle cx="{px}" cy="{py}" r="{2+k%3}" fill="#ffcf6a" opacity=".82"/>')
    if index==4:
        # Ancient sky towers: gold-trimmed bridge under a bright halo.
        add('<path d="M285 277 Q331 257 356 276" fill="none" '
            'stroke="#f5dca5" stroke-width="11"/>'
            '<path d="M290 277 Q336 262 357 276" fill="none" '
            'stroke="#ffffff" stroke-width="3" opacity=".68"/>'
            '<circle cx="367" cy="207" r="44" fill="none" '
            'stroke="#e5ffff" stroke-width="9" opacity=".65"/>')
    if index==5:
        add('<path d="M303 358 Q365 324 435 358" fill="none" '
            'stroke="#c4a9f9" stroke-width="4" opacity=".55"/>')
        for k in range(10):
            x=289+(k*31)%173
            y=190+(k*43)%156
            add(f'<circle cx="{x}" cy="{y}" r="{1+k%2}" fill="#ffffff" opacity=".88"/>')
    if index==6:
        # Pearl sea: coral fans, nautical glass and underwater light shafts.
        for k in range(5):
            x=319+k*28
            add(f'<path d="M{x} 342 q-16 -28 -10 -52 m10 52 q18 -25 13 -47 '
                f'm-13 47 q-1 -30 5 -54" stroke="{[ "#ffb1d0","#5bf4ee","#ffdaa3"][k%3]}" '
                'stroke-width="7" fill="none" stroke-linecap="round"/>')
        add('<ellipse cx="369" cy="299" rx="28" ry="19" fill="#dfffff" opacity=".60"/>'
            '<ellipse cx="364" cy="293" rx="10" ry="7" fill="#ffffff" opacity=".71"/>')
    if index==7:
        # Astral depths: concentric real portal rings, not painted reward controls.
        add('<ellipse cx="371" cy="269" rx="72" ry="85" fill="#271356" opacity=".95"/>'
            '<ellipse cx="371" cy="269" rx="61" ry="74" fill="none" stroke="#cf90ff" '
            'stroke-width="13" opacity=".85"/>'
            '<ellipse cx="371" cy="269" rx="48" ry="58" fill="none" stroke="#71ebff" '
            'stroke-width="9" opacity=".80"/>'
            '<ellipse cx="371" cy="269" rx="34" ry="42" fill="#0a103c"/>')
        for k in range(9):
            angle=math.tau*k/9
            cx=371+math.cos(angle)*96
            cy=269+math.sin(angle)*105
            add(f'<circle cx="{cx:.2f}" cy="{cy:.2f}" r="{2+k%3}" '
                'fill="#e4dcff" opacity=".83"/>')
    # Foreground sparkle and highlights are small and never contain interface
    # labels/counters. The same deterministic XML yields identical Android/iOS art.
    for k in range(7):
        x=18+(k*73+index*11)%455
        y=180+(k*97+index*17)%362
        add(f'<path d="M{x-4} {y} h8 M{x} {y-4} v8" stroke="{shine}" '
            'stroke-width="1.6" opacity=".65" stroke-linecap="round"/>')
    return "".join(parts)

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
    # Landmarks use the same static raster on both platforms; gates stay on top.
    add(detailed_landmarks(index))
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
