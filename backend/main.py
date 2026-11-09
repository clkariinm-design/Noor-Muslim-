from __future__ import annotations

import json
import os
from datetime import datetime, timezone
from typing import Any

import httpx
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware


SUPABASE_URL = os.getenv("SUPABASE_URL", "").rstrip("/")
SUPABASE_PUBLISHABLE_KEY = os.getenv("SUPABASE_PUBLISHABLE_KEY", "")
APP_NAME = "Noor Muslim API"
APP_VERSION = "1.0.0"

DEFAULT_FEATURES = {
    "quran_search": True,
    "daily_content": True,
    "qibla": True,
    "tasbeeh": True,
    "duas": True,
    "ai_assistant": False,
}


def _env_bool(name: str, default: bool = False) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def _feature_flags() -> dict[str, bool]:
    """Read safe, public feature switches controlled by Railway variables."""
    raw = os.getenv("NOOR_FEATURE_FLAGS", "")
    if not raw:
        return DEFAULT_FEATURES.copy()
    try:
        supplied = json.loads(raw)
    except json.JSONDecodeError:
        return DEFAULT_FEATURES.copy()
    if not isinstance(supplied, dict):
        return DEFAULT_FEATURES.copy()
    flags = DEFAULT_FEATURES.copy()
    for key, value in supplied.items():
        if key in flags and isinstance(value, bool):
            flags[key] = value
    return flags


def build_app_config() -> dict[str, Any]:
    """Build remote app settings without requiring a database or secret key."""
    return {
        "app": {"name": "Noor Muslim", "latest_version": os.getenv("NOOR_LATEST_VERSION", "1.0.0")},
        "maintenance": {
            "enabled": _env_bool("NOOR_MAINTENANCE_MODE"),
            "message": os.getenv(
                "NOOR_MAINTENANCE_MESSAGE",
                "Noor Muslim is temporarily under maintenance. Please try again soon.",
            ),
        },
        "minimum_supported_version": os.getenv("NOOR_MINIMUM_VERSION", "1.0.0"),
        "features": _feature_flags(),
        "announcement": {
            "enabled": _env_bool("NOOR_ANNOUNCEMENT_ENABLED"),
            "title": os.getenv("NOOR_ANNOUNCEMENT_TITLE", ""),
            "message": os.getenv("NOOR_ANNOUNCEMENT_MESSAGE", ""),
            "url": os.getenv("NOOR_ANNOUNCEMENT_URL", ""),
        },
        "updated_at": datetime.now(timezone.utc).isoformat(),
    }


def supabase_headers() -> dict[str, str]:
    if not SUPABASE_URL or not SUPABASE_PUBLISHABLE_KEY:
        raise HTTPException(
            status_code=503,
            detail="Supabase connection is not configured on the backend.",
        )
    return {
        "apikey": SUPABASE_PUBLISHABLE_KEY,
        "Authorization": f"Bearer {SUPABASE_PUBLISHABLE_KEY}",
        "Accept": "application/json",
    }


app = FastAPI(
    title=APP_NAME,
    description="Server-side API layer and remote configuration for the Noor Muslim mobile app.",
    version=APP_VERSION,
)

allowed_origins = [
    origin.strip()
    for origin in os.getenv("CORS_ORIGINS", "*").split(",")
    if origin.strip()
]
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=False,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)


@app.get("/")
async def root() -> dict[str, str]:
    return {"name": APP_NAME, "status": "running", "docs": "/docs"}


@app.get("/health")
async def health() -> dict[str, str]:
    return {
        "status": "ok",
        "service": "noor-muslim-python-api",
        "version": APP_VERSION,
    }


@app.get("/api/v1/app/config")
async def app_config() -> dict[str, Any]:
    """Public remote settings: maintenance mode, feature switches and announcements."""
    return build_app_config()


@app.get("/api/v1/app/announcement")
async def app_announcement() -> dict[str, Any]:
    """Return the currently configured announcement for clients that need it separately."""
    announcement = build_app_config()["announcement"]
    return announcement


@app.get("/api/v1/quran/surahs")
async def list_surahs() -> list[dict[str, Any]]:
    """Return the existing public Quran surah catalogue from Supabase."""
    headers = supabase_headers()
    url = f"{SUPABASE_URL}/rest/v1/quran_surahs"
    params = {"select": "*", "order": "surah_number.asc"}

    async with httpx.AsyncClient(timeout=15.0) as client:
        response = await client.get(url, headers=headers, params=params)

    if response.is_error:
        raise HTTPException(
            status_code=502,
            detail="Could not load the Quran surah catalogue.",
        )
    return response.json()


@app.get("/api/v1/quran/surahs/{surah_number}/ayahs")
async def get_surah_ayahs(surah_number: int) -> list[dict[str, Any]]:
    """Return the verified Uthmani ayah text for one surah."""
    if not 1 <= surah_number <= 114:
        raise HTTPException(status_code=422, detail="Surah number must be 1–114.")

    headers = supabase_headers()
    url = f"{SUPABASE_URL}/rest/v1/quran_ayahs"
    params = {
        "select": "surah_number,ayah_number,text_uthmani,juz_number,page_number",
        "surah_number": f"eq.{surah_number}",
        "order": "ayah_number.asc",
    }

    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.get(url, headers=headers, params=params)

    if response.is_error:
        raise HTTPException(
            status_code=502,
            detail="Could not load Quran ayahs from Supabase.",
        )
    return response.json()


@app.get("/api/v1/quran/search")
async def search_quran(
    q: str = Query(min_length=2, max_length=80),
    limit: int = Query(default=20, ge=1, le=50),
) -> list[dict[str, Any]]:
    """Search the Arabic Uthmani Quran text without changing its source text."""
    headers = supabase_headers()
    url = f"{SUPABASE_URL}/rest/v1/quran_ayahs"
    params = {
        "select": "surah_number,ayah_number,text_uthmani,juz_number,page_number",
        "text_uthmani": f"ilike.*{q}*",
        "order": "surah_number.asc,ayah_number.asc",
        "limit": str(limit),
    }

    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.get(url, headers=headers, params=params)

    if response.is_error:
        raise HTTPException(status_code=502, detail="Quran search is temporarily unavailable.")
    return response.json()
