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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.repository.DuasRepository
import com.example.data.repository.PlaybackState
import com.example.data.repository.CurrentPlayingTrack
import com.example.data.repository.PrayerTimesCalculator
import com.example.ui.components.NoorNavDestination
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val HomeBackground = Color(0xFFF7FAFE)
private val HomeWhite = Color(0xFFFFFFFF)
private val HomeLightBlue = Color(0xFFEAF3FF)
private val HomeBlue = Color(0xFF3478D4)
private val HomeNavy = Color(0xFF18324F)
private val HomeMuted = Color(0xFF6D7F95)

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
            delay(30_000)
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
    val hour = currentTime.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> strings.greetingMorning
        in 12..16 -> strings.greetingAfternoon
        in 17..20 -> strings.greetingEvening
        else -> strings.greetingNight
    }
    val nextPrayerName = when (prayers.nextPrayer.type) {
        PrayerType.FAJR -> strings.fajr
        PrayerType.SUNRISE -> strings.sunrise
        PrayerType.DHUHR -> strings.dhuhr
        PrayerType.ASR -> strings.asr
        PrayerType.MAGHRIB -> strings.maghrib
        PrayerType.ISHA -> strings.isha
    }
    val remainingSeconds = (prayers.remainingMillis / 1000).coerceAtLeast(0L)
    val countdown = String.format(
        Locale.getDefault(), "%02d:%02d:%02d",
        remainingSeconds / 3600, (remainingSeconds % 3600) / 60, remainingSeconds % 60
    )
    val dateText = remember(currentTime.get(Calendar.DAY_OF_YEAR)) {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(currentTime.time)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(HomeBackground).testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HomeNavy
                )
                Text(
                    text = "$dateText  •  ${currentCity.name}, ${currentCity.country}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HomeMuted
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(NoorNavDestination.PRAYER) }
                    .testTag("next_prayer_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HomeLightBlue)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.nextPrayer, color = HomeBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(nextPrayerName, color = HomeNavy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                            Text("${strings.prayerIn} $countdown", color = HomeMuted, fontSize = 13.sp)
                        }
                        Text(
                            prayers.nextPrayer.timeFormatted,
                            color = HomeBlue,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = {
                            val total = 6 * 60 * 60 * 1000f
                            ((total - prayers.remainingMillis).coerceIn(0f, total)) / total
                        },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                        color = IslamicGold,
                        trackColor = HomeBlue.copy(alpha = 0.14f)
                    )
                    Text(strings.todayPrayerTimes, color = HomeMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        prayers.asList().forEach { prayer ->
                            val label = when (prayer.type) {
                                PrayerType.FAJR -> strings.fajr
                                PrayerType.SUNRISE -> strings.sunrise
                                PrayerType.DHUHR -> strings.dhuhr
                                PrayerType.ASR -> strings.asr
                                PrayerType.MAGHRIB -> strings.maghrib
                                PrayerType.ISHA -> strings.isha
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, color = HomeMuted, fontSize = 10.sp)
                                Text(
                                    prayer.timeFormatted,
                                    color = HomeNavy,
                                    fontSize = 11.sp,
                                    fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = strings.quickAccess,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessTile(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = strings.navQuran,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_quran",
                    onClick = { onNavigate(NoorNavDestination.QURAN) }
                )
                QuickAccessTile(
                    icon = Icons.Filled.AccessTime,
                    title = strings.navPrayer,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_prayer",
                    onClick = { onNavigate(NoorNavDestination.PRAYER) }
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessTile(
                    icon = Icons.Filled.Explore,
                    title = strings.navQibla,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_qibla",
                    onClick = { onNavigate(NoorNavDestination.QIBLA) }
                )
                QuickAccessTile(
                    icon = Icons.Filled.AutoAwesome,
                    title = strings.navDuas,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_duas",
                    onClick = { onNavigate(NoorNavDestination.DUAS) }
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HomeWhite)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.dailyVerseTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HomeBlue
                    )
                    Text(
                        text = DuasRepository.dailyInspiration.verseArabic,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = AmiriFontFamily,
                        fontSize = 23.sp,
                        lineHeight = 38.sp,
                        textAlign = TextAlign.Center,
                        color = HomeNavy
                    )
                    val translation = when (language) {
                        AppLanguage.SOMALI -> DuasRepository.dailyInspiration.verseSo
                        AppLanguage.ARABIC -> ""
                        AppLanguage.ENGLISH -> DuasRepository.dailyInspiration.verseEn
                        AppLanguage.TURKISH -> DuasRepository.dailyInspiration.verseTr
                        AppLanguage.FRENCH -> DuasRepository.dailyInspiration.verseFr
                    }
                    if (translation.isNotBlank()) {
                        Text(
                            text = translation,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = DuasRepository.dailyInspiration.verseRef,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick).testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = HomeBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
