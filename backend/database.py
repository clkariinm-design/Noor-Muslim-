"""Database connectivity helpers. Schema changes are applied via SQL migrations."""
from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine

from config import get_settings


def create_database_engine(database_url: str) -> Engine:
    return create_engine(database_url, pool_pre_ping=True, pool_size=5, max_overflow=10)


def database_is_ready(engine: Engine) -> bool:
    with engine.connect() as connection:
        return connection.execute(text("SELECT 1")).scalar_one() == 1


def configured_engine() -> Engine:
    settings = get_settings()
    if not settings.sqlalchemy_database_url:
        raise RuntimeError("DATABASE_URL is not configured")
    return create_database_engine(settings.sqlalchemy_database_url)
