#!/usr/bin/env python3
"""Only two stable releases in CHANGELOG; handle immutable legacy title formats."""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools"))
from sync_release_docs import decode_pages, releases, render, section, three_action_version, three_action_catalog, three_action_documents


def record(tag, **opts):
    return dict(
        tag_name=tag,
        published_at="2026-10-20T00:00:00Z",
        draft=opts.get("draft", False),
        prerelease=opts.get("prerelease", False),
        body=f"## {tag} — Android (build 131)\n- Real verified update.",
    )


class DocsTests(unittest.TestCase):
    def test_two_latest_releases(self):
        data = [
            record("v1.0.0"), record("v1.0.2"), record("v1.0.1"),
            record("v0.2.99"), record("v1.0.3", draft=True),
        ]
        out = render(releases(data))
        self.assertEqual(out.count("## v"), 2)
        self.assertIn("## v1.0.2", out)
        self.assertIn("## v1.0.1", out)
        self.assertNotIn("## v1.0.3", out)

    def test_invalid_release_fails(self):
        with self.assertRaisesRegex(ValueError, "Duplicate"):
            releases([record("v1.0.0"), record("v1.0.0")])

    def test_historical_short_heading_uses_original_published_build_footer(self):
        item = record("v1.0.28")
        item["body"] = """## v1.0.28

- Croatian results and accessible navigation.

**Build 158 distribution:** unsigned APK and AAB.
"""
        rendered = section(item)
        self.assertIn("## v1.0.28 — Android (build 158)", rendered)
        self.assertIn("Croatian results and accessible navigation.", rendered)
        self.assertNotIn("distribution:", rendered)

    def test_github_crlf_legacy_release_notes_are_accepted(self):
        item = record("v1.0.28")
        item["body"] = (
            "## v1.0.28 \r\n\r\n"
            "- Published Croatian UI fixes.\r\n"
            "\r\n**Build 158 distribution:** immutable artifacts.\r\n"
        )
        rendered = section(item)
        self.assertIn("## v1.0.28 — Android (build 158)", rendered)
        self.assertIn("- Published Croatian UI fixes.", rendered)

    def test_short_heading_must_have_matching_tag_and_real_build_footer(self):
        for body in (
            "## v1.0.28\n- Correct button.\n",
            "## v1.0.28\n- Correct button.\n**Build unknown distribution:**",
            "## v1.0.27\n- Correct button.\n**Build 158 distribution:**",
            "## v1.0.28 — Android polish\n- Correct button.\n**Build 158 distribution:**",
        ):
            item = record("v1.0.28")
            item["body"] = body
            with self.assertRaisesRegex(ValueError, "Invalid published release notes"):
                section(item)

    def test_current_release_requires_build_in_title(self):
        item = record("v1.0.29")
        item["body"] = "## v1.0.29 — Changes\n- Verified build.\n**Build 159 distribution:**"
        with self.assertRaisesRegex(ValueError, "Invalid published release notes"):
            section(item)


    def test_new_published_three_action_docs_exclude_removed_routes(self):
        self.assertFalse(three_action_version("v1.0.83"))
        self.assertTrue(three_action_version("v1.0.84"))
        self.assertTrue(three_action_version("v1.0.85"))
        base={"screens":["home","flight","pause","results"],"features":[
            {"id":x,"status":"visible"} for x in (
                "home","flight","quick_options_overlay","local_records_overlay",
                "shop_screen","skin_selection_screen","settings_screen",
                "achievements_screen","records_screen","level_selection_screen",
                "virtual_wallet_save","no_ads","no_account","offline")
        ]}
        transformed=three_action_catalog(base)
        self.assertEqual(transformed["screens"],["home","skins","shop","settings"])
        self.assertEqual(transformed["home_navigation_decision"],"approved-three-actions")
        flags={f["id"]:f["status"] for f in transformed["features"]}
        for item in ("shop_screen","skin_selection_screen","settings_screen",
                     "three_home_actions","virtual_wallet_save"):
            self.assertEqual(flags[item],"visible")
        for item in ("quick_options_overlay","local_records_overlay",
                     "achievements_screen","progress_screen","leaderboard_screen",
                     "level_map_screen","records_screen"):
            self.assertEqual(flags[item],"removed")
        pages=three_action_documents("v1.0.84",214,transformed)
        self.assertEqual(len(pages),4)
        matrix=pages["docs/product/FEATURE-MATRIX.md"]
        self.assertIn('"screens": [',matrix)
        self.assertIn('"settings"',matrix)
        for name in ("Shop","Skins","Settings"):
            self.assertIn(name,pages["docs/product/CURRENT_SCREENS.md"])
        self.assertNotIn("BOPAVI-Android-Records.png",
                         pages["docs/product/CURRENT_SCREENS.md"])
        self.assertNotIn("glavni gumb IGRAJ",
                         pages["docs/play/STORE-LISTING-hr-HR.md"])
        self.assertIn("nema dostupnog gumba za pokretanje novog leta",
                      pages["docs/play/STORE-LISTING-hr-HR.md"])
        self.assertIn("build 214",pages["docs/play/RELEASE-CHECKLIST.md"])

    def test_paginated_arrays(self):
        import json
        self.assertEqual(len(decode_pages(
            json.dumps([record("v1.0.0")]) + "\n"
            + json.dumps([record("v1.0.1")])
        )), 2)


if __name__ == "__main__":
    unittest.main(verbosity=2)
