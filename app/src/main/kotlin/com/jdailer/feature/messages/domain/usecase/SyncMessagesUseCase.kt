package com.jdailer.feature.messages.domain.usecase

import com.jdailer.feature.messages.domain.repository.UnifiedMessageRepository
import com.jdailer.feature.messages.domain.model.SmsMmsSyncResult
import com.jdailer.feature.messages.domain.model.SmsMmsSyncRequest

class SyncMessagesUseCase(
    private val repository: UnifiedMessageRepository
) {
    suspend operator fun invoke(limitPerType: Int = 250): Result<SmsMmsSyncResult> =
        repository.syncFromDevice(limitPerType)

    suspend fun syncWithPolicy(
        request: SmsMmsSyncRequest
    ): Result<SmsMmsSyncResult> = repository.syncFromDevice(
        limitPerType = request.limitPerType.coerceIn(25, 1000)
    ).fold(
        onSuccess = { result ->
            val safeRequest = request
            if (!safeRequest.refreshRcsCapability && result.hasRcsCapability) {
                Result.success(result.copy(hasRcsCapability = false))
            } else {
                Result.success(result)
            }
        },
        onFailure = { Result.failure(it) }
    )
}
