"""Command-line entry point for the finite UTEGear product crawler."""

from __future__ import annotations

import argparse
import os
from pathlib import Path
from urllib.parse import urlsplit

from cleaners.product_cleaner import ProductCleaner
from crawlers.http_client import RespectfulHttpClient
from exporters.jsonl_exporter import JsonlExporter
from parsers.gearvn import GearvnProductParser
from pipeline import CrawlerPipeline
from validators.product_validator import ProductValidator


SOURCE_HOSTS = {
    "gearvn": {"gearvn.com", "www.gearvn.com"},
}


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Prepare validated UTEGear product seed data")
    parser.add_argument("--source", choices=("gearvn",), default="gearvn")
    parser.add_argument("--url", action="append", default=[], help="Public product URL to fetch")
    parser.add_argument("--html-file", type=Path, help="Parse a saved HTML file without network access")
    parser.add_argument("--source-url", help="Original URL represented by --html-file")
    parser.add_argument("--allow-network", action="store_true", help="Explicitly allow live HTTP requests")
    parser.add_argument(
        "--output",
        type=Path,
        default=Path(os.getenv("CRAWLER_OUTPUT_DIR", "data/output")) / "products.jsonl",
    )
    return parser


def main() -> int:
    args = build_parser().parse_args()
    if args.url and not args.allow_network:
        raise SystemExit("Live URLs require --allow-network")
    if args.html_file and not args.source_url:
        raise SystemExit("--source-url is required with --html-file")
    if not args.url and not args.html_file:
        raise SystemExit("Provide --html-file or at least one --url")
    source_urls = [*args.url, *([args.source_url] if args.source_url else [])]
    for source_url in source_urls:
        validate_source_url(args.source, source_url)

    pipeline = CrawlerPipeline(
        parser=GearvnProductParser(),
        cleaner=ProductCleaner(),
        validator=ProductValidator(),
    )
    pages: list[tuple[str, str]] = []
    if args.html_file:
        pages.append((args.source_url, args.html_file.read_text(encoding="utf-8")))

    if args.url:
        delay = float(os.getenv("CRAWLER_REQUEST_DELAY_SECONDS", "2"))
        user_agent = os.getenv("CRAWLER_USER_AGENT", "UTEGearInitialDataCrawler/0.1")
        client = RespectfulHttpClient(user_agent=user_agent, delay_seconds=delay)
        pages.extend((url, client.get(url)) for url in args.url)

    result = pipeline.run(pages)
    for error in result.errors:
        print(f"SKIP {error.source_url}: {error.message}")
    if not result.products:
        print("No valid products were exported.")
        return 1

    JsonlExporter().export(result.products, args.output)
    print(f"Exported {len(result.products)} product(s) to {args.output}")
    return 0 if not result.errors else 2


def validate_source_url(source: str, source_url: str) -> None:
    host = (urlsplit(source_url).hostname or "").lower()
    if host not in SOURCE_HOSTS[source]:
        raise SystemExit(f"URL host is not allowed for source {source}: {host or 'missing'}")


if __name__ == "__main__":
    raise SystemExit(main())
