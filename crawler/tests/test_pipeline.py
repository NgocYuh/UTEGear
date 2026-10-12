from __future__ import annotations

import json
import sys
import tempfile
import unittest
from dataclasses import replace
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from cleaners.product_cleaner import ProductCleaner
from exporters.jsonl_exporter import JsonlExporter
from main import validate_source_url
from parsers.gearvn import GearvnProductParser
from pipeline import CrawlerPipeline
from validators.product_validator import ProductValidationError, ProductValidator


class CrawlerPipelineTest(unittest.TestCase):
    def setUp(self) -> None:
        self.html = (ROOT / "tests" / "fixtures" / "gearvn-product.html").read_text(encoding="utf-8")
        self.url = "https://gearvn.com/products/ban-phim-co-e-dra-ek384"
        self.pipeline = CrawlerPipeline(GearvnProductParser(), ProductCleaner(), ProductValidator())

    def test_parses_cleans_and_validates_fixed_html(self) -> None:
        result = self.pipeline.run([(self.url, self.html)])

        self.assertEqual([], result.errors)
        self.assertEqual(1, len(result.products))
        product = result.products[0]
        self.assertEqual("ban-phim", product.category)
        self.assertEqual("E-Dra", product.brand)
        self.assertEqual(1_290_000, product.base_price)
        self.assertEqual("EDRA-EK384-BROWN", product.variants[0].sku)
        self.assertTrue(product.variants[0].is_default)
        self.assertEqual({}, product.variants[0].option_values)

    def test_generates_stable_default_variant_when_source_has_no_sku(self) -> None:
        html = self.html.replace('"sku": "EDRA-EK384-BROWN",', "")
        first = self.pipeline.run([(self.url, html)]).products[0]
        second = self.pipeline.run([(self.url, html)]).products[0]

        self.assertEqual(first.variants[0].sku, second.variants[0].sku)
        self.assertTrue(first.variants[0].sku.startswith("GEARVN-"))
        self.assertEqual({}, first.variants[0].option_values)

    def test_invalid_product_is_reported_without_exporting_it(self) -> None:
        html = self.html.replace('"price": "1290000"', '"price": "0"')
        result = self.pipeline.run([(self.url, html)])

        self.assertEqual([], result.products)
        self.assertIn("price must be positive", result.errors[0].message)

    def test_duplicate_source_url_is_reported(self) -> None:
        result = self.pipeline.run([(self.url, self.html), (self.url, self.html)])

        self.assertEqual(1, len(result.products))
        self.assertEqual("duplicate source URL", result.errors[0].message)

    def test_validator_requires_one_default_variant(self) -> None:
        product = self.pipeline.run([(self.url, self.html)]).products[0]
        invalid_variant = replace(product.variants[0], is_default=False)

        with self.assertRaisesRegex(ProductValidationError, "exactly one default variant"):
            ProductValidator().validate(replace(product, variants=[invalid_variant]))

    def test_exports_utf8_json_lines(self) -> None:
        product = self.pipeline.run([(self.url, self.html)]).products[0]
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "products.jsonl"
            JsonlExporter().export([product], output)
            exported = json.loads(output.read_text(encoding="utf-8"))

        self.assertEqual("Bàn phím cơ E-Dra EK384 Triple Mode", exported["name"])
        self.assertEqual(self.url, exported["source_url"])

    def test_rejects_url_from_another_host(self) -> None:
        with self.assertRaisesRegex(SystemExit, "not allowed"):
            validate_source_url("gearvn", "https://example.com/products/copied-page")


if __name__ == "__main__":
    unittest.main()
