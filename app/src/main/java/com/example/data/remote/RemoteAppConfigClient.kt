package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class RemoteAppConfig(
    val latestVersion: String,
    val minimumSupportedVersion: String,
    val maintenanceEnabled: Boolean,
    val maintenanceMessage: String,
    val announcementEnabled: Boolean,
    val announcementTitle: String,
    val announcementMessage: String,
    val announcementUrl: String,
    val features: Map<String, Boolean>
) {
    companion object {
        fun fromJson(json: JSONObject): RemoteAppConfig {
            val app = json.optJSONObject("app") ?: JSONObject()
            val maintenance = json.optJSONObject("maintenance") ?: JSONObject()
            val announcement = json.optJSONObject("announcement") ?: JSONObject()
            val featuresJson = json.optJSONObject("features") ?: JSONObject()
            val knownFeatures = listOf("quran_search", "daily_content", "qibla", "tasbeeh", "duas", "ai_assistant")
            val features = knownFeatures.associateWith { key -> featuresJson.optBoolean(key, key != "ai_assistant") }
            return RemoteAppConfig(
                latestVersion = app.optString("latest_version", "1.0.0"),
                minimumSupportedVersion = json.optString("minimum_supported_version", "1.0.0"),
                maintenanceEnabled = maintenance.optBoolean("enabled", false),
                maintenanceMessage = maintenance.optString("message", "Noor Muslim is temporarily under maintenance."),
                announcementEnabled = announcement.optBoolean("enabled", false),
                announcementTitle = announcement.optString("title", ""),
                announcementMessage = announcement.optString("message", ""),
                announcementUrl = announcement.optString("url", ""),
                features = features
            )
        }
    }
}

/** Fetches public settings; network failure never blocks offline app features. */
object RemoteAppConfigClient {
    private const val CONFIG_URL = "https://noor-muslim-python-api-production.up.railway.app/api/v1/app/config"
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetch(): RemoteAppConfig = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(CONFIG_URL).header("Accept", "application/json").get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("Remote app configuration unavailable (" + response.code + ").")
            if (body.isBlank()) throw IOException("Remote app configuration was empty.")
            RemoteAppConfig.fromJson(JSONObject(body))
        }
    }
}