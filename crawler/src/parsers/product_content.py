"""Read product copy and specifications from public HTML, without executing scripts."""
from __future__ import annotations

import json
import re
from html.parser import HTMLParser


class _Text(HTMLParser):
    BLOCKS = {"p", "li", "h1", "h2", "h3", "h4", "tr", "br", "div"}

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.parts = []
        self.ignored = 0

    def handle_starttag(self, tag, attrs):
        if tag in {"script", "style", "svg"}:
            self.ignored += 1
        if not self.ignored and tag in self.BLOCKS:
            self.parts.append("\n")
        if not self.ignored and tag in {"td", "th"}:
            self.parts.append(" ")

    def handle_endtag(self, tag):
        if tag in {"script", "style", "svg"}:
            self.ignored = max(0, self.ignored - 1)
        if not self.ignored and tag in self.BLOCKS:
            self.parts.append("\n")

    def handle_data(self, data):
        if not self.ignored:
            self.parts.append(data)

    def text(self):
        return "\n\n".join(" ".join(line.split()) for line in "".join(self.parts).splitlines() if line.strip())


def plain(value):
    parser = _Text()
    parser.feed(str(value or ""))
    return parser.text()


def _flight(html):
    chunks = []
    for match in re.finditer(r'self\.__next_f\.push\((\[.*?\])\)</script>', html, re.DOTALL):
        try:
            value = json.loads(match.group(1))
            if len(value) > 1 and value[0] == 1 and isinstance(value[1], str):
                chunks.append(value[1])
        except (ValueError, TypeError):
            continue
    return "".join(chunks)


def _rte(html):
    match = re.search(r'<div\b[^>]*class=["\'][^"\']*\brte\b[^"\']*["\'][^>]*>', html, re.I)
    if not match:
        return ""
    depth, end = 1, match.end()
    for token in re.finditer(r'</?div\b[^>]*>', html[match.end():], re.I):
        depth += -1 if token.group(0).startswith("</") else 1
        if depth == 0:
            end = match.end() + token.start()
            return html[match.end():end]
    return ""


def read_product_content(html, product):
    specs = {}
    for prop in product.get("additionalProperty") or []:
        if isinstance(prop, dict) and prop.get("name") and prop.get("value"):
            specs[plain(prop["name"])] = plain(prop["value"])
    stream = _flight(html)
    for match in re.finditer(r'"product"\s*:', stream):
        try:
            candidate, _ = json.JSONDecoder().raw_decode(stream[match.end():].lstrip())
            if not isinstance(candidate, dict) or "name" not in candidate:
                continue
            if candidate.get("name", "").strip().casefold() != str(product.get("name", "")).strip().casefold():
                continue
            for group in candidate.get("specs_table") or []:
                for row in group.get("rows") or []:
                    if row.get("label") and row.get("value"):
                        specs[plain(row["label"])] = plain(row["value"])
        except (ValueError, TypeError, AttributeError):
            continue

    body = ""
    field = re.search(r'"descriptionBody"\s*:', stream)
    if field:
        try:
            value, _ = json.JSONDecoder().raw_decode(stream[field.end():].lstrip())
            if isinstance(value, str) and not value.startswith("$"):
                body = value
            elif isinstance(value, str) and re.fullmatch(r'\$[0-9a-f]+', value):
                record = re.search(r'(?:^|\n|(?<=>))' + value[1:] + r':', stream)
                if record:
                    remainder = stream[record.end():]
                    text_header = re.match(r'T([0-9a-f]+),', remainder)
                    if text_header and int(text_header.group(1), 16) <= 1_000_000:
                        body = remainder[text_header.end():].encode("utf-8")[:int(text_header.group(1), 16)].decode("utf-8", errors="replace")
                    elif not text_header:
                        decoded, _ = json.JSONDecoder().raw_decode(remainder)
                        if isinstance(decoded, str):
                            body = decoded
        except (ValueError, TypeError):
            pass
    if not body:
        body = _rte(html)

    # Read table pairs and labelled paragraphs only inside the product description.
    for row in re.findall(r'<tr\b[^>]*>(.*?)</tr>', body, re.I | re.DOTALL):
        cells = re.findall(r'<t[dh]\b[^>]*>(.*?)</t[dh]>', row, re.I | re.DOTALL)
        if len(cells) == 2:
            key, value = (plain(cell) for cell in cells)
            if key and value and len(key) <= 200:
                specs.setdefault(key.rstrip(":"), value)
    content = plain(body) or plain(product.get("description"))
    paragraphs = []
    for part in content.split("\n\n"):
        # Exclude source-store adverts, shipping instructions and purchase calls.
        sentences = re.split(r'(?<=[.!?])\s+', part)
        part = " ".join(s for s in sentences if not re.search(r'gearvn|kicap|hotline|mua ngay|đặt hàng|rất cảm ơn', s, re.I))
        if not part:
            continue
        if part and part not in paragraphs:
            paragraphs.append(part)
        if ":" in part:
            key, value = part.lstrip("-• ").split(":", 1)
            if 1 < len(key.strip()) <= 80 and value.strip():
                specs.setdefault(key.strip(), value.strip())
    bullets = [p.lstrip("-•✅✔️ ") for p in paragraphs if p.startswith(("-", "•", "✅", "✔"))]
    if not bullets and not specs:
        bullets = [p for p in paragraphs if re.search(r'\b(?:PBT|ABS|Resin|profile|layout|doubleshot)\b|chất liệu|kích thước|xuyên led', p, re.I)]
    if not bullets and not specs:
        bullets = [s for p in paragraphs for s in re.split(r'(?<=[.!?])\s+', p)
                   if re.search(r'\b(?:USB|Bluetooth|ENC|RGB|condenser|cardioid|Windows|Android)\b|\d\s*(?:mm|[kK]?[hH]z|bit|giờ|mét)\b', s)]
    if bullets and not specs:
        specs["Đặc điểm theo nguồn"] = "; ".join(bullets)
    specs = {k[:200]: v[:4000] for k, v in specs.items() if k and v}
    if not paragraphs and specs:
        paragraphs = [str(product.get("name", "")).strip()] + [f"{key}: {value}" for key, value in specs.items()]
    # Leave absent source specifications absent; never invent dimensions or capabilities.
    return "\n\n".join(paragraphs)[:12000], specs
