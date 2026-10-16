"""GEARVN product parser based on public schema.org JSON-LD."""

from __future__ import annotations

import json
from html.parser import HTMLParser
from typing import Any

from models import RawOffer, RawProduct
from parsers.product_content import read_product_content


class _JsonLdCollector(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.documents: list[str] = []
        self._capturing = False
        self._chunks: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        attributes = dict(attrs)
        if tag.lower() == "script" and attributes.get("type", "").lower() == "application/ld+json":
            self._capturing = True
            self._chunks = []

    def handle_data(self, data: str) -> None:
        if self._capturing:
            self._chunks.append(data)

    def handle_endtag(self, tag: str) -> None:
        if tag.lower() == "script" and self._capturing:
            self.documents.append("".join(self._chunks))
            self._capturing = False
            self._chunks = []


class GearvnProductParser:
    source_name = "gearvn"

    def parse(self, html: str, source_url: str) -> RawProduct:
        collector = _JsonLdCollector()
        collector.feed(html)
        product = self._find_product(collector.documents)
        if product is None:
            raise ValueError("No schema.org Product JSON-LD found")

        images = product.get("image") or []
        if isinstance(images, str):
            images = [images]
        brand_value = product.get("brand")
        brand = brand_value.get("name") if isinstance(brand_value, dict) else brand_value
        description, specifications = read_product_content(html, product)
        return RawProduct(
            source_name=self.source_name,
            source_url=source_url,
            name=product.get("name"),
            description=description,
            brand=brand,
            category=product.get("category"),
            images=[str(image) for image in images if image],
            offers=self._parse_offers(product),
            specifications=specifications,
        )

    def _find_product(self, documents: list[str]) -> dict[str, Any] | None:
        for document in documents:
            try:
                decoded = json.loads(document)
            except json.JSONDecodeError:
                continue
            for item in self._flatten(decoded):
                item_type = item.get("@type")
                types = item_type if isinstance(item_type, list) else [item_type]
                if "Product" in types:
                    return item
        return None

    def _flatten(self, value: Any) -> list[dict[str, Any]]:
        if isinstance(value, list):
            return [item for entry in value for item in self._flatten(entry)]
        if not isinstance(value, dict):
            return []
        graph = value.get("@graph")
        return self._flatten(graph) if graph is not None else [value]

    def _parse_offers(self, product: dict[str, Any]) -> list[RawOffer]:
        offers = product.get("offers") or []
        if isinstance(offers, dict):
            offers = [offers]
        parsed: list[RawOffer] = []
        for offer in offers:
            if not isinstance(offer, dict):
                continue
            parsed.append(RawOffer(
                sku=offer.get("sku") or product.get("sku"),
                name=offer.get("name"),
                price=offer.get("price") or offer.get("lowPrice"),
            ))
        if not parsed:
            parsed.append(RawOffer(product.get("sku"), None, None))
        return parsed
