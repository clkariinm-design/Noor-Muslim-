package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.AudioRepeatMode
import com.example.data.model.AvailableQaris
import com.example.data.model.Qari
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlaybackState {
    IDLE,
    PREPARING,
    PLAYING,
    PAUSED,
    ERROR
}

data class CurrentPlayingTrack(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val totalAyahs: Int,
    val qari: Qari,
    val isOfflineSource: Boolean = false
)

object AudioPlayerManager {

    private var exoPlayer: ExoPlayer? = null
    private var appContext: Context? = null
    private var positionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<CurrentPlayingTrack?>(null)
    val currentTrack: StateFlow<CurrentPlayingTrack?> = _currentTrack.asStateFlow()

    private val _repeatMode = MutableStateFlow(AudioRepeatMode.OFF)
    val repeatMode: StateFlow<AudioRepeatMode> = _repeatMode.asStateFlow()

    // Backward compatibility for existing code expecting isRepeatOne StateFlow
    private val _isRepeatOne = MutableStateFlow(false)
    val isRepeatOne: StateFlow<Boolean> = _isRepeatOne.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    var onAyahCompletedListener: ((surahNumber: Int, ayahNumber: Int) -> Unit)? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            getOrCreatePlayer(context.applicationContext)
        }
    }

    @OptIn(UnstableApi::class)
    private fun getOrCreatePlayer(context: Context): ExoPlayer {
        exoPlayer?.let { return it }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(context.applicationContext)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.value = PlaybackState.PREPARING
                    }
                    Player.STATE_READY -> {
                        _durationMs.value = player.duration.coerceAtLeast(0L)
                        _playbackState.value = if (player.playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSED
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.value = PlaybackState.IDLE
                        handleTrackCompletion()
                    }
                    Player.STATE_IDLE -> {
                        _playbackState.value = PlaybackState.IDLE
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    _playbackState.value = PlaybackState.PLAYING
                    startPositionUpdates()
                } else {
                    if (_playbackState.value != PlaybackState.PREPARING && _playbackState.value != PlaybackState.IDLE) {
                        _playbackState.value = PlaybackState.PAUSED
                    }
                    stopPositionUpdates()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _playbackState.value = PlaybackState.ERROR
                stopPositionUpdates()
            }
        })

        exoPlayer = player
        return player
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { p ->
                    _currentPositionMs.value = p.currentPosition.coerceAtLeast(0L)
                    val dur = p.duration
                    if (dur > 0) _durationMs.value = dur
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun handleTrackCompletion() {
        val track = _currentTrack.value ?: return

        when (_repeatMode.value) {
            AudioRepeatMode.REPEAT_ONE -> {
                playAyah(
                    surahNumber = track.surahNumber,
                    surahName = track.surahName,
                    ayahNumber = track.ayahNumber,
                    totalAyahs = track.totalAyahs,
                    qari = track.qari
                )
            }
            AudioRepeatMode.REPEAT_SURAH -> {
                val nextAyah = if (track.ayahNumber < track.totalAyahs) track.ayahNumber + 1 else 1
                playAyah(
                    surahNumber = track.surahNumber,
                    surahName = track.surahName,
                    ayahNumber = nextAyah,
                    totalAyahs = track.totalAyahs,
                    qari = track.qari
                )
            }
            AudioRepeatMode.OFF -> {
                onAyahCompletedListener?.invoke(track.surahNumber, track.ayahNumber)
            }
        }
    }

    fun playAyah(
        surahNumber: Int,
        surahName: String,
        ayahNumber: Int,
        totalAyahs: Int,
        qari: Qari = AvailableQaris[0]
    ) {
        val context = appContext
        val player = if (context != null) getOrCreatePlayer(context) else exoPlayer
        if (player == null) {
            _playbackState.value = PlaybackState.ERROR
            return
        }

        // Playback streams by default. Audio is stored only through an explicit download action.
        val localFile = if (context != null) {
            QuranAudioDownloadManager.getLocalAyahAudioFile(context, qari, surahNumber, ayahNumber)
        } else null

        val isOffline = localFile != null
        val mediaUri = if (isOffline) {
            Uri.fromFile(localFile)
        } else {
            Uri.parse(QuranRepository.getAyahAudioUrl(qari, surahNumber, ayahNumber))
        }

        val metadata = MediaMetadata.Builder()
            .setTitle("$surahName: $ayahNumber/$totalAyahs")
            .setArtist(qari.name)
            .setAlbumTitle("The Holy Quran")
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(mediaUri)
            .setMediaMetadata(metadata)
            .build()

        _currentTrack.value = CurrentPlayingTrack(
            surahNumber = surahNumber,
            surahName = surahName,
            ayahNumber = ayahNumber,
            totalAyahs = totalAyahs,
            qari = qari,
            isOfflineSource = isOffline
        )

        _playbackState.value = PlaybackState.PREPARING
        _currentPositionMs.value = 0L

        player.stop()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun stop() {
        stopPositionUpdates()
        exoPlayer?.stop()
        _playbackState.value = PlaybackState.IDLE
        _currentPositionMs.value = 0L
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs.coerceAtLeast(0L))
        _currentPositionMs.value = positionMs
    }

    fun toggleRepeat() {
        val nextMode = when (_repeatMode.value) {
            AudioRepeatMode.OFF -> AudioRepeatMode.REPEAT_ONE
            AudioRepeatMode.REPEAT_ONE -> AudioRepeatMode.REPEAT_SURAH
            AudioRepeatMode.REPEAT_SURAH -> AudioRepeatMode.OFF
        }
        setRepeatMode(nextMode)
    }

    fun setRepeatMode(mode: AudioRepeatMode) {
        _repeatMode.value = mode
        _isRepeatOne.value = (mode == AudioRepeatMode.REPEAT_ONE)
    }

    fun release() {
        stopPositionUpdates()
        exoPlayer?.release()
        exoPlayer = null
        _playbackState.value = PlaybackState.IDLE
    }
}
