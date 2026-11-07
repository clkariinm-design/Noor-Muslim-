package com.example.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.AppPreferences
import com.example.data.model.CityLocation
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerTimesCalculator
import java.util.Calendar
import java.util.TimeZone

/**
 * Keeps the next occurrence of each enabled prayer scheduled.
 * Exact alarms are used when Android grants access; otherwise an inexact idle-safe
 * alarm is used and the app explains the limitation in its prayer settings.
 */
object AdhanScheduler {
    private val prayers = listOf(
        PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR,
        PrayerType.MAGHRIB, PrayerType.ISHA
    )

    fun scheduleAll(context: Context, city: CityLocation? = null) {
        val app = context.applicationContext
        val prefs = AppPreferences(app)
        AdhanPlaybackService.prefetchAll(app)
        val location = city ?: prefs.getSelectedCity()
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val zone = TimeZone.getTimeZone(location.timeZoneId)
        val now = Calendar.getInstance(zone)

        prayers.forEach { prayer ->
            val requestCode = requestCode(prayer)
            val pending = pendingIntent(app, prayer, requestCode)
            if (!prefs.isAdhanEnabled(prayer)) {
                alarmManager.cancel(pending)
                return@forEach
            }

            val candidates = (0..1).map { offset ->
                val date = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
                val calculated = PrayerTimesCalculator.calculate(
                    location.latitude, location.longitude, location.timeZoneId, date,
                    prefs.getCalculationMethod(), prefs.getJuristicMethod()
                )
                val item = when (prayer) {
                    PrayerType.FAJR -> calculated.fajr
                    PrayerType.DHUHR -> calculated.dhuhr
                    PrayerType.ASR -> calculated.asr
                    PrayerType.MAGHRIB -> calculated.maghrib
                    PrayerType.ISHA -> calculated.isha
                    PrayerType.SUNRISE -> return@map null
                }
                (date.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, item.hour)
                    set(Calendar.MINUTE, item.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }.filterNotNull()

            val next = candidates.firstOrNull { it.timeInMillis > now.timeInMillis } ?: return@forEach
            val triggerAt = next.timeInMillis
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        }
    }

    private fun requestCode(prayer: PrayerType): Int = 7100 + prayer.ordinal

    private fun pendingIntent(context: Context, prayer: PrayerType, requestCode: Int): PendingIntent {
        val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            action = ACTION_ADHAN
            putExtra(EXTRA_PRAYER, prayer.name)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    const val ACTION_ADHAN = "com.example.noormuslim.PLAY_ADHAN"
    const val EXTRA_PRAYER = "prayer_type"
}
