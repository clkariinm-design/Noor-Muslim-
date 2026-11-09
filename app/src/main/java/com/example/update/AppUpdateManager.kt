package com.example.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.core.content.getSystemService
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val mandatory: Boolean,
    val releaseNotes: String
)

object AppUpdateManager {
    private const val UPDATE_MANIFEST_URL =
        "https://raw.githubusercontent.com/clkariinm-design/Noor-Muslim-/main/update.json"

    suspend fun checkForUpdate(currentVersionCode: Int): AppUpdateInfo? {
        val connection = (URL(UPDATE_MANIFEST_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            requestMethod = "GET"
        }
        try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val info = AppUpdateInfo(
                versionCode = json.getInt("versionCode"),
                versionName = json.getString("versionName"),
                apkUrl = json.getString("apkUrl"),
                mandatory = json.optBoolean("mandatory", false),
                releaseNotes = json.optString("releaseNotes", "")
            )
            return info.takeIf {
                UpdatePolicy.requiresUpdate(currentVersionCode, it.versionCode, it.mandatory) &&
                    it.apkUrl.startsWith("https://")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun downloadUpdate(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit
    ): Uri {
        val manager = context.getSystemService<DownloadManager>()
            ?: error("Download service is unavailable")
        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Noor Muslim update")
            .setDescription("Downloading the required app update")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "noor-muslim-update.apk"
            )
        val downloadId = manager.enqueue(request)

        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            manager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) error("Update download was not found")
                when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        onProgress(100)
                        return manager.getUriForDownloadedFile(downloadId)
                            ?: error("Downloaded update file is unavailable")
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        error("Update download failed ($reason)")
                    }
                    else -> {
                        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        if (total > 0L) onProgress(((downloaded * 100L) / total).toInt())
                    }
                }
            }
            delay(500)
        }
    }

    fun openInstaller(context: Context, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun openInstallPermissionSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
