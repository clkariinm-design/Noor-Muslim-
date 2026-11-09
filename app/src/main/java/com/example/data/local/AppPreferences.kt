package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.location.GlobalLocationEngine
import com.example.data.location.LocationData
import com.example.data.model.AppLanguage
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.DefaultCities
import com.example.data.model.JuristicMethod
import com.example.data.model.Qari
import com.example.data.model.ReadingBookmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("noor_muslim_prefs", Context.MODE_PRIVATE)

    private val _languageFlow = MutableStateFlow(getLanguage())
    val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()

    private val _locationFlow = MutableStateFlow(getCachedLocation())
    val locationFlow: StateFlow<LocationData> = _locationFlow.asStateFlow()

    private val _selectedCityFlow = MutableStateFlow(getSelectedCity())
    val selectedCityFlow: StateFlow<CityLocation> = _selectedCityFlow.asStateFlow()

    private val _arabicFontSizeFlow = MutableStateFlow(getArabicFontSize())
    val arabicFontSizeFlow: StateFlow<Float> = _arabicFontSizeFlow.asStateFlow()

    private val _translationFontSizeFlow = MutableStateFlow(getTranslationFontSize())
    val translationFontSizeFlow: StateFlow<Float> = _translationFontSizeFlow.asStateFlow()

    private val _tasbeehCountFlow = MutableStateFlow(getTasbeehCount())
    val tasbeehCountFlow: StateFlow<Int> = _tasbeehCountFlow.asStateFlow()

    private val _tasbeehTargetFlow = MutableStateFlow(getTasbeehTarget())
    val tasbeehTargetFlow: StateFlow<Int> = _tasbeehTargetFlow.asStateFlow()

    private val _tasbeehLifetimeFlow = MutableStateFlow(getTasbeehLifetime())
    val tasbeehLifetimeFlow: StateFlow<Int> = _tasbeehLifetimeFlow.asStateFlow()

    private val _selectedDhikrFlow = MutableStateFlow(getSelectedDhikr())
    val selectedDhikrFlow: StateFlow<String> = _selectedDhikrFlow.asStateFlow()

    private val _vibrationEnabledFlow = MutableStateFlow(isVibrationEnabled())
    val vibrationEnabledFlow: StateFlow<Boolean> = _vibrationEnabledFlow.asStateFlow()

    private val _bookmarksFlow = MutableStateFlow(getBookmarks())
    val bookmarksFlow: StateFlow<List<ReadingBookmark>> = _bookmarksFlow.asStateFlow()

    private val _lastReadFlow = MutableStateFlow(getLastRead())
    val lastReadFlow: StateFlow<ReadingBookmark?> = _lastReadFlow.asStateFlow()

    // Language
    fun getLanguage(): AppLanguage {
        val code = prefs.getString("key_language", "en") ?: "en"
        return AppLanguage.fromCode(code)
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("key_language", lang.code).apply()
        _languageFlow.value = lang
    }

    // First-launch onboarding
    fun hasSeenWelcome(): Boolean = prefs.getBoolean("key_has_seen_welcome", false)
    fun setHasSeenWelcome(seen: Boolean) {
        prefs.edit().putBoolean("key_has_seen_welcome", seen).apply()
    }

    // Theme: "SYSTEM", "LIGHT", "DARK"
    fun getThemeMode(): String = prefs.getString("key_theme", "SYSTEM") ?: "SYSTEM"
    fun setThemeMode(mode: String) {
        prefs.edit().putString("key_theme", mode).apply()
        _themeModeFlow.value = mode
    }

    // Font Sizes
    fun getArabicFontSize(): Float = prefs.getFloat("key_arabic_font_size", 26f)
    fun setArabicFontSize(size: Float) {
        prefs.edit().putFloat("key_arabic_font_size", size).apply()
        _arabicFontSizeFlow.value = size
    }

    fun getTranslationFontSize(): Float = prefs.getFloat("key_trans_font_size", 15f)
    fun setTranslationFontSize(size: Float) {
        prefs.edit().putFloat("key_trans_font_size", size).apply()
        _translationFontSizeFlow.value = size
    }

    // Location Persistence & Cache
    fun getCachedLocation(): LocationData {
        val name = prefs.getString("key_city_name", GlobalLocationEngine.DefaultLocation.name) ?: GlobalLocationEngine.DefaultLocation.name
        val country = prefs.getString("key_city_country", GlobalLocationEngine.DefaultLocation.country) ?: GlobalLocationEngine.DefaultLocation.country
        val lat = prefs.getFloat("key_city_lat", GlobalLocationEngine.DefaultLocation.latitude.toFloat()).toDouble()
        val lng = prefs.getFloat("key_city_lng", GlobalLocationEngine.DefaultLocation.longitude.toFloat()).toDouble()
        val tz = prefs.getString("key_city_tz", GlobalLocationEngine.DefaultLocation.timeZoneId) ?: GlobalLocationEngine.DefaultLocation.timeZoneId
        val isGps = prefs.getBoolean("key_city_is_gps", false)
        val timestamp = prefs.getLong("key_city_timestamp", System.currentTimeMillis())
        return LocationData(name, country, lat, lng, tz, isGps, timestamp)
    }

    fun saveLocation(location: LocationData) {
        prefs.edit()
            .putString("key_city_name", location.name)
            .putString("key_city_country", location.country)
            .putFloat("key_city_lat", location.latitude.toFloat())
            .putFloat("key_city_lng", location.longitude.toFloat())
            .putString("key_city_tz", location.timeZoneId)
            .putBoolean("key_city_is_gps", location.isGpsDerived)
            .putLong("key_city_timestamp", location.lastUpdatedTimestamp)
            .apply()
        _locationFlow.value = location
        _selectedCityFlow.value = CityLocation(location.name, location.country, location.latitude, location.longitude, location.timeZoneId)
    }

    fun getSelectedCity(): CityLocation {
        val loc = getCachedLocation()
        return CityLocation(loc.name, loc.country, loc.latitude, loc.longitude, loc.timeZoneId)
    }

    fun setSelectedCity(city: CityLocation) {
        saveLocation(LocationData(city.name, city.country, city.latitude, city.longitude, city.timeZoneId, false))
    }

    // Calculation Method
    fun getCalculationMethod(): CalculationMethod {
        val name = prefs.getString("key_calc_method", CalculationMethod.MUSLIM_WORLD_LEAGUE.name)
        return try {
            CalculationMethod.valueOf(name ?: CalculationMethod.MUSLIM_WORLD_LEAGUE.name)
        } catch (e: Exception) {
            CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString("key_calc_method", method.name).apply()
    }

    // Juristic Method
    fun getJuristicMethod(): JuristicMethod {
        val name = prefs.getString("key_juristic_method", JuristicMethod.STANDARD.name)
        return try {
            JuristicMethod.valueOf(name ?: JuristicMethod.STANDARD.name)
        } catch (e: Exception) {
            JuristicMethod.STANDARD
        }
    }

    fun setJuristicMethod(method: JuristicMethod) {
        prefs.edit().putString("key_juristic_method", method.name).apply()
    }

    // Selected Qari
    fun getSelectedQariId(): String = prefs.getString("key_qari_id", "alafasy") ?: "alafasy"
    fun setSelectedQariId(qariId: String) {
        prefs.edit().putString("key_qari_id", qariId).apply()
    }

    // Tasbeeh
    fun getTasbeehCount(): Int = prefs.getInt("key_tasbeeh_count", 0)
    fun setTasbeehCount(count: Int) {
        prefs.edit().putInt("key_tasbeeh_count", count).apply()
        _tasbeehCountFlow.value = count
    }

    fun getTasbeehTarget(): Int = prefs.getInt("key_tasbeeh_target", 33)
    fun setTasbeehTarget(target: Int) {
        prefs.edit().putInt("key_tasbeeh_target", target).apply()
        _tasbeehTargetFlow.value = target
    }

    fun getTasbeehLifetime(): Int = prefs.getInt("key_tasbeeh_lifetime", 0)
    fun addTasbeehLifetime(added: Int = 1) {
        val newTotal = getTasbeehLifetime() + added
        prefs.edit().putInt("key_tasbeeh_lifetime", newTotal).apply()
        _tasbeehLifetimeFlow.value = newTotal
    }

    fun getSelectedDhikr(): String = prefs.getString("key_selected_dhikr", "SubhanAllah (سُبْحَانَ اللَّهِ)") ?: "SubhanAllah (سُبْحَانَ اللَّهِ)"
    fun setSelectedDhikr(dhikr: String) {
        prefs.edit().putString("key_selected_dhikr", dhikr).apply()
        _selectedDhikrFlow.value = dhikr
    }

    fun isVibrationEnabled(): Boolean = prefs.getBoolean("key_vibration_enabled", true)
    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("key_vibration_enabled", enabled).apply()
        _vibrationEnabledFlow.value = enabled
    }

    // Bookmarks
    fun getBookmarks(): List<ReadingBookmark> {
        val raw = prefs.getString("key_bookmarks_json", "[]") ?: "[]"
        val list = mutableListOf<ReadingBookmark>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ReadingBookmark(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        surahNumber = obj.getInt("surahNumber"),
                        surahName = obj.getString("surahName"),
                        ayahNumber = obj.getInt("ayahNumber"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            // ignore
        }
        return list
    }

    fun toggleBookmark(surahNumber: Int, surahName: String, ayahNumber: Int): Boolean {
        val current = getBookmarks().toMutableList()
        val existingIndex = current.indexOfFirst { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }
        val added: Boolean
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            added = false
        } else {
            current.add(0, ReadingBookmark(surahNumber = surahNumber, surahName = surahName, ayahNumber = ayahNumber))
            added = true
        }
        val arr = JSONArray()
        for (b in current) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("surahNumber", b.surahNumber)
            obj.put("surahName", b.surahName)
            obj.put("ayahNumber", b.ayahNumber)
            obj.put("timestamp", b.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("key_bookmarks_json", arr.toString()).apply()
        _bookmarksFlow.value = current
        return added
    }

    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean {
        return getBookmarks().any { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }
    }

    // Last Read
    fun getLastRead(): ReadingBookmark? {
        val sNum = prefs.getInt("key_last_read_surah", 1)
        val sName = prefs.getString("key_last_read_name", "Al-Fatihah") ?: "Al-Fatihah"
        val aNum = prefs.getInt("key_last_read_ayah", 1)
        return ReadingBookmark(surahNumber = sNum, surahName = sName, ayahNumber = aNum)
    }

    fun setLastRead(surahNumber: Int, surahName: String, ayahNumber: Int) {
        prefs.edit()
            .putInt("key_last_read_surah", surahNumber)
            .putString("key_last_read_name", surahName)
            .putInt("key_last_read_ayah", ayahNumber)
            .apply()
        _lastReadFlow.value = ReadingBookmark(surahNumber = surahNumber, surahName = surahName, ayahNumber = ayahNumber)
    }

    // Notifications
    fun isPrayerNotificationsEnabled(): Boolean = prefs.getBoolean("key_prayer_notifs", true)
    fun setPrayerNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("key_prayer_notifs", enabled).apply()
    }
    // Per-prayer Adhan controls (enabled by default for the five obligatory prayers).
    fun isAdhanEnabled(prayer: com.example.data.model.PrayerType): Boolean =
        prefs.getBoolean("key_adhan_${prayer.name.lowercase()}", prayer != com.example.data.model.PrayerType.SUNRISE)

    fun setAdhanEnabled(prayer: com.example.data.model.PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean("key_adhan_${prayer.name.lowercase()}", enabled).apply()
    }

    fun areAdhansEnabled(): Boolean =
        listOf(
            com.example.data.model.PrayerType.FAJR,
            com.example.data.model.PrayerType.DHUHR,
            com.example.data.model.PrayerType.ASR,
            com.example.data.model.PrayerType.MAGHRIB,
            com.example.data.model.PrayerType.ISHA
        ).any { isAdhanEnabled(it) }

}
