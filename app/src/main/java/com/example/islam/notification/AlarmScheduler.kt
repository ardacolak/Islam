package com.example.islam.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.timeFor
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Schedules exact alarms for all prayers in the given PrayerTime */
    fun schedulePrayerAlarms(prayerTime: PrayerTime) {
        Prayer.entries.forEach { prayer ->
            val timeStr = prayerTime.timeFor(prayer)
            scheduleSingleAlarm(prayer, timeStr)
        }
    }

    private fun scheduleSingleAlarm(prayer: Prayer, timeStr: String) {
        val parts = timeStr.split(":")
        if (parts.size < 2) return

        val triggerCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, skip
        if (triggerCal.before(Calendar.getInstance())) return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayer.turkishName)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, timeStr)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayer.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerCal.timeInMillis,
            pendingIntent
        )
    }

    /** Cancels all prayer alarms */
    fun cancelAllAlarms() {
        Prayer.entries.forEach { prayer ->
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.ordinal,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
