"""Optional: grow the catalog with extra models from the free NHTSA vPIC API.

The vPIC API (https://vpic.nhtsa.dot.gov/api/) is free and needs no key. It
provides makes/models but NOT photos, body types, or generations — so models
added here get a placeholder image and are tagged body_type="Unknown". Use this
to bulk-expand breadth; curate details/images in cars.json for the headline cars.

Examples:
  python -m app.expand_from_nhtsa --make Kia --make Lexus
  python -m app.expand_from_nhtsa --make Volvo --limit 30
"""
from __future__ import annotations

import argparse
from urllib.parse import quote

import httpx
from sqlmodel import Session, select

from app.database import engine, init_db
from app.models import Car, Manufacturer
from app.seed import placeholder

VPIC_MODELS = "https://vpic.nhtsa.dot.gov/api/vehicles/getmodelsformake/{make}?format=json"
USER_AGENT = "CarSpotter/1.0 (local dev expand script)"


def fetch_models(client: httpx.Client, make: str) -> list[str]:
    url = VPIC_MODELS.format(make=quote(make))
    resp = client.get(url, timeout=30, follow_redirects=True)
    resp.raise_for_status()
    results = resp.json().get("Results", [])
    # De-dup while preserving order.
    seen, models = set(), []
    for row in results:
        name = (row.get("Model_Name") or "").strip()
        if name and name.lower() not in seen:
            seen.add(name.lower())
            models.append(name)
    return models


def expand(makes: list[str], limit: int | None) -> None:
    init_db()
    headers = {"User-Agent": USER_AGENT}
    added_total = 0
    with Session(engine) as session, httpx.Client(headers=headers) as client:
        for make_name in makes:
            try:
                models = fetch_models(client, make_name)
            except httpx.HTTPError as exc:
                print(f"  {make_name}: fetch failed ({exc}) — skipped")
                continue
            if limit:
                models = models[:limit]

            manufacturer = session.exec(
                select(Manufacturer).where(Manufacturer.name == make_name)
            ).first()
            if not manufacturer:
                manufacturer = Manufacturer(name=make_name)
                session.add(manufacturer)
                session.commit()
                session.refresh(manufacturer)

            existing = {
                c.model.lower()
                for c in session.exec(
                    select(Car).where(Car.manufacturer_id == manufacturer.id)
                ).all()
            }
            added = 0
            for model in models:
                if model.lower() in existing:
                    continue
                session.add(
                    Car(
                        manufacturer_id=manufacturer.id,
                        model=model,
                        body_type="Unknown",
                        image_url=placeholder(make_name, model),
                    )
                )
                added += 1
            session.commit()
            added_total += added
            print(f"  {make_name}: +{added} models")
    print(f"Added {added_total} models total.")


def main() -> None:
    parser = argparse.ArgumentParser(description="Expand the catalog from NHTSA vPIC.")
    parser.add_argument("--make", action="append", required=True, help="Make name (repeatable).")
    parser.add_argument("--limit", type=int, default=None, help="Max models per make.")
    args = parser.parse_args()
    expand(args.make, args.limit)


if __name__ == "__main__":
    main()
