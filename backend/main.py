from __future__ import annotations

import os
from contextlib import asynccontextmanager
from typing import Any

import httpx
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware


SUPABASE_URL = os.getenv("SUPABASE_URL", "").rstrip("/")
SUPABASE_PUBLISHABLE_KEY = os.getenv("SUPABASE_PUBLISHABLE_KEY", "")
APP_NAME = "Noor Muslim API"


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


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield


app = FastAPI(
    title=APP_NAME,
    description="Private server-side API layer for the Noor Muslim mobile app.",
    version="0.1.0",
    lifespan=lifespan,
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
    return {"status": "ok", "service": "noor-muslim-python-api"}


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
