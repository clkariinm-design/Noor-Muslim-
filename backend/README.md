# Noor Muslim Python API

FastAPI service for the Noor Muslim Android app. This service is designed to run beside the existing Supabase backend; it does not replace Supabase or modify its database.

## Endpoints

- `GET /health` — Railway health check.
- `GET /api/v1/quran/surahs` — reads the existing surah catalogue.
- `GET /api/v1/quran/surahs/{surah_number}/ayahs` — reads Uthmani ayahs for one surah.
- `GET /api/v1/quran/search?q=...` — searches the Uthmani Arabic text.
- `GET /docs` — interactive API documentation.

## Railway configuration

Set these service variables in Railway:

- `SUPABASE_URL`: existing Noor Muslim Supabase project URL.
- `SUPABASE_PUBLISHABLE_KEY`: Supabase publishable key only. Never use a service-role or secret key here.
- `CORS_ORIGINS`: comma-separated allowed origins; `*` is acceptable for this read-only public API during initial testing.

Start command:

```bash
uvicorn main:app --host 0.0.0.0 --port $PORT
```

Health check path: `/health`.

## Local run

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload
```

The Quran text is read from the existing Supabase tables. This service does not write or alter Quran content.
