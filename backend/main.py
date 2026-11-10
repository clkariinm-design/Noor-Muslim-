"""Noor Muslim API entry point.

The backend starts as a modular monolith: one deployable FastAPI service with
versioned domain modules and PostgreSQL as the source of truth.
"""
from uuid import UUID, uuid4

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from starlette.responses import Response

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


@app.middleware("http")
async def apply_security_headers(request: Request, call_next) -> Response:
    """Attach a conservative baseline of security headers to every response."""
    supplied_request_id = request.headers.get("X-Request-ID", "")
    try:
        request_id = str(UUID(supplied_request_id))
    except (ValueError, TypeError, AttributeError):
        request_id = str(uuid4())

    response = await call_next(request)
    response.headers["X-Request-ID"] = request_id
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
    response.headers["Permissions-Policy"] = (
        "camera=(), microphone=(), geolocation=()"
    )
    response.headers["Cross-Origin-Resource-Policy"] = "same-site"
    if request.url.scheme == "https":
        response.headers["Strict-Transport-Security"] = (
            "max-age=31536000; includeSubDomains"
        )
    return response


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
