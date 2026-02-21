package com.jdailer.feature.messages.domain.usecase

import com.jdailer.feature.messages.domain.model.SmsMmsSyncRequest
import com.jdailer.feature.messages.domain.model.SmsMmsSyncResult

private const val MIN_LIMIT = 25
private const val MAX_LIMIT = 2000

data class SmsSyncPolicy(
    val minLimitPerType: Int = MIN_LIMIT,
    val maxLimitPerType: Int = MAX_LIMIT,
    val allowThrottling: Boolean = true,
    val throttleWindowMs: Long = 250L,
    val refreshRcsWhenUnknown: Boolean = true
)

class SyncMessagesWithPolicyUseCase(
    private val syncMessagesUseCase: SyncMessagesUseCase
) {
    suspend operator fun invoke(
        request: SmsMmsSyncRequest = SmsMmsSyncRequest(),
        policy: SmsSyncPolicy = SmsSyncPolicy()
    ): Result<SmsMmsSyncResult> {
        val safeLimit = request.limitPerType.coerceIn(policy.minLimitPerType, policy.maxLimitPerType)
        val safeRequest = request.copy(
            limitPerType = safeLimit,
            refreshRcsCapability = request.refreshRcsCapability || policy.refreshRcsWhenUnknown
        )

        return syncMessagesUseCase.syncWithPolicy(safeRequest).let { syncResult ->
            if (policy.allowThrottling && safeLimit >= policy.maxLimitPerType && policy.throttleWindowMs > 0) {
                kotlinx.coroutines.delay(policy.throttleWindowMs)
            }
            syncResult
        }
    }
}
