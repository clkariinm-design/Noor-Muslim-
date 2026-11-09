package com.example.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * Quietly prepares Quran text for offline reading in small network-bound batches.
 * No notification, progress screen, or user-facing download counter is shown.
 */
class QuranTextPrefetchWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val start = inputData.getInt(KEY_START_SURAH, 1).coerceIn(1, 114)
        val end = minOf(start + BATCH_SIZE - 1, 114)

        return try {
            for (surah in start..end) {
                if (!QuranRepository.hasCompleteTranslations(applicationContext, surah)) {
                    QuranRepository.cacheSurahPayload(applicationContext, surah)
                }
            }

            val next = end + 1
            if (next <= 114) enqueueBatch(applicationContext, next)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val KEY_START_SURAH = "start_surah"
        private const val BATCH_SIZE = 8
        private const val UNIQUE_PREFIX = "noor-quran-prefetch-"

        fun enqueue(context: Context) {
            enqueueBatch(context.applicationContext, 1)
        }

        private fun enqueueBatch(context: Context, startSurah: Int) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<QuranTextPrefetchWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_START_SURAH to startSurah))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_PREFIX + startSurah,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
