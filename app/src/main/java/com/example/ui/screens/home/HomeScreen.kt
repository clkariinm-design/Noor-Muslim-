package com.example.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.model.LocalizedStrings
import com.example.data.model.PrayerType
import com.example.data.repository.AudioPlayerManager
import com.example.data.repository.CurrentPlayingTrack
import com.example.data.repository.DuasRepository
import com.example.data.repository.PlaybackState
import com.example.data.repository.PrayerTimesCalculator
import com.example.ui.components.IslamicOrnamentCard
import com.example.ui.components.NoorNavDestination
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldDeep
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.PrayerCardActive
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    strings: LocalizedStrings,
    language: AppLanguage,
    currentCity: CityLocation,
    onNavigate: (NoorNavDestination) -> Unit,
    playbackState: PlaybackState,
    currentTrack: CurrentPlayingTrack?
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val prayers = remember(currentCity, currentTime.get(Calendar.MINUTE)) {
        PrayerTimesCalculator.calculate(
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            timeZoneId = currentCity.timeZoneId
        )
    }
    val hijriDate = remember(currentTime.get(Calendar.DAY_OF_YEAR)) {
        PrayerTimesCalculator.getHijriDate(currentTime)
    }
    val gregorianDate = remember(currentTime.get(Calendar.DAY_OF_YEAR)) {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(currentTime.time)
    }
    val hijriDateText =
        "${hijriDate.day} ${hijriDate.monthNameArabic} • ${hijriDate.year} AH"

    val greeting = when (currentTime.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> strings.greetingMorning
        in 12..16 -> strings.greetingAfternoon
        in 17..20 -> strings.greetingEvening
        else -> strings.greetingNight
    }

    val remainingSeconds = (prayers.remainingMillis / 1000).coerceAtLeast(0L)
    val countdown = String.format(
        Locale.getDefault(),
        "%02d:%02d:%02d",
        remainingSeconds / 3600,
        (remainingSeconds % 3600) / 60,
        remainingSeconds % 60
    )
    val nextPrayerName = prayerName(prayers.nextPrayer.type, strings)

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            // Calm, premium emerald header; all existing date and city information is retained.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(IslamicEmeraldDeep, IslamicEmerald, Color(0xFF174B43))
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.islamicGreeting,
                                color = IslamicGoldBright,
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = greeting,
                                color = Color.White.copy(alpha = 0.86f),
                                fontSize = 14.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { onNavigate(NoorNavDestination.PRAYER) }
                                .padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = IslamicGoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = currentCity.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Column {
                        Text(
                            text = hijriDateText,
                            color = IslamicGoldBright,
                            fontFamily = AmiriFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = gregorianDate,
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigate(NoorNavDestination.PRAYER) }
                    .testTag("next_prayer_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicEmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                strings.nextPrayer,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                nextPrayerName,
                                color = Color.White,
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                prayers.nextPrayer.timeFormatted,
                                color = IslamicGoldBright,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                strings.prayerIn,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        countdown,
                        color = IslamicGoldBright,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(13.dp))
                    LinearProgressIndicator(
                        progress = {
                            val total = 6 * 60 * 60 * 1000f
                            ((total - prayers.remainingMillis).coerceIn(0f, total)) / total
                        },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                        color = IslamicGold,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        item {
            Column {
                SectionHeading(
                    title = strings.todayPrayerTimes,
                    action = strings.viewAll,
                    onAction = { onNavigate(NoorNavDestination.PRAYER) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(prayers.asList().filter { it.type != PrayerType.SUNRISE }) { prayer ->
                        val active = prayer.isNext
                        Card(
                            modifier = Modifier.width(104.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (active) IslamicEmerald else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(if (active) 3.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    prayerName(prayer.type, strings),
                                    color = if (active) IslamicGoldBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(7.dp))
                                Text(
                                    prayer.timeFormatted,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(5.dp))
                                if (active) {
                                    Text("NEXT", color = IslamicGoldBright, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Spacer(Modifier.height(11.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    strings.quickAccess,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickTile(Icons.AutoMirrored.Filled.MenuBook, strings.navQuran, IslamicEmerald, Modifier.weight(1f)) {
                        onNavigate(NoorNavDestination.QURAN)
                    }
                    QuickTile(Icons.Filled.Explore, strings.navQibla, Color(0xFFC28A32), Modifier.weight(1f)) {
                        onNavigate(NoorNavDestination.QIBLA)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickTile(Icons.Filled.TouchApp, strings.navTasbeeh, Color(0xFF287A91), Modifier.weight(1f)) {
                        onNavigate(NoorNavDestination.TASBEEH)
                    }
                    QuickTile(Icons.AutoAwesome, strings.navDuas, Color(0xFF7454A5), Modifier.weight(1f)) {
                        onNavigate(NoorNavDestination.DUAS)
                    }
                }
            }
        }

        if (currentTrack != null && playbackState != PlaybackState.IDLE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = IslamicEmeraldDark)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = IslamicGold, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${currentTrack.surahName} • ${currentTrack.ayahNumber}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(currentTrack.qari.name, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        IconButton(onClick = { AudioPlayerManager.togglePlayPause() }) {
                            Icon(
                                if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play or pause",
                                tint = IslamicGoldBright
                            )
                        }
                        IconButton(onClick = { AudioPlayerManager.stop() }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                        }
                    }
                }
            }
        }

        item {
            IslamicOrnamentCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    SectionLabel(strings.dailyVerseTitle, DuasRepository.dailyInspiration.verseRef)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        DuasRepository.dailyInspiration.verseArabic,
                        fontFamily = AmiriFontFamily,
                        fontSize = 24.sp,
                        lineHeight = 40.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val translation = when (language) {
                        AppLanguage.SOMALI -> DuasRepository.dailyInspiration.verseSo
                        AppLanguage.ARABIC -> ""
                        AppLanguage.ENGLISH -> DuasRepository.dailyInspiration.verseEn
                        AppLanguage.TURKISH -> DuasRepository.dailyInspiration.verseTr
                        AppLanguage.FRENCH -> DuasRepository.dailyInspiration.verseFr
                    }
                    if (translation.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "“$translation”",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            IslamicOrnamentCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    SectionLabel(strings.dailyHadithTitle, DuasRepository.dailyInspiration.hadithRef)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        DuasRepository.dailyInspiration.hadithArabic,
                        fontFamily = AmiriFontFamily,
                        fontSize = 21.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val translation = when (language) {
                        AppLanguage.SOMALI -> DuasRepository.dailyInspiration.hadithSo
                        AppLanguage.ARABIC -> ""
                        AppLanguage.ENGLISH -> DuasRepository.dailyInspiration.hadithEn
                        AppLanguage.TURKISH -> DuasRepository.dailyInspiration.hadithTr
                        AppLanguage.FRENCH -> DuasRepository.dailyInspiration.hadithFr
                    }
                    if (translation.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            translation,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            action,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onAction).padding(6.dp)
        )
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SectionLabel(title: String, reference: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(reference, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun QuickTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(tint.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(9.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun prayerName(type: PrayerType, strings: LocalizedStrings): String = when (type) {
    PrayerType.FAJR -> strings.fajr
    PrayerType.SUNRISE -> strings.sunrise
    PrayerType.DHUHR -> strings.dhuhr
    PrayerType.ASR -> strings.asr
    PrayerType.MAGHRIB -> strings.maghrib
    PrayerType.ISHA -> strings.isha
}
