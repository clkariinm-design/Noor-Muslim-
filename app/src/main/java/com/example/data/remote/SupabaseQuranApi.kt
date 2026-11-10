package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Read-only client for the public Noor Muslim Quran catalogue. */
object SupabaseQuranApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl get() = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val publishableKey get() = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    val isConfigured get() = baseUrl.startsWith("https://") && publishableKey.startsWith("sb_publishable_")

    suspend fun getSurahPayload(surahNumber: Int): JSONObject = withContext(Dispatchers.IO) {
        require(surahNumber in 1..114) { "Surah number must be between 1 and 114" }
        check(isConfigured) { "Supabase is not configured." }
        val request = Request.Builder()
            .url("$baseUrl/functions/v1/quran-surah?surah=$surahNumber")
            .header("apikey", publishableKey)
            .header("Accept", "application/json")
            .get()
            .build()
        client.newCall(request).execute().use { result ->
            val body = result.body?.string().orEmpty()
            if (!result.isSuccessful) throw IOException("Quran text service unavailable (" + result.code + "): " + body)
            JSONObject(body)
        }
    }

    suspend fun getSurahs() = get("quran_surahs?select=*&order=surah_number.asc")
    suspend fun getReciters() = get("quran_reciters?select=id,slug,name,country,style,sort_order&is_active=eq.true&order=sort_order.asc")
    suspend fun getTranslationEditions() = get("quran_translations?select=id,language_code,language_name,translator_name,edition_slug,source_name,license_name,license_url&is_active=eq.true&order=language_code.asc")

    suspend fun getAyahTranslations(surahNumber: Int, translationId: String): JSONArray {
        require(surahNumber in 1..114) { "Surah number must be between 1 and 114" }
        require(translationId.matches(Regex("[0-9a-fA-F-]{36}"))) { "Invalid translation ID" }
        return get("quran_ayah_translations?select=surah_number,ayah_number,text&surah_number=eq.$surahNumber&translation_id=eq.$translationId&order=ayah_number.asc")
    }

    suspend fun getAudioAssets(surahNumber: Int, reciterId: String): JSONArray {
        require(surahNumber in 1..114) { "Surah number must be between 1 and 114" }
        require(reciterId.matches(Regex("[0-9a-fA-F-]{36}"))) { "Invalid reciter ID" }
        return get("quran_audio_assets?select=id,surah_number,ayah_number,asset_type,storage_provider,object_path,public_url,source_name&surah_number=eq.$surahNumber&reciter_id=eq.$reciterId&is_active=eq.true&order=ayah_number.asc")
    }

    private suspend fun get(path: String): JSONArray = withContext(Dispatchers.IO) {
        check(isConfigured) { "Supabase is not configured. Check .env / .env.example." }
        val request = Request.Builder()
            .url("$baseUrl/rest/v1/$path")
            .header("apikey", publishableKey)
            .header("Accept", "application/json")
            .get()
            .build()
        client.newCall(request).execute().use { result ->
            val body = result.body?.string().orEmpty()
            if (!result.isSuccessful) throw IOException("Quran service request failed (" + result.code + "): " + body)
            JSONArray(body)
        }
    }
}
