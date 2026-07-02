#!/usr/bin/env python3
"""Fetch a photo for every car in cars.json from Wikipedia and bundle it
into the app's assets as images/<id>.jpg.

Runs in CI before the Gradle build. Cars whose lookup fails simply keep the
in-app placeholder drawable, so this script never fails the build outright —
it prints a report instead.
"""

import io
import json
import sys
import urllib.parse
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

import requests
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app" / "src" / "main" / "assets"
IMAGES = ASSETS / "images"

HEADERS = {
    "User-Agent": "CarSpotterBuildBot/1.0 (image fetch for offline hobby app)"
}
SUMMARY_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/{title}"
MAX_WIDTH = 480
JPEG_QUALITY = 80
THUMB_REQUEST_WIDTH = 640


def thumb_url(car):
    title = urllib.parse.quote(car["wiki"].replace(" ", "_"), safe="")
    resp = requests.get(
        SUMMARY_URL.format(title=title), headers=HEADERS, timeout=30
    )
    resp.raise_for_status()
    data = resp.json()
    thumb = data.get("thumbnail", {}).get("source")
    if not thumb:
        raise ValueError("page has no thumbnail")
    # Summary thumbnails default to 320px; ask for a wider rendition when
    # the URL follows the standard /thumb/.../NNNpx-name pattern.
    parts = thumb.rsplit("/", 1)
    if len(parts) == 2 and "px-" in parts[1]:
        size, rest = parts[1].split("px-", 1)
        if size.isdigit():
            thumb = f"{parts[0]}/{THUMB_REQUEST_WIDTH}px-{rest}"
    return thumb


def fetch(car):
    try:
        url = thumb_url(car)
        resp = requests.get(url, headers=HEADERS, timeout=60)
        if resp.status_code != 200:
            # Wider rendition can 404 if it exceeds the original size;
            # fall back to whatever the summary offered.
            title = urllib.parse.quote(car["wiki"].replace(" ", "_"), safe="")
            data = requests.get(
                SUMMARY_URL.format(title=title), headers=HEADERS, timeout=30
            ).json()
            resp = requests.get(
                data["thumbnail"]["source"], headers=HEADERS, timeout=60
            )
            resp.raise_for_status()
        img = Image.open(io.BytesIO(resp.content)).convert("RGB")
        if img.width > MAX_WIDTH:
            img = img.resize(
                (MAX_WIDTH, round(img.height * MAX_WIDTH / img.width)),
                Image.LANCZOS,
            )
        out = IMAGES / f"{car['id']}.jpg"
        img.save(out, "JPEG", quality=JPEG_QUALITY, optimize=True)
        return car["id"], None
    except Exception as exc:  # noqa: BLE001 - report and continue
        return car["id"], f"{type(exc).__name__}: {exc}"


def main():
    cars = json.loads((ASSETS / "cars.json").read_text(encoding="utf-8"))
    IMAGES.mkdir(parents=True, exist_ok=True)

    with ThreadPoolExecutor(max_workers=6) as pool:
        results = list(pool.map(fetch, cars))

    failures = [(cid, err) for cid, err in results if err]
    ok = len(results) - len(failures)
    print(f"Fetched {ok}/{len(results)} car images")
    if failures:
        print("\nMissing images (placeholder will be shown in app):")
        for cid, err in failures:
            print(f"  {cid}: {err}")
    # Fail the build only if the database would be mostly image-less.
    if ok < len(results) * 0.7:
        print("Too many image fetches failed; aborting build.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
