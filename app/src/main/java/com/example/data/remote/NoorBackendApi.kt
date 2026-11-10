package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Read-only client for the Noor Muslim Python API hosted on Railway.
 *
 * The existing bundled Quran asset and Supabase client remain the app's
 * offline-first/fallback sources. This client is additive and does not
 * replace either one.
 */
object NoorBackendApi {
    private const val BASE_URL =
        "https://noor-muslim-python-api-production.up.railway.app"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun health(): JSONObject = withContext(Dispatchers.IO) {
        JSONObject(getText("$BASE_URL/health"))
    }

    suspend fun getSurahs(): JSONArray = withContext(Dispatchers.IO) {
        JSONArray(getText("$BASE_URL/api/v1/quran/surahs"))
    }

    suspend fun getSurahAyahs(surahNumber: Int): JSONArray = withContext(Dispatchers.IO) {
        require(surahNumber in 1..114) { "Surah number must be between 1 and 114." }
        JSONArray(getText("$BASE_URL/api/v1/quran/surahs/$surahNumber/ayahs"))
    }

    suspend fun searchQuran(query: String, limit: Int = 20): JSONArray =
        withContext(Dispatchers.IO) {
            val cleanedQuery = query.trim()
            require(cleanedQuery.length in 2..80) {
                "Search text must contain between 2 and 80 characters."
            }
            require(limit in 1..50) { "Search limit must be between 1 and 50." }

            val url = "$BASE_URL/api/v1/quran/search".toHttpUrl()
                .newBuilder()
                .addQueryParameter("q", cleanedQuery)
                .addQueryParameter("limit", limit.toString())
                .build()

            JSONArray(getText(url.toString()))
        }

    private fun getText(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException(
                    "Noor Muslim API request failed (${response.code}): " +
                        body.take(300)
                )
            }
            if (body.isBlank()) {
                throw IOException("Noor Muslim API returned an empty response.")
            }
            return body
        }
    }
}
