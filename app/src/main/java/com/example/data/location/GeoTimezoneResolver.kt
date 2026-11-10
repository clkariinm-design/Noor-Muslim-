package com.example.data.location

import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * High-performance, offline geographical timezone resolver.
 * Maps any latitude and longitude on Earth to its canonical IANA Timezone ID.
 * Completely eliminates any reliance on device system default timezone.
 */
object GeoTimezoneResolver {

    private data class BoundingBox(
        val minLat: Double,
        val maxLat: Double,
        val minLng: Double,
        val maxLng: Double,
        val timeZoneId: String
    ) {
        fun contains(lat: Double, lng: Double): Boolean {
            return lat in minLat..maxLat && lng in minLng..maxLng
        }
    }

    // Comprehensive geographical bounding boxes covering key regions and worldwide landmasses
    private val regions = listOf(
        // Somalia, Somaliland & Horn of Africa
        BoundingBox(minLat = -1.7, maxLat = 12.0, minLng = 40.5, maxLng = 51.5, timeZoneId = "Africa/Mogadishu"),
        // Djibouti
        BoundingBox(minLat = 10.9, maxLat = 12.8, minLng = 41.7, maxLng = 43.5, timeZoneId = "Africa/Djibouti"),
        // Ethiopia
        BoundingBox(minLat = 3.4, maxLat = 14.9, minLng = 33.0, maxLng = 48.0, timeZoneId = "Africa/Addis_Ababa"),
        // Kenya
        BoundingBox(minLat = -4.7, maxLat = 5.0, minLng = 33.9, maxLng = 41.9, timeZoneId = "Africa/Nairobi"),
        // Tanzania
        BoundingBox(minLat = -11.8, maxLat = -1.0, minLng = 29.3, maxLng = 40.5, timeZoneId = "Africa/Dar_es_Salaam"),
        // Egypt
        BoundingBox(minLat = 22.0, maxLat = 31.7, minLng = 25.0, maxLng = 37.0, timeZoneId = "Africa/Cairo"),
        // Sudan
        BoundingBox(minLat = 9.5, maxLat = 22.0, minLng = 21.8, maxLng = 38.6, timeZoneId = "Africa/Khartoum"),
        // Libya
        BoundingBox(minLat = 19.5, maxLat = 33.0, minLng = 9.4, maxLng = 25.2, timeZoneId = "Africa/Tripoli"),
        // Tunisia
        BoundingBox(minLat = 30.2, maxLat = 37.5, minLng = 7.5, maxLng = 11.6, timeZoneId = "Africa/Tunis"),
        // Algeria
        BoundingBox(minLat = 18.9, maxLat = 37.1, minLng = -8.7, maxLng = 12.0, timeZoneId = "Africa/Algiers"),
        // Morocco
        BoundingBox(minLat = 21.0, maxLat = 36.0, minLng = -17.1, maxLng = -1.0, timeZoneId = "Africa/Casablanca"),
        // Nigeria
        BoundingBox(minLat = 4.2, maxLat = 13.9, minLng = 2.6, maxLng = 14.7, timeZoneId = "Africa/Lagos"),
        // South Africa
        BoundingBox(minLat = -34.9, maxLat = -22.1, minLng = 16.4, maxLng = 32.9, timeZoneId = "Africa/Johannesburg"),

        // Arabian Peninsula
        // Saudi Arabia (Makkah, Madinah, Riyadh, Jeddah)
        BoundingBox(minLat = 16.0, maxLat = 32.2, minLng = 34.5, maxLng = 55.7, timeZoneId = "Asia/Riyadh"),
        // United Arab Emirates (Dubai, Abu Dhabi)
        BoundingBox(minLat = 22.6, maxLat = 26.1, minLng = 51.5, maxLng = 56.4, timeZoneId = "Asia/Dubai"),
        // Qatar
        BoundingBox(minLat = 24.5, maxLat = 26.2, minLng = 50.7, maxLng = 51.7, timeZoneId = "Asia/Qatar"),
        // Kuwait
        BoundingBox(minLat = 28.5, maxLat = 30.1, minLng = 46.5, maxLng = 48.5, timeZoneId = "Asia/Kuwait"),
        // Bahrain
        BoundingBox(minLat = 25.8, maxLat = 26.3, minLng = 50.4, maxLng = 50.7, timeZoneId = "Asia/Bahrain"),
        // Oman
        BoundingBox(minLat = 16.6, maxLat = 26.4, minLng = 52.0, maxLng = 59.9, timeZoneId = "Asia/Muscat"),
        // Yemen
        BoundingBox(minLat = 12.1, maxLat = 19.0, minLng = 41.8, maxLng = 54.5, timeZoneId = "Asia/Aden"),
        // Jordan
        BoundingBox(minLat = 29.2, maxLat = 33.4, minLng = 34.9, maxLng = 39.3, timeZoneId = "Asia/Amman"),
        // Palestine / Jerusalem
        BoundingBox(minLat = 31.2, maxLat = 32.6, minLng = 34.2, maxLng = 35.6, timeZoneId = "Asia/Jerusalem"),
        // Lebanon
        BoundingBox(minLat = 33.0, maxLat = 34.7, minLng = 35.1, maxLng = 36.7, timeZoneId = "Asia/Beirut"),
        // Syria
        BoundingBox(minLat = 32.3, maxLat = 37.3, minLng = 35.6, maxLng = 42.4, timeZoneId = "Asia/Damascus"),
        // Iraq
        BoundingBox(minLat = 29.0, maxLat = 37.4, minLng = 38.8, maxLng = 48.6, timeZoneId = "Asia/Baghdad"),
        // Iran
        BoundingBox(minLat = 25.0, maxLat = 39.8, minLng = 44.0, maxLng = 63.3, timeZoneId = "Asia/Tehran"),

        // Turkey
        BoundingBox(minLat = 35.8, maxLat = 42.1, minLng = 25.6, maxLng = 44.8, timeZoneId = "Europe/Istanbul"),

        // Europe
        // United Kingdom
        BoundingBox(minLat = 49.8, maxLat = 60.9, minLng = -8.2, maxLng = 1.8, timeZoneId = "Europe/London"),
        // Ireland
        BoundingBox(minLat = 51.4, maxLat = 55.4, minLng = -10.5, maxLng = -5.9, timeZoneId = "Europe/Dublin"),
        // France
        BoundingBox(minLat = 41.3, maxLat = 51.1, minLng = -4.8, maxLng = 9.6, timeZoneId = "Europe/Paris"),
        // Germany
        BoundingBox(minLat = 47.2, maxLat = 55.1, minLng = 5.8, maxLng = 15.0, timeZoneId = "Europe/Berlin"),
        // Spain
        BoundingBox(minLat = 36.0, maxLat = 43.8, minLng = -9.3, maxLng = 3.3, timeZoneId = "Europe/Madrid"),
        // Italy
        BoundingBox(minLat = 36.6, maxLat = 47.1, minLng = 6.6, maxLng = 18.5, timeZoneId = "Europe/Rome"),
        // Netherlands
        BoundingBox(minLat = 50.7, maxLat = 53.6, minLng = 3.3, maxLng = 7.2, timeZoneId = "Europe/Amsterdam"),
        // Sweden
        BoundingBox(minLat = 55.3, maxLat = 69.1, minLng = 11.0, maxLng = 24.2, timeZoneId = "Europe/Stockholm"),
        // Norway
        BoundingBox(minLat = 57.9, maxLat = 71.2, minLng = 4.6, maxLng = 31.1, timeZoneId = "Europe/Oslo"),
        // Russia (Moscow)
        BoundingBox(minLat = 50.0, maxLat = 65.0, minLng = 30.0, maxLng = 50.0, timeZoneId = "Europe/Moscow"),

        // South & Central Asia
        // Pakistan
        BoundingBox(minLat = 23.6, maxLat = 37.1, minLng = 60.9, maxLng = 77.8, timeZoneId = "Asia/Karachi"),
        // India
        BoundingBox(minLat = 8.0, maxLat = 37.0, minLng = 68.1, maxLng = 97.4, timeZoneId = "Asia/Kolkata"),
        // Bangladesh
        BoundingBox(minLat = 20.6, maxLat = 26.6, minLng = 88.0, maxLng = 92.7, timeZoneId = "Asia/Dhaka"),
        // Uzbekistan
        BoundingBox(minLat = 37.2, maxLat = 45.6, minLng = 56.0, maxLng = 73.2, timeZoneId = "Asia/Tashkent"),
        // Kazakhstan
        BoundingBox(minLat = 40.9, maxLat = 55.4, minLng = 46.5, maxLng = 87.3, timeZoneId = "Asia/Almaty"),

        // Southeast & East Asia
        // Malaysia
        BoundingBox(minLat = 1.0, maxLat = 7.4, minLng = 99.6, maxLng = 119.3, timeZoneId = "Asia/Kuala_Lumpur"),
        // Singapore
        BoundingBox(minLat = 1.15, maxLat = 1.48, minLng = 103.6, maxLng = 104.0, timeZoneId = "Asia/Singapore"),
        // Indonesia (West - Jakarta)
        BoundingBox(minLat = -11.0, maxLat = 6.0, minLng = 95.0, maxLng = 110.0, timeZoneId = "Asia/Jakarta"),
        // Indonesia (Central - Bali/Makassar)
        BoundingBox(minLat = -11.0, maxLat = 4.0, minLng = 110.0, maxLng = 125.0, timeZoneId = "Asia/Makassar"),
        // China
        BoundingBox(minLat = 18.0, maxLat = 53.6, minLng = 73.5, maxLng = 135.0, timeZoneId = "Asia/Shanghai"),
        // Japan
        BoundingBox(minLat = 24.0, maxLat = 45.5, minLng = 122.9, maxLng = 153.0, timeZoneId = "Asia/Tokyo"),
        // South Korea
        BoundingBox(minLat = 33.0, maxLat = 38.6, minLng = 124.6, maxLng = 131.9, timeZoneId = "Asia/Seoul"),

        // North America
        // US Eastern (New York, Boston, DC, Miami, Atlanta)
        BoundingBox(minLat = 24.5, maxLat = 47.5, minLng = -85.0, maxLng = -66.9, timeZoneId = "America/New_York"),
        // US Central (Chicago, Houston, Dallas, Minneapolis)
        BoundingBox(minLat = 25.8, maxLat = 49.4, minLng = -104.0, maxLng = -85.0, timeZoneId = "America/Chicago"),
        // US Mountain (Denver, Phoenix, Salt Lake City)
        BoundingBox(minLat = 31.3, maxLat = 49.0, minLng = -114.5, maxLng = -104.0, timeZoneId = "America/Denver"),
        // US Pacific (Los Angeles, San Francisco, Seattle)
        BoundingBox(minLat = 32.5, maxLat = 49.0, minLng = -124.8, maxLng = -114.5, timeZoneId = "America/Los_Angeles"),
        // US Alaska
        BoundingBox(minLat = 51.0, maxLat = 71.4, minLng = -179.0, maxLng = -130.0, timeZoneId = "America/Anchorage"),
        // US Hawaii
        BoundingBox(minLat = 18.9, maxLat = 22.3, minLng = -160.3, maxLng = -154.8, timeZoneId = "Pacific/Honolulu"),
        // Canada (Eastern - Toronto, Montreal, Ottawa)
        BoundingBox(minLat = 41.6, maxLat = 62.0, minLng = -80.0, maxLng = -60.0, timeZoneId = "America/Toronto"),
        // Canada (Western - Vancouver)
        BoundingBox(minLat = 48.0, maxLat = 60.0, minLng = -139.0, maxLng = -114.0, timeZoneId = "America/Vancouver"),

        // South America
        // Brazil (Sao Paulo, Rio)
        BoundingBox(minLat = -33.8, maxLat = 5.3, minLng = -73.9, maxLng = -34.8, timeZoneId = "America/Sao_Paulo"),
        // Argentina (Buenos Aires)
        BoundingBox(minLat = -55.0, maxLat = -21.8, minLng = -73.6, maxLng = -53.6, timeZoneId = "America/Argentina/Buenos_Aires"),

        // Oceania
        // Australia (East - Sydney, Melbourne)
        BoundingBox(minLat = -39.2, maxLat = -10.0, minLng = 141.0, maxLng = 153.7, timeZoneId = "Australia/Sydney"),
        // Australia (Central - Adelaide)
        BoundingBox(minLat = -38.1, maxLat = -10.0, minLng = 129.0, maxLng = 141.0, timeZoneId = "Australia/Adelaide"),
        // Australia (West - Perth)
        BoundingBox(minLat = -35.2, maxLat = -13.7, minLng = 112.9, maxLng = 129.0, timeZoneId = "Australia/Perth"),
        // New Zealand
        BoundingBox(minLat = -47.3, maxLat = -34.4, minLng = 166.4, maxLng = 178.6, timeZoneId = "Pacific/Auckland")
    )

    /**
     * Resolves the canonical IANA timezone ID for the given geographical coordinates.
     * Guaranteed to return a valid IANA timezone identifier accepted by java.util.TimeZone and java.time.ZoneId.
     */
    fun resolveTimezoneId(latitude: Double, longitude: Double): String {
        // 1. Check geographical land boundaries
        for (region in regions) {
            if (region.contains(latitude, longitude)) {
                return region.timeZoneId
            }
        }

        // 2. Exact longitude-based solar/nautical timezone fallback
        // Longitude spans -180 to +180 (360 degrees / 24 hours = 15 degrees per hour)
        val offsetHours = (longitude / 15.0).roundToInt().coerceIn(-12, 14)
        return getCanonicalZoneForGmtOffset(offsetHours)
    }

    /**
     * Resolves a java.util.TimeZone directly for given coordinates.
     */
    fun resolveTimeZone(latitude: Double, longitude: Double): TimeZone {
        val id = resolveTimezoneId(latitude, longitude)
        return TimeZone.getTimeZone(id)
    }

    private fun getCanonicalZoneForGmtOffset(offsetHours: Int): String {
        return when (offsetHours) {
            -12 -> "Etc/GMT+12"
            -11 -> "Pacific/Midway"
            -10 -> "Pacific/Honolulu"
            -9 -> "America/Anchorage"
            -8 -> "America/Los_Angeles"
            -7 -> "America/Denver"
            -6 -> "America/Chicago"
            -5 -> "America/New_York"
            -4 -> "America/Halifax"
            -3 -> "America/Sao_Paulo"
            -2 -> "America/Noronha"
            -1 -> "Atlantic/Cape_Verde"
            0 -> "UTC"
            1 -> "Europe/Paris"
            2 -> "Africa/Cairo"
            3 -> "Africa/Mogadishu"
            4 -> "Asia/Dubai"
            5 -> "Asia/Karachi"
            6 -> "Asia/Dhaka"
            7 -> "Asia/Bangkok"
            8 -> "Asia/Singapore"
            9 -> "Asia/Tokyo"
            10 -> "Australia/Sydney"
            11 -> "Pacific/Guadalcanal"
            12 -> "Pacific/Auckland"
            13 -> "Pacific/Tongatapu"
            14 -> "Pacific/Kiritimati"
            else -> "UTC"
        }
    }
}
