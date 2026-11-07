package com.example.data.repository

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.model.AvailableQaris
import com.example.data.model.Qari
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val qari: Qari
)

object AudioPlayerManager {

    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<CurrentPlayingTrack?>(null)
    val currentTrack: StateFlow<CurrentPlayingTrack?> = _currentTrack.asStateFlow()

    private val _isRepeatOne = MutableStateFlow(false)
    val isRepeatOne: StateFlow<Boolean> = _isRepeatOne.asStateFlow()

    var onAyahCompletedListener: ((surahNumber: Int, ayahNumber: Int) -> Unit)? = null

    fun playAyah(
        surahNumber: Int,
        surahName: String,
        ayahNumber: Int,
        totalAyahs: Int,
        qari: Qari = AvailableQaris[0]
    ) {
        val audioUrl = QuranRepository.getAyahAudioUrl(qari, surahNumber, ayahNumber)

        stop()
        _playbackState.value = PlaybackState.PREPARING
        _currentTrack.value = CurrentPlayingTrack(
            surahNumber = surahNumber,
            surahName = surahName,
            ayahNumber = ayahNumber,
            totalAyahs = totalAyahs,
            qari = qari
        )

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                    _playbackState.value = PlaybackState.PLAYING
                }
                setOnCompletionListener {
                    if (_isRepeatOne.value) {
                        playAyah(surahNumber, surahName, ayahNumber, totalAyahs, qari)
                    } else {
                        _playbackState.value = PlaybackState.IDLE
                        onAyahCompletedListener?.invoke(surahNumber, ayahNumber)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    _playbackState.value = PlaybackState.ERROR
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.ERROR
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playbackState.value = PlaybackState.PAUSED
        } else {
            player.start()
            _playbackState.value = PlaybackState.PLAYING
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playbackState.value = PlaybackState.PAUSED
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
            _playbackState.value = PlaybackState.IDLE
        }
    }

    fun toggleRepeat() {
        _isRepeatOne.value = !_isRepeatOne.value
    }
}
