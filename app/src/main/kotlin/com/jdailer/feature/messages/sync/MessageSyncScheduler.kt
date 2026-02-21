package com.jdailer.feature.messages.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MessageSyncScheduler {
    private const val WORK_NAME = "message_sync_worker"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<SmsMmsSyncWorker>(
            15,
            TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
