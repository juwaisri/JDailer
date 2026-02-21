package com.jdailer.feature.history.domain.repository

import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.history.domain.model.UnifiedHistoryItem
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface UnifiedHistoryRepository {
    fun observeHistory(
        sources: Set<HistorySource> = emptySet(),
        limit: Int = 100
    ): Flow<PagingData<UnifiedHistoryItem>>
}
