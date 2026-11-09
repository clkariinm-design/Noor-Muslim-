# Noor Muslim — Supabase backend

Project: `noor-muslim`  
Project ref: `lwfzvhkixpneajkfmwgd`  
Region: `eu-central-1` (Frankfurt)

## Android configuration

The project already applies Google's Secrets Gradle Plugin and reads `.env`, with `.env.example` as its build fallback. Supabase URL and the **publishable** key are configured there. Never place a service-role or secret key in GitHub or an APK.

The Android API wrapper is `SupabaseQuranApi`. Quran reading calls the public `quran-surah` Supabase Edge Function; it serves Uthmani Arabic from Supabase and English, Somali, Turkish, and French translations from QuranEnc.com, retaining translator/publisher and version metadata. The Android app now includes the verbatim Tanzil Uthmani Quran asset (`app/src/main/assets/quran_uthmani.json`) so all 114 surahs and 6,236 numbered ayahs are available immediately without internet. On first read, an online payload is preferred and cached privately; if the service is unavailable, the bundled Arabic text is used and cached. The bundled asset is Arabic-only; all four translations are quietly cached per surah in app-private storage as the online prefetch worker runs. A quiet WorkManager job prepares all 114 surahs in batches of eight while a network is available. The reader opens the requested surah immediately and does not show download progress or storage counters. The same wrapper exposes read operations for catalogue metadata, reciters, translation editions, ayah translations, and audio metadata.

## Database

The live database has six tables: `quran_surahs` (114 surahs), `quran_ayahs` (6,236 primary Uthmani ayahs), `quran_reciters` (7 active, verified reciters; older unverified entries are inactive), `quran_translations` (English/Somali/Turkish/French edition metadata), `quran_ayah_translations` (licensed text keyed by edition/surah/ayah), and `quran_audio_assets` (per-ayah or per-surah audio metadata). The `quran-surah` Edge Function prefers a complete surah from `quran_ayahs`, uses complete translation rows when present, and falls back to QuranEnc.com for translation editions missing from the database.

All five tables have RLS enabled. Anonymous and authenticated clients can only read active catalogue rows. No public write policy exists. The Supabase Security Advisor returned no findings after initial schema setup.

## Content status

The Arabic Uthmani text has now been imported into `quran_ayahs`: exactly 6,236 ayahs across all 114 surahs. A post-import database check confirmed 6,236 unique (surah, ayah) keys and zero surah ayah-count mismatches. The source is Tanzil Quran Text (Uthmani, Version 1.1), Copyright (C) 2007-2021 Tanzil Project, Creative Commons Attribution 3.0. The text is stored verbatim and must not be modified. The Android About dialog displays the required attribution and links to Tanzil and its updates page. Dataset packaging/source: `dotquran/corpus`, which documents Tanzil as the original source.\n\nThe four active translation metadata rows now identify the QuranEnc.com editions: English (Saheeh International / Noor International Center, 1.1.2), Somali (Abdullah Hasan Yaqoub, 1.0.27), Turkish (Rowwad Translation Center, 1.0.4), and French (Rachid Maach, 1.0.3). The `quran_ayah_translations` table remains empty; the Edge Function retrieves each complete surah from QuranEnc and the Android app quietly caches the full translations locally for offline use. QuranEnc's terms require verbatim reproduction, source/publisher attribution, version and transcript information, and no inappropriate ads alongside translations.

No audio files have been uploaded to this project yet. The app streams from the officially listed Islamic Network / AlQuran Cloud verse-by-verse audio CDN editions. Every configured reciter's API edition identifier and first-ayah audio URL have been verified. Audio caching has been changed to download only the surah the user starts listening to; it no longer bulk-downloads all 114 surahs at first launch. No audio files are hosted in Supabase. Playback streams from the source CDN; the app caches a surah locally only after the user starts listening. Rights holders retain copyright and may request takedown. The bundled Arabic Quran asset remains in the Android project for offline-first reading.

## Next integration stages

1. Keep the verified 6,236 Arabic ayahs in Supabase as the online primary source and the bundled Tanzil asset as the offline fallback; retain the completeness check before serving database content.
2. Optionally mirror complete translation text into Supabase later, with a version-refresh process that preserves QuranEnc's verbatim and attribution conditions.
3. Keep audio provider attribution current and verify source URLs when adding reciters.
4. Configure object storage/CDN for authorized MP3s.
5. Build and test the Android app, then exercise online/offline reading, streaming, interrupted downloads, and audio playback on a physical device.
 
## Translations

QuranEnc.com supplies English (Saheeh International / Noor International Center), Somali (Abdullah Hasan Yaqoub), Turkish (Rowwad Translation Center), and French (Rachid Maach). The app preserves the translation text verbatim and carries publisher/source and version metadata. QuranEnc's republication terms require source/publisher attribution, version and transcript information, no modifications, and no inappropriate ads alongside translations. Source and terms: https://quranenc.com/en/home/api.

## Audio

Recitations stream from the Islamic Network / AlQuran Cloud CDN using the officially listed verse-by-verse editions (Alafasy, Abdul Samad, Sudais, Husary, Shatri, Hudhaify, and Maher Al-Muaiqly). Audio is cached locally only after the user starts listening; no audio is uploaded to our server. Recordings remain copyrighted by their reciters or estates and may be subject to takedown. Source and terms: https://alquran.cloud/terms-and-conditions.
