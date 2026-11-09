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
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val HomeBackground = Color(0xFFF7FAFE)
private val HomeWhite = Color(0xFFFFFFFF)
private val HomeLightBlue = Color(0xFFEAF3FF)
private val HomeBlue = Color(0xFF3478D4)
private val HomeNavy = Color(0xFF18324F)
private val HomeMuted = Color(0xFF71839A)

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
    val dailyTranslation = when (language) {
        AppLanguage.SOMALI -> DuasRepository.dailyInspiration.verseSo
        AppLanguage.ARABIC -> ""
        AppLanguage.ENGLISH -> DuasRepository.dailyInspiration.verseEn
        AppLanguage.TURKISH -> DuasRepository.dailyInspiration.verseTr
        AppLanguage.FRENCH -> DuasRepository.dailyInspiration.verseFr
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(HomeLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = HomeBlue,
                        modifier = Modifier.size(25.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Noor Muslim",
                        color = HomeNavy,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dateText  •  ${currentCity.name}",
                        color = HomeMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = greeting,
                color = HomeNavy,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(NoorNavDestination.PRAYER) }
                    .testTag("next_prayer_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = HomeLightBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            text = strings.nextPrayer,
                            color = HomeBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = nextPrayerName,
                            color = HomeNavy,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${strings.prayerIn} $countdown",
                            color = HomeMuted,
                            fontSize = 13.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(HomeWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = HomeBlue,
                                modifier = Modifier.size(27.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = prayers.nextPrayer.timeFormatted,
                            color = HomeBlue,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.todayPrayerTimes,
                    color = HomeNavy,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = HomeWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        prayers.asList()
                            .filter { it.type != PrayerType.SUNRISE }
                            .forEach { prayer ->
                                val label = when (prayer.type) {
                                    PrayerType.FAJR -> strings.fajr
                                    PrayerType.SUNRISE -> strings.sunrise
                                    PrayerType.DHUHR -> strings.dhuhr
                                    PrayerType.ASR -> strings.asr
                                    PrayerType.MAGHRIB -> strings.maghrib
                                    PrayerType.ISHA -> strings.isha
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (prayer.isNext) HomeBlue else HomeMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = prayer.timeFormatted,
                                        color = HomeNavy,
                                        fontSize = 10.sp,
                                        fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = strings.quickAccess,
                    color = HomeNavy,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAccessTile(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = strings.navQuran,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_quran",
                        onClick = { onNavigate(NoorNavDestination.QURAN) }
                    )
                    QuickAccessTile(
                        icon = Icons.Filled.Explore,
                        title = strings.navQibla,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_qibla",
                        onClick = { onNavigate(NoorNavDestination.QIBLA) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAccessTile(
                        icon = Icons.Filled.AutoAwesome,
                        title = strings.navDuas,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_duas",
                        onClick = { onNavigate(NoorNavDestination.DUAS) }
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
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HomeWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.dailyVerseTitle,
                        color = HomeBlue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                    if (dailyTranslation.isNotBlank()) {
                        Text(
                            text = dailyTranslation,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HomeMuted
                        )
                    }
                    Text(
                        text = DuasRepository.dailyInspiration.verseRef,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelMedium,
                        color = HomeMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HomeWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HomeLightBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = HomeBlue,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                color = HomeNavy,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
