#!/usr/bin/env python3
"""Build the full Car Spotter database from netcarshow.com's public catalog.

Walks the make index and each make page, extracts every model and its press
years, merges the result into the app's cars.json (existing curated entries
are kept untouched, including their bundled photos), and resolves a Wikipedia
thumbnail URL for each newly added model so the app can show photos over the
network.

Runs in CI (the dev container has no access to these hosts). Respects
robots.txt and paces its requests.
"""

import json
import re
import sys
import time
import urllib.parse
from html import unescape
from pathlib import Path

import requests

ROOT = Path(__file__).resolve().parent.parent
CARS_JSON = ROOT / "app" / "src" / "main" / "assets" / "cars.json"

BASE = "https://www.netcarshow.com"
WIKI_API = "https://en.wikipedia.org/w/api.php"
HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/126.0 Safari/537.36 CarSpotterCatalogBot/1.0 "
        "(+https://github.com/pftttt123/plugin)"
    ),
    "Accept-Language": "en",
}
PAGE_DELAY_S = 0.5
NON_MAKE_SLUGS = {
    "news", "videos", "video", "wallpapers", "wallpaper", "about", "contact",
    "privacy", "search", "sitemap", "cookies", "terms", "login", "register",
    "rss", "feed", "tags", "brands", "help", "faq", "press", "legal",
}
MAX_MAKES = 400

TAG_RE = re.compile(r"<[^>]+>")
MODEL_LINK_RE = re.compile(r'<a[^>]+href="/(?P<slug>[a-z0-9_%-]+)/(?P<year>(?:19|20)\d{2})-(?P<model>[a-z0-9_%.-]+)/?"[^>]*>(?P<text>.*?)</a>', re.I | re.S)
MAKE_LINK_RE = re.compile(r'<a[^>]+href="/(?P<slug>[a-z][a-z0-9_-]{1,40})/?"[^>]*>(?P<text>.*?)</a>', re.I | re.S)


def get(session, url, tries=5):
    last = None
    for attempt in range(tries):
        try:
            resp = session.get(url, timeout=45)
        except requests.RequestException as exc:
            last = exc
            time.sleep(min(30, 2 ** attempt))
            continue
        if resp.status_code in (429, 503):
            retry_after = resp.headers.get("Retry-After", "")
            wait = int(retry_after) if retry_after.isdigit() else min(60, 5 * 2 ** attempt)
            print(f"  {resp.status_code} for {url}; waiting {wait}s", flush=True)
            time.sleep(wait)
            last = requests.HTTPError(f"{resp.status_code} for {url}")
            continue
        resp.raise_for_status()
        return resp
    raise last if last else RuntimeError(f"failed: {url}")


def disallowed_prefixes(session):
    try:
        text = get(session, f"{BASE}/robots.txt").text
    except Exception:
        return []
    prefixes, applies = [], False
    for line in text.splitlines():
        line = line.split("#", 1)[0].strip()
        if not line or ":" not in line:
            continue
        field, value = (part.strip() for part in line.split(":", 1))
        field = field.lower()
        if field == "user-agent":
            applies = value == "*"
        elif field == "disallow" and applies and value:
            prefixes.append(value)
    return prefixes


def clean_text(html_fragment):
    return unescape(TAG_RE.sub(" ", html_fragment)).strip()


def name_from_slug(slug):
    words = []
    for token in urllib.parse.unquote(slug).replace("_", "-").split("-"):
        if not token:
            continue
        if any(ch.isdigit() for ch in token) or len(token) <= 3:
            words.append(token.upper())
        else:
            words.append(token.capitalize())
    return " ".join(words)


def clean_model_name(raw, make_name, year):
    name = re.sub(r"\(\s*%s\s*\)" % year, "", raw).strip()
    name = re.sub(r"^\s*%s\b" % re.escape(make_name), "", name, flags=re.I).strip()
    name = re.sub(r"\b(19|20)\d{2}\b", "", name).strip(" -–—")
    name = re.sub(r"\s{2,}", " ", name)
    return name


def slugify(text):
    return re.sub(r"-{2,}", "-", re.sub(r"[^a-z0-9]+", "-", text.lower())).strip("-")


def scrape_catalog(session):
    home_html = get(session, BASE + "/").text
    candidates = {}
    for m in MAKE_LINK_RE.finditer(home_html):
        slug = m.group("slug").lower().strip("/")
        if "/" in slug or slug in NON_MAKE_SLUGS:
            continue
        text = clean_text(m.group("text"))
        if slug not in candidates or (text and not candidates[slug]):
            candidates[slug] = text
    print(f"Found {len(candidates)} candidate make pages on the index")

    blocked = disallowed_prefixes(session)

    def allowed(path):
        return not any(path.startswith(p) for p in blocked)

    if not allowed("/"):
        print("robots.txt disallows crawling; aborting.", file=sys.stderr)
        sys.exit(1)

    catalog = {}
    for i, (slug, link_text) in enumerate(sorted(candidates.items())[:MAX_MAKES], 1):
        path = f"/{slug}/"
        if not allowed(path):
            continue
        time.sleep(PAGE_DELAY_S)
        try:
            page = get(session, BASE + path).text
        except Exception as exc:
            print(f"  skip {slug}: {type(exc).__name__}: {exc}")
            continue
        make_name = link_text or name_from_slug(slug)
        models = {}
        for m in MODEL_LINK_RE.finditer(page):
            if m.group("slug").lower() != slug:
                continue
            year = int(m.group("year"))
            raw_text = clean_text(m.group("text"))
            name = clean_model_name(raw_text, make_name, year) if raw_text else ""
            if not name:
                name = name_from_slug(m.group("model"))
            key = name.lower()
            entry = models.setdefault(key, {"name": name, "years": set()})
            entry["years"].add(year)
        if models:
            catalog[make_name] = models
            if i % 25 == 0:
                print(f"  {i}/{len(candidates)} pages scanned, {sum(len(v) for v in catalog.values())} models so far", flush=True)
    return catalog


def resolve_wiki_images(session, titles):
    thumbs = {}
    for start in range(0, len(titles), 50):
        batch = titles[start : start + 50]
        try:
            resp = get(
                session,
                WIKI_API
                + "?"
                + urllib.parse.urlencode(
                    {
                        "action": "query",
                        "format": "json",
                        "prop": "pageimages",
                        "piprop": "thumbnail",
                        "pithumbsize": 640,
                        "redirects": 1,
                        "titles": "|".join(batch),
                    }
                ),
            )
        except Exception as exc:
            print(f"  wiki batch failed: {exc}")
            continue
        data = resp.json().get("query", {})
        forward = {}
        for step in ("normalized", "redirects"):
            for entry in data.get(step, []):
                forward[entry["from"]] = entry["to"]

        def final_title(title):
            seen = set()
            while title in forward and title not in seen:
                seen.add(title)
                title = forward[title]
            return title

        by_title = {
            page.get("title"): page.get("thumbnail", {}).get("source")
            for page in data.get("pages", {}).values()
        }
        for requested in batch:
            thumbs[requested] = by_title.get(final_title(requested))
        time.sleep(1)
    return thumbs


def main():
    existing = json.loads(CARS_JSON.read_text(encoding="utf-8"))
    curated_keys = {
        re.sub(r"[^a-z0-9]", "", (c["make"] + c["model"]).lower()) for c in existing
    }
    used_ids = {c["id"] for c in existing}

    session = requests.Session()
    session.headers.update(HEADERS)

    catalog = scrape_catalog(session)
    total_models = sum(len(v) for v in catalog.values())
    print(f"\nScraped {total_models} models across {len(catalog)} makes")
    if total_models < 100:
        print("Suspiciously few models scraped; aborting without changes.", file=sys.stderr)
        return 1

    new_cars = []
    for make_name in sorted(catalog):
        for entry in catalog[make_name].values():
            key = re.sub(r"[^a-z0-9]", "", (make_name + entry["name"]).lower())
            if key in curated_keys:
                continue
            curated_keys.add(key)
            years = sorted(entry["years"])
            year_str = str(years[0]) if len(years) == 1 else f"{years[0]}–{years[-1]}"
            car_id = slugify(f"{make_name}-{entry['name']}") or slugify(key)
            while car_id in used_ids:
                car_id += "-x"
            used_ids.add(car_id)
            new_cars.append(
                {
                    "id": car_id,
                    "make": make_name,
                    "model": entry["name"],
                    "years": year_str,
                    "body": "",
                    "wikiTitle": f"{make_name} {entry['name']}",
                }
            )

    print(f"{len(new_cars)} new cars to add (after deduplicating against curated set)")

    titles = sorted({c["wikiTitle"] for c in new_cars})
    print(f"Resolving {len(titles)} Wikipedia thumbnails…")
    thumbs = resolve_wiki_images(session, titles)
    with_img = 0
    for car in new_cars:
        img = thumbs.get(car.pop("wikiTitle"))
        if img:
            car["img"] = img
            with_img += 1
    print(f"{with_img}/{len(new_cars)} new cars got a photo URL")

    merged = existing + new_cars
    merged.sort(key=lambda c: (c["make"].lower(), c["model"].lower()))
    CARS_JSON.write_text(
        "[\n" + ",\n".join(json.dumps(c, ensure_ascii=False) for c in merged) + "\n]\n",
        encoding="utf-8",
    )
    print(f"\ncars.json now has {len(merged)} cars across "
          f"{len({c['make'] for c in merged})} makes")
    return 0


if __name__ == "__main__":
    sys.exit(main())
