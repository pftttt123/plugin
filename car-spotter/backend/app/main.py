"""Car Spotter backend — a read-only catalog API.

Serves manufacturers and car models (with photos + key details) to the
Android app. Spotted/checklist state is stored on the device, not here.
"""
from __future__ import annotations

from typing import Optional

from fastapi import Depends, FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import func
from sqlmodel import Session, select

from app.database import engine, get_session, init_db
from app.models import Car, Manufacturer
from app.schemas import CarRead, ManufacturerRead, ManufacturerWithCars

app = FastAPI(
    title="Car Spotter API",
    description="A browsable catalog of cars grouped by manufacturer.",
    version="1.0.0",
)

# Wide-open CORS: this is a local dev API consumed by a mobile app.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def on_startup() -> None:
    init_db()


def _car_to_read(car: Car, manufacturer_name: str) -> CarRead:
    return CarRead(
        id=car.id,
        manufacturer_id=car.manufacturer_id,
        manufacturer_name=manufacturer_name,
        model=car.model,
        year_start=car.year_start,
        year_end=car.year_end,
        generation=car.generation,
        body_type=car.body_type,
        image_url=car.image_url,
    )


@app.get("/health")
def health() -> dict:
    """Liveness check + quick catalog size."""
    with Session(engine) as session:
        n_makes = session.exec(select(func.count()).select_from(Manufacturer)).one()
        n_cars = session.exec(select(func.count()).select_from(Car)).one()
    return {"status": "ok", "manufacturers": n_makes, "cars": n_cars}


@app.get("/manufacturers", response_model=list[ManufacturerRead])
def list_manufacturers(session: Session = Depends(get_session)) -> list[ManufacturerRead]:
    """All manufacturers, alphabetical, each with its car count."""
    counts = dict(
        session.exec(select(Car.manufacturer_id, func.count(Car.id)).group_by(Car.manufacturer_id)).all()
    )
    makes = session.exec(select(Manufacturer).order_by(Manufacturer.name)).all()
    return [
        ManufacturerRead(
            id=m.id,
            name=m.name,
            country=m.country,
            logo_url=m.logo_url,
            car_count=counts.get(m.id, 0),
        )
        for m in makes
    ]


@app.get("/cars", response_model=list[CarRead])
def list_cars(
    session: Session = Depends(get_session),
    manufacturer_id: Optional[int] = Query(default=None),
    body_type: Optional[str] = Query(default=None),
    q: Optional[str] = Query(default=None, description="Match manufacturer name or model"),
) -> list[CarRead]:
    """Cars, optionally filtered by manufacturer, body type, or free-text search."""
    statement = select(Car, Manufacturer).join(Manufacturer, Car.manufacturer_id == Manufacturer.id)

    if manufacturer_id is not None:
        statement = statement.where(Car.manufacturer_id == manufacturer_id)
    if body_type:
        statement = statement.where(Car.body_type == body_type)
    if q:
        like = f"%{q.strip()}%"
        statement = statement.where(
            func.lower(Manufacturer.name).like(func.lower(like))
            | func.lower(Car.model).like(func.lower(like))
        )

    statement = statement.order_by(Manufacturer.name, Car.model)
    rows = session.exec(statement).all()
    return [_car_to_read(car, make.name) for car, make in rows]


@app.get("/cars/{car_id}", response_model=CarRead)
def get_car(car_id: int, session: Session = Depends(get_session)) -> CarRead:
    """A single car with its manufacturer name."""
    car = session.get(Car, car_id)
    if not car:
        raise HTTPException(status_code=404, detail="Car not found")
    make = session.get(Manufacturer, car.manufacturer_id)
    return _car_to_read(car, make.name if make else "")


@app.get("/catalog", response_model=list[ManufacturerWithCars])
def catalog(session: Session = Depends(get_session)) -> list[ManufacturerWithCars]:
    """Full catalog: manufacturers A→Z, each with its cars nested.

    The app loads this once to power the grouped browse view offline.
    """
    makes = session.exec(select(Manufacturer).order_by(Manufacturer.name)).all()
    result: list[ManufacturerWithCars] = []
    for m in makes:
        cars = session.exec(
            select(Car).where(Car.manufacturer_id == m.id).order_by(Car.model)
        ).all()
        result.append(
            ManufacturerWithCars(
                id=m.id,
                name=m.name,
                country=m.country,
                logo_url=m.logo_url,
                car_count=len(cars),
                cars=[_car_to_read(c, m.name) for c in cars],
            )
        )
    return result
