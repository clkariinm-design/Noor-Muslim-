package com.example.data.remote

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RemoteAppConfigTest {
    @Test
    fun parsesMaintenanceAnnouncementAndFeatureFlags() {
        val config = RemoteAppConfig.fromJson(JSONObject("""{"app":{"latest_version":"1.2.0"},"minimum_supported_version":"1.1.0","maintenance":{"enabled":true,"message":"Dayactir ayaa socda"},"announcement":{"enabled":true,"title":"Cusub","message":"Quran search"},"features":{"qibla":false,"ai_assistant":false}}"""))
        assertEquals("1.2.0", config.latestVersion)
        assertEquals("1.1.0", config.minimumSupportedVersion)
        assertTrue(config.maintenanceEnabled)
        assertEquals("Dayactir ayaa socda", config.maintenanceMessage)
        assertTrue(config.announcementEnabled)
        assertEquals("Cusub", config.announcementTitle)
        assertFalse(config.features.getValue("qibla"))
        assertFalse(config.features.getValue("ai_assistant"))
        assertTrue(config.features.getValue("duas"))
    }

    @Test
    fun missingFieldsUseSafeDefaults() {
        val config = RemoteAppConfig.fromJson(JSONObject())
        assertFalse(config.maintenanceEnabled)
        assertFalse(config.announcementEnabled)
        assertTrue(config.features.getValue("quran_search"))
        assertFalse(config.features.getValue("ai_assistant"))
    }
}