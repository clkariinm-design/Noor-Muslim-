package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun bundledUthmaniAssetContainsAll114SurahsAnd6236Ayahs() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val root = context.assets.open("quran_uthmani.json")
      .bufferedReader(Charsets.UTF_8)
      .use { JSONObject(it.readText()) }
    val surahs = root.getJSONArray("surahs")
    assertEquals(114, surahs.length())

    var totalNumberedAyahs = 0
    for (index in 0 until surahs.length()) {
      val ayahs = surahs.getJSONObject(index).getJSONArray("ayahs")
      for (ayahIndex in 0 until ayahs.length()) {
        if (ayahs.getJSONObject(ayahIndex).getInt("number") > 0) {
          totalNumberedAyahs++
        }
      }
    }
    assertEquals(6236, totalNumberedAyahs)
  }

  @Test
  fun testAppNameString() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Noor Muslim", appName)
  }

  @Test
  fun testAppPreferencesPersistence() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = AppPreferences(context)

    // Test language persistence
    prefs.setLanguage(AppLanguage.SOMALI)
    assertEquals(AppLanguage.SOMALI, prefs.getLanguage())

    prefs.setLanguage(AppLanguage.ARABIC)
    assertEquals(AppLanguage.ARABIC, prefs.getLanguage())

    // Test default city
    val city = prefs.getSelectedCity()
    assertNotNull(city)
    assertEquals("Hargeisa", city.name)
    assertEquals("Somalia", city.country)

    // Test Tasbeeh counters
    prefs.setTasbeehCount(33)
    assertEquals(33, prefs.getTasbeehCount())
    prefs.setTasbeehTarget(99)
    assertEquals(99, prefs.getTasbeehTarget())

    val initialLifetime = prefs.getTasbeehLifetime()
    prefs.addTasbeehLifetime(7)
    assertEquals(initialLifetime + 7, prefs.getTasbeehLifetime())

    // Test Bookmarks
    val isAdded = prefs.toggleBookmark(1, "Al-Fatihah", 5)
    assertTrue(isAdded)
    assertTrue(prefs.isBookmarked(1, 5))

    val isRemoved = prefs.toggleBookmark(1, "Al-Fatihah", 5)
    assertFalse(isRemoved)
    assertFalse(prefs.isBookmarked(1, 5))

    // Test Last Read
    prefs.setLastRead(36, "Ya-Sin", 1)
    val lastRead = prefs.getLastRead()
    assertNotNull(lastRead)
    assertEquals(36, lastRead!!.surahNumber)
    assertEquals("Ya-Sin", lastRead.surahName)
    assertEquals(1, lastRead.ayahNumber)
  }
}
