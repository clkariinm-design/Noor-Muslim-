"""Noor Muslim API entry point.

The backend starts as a modular monolith: one deployable FastAPI service with
versioned domain modules and PostgreSQL as the source of truth.
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from config import get_settings
from database import configured_engine, database_is_ready

settings = get_settings()
app = FastAPI(
    title="Noor Muslim API",
    description="Versioned backend API for the Noor Muslim application.",
    version="1.0.0",
    docs_url="/docs" if settings.environment != "production" else None,
    redoc_url=None,
    openapi_url="/openapi.json" if settings.environment != "production" else None,
)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "X-Request-ID"],
)


@app.get("/health", tags=["operations"])
def health() -> dict[str, str]:
    """Liveness probe; deliberately does not depend on external services."""
    return {"status": "ok", "service": "noor-muslim-api"}


@app.get("/health/ready", tags=["operations"])
def readiness() -> dict[str, str]:
    """Readiness probe; returns unavailable until PostgreSQL responds to SELECT 1."""
    engine = None
    try:
        engine = configured_engine()
        if not database_is_ready(engine):
            raise RuntimeError("database readiness query failed")
        return {"status": "ok", "database": "ready"}
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database_not_ready") from exc
    finally:
        if engine is not None:
            engine.dispose()


@app.get("/api/v1/meta", tags=["system"])
def api_metadata() -> dict[str, object]:
    """Public, non-sensitive API metadata for mobile-client compatibility."""
    return {
        "service": "Noor Muslim API",
        "api_version": "v1",
        "supported_api_versions": ["v1"],
        "default_language": "en",
        "monetization": "free",
    }
