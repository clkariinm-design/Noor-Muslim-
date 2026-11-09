# Noor Muslim Python API

FastAPI service for the Noor Muslim Android app. It runs beside the existing Supabase backend; it does not replace Supabase or modify Quran content.

## Endpoints

- `GET /health` — Railway health check and API version.
- `GET /api/v1/app/config` — remote app settings, feature switches, maintenance mode, version policy and announcement.
- `GET /api/v1/app/announcement` — current public announcement.
- `GET /api/v1/quran/surahs` — reads the existing surah catalogue.
- `GET /api/v1/quran/surahs/{surah_number}/ayahs` — reads Uthmani ayahs for one surah.
- `GET /api/v1/quran/search?q=...` — searches the Uthmani Arabic text.
- `GET /docs` — interactive API documentation.

## Railway configuration

Existing variables:

- `SUPABASE_URL`: existing Noor Muslim Supabase project URL.
- `SUPABASE_PUBLISHABLE_KEY`: Supabase publishable key only. Never use a service-role or secret key here.
- `CORS_ORIGINS`: comma-separated allowed origins.

Optional remote app controls (set in Railway Variables):

- `NOOR_LATEST_VERSION`: latest app version shown in remote config (default `1.0.0`).
- `NOOR_MINIMUM_VERSION`: minimum supported version metadata (default `1.0.0`).
- `NOOR_MAINTENANCE_MODE`: `true` or `false`; tells compatible app clients maintenance is active.
- `NOOR_MAINTENANCE_MESSAGE`: message displayed during maintenance.
- `NOOR_FEATURE_FLAGS`: JSON object for known feature switches, e.g. `{"qibla":false,"ai_assistant":false}`.
- `NOOR_ANNOUNCEMENT_ENABLED`: `true` or `false`.
- `NOOR_ANNOUNCEMENT_TITLE`, `NOOR_ANNOUNCEMENT_MESSAGE`, `NOOR_ANNOUNCEMENT_URL`: public announcement content.

Only known feature names and boolean values are accepted. AI assistant remains disabled by default; enabling its flag alone does not create an AI service.

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

## Tests

```bash
pip install -r requirements-dev.txt
pytest -q
```

The Quran text is read from the existing Supabase tables. This service does not write or alter Quran content. Remote settings are public client configuration; never put passwords, private keys, or admin credentials in these variables.
