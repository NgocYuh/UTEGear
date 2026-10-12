"""Rate-limited HTTP client that checks robots.txt before fetching a page."""

from __future__ import annotations

import time
from urllib.parse import urlsplit, urlunsplit
from urllib.request import Request, urlopen
from urllib.robotparser import RobotFileParser


class CrawlNotAllowedError(RuntimeError):
    pass


class RespectfulHttpClient:
    def __init__(self, user_agent: str, delay_seconds: float = 2, timeout_seconds: float = 15):
        if delay_seconds < 0:
            raise ValueError("delay_seconds must not be negative")
        self.user_agent = user_agent
        self.delay_seconds = delay_seconds
        self.timeout_seconds = timeout_seconds
        self._last_request_at: float | None = None
        self._robots: dict[str, RobotFileParser] = {}

    def get(self, url: str) -> str:
        robots = self._robots_for(url)
        if not robots.can_fetch(self.user_agent, url):
            raise CrawlNotAllowedError(f"robots.txt does not allow {url}")
        self._wait_for_rate_limit()
        request = Request(url, headers={"User-Agent": self.user_agent, "Accept": "text/html"})
        with urlopen(request, timeout=self.timeout_seconds) as response:
            content_type = response.headers.get_content_type()
            if content_type != "text/html":
                raise ValueError(f"Expected text/html but received {content_type}")
            charset = response.headers.get_content_charset() or "utf-8"
            body = response.read().decode(charset, errors="replace")
        self._last_request_at = time.monotonic()
        return body

    def _robots_for(self, url: str) -> RobotFileParser:
        parts = urlsplit(url)
        if parts.scheme not in {"http", "https"} or not parts.netloc:
            raise ValueError("Only absolute HTTP(S) URLs can be crawled")
        origin = urlunsplit((parts.scheme, parts.netloc, "", "", ""))
        if origin not in self._robots:
            parser = RobotFileParser(f"{origin}/robots.txt")
            parser.read()
            self._robots[origin] = parser
        return self._robots[origin]

    def _wait_for_rate_limit(self) -> None:
        if self._last_request_at is None:
            return
        remaining = self.delay_seconds - (time.monotonic() - self._last_request_at)
        if remaining > 0:
            time.sleep(remaining)
