package com.jdailer.feature.messages.domain.usecase

import androidx.paging.PagingData
import com.jdailer.core.database.enums.HistorySource
import com.jdailer.feature.messages.domain.model.UnifiedMessageThread
import com.jdailer.feature.messages.domain.repository.UnifiedMessageRepository
import kotlinx.coroutines.flow.Flow

class ObserveMessageThreadsUseCase(
    private val repository: UnifiedMessageRepository
) {
    operator fun invoke(
        query: String? = null,
        sources: Set<HistorySource> = setOf(HistorySource.SMS, HistorySource.MMS),
        pageSize: Int = 30
    ): Flow<PagingData<UnifiedMessageThread>> =
        repository.observeThreads(query, sources, pageSize)
}
