package com.example.islam.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.islam.worker.PrayerTimeUpdateWorker

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Re-schedule the daily prayer time update worker after reboot
            val request = OneTimeWorkRequestBuilder<PrayerTimeUpdateWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                PrayerTimeUpdateWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
