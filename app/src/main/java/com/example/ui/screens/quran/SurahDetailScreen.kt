package com.example.ui.screens.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.AudioRepeatMode
import com.example.data.model.AvailableQaris
import com.example.data.model.Ayah
import com.example.data.model.LocalizedStrings
import com.example.data.model.Qari
import com.example.data.repository.AudioPlayerManager
import com.example.data.repository.CurrentPlayingTrack
import com.example.data.repository.PlaybackState
import com.example.data.repository.QuranRepository
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.QuranParchment
import kotlinx.coroutines.launch

@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    strings: LocalizedStrings,
    language: AppLanguage,
    appPreferences: AppPreferences,
    playbackState: PlaybackState,
    currentTrack: CurrentPlayingTrack?,
    isRepeatOne: Boolean,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val surah = remember(surahNumber) {
        QuranRepository.allSurahs.firstOrNull { it.number == surahNumber } ?: QuranRepository.allSurahs[0]
    }

    var selectedQari by remember {
        val qId = appPreferences.getSelectedQariId()
        mutableStateOf(AvailableQaris.firstOrNull { it.id == qId } ?: AvailableQaris[0])
    }

    val repeatMode by AudioPlayerManager.repeatMode.collectAsState()
    val currentPositionMs by AudioPlayerManager.currentPositionMs.collectAsState()
    val durationMs by AudioPlayerManager.durationMs.collectAsState()
    var ayahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }

    var arabicFontSize by remember { mutableStateOf(appPreferences.getArabicFontSize()) }
    var translationFontSize by remember { mutableStateOf(appPreferences.getTranslationFontSize()) }

    // Load Ayahs
    LaunchedEffect(surahNumber, selectedQari) {
        isLoading = true
        loadError = null
        try {
            ayahs = QuranRepository.getSurahAyahs(context, surahNumber, selectedQari)
            appPreferences.setLastRead(surah.number, surah.nameEnglish, 1)
        } catch (error: Exception) {
            ayahs = emptyList()
            loadError = error.message
        } finally {
            isLoading = false
        }
    }

    // Connect auto-play next Ayah
    LaunchedEffect(ayahs, selectedQari) {
        AudioPlayerManager.onAyahCompletedListener = { sNum, aNum ->
            if (sNum == surah.number && aNum < ayahs.size) {
                AudioPlayerManager.playAyah(
                    surahNumber = surah.number,
                    surahName = surah.nameEnglish,
                    ayahNumber = aNum + 1,
                    totalAyahs = ayahs.size,
                    qari = selectedQari
                )
                scope.launch {
                    listState.animateScrollToItem((aNum).coerceIn(0, ayahs.size))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_detail_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("surah_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "${surah.number}. ${surah.nameEnglish}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "${if (surah.isMeccan) strings.meccan else strings.medinan} • ${surah.ayahCount} ${strings.versesCount}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reciter selection button
                IconButton(onClick = { showReciterDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = strings.selectReciter,
                        tint = IslamicGold
                    )
                }

                // Font size button
                IconButton(onClick = { showFontSizeDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "Font Size",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = surah.nameArabic,
                    fontFamily = AmiriFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IslamicEmerald)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Opening Surah...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (ayahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = surah.nameArabic,
                            fontFamily = AmiriFontFamily,
                            fontSize = 32.sp,
                            color = IslamicEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${surah.number}. ${surah.nameEnglish} (${surah.englishMeaning})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${surah.ayahCount} ${strings.versesCount} • ${if (surah.isMeccan) strings.meccan else strings.medinan}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "The Quran text could not be opened. Please try again.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    loadError = null
                                    try {
                                        ayahs = QuranRepository.getSurahAyahs(context, surahNumber, selectedQari)
                                    } catch (error: Exception) {
                                        ayahs = emptyList()
                                        loadError = error.message
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                        ) {
                            Text("Try Again", color = Color.White)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // Quran text is fetched online from the Noor Muslim Quran service.

                    // Ayah Items
                    items(ayahs, key = { it.numberInSurah }) { ayah ->
                        val isPlayingThisAyah = currentTrack != null &&
                            currentTrack.surahNumber == surah.number &&
                            currentTrack.ayahNumber == ayah.numberInSurah &&
                            playbackState != PlaybackState.IDLE

                        val isBookmarked = appPreferences.isBookmarked(surah.number, ayah.numberInSurah)

                        AyahCardItem(
                            ayah = ayah,
                            surah = surah,
                            isPlaying = isPlayingThisAyah,
                            isBookmarked = isBookmarked,
                            arabicFontSize = arabicFontSize,
                            translationFontSize = translationFontSize,
                            strings = strings,
                            language = language,
                            onPlay = {
                                if (isPlayingThisAyah && playbackState == PlaybackState.PLAYING) {
                                    AudioPlayerManager.pause()
                                } else {
                                    AudioPlayerManager.playAyah(
                                        surahNumber = surah.number,
                                        surahName = surah.nameEnglish,
                                        ayahNumber = ayah.numberInSurah,
                                        totalAyahs = ayahs.size,
                                        qari = selectedQari
                                    )
                                }
                            },
                            onToggleBookmark = {
                                appPreferences.toggleBookmark(surah.number, surah.nameEnglish, ayah.numberInSurah)
                            },
                            onCopy = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val text = "${ayah.textArabic}\n\n${ayah.getTranslation(language)}\n[${surah.nameEnglish} ${surah.number}:${ayah.numberInSurah}]"
                                cm.setPrimaryClip(ClipData.newPlainText("Ayah", text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // Persistent Audio Playback Control Panel
                if (currentTrack != null && currentTrack.surahNumber == surah.number && playbackState != PlaybackState.IDLE) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp)
                            .testTag("surah_audio_controller"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = IslamicEmerald),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Scrubber progress
                            val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = IslamicGoldBright,
                                trackColor = Color.Black.copy(alpha = 0.2f)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${currentTrack.surahName} • Verse ${currentTrack.ayahNumber}/${currentTrack.totalAyahs}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (currentTrack.isOfflineSource) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.OfflinePin,
                                                contentDescription = strings.offlineAudioReady,
                                                tint = IslamicGoldBright,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${currentTrack.qari.name} (${currentTrack.qari.quality})",
                                        color = IslamicGoldBright,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Repeat toggle
                                    IconButton(onClick = { AudioPlayerManager.toggleRepeat() }) {
                                        Icon(
                                            imageVector = when (repeatMode) {
                                                AudioRepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                                AudioRepeatMode.REPEAT_SURAH -> Icons.Default.Repeat
                                                AudioRepeatMode.OFF -> Icons.Default.Repeat
                                            },
                                            contentDescription = "Repeat",
                                            tint = if (repeatMode != AudioRepeatMode.OFF) IslamicGoldBright else Color.White.copy(alpha = 0.5f)
                                        )
                                    }

                                    // Prev
                                    IconButton(
                                        onClick = {
                                            if (currentTrack.ayahNumber > 1) {
                                                AudioPlayerManager.playAyah(
                                                    surahNumber = surah.number,
                                                    surahName = surah.nameEnglish,
                                                    ayahNumber = currentTrack.ayahNumber - 1,
                                                    totalAyahs = ayahs.size,
                                                    qari = selectedQari
                                                )
                                            }
                                        },
                                        enabled = currentTrack.ayahNumber > 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous",
                                            tint = Color.White
                                        )
                                    }

                                    // Play / Pause
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(IslamicGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(onClick = { AudioPlayerManager.togglePlayPause() }) {
                                            Icon(
                                                imageVector = if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = IslamicEmeraldDark
                                            )
                                        }
                                    }

                                    // Next
                                    IconButton(
                                        onClick = {
                                            if (currentTrack.ayahNumber < ayahs.size) {
                                                AudioPlayerManager.playAyah(
                                                    surahNumber = surah.number,
                                                    surahName = surah.nameEnglish,
                                                    ayahNumber = currentTrack.ayahNumber + 1,
                                                    totalAyahs = ayahs.size,
                                                    qari = selectedQari
                                                )
                                            }
                                        },
                                        enabled = currentTrack.ayahNumber < ayahs.size
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Font Size Adjust Dialog
    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            title = { Text(strings.fontSize, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("${strings.arabicFontSize}: ${arabicFontSize.toInt()} sp")
                    Slider(
                        value = arabicFontSize,
                        onValueChange = {
                            arabicFontSize = it
                            appPreferences.setArabicFontSize(it)
                        },
                        valueRange = 20f..38f,
                        colors = SliderDefaults.colors(thumbColor = IslamicEmerald, activeTrackColor = IslamicEmerald)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("${strings.translationFontSize}: ${translationFontSize.toInt()} sp")
                    Slider(
                        value = translationFontSize,
                        onValueChange = {
                            translationFontSize = it
                            appPreferences.setTranslationFontSize(it)
                        },
                        valueRange = 12f..22f,
                        colors = SliderDefaults.colors(thumbColor = IslamicEmerald, activeTrackColor = IslamicEmerald)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontSizeDialog = false }) {
                    Text("OK", color = IslamicEmerald, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Reciter Selection Dialog
    if (showReciterDialog) {
        ReciterSelectionDialog(
            selectedQariId = selectedQari.id,
            strings = strings,
            onSelectQari = { newQari ->
                selectedQari = newQari
                appPreferences.setSelectedQariId(newQari.id)
                showReciterDialog = false
            },
            onDismiss = { showReciterDialog = false }
        )
    }
}

@Composable
fun AyahCardItem(
    ayah: Ayah,
    surah: com.example.data.model.Surah,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    arabicFontSize: Float,
    translationFontSize: Float,
    strings: LocalizedStrings,
    language: AppLanguage,
    onPlay: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("ayah_card_${ayah.numberInSurah}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) IslamicEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Ayah badge & Action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(IslamicEmerald.copy(alpha = 0.12f))
                        .border(1.dp, IslamicGold.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}:${ayah.numberInSurah}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmerald
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPlay, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play Ayah",
                            tint = if (isPlaying) IslamicEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic Quran Text in Amiri Font
            Text(
                text = ayah.textArabic,
                fontFamily = AmiriFontFamily,
                fontSize = arabicFontSize.sp,
                lineHeight = (arabicFontSize * 1.6f).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            // Translation (for non-Arabic languages)
            val translationText = ayah.getTranslation(language)
            if (translationText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = translationText,
                    fontSize = translationFontSize.sp,
                    lineHeight = (translationFontSize * 1.45f).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
