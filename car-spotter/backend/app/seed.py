"""Seed the SQLite catalog from app/data/cars.json.

Real photos are resolved once, at seed time, from the Wikipedia REST summary
API (the page's lead image) using each car's "wiki" article title. This keeps
the runtime API dependency-free and offline-friendly. If an image can't be
resolved, a labelled placeholder is stored so the app still renders something.

Run:  python -m app.seed            (skip if already seeded)
      python -m app.seed --reset    (wipe and re-seed)
      python -m app.seed --no-images (skip network image lookups)
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from urllib.parse import quote

import httpx
from sqlmodel import Session, select

from app.database import engine, init_db
from app.models import Car, Manufacturer

DATA_FILE = Path(__file__).resolve().parent / "data" / "cars.json"
WIKI_SUMMARY = "https://en.wikipedia.org/api/rest_v1/page/summary/"
USER_AGENT = "CarSpotter/1.0 (local dev seed script)"


def placeholder(make: str, model: str) -> str:
    label = quote(f"{make} {model}")
    return f"https://placehold.co/600x400/1f2933/ffffff.png?text={label}"


def resolve_image(client: httpx.Client, wiki_title: str, make: str, model: str) -> str:
    """Return a real lead-image URL from Wikipedia, or a placeholder on failure."""
    try:
        url = WIKI_SUMMARY + quote(wiki_title.replace(" ", "_"))
        resp = client.get(url, timeout=15, follow_redirects=True)
        if resp.status_code == 200:
            data = resp.json()
            # Prefer a reasonably sized thumbnail; fall back to original.
            img = (data.get("thumbnail") or {}).get("source") or (
                data.get("originalimage") or {}
            ).get("source")
            if img:
                return img
    except (httpx.HTTPError, ValueError):
        pass
    return placeholder(make, model)


def seed(reset: bool = False, fetch_images: bool = True) -> None:
    init_db()

    with Session(engine) as session:
        existing = session.exec(select(Manufacturer)).first()
        if existing and not reset:
            print("Catalog already seeded. Use --reset to rebuild.")
            return
        if reset:
            for car in session.exec(select(Car)).all():
                session.delete(car)
            for make in session.exec(select(Manufacturer)).all():
                session.delete(make)
            session.commit()

        payload = json.loads(DATA_FILE.read_text(encoding="utf-8"))
        makes = payload["manufacturers"]

        headers = {"User-Agent": USER_AGENT}
        with httpx.Client(headers=headers) as client:
            total = 0
            for m in makes:
                manufacturer = Manufacturer(
                    name=m["name"], country=m.get("country"), logo_url=m.get("logo_url")
                )
                session.add(manufacturer)
                session.commit()
                session.refresh(manufacturer)

                for c in m["cars"]:
                    if fetch_images:
                        image = resolve_image(client, c.get("wiki", ""), m["name"], c["model"])
                    else:
                        image = placeholder(m["name"], c["model"])
                    session.add(
                        Car(
                            manufacturer_id=manufacturer.id,
                            model=c["model"],
                            year_start=c.get("year_start"),
                            year_end=c.get("year_end"),
                            generation=c.get("generation"),
                            body_type=c.get("body_type"),
                            image_url=image,
                        )
                    )
                    total += 1
                session.commit()
                print(f"  {m['name']}: {len(m['cars'])} cars")

        print(f"Seeded {len(makes)} manufacturers / {total} cars.")


def main() -> None:
    parser = argparse.ArgumentParser(description="Seed the Car Spotter catalog.")
    parser.add_argument("--reset", action="store_true", help="Wipe and re-seed.")
    parser.add_argument("--no-images", action="store_true", help="Skip Wikipedia image lookups.")
    args = parser.parse_args()
    seed(reset=args.reset, fetch_images=not args.no_images)


if __name__ == "__main__":
    sys.exit(main())
