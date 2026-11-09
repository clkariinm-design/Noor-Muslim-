package com.example.ui.screens.home

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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

    // Live 1-second ticker for prayer countdown and clock
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance()
        }
    }

    val calculatedPrayers = remember(currentCity, currentTime.get(Calendar.MINUTE)) {
        PrayerTimesCalculator.calculate(
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            timeZoneId = currentCity.timeZoneId
        )
    }

    val hijriDate = remember(currentTime.get(Calendar.DAY_OF_YEAR)) {
        PrayerTimesCalculator.getHijriDate(currentTime)
    }

    val gregorianDateStr = remember(currentTime.get(Calendar.DAY_OF_YEAR)) {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        sdf.format(currentTime.time)
    }

    val hijriDateStr = "${hijriDate.day} ${hijriDate.monthNameArabic} (${hijriDate.monthNameEnglish}) ${hijriDate.year} AH"

    // Time of day greeting
    val hourOfDay = currentTime.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hourOfDay) {
        in 5..11 -> strings.greetingMorning
        in 12..16 -> strings.greetingAfternoon
        in 17..20 -> strings.greetingEvening
        else -> strings.greetingNight
    }

    // Format remaining time into HH:MM:SS
    val remSec = (calculatedPrayers.remainingMillis / 1000).coerceAtLeast(0L)
    val remHours = remSec / 3600
    val remMinutes = (remSec % 3600) / 60
    val remSeconds = remSec % 60
    val countdownFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", remHours, remMinutes, remSeconds)

    val nextPrayerName = when (calculatedPrayers.nextPrayer.type) {
        PrayerType.FAJR -> strings.fajr
        PrayerType.SUNRISE -> strings.sunrise
        PrayerType.DHUHR -> strings.dhuhr
        PrayerType.ASR -> strings.asr
        PrayerType.MAGHRIB -> strings.maghrib
        PrayerType.ISHA -> strings.isha
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO BANNER WITH KAABA ARTWORK & HIJRI/GREGORIAN DATES
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_islamic_header),
                    contentDescription = "Holy Kaaba Mosque Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay for perfect readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    IslamicEmeraldDeep.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: Islamic Greeting & Current City
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.islamicGreeting,
                                color = IslamicGoldBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = AmiriFontFamily
                            )
                            Text(
                                text = greeting,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable { onNavigate(NoorNavDestination.PRAYER) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "City",
                                tint = IslamicGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentCity.name}, ${currentCity.country}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Bottom: Hijri & Gregorian Dates
                    Column {
                        Text(
                            text = hijriDateStr,
                            color = IslamicGoldBright,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AmiriFontFamily
                        )
                        Text(
                            text = gregorianDateStr,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 2. NEXT PRAYER COUNTDOWN CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigate(NoorNavDestination.PRAYER) }
                    .testTag("next_prayer_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = IslamicEmerald
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.nextPrayer,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = nextPrayerName,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = calculatedPrayers.nextPrayer.timeFormatted,
                                color = IslamicGoldBright,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.prayerIn} $countdownFormatted",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = {
                            val totalSpan = 6 * 3600 * 1000f
                            val elapsed = (totalSpan - calculatedPrayers.remainingMillis).coerceIn(0f, totalSpan)
                            elapsed / totalSpan
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = IslamicGold,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // 3. TODAY'S PRAYER TIMES HORIZONTAL SCROLL RIBBON
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.todayPrayerTimes,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = strings.viewAll,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(NoorNavDestination.PRAYER) }
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(calculatedPrayers.asList()) { item ->
                        val pName = when (item.type) {
                            PrayerType.FAJR -> strings.fajr
                            PrayerType.SUNRISE -> strings.sunrise
                            PrayerType.DHUHR -> strings.dhuhr
                            PrayerType.ASR -> strings.asr
                            PrayerType.MAGHRIB -> strings.maghrib
                            PrayerType.ISHA -> strings.isha
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isNext) PrayerCardActive else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (item.isNext) 4.dp else 1.dp),
                            modifier = Modifier.width(96.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = pName,
                                    fontSize = 12.sp,
                                    fontWeight = if (item.isNext) FontWeight.Bold else FontWeight.Medium,
                                    color = if (item.isNext) IslamicGoldBright else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.timeFormatted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isNext) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick access for useful secondary tools. Quran and Qibla already have primary navigation tabs.
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = strings.quickAccess,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessTile(
                        icon = Icons.Filled.TouchApp,
                        title = strings.navTasbeeh,
                        color = IslamicEmerald,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_tasbeeh",
                        onClick = { onNavigate(NoorNavDestination.TASBEEH) }
                    )
                    QuickAccessTile(
                        icon = Icons.Filled.AutoAwesome,
                        title = strings.navDuas,
                        color = IslamicGold,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_duas",
                        onClick = { onNavigate(NoorNavDestination.DUAS) }
                    )
                }
            }
        }

        // 5. MINI AUDIO PLAYER BANNER (If recitation is playing)
        if (currentTrack != null && playbackState != PlaybackState.IDLE) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IslamicEmeraldDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(IslamicGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = IslamicGold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${currentTrack.surahName} - Verse ${currentTrack.ayahNumber}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = currentTrack.qari.name,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { AudioPlayerManager.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = IslamicGold
                                )
                            }
                            IconButton(onClick = { AudioPlayerManager.stop() }) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. DAILY QURAN VERSE CARD
        item {
            IslamicOrnamentCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IslamicGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.dailyVerseTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = DuasRepository.dailyInspiration.verseRef,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = DuasRepository.dailyInspiration.verseArabic,
                        fontFamily = AmiriFontFamily,
                        fontSize = 22.sp,
                        lineHeight = 36.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val transText = when (language) {
                        AppLanguage.SOMALI -> DuasRepository.dailyInspiration.verseSo
                        AppLanguage.ARABIC -> ""
                        AppLanguage.ENGLISH -> DuasRepository.dailyInspiration.verseEn
                        AppLanguage.TURKISH -> DuasRepository.dailyInspiration.verseTr
                        AppLanguage.FRENCH -> DuasRepository.dailyInspiration.verseFr
                    }

                    if (transText.isNotEmpty()) {
                        Text(
                            text = "“$transText”",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 7. DAILY HADITH CARD
        item {
            IslamicOrnamentCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = IslamicGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.dailyHadithTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = DuasRepository.dailyInspiration.hadithRef,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = DuasRepository.dailyInspiration.hadithArabic,
                        fontFamily = AmiriFontFamily,
                        fontSize = 19.sp,
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val hadithTrans = when (language) {
                        AppLanguage.SOMALI -> DuasRepository.dailyInspiration.hadithSo
                        AppLanguage.ARABIC -> ""
                        AppLanguage.ENGLISH -> DuasRepository.dailyInspiration.hadithEn
                        AppLanguage.TURKISH -> DuasRepository.dailyInspiration.hadithTr
                        AppLanguage.FRENCH -> DuasRepository.dailyInspiration.hadithFr
                    }

                    if (hadithTrans.isNotEmpty()) {
                        Text(
                            text = hadithTrans,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 19.sp,
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
private fun QuickAccessTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
        }
    }
}
