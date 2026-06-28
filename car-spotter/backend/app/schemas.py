"""Response schemas returned by the API (decoupled from DB tables)."""
from __future__ import annotations

from typing import Optional

from sqlmodel import SQLModel


class CarRead(SQLModel):
    id: int
    manufacturer_id: int
    manufacturer_name: str
    model: str
    year_start: Optional[int] = None
    year_end: Optional[int] = None
    generation: Optional[str] = None
    body_type: Optional[str] = None
    image_url: Optional[str] = None


class ManufacturerRead(SQLModel):
    id: int
    name: str
    country: Optional[str] = None
    logo_url: Optional[str] = None
    car_count: int = 0


class ManufacturerWithCars(ManufacturerRead):
    cars: list[CarRead] = []
