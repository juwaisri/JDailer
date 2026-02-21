package com.jdailer.feature.messages.domain.usecase

import androidx.paging.PagingData
import com.jdailer.feature.messages.domain.model.UnifiedMessageItem
import com.jdailer.feature.messages.domain.repository.UnifiedMessageRepository
import kotlinx.coroutines.flow.Flow

class ObserveMessageItemsUseCase(
    private val repository: UnifiedMessageRepository
) {
    operator fun invoke(
        threadId: String,
        pageSize: Int = 40
    ): Flow<PagingData<UnifiedMessageItem>> = repository.observeThreadItems(threadId, pageSize)
}
