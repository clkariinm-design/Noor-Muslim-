from pathlib import Path


MIGRATION = Path(__file__).parents[1] / "migrations" / "001_initial_schema.sql"


def test_initial_schema_contains_core_quran_and_recitation_entities():
    sql = MIGRATION.read_text(encoding="utf-8").lower()

    for table in (
        "quran_surahs",
        "quran_ayahs",
        "quran_translations",
        "quran_ayah_translations",
        "reciters",
        "reciter_audio_assets",
    ):
        assert f"create table if not exists {table}" in sql


def test_user_owned_data_cascades_when_account_is_deleted():
    sql = MIGRATION.read_text(encoding="utf-8").lower()

    assert "references users(id) on delete cascade" in sql
    assert "refresh_token_hash char(64)" in sql
    assert "token_hash char(64)" in sql
    assert "password_hash text" in sql


def test_prayer_and_notification_preferences_are_persisted_without_precise_gps_storage():
    sql = MIGRATION.read_text(encoding="utf-8").lower()

    assert "create table if not exists user_prayer_preferences" in sql
    assert "create table if not exists user_notification_preferences" in sql
    assert "location_latitude" not in sql
    assert "location_longitude" not in sql


def test_initial_schema_includes_islamic_content_and_announcement_modules():
    sql = MIGRATION.read_text(encoding="utf-8").lower()

    for table in ("duas", "hadith_collections", "hadiths", "tasbih_sessions", "app_announcements"):
        assert f"create table if not exists {table}" in sql
    assert "'sahih', 'hasan', 'daif', 'unknown'" in sql
