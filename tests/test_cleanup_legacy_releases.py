#!/usr/bin/env python3
"""Regression tests: legacy-release retention can never delete v1+ releases."""
import importlib.util
import json
from pathlib import Path
import unittest
from unittest.mock import patch

PATH = Path(__file__).resolve().parents[1] / "tools/cleanup_legacy_releases.py"
spec = importlib.util.spec_from_file_location("cleanup_legacy_releases", PATH)
cleanup = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cleanup)


def release(tag, published="2026-10-19T00:00:00Z", **flags):
    return {"tag_name": tag, "published_at": published, "draft": False,
            "prerelease": False, "assets": [], **flags}


class RetentionTests(unittest.TestCase):
    def test_paginates_all_api_results(self):
        first = [release("v0.2.0"), release("v0.2.1")]
        second = [release("v0.2.2"), release("v0.2.3"), release("v1.0.0")]
        payload = json.dumps(first, indent=2) + "\n" + json.dumps(second)
        with patch.object(cleanup, "gh", return_value=payload) as mocked:
            records = cleanup.list_releases("bren-wp/BOPAVI")
        self.assertEqual(len(records), 5)
        mocked.assert_called_once()
        kept, removed = cleanup.plan_cleanup(records)
        self.assertEqual(removed, ["v0.2.0"])
        self.assertEqual(set(kept), {"v0.2.1", "v0.2.2", "v0.2.3"})

    def test_rejects_malformed_api_page(self):
        with patch.object(cleanup, "gh", return_value='{"unexpected": true}'):
            with self.assertRaisesRegex(ValueError, "array"):
                cleanup.list_releases("bren-wp/BOPAVI")

    def test_zero_through_four_legacy_releases(self):
        for count in range(10):
            records = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(count)]
            kept, removed = cleanup.plan_cleanup(records)
            self.assertEqual(len(kept), min(3, count))
            self.assertEqual(len(removed), max(0, count-3))
            self.assertTrue(set(kept).isdisjoint(removed))
            self.assertEqual(kept[:1], [f"v0.2.{count - 1}"] if count else [])

    def test_stable_release_is_never_deleted_at_any_version(self):
        records = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(6)]
        records.extend(release(f"v1.0.{n}", f"2026-10-{n+7:02d}T00:00:00Z") for n in range(8))
        records += [release("v1.1.0"), release("v2.0.0"), release("v10.7.5")]
        kept, removed = cleanup.plan_cleanup(records)
        self.assertEqual(removed, ["v0.2.0", "v0.2.1", "v0.2.2"])
        self.assertEqual(kept, ["v0.2.5", "v0.2.4", "v0.2.3"])
        self.assertTrue(all(cleanup.LEGACY.fullmatch(tag) for tag in removed))

    def test_never_prune_nonstandard_drafts_or_prereleases(self):
        records = [release(f"v0.2.{n}", f"2026-10-{n+1:02d}T00:00:00Z") for n in range(4)]
        records += [{"tag_name": "v0.2.99", "published_at": "2026-10-20T00:00:00Z",
                    "draft": True, "prerelease": False},
                    {"tag_name": "v0.2.98", "published_at": "2026-10-20T00:00:00Z",
                     "draft": False, "prerelease": True},
                    release("nightly-test"), release("v1.0.0")]
        kept, removed = cleanup.plan_cleanup(records)
        self.assertEqual(removed, ["v0.2.0"])
        self.assertNotIn("nightly-test", removed)
        self.assertNotIn("v1.0.0", removed)

    def test_fail_closed_when_api_returns_duplicates(self):
        with self.assertRaisesRegex(ValueError, "Duplicate"):
            cleanup.plan_cleanup([release("v0.2.0"), release("v0.2.0")])

    def test_minimum_three_legacy_releases(self):
        with self.assertRaises(ValueError):
            cleanup.plan_cleanup([release("v0.2.0")], keep_legacy=2)


if __name__ == "__main__":
    unittest.main(verbosity=2)
