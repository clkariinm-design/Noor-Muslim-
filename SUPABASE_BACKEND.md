# Noor Muslim — Supabase backend

Project: `noor-muslim`  
Project ref: `lwfzvhkixpneajkfmwgd`  
Region: `eu-central-1` (Frankfurt)

## Android configuration

The project already applies Google's Secrets Gradle Plugin and reads `.env`, with `.env.example` as its build fallback. Supabase URL and the **publishable** key are configured there. Never place a service-role or secret key in GitHub or an APK.

The Android API wrapper is `SupabaseQuranApi`. Quran reading calls the public `quran-surah` Supabase Edge Function; it currently fetches Uthmani Arabic plus English, Somali, Turkish, and French editions from AlQuran Cloud. The app no longer bundles the Quran text. On first successful read, each surah payload is validated and saved in app-private storage; later reads use that cache, so already cached surahs remain readable offline. A quiet WorkManager job prepares all 114 surahs in batches of eight while a network is available. The reader opens the requested surah immediately and does not show download progress or storage counters. The same wrapper exposes read operations for catalogue metadata, reciters, translation editions, ayah translations, and audio metadata.

## Database

The live database has five tables: `quran_surahs` (114 surahs), `quran_reciters` (10 catalogue entries), `quran_translations` (English/Somali/Turkish/French edition placeholders), `quran_ayah_translations` (licensed text keyed by edition/surah/ayah), and `quran_audio_assets` (per-ayah or per-surah audio metadata).

All five tables have RLS enabled. Anonymous and authenticated clients can only read active catalogue rows. No public write policy exists. The Supabase Security Advisor returned no findings after initial schema setup.

## Content status

Translation editions currently contain metadata placeholders only; the ayah translation table is empty. Do not present these as populated translations until a source and redistribution licence are verified and the actual text is imported.

No audio files have been uploaded to this project yet. The app's existing EveryAyah playback URLs remain the current playback path. Audio caching has been changed to download only the surah the user starts listening to; it no longer bulk-downloads all 114 surahs at first launch. Populate `quran_audio_assets` only after verifying that the recording source permits redistribution/hosting. The bundled Quran text asset has been removed from the Android project.

## Next integration stages

1. Store the validated Arabic ayah text in Supabase as the primary source, with AlQuran Cloud as a fallback API; add a completeness check before serving database content.
2. Add verified licensed translation datasets and import them; the current translation rows are placeholders and must not be presented as complete offline translations.
3. Add audio provider metadata and test every reciter's actual file paths.
4. Configure object storage/CDN for authorized MP3s.
5. Build and test the Android app, then exercise online/offline reading, streaming, interrupted downloads, and audio playback on a physical device.
