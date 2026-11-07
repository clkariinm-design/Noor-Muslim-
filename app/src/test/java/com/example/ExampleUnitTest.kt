package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.AvailableQaris
import com.example.data.model.CalculationMethod
import com.example.data.model.JuristicMethod
import com.example.data.model.PrayerType
import com.example.data.model.Translations
import com.example.data.repository.PrayerTimesCalculator
import com.example.data.repository.QiblaCalculator
import com.example.data.repository.QuranRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExampleUnitTest {

    @Test
    fun testQuranAll114SurahsAndAyahCount() {
        assertEquals("Total surahs must be 114", 114, QuranRepository.allSurahs.size)
        assertEquals("Total juz must be 30", 30, QuranRepository.allJuz.size)

        // Verify the exact Quranic total of 6,236 Ayahs
        val totalAyahs = QuranRepository.allSurahs.sumOf { it.ayahCount }
        assertEquals("Total Quranic ayahs must equal 6236", 6236, totalAyahs)

        // Check first, middle and last Surahs
        val s1 = QuranRepository.allSurahs[0]
        assertEquals(1, s1.number)
        assertEquals("Al-Fatihah", s1.nameEnglish)
        assertEquals(7, s1.ayahCount)
        assertTrue(s1.isMeccan)

        val s2 = QuranRepository.allSurahs[1]
        assertEquals(2, s2.number)
        assertEquals("Al-Baqarah", s2.nameEnglish)
        assertEquals(286, s2.ayahCount)

        val s36 = QuranRepository.allSurahs[35]
        assertEquals(36, s36.number)
        assertEquals("Ya-Sin", s36.nameEnglish)
        assertEquals(83, s36.ayahCount)

        val s114 = QuranRepository.allSurahs[113]
        assertEquals(114, s114.number)
        assertEquals("An-Nas", s114.nameEnglish)
        assertEquals(6, s114.ayahCount)
    }

    @Test
    fun testTranslationsForAllFiveLanguages() {
        AppLanguage.entries.forEach { lang ->
            val strings = Translations.get(lang)
            assertNotNull(strings.appName)
            assertTrue("App name in ${lang.name} should not be empty", strings.appName.isNotEmpty())
            assertTrue("Holy Quran in ${lang.name} should not be empty", strings.holyQuran.isNotEmpty())
            assertTrue("Fajr in ${lang.name} should not be empty", strings.fajr.isNotEmpty())
            assertTrue("Dhuhr in ${lang.name} should not be empty", strings.dhuhr.isNotEmpty())
            assertTrue("Asr in ${lang.name} should not be empty", strings.asr.isNotEmpty())
            assertTrue("Maghrib in ${lang.name} should not be empty", strings.maghrib.isNotEmpty())
            assertTrue("Isha in ${lang.name} should not be empty", strings.isha.isNotEmpty())
            assertTrue("Qibla in ${lang.name} should not be empty", strings.qiblaCompass.isNotEmpty())
            assertTrue("Tasbeeh in ${lang.name} should not be empty", strings.digitalTasbeeh.isNotEmpty())
            assertTrue("Duas in ${lang.name} should not be empty", strings.dailyDuas.isNotEmpty())
            assertTrue("Settings in ${lang.name} should not be empty", strings.settingsTitle.isNotEmpty())
        }
    }

    @Test
    fun testHargeisaSomaliaPrayerTimes() {
        // Hargeisa, Somalia coordinates: 9.5600° N, 44.0650° E, TimeZone: Africa/Mogadishu (+3 UTC)
        val tz = TimeZone.getTimeZone("Africa/Mogadishu")
        val calendar = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 7, 12, 0, 0)
        }

        val prayers = PrayerTimesCalculator.calculate(
            latitude = 9.5600,
            longitude = 44.0650,
            timeZone = tz,
            calendar = calendar,
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE,
            juristic = JuristicMethod.STANDARD
        )

        assertNotNull(prayers.fajr)
        assertNotNull(prayers.sunrise)
        assertNotNull(prayers.dhuhr)
        assertNotNull(prayers.asr)
        assertNotNull(prayers.maghrib)
        assertNotNull(prayers.isha)

        // Verify logical progression of prayer hours
        assertTrue("Fajr must be before sunrise", prayers.fajr.hour < prayers.sunrise.hour || (prayers.fajr.hour == prayers.sunrise.hour && prayers.fajr.minute < prayers.sunrise.minute))
        assertTrue("Sunrise must be before Dhuhr", prayers.sunrise.hour < prayers.dhuhr.hour)
        assertTrue("Dhuhr must be before Asr", prayers.dhuhr.hour < prayers.asr.hour)
        assertTrue("Asr must be before Maghrib", prayers.asr.hour < prayers.maghrib.hour)
        assertTrue("Maghrib must be before Isha", prayers.maghrib.hour < prayers.isha.hour)

        // Hargeisa Dhuhr is around 11:45 AM - 12:15 PM
        assertTrue("Hargeisa Dhuhr hour should be around 11 or 12", prayers.dhuhr.hour in 11..12)
        // Fajr is in early morning (4-5 AM)
        assertTrue("Hargeisa Fajr hour should be around 4 or 5", prayers.fajr.hour in 4..5)
    }

    @Test
    fun testHargeisaSomaliaQiblaCalculation() {
        // Hargeisa, Somalia (9.5600° N, 44.0650° E) to Holy Kaaba (21.4225° N, 39.8262° E)
        val bearing = QiblaCalculator.calculateBearing(9.5600, 44.0650)
        // Kaaba is NNW of Hargeisa, bearing ~335-345 degrees
        assertTrue("Qibla bearing from Hargeisa should be ~335-345°", bearing in 335f..350f)

        val distanceKm = QiblaCalculator.calculateDistanceKm(9.5600, 44.0650)
        // Great circle distance from Hargeisa to Mecca is ~1350-1450 km
        assertTrue("Distance from Hargeisa to Makkah should be ~1350-1450 km", distanceKm in 1300..1500)
    }

    @Test
    fun testQuranAudioReciterUrls() {
        assertTrue("Must support at least 4 Qaris", AvailableQaris.size >= 4)
        val qariIds = AvailableQaris.map { it.id }
        assertTrue(qariIds.contains("alafasy"))
        assertTrue(qariIds.contains("abdulbaset"))
        assertTrue(qariIds.contains("ghamdi"))
        assertTrue(qariIds.contains("husary"))

        AvailableQaris.forEach { qari ->
            val url = QuranRepository.getAyahAudioUrl(qari, 1, 1)
            assertTrue("Audio URL must start with https://everyayah.com/data/", url.startsWith("https://everyayah.com/data/"))
            assertTrue("Audio URL must end with 001001.mp3", url.endsWith("001001.mp3"))
        }
    }

    @Test
    fun testArabicRtlLayoutDirection() {
        assertEquals("Arabic must use RTL", androidx.compose.ui.unit.LayoutDirection.Rtl, AppLanguage.ARABIC.layoutDirection)
        assertEquals("Somali must use LTR", androidx.compose.ui.unit.LayoutDirection.Ltr, AppLanguage.SOMALI.layoutDirection)
        assertEquals("English must use LTR", androidx.compose.ui.unit.LayoutDirection.Ltr, AppLanguage.ENGLISH.layoutDirection)
        assertEquals("Turkish must use LTR", androidx.compose.ui.unit.LayoutDirection.Ltr, AppLanguage.TURKISH.layoutDirection)
        assertEquals("French must use LTR", androidx.compose.ui.unit.LayoutDirection.Ltr, AppLanguage.FRENCH.layoutDirection)
    }

    @Test
    fun testDownloadManagerFormatBytes() {
        assertEquals("500 B", com.example.data.repository.QuranAudioDownloadManager.formatBytes(500L))
        assertEquals("100 KB", com.example.data.repository.QuranAudioDownloadManager.formatBytes(102400L))
        assertEquals("15.0 MB", com.example.data.repository.QuranAudioDownloadManager.formatBytes(15728640L))
    }

    @Test
    fun testRecitersHaveQualityAndStyle() {
        AvailableQaris.forEach { qari ->
            assertTrue("Reciter ${qari.name} should specify audio quality", qari.quality.isNotEmpty())
            assertTrue("Reciter ${qari.name} should specify style", qari.style.isNotEmpty())
        }
    }
}
