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
    # Cinematic landmark pass: rock facets, real flowing waterfall strands,
    # clustered canopies and bright mist. These are *background-only* pixels;
    # live gates, the chosen bird, score and touch controls stay independent.
    # Drawn once at build time for identical Android/iOS full-screen assets.
    add('<defs><radialGradient id="falls-mist">'
        '<stop stop-color="#f2ffff" stop-opacity=".82"/>'
        '<stop offset=".48" stop-color="#a6f6ff" stop-opacity=".37"/>'
        '<stop offset="1" stop-color="#b1eaff" stop-opacity="0"/>'
        '</radialGradient></defs>')
    waterfall_world = index in (0, 1, 2, 4, 5, 6)
    if waterfall_world:
        waterfall_tint = "#8af6ff" if index not in (2, 5) else "#b9cdff"
        # Three unequal strands curve beneath the same landmark; they have
        # translucent edges rather than a single flat bright rectangle.
        for n, length in enumerate((166, 189, 154, 174, 132)):
            source_x = 324 + n * 12
            source_y = 356 + (n % 2) * 3
            destination_x = source_x - 9 + (n % 3) * 6
            destination_y = source_y + length
            add(f'<path d="M{source_x} {source_y} Q{source_x-13} '
                f'{source_y+length*.54:.1f} {destination_x} {destination_y}" '
                f'fill="none" stroke="{waterfall_tint}" '
                f'stroke-width="{5+(n%2)*4}" opacity=".55" '
                'stroke-linecap="round"/>')
            add(f'<path d="M{source_x-1} {source_y+11} Q{source_x-6} '
                f'{source_y+length*.54:.1f} {destination_x-2} {destination_y-8}" '
                'fill="none" stroke="#efffff" stroke-width="2" opacity=".69"/>')
        add('<ellipse cx="350" cy="535" rx="67" ry="29" fill="url(#falls-mist)"/>')
        for n in range(11):
            x = 312 + (n*37) % 94
            y = 469 + (n*19) % 90
            add(f'<circle cx="{x}" cy="{y}" r="{1.1+(n%3)*.7:.1f}" '
                'fill="#edffff" opacity=".57"/>')
    # Faceted cliff faces below the right landmark: specular edges,
    # fine cracks and embedded mineral chips suggest physical thickness.
    for n in range(9):
        sx = 303 + (n * 37) % 132
        sy = 354 + (n * 41) % 57
        ex = sx + (n % 3 - 1) * 11
        ey = sy + 19 + n % 4 * 6
        add(f'<path d="M{sx} {sy} L{ex} {ey}" fill="none" '
            f'stroke="{shine if n%3 == 0 else shade}" stroke-width="{1+n%3}" '
            'stroke-opacity=".42" stroke-linecap="round"/>')
    # Small tree crowns trace a soft irregular silhouette along floating
    # terraces. Biome-specific luminous highlights replace generic circles.
    if index in (0, 1, 4, 5):
        canopy = "#8bf3ae" if index in (0, 1) else "#d6d4ff" if index==5 else "#ffe0a2"
        trunk = "#376b5d" if index in (0, 1) else "#79655b"
        for n in range(6):
            tx = 292 + n * 30
            ty = 332 - (n % 3) * 4
            size = 8 + (n % 3) * 3
            add(f'<path d="M{tx} {ty+4} q-2 {-size} 0 {-size*2}" '
                f'stroke="{trunk}" stroke-width="4" fill="none"/>')
            add(f'<ellipse cx="{tx-5}" cy="{ty-size*2}" rx="{size}" '
                f'ry="{size*.64:.1f}" fill="{shade}" opacity=".91"/>')
            add(f'<ellipse cx="{tx+4}" cy="{ty-size*2-4}" rx="{size*.8:.1f}" '
                f'ry="{size*.68:.1f}" fill="{canopy}" opacity=".85"/>')
            add(f'<ellipse cx="{tx-2}" cy="{ty-size*2-8}" rx="{size*.46:.1f}" '
                f'ry="{size*.28:.1f}" fill="#f3ffe7" opacity=".51"/>')
    # Receding bridge and distant miniature archways reinforce perspective.
    if index in (0, 1, 4, 5):
        rim = "#c8ecff" if index!=5 else "#aaa1f8"
        add(f'<path d="M244 320 Q281 290 308 321" '
            f'fill="none" stroke="{rim}" stroke-width="8" opacity=".60"/>'
            '<path d="M244 321 Q281 302 308 322" fill="none" '
            'stroke="#385681" stroke-width="3" opacity=".49"/>')
        for n in range(5):
            x = 249 + n*12
            add(f'<path d="M{x} 315 v{-4-n%2*4}" '
                'stroke="#ebf7ff" stroke-width="2" opacity=".55"/>')
    # Foreground sparkle and highlights are small and never contain interface
    # labels/counters. The same deterministic XML yields identical Android/iOS art.
    for k in range(7):
        x=18+(k*73+index*11)%455
        y=180+(k*97+index*17)%362
        add(f'<path d="M{x-4} {y} h8 M{x} {y-4} v8" stroke="{shine}" '
            'stroke-width="1.6" opacity=".65" stroke-linecap="round"/>')
    return "".join(parts)


def storybook_landmarks(index: int) -> str:
    """A distinct illustrated set-piece for each reference world.

    Decorative art is baked into the Android/iOS world background *behind*
    the live gates, Bopi, coins, HUD and collision shapes. Deterministic output
    means a saved run looks the same on both platforms and without a network.
    Keep the central flight lane relatively clear and artwork off the HUD.
    """
    rng = random.Random(26017 + index)
    p = [
        '<defs>'
        '<radialGradient id="story-glow">'
        '<stop stop-color="#fff8c1" stop-opacity=".72"/>'
        '<stop offset="1" stop-color="#fff8c1" stop-opacity="0"/>'
        '</radialGradient>'
        '<linearGradient id="story-falls" x2="1" y2=".13">'
        '<stop stop-color="#31a9ff" stop-opacity=".35"/>'
        '<stop offset=".45" stop-color="#f0ffff" stop-opacity=".94"/>'
        '<stop offset="1" stop-color="#19b8ef" stop-opacity=".46"/>'
        '</linearGradient>'
        '<linearGradient id="story-gold" x2="0" y2="1">'
        '<stop stop-color="#fff9dd"/>'
        '<stop offset=".57" stop-color="#ffe08a"/>'
        '<stop offset="1" stop-color="#bb791d"/>'
        '</linearGradient>'
        '</defs>'
    ]
    def add(x: str) -> None:
        p.append(x)

    if index == 0:
        # Green islands — tiers of turquoise water, sunlit houses and flowers.
        for x, top, bottom, wide in ((307,354,559,14),(330,355,594,12),(365,356,555,10)):
            add(f'<path d="M{x} {top} Q{x-17} {(top+bottom)/2:.0f} {x-4} {bottom}" '
                f'stroke="url(#story-falls)" stroke-width="{wide}" '
                'stroke-linecap="round" fill="none" opacity=".75"/>')
            add(f'<path d="M{x+2} {top+6} Q{x-6} {(top+bottom)/2:.0f} {x-2} {bottom-8}" '
                'stroke="#f5ffff" stroke-width="2.2" fill="none" opacity=".68"/>')
        for x,y in ((26,457),(74,481),(323,303),(417,299)):
            add(f'<path d="M{x-9} {y} L{x-9} {y-25} L{x} {y-39} '
                f'L{x+9} {y-25} V{y}Z" fill="#fff6da" stroke="#91bcc4" stroke-width="2"/>'
                f'<path d="M{x-12} {y-24} L{x} {y-43} L{x+12} {y-24}Z" fill="#2389d8"/>'
                f'<rect x="{x-3}" y="{y-19}" width="6" height="11" rx="2" fill="#ffe495"/>')
        for n in range(24):
            x = [35,74,319,389][n%4] + rng.randrange(-23,24)
            y = [489,493,333,326][n%4] + rng.randrange(-8,8)
            add(f'<circle cx="{x}" cy="{y}" r="{1+n%2}" '
                f'fill="{["#ffffff","#ffee84","#ffb6e5"][n%3]}" opacity=".94"/>')

    elif index == 1:
        # Enchanted forest — real hanging lanterns and luminous fungi.
        add('<path d="M18 250 Q83 191 111 274 M474 236 Q408 177 381 279" '
            'fill="none" stroke="#38663a" stroke-width="12" opacity=".79"/>')
        for x, y, length in ((65,250,55),(96,263,32),(422,241,64),(390,270,39)):
            add(f'<path d="M{x} {y} q-4 {length//2} 0 {length}" '
                'fill="none" stroke="#8c7242" stroke-width="2.5"/>'
                f'<ellipse cx="{x}" cy="{y+length+12}" rx="25" ry="25" '
                'fill="url(#story-glow)" opacity=".78"/>'
                f'<path d="M{x-8} {y+length+2} h16 l-3 16 h-10Z" '
                'fill="#ffdb63" stroke="#fff4b6" stroke-width="2"/>'
                f'<circle cx="{x}" cy="{y+length+9}" r="3" fill="#fff9da"/>')
        for n in range(19):
            x=25+(n*79)%436
            y=365+(n*41)%328
            add(f'<ellipse cx="{x}" cy="{y}" rx="{8+n%3*2}" ry="{4+n%3}" '
                f'fill="{["#edabff","#75f2ff","#eaff9b"][n%3]}" opacity=".70"/>'
                f'<path d="M{x} {y+3} v9" stroke="#d4e6bd" stroke-width="2"/>')

    elif index == 2:
        # Crystal sky — faceted colored spires anchored to existing cliffs.
        for k,(x,y,h,w,col) in enumerate((
            (328,329,87,19,"#6af0ff"),(352,327,136,27,"#80a5ff"),
            (384,324,173,34,"#de9bff"),(424,331,118,25,"#ffbeec"),
            (58,499,69,17,"#9cdbff"),(84,500,81,19,"#f4a0ff")
        )):
            add(f'<path d="M{x-w} {y} L{x} {y-h} L{x+w} {y}Z" '
                f'fill="{col}" stroke="#ecffff" stroke-width="2.7" opacity=".88"/>'
                f'<path d="M{x} {y-h} L{x+w} {y} L{x+2} {y-14}Z" '
                'fill="#4657ba" opacity=".34"/>'
                f'<path d="M{x} {y-h+7} L{x-2} {y-18}" '
                'stroke="#ffffff" stroke-width="2" opacity=".66"/>')
        for n in range(16):
            x=283+(n*53)%172
            y=230+(n*79)%290
            add(f'<path d="M{x-3} {y} h6 M{x} {y-3} v6" '
                'stroke="#f9ffff" stroke-width="1.7" opacity=".68"/>')

    elif index == 3:
        # Lava peaks — descending magma ribbons and incandescent rock rims.
        for x,y,h in ((320,333,254),(360,336,284),(421,337,229),(49,512,161)):
            add(f'<path d="M{x} {y} Q{x-11} {y+h*.45:.1f} '
                f'{x-4} {y+h}" stroke="#8d2413" stroke-width="18" '
                'stroke-linecap="round" fill="none" opacity=".75"/>'
                f'<path d="M{x} {y} Q{x-12} {y+h*.45:.1f} '
                f'{x-4} {y+h}" stroke="#ff8a17" stroke-width="11" '
                'stroke-linecap="round" fill="none"/>'
                f'<path d="M{x+2} {y+7} Q{x-9} {y+h*.45:.1f} '
                f'{x-3} {y+h-14}" stroke="#fff18a" stroke-width="3.6" '
                'stroke-linecap="round" fill="none" opacity=".85"/>')
        for n in range(27):
            x=280+(n*61)%180; y=140+(n*83)%463
            add(f'<circle cx="{x}" cy="{y}" r="{1+(n%3)*.8:.1f}" '
                'fill="#ffcd65" opacity=".81"/>')

    elif index == 4:
        # Heavenly towers — gold-capped marble columns, bridges and halo.
        for x,y,scale in ((316,319,.76),(416,307,.85),(46,505,.55)):
            add(f'<g transform="translate({x} {y}) scale({scale})">'
                '<path d="M-29 0 V-78 H-17 V-3 M-6 -2 V-97 H7 V-2 '
                'M18 -2 V-82 H30 V0" stroke="#fff9e6" stroke-width="13" fill="none"/>'
                '<path d="M-34 -87 H35 M-40 -2 H39" stroke="url(#story-gold)" '
                'stroke-width="11" fill="none"/>'
                '<path d="M-27 -87 L0 -113 L30 -87Z" fill="#d29e4b" '
                'stroke="#fff5d0" stroke-width="4"/>'
                '</g>')
        add('<path d="M290 332 Q344 288 415 327" fill="none" '
            'stroke="#fff5ce" stroke-width="8" opacity=".90"/>'
            '<path d="M290 331 Q344 307 415 326" fill="none" '
            'stroke="#8ac9e2" stroke-width="3" opacity=".76"/>')
        for x in (299,318,337,357,376,396):
            add(f'<path d="M{x} 313 v-12" fill="none" stroke="#fce5ab" '
                'stroke-width="2.4" opacity=".85"/>')

    elif index == 5:
        # Moonlit castles — extra Gothic roofs, candle windows and starbursts.
        add('<circle cx="372" cy="167" r="73" fill="#f4f7ff" opacity=".16"/>'
            '<circle cx="372" cy="167" r="51" fill="#e0edff" opacity=".30"/>')
        for x,y,h in ((310,330,92),(344,328,126),(390,324,147),
                      (421,332,107),(72,500,89)):
            add(f'<path d="M{x-10} {y} V{y-h+22} L{x} {y-h} '
                f'L{x+10} {y-h+22} V{y}Z" fill="#1d2c69" '
                'stroke="#6e81c2" stroke-width="2"/>'
                f'<path d="M{x-5} {y-26} v-12 q5 -11 10 0 v12Z" fill="#ffd274"/>'
                f'<path d="M{x-4} {y-55} v-10 q4 -8 8 0 v10Z" fill="#ffd57e"/>')
        for n in range(17):
            x=19+(n*89)%458; y=97+(n*57)%368
            add(f'<path d="M{x-4} {y} h8 M{x} {y-4} v8" '
                'stroke="#ffffff" stroke-width="1.3" opacity=".72"/>')

    elif index == 6:
        # Pearl sea — coral thickets, mother-of-pearl domes and swimming fish.
        for x,y,s in ((312,340,.76),(402,346,.95),(58,498,.65)):
            add(f'<g transform="translate({x} {y}) scale({s})">'
                '<path d="M-31 0 Q-35 -63 0 -69 Q36 -63 31 0Z" '
                'fill="#c7fff8" opacity=".72" stroke="#f1ffff" stroke-width="4"/>'
                '<path d="M-39 0 H39" stroke="#48e8f3" stroke-width="9"/>'
                '<path d="M-15 -36 Q0 -61 15 -36" fill="none" '
                'stroke="#fff8e6" stroke-width="5" opacity=".84"/>'
                '</g>')
        for n in range(36):
            x=35+(n*53)%425; y=195+(n*97)%460
            radius=2+(n%4)*2
            add(f'<circle cx="{x}" cy="{y}" r="{radius}" fill="none" '
                'stroke="#e6ffff" stroke-width="1.6" opacity=".55"/>')
        for x,y,dx in ((119,274,18),(397,183,-21),(271,530,23)):
            add(f'<path d="M{x} {y} q{dx} -9 {2*dx} 0 q{-dx} 10 {-2*dx} 0Z" '
                'fill="#90f1ed" opacity=".64"/>'
                f'<path d="M{x} {y} l{-dx/2:.1f} -8 l0 16Z" '
                'fill="#ffde80" opacity=".75"/>')

    elif index == 7:
        # Star abyss — vortex energy arcs, asteroid chips and violet dust.
        for r,rotation,opacity in ((105,-31,.48),(124,13,.35),(145,-14,.30)):
            add(f'<ellipse cx="371" cy="269" rx="{r}" ry="{r*.72:.0f}" '
                'fill="none" stroke="#ddafff" stroke-width="5" '
                f'opacity="{opacity}" transform="rotate({rotation} 371 269)"/>')
        for n in range(30):
            x=205+(n*113)%261; y=113+(n*67)%535
            rad=3+n%7
            add(f'<path d="M{x-rad} {y} L{x-2} {y-rad} '
                f'L{x+rad} {y-rad/2:.1f} L{x+rad/2:.1f} {y+rad}Z" '
                'fill="#b3a9ef" stroke="#6a63aa" stroke-width=".8" opacity=".53"/>')
        for n in range(15):
            x=19+(n*71)%460; y=121+(n*83)%514
            add(f'<circle cx="{x}" cy="{y}" r="{1+(n%3)*.7:.1f}" '
                'fill="#e1baff" opacity=".80"/>')

    # Low-alpha light motes introduce atmosphere without UI-like icons.
    if index != 3:
        for n in range(11):
            x=29+(n*103+index*17)%419
            y=124+(n*71+index*13)%537
            r=1.1+(n%3)*.8
            add(f'<circle cx="{x}" cy="{y}" r="{r:.1f}" '
                'fill="#ffffff" opacity=".49"/>')
    return "".join(p)


def foreground_cloud_bank(index: int) -> str:
    """Soft foreground clouds replacing the former flat ground-colored strip.

    This is static world illustration, behind all live gates and collectibles.
    Night, volcanic and underwater palettes preserve the biome atmosphere.
    """
    if index in (5, 7):
        top, middle, shadow = "#b5bdff", "#7469bc", "#30336e"
    elif index == 3:
        top, middle, shadow = "#ffe0b9", "#d67b8b", "#6c405d"
    elif index == 6:
        top, middle, shadow = "#dafff9", "#8fd6e8", "#3486b5"
    else:
        top, middle, shadow = "#ffffff", "#c7ecff", "#65b5e3"
    parts = [
        '<defs>'
        '<linearGradient id="cloud-bank" x1="0" y1="0" x2=".1" y2="1">'
        f'<stop stop-color="{top}"/><stop offset=".55" stop-color="{middle}"/>'
        f'<stop offset="1" stop-color="{shadow}"/></linearGradient>'
        '<radialGradient id="cloud-soft">'
        f'<stop stop-color="{top}" stop-opacity=".95"/>'
        f'<stop offset=".66" stop-color="{middle}" stop-opacity=".63"/>'
        f'<stop offset="1" stop-color="{middle}" stop-opacity="0"/>'
        '</radialGradient>'
        '</defs>',
        '<path d="M0 800 V772 Q39 735 87 760 Q142 710 196 751 '
        'Q253 721 304 758 Q371 709 431 748 Q463 738 480 759 '
        'V800Z" fill="url(#cloud-bank)" opacity=".82"/>',
    ]
    # Curves are deliberately irregular; a straight horizon implies a ground
    # plane in a world that should float entirely above the clouds.
    for n in range(8):
        x = -36 + n * 77 + (index * 13 + n * 17) % 26
        y = 764 + (n % 3) * 12
        width = 60 + (n % 4) * 9
        parts.append(
            f'<ellipse cx="{x}" cy="{y}" rx="{width}" ry="{26 + n%3*8}" '
            'fill="url(#cloud-soft)" opacity=".79"/>'
        )
    for n in range(5):
        x = 17 + n * 115
        y = 751 + (n % 2) * 11
        parts.append(
            f'<path d="M{x} {y} q22 -13 49 -4" fill="none" '
            f'stroke="{top}" stroke-width="2.5" opacity=".57" '
            'stroke-linecap="round"/>'
        )
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
    # Each world retains its original live collision and collectibles.
    add(storybook_landmarks(index))
    # The illustrated world has no solid ground; clouds visually support
    # floating cliffs without covering the collision corridor or UI.
    add(foreground_cloud_bank(index))
    add('</svg>')
    return "".join(items)

# Reference gallery index -> corresponding existing scenic source.
# Gameplay world IDs and save-key indices DO NOT change.
# The forest uses an original woodland overlay to avoid duplicate island art.
# Match *visual content* to the eight established world IDs, not just tint:
# 1 now has genuine crystal/snow spires; 6 has its own warm woodland
# baseline plus the enchanted-forest canopy, instead of duplicating world 0.
# This changes generated scenery only. Game/save world IDs remain unchanged.
WORLD_SCENE_SOURCE = (0, 2, 3, 5, 6, 4, 1, 7)
REFERENCE_WORLD_NAMES = (
    "Zeleni otoci", "Kristalno nebo", "Lava vrhovi", "Noćni dvorci",
    "Biserno more", "Nebeske kule", "Čarobna šuma", "Zvjezdani bezdani",
)


def enchanted_forest_overlay() -> str:
    """Add mystic trees, glowing mushrooms and fireflies as raster-only scenery.

    The collision zone and all collectible controls are separate live views.
    Forest elements line the image edges, not the central flight corridor.
    """
    artwork = [
        '<defs><radialGradient id="forest-light"><stop stop-color="#faffaa" '
        'stop-opacity=".9"/><stop offset="1" stop-color="#acff65" '
        'stop-opacity="0"/></radialGradient></defs>',
        '<g fill="none" stroke-linecap="round">',
        '<path d="M-30 755 Q30 590 31 428 Q34 375 73 339" stroke="#274d35" stroke-width="35"/>',
        '<path d="M480 755 Q431 606 447 425 Q455 362 407 310" stroke="#243d38" stroke-width="34"/>',
        '<path d="M29 497 Q75 458 106 402 M447 480 Q392 455 361 399" '
        'stroke="#386c38" stroke-width="17"/>',
        '<path d="M-30 755 Q30 590 31 428 M480 755 Q431 606 447 425" '
        'stroke="#9fcc64" stroke-width="5" opacity=".51"/>',
        '</g>',
    ]
    # Bracket the sky with foliage to preserve a clear flight lane.
    for side in (-1, 1):
        cx = 41 if side < 0 else 441
        for idx in range(4):
            cy = 344 + idx * 78
            rx = 64 - idx * 4
            artwork.append(
                f'<ellipse cx="{cx}" cy="{cy}" rx="{rx}" ry="{33+idx*3}" '
                f'fill="{("#3a9750" if idx%2 else "#62b64d")}" opacity=".73"/>'
            )
            artwork.append(
                f'<ellipse cx="{cx+side*16}" cy="{cy-10}" rx="{rx*.56}" ry="16" '
                'fill="#b6e978" opacity=".42"/>'
            )
    for n in range(17):
        px = 16 + (n * 73) % 452
        py = 228 + (n * 59) % 435
        radius = 7 + (n % 3) * 3
        artwork.append(f'<circle cx="{px}" cy="{py}" r="{radius}" fill="url(#forest-light)"/>')
        artwork.append(f'<circle cx="{px}" cy="{py}" r="2" fill="#f2ffc1" opacity=".93"/>')
    for cx,cy,sz in ((55,705,1),(108,723,.72),(408,706,1.05),(460,723,.65)):
        artwork.append(
            f'<g transform="translate({cx} {cy}) scale({sz})">'
            '<path d="M-4 0 V-17 H4 V0" fill="#e8e1b7"/>'
            '<path d="M-25 -16 Q0 -51 25 -16Z" fill="#f0a347" stroke="#ffde76" '
            'stroke-width="2"/><circle cx="-6" cy="-22" r="3" fill="#fff7b5"/>'
            '<circle cx="8" cy="-26" r="3" fill="#fff7b5"/></g>'
        )
    return "".join(artwork)


def generate_worlds(android_res:Path):
    android=android_res/"drawable-nodpi"
    android.mkdir(parents=True,exist_ok=True)
    for world in range(8):
        source_index=WORLD_SCENE_SOURCE[world]
        svg=shapes(source_index)
        if world==6:
            svg=svg.replace("</svg>",enchanted_forest_overlay()+"</svg>")
        png=cairosvg.svg2png(bytestring=svg.encode(),output_width=960,output_height=1600)
        im=Image.open(io.BytesIO(png)).convert("RGB")
        name=f"world{world}"
        im.save(android/(name+".png"),optimize=True)
    print("PASS: 8 reference-ordered world backdrops for Android")
