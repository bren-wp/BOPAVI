#!/usr/bin/env python3
"""Five supplied device canvases must be captured without synthetic resizing."""
from pathlib import Path
import ast
import sys
root=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(root/"tools"))
import qa_android_owner_viewports as native
assert len(native.VIEWPORTS)==5
assert {name for name,_,_,_ in native.VIEWPORTS} == {
    "Android_16_9_1080x1920","Android_FHD_1080x2280",
    "Android_FHD_Plus_1080x2340","Android_20_9_1080x2400",
    "Android_QHD_1440x3040"
}
assert {route for route,_ in native.DESTINATIONS}=={"Skins","Shop","Settings"}
src=(root/"tools/qa_android_owner_viewports.py").read_text()
assert 'actual=screenshot(name)' in src
assert 'assert actual==(width,height)' in src
assert 'min(right-left,bottom-top)>=minimum-1' in src
assert 'wait_home(width,height,density)' in src
assert 'open_route(route)' in src
assert '10_Politika_privatnosti' in src
assert 'tap(node_with("Pročitaj politiku privatnosti",attempts=10))' in src
assert '"ZATVORI" in label(n)' in src
assert 'capture(f"owner-{folder}-10_Politika_privatnosti",' in src
assert '45 authentic native screenshots' in src
assert 'owner-{folder}-05_Kraj_igre' in src
assert 'tap(pause)' in src and 'tap(resumed)' in src
assert 'Real Game Over not reached via natural GameSimulation collision' in src
assert 'len(retry)==1 and len(home_buttons)==1' in src
assert 'time.sleep(2)' in src
assert 'def real_flight_screens(folder:str,width:int,height:int)' in src
for stem in ("02_Gameplay_upute","03_Gameplay_let","04_Gameplay_pauza"):
    assert stem in src,stem
assert '"bopavi_qa_launch_flight","true"' in src
assert 'tap(ready)' in src and 'tap(retry)' in src
assert 'assert term in label(active)' in src
assert 'Duplicated native pause continuation' in src
assert '.resize(' not in src and 'Image.new(' not in src
assert 'adb("shell","wm","size","reset")' in src
flow=(root/".github/workflows/native-ci.yml").read_text()
assert 'python3 tools/qa_android_owner_viewports.py' in flow
print("PASS: native 45-frame owner viewport QA, Game Over from natural collision")
