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
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    val selectedQari = remember {
        val qId = appPreferences.getSelectedQariId()
        AvailableQaris.firstOrNull { it.id == qId } ?: AvailableQaris[0]
    }

    var ayahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    var arabicFontSize by remember { mutableStateOf(appPreferences.getArabicFontSize()) }
    var translationFontSize by remember { mutableStateOf(appPreferences.getTranslationFontSize()) }

    // Load Ayahs
    LaunchedEffect(surahNumber) {
        isLoading = true
        ayahs = QuranRepository.getSurahAyahs(context, surahNumber, selectedQari)
        isLoading = false

        // Record Last Read
        appPreferences.setLastRead(surah.number, surah.nameEnglish, 1)
    }

    // Connect auto-play next Ayah
    LaunchedEffect(ayahs) {
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
                    Text(text = "Loading verified verses...", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            text = "Connect to the internet to download the complete verified original Uthmani text and translations for this Surah.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    ayahs = QuranRepository.getSurahAyahs(context, surahNumber, selectedQari)
                                    isLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download Verified Verses", color = Color.White, fontWeight = FontWeight.Bold)
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
                    // Bismillah Banner (All surahs except Surah 9 At-Tawbah)
                    if (surah.number != 9) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = IslamicEmeraldDark
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 26.sp,
                                        color = IslamicGoldBright,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = strings.bismillah,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Ayahs List
                    items(ayahs, key = { it.numberInSurah }) { ayah ->
                        val isPlayingThisAyah = currentTrack?.surahNumber == surah.number &&
                                currentTrack.ayahNumber == ayah.numberInSurah &&
                                playbackState == PlaybackState.PLAYING

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
                                if (isPlayingThisAyah) {
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
                                val added = appPreferences.toggleBookmark(
                                    surah.number,
                                    surah.nameEnglish,
                                    ayah.numberInSurah
                                )
                                Toast.makeText(
                                    context,
                                    if (added) strings.bookmarkAdded else strings.bookmarkRemoved,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onCopy = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "Ayah",
                                    "${ayah.textArabic}\n${ayah.getTranslation(language)}\n[${surah.nameEnglish} ${surah.number}:${ayah.numberInSurah}]"
                                )
                                cm.setPrimaryClip(clip)
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${currentTrack.surahName} • Verse ${currentTrack.ayahNumber}/${currentTrack.totalAyahs}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = currentTrack.qari.name,
                                    color = IslamicGoldBright,
                                    fontSize = 12.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Repeat toggle
                                IconButton(onClick = { AudioPlayerManager.toggleRepeat() }) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = "Repeat",
                                        tint = if (isRepeatOne) IslamicGoldBright else Color.White.copy(alpha = 0.5f)
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
