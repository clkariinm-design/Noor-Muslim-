import sys
from pathlib import Path

# Ensure the backend application module is importable when pytest runs from
# the backend directory or from the repository root.
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

import main


def test_config_defaults_are_safe(monkeypatch):
    monkeypatch.delenv("NOOR_MAINTENANCE_MODE", raising=False)
    monkeypatch.delenv("NOOR_FEATURE_FLAGS", raising=False)
    config = main.build_app_config()

    assert config["maintenance"]["enabled"] is False
    assert config["features"]["quran_search"] is True
    assert config["features"]["ai_assistant"] is False
    assert config["minimum_supported_version"] == "1.0.0"


def test_remote_maintenance_and_announcement(monkeypatch):
    monkeypatch.setenv("NOOR_MAINTENANCE_MODE", "true")
    monkeypatch.setenv("NOOR_MAINTENANCE_MESSAGE", "Update in progress")
    monkeypatch.setenv("NOOR_ANNOUNCEMENT_ENABLED", "1")
    monkeypatch.setenv("NOOR_ANNOUNCEMENT_TITLE", "New feature")
    monkeypatch.setenv("NOOR_ANNOUNCEMENT_MESSAGE", "Quran search is ready")

    config = main.build_app_config()

    assert config["maintenance"]["enabled"] is True
    assert config["maintenance"]["message"] == "Update in progress"
    assert config["announcement"]["enabled"] is True
    assert config["announcement"]["title"] == "New feature"


def test_feature_flags_only_accept_known_boolean_values(monkeypatch):
    monkeypatch.setenv(
        "NOOR_FEATURE_FLAGS",
        '{"qibla": false, "ai_assistant": true, "unknown_feature": true, "duas": "false"}',
    )

    flags = main.build_app_config()["features"]

    assert flags["qibla"] is False
    assert flags["ai_assistant"] is True
    assert flags["duas"] is True
    assert "unknown_feature" not in flags


def test_invalid_feature_flag_json_uses_defaults(monkeypatch):
    monkeypatch.setenv("NOOR_FEATURE_FLAGS", "not-json")

    assert main.build_app_config()["features"] == main.DEFAULT_FEATURES
