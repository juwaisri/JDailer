package com.jdailer.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jdailer.feature.messages.sync.MessageSyncScheduler
import java.util.concurrent.TimeUnit

object CommunicationsSyncScheduler {
    private const val WORK_NAME = "communications_hub_sync"

    fun schedule(context: Context) {
        scheduleContacts(context)
        MessageSyncScheduler.schedule(context)
    }

    private fun scheduleContacts(context: Context) {
        val request = PeriodicWorkRequestBuilder<ContactsSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
