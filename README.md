# Noor Muslim

The project is being rebuilt from a clean Android shell with a Railway + PostgreSQL backend.

## Current implementation stage

- Android project shell retained; app screens and features are not yet rebuilt.
- Backend foundation is being introduced in `backend/` as a versioned FastAPI service.
- Railway PostgreSQL is the intended system of record.
- Quran text, translations and reciter/audio catalogues will be imported only after source integrity and licensing checks.
- No production schema migration or authentication rollout is considered complete until it is applied and verified against the Railway database.

See [backend/README.md](backend/README.md) for backend setup and deployment details.
