package com.example.data.repository

import android.content.Context
import com.example.data.model.AvailableQaris
import com.example.data.model.DownloadState
import com.example.data.model.Qari
import com.example.data.model.SurahDownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object QuranAudioDownloadManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _downloadStatuses = MutableStateFlow<Map<String, SurahDownloadStatus>>(emptyMap())
    val downloadStatuses: StateFlow<Map<String, SurahDownloadStatus>> = _downloadStatuses.asStateFlow()

    private fun statusKey(surahNumber: Int, qariId: String): String = "${qariId}_$surahNumber"

    /**
     * Initializes the download manager and scans internal storage for completed downloads.
     */
    fun initialize(context: Context) {
        scope.launch {
            val rootDir = getAudioRootDirectory(context)
            if (!rootDir.exists()) {
                rootDir.mkdirs()
            }
            refreshDownloadedStatuses(context)
            // Audio is cached only when a user starts listening to a surah.
            // This avoids downloading the entire reciter library on first launch.
        }
    }

    /**
     * Quietly fills the local audio cache one Surah at a time.
     * Failed or unavailable files are skipped; the player streams them on demand.
     */
    fun downloadAllSurahsInBackground(context: Context, qari: Qari = AvailableQaris.first()) {
        val appContext = context.applicationContext
        scope.launch {
            for (surah in QuranRepository.allSurahs) {
                if (isSurahDownloaded(appContext, surah.number, qari.id)) continue
                downloadSurah(appContext, surah.number, qari)
                val key = statusKey(surah.number, qari.id)
                while (activeJobs[key]?.isActive == true) delay(1_000)
                delay(1_000)
            }
        }
    }

    private fun getAudioRootDirectory(context: Context): File {
        return File(context.filesDir, "quran_audio")
    }

    private fun getSurahDirectory(context: Context, qariId: String, surahNumber: Int): File {
        return File(getAudioRootDirectory(context), "$qariId/surah_$surahNumber")
    }

    fun getLocalAyahAudioFile(context: Context, qari: Qari, surahNumber: Int, ayahNumber: Int): File? {
        val sDir = getSurahDirectory(context, qari.id, surahNumber)
        val file = File(sDir, "ayah_$ayahNumber.mp3")
        return if (file.exists() && file.length() > 1024L) file else null
    }

    fun isSurahDownloaded(context: Context, surahNumber: Int, qariId: String): Boolean {
        val surah = QuranRepository.allSurahs.firstOrNull { it.number == surahNumber } ?: return false
        val sDir = getSurahDirectory(context, qariId, surahNumber)
        if (!sDir.exists() || !sDir.isDirectory) return false

        // Check if all Ayahs for this surah exist and are non-empty
        for (a in 1..surah.ayahCount) {
            val file = File(sDir, "ayah_$a.mp3")
            if (!file.exists() || file.length() < 1024L) {
                return false
            }
        }
        return true
    }

    suspend fun refreshDownloadedStatuses(context: Context) = withContext(Dispatchers.IO) {
        val updatedMap = _downloadStatuses.value.toMutableMap()
        for (qari in AvailableQaris) {
            for (surah in QuranRepository.allSurahs) {
                val key = statusKey(surah.number, qari.id)
                val isDownloaded = isSurahDownloaded(context, surah.number, qari.id)
                if (isDownloaded) {
                    val sDir = getSurahDirectory(context, qari.id, surah.number)
                    val totalSize = sDir.listFiles()?.sumOf { it.length() } ?: 0L
                    updatedMap[key] = SurahDownloadStatus(
                        surahNumber = surah.number,
                        qariId = qari.id,
                        state = DownloadState.COMPLETED,
                        progressPercent = 100,
                        downloadedAyahs = surah.ayahCount,
                        totalAyahs = surah.ayahCount,
                        downloadedBytes = totalSize,
                        totalBytes = totalSize,
                        localDirectoryPath = sDir.absolutePath
                    )
                } else if (updatedMap[key]?.state == DownloadState.COMPLETED) {
                    updatedMap.remove(key)
                }
            }
        }
        _downloadStatuses.value = updatedMap
    }

    /**
     * Downloads an individual Surah for the selected reciter.
     * Downloads are stored in app-specific internal storage and verified per Ayah.
     */
    fun downloadSurah(context: Context, surahNumber: Int, qari: Qari) {
        val key = statusKey(surahNumber, qari.id)
        if (activeJobs[key]?.isActive == true) return

        val surah = QuranRepository.allSurahs.firstOrNull { it.number == surahNumber } ?: return
        val targetDir = getSurahDirectory(context, qari.id, surahNumber)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        updateStatus(
            key,
            SurahDownloadStatus(
                surahNumber = surahNumber,
                qariId = qari.id,
                state = DownloadState.DOWNLOADING,
                progressPercent = 0,
                downloadedAyahs = 0,
                totalAyahs = surah.ayahCount
            )
        )

        val job = scope.launch {
            var downloadedAyahs = 0
            var downloadedBytes = 0L

            try {
                for (ayahNum in 1..surah.ayahCount) {
                    val destFile = File(targetDir, "ayah_$ayahNum.mp3")

                    // Skip if already completely downloaded
                    if (destFile.exists() && destFile.length() > 1024L) {
                        downloadedAyahs++
                        downloadedBytes += destFile.length()
                        val percent = ((downloadedAyahs.toFloat() / surah.ayahCount) * 100).toInt()
                        updateStatus(
                            key,
                            SurahDownloadStatus(
                                surahNumber = surahNumber,
                                qariId = qari.id,
                                state = DownloadState.DOWNLOADING,
                                progressPercent = percent,
                                downloadedAyahs = downloadedAyahs,
                                totalAyahs = surah.ayahCount,
                                downloadedBytes = downloadedBytes
                            )
                        )
                        continue
                    }

                    val url = QuranRepository.getAyahAudioUrl(qari, surahNumber, ayahNum)
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        throw Exception("HTTP ${response.code} downloading $url")
                    }

                    val body = response.body ?: throw Exception("Empty body from $url")
                    val tempFile = File(targetDir, "ayah_${ayahNum}.tmp")

                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(8192)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloadedBytes += read
                            }
                        }
                    }

                    // Rename temp to target to ensure atomic file completion
                    if (tempFile.length() > 512L) {
                        if (destFile.exists()) destFile.delete()
                        tempFile.renameTo(destFile)
                        downloadedAyahs++
                    } else {
                        tempFile.delete()
                        throw Exception("File size too small (corrupt download) for Ayah $ayahNum")
                    }

                    val percent = ((downloadedAyahs.toFloat() / surah.ayahCount) * 100).toInt()
                    updateStatus(
                        key,
                        SurahDownloadStatus(
                            surahNumber = surahNumber,
                            qariId = qari.id,
                            state = DownloadState.DOWNLOADING,
                            progressPercent = percent,
                            downloadedAyahs = downloadedAyahs,
                            totalAyahs = surah.ayahCount,
                            downloadedBytes = downloadedBytes
                        )
                    )
                }

                // Completed successfully
                updateStatus(
                    key,
                    SurahDownloadStatus(
                        surahNumber = surahNumber,
                        qariId = qari.id,
                        state = DownloadState.COMPLETED,
                        progressPercent = 100,
                        downloadedAyahs = surah.ayahCount,
                        totalAyahs = surah.ayahCount,
                        downloadedBytes = downloadedBytes,
                        totalBytes = downloadedBytes,
                        localDirectoryPath = targetDir.absolutePath
                    )
                )
            } catch (e: Exception) {
                updateStatus(
                    key,
                    SurahDownloadStatus(
                        surahNumber = surahNumber,
                        qariId = qari.id,
                        state = DownloadState.FAILED,
                        progressPercent = ((downloadedAyahs.toFloat() / surah.ayahCount) * 100).toInt(),
                        downloadedAyahs = downloadedAyahs,
                        totalAyahs = surah.ayahCount,
                        downloadedBytes = downloadedBytes,
                        errorMessage = e.message ?: "Download failed"
                    )
                )
            } finally {
                activeJobs.remove(key)
            }
        }

        activeJobs[key] = job
    }

    fun pauseDownload(surahNumber: Int, qariId: String) {
        val key = statusKey(surahNumber, qariId)
        activeJobs[key]?.cancel()
        activeJobs.remove(key)

        val current = _downloadStatuses.value[key]
        if (current != null && current.state == DownloadState.DOWNLOADING) {
            updateStatus(key, current.copy(state = DownloadState.PAUSED))
        }
    }

    fun cancelDownload(context: Context, surahNumber: Int, qariId: String) {
        val key = statusKey(surahNumber, qariId)
        activeJobs[key]?.cancel()
        activeJobs.remove(key)

        val sDir = getSurahDirectory(context, qariId, surahNumber)
        if (sDir.exists()) {
            sDir.deleteRecursively()
        }

        val map = _downloadStatuses.value.toMutableMap()
        map.remove(key)
        _downloadStatuses.value = map
    }

    fun deleteSurahDownload(context: Context, surahNumber: Int, qariId: String) {
        cancelDownload(context, surahNumber, qariId)
    }

    fun getTotalDownloadedBytes(context: Context): Long {
        val root = getAudioRootDirectory(context)
        return if (root.exists()) calculateDirectorySize(root) else 0L
    }

    fun getDownloadedSurahsList(context: Context): List<Pair<Int, Qari>> {
        val result = mutableListOf<Pair<Int, Qari>>()
        for (qari in AvailableQaris) {
            for (surah in QuranRepository.allSurahs) {
                if (isSurahDownloaded(context, surah.number, qari.id)) {
                    result.add(Pair(surah.number, qari))
                }
            }
        }
        return result
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun calculateDirectorySize(directory: File): Long {
        var length = 0L
        val files = directory.listFiles() ?: return 0L
        for (file in files) {
            length += if (file.isFile) file.length() else calculateDirectorySize(file)
        }
        return length
    }

    private fun updateStatus(key: String, status: SurahDownloadStatus) {
        val map = _downloadStatuses.value.toMutableMap()
        map[key] = status
        _downloadStatuses.value = map
    }
}
