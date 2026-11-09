# Noor Muslim — Supabase backend

Project: `noor-muslim`  
Project ref: `lwfzvhkixpneajkfmwgd`  
Region: `eu-central-1` (Frankfurt)

## Android configuration

The project already applies Google's Secrets Gradle Plugin and reads `.env`, with `.env.example` as its build fallback. Supabase URL and the **publishable** key are configured there. Never place a service-role or secret key in GitHub or an APK.

The Android API wrapper is `SupabaseQuranApi`. Quran reading calls the public `quran-surah` Supabase Edge Function; it currently fetches Uthmani Arabic plus English, Somali, Turkish, and French editions from AlQuran Cloud. The Android app now includes the verbatim Tanzil Uthmani Quran asset (`app/src/main/assets/quran_uthmani.json`) so all 114 surahs and 6,236 numbered ayahs are available immediately without internet. On first read, an online payload is preferred and cached privately; if the service is unavailable, the bundled Arabic text is used and cached. The bundled asset is Arabic-only; translations are supplied by the online payload when available. A quiet WorkManager job prepares all 114 surahs in batches of eight while a network is available. The reader opens the requested surah immediately and does not show download progress or storage counters. The same wrapper exposes read operations for catalogue metadata, reciters, translation editions, ayah translations, and audio metadata.

## Database

The live database has six tables: `quran_surahs` (114 surahs), `quran_ayahs` (6,236 primary Uthmani ayahs), `quran_reciters` (10 catalogue entries), `quran_translations` (English/Somali/Turkish/French edition metadata), `quran_ayah_translations` (licensed text keyed by edition/surah/ayah), and `quran_audio_assets` (per-ayah or per-surah audio metadata). The `quran-surah` Edge Function prefers a complete surah from `quran_ayahs`, uses complete translation rows when present, and falls back to AlQuran Cloud for missing content.

All five tables have RLS enabled. Anonymous and authenticated clients can only read active catalogue rows. No public write policy exists. The Supabase Security Advisor returned no findings after initial schema setup.

## Content status

The Arabic Uthmani text has now been imported into `quran_ayahs`: exactly 6,236 ayahs across all 114 surahs. A post-import database check confirmed 6,236 unique (surah, ayah) keys and zero surah ayah-count mismatches. The source is Tanzil Quran Text (Uthmani, Version 1.1), Copyright (C) 2007-2021 Tanzil Project, Creative Commons Attribution 3.0. The text is stored verbatim and must not be modified. The Android About dialog displays the required attribution and links to Tanzil and its updates page. Dataset packaging/source: `dotquran/corpus`, which documents Tanzil as the original source.\n\nTranslation editions currently contain metadata only; `quran_ayah_translations` is empty. The Edge Function therefore retrieves translations from AlQuran Cloud as an online fallback. Those translations are not yet stored for offline use. Before importing or redistributing any translation edition, verify its specific rights and licence with the translator/publisher.

No audio files have been uploaded to this project yet. The app's existing EveryAyah playback URLs remain the current playback path. Audio caching has been changed to download only the surah the user starts listening to; it no longer bulk-downloads all 114 surahs at first launch. Populate `quran_audio_assets` only after verifying that the recording source permits redistribution/hosting. The bundled Arabic Quran asset remains in the Android project for offline-first reading.

## Next integration stages

1. Keep the verified 6,236 Arabic ayahs in Supabase as the online primary source and the bundled Tanzil asset as the offline fallback; retain the completeness check before serving database content.
2. Add verified licensed translation datasets and import them; the current translation rows are placeholders and must not be presented as complete offline translations.
3. Add audio provider metadata and test every reciter's actual file paths.
4. Configure object storage/CDN for authorized MP3s.
5. Build and test the Android app, then exercise online/offline reading, streaming, interrupted downloads, and audio playback on a physical device.
