"""Data structures shared by crawler pipeline stages."""

from __future__ import annotations

from dataclasses import asdict, dataclass, field
from typing import Any


@dataclass(frozen=True)
class RawOffer:
    sku: str | None
    name: str | None
    price: str | int | float | None


@dataclass(frozen=True)
class RawProduct:
    source_name: str
    source_url: str
    name: str | None
    description: str | None
    brand: str | None
    category: str | None
    images: list[str] = field(default_factory=list)
    offers: list[RawOffer] = field(default_factory=list)


@dataclass(frozen=True)
class ProductVariantRecord:
    sku: str
    option_values: dict[str, str]
    price: int
    is_default: bool


@dataclass(frozen=True)
class ProductRecord:
    source_name: str
    source_url: str
    crawled_at: str
    name: str
    description: str | None
    brand: str | None
    category: str
    base_price: int
    image_urls: list[str]
    variants: list[ProductVariantRecord]

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


@dataclass(frozen=True)
class PipelineError:
    source_url: str
    message: str


@dataclass(frozen=True)
class PipelineResult:
    products: list[ProductRecord]
    errors: list[PipelineError]
