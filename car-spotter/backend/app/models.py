"""SQLModel tables for the Car Spotter catalog (read-only at runtime).

Note: no ORM Relationship() is declared — the API queries with explicit joins,
which keeps the models simple and avoids SQLAlchemy 2.0 Mapped[] requirements.
"""
from __future__ import annotations

from typing import Optional

from sqlmodel import Field, SQLModel


class Manufacturer(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str = Field(index=True, unique=True)
    country: Optional[str] = None
    logo_url: Optional[str] = None


class Car(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    manufacturer_id: int = Field(foreign_key="manufacturer.id", index=True)
    model: str = Field(index=True)
    year_start: Optional[int] = None
    year_end: Optional[int] = None  # None == still in production
    generation: Optional[str] = None  # e.g. "Mk7", "F-Series 14th gen"
    body_type: Optional[str] = Field(default=None, index=True)  # Sedan/SUV/Coupe/...
    image_url: Optional[str] = None
