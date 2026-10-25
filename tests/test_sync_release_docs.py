#!/usr/bin/env python3
"""Two stable-release policy; prereleases and legacy versions never enter CHANGELOG."""
import sys
import unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/"tools"))
from sync_release_docs import decode_pages,releases,render,section
def record(tag,**opts):
    return dict(tag_name=tag,published_at="2026-10-20T00:00:00Z",
                draft=opts.get("draft",False),prerelease=opts.get("prerelease",False),
                body=f"## {tag} — Android (build 131)\n- Real verified update.")
class DocsTests(unittest.TestCase):
    def test_two_latest_releases(self):
        data=[record("v1.0.0"),record("v1.0.2"),record("v1.0.1"),
              record("v0.2.99"),record("v1.0.3",draft=True)]
        out=render(releases(data))
        self.assertEqual(out.count("## v"),2)
        self.assertIn("## v1.0.2",out)
        self.assertIn("## v1.0.1",out)
        self.assertNotIn("## v1.0.3",out)
    def test_invalid_release_fails(self):
        with self.assertRaisesRegex(ValueError,"Duplicate"):
            releases([record("v1.0.0"),record("v1.0.0")])
    def test_historical_short_heading_uses_original_published_build_footer(self):
        item=record("v1.0.28")
        item["body"]=(
            "## v1.0.28 \\n\\n"
            "- Croatian results and accessible navigation.\\n"
            "\\n**Build 158 distribution:** unsigned APK and AAB.\\n"
        ).replace("\\\\n","\\n")
        rendered=section(item)
        self.assertIn("## v1.0.28 — Android (build 158)",rendered)
        self.assertIn("Croatian results and accessible navigation.",rendered)
        self.assertNotIn("distribution:",rendered)

    def test_short_heading_must_have_matching_tag_and_real_build_footer(self):
        for body in (
            "## v1.0.28\\n- Correct button.\\n",
            "## v1.0.28\\n- Correct button.\\n**Build unknown distribution:**",
            "## v1.0.27\\n- Correct button.\\n**Build 158 distribution:**",
            "## v1.0.28 — Android polish\\n- Correct button.\\n**Build 158 distribution:**",
        ):
            item=record("v1.0.28");item["body"]=body
            with self.assertRaisesRegex(ValueError,"Invalid published release notes"):
                section(item)

    def test_paginated_arrays(self):
        import json
        self.assertEqual(len(decode_pages(json.dumps([record("v1.0.0")])+"\\n"+json.dumps([record("v1.0.1")]))),2)
if __name__=="__main__":unittest.main(verbosity=2)
