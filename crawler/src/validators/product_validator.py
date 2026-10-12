"""Validation rules applied before crawler records are exported."""

from __future__ import annotations

from urllib.parse import urlsplit

from models import ProductRecord


class ProductValidationError(ValueError):
    pass


class ProductValidator:
    ALLOWED_CATEGORIES = {"ban-phim", "chuot", "tai-nghe", "keycap", "switch", "phu-kien"}

    def validate(self, product: ProductRecord) -> None:
        errors: list[str] = []
        if not product.name:
            errors.append("name is required")
        if product.category not in self.ALLOWED_CATEGORIES:
            errors.append("category is not supported")
        if not self._is_http_url(product.source_url):
            errors.append("sourceUrl must be an absolute HTTP(S) URL")
        if not product.variants:
            errors.append("at least one variant is required")
        if sum(variant.is_default for variant in product.variants) != 1:
            errors.append("exactly one default variant is required")
        skus = [variant.sku for variant in product.variants]
        if any(not sku for sku in skus) or len(skus) != len(set(skus)):
            errors.append("variant SKUs must be present and unique")
        if any(variant.price <= 0 for variant in product.variants):
            errors.append("variant price must be positive")
        if not product.image_urls or any(not self._is_http_url(url) for url in product.image_urls):
            errors.append("at least one valid image URL is required")
        if errors:
            raise ProductValidationError("; ".join(errors))

    def _is_http_url(self, value: str) -> bool:
        parsed = urlsplit(value)
        return parsed.scheme in {"http", "https"} and bool(parsed.netloc)
