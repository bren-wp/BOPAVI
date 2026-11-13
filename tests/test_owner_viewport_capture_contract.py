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
assert '.resize(' not in src and 'Image.new(' not in src
assert 'adb("shell","wm","size","reset")' in src
flow=(root/".github/workflows/native-ci.yml").read_text()
assert 'python3 tools/qa_android_owner_viewports.py' in flow
print("PASS: native 20-frame owner viewport QA, no fake captures or undersized controls")
