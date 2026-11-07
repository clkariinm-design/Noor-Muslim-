package com.example.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
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
 * Foreground playback for Adhan. Audio is cached silently in app-private storage;
 * when not cached yet, ExoPlayer streams the licensed Archive.org source and a
 * background worker stores a local copy for later offline playback.
 */
class AdhanPlaybackService : Service() {
    private var player: ExoPlayer? = null
    private var resumeQuranAfterAdhan = false
    private val io = Executors.newSingleThreadExecutor()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_ALARM)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build().also { exo ->
                exo.volume = 0.72f
                exo.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) finishAdhan()
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        finishAdhan()
                    }
                })
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                player?.stop()
                finishAdhan()
                return START_NOT_STICKY
            }
            ACTION_PLAY, ACTION_TEST -> {
                val name = intent.getStringExtra(AdhanScheduler.EXTRA_PRAYER) ?: PrayerType.DHUHR.name
                val prayer = runCatching { PrayerType.valueOf(name) }.getOrDefault(PrayerType.DHUHR)
                startForeground(NOTIFICATION_ID, buildNotification(prayer))
                if (AudioPlayerManager.playbackState.value == PlaybackState.PLAYING) {
                    resumeQuranAfterAdhan = true
                    AudioPlayerManager.pause()
                }
                play(prayer)
            }
        }
        return START_NOT_STICKY
    }

    private fun play(prayer: PrayerType) {
        val url = AUDIO_URLS[prayer] ?: AUDIO_URLS[PrayerType.DHUHR]!!
        val file = File(filesDir, "adhan/${prayer.name.lowercase()}.mp3")
        file.parentFile?.mkdirs()
        val source = if (file.exists() && file.length() > 20_000L) Uri.fromFile(file) else Uri.parse(url)
        player?.apply {
            stop()
            setMediaItem(MediaItem.fromUri(source))
            prepare()
            playWhenReady = true
        }
        if (!file.exists()) {
            io.execute {
                runCatching {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    connection.instanceFollowRedirects = true
                    connection.connect()
                    connection.inputStream.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    connection.disconnect()
                }
            }
        }
    }

    private fun finishAdhan() {
        if (resumeQuranAfterAdhan) {
            resumeQuranAfterAdhan = false
            AudioPlayerManager.resume()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Adhan", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Prayer call audio"
                setSound(null, null)
                if (manager.isNotificationPolicyAccessGranted) {
                    setBypassDnd(true)
                }
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(prayer: PrayerType): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, AdhanPlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Adhan")
            .setContentText("The call to prayer is playing: ${prayer.name.lowercase().replaceFirstChar { it.uppercase() }}")
            .setContentIntent(openApp)
            .setOngoing(true)
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
        io.shutdown()
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY = "com.example.noormuslim.ADHAN_PLAY"
        const val ACTION_TEST = "com.example.noormuslim.ADHAN_TEST"
        const val ACTION_STOP = "com.example.noormuslim.ADHAN_STOP"
        private const val CHANNEL_ID = "noor_adhan_audio"
        private const val NOTIFICATION_ID = 7101

        // Internet Archive item is marked Public Domain Mark 1.0. Separate Fajr
        // recording includes the Fajr-specific phrase. Files are cached on device.
        private val AUDIO_URLS = mapOf(
            PrayerType.FAJR to "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_01_Fajr_Adhan.mp3",
            PrayerType.DHUHR to "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_02_Dhuhr_Adhan.mp3",
            PrayerType.ASR to "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_03_Asr_Adhan.mp3",
            PrayerType.MAGHRIB to "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_04_Maghrib_Adhan.mp3",
            PrayerType.ISHA to "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_05_Isha_Adhan.mp3"
        )

        fun playTest(context: android.content.Context, prayer: PrayerType) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_TEST
                putExtra(AdhanScheduler.EXTRA_PRAYER, prayer.name)
            }
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: android.content.Context) {
            context.startService(Intent(context, AdhanPlaybackService::class.java).setAction(ACTION_STOP))
        }
    }
}
