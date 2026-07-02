#!/usr/bin/env python3
"""Fetch a photo for every car in cars.json from Wikipedia and bundle it
into the app's assets as images/<id>.jpg.

Runs in CI before the Gradle build. Designed to be polite to Wikimedia:
thumbnail URLs are resolved in batches of 50 via the MediaWiki API, image
downloads run serially with pacing, and 429 responses are retried with
backoff. Already-downloaded images are skipped so a cached images folder
short-circuits the whole fetch.

Cars whose lookup fails keep the in-app placeholder drawable; the build only
aborts if most images are missing.
"""

import io
import json
import sys
import time
from pathlib import Path

import requests
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app" / "src" / "main" / "assets"
IMAGES = ASSETS / "images"

API_URL = "https://en.wikipedia.org/w/api.php"
HEADERS = {
    "User-Agent": (
        "CarSpotterBuildBot/1.0 "
        "(+https://github.com/pftttt123/plugin) "
        f"python-requests/{requests.__version__}"
    )
}
THUMB_WIDTH = 640
MAX_WIDTH = 480
JPEG_QUALITY = 80
BATCH_SIZE = 50
DOWNLOAD_DELAY_S = 0.6


def get_with_retry(session, url, params=None, tries=6):
    last = None
    for attempt in range(tries):
        try:
            resp = session.get(url, params=params, timeout=60)
        except requests.RequestException as exc:
            last = exc
            time.sleep(min(30, 2 ** attempt))
            continue
        if resp.status_code == 429:
            retry_after = resp.headers.get("Retry-After")
            wait = int(retry_after) if retry_after and retry_after.isdigit() else min(60, 3 * 2 ** attempt)
            print(f"  429 from {resp.url.split('?')[0]}; waiting {wait}s", flush=True)
            time.sleep(wait)
            last = requests.HTTPError(f"429 for {url}")
            continue
        resp.raise_for_status()
        return resp
    raise last if last else RuntimeError(f"failed to fetch {url}")


def resolve_thumbnails(session, titles):
    """Map each requested title to a thumbnail URL via batched API queries."""
    thumbs = {}
    for start in range(0, len(titles), BATCH_SIZE):
        batch = titles[start : start + BATCH_SIZE]
        resp = get_with_retry(
            session,
            API_URL,
            params={
                "action": "query",
                "format": "json",
                "prop": "pageimages",
                "piprop": "thumbnail",
                "pithumbsize": THUMB_WIDTH,
                "redirects": 1,
                "titles": "|".join(batch),
            },
        )
        data = resp.json().get("query", {})

        # Follow title rewrites: requested -> normalized -> redirected -> page.
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


def save_image(content, out_path):
    img = Image.open(io.BytesIO(content)).convert("RGB")
    if img.width > MAX_WIDTH:
        img = img.resize(
            (MAX_WIDTH, round(img.height * MAX_WIDTH / img.width)),
            Image.LANCZOS,
        )
    img.save(out_path, "JPEG", quality=JPEG_QUALITY, optimize=True)


def main():
    cars = json.loads((ASSETS / "cars.json").read_text(encoding="utf-8"))
    IMAGES.mkdir(parents=True, exist_ok=True)

    pending = [c for c in cars if not (IMAGES / f"{c['id']}.jpg").exists()]
    cached = len(cars) - len(pending)
    if cached:
        print(f"{cached} images already present, fetching {len(pending)}")
    if not pending:
        print("All images cached; nothing to do.")
        return 0

    session = requests.Session()
    session.headers.update(HEADERS)

    titles = sorted({c["wiki"] for c in pending})
    print(f"Resolving {len(titles)} Wikipedia titles in batches of {BATCH_SIZE}…")
    thumbs = resolve_thumbnails(session, titles)

    failures = []
    for i, car in enumerate(pending, 1):
        url = thumbs.get(car["wiki"])
        if not url:
            failures.append((car["id"], "no thumbnail for page"))
            continue
        try:
            resp = get_with_retry(session, url)
            save_image(resp.content, IMAGES / f"{car['id']}.jpg")
        except Exception as exc:  # noqa: BLE001 - report and continue
            failures.append((car["id"], f"{type(exc).__name__}: {exc}"))
        if i % 25 == 0:
            print(f"  {i}/{len(pending)} downloaded", flush=True)
        time.sleep(DOWNLOAD_DELAY_S)

    ok = len(cars) - len(failures)
    print(f"\n{ok}/{len(cars)} car images bundled")
    if failures:
        print("\nMissing images (placeholder will be shown in app):")
        for cid, err in failures:
            print(f"  {cid}: {err}")
    if ok < len(cars) * 0.7:
        print("Too many image fetches failed; aborting build.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
