"""Normalize source data into the UTEGear crawler contract."""

from __future__ import annotations

import hashlib
import re
from datetime import datetime, timezone
from decimal import Decimal, InvalidOperation

from models import ProductRecord, ProductVariantRecord, RawProduct


class ProductCleaner:
    CATEGORY_KEYWORDS = (
        ("bàn phím", "ban-phim"),
        ("keyboard", "ban-phim"),
        ("chuột", "chuot"),
        ("mouse", "chuot"),
        ("tai nghe", "tai-nghe"),
        ("headset", "tai-nghe"),
        ("keycap", "keycap"),
        ("switch", "switch"),
    )

    def clean(self, raw: RawProduct) -> ProductRecord:
        name = self._text(raw.name) or ""
        category = self._category(raw.category, name)
        variants: list[ProductVariantRecord] = []
        for index, offer in enumerate(raw.offers):
            price = self._price(offer.price)
            sku = self._text(offer.sku) or self._generated_sku(raw.source_name, raw.source_url, index)
            option_name = self._text(offer.name)
            option_values = {"sourceOption": option_name} if option_name and len(raw.offers) > 1 else {}
            variants.append(ProductVariantRecord(sku, option_values, price, index == 0))

        return ProductRecord(
            source_name=raw.source_name,
            source_url=raw.source_url,
            crawled_at=datetime.now(timezone.utc).isoformat(),
            name=name,
            description=self._text(raw.description),
            brand=self._text(raw.brand),
            category=category,
            base_price=min((variant.price for variant in variants), default=0),
            image_urls=list(dict.fromkeys(url.strip() for url in raw.images if url and url.strip())),
            variants=variants,
        )

    def _category(self, source_category: str | None, name: str) -> str:
        value = f"{source_category or ''} {name}".lower()
        for keyword, category in self.CATEGORY_KEYWORDS:
            if keyword in value:
                return category
        return "phu-kien"

    def _price(self, value: str | int | float | None) -> int:
        if value is None:
            return 0
        normalized = re.sub(r"[^0-9.,-]", "", str(value)).replace(",", "")
        try:
            return int(Decimal(normalized))
        except (InvalidOperation, ValueError):
            return 0

    def _text(self, value: object | None) -> str | None:
        if value is None:
            return None
        normalized = " ".join(str(value).split())
        return normalized or None

    def _generated_sku(self, source_name: str, source_url: str, index: int) -> str:
        digest = hashlib.sha256(f"{source_url}#{index}".encode()).hexdigest()[:12].upper()
        return f"{source_name.upper()}-{digest}"
