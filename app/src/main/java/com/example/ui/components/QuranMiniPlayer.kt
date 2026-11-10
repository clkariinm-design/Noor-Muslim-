package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioRepeatMode
import com.example.data.model.LocalizedStrings
import com.example.data.repository.AudioPlayerManager
import com.example.data.repository.CurrentPlayingTrack
import com.example.data.repository.PlaybackState
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright

@Composable
fun QuranMiniPlayer(
    currentTrack: CurrentPlayingTrack?,
    playbackState: PlaybackState,
    repeatMode: AudioRepeatMode,
    currentPositionMs: Long,
    durationMs: Long,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier,
    onExpand: () -> Unit = {}
) {
    val isVisible = currentTrack != null && playbackState != PlaybackState.IDLE

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (currentTrack == null) return@AnimatedVisibility

        val progress = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        } else 0f

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable { onExpand() }
                .testTag("quran_mini_player"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top progress line
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = IslamicGold,
                    trackColor = Color.Transparent
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Track Information
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IslamicEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${currentTrack.ayahNumber}",
                                color = IslamicGoldBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentTrack.surahName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (currentTrack.isOfflineSource) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.OfflinePin,
                                        contentDescription = strings.offlineAudioReady,
                                        tint = IslamicEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${currentTrack.qari.name} • ${currentTrack.ayahNumber}/${currentTrack.totalAyahs}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Repeat Mode Toggle
                        IconButton(
                            onClick = { AudioPlayerManager.toggleRepeat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    AudioRepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                    AudioRepeatMode.REPEAT_SURAH -> Icons.Default.Repeat
                                    AudioRepeatMode.OFF -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = if (repeatMode != AudioRepeatMode.OFF) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Play/Pause
                        if (playbackState == PlaybackState.PREPARING) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp),
                                strokeWidth = 2.dp,
                                color = IslamicEmerald
                            )
                        } else {
                            IconButton(
                                onClick = { AudioPlayerManager.togglePlayPause() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState == PlaybackState.PLAYING) strings.pauseAudio else strings.playAudio,
                                    tint = IslamicEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Close/Stop
                        IconButton(
                            onClick = { AudioPlayerManager.stop() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
