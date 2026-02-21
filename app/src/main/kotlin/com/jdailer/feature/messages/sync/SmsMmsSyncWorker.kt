package com.jdailer.feature.messages.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jdailer.feature.messages.domain.model.SmsMmsSyncRequest
import com.jdailer.feature.messages.domain.usecase.SyncMessagesWithPolicyUseCase
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber

class SmsMmsSyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val syncMessagesUseCase: SyncMessagesWithPolicyUseCase by lazy {
        get(SyncMessagesWithPolicyUseCase::class.java)
    }

    override suspend fun doWork(): Result {
        return runCatching {
            syncMessagesUseCase(
                SmsMmsSyncRequest(forceFromDevice = true, refreshRcsCapability = true)
            )
        }.fold(
            onSuccess = { result ->
                if (result.isSuccess) {
                    val summary = result.getOrNull()
                    Timber.i(
                        "SMS/MMS sync worker completed: messages=%s threads=%s rcs=%s",
                        summary?.syncedMessages ?: 0,
                        summary?.syncedThreads ?: 0,
                        summary?.hasRcsCapability ?: false
                    )
                    Result.success()
                } else {
                    Timber.w("SMS/MMS sync worker retrying")
                    Result.retry()
                }
            },
            onFailure = {
                Timber.e("SMS/MMS sync worker failed: ${it.message}")
                Result.retry()
            }
        )
    }
}
