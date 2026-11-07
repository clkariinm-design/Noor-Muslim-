package com.example.data.model

enum class PrayerType {
    FAJR,
    SUNRISE,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA
}

data class PrayerTimeItem(
    val type: PrayerType,
    val timeFormatted: String,
    val hour: Int,
    val minute: Int,
    val isNext: Boolean = false,
    val isPast: Boolean = false
)

enum class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    MUSLIM_WORLD_LEAGUE("Muslim World League", 18.0, 17.0),
    ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
    EGYPT("Egyptian General Authority of Survey", 19.5, 17.5),
    UMM_AL_QURA("Umm Al-Qura University, Makkah", 18.5, 0.0, 90),
    KARACHI("Univ. of Islamic Sciences, Karachi", 18.0, 18.0)
}

enum class JuristicMethod(val title: String, val shadowFactor: Int) {
    STANDARD("Standard (Shafi'i, Maliki, Hanbali)", 1),
    HANAFI("Hanafi", 2)
}

data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

val DefaultCities = listOf(
    CityLocation("Hargeisa", "Somalia", 9.5600, 44.0650, "Africa/Mogadishu"),
    CityLocation("Mogadishu", "Somalia", 2.0469, 45.3182, "Africa/Mogadishu"),
    CityLocation("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
    CityLocation("Madinah", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
    CityLocation("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
    CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
    CityLocation("Ankara", "Turkey", 39.9334, 32.8597, "Europe/Istanbul"),
    CityLocation("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
    CityLocation("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
    CityLocation("New York", "United States", 40.7128, -74.0060, "America/New_York"),
    CityLocation("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
    CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
    CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
    CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
    CityLocation("Nairobi", "Kenya", -1.2921, 36.8219, "Africa/Nairobi"),
    CityLocation("Johannesburg", "South Africa", -26.2041, 28.0473, "Africa/Johannesburg")
)
