package com.jdailer.feature.history.domain.usecase

import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.history.domain.model.UnifiedHistoryItem
import com.jdailer.feature.history.domain.repository.UnifiedHistoryRepository
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

class GetUnifiedHistoryUseCase(
    private val repository: UnifiedHistoryRepository
) {
    operator fun invoke(
        sources: Set<HistorySource> = emptySet(),
        limit: Int = 40
    ): Flow<PagingData<UnifiedHistoryItem>> = repository.observeHistory(sources, limit)
}
