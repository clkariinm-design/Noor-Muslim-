# Noor Muslim Backend

FastAPI + PostgreSQL modular monolith deployed on Railway. The Android client talks only to the versioned API; audio media is served by approved external audio providers/CDNs.

## Local development

```bash
python -m venv .venv
. .venv/bin/activate
pip install -r requirements-dev.txt
uvicorn main:app --reload
pytest
```

## Railway

- Service root directory: `/backend`
- Start command: `uvicorn main:app --host 0.0.0.0 --port $PORT`
- Liveness health check: `/health`
- Readiness check: `/health/ready`
- Required production variable: `DATABASE_URL` (Railway Postgres reference)
- Set `ENVIRONMENT=production` and set `CORS_ORIGINS` only to trusted origins.
- Set `AUTH_JWT_SECRET` to a unique random value of at least 32 characters.
- Authentication uses email and password; Google OAuth credentials are not required.

## Authentication

- `POST /api/v1/auth/register` creates an account with email and password.
- `POST /api/v1/auth/login` signs in with email and password.
- `POST /api/v1/auth/refresh` rotates the refresh token.
- `POST /api/v1/auth/logout` revokes the refresh token.
- `GET /api/v1/auth/me` returns the authenticated account profile.
- Auth endpoints return `503 database_schema_not_ready` until migration
  `001_initial_schema` is recorded in PostgreSQL; they do not attempt schema
  changes automatically.

Google Sign-In is intentionally not enabled. Account email verification and password-reset email delivery require a mail provider and are not yet configured.

## Database

The initial PostgreSQL schema is in `migrations/001_initial_schema.sql`. It is not run automatically on app startup. Apply it deliberately with a transaction:

```bash
psql "$DATABASE_URL" --single-transaction -v ON_ERROR_STOP=1 -f migrations/001_initial_schema.sql
```

The migration creates the user/auth records, Quran and translation catalogues, reciters/audio metadata, bookmarks, reading progress, prayer and notification preferences, duas, hadiths, tasbih sessions, announcements and app-release policy. It stores hashes—not raw values—for refresh and account-action tokens.

## API policy

- Stable public API prefix: `/api/v1`.
- Breaking changes require a new version prefix.
- `/health` is a liveness check and does not depend on PostgreSQL.
- `/health/ready` verifies PostgreSQL connectivity.
- Quran text and translations must be imported only from sources whose accuracy and redistribution rights have been verified.
- No reciter catalogue or audio URL is seeded until source, availability and licensing checks are complete.
- Precise GPS coordinates are not stored in the account schema; location should be processed transiently for prayer-time calculation.
- Never commit credentials or production secrets.
