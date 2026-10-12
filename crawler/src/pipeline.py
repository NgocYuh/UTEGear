"""Orchestrate parse, clean, and validate stages."""

from __future__ import annotations

from models import PipelineError, PipelineResult


class CrawlerPipeline:
    def __init__(self, parser, cleaner, validator) -> None:
        self.parser = parser
        self.cleaner = cleaner
        self.validator = validator

    def run(self, pages: list[tuple[str, str]]) -> PipelineResult:
        products = []
        errors = []
        seen_urls: set[str] = set()
        seen_skus: set[str] = set()
        for source_url, html in pages:
            try:
                if source_url in seen_urls:
                    raise ValueError("duplicate source URL")
                raw = self.parser.parse(html, source_url)
                product = self.cleaner.clean(raw)
                self.validator.validate(product)
                product_skus = {variant.sku for variant in product.variants}
                if product_skus & seen_skus:
                    raise ValueError("duplicate SKU across products")
                products.append(product)
                seen_urls.add(source_url)
                seen_skus.update(product_skus)
            except (TypeError, ValueError) as exception:
                errors.append(PipelineError(source_url, str(exception)))
        return PipelineResult(products, errors)
