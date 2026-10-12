"""UTF-8 JSON Lines exporter for validated product records."""

from __future__ import annotations

import json
from pathlib import Path

from models import ProductRecord


class JsonlExporter:
    def export(self, products: list[ProductRecord], output_path: Path) -> None:
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with output_path.open("w", encoding="utf-8", newline="\n") as output:
            for product in products:
                output.write(json.dumps(product.to_dict(), ensure_ascii=False, separators=(",", ":")))
                output.write("\n")
