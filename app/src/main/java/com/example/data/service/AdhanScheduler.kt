package com.example.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppPreferences
import com.example.data.model.CityLocation
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerTimesCalculator
import java.util.Calendar
import java.util.TimeZone

/** Rebuilds the next alarm for each enabled obligatory prayer. */
object AdhanScheduler {
    private val prayers = listOf(
        PrayerType.FAJR,
        PrayerType.DHUHR,
        PrayerType.ASR,
        PrayerType.MAGHRIB,
        PrayerType.ISHA
    )

    fun scheduleAll(context: Context, city: CityLocation? = null) {
        val app = context.applicationContext
        val prefs = AppPreferences(app)
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val location = city ?: prefs.getSelectedCity()
        val zone = TimeZone.getTimeZone(location.timeZoneId)
        val now = Calendar.getInstance(zone)

        prayers.forEach { prayer ->
            val pending = pendingIntent(app, prayer)
            if (!prefs.isPrayerNotificationsEnabled() || !prefs.isAdhanEnabled(prayer)) {
                alarmManager.cancel(pending)
                return@forEach
            }

            try {
                val next = (0..2).asSequence()
                    .mapNotNull { dayOffset ->
                        val date = (now.clone() as Calendar).apply {
                            add(Calendar.DAY_OF_YEAR, dayOffset)
                        }
                        val calculated = PrayerTimesCalculator.calculate(
                            location.latitude,
                            location.longitude,
                            location.timeZoneId,
                            date,
                            prefs.getCalculationMethod(),
                            prefs.getJuristicMethod()
                        )
                        val item = when (prayer) {
                            PrayerType.FAJR -> calculated.fajr
                            PrayerType.DHUHR -> calculated.dhuhr
                            PrayerType.ASR -> calculated.asr
                            PrayerType.MAGHRIB -> calculated.maghrib
                            PrayerType.ISHA -> calculated.isha
                            PrayerType.SUNRISE -> null
                        } ?: return@mapNotNull null

                        (date.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, item.hour)
                            set(Calendar.MINUTE, item.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.takeIf { it.timeInMillis > now.timeInMillis }
                    }
                    .firstOrNull()

                if (next == null) {
                    alarmManager.cancel(pending)
                    Log.w(TAG, "No future time found for $prayer")
                    return@forEach
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        next.timeInMillis,
                        pending
                    )
                } else {
                    // Fallback keeps a reminder scheduled if exact-alarm access is unavailable.
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        next.timeInMillis,
                        pending
                    )
                }
                Log.d(TAG, "Scheduled $prayer at ${next.time}")
            } catch (error: Exception) {
                Log.e(TAG, "Failed to schedule $prayer", error)
            }
        }
    }

    private fun pendingIntent(context: Context, prayer: PrayerType): PendingIntent {
        val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            action = ACTION_ADHAN
            putExtra(EXTRA_PRAYER, prayer.name)
        }
        return PendingIntent.getBroadcast(
            context,
            7100 + prayer.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private const val TAG = "NoorAdhanScheduler"
    const val ACTION_ADHAN = "com.example.noormuslim.PLAY_ADHAN"
    const val EXTRA_PRAYER = "prayer_type"
}
