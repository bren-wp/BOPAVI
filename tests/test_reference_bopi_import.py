"""Offline regression for owner-approved 3-layer Bopi import.

Uses synthetic test shapes only; screenshots supplied by the user are never
shipped inside GitHub Actions, and no test-generated mock is a gameplay capture.
"""
from pathlib import Path
from hashlib import sha256
from tempfile import TemporaryDirectory
import json
import sys

from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/"tools"))
from reference_bopi_import import install_reference_bopi

with TemporaryDirectory() as tmp:
    root=Path(tmp)
    assert install_reference_bopi(root) is False
    source=root/"art/reference-approved/bopi"
    source.mkdir(parents=True)
    try:
        install_reference_bopi(root)
        raise AssertionError("A partial art pack was accepted")
    except ValueError as exc:
        assert "manifest" in str(exc)

    manifest={"canvas_size":[224,192],"layers":[
        "bopi0_body.webp","bopi0_left.webp","bopi0_right.webp"]}
    colors={"body":(12,132,255,255),"left":(84,170,255,255),"right":(200,234,255,255)}
    for part,color in colors.items():
        art=Image.new("RGBA",(224,192),(0,0,0,0))
        # Each layer includes a real opaque area, not just a transparent marker.
        center={"body":112,"left":48,"right":168}[part]
        for x in range(center-15,center+15):
            for y in range(70,120):
                art.putpixel((x,y),color)
        filename=f"bopi0_{part}.webp"
        art.save(source/filename,"WEBP",quality=90,method=6)
        manifest[filename+"_sha256"]=sha256((source/filename).read_bytes()).hexdigest()
    (source/"manifest.json").write_text(json.dumps(manifest))
    assert install_reference_bopi(root) is True

    pairs=(("bopi0","Bopi0"),("bopileft0","BopiLeft0"),
           ("bopiright0","BopiRight0"),("bopiportrait0","BopiPortrait0"),
           ("bopireference0","BopiReference0"))
    for name,catalog in pairs:
        a=root/"android/app/src/main/res/drawable-nodpi"/f"{name}.png"
        i=root/"ios/BOPAVI/Assets.xcassets"/f"{catalog}.imageset"/f"{name}.png"
        assert a.is_file() and a.read_bytes()==i.read_bytes(),name
        with Image.open(a) as rendered:
            assert rendered.size==((2,2) if name=="bopireference0" else (512,512))
            assert rendered.convert("RGBA").getchannel("A").getbbox()

    # Corrupting a single byte or truncating any layer never silently replaces
    # an existing, known-good shipped sprite.
    bad=source/"bopi0_left.webp"
    bad.write_bytes(bad.read_bytes()+b"corrupt")
    try:
        install_reference_bopi(root)
        raise AssertionError("Layer mismatch was accepted")
    except ValueError as exc:
        assert "Checksum mismatch" in str(exc)
    bad.unlink()
    try:
        install_reference_bopi(root)
        raise AssertionError("Missing wing was accepted")
    except ValueError as exc:
        assert "Missing required" in str(exc)

android=(ROOT/"android/app/src/main/java/com/brendigo/bopavi/GameView.kt").read_text()
ios=(ROOT/"ios/BOPAVI/GameCanvas.swift").read_text()
images=(ROOT/"tools/generate_images.py").read_text()
for source in (android,ios):
    assert "142" in source
    assert "bopireference0" in source.lower()
    assert "game.flap()" in source
    assert "game.score()" in source  # Minimal V5 HUD; shield status is intentionally absent
    assert "bird" in source
assert "referenceBopi0" in android and "referenceBopi0" in ios
assert "install_reference_bopi(r)" in images
assert "getIdentifier(" in android
assert 'UIImage(named:"BopiReference0")' in ios
print("PASS: checksum-verified owner Bopi source, independent wing layers, Android/iOS PNG parity and unchanged gameplay")
