package com.example.data.service

/** Small, platform-version-aware policy for requesting notification permission. */
object PrayerPermissionPolicy {
    fun shouldRequestPostNotifications(sdkInt: Int, alreadyGranted: Boolean): Boolean =
        sdkInt >= 33 && !alreadyGranted
}
