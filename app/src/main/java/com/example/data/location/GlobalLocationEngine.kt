package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.repository.QiblaCalculator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LocationData(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val isGpsDerived: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

data class TimezoneCrossingEvent(
    val oldTimezone: String,
    val newTimezone: String,
    val distanceKm: Double,
    val isSignificantTravel: Boolean
)

sealed class LocationError(val message: String) {
    object PermissionDenied : LocationError("Location permission denied by user")
    object GpsDisabled : LocationError("Location services / GPS is disabled on this device")
    object LocationUnavailable : LocationError("Unable to acquire GPS fix. Please verify location settings.")
}

object GlobalLocationEngine {

    val DefaultLocation = LocationData(
        name = "Hargeisa",
        country = "Somalia",
        latitude = 9.5600,
        longitude = 44.0650,
        timeZoneId = "Africa/Mogadishu",
        isGpsDerived = false
    )

    // Curated starter cities from across the world - NOT a restriction, any worldwide location can be searched
    val PopularGlobalLocations = listOf(
        LocationData("Hargeisa", "Somalia", 9.5600, 44.0650, "Africa/Mogadishu"),
        LocationData("Mogadishu", "Somalia", 2.0469, 45.3182, "Africa/Mogadishu"),
        LocationData("Makkah", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
        LocationData("Madinah", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
        LocationData("Jerusalem (Al-Quds)", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem"),
        LocationData("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        LocationData("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
        LocationData("Ankara", "Turkey", 39.9334, 32.8597, "Europe/Istanbul"),
        LocationData("Dubai", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
        LocationData("Doha", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
        LocationData("Amman", "Jordan", 31.9454, 35.9284, "Asia/Amman"),
        LocationData("Baghdad", "Iraq", 33.3152, 44.3661, "Asia/Baghdad"),
        LocationData("London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
        LocationData("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
        LocationData("Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
        LocationData("New York", "United States", 40.7128, -74.0060, "America/New_York"),
        LocationData("Chicago", "United States", 41.8781, -87.6298, "America/Chicago"),
        LocationData("Los Angeles", "United States", 34.0522, -118.2437, "America/Los_Angeles"),
        LocationData("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
        LocationData("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        LocationData("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
        LocationData("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
        LocationData("Lahore", "Pakistan", 31.5204, 74.3587, "Asia/Karachi"),
        LocationData("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
        LocationData("Nairobi", "Kenya", -1.2921, 36.8219, "Africa/Nairobi"),
        LocationData("Johannesburg", "South Africa", -26.2041, 28.0473, "Africa/Johannesburg"),
        LocationData("Casablanca", "Morocco", 33.5731, -7.5898, "Africa/Casablanca"),
        LocationData("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
        LocationData("Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo")
    )

    /**
     * Checks if location service (GPS or Network provider) is enabled on the device.
     */
    fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val gpsEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
        val networkEnabled = try {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
        return gpsEnabled || networkEnabled
    }

    /**
     * Checks if location permissions are granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Fetches real GPS coordinates using FusedLocationProviderClient with PRIORITY_HIGH_ACCURACY.
     * Maps coordinates to accurate IANA timezone using GeoTimezoneResolver.
     */
    @SuppressLint("MissingPermission")
    fun fetchGpsLocation(
        context: Context,
        onSuccess: (LocationData) -> Unit,
        onError: (LocationError) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError(LocationError.PermissionDenied)
            return
        }

        if (!isLocationServiceEnabled(context)) {
            onError(LocationError.GpsDisabled)
            return
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val resolved = buildLocationFromCoordinates(
                        context = context,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isGps = true
                    )
                    onSuccess(resolved)
                } else {
                    // Fallback to lastLocation if current location returned null
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            val resolved = buildLocationFromCoordinates(
                                context = context,
                                latitude = lastLoc.latitude,
                                longitude = lastLoc.longitude,
                                isGps = true
                            )
                            onSuccess(resolved)
                        } else {
                            onError(LocationError.LocationUnavailable)
                        }
                    }.addOnFailureListener {
                        onError(LocationError.LocationUnavailable)
                    }
                }
            }
            .addOnFailureListener {
                // Try lastLocation as fallback
                fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        val resolved = buildLocationFromCoordinates(
                            context = context,
                            latitude = lastLoc.latitude,
                            longitude = lastLoc.longitude,
                            isGps = true
                        )
                        onSuccess(resolved)
                    } else {
                        onError(LocationError.LocationUnavailable)
                    }
                }.addOnFailureListener {
                    onError(LocationError.LocationUnavailable)
                }
            }
    }

    /**
     * Builds LocationData by strictly mapping coordinates to IANA geographical timezone,
     * resolving Geocoder locality when available.
     */
    fun buildLocationFromCoordinates(
        context: Context?,
        latitude: Double,
        longitude: Double,
        isGps: Boolean = false,
        nameOverride: String? = null,
        countryOverride: String? = null
    ): LocationData {
        val timeZoneId = GeoTimezoneResolver.resolveTimezoneId(latitude, longitude)

        var cityName = nameOverride
        var countryName = countryOverride

        if (cityName.isNullOrBlank() && context != null) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    countryName = addr.countryName
                }
            } catch (e: Exception) {
                // Geocoder may be offline or unavailable, fallback gracefully
            }
        }

        val finalName = if (!cityName.isNullOrBlank()) cityName else String.format(Locale.US, "%.2f°, %.2f°", latitude, longitude)
        val finalCountry = countryName ?: ""

        return LocationData(
            name = finalName,
            country = finalCountry,
            latitude = latitude,
            longitude = longitude,
            timeZoneId = timeZoneId,
            isGpsDerived = isGps,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Detects whether user traveled across geographical timezone boundaries.
     */
    fun detectTravelAcrossTimezone(
        previousLocation: LocationData?,
        newLocation: LocationData
    ): TimezoneCrossingEvent? {
        if (previousLocation == null) return null

        val distKm = calculateDistanceKm(
            previousLocation.latitude, previousLocation.longitude,
            newLocation.latitude, newLocation.longitude
        )

        val isDifferentTz = previousLocation.timeZoneId != newLocation.timeZoneId
        val isSignificantTravel = distKm >= 20.0 || isDifferentTz

        return if (isSignificantTravel) {
            TimezoneCrossingEvent(
                oldTimezone = previousLocation.timeZoneId,
                newTimezone = newLocation.timeZoneId,
                distanceKm = distKm,
                isSignificantTravel = isSignificantTravel
            )
        } else {
            null
        }
    }

    /**
     * Calculates spherical great-circle distance between two coordinates in kilometers.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Search any city, country, or location worldwide using Android Geocoder and global offline index.
     * Completely removes any 10-city restriction.
     */
    fun searchWorldwideLocations(context: Context, query: String): List<LocationData> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return PopularGlobalLocations

        val results = mutableListOf<LocationData>()

        // 1. Try Android Geocoder for any address worldwide
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(trimmed, 8)
            if (!addresses.isNullOrEmpty()) {
                for (addr in addresses) {
                    val name = addr.locality ?: addr.featureName ?: addr.subAdminArea ?: trimmed
                    val country = addr.countryName ?: ""
                    val tz = GeoTimezoneResolver.resolveTimezoneId(addr.latitude, addr.longitude)
                    results.add(
                        LocationData(
                            name = name,
                            country = country,
                            latitude = addr.latitude,
                            longitude = addr.longitude,
                            timeZoneId = tz,
                            isGpsDerived = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Geocoder unavailable or network error, fallback to offline filter
        }

        // 2. Also match from offline popular list
        val offlineMatches = PopularGlobalLocations.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
            it.country.contains(trimmed, ignoreCase = true) ||
            it.timeZoneId.contains(trimmed, ignoreCase = true)
        }

        for (match in offlineMatches) {
            if (results.none { it.name.equals(match.name, ignoreCase = true) }) {
                results.add(match)
            }
        }

        return results
    }
}
