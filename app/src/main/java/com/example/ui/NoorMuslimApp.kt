package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.data.local.AppPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.Translations
import com.example.data.repository.AudioPlayerManager
import com.example.ui.components.NoorBottomNav
import com.example.ui.components.NoorNavDestination
import com.example.ui.components.NoorTopBar
import com.example.ui.screens.duas.DuasScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.prayer.PrayerTimesScreen
import com.example.ui.screens.qibla.QiblaScreen
import com.example.ui.screens.quran.QuranScreen
import com.example.ui.screens.quran.SurahDetailScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tasbeeh.TasbeehScreen
import com.example.ui.theme.NoorMuslimTheme

@Composable
fun NoorMuslimApp() {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences(context) }

    val currentLanguage by appPreferences.languageFlow.collectAsState()
    val themeMode by appPreferences.themeModeFlow.collectAsState()
    val currentCity by appPreferences.selectedCityFlow.collectAsState()
    val bookmarks by appPreferences.bookmarksFlow.collectAsState()
    val lastRead by appPreferences.lastReadFlow.collectAsState()

    val playbackState by AudioPlayerManager.playbackState.collectAsState()
    val currentTrack by AudioPlayerManager.currentTrack.collectAsState()
    val isRepeatOne by AudioPlayerManager.isRepeatOne.collectAsState()

    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val strings = remember(currentLanguage) {
        Translations.get(currentLanguage)
    }

    var currentDestination by remember { mutableStateOf(NoorNavDestination.HOME) }
    var viewingSurahNumber by remember { mutableStateOf<Int?>(null) }

    // System Back Handler
    BackHandler(enabled = viewingSurahNumber != null || currentDestination != NoorNavDestination.HOME) {
        if (viewingSurahNumber != null) {
            viewingSurahNumber = null
        } else if (currentDestination != NoorNavDestination.HOME) {
            currentDestination = NoorNavDestination.HOME
        }
    }

    NoorMuslimTheme(darkTheme = isDark) {
        // Enforce RTL for Arabic and LTR for Somali, English, Turkish, French
        CompositionLocalProvider(LocalLayoutDirection provides currentLanguage.layoutDirection) {
            Scaffold(
                topBar = {
                    if (viewingSurahNumber == null) {
                        val title = when (currentDestination) {
                            NoorNavDestination.HOME -> strings.appName
                            NoorNavDestination.QURAN -> strings.holyQuran
                            NoorNavDestination.PRAYER -> strings.todayPrayerTimes
                            NoorNavDestination.QIBLA -> strings.qiblaCompass
                            NoorNavDestination.TASBEEH -> strings.digitalTasbeeh
                            NoorNavDestination.DUAS -> strings.dailyDuas
                            NoorNavDestination.SETTINGS -> strings.settingsTitle
                        }
                        NoorTopBar(
                            title = title,
                            currentLanguage = currentLanguage,
                            onLanguageChange = { appPreferences.setLanguage(it) }
                        )
                    }
                },
                bottomBar = {
                    if (viewingSurahNumber == null) {
                        NoorBottomNav(
                            currentDestination = currentDestination,
                            onSelectDestination = {
                                viewingSurahNumber = null
                                currentDestination = it
                            },
                            strings = strings
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (viewingSurahNumber != null) {
                        SurahDetailScreen(
                            surahNumber = viewingSurahNumber!!,
                            strings = strings,
                            language = currentLanguage,
                            appPreferences = appPreferences,
                            playbackState = playbackState,
                            currentTrack = currentTrack,
                            isRepeatOne = isRepeatOne,
                            onBack = { viewingSurahNumber = null }
                        )
                    } else {
                        when (currentDestination) {
                            NoorNavDestination.HOME -> HomeScreen(
                                strings = strings,
                                language = currentLanguage,
                                currentCity = currentCity,
                                onNavigate = { destination -> currentDestination = destination },
                                playbackState = playbackState,
                                currentTrack = currentTrack
                            )
                            NoorNavDestination.QURAN -> QuranScreen(
                                strings = strings,
                                language = currentLanguage,
                                lastRead = lastRead,
                                bookmarks = bookmarks,
                                onSelectSurah = { surahNum -> viewingSurahNumber = surahNum }
                            )
                            NoorNavDestination.PRAYER -> PrayerTimesScreen(
                                strings = strings,
                                currentCity = currentCity,
                                appPreferences = appPreferences,
                                onCityChanged = { appPreferences.setSelectedCity(it) }
                            )
                            NoorNavDestination.QIBLA -> QiblaScreen(
                                strings = strings,
                                currentCity = currentCity
                            )
                            NoorNavDestination.TASBEEH -> TasbeehScreen(
                                strings = strings,
                                appPreferences = appPreferences
                            )
                            NoorNavDestination.DUAS -> DuasScreen(
                                strings = strings,
                                language = currentLanguage
                            )
                            NoorNavDestination.SETTINGS -> SettingsScreen(
                                strings = strings,
                                currentLanguage = currentLanguage,
                                appPreferences = appPreferences,
                                onLanguageChange = { appPreferences.setLanguage(it) },
                                onThemeChange = { appPreferences.setThemeMode(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
