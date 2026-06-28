"""SQLite engine + session helpers for the Car Spotter catalog."""
from __future__ import annotations

import os
from pathlib import Path

from sqlmodel import Session, SQLModel, create_engine

# DB lives next to the backend package so it is easy to find/delete during dev.
DB_PATH = Path(os.getenv("CARSPOTTER_DB", Path(__file__).resolve().parent.parent / "carspotter.db"))
DATABASE_URL = f"sqlite:///{DB_PATH}"

# check_same_thread=False lets FastAPI's threadpool share the connection safely.
engine = create_engine(DATABASE_URL, echo=False, connect_args={"check_same_thread": False})


def init_db() -> None:
    """Create tables for all imported SQLModel models."""
    # Import models so they register on SQLModel.metadata before create_all.
    from app import models  # noqa: F401

    SQLModel.metadata.create_all(engine)


def get_session() -> Session:
    """FastAPI dependency that yields a scoped DB session."""
    with Session(engine) as session:
        yield session
