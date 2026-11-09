package com.example.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AppPreferences
import com.example.data.model.PrayerType

class AdhanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prayerName = intent?.getStringExtra(AdhanScheduler.EXTRA_PRAYER) ?: return
        val prayer = runCatching { PrayerType.valueOf(prayerName) }.getOrNull() ?: return
        val prefs = AppPreferences(context)

        // Always calculate and set the following occurrence, even if this prayer is disabled.
        AdhanScheduler.scheduleAll(context)

        if (!prefs.isPrayerNotificationsEnabled() || !prefs.isAdhanEnabled(prayer)) return

        val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
            action = AdhanPlaybackService.ACTION_PLAY
            putExtra(AdhanScheduler.EXTRA_PRAYER, prayer.name)
        }
        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (error: Exception) {
            Log.e("NoorAdhan", "Could not start Adhan service for $prayer", error)
        }
    }
}

class AdhanBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_DATE_CHANGED -> AdhanScheduler.scheduleAll(context)
        }
    }
}
