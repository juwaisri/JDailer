package com.jdailer.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jdailer.core.common.result.AppResult
import com.jdailer.feature.contacts.domain.repository.ContactsRepository
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber

class ContactsSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val contactsRepository: ContactsRepository by lazy { get(ContactsRepository::class.java) }

    override suspend fun doWork(): Result {
        return when (contactsRepository.syncFromDevice()) {
            is AppResult.Error -> {
                Timber.e("Contact sync failed")
                Result.retry()
            }
            is AppResult.Success -> {
                Result.success()
            }
            else -> Result.success()
        }
    }
}
