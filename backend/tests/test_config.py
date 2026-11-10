import pytest

from config import Settings


def test_cors_origins_are_trimmed_and_normalized():
    settings = Settings(cors_origins_raw=" https://app.example ,https://admin.example/ ,, ")

    assert settings.cors_origins == ["https://app.example", "https://admin.example"]


def test_railway_postgres_url_is_normalized_for_psycopg():
    settings = Settings(database_url="postgres://user:pass@host:5432/noor")

    assert settings.sqlalchemy_database_url == "postgresql+psycopg://user:pass@host:5432/noor"


def test_unknown_environment_is_rejected():
    with pytest.raises(ValueError):
        Settings(environment="prod-ish")


def test_cors_origins_load_from_railway_cors_origins_variable(monkeypatch):
    monkeypatch.setenv("CORS_ORIGINS", "https://noor.example")

    settings = Settings(_env_file=None)

    assert settings.cors_origins == ["https://noor.example"]
