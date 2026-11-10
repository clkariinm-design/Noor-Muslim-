package com.example.data.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerPermissionPolicyTest {
    @Test
    fun notificationPermissionIsNotRequiredBeforeAndroid13() {
        assertFalse(PrayerPermissionPolicy.shouldRequestPostNotifications(32, false))
    }

    @Test
    fun notificationPermissionIsRequestedOnAndroid13WhenNotGranted() {
        assertTrue(PrayerPermissionPolicy.shouldRequestPostNotifications(33, false))
    }

    @Test
    fun notificationPermissionIsNotRequestedWhenAlreadyGranted() {
        assertFalse(PrayerPermissionPolicy.shouldRequestPostNotifications(36, true))
    }
}
