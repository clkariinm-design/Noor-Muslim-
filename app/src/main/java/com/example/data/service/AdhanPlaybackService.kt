package com.example.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.MainActivity
import com.example.data.model.PrayerType
import com.example.data.repository.AudioPlayerManager
import com.example.data.repository.PlaybackState
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Dedicated foreground playback service for the five daily Adhans.
 * The player is created only after the service has entered foreground state,
 * avoiding slow service startup and Android foreground-service timeouts.
 */
class AdhanPlaybackService : Service() {
    private var player: ExoPlayer? = null
    private var resumeQuranAfterAdhan = false
    private var activePrayer: PrayerType = PrayerType.DHUHR
    private var activeCacheFile: File? = null
    private var playingFromCache = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                player?.stop()
                finishAdhan()
                return START_NOT_STICKY
            }
            ACTION_PLAY, ACTION_TEST -> {
                val prayerName = intent.getStringExtra(AdhanScheduler.EXTRA_PRAYER) ?: PrayerType.DHUHR.name
                val prayer = runCatching { PrayerType.valueOf(prayerName) }.getOrDefault(PrayerType.DHUHR)

                // Must happen immediately, before player creation or any network work.
                startForeground(NOTIFICATION_ID, buildNotification(prayer))

                if (AudioPlayerManager.playbackState.value == PlaybackState.PLAYING) {
                    resumeQuranAfterAdhan = true
                    AudioPlayerManager.pause()
                }

                try {
                    play(prayer)
                } catch (error: Exception) {
                    Log.e(TAG, "Unable to start Adhan playback for $prayer", error)
                    finishAdhan()
                }
            }
            else -> stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    private fun getOrCreatePlayer(): ExoPlayer {
        player?.let { return it }
        return ExoPlayer.Builder(applicationContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_ALARM)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { exo ->
                exo.volume = 1.0f
                exo.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d(TAG, "Playback state=$state prayer=$activePrayer cached=$playingFromCache")
                        if (state == Player.STATE_ENDED) finishAdhan()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(TAG, "isPlaying=$isPlaying prayer=$activePrayer cached=$playingFromCache")
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e(
                            TAG,
                            "Adhan playback error prayer=$activePrayer cached=$playingFromCache code=${error.errorCodeName}",
                            error
                        )
                        val cachedFile = activeCacheFile
                        if (playingFromCache && cachedFile != null) {
                            // A stale or damaged cache must never silence Adhan permanently.
                            cachedFile.delete()
                            playingFromCache = false
                            retryFromServer(activePrayer)
                        } else {
                            finishAdhan()
                        }
                    }
                })
                player = exo
            }
    }

    private fun play(prayer: PrayerType) {
        val file = File(filesDir, "adhan/${prayer.name.lowercase()}.mp3")
        file.parentFile?.mkdirs()
        activePrayer = prayer
        activeCacheFile = file
        playingFromCache = file.exists() && file.length() > MIN_AUDIO_BYTES

        val source = if (playingFromCache) Uri.fromFile(file) else Uri.parse(audioUrl(prayer))
        Log.i(TAG, "Starting Adhan prayer=$prayer source=${if (playingFromCache) "cache" else "server"}")

        getOrCreatePlayer().apply {
            stop()
            setMediaItem(MediaItem.fromUri(source))
            prepare()
            playWhenReady = true
        }

        if (!playingFromCache) prefetchPrayer(applicationContext, prayer)
    }

    private fun retryFromServer(prayer: PrayerType) {
        Log.w(TAG, "Retrying Adhan from server after cached audio failed: $prayer")
        activePrayer = prayer
        playingFromCache = false
        getOrCreatePlayer().apply {
            stop()
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(audioUrl(prayer)))
            prepare()
            playWhenReady = true
        }
    }

    private fun finishAdhan() {
        if (resumeQuranAfterAdhan) {
            resumeQuranAfterAdhan = false
            AudioPlayerManager.resume()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Adhan playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when the Adhan is playing"
                setSound(null, null)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(prayer: PrayerType): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this,
            1,
            Intent(this, AdhanPlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val prayerLabel = prayer.name.lowercase().replaceFirstChar { it.uppercase() }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Adhan is playing")
            .setContentText("$prayerLabel prayer")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stop)
            .build()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        if (resumeQuranAfterAdhan) {
            resumeQuranAfterAdhan = false
            AudioPlayerManager.resume()
        }
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY = "com.example.noormuslim.ADHAN_PLAY"
        const val ACTION_TEST = "com.example.noormuslim.ADHAN_TEST"
        const val ACTION_STOP = "com.example.noormuslim.ADHAN_STOP"

        private const val TAG = "NoorAdhan"
        private const val CHANNEL_ID = "noor_adhan_playback_v2"
        private const val NOTIFICATION_ID = 7101
        private const val MIN_AUDIO_BYTES = 20_000L

        private const val ADHAN_SERVER = "https://lwfzvhkixpneajkfmwgd.supabase.co/functions/v1/adhan-audio"

        private fun audioUrl(prayer: PrayerType): String {
            val name = when (prayer) {
                PrayerType.FAJR -> "fajr"
                PrayerType.DHUHR -> "dhuhr"
                PrayerType.ASR -> "asr"
                PrayerType.MAGHRIB -> "maghrib"
                PrayerType.ISHA -> "isha"
                PrayerType.SUNRISE -> "dhuhr"
            }
            return "$ADHAN_SERVER?prayer=$name"
        }

        private val cacheExecutor = Executors.newSingleThreadExecutor()

        fun prefetchAll(context: android.content.Context) {
            PrayerType.entries
                .filter { it != PrayerType.SUNRISE }
                .forEach { prefetchPrayer(context.applicationContext, it) }
        }

        private fun prefetchPrayer(context: android.content.Context, prayer: PrayerType) {
            if (prayer == PrayerType.SUNRISE) return
            val source = audioUrl(prayer)
            val target = File(context.filesDir, "adhan/${prayer.name.lowercase()}.mp3")
            if (target.exists() && target.length() > MIN_AUDIO_BYTES) return
            cacheExecutor.execute {
                val temporary = File(target.parentFile, target.name + ".part")
                runCatching {
                    target.parentFile?.mkdirs()
                    val connection = URL(source).openConnection() as HttpURLConnection
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    connection.instanceFollowRedirects = true
                    try {
                        connection.connect()
                        connection.inputStream.use { input ->
                            temporary.outputStream().use { output -> input.copyTo(output) }
                        }
                        if (temporary.length() > MIN_AUDIO_BYTES) {
                            temporary.renameTo(target)
                        } else {
                            temporary.delete()
                        }
                    } finally {
                        connection.disconnect()
                    }
                }.onFailure {
                    Log.w(TAG, "Background Adhan audio cache failed for $prayer", it)
                    temporary.delete()
                }
            }
        }

        fun playTest(context: android.content.Context, prayer: PrayerType) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_TEST
                putExtra(AdhanScheduler.EXTRA_PRAYER, prayer.name)
            }
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: android.content.Context) {
            context.startService(
                Intent(context, AdhanPlaybackService::class.java).setAction(ACTION_STOP)
            )
        }
    }
}
