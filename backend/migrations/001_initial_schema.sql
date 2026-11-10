-- Noor Muslim initial PostgreSQL schema. Apply once using the migration runner.
CREATE TABLE IF NOT EXISTS schema_migrations (
    version VARCHAR(64) PRIMARY KEY,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash TEXT,
    google_subject VARCHAR(255) UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_auth_method_present CHECK (password_hash IS NOT NULL OR google_subject IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS idx_users_active ON users (is_active);

CREATE TABLE IF NOT EXISTS user_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    language_code VARCHAR(16) NOT NULL DEFAULT 'en',
    preferred_reciter_id UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS quran_surahs (
    surah_number SMALLINT PRIMARY KEY CHECK (surah_number BETWEEN 1 AND 114),
    name_arabic VARCHAR(100) NOT NULL,
    name_english VARCHAR(100) NOT NULL,
    revelation_type VARCHAR(16) NOT NULL CHECK (revelation_type IN ('meccan', 'medinan')),
    ayah_count SMALLINT NOT NULL CHECK (ayah_count > 0)
);

CREATE TABLE IF NOT EXISTS quran_ayahs (
    surah_number SMALLINT NOT NULL REFERENCES quran_surahs(surah_number),
    ayah_number SMALLINT NOT NULL CHECK (ayah_number > 0),
    text_uthmani TEXT NOT NULL,
    juz_number SMALLINT,
    page_number SMALLINT,
    source_name VARCHAR(200) NOT NULL DEFAULT 'Tanzil Project',
    source_url TEXT NOT NULL DEFAULT 'https://tanzil.net/',
    license_name VARCHAR(200) NOT NULL DEFAULT 'Creative Commons Attribution 3.0',
    license_url TEXT NOT NULL DEFAULT 'https://tanzil.net/docs/Text_License',
    PRIMARY KEY (surah_number, ayah_number)
);

CREATE TABLE IF NOT EXISTS quran_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code VARCHAR(16) NOT NULL,
    translator_name VARCHAR(200) NOT NULL,
    source_name VARCHAR(200) NOT NULL,
    source_url TEXT,
    license_name TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (language_code, translator_name, source_name)
);

CREATE TABLE IF NOT EXISTS quran_ayah_translations (
    translation_id UUID NOT NULL REFERENCES quran_translations(id) ON DELETE CASCADE,
    surah_number SMALLINT NOT NULL,
    ayah_number SMALLINT NOT NULL,
    translation_text TEXT NOT NULL,
    PRIMARY KEY (translation_id, surah_number, ayah_number),
    FOREIGN KEY (surah_number, ayah_number) REFERENCES quran_ayahs(surah_number, ayah_number) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reciters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name_arabic VARCHAR(200) NOT NULL,
    name_english VARCHAR(200) NOT NULL,
    biography TEXT,
    country_code CHAR(2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reciter_audio_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reciter_id UUID NOT NULL REFERENCES reciters(id) ON DELETE CASCADE,
    surah_number SMALLINT NOT NULL REFERENCES quran_surahs(surah_number),
    source_name VARCHAR(200) NOT NULL,
    audio_url TEXT NOT NULL,
    quality_label VARCHAR(32) NOT NULL DEFAULT 'standard',
    format VARCHAR(16) NOT NULL,
    file_size_bytes BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_checked_at TIMESTAMPTZ,
    last_check_status VARCHAR(16) NOT NULL DEFAULT 'unknown',
    UNIQUE (reciter_id, surah_number, source_name, quality_label)
);
CREATE INDEX IF NOT EXISTS idx_audio_reciter_surah ON reciter_audio_assets (reciter_id, surah_number) WHERE is_active = TRUE;

CREATE TABLE IF NOT EXISTS user_bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    surah_number SMALLINT NOT NULL,
    ayah_number SMALLINT NOT NULL,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (surah_number, ayah_number) REFERENCES quran_ayahs(surah_number, ayah_number),
    UNIQUE (user_id, surah_number, ayah_number)
);

CREATE TABLE IF NOT EXISTS reading_progress (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    surah_number SMALLINT NOT NULL,
    ayah_number SMALLINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (surah_number, ayah_number) REFERENCES quran_ayahs(surah_number, ayah_number)
);

CREATE TABLE IF NOT EXISTS user_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform VARCHAR(16) NOT NULL CHECK (platform IN ('android', 'ios')),
    push_token TEXT NOT NULL UNIQUE,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS app_release_policy (
    id SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    latest_version_code INTEGER NOT NULL DEFAULT 1,
    minimum_supported_version_code INTEGER NOT NULL DEFAULT 1,
    release_notes TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (minimum_supported_version_code <= latest_version_code)
);

-- Store hashes only; raw refresh/reset/verification tokens are never persisted.
CREATE TABLE IF NOT EXISTS auth_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_user_active ON auth_sessions (user_id, expires_at) WHERE revoked_at IS NULL;

CREATE TABLE IF NOT EXISTS auth_action_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purpose VARCHAR(32) NOT NULL CHECK (purpose IN ('email_verification', 'password_reset')),
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_auth_action_tokens_user ON auth_action_tokens (user_id, purpose, expires_at);

CREATE TABLE IF NOT EXISTS user_notification_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    prayer_reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    quran_reminders_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    announcements_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_prayer_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    calculation_method VARCHAR(64),
    asr_madhab VARCHAR(16) CHECK (asr_madhab IS NULL OR asr_madhab IN ('hanafi', 'shafi')),
    adhan_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    timezone_name VARCHAR(64),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_lower ON users (LOWER(email));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'user_preferences_preferred_reciter_fk'
    ) THEN
        ALTER TABLE user_preferences
            ADD CONSTRAINT user_preferences_preferred_reciter_fk
            FOREIGN KEY (preferred_reciter_id) REFERENCES reciters(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS duas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(64) NOT NULL,
    title VARCHAR(240) NOT NULL,
    arabic_text TEXT NOT NULL,
    transliteration TEXT,
    translation_text TEXT NOT NULL,
    language_code VARCHAR(16) NOT NULL DEFAULT 'en',
    source_reference TEXT NOT NULL,
    source_url TEXT,
    authenticity_grade VARCHAR(32),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_duas_category_language ON duas (category, language_code) WHERE is_active = TRUE;

CREATE TABLE IF NOT EXISTS hadith_collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_name VARCHAR(200) NOT NULL UNIQUE,
    compiler_name VARCHAR(200),
    source_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS hadiths (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID NOT NULL REFERENCES hadith_collections(id),
    hadith_reference VARCHAR(120) NOT NULL,
    chapter_reference VARCHAR(200),
    narrator TEXT,
    arabic_text TEXT NOT NULL,
    translation_text TEXT NOT NULL,
    language_code VARCHAR(16) NOT NULL DEFAULT 'en',
    grade VARCHAR(32) CHECK (grade IS NULL OR grade IN ('sahih', 'hasan', 'daif', 'unknown')),
    grading_source TEXT,
    source_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (collection_id, hadith_reference, language_code)
);
CREATE INDEX IF NOT EXISTS idx_hadiths_collection_ref ON hadiths (collection_id, hadith_reference) WHERE is_active = TRUE;

CREATE TABLE IF NOT EXISTS tasbih_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dhikr_key VARCHAR(80) NOT NULL,
    count BIGINT NOT NULL DEFAULT 0 CHECK (count >= 0),
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_tasbih_user_updated ON tasbih_sessions (user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS app_announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(240) NOT NULL,
    body TEXT NOT NULL,
    language_code VARCHAR(16) NOT NULL DEFAULT 'en',
    severity VARCHAR(16) NOT NULL DEFAULT 'info' CHECK (severity IN ('info', 'warning', 'critical')),
    published_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_announcements_active ON app_announcements (published_at DESC) WHERE is_active = TRUE;

INSERT INTO schema_migrations (version) VALUES ('001_initial_schema') ON CONFLICT DO NOTHING;
