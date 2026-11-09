package com.example.ui.screens.quran

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AvailableQaris
import com.example.data.model.Qari
import com.example.data.model.Juz
import com.example.data.model.LocalizedStrings
import com.example.data.model.ReadingBookmark
import com.example.data.model.Surah
import com.example.data.repository.AudioPlayerManager
import com.example.data.repository.QuranRepository
import com.example.ui.components.QuranMiniPlayer
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright

@Composable
fun QuranScreen(
    strings: LocalizedStrings,
    language: AppLanguage,
    lastRead: ReadingBookmark?,
    bookmarks: List<ReadingBookmark>,
    onSelectSurah: (Int) -> Unit,
    selectedQariId: String,
    onReciterSelected: (Qari) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showReciterDialog by remember { mutableStateOf(false) }
    val selectedQari = AvailableQaris.firstOrNull { it.id == selectedQariId } ?: AvailableQaris[0]

    val playbackState by AudioPlayerManager.playbackState.collectAsState()
    val currentTrack by AudioPlayerManager.currentTrack.collectAsState()
    val repeatMode by AudioPlayerManager.repeatMode.collectAsState()
    val currentPositionMs by AudioPlayerManager.currentPositionMs.collectAsState()
    val durationMs by AudioPlayerManager.durationMs.collectAsState()

    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            QuranRepository.allSurahs
        } else {
            val q = searchQuery.trim().lowercase()
            QuranRepository.allSurahs.filter {
                it.nameEnglish.lowercase().contains(q) ||
                it.englishMeaning.lowercase().contains(q) ||
                it.nameArabic.contains(q) ||
                it.number.toString() == q
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("quran_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showReciterDialog = true }
                    .testTag("quran_reciter_selector"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.reciter, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(selectedQari.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(selectedQari.subName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("quran_search_field"),
                placeholder = { Text(strings.searchSurah, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicEmerald,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Tabs: Surahs, Juz, Bookmarks
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IslamicEmerald,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = IslamicEmerald
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${strings.surahsTab} (${QuranRepository.allSurahs.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_surahs")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${strings.juzTab} (${QuranRepository.allJuz.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_juz")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("${strings.bookmarksTab} (${bookmarks.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_bookmarks")
                )
            }

            val bottomPadding = if (currentTrack != null && playbackState != com.example.data.repository.PlaybackState.IDLE) 80.dp else 16.dp

            when (selectedTab) {
                0 -> {
                    // Surahs List
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Calm Quran introduction banner
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                                    .testTag("quran_intro_card"),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = IslamicEmerald)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "الْقُرْآنُ الْكَرِيمُ",
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 27.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGoldBright
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "114 Surahs  •  6,236 Ayahs",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Offline reading is prepared automatically",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.78f)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(IslamicGold.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "۞",
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 36.sp,
                                            color = IslamicGoldBright
                                        )
                                    }
                                }
                            }
                        }

                        // Last read banner
                        if (lastRead != null && searchQuery.isBlank()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectSurah(lastRead.surahNumber) }
                                        .testTag("continue_reading_card"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = IslamicEmerald)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = strings.lastRead,
                                                color = IslamicGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${lastRead.surahName} • Verse ${lastRead.ayahNumber}",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = strings.continueReading,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = IslamicGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(filteredSurahs, key = { it.number }) { surah ->
                            SurahRowItem(
                                surah = surah,
                                strings = strings,
                                onClick = { onSelectSurah(surah.number) }
                            )
                        }

                        if (searchQuery.isBlank()) {
                            item {
                                Text(
                                    text = "Quran text: Tanzil Project • tanzil.net",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { uriHandler.openUri("https://tanzil.net/") }
                                        .padding(vertical = 18.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Juz List
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(QuranRepository.allJuz, key = { it.number }) { juz ->
                            JuzRowItem(
                                juz = juz,
                                strings = strings,
                                onClick = { onSelectSurah(juz.startSurahNumber) }
                            )
                        }
                    }
                }
                2 -> {
                    // Bookmarks List
                    if (bookmarks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = strings.noBookmarksYet,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(bookmarks, key = { "${it.surahNumber}_${it.ayahNumber}_${it.timestamp}" }) { b ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectSurah(b.surahNumber) }
                                        .testTag("bookmark_item_${b.surahNumber}_${b.ayahNumber}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = IslamicGold,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = b.surahName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    text = "Verse ${b.ayahNumber}",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
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
        }

        // Docked Mini Player
        if (showReciterDialog) {
            ReciterSelectionDialog(
                selectedQariId = selectedQari.id,
                strings = strings,
                onSelectQari = onReciterSelected,
                onDismiss = { showReciterDialog = false }
            )
        }

        QuranMiniPlayer(
            currentTrack = currentTrack,
            playbackState = playbackState,
            repeatMode = repeatMode,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            strings = strings,
            modifier = Modifier.align(Alignment.BottomCenter),
            onExpand = {
                currentTrack?.let { onSelectSurah(it.surahNumber) }
            }
        )
    }
}

@Composable
fun SurahRowItem(
    surah: Surah,
    strings: LocalizedStrings,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Number Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(IslamicEmerald.copy(alpha = 0.1f))
                        .border(1.dp, IslamicGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = IslamicEmerald
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = surah.nameEnglish,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${if (surah.isMeccan) strings.meccan else strings.medinan} • ${surah.ayahCount} ${strings.versesCount}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arabic Surah Name in Amiri font
            Text(
                text = surah.nameArabic,
                fontFamily = AmiriFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun JuzRowItem(
    juz: Juz,
    strings: LocalizedStrings,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("juz_item_${juz.number}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(IslamicGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = juz.number.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "${strings.juzNumber} ${juz.number}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${juz.startSurahName} (${juz.startAyah}) → ${juz.endSurahName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = juz.nameArabic,
                fontFamily = AmiriFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
