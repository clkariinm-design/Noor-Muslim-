from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_health_endpoint_is_a_liveness_probe():
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok", "service": "noor-muslim-api"}


def test_api_metadata_advertises_stable_v1_contract():
    response = client.get("/api/v1/meta")

    assert response.status_code == 200
    payload = response.json()
    assert payload["api_version"] == "v1"
    assert payload["supported_api_versions"] == ["v1"]
    assert payload["default_language"] == "en"
    assert payload["monetization"] == "free"


def test_readiness_endpoint_returns_503_when_database_is_not_configured(monkeypatch):
    import main

    def no_database():
        raise RuntimeError("DATABASE_URL is not configured")

    monkeypatch.setattr(main, "configured_engine", no_database)
    response = client.get("/health/ready")

    assert response.status_code == 503
    assert response.json()["detail"] == "database_not_ready"


def test_readiness_endpoint_succeeds_when_database_query_succeeds(monkeypatch):
    import main
    from sqlalchemy import create_engine

    engine = create_engine("sqlite+pysqlite:///:memory:")
    monkeypatch.setattr(main, "configured_engine", lambda: engine)
    response = client.get("/health/ready")
    engine.dispose()

    assert response.status_code == 200
    assert response.json()["database"] == "ready"


def test_authentication_uses_email_password_without_google_oauth():
    paths = {route.path for route in app.routes}

    assert "/api/v1/auth/register" in paths
    assert "/api/v1/auth/login" in paths
    assert "/api/v1/auth/refresh" in paths
    assert "/api/v1/auth/logout" in paths
    assert "/api/v1/auth/me" in paths
    assert "/api/v1/auth/google" not in paths
